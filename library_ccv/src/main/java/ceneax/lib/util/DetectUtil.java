package ceneax.lib.util;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @Description: 检测功能类
 * @Date: 2020/11/12 17:59
 * @Author: ceneax
 */
public class DetectUtil {

    private static DetectUtil instance;

    private CascadeClassifier casFace;
    private CascadeClassifier casEye;
    private CascadeClassifier casGlasses;
    private FaceDet faceDet;

    public DetectUtil() {
        casFace = new CascadeClassifier();
        casEye = new CascadeClassifier();
        casGlasses = new CascadeClassifier();
    }

    public synchronized static DetectUtil getInstance() {
        if (instance == null) {
            instance = new DetectUtil();
        }
        return instance;
    }

    public DetectUtil init(Context context) {
        File file = null;
        try {
            InputStream inputStream = context.getResources().getAssets().open("lbpcascade_frontalface.xml");
            file = new File(context.getExternalCacheDir(), "lbpcascade_frontalface.xml");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int byteRead;
            while ((byteRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteRead);
            }
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        casFace.load(file.getAbsolutePath());
        file.delete();

        File file2 = null;
        try {
            InputStream inputStream = context.getResources().getAssets().open("haarcascade_eye.xml");
            file2 = new File(context.getExternalCacheDir(), "haarcascade_eye.xml");
            FileOutputStream outputStream = new FileOutputStream(file2);
            byte[] buffer = new byte[4096];
            int byteRead;
            while ((byteRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteRead);
            }
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        casEye.load(file2.getAbsolutePath());
        file2.delete();

        File file3 = null;
        try {
            InputStream inputStream = context.getResources().getAssets().open("haarcascade_smile.xml");
            file3 = new File(context.getExternalCacheDir(), "haarcascade_smile.xml");
            FileOutputStream outputStream = new FileOutputStream(file3);
            byte[] buffer = new byte[4096];
            int byteRead;
            while ((byteRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteRead);
            }
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        casGlasses.load(file3.getAbsolutePath());
        file3.delete();

        File file4 = new File(context.getExternalCacheDir(), "shape_predictor_68_face_landmarks.dat"); // dlib_face_recognition_resnet_model_v1.dat
        if (!file4.exists()) {
            try {
                InputStream inputStream = context.getResources().getAssets().open("shape_predictor_68_face_landmarks.dat");
                FileOutputStream outputStream = new FileOutputStream(file4);
                byte[] buffer = new byte[4096];
                int byteRead;
                while ((byteRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, byteRead);
                }
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        faceDet = new FaceDet(file4.getAbsolutePath());

        return this;
    }

    public void detectFaces(Mat mat, MatOfRect faces) {
        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGRA2GRAY);
        casFace.detectMultiScale(gray, faces);
    }

    public void detectEyes(Mat mat, MatOfRect eyes) {
        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGRA2GRAY);
        casEye.detectMultiScale(gray, eyes);
    }

    public void detectGlasses(Mat mat, MatOfRect glasses) {
        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGRA2GRAY);
        casGlasses.detectMultiScale(gray, glasses);
    }

    @Nullable
    @WorkerThread
    public List<VisionDetRet> detect(@NonNull String path) {
        return faceDet.detect(path);
    }

    @Nullable
    @WorkerThread
    public List<VisionDetRet> detect(@NonNull Bitmap bitmap) {
        return faceDet.detect(bitmap);
    }

}
