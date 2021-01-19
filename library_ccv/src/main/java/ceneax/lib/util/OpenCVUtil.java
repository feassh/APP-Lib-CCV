package ceneax.lib.util;

import org.opencv.core.Core;
import org.opencv.core.Mat;

/**
 * @Description: OpenCV工具类
 * @Date: 2020/11/12 12:09
 * @Author: ceneax
 */
public class OpenCVUtil {

    /**
     * 图像旋转
     * @param src 源Mat
     * @param dst 修改后的Mat
     * @param angle 旋转角度
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

}