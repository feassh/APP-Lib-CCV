package ceneax.lib.ccv.core;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import cenax.dlib.Dlib;
import ceneax.lib.ccv.util.FileUtil;

/**
 * @Description: 检测功能类
 * @Date: 2020/11/12 17:59
 * @Author: ceneax
 */
public class Detector {

    // OpenCV 人脸分类器
    private static CascadeClassifier mCasFace;

    // 人脸对比 欧氏距离 阈值
    public static float THRESHOLD = 0.6f;

    /**
     * 私有化 构造方法
     */
    private Detector() { }

    /**
     * 初始化
     */
    public static void init(Context context) {
        // 初始化OpenCV
        OpenCVLoader.initDebug();

        // 初始化分类器、检测器
        mCasFace = new CascadeClassifier();
        mCasFace.load(FileUtil.assetsToFile(context, "lbpcascade_frontalface.xml"));
        Dlib.loadModel(FileUtil.assetsToFile(context, "shape_predictor_68_face_landmarks.dat"));
        Dlib.loadRecgModel(FileUtil.assetsToFile(context, "dlib_face_recognition_resnet_model_v1.dat"));
    }

    /**
     * OpenCV检测人脸, 传入 [mat] 和 [faces], 结果会保存到 [faces] 中
     */
    @WorkerThread
    public static void detectFaces(Mat mat, MatOfRect faces) {
        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGRA2GRAY);
        mCasFace.detectMultiScale(gray, faces);
    }


    /**
     * Dlib人脸检测, 传入 [bitmap] 对象
     */
    @WorkerThread
    public static float[] recgFace(@NonNull Bitmap bitmap) {
        return Dlib.recg(bitmap);
    }

    /**
     * Dlib人脸检测, 传入 [mat] 对象
     */
    @WorkerThread
    public static float[] recgFace(@NonNull Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(mat, bitmap);
        float[] res = recgFace(bitmap);
        bitmap.recycle();
        return res;
    }

    /**
     * 传入一组人脸128D数据，进行对比
     */
    @WorkerThread
    public static float compute(float[] src, float[] tar) {
        return Dlib.compute(src, tar);
    }

}
