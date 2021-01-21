#include <jni.h>

#include <string>
#include <vector>
#include <mutex>

#include <android/log.h>
#include <android/bitmap.h>

#include <opencv2/opencv.hpp>
#include <opencv2/features2d.hpp>  // FAST
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/video/tracking.hpp>

#include <dlib/image_io.h>
#include <dlib/image_processing.h>
#include <dlib/image_processing/generic_image.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/opencv/cv_image.h>
#include <dlib/dnn.h>

#define LOG_TAG "native-lib"
#define LOGD(...) \
  ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

#define JNI_METHOD(NAME) \
    Java_ceneax_dlib_Native_##NAME

#define KERNEL_SIZE 5 // 3, 5, 7, 9

#define NV21 17
#define YV12 842094169
#define YUV_420_888 35
#define PYRAMIDS 3
#define MAX_FRAME_COUNT 5

using namespace std;

// global variables:
dlib::frontal_face_detector detector;
dlib::shape_predictor shape_predictor;
std::mutex _mutex;
int imageFormat = NV21;

// ----------------------------------------------------------------------------------------
// recg

template <template <int,template<typename>class,int,typename> class block, int N, template<typename>class BN, typename SUBNET>
using residual = dlib::add_prev1<block<N,BN,1,dlib::tag1<SUBNET>>>;

template <template <int,template<typename>class,int,typename> class block, int N, template<typename>class BN, typename SUBNET>
using residual_down = dlib::add_prev2<dlib::avg_pool<2,2,2,2,dlib::skip1<dlib::tag2<block<N,BN,2,dlib::tag1<SUBNET>>>>>>;

template <int N, template <typename> class BN, int stride, typename SUBNET>
using block  = BN<dlib::con<N,3,3,1,1,dlib::relu<BN<dlib::con<N,3,3,stride,stride,SUBNET>>>>>;

template <int N, typename SUBNET> using ares      = dlib::relu<residual<block,N,dlib::affine,SUBNET>>;
template <int N, typename SUBNET> using ares_down = dlib::relu<residual_down<block,N,dlib::affine,SUBNET>>;

template <typename SUBNET> using alevel0 = ares_down<256,SUBNET>;
template <typename SUBNET> using alevel1 = ares<256,ares<256,ares_down<256,SUBNET>>>;
template <typename SUBNET> using alevel2 = ares<128,ares<128,ares_down<128,SUBNET>>>;
template <typename SUBNET> using alevel3 = ares<64,ares<64,ares<64,ares_down<64,SUBNET>>>>;
template <typename SUBNET> using alevel4 = ares<32,ares<32,ares<32,SUBNET>>>;

using anet_type = dlib::loss_metric<dlib::fc_no_bias<128,dlib::avg_pool_everything<
                                             alevel0<
                                                     alevel1<
                                                             alevel2<
                                                                     alevel3<
                                                                             alevel4<
                                                                                     dlib::max_pool<3,3,2,2,dlib::relu<dlib::affine<dlib::con<32,7,7,2,2,
                                                                                             dlib::input_rgb_image_sized<150>
                                                                     >>>>>>>>>>>>;

anet_type net;
// ----------------------------------------------------------------------------------------

namespace CCV {
    // variables
    cv::Mat prev_img;
    vector<cv::Point2f> prev_pts;
    vector<cv::Point2f> next_pts;
    cv::TermCriteria criteria(cv::TermCriteria::COUNT | cv::TermCriteria::EPS, 25, 0.01);
    cv::Size ROI(20, 20);
}

/*
 * 旋转图像
 */
void rotateMat(cv::Mat &mat, int rotation) {
    if (rotation == 90) { // portrait
        LOGD("JNI: rotation 90");
        cv::transpose(mat, mat);
        cv::flip(mat, mat, -1);
    } else if (rotation == 0) { // landscape-left
        LOGD("JNI: rotation 0");
        cv::flip(mat, mat, 1);
    } else if (rotation == 180) { // landscape-right
        LOGD("JNI: rotation 180");
        cv::flip(mat, mat, 0);
    }
}

/*
 * 将Bitmap对象转换为Mat
 */
void ConvertBitmapToRGBAMat(JNIEnv* env, jobject& bitmap, cv::Mat& dst,
                            bool needUnPremultiplyAlpha) {
    AndroidBitmapInfo info;
    void* pixels = 0;

    try {
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                  info.format == ANDROID_BITMAP_FORMAT_RGB_565);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);
        dst.create(info.height, info.width, CV_8UC4);
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if (needUnPremultiplyAlpha)
                cvtColor(tmp, dst, cv::COLOR_mRGBA2RGBA);
            else
                tmp.copyTo(dst);
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cvtColor(tmp, dst, cv::COLOR_BGR5652RGBA);
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("org/opencv/core/CvException");
        if (!je)
            je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
        return;
    }
}

