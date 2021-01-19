package ceneax.lib.util;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import ceneax.lib.bean.CompareRes;

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
     * 将 RGB色彩的 [src] 转换为灰度的 [dst]
     */
    public static void mat2gray(Mat src, Mat dst) {
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGRA2GRAY);
    }

    /**
     * 特征对比：欧式距离(欧几里得度量 Euclidean Distance) 对比法
     * 计算结果越趋近于0表示越相似
     */
    public static double compareED(List<Double> src, List<Double> target) {
        double sum = 0;

        for (int i = 0; i < src.size(); i ++) {
            sum += (src.get(i) - target.get(i)) * (src.get(i) - target.get(i));
        }

        return Math.sqrt(sum);
    }

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

}