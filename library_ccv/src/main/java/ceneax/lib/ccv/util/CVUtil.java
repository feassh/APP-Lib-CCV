package ceneax.lib.ccv.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.util.List;

import cenax.dlib.Dlib;
import ceneax.lib.ccv.bean.CompareRes;

/**
 * @Description: CV 工具类
 * @Date: 2020/11/12 12:09
 * @Author: ceneax
 */
public class CVUtil {

    /**
     * 图像旋转, 将 [src] 旋转指定 [angle] 角度后, 产生新的 Mat, 存放到 [dst] 中
     */
    public static void  rotate(Mat src, Mat dst, int angle) {
        switch (angle) {
            case 90:
                Core.transpose(src, dst);
                Core.flip(src, dst, 1);
                break;
            case 180:
                Core.flip(src, dst, 0);
                Core.flip(src, dst, 1);
                break;
            case 270:
            case -90:
                Core.transpose(src, dst);
                Core.flip(src, dst, 0);
                break;
        }
    }

    /**
     * 将NV21图像数据逆时针旋转90度
     */
    public static byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];

        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }

        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth)
                        + (x - 1)];
                i--;
            }
        }

        return yuv;
    }

    /**
     * 将NV21图像数据逆时针旋转270度并镜像处理
     */
    public static byte[] rotateYUVDegree270AndMirror(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];

        // 将 Y 进行旋转和镜像处理
        int i = 0;
        int maxY = 0;
        for (int x = imageWidth - 1; x >= 0; x--) {
            maxY = imageWidth * (imageHeight - 1) + x * 2;
            for (int y = 0; y < imageHeight; y++) {
                yuv[i] = data[maxY - (y * imageWidth + x)];
                i++;
            }
        }

        // 将 U V 进行旋转和镜像处理
        int uvSize = imageWidth * imageHeight;
        i = uvSize;
        int maxUV = 0;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            maxUV = imageWidth * (imageHeight / 2 - 1) + x * 2 + uvSize;
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[maxUV - 2 - (y * imageWidth + x - 1)];
                i++;
                yuv[i] = data[maxUV - (y * imageWidth + x)];
                i++;
            }
        }

        return yuv;
    }

    /**
     * 将NV21图像数据转换为Bitmap
     */
    public static Bitmap nv21ToBitmap(byte[] data, int width, int height) {
        BitmapFactory.Options newOptions =new BitmapFactory.Options();
        newOptions.inJustDecodeBounds = true;
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, baos); // JPG图片的质量[0-100], 100最高
        byte[] rawImage = baos.toByteArray();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);

        return bitmap;
    }

    /**
     * 将 RGB色彩的 [src] 转换为灰度的 [dst]
     */
    public static void mat2gray(Mat src, Mat dst) {
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGRA2GRAY);
    }

    /**
     * 特征对比：欧氏距离(欧几里得度量 Euclidean Distance) 对比法
     * 计算结果越趋近于0表示越相似
     */
    @Deprecated
    public static double compareED(List<Double> src, List<Double> target) {
        double sum = 0;

        for (int i = 0; i < src.size(); i ++) {
            sum += (src.get(i) - target.get(i)) * (src.get(i) - target.get(i));
        }

        return Math.sqrt(sum);
    }

    @Deprecated
    public static CompareRes compareEDs(List<Double> src, List<List<Double>> target) {
        // 初始化索引为-1
        // 如果最后返回结果为-1，则表示未匹配到相似数据
        CompareRes compareRes = new CompareRes(-1, 1000);

        for (int i = 0; i < target.size(); i ++) {
            double com = compareED(src, target.get(i));
            if (com <= compareRes.getValue()) {
                compareRes.setIndex(i);
                compareRes.setValue(com);
            }
        }

        return compareRes;
    }

    /**
     * 传入一组人脸128D数据，进行对比
     */
    public static float compute(float[] src, float[] tar) {
        return Dlib.compute(src, tar);
    }

    /**
     * 传入多组人脸128D数据，进行对比
     */
    public static CompareRes compute(float[] src, List<float[]> tar) {
        CompareRes compareRes = new CompareRes(-1, 9999);

        for (int i = 0; i < tar.size() - 1; i ++) {
            float curValue = compute(src, tar.get(i));
            if (curValue < compareRes.getValue()) {
                compareRes.setIndex(i);
                compareRes.setValue(curValue);
            }
        }

        return compareRes;
    }

}