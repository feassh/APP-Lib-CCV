package ceneax.lib.core;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ceneax.lib.util.FileUtil;

/**
 * @Description: 检测功能类
 * @Date: 2020/11/12 17:59
 * @Author: ceneax
 */
public class Detector {

    // OpenCV 人脸分类器
    private static CascadeClassifier mCasFace;
    // Dlib 人脸数据检测提取类
    private static FaceDet mFaceDet;

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
        mFaceDet = new FaceDet(FileUtil.assetsToFile(context, "shape_predictor_68_face_landmarks.dat"));
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
     * Dlib人脸检测, 传入 [path] 图片路径
     */
    @Nullable
    @WorkerThread
    public static List<VisionDetRet> detect(@NonNull String path) {
        return mFaceDet.detect(path);
    }

    /**
     * Dlib人脸检测, 传入 [bitmap] 对象
     */
    @Nullable
    @WorkerThread
    public static List<VisionDetRet> detect(@NonNull Bitmap bitmap) {
        return mFaceDet.detect(bitmap);
    }

    /**
     * Dlib人脸检测, 传入 [mat] 对象
     */
    @Nullable
    @WorkerThread
    public static List<VisionDetRet> detect(@NonNull Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(mat, bitmap);
        List<VisionDetRet> list = detect(bitmap);
        bitmap.recycle();
        return list;
    }

}
