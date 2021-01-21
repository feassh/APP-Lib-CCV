package ceneax.lib.ccv.util;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

/**
 * @Description: 对象转换 工具类
 * @Date: 2021/1/19 14:09
 * @Author: ceneax
 */
public class ObjectUtil {

    /**
     * 判断 [large] 矩形 是否包含 [small] 矩形
     */
    public static boolean isContainsOtherRect(Rect small, Rect large) {
        return small.tl().inside(large) && small.br().inside(large);
    }

    /**
     * 将 MatOfPoint 对象 转换成 MatOfPoint2f 对象
     */
    public static MatOfPoint2f matOfPointToMatOfPoint2f(MatOfPoint matOfPoint) {
        return new MatOfPoint2f(matOfPoint.toArray());
    }

    /**
     * 将 MatOfPoint2f 对象 转换成 Rect 对象
     */
    public static Rect matOfPoint2fToRect(MatOfPoint2f matOfPoint2f) {
        return Imgproc.minAreaRect(matOfPoint2f).boundingRect();
    }

    /**
     * 将 MatOfPoint 对象 转换成 Rect 对象
     */
    public static Rect matOfPointToRect(MatOfPoint matOfPoint) {
        return matOfPoint2fToRect(matOfPointToMatOfPoint2f(matOfPoint));
    }

    /**
     * 求出 [p1] 和 [p2] 两点的距离
     */
    public static double distance(Point p1, Point p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

}
