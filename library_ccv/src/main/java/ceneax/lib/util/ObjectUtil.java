package ceneax.lib.util;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class ObjectUtil {

    public static boolean isContainsOtherRect(Rect small, Rect large) {
        return small.tl().inside(large) && small.br().inside(large);
    }

    public static MatOfPoint2f matOfPointToMatOfPoint2f(MatOfPoint matOfPoint) {
        return new MatOfPoint2f(matOfPoint.toArray());
    }

    public static Rect matOfPoint2fToRect(MatOfPoint2f matOfPoint2f) {
        return Imgproc.minAreaRect(matOfPoint2f).boundingRect();
    }

    public static Rect matOfPointToRect(MatOfPoint matOfPoint) {
        return matOfPoint2fToRect(matOfPointToMatOfPoint2f(matOfPoint));
    }

    public static double distance(Point p1, Point p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

}
