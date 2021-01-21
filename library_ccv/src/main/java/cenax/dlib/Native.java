package cenax.dlib;

import android.graphics.Bitmap;

/**
 * Native:  act as an interface between Kotlin and C++
 * Created by Luca on 12/04/2018.
 */
public final class Native {

    static {
        System.loadLibrary("native-lib");
    }

    /** load the specified landmark model (for dlib) */
    public static native void loadModel(final String path);

    public static native void loadRecgModel(final String path);

    public static native void setImageFormat(final int format);

    public static native float[] detectLandmarks(final Bitmap bitmap);

}