//--------------------------------------------------------------------------------------------------
extern "C"
JNIEXPORT void JNICALL
Java_cenax_dlib_Native_loadModel(JNIEnv *env, jclass clazz, jstring detectorPath) {
    try {
        const char *path = env->GetStringUTFChars(detectorPath, JNI_FALSE);

        _mutex.lock();

        // 初始化人脸检测器
        detector = dlib::get_frontal_face_detector();

        // load the shape predictor
        dlib::deserialize(path) >> shape_predictor;
        _mutex.unlock();

        env->ReleaseStringUTFChars(detectorPath, path); //free mem
        LOGD("JNI: model loaded");

    } catch (dlib::serialization_error &e) {
        LOGD("JNI: failed to model -> %s", e.what());
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_cenax_dlib_Native_loadRecgModel(JNIEnv *env, jclass clazz, jstring recgPath) {
    try {
        const char *path = env->GetStringUTFChars(recgPath, JNI_FALSE);

        _mutex.lock();
        dlib::deserialize(path) >> net;
        _mutex.unlock();

        env->ReleaseStringUTFChars(recgPath, path); //free mem
        LOGD("JNI: model loaded");

    } catch (dlib::serialization_error &e) {
        LOGD("JNI: failed to model -> %s", e.what());
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_cenax_dlib_Native_setImageFormat(JNIEnv *env, jclass clazz, jint format) {
    imageFormat = format;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_cenax_dlib_Native_detectLandmarks(JNIEnv *env, jclass clazz, jobject bitmap) {
    cv::Mat rgbaMat, bgrMat;

    // 将Bitmap转成Mat
    ConvertBitmapToRGBAMat(env, bitmap, rgbaMat, true);
    // Mat转为BGR
    cv::cvtColor(rgbaMat, bgrMat, cv::COLOR_RGBA2BGR);

    if (bgrMat.empty()) {
        return nullptr;
    }
    if (bgrMat.channels() == 1) {
        cv::cvtColor(bgrMat, bgrMat, cv::COLOR_GRAY2BGR);
    }

    // 将Mat转为dlib image
    dlib::cv_image<dlib::bgr_pixel> img(bgrMat);

    // 检测人脸
    std::vector<dlib::rectangle> faceRects = detector(img);
    if (faceRects.empty()) {
        return nullptr;
    }

    // 只取一张人脸
    dlib::rectangle faceRect = faceRects[0];

    // 获取68点
    dlib::full_object_detection shape = shape_predictor(img, faceRect);

    // 128D
    dlib::matrix<dlib::rgb_pixel> face_chip;
    extract_image_chip(img, get_face_chip_details(shape,150,0.25), face_chip);
    dlib::matrix<float,0,1> face_descriptor = net(face_chip);

    // 返回
    jfloatArray jfa = env->NewFloatArray(face_descriptor.nr());
    jfloat jf[face_descriptor.nr()];
    for (int i = 0; i < face_descriptor.nr(); ++i) {
        jf[i] = face_descriptor(i, 0);
    }
    env->SetFloatArrayRegion(jfa, 0, face_descriptor.nr(), jf);

    return jfa;

//    // copy content of frame into image
//    jbyte *data = env->GetByteArrayElements(yuvFrame, 0);
//
//    // convert yuv-frame to cv::Mat
//    cv::Mat yuvMat(height + height / 2, width, CV_8UC1, (unsigned char *) data);
//    cv::Mat grayMat(height, width, CV_8UC1);
//
//    // to grayscale
//    if (imageFormat == NV21)
//        cv::cvtColor(yuvMat, grayMat, cv::COLOR_YUV2GRAY_NV21);  // was CV_YUV2GRAY_NV21
//    else if (imageFormat == YV12)
//        cv::cvtColor(yuvMat, grayMat, cv::COLOR_YUV2GRAY_YV12);  // was CV_YUV2GRAY_YV12
//
//    // adjust rotation according to phone orientation
//    rotateMat(grayMat, rotation);
//
//    // crop face for enhancements
//    cv::Rect faceROI(left, top, right - left, bottom - top);
//    cv::Mat face = grayMat(faceROI);

//    // apply filters
//    cv::medianBlur(face, face, KERNEL_SIZE);  // remove noise
//    cv::equalizeHist(face, face);  // improve contrast
//
//    // -- DETECT LANDMARKS -- //
//
//    // cv::mat to dlib::image
//    dlib::cv_image<unsigned char> image(grayMat);
//
//    // detect landmark points
//    _mutex.lock();
//    dlib::rectangle region(left, top, right, bottom);
//    dlib::full_object_detection points = shape_predictor(image, region);
//    _mutex.unlock();
//
//    // result
//    auto num_points = points.num_parts();
//    jsize len = (jsize) (num_points * sizeof(short)); // num_points * 2
//
//    jlong buffer[len];
//    jlongArray result = env->NewLongArray(len);
//
//    // copy points in the buffer
//    auto k = 0;
//    for (unsigned long i = 0l; i < num_points; ++i) {
//        dlib::point p = points.part(i);
//        buffer[k++] = p.x();
//        buffer[k++] = p.y();
//    }
//
//    // set the content of buffer into result array
//    env->SetLongArrayRegion(result, 0, len, buffer);
//
//    // free mem
//    env->ReleaseByteArrayElements(yuvFrame, data, 0);
//
//    // uncomment to enable tracking for the next frames
////        LK::start(grayMat, points);
//
//    return result;
}