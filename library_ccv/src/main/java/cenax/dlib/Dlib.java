package cenax.dlib;

import android.graphics.Bitmap;

/**
 * Dlib 类
 */
public final class Dlib {

    static {
        // 加载so库
        System.loadLibrary("native-lib");
    }

    /**
     * 加载68点模型
     */
    public static native void loadModel(final String path);

    /**
     * 加载128D模型
     */
    public static native void loadRecgModel(final String path);

    /**
     * 传入bitmap，取出128D数据
     */
    public static native float[] recg(final Bitmap bitmap);

    /**
     * 比对一组人脸128D数据
     */
    public static native float compute(final float[] src, final float[] tar);

}
