package ceneax.lib.ccv.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

/**
 * @Description: Mat数据预览 SurfaceView
 * @Date: 2020/11/12 12:06
 * @Author: ceneax
 */
public class PreviewSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable, View.OnTouchListener {

    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;

    // 是否正在绘制过程 标志位
    private boolean mIsDrawing = false;
    // 是否为旋转过的图片
    private boolean mRotated = true;

    // 当前要绘制的Mat数据
    private Mat mMat;

    // 是否需要手指滑动功能
    private boolean mTouch = false;
    // 记录最后一次手指滑动的坐标
    private PointF lastPoint = new PointF();

    public PreviewSurfaceView(Context context) {
        this(context, null);
    }

    public PreviewSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreviewSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        setOnTouchListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
    }

    @Override
    public void run() {
        try {
            mCanvas = mSurfaceHolder.lockCanvas();
            mCanvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);

            if (mRotated) {
                // 将Mat转换为Bitmap
                Bitmap bitmap = Bitmap.createBitmap(mMat.width(), mMat.height(), Bitmap.Config.RGB_565);
                Utils.matToBitmap(mMat, bitmap);

                /*** 修改缩放比 ***/
                float mScale = Math.min((mCanvas.getWidth() / (float) bitmap.getWidth()),
                        (mCanvas.getHeight() / (float) bitmap.getHeight()));

                if (mScale != 0) {
                    mCanvas.drawBitmap(bitmap, new Rect(0,0,bitmap.getWidth(), bitmap.getHeight()),
                            new Rect((int)((mCanvas.getWidth() - mScale*bitmap.getWidth()) / 2),
                                    (int)((mCanvas.getHeight() - mScale*bitmap.getHeight()) / 2),
                                    (int)((mCanvas.getWidth() - mScale*bitmap.getWidth()) / 2 + mScale*bitmap.getWidth()),
                                    (int)((mCanvas.getHeight() - mScale*bitmap.getHeight()) / 2 + mScale*bitmap.getHeight())), null);
                } else {
                    mCanvas.drawBitmap(bitmap, new Rect(0,0,bitmap.getWidth(), bitmap.getHeight()),
                            new Rect((mCanvas.getWidth() - bitmap.getWidth()) / 2,
                                    (mCanvas.getHeight() - bitmap.getHeight()) / 2,
                                    (mCanvas.getWidth() - bitmap.getWidth()) / 2 + bitmap.getWidth(),
                                    (mCanvas.getHeight() - bitmap.getHeight()) / 2 + bitmap.getHeight()), null);
                }
            } else {
                // 将Mat转换为Bitmap
                Bitmap bitmap = Bitmap.createBitmap(mMat.width(), mMat.height(), Bitmap.Config.RGB_565);
                Utils.matToBitmap(mMat, bitmap);

                /********* 旋转缩放画布 *********/
                mCanvas.rotate(90, 0, 0);
                float scale = mCanvas.getWidth() / (float) bitmap.getHeight();
                float scale2 = mCanvas.getHeight() / (float) bitmap.getWidth();
                if (scale2 > scale) {
                    scale = scale2;
                }
                if (scale != 0) {
                    mCanvas.scale(scale, scale, 0, 0);
                }

                mCanvas.drawBitmap(bitmap, 0, -bitmap.getHeight(), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            }
        }
        mIsDrawing = false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mTouch) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastPoint.set(event.getRawX(), event.getRawY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    float x = event.getRawX() - lastPoint.x;
                    float y = event.getRawY() - lastPoint.y;
                    v.setX(v.getX() + x);
                    v.setY(v.getY() + y);
                    lastPoint.set(event.getRawX(), event.getRawY());
                    break;
            }
        }
        return mTouch;
    }

    /**
     * 预览
     * @param mat Mat数据
     */
    public void preview(Mat mat, boolean rotated) {
        mIsDrawing = true;
        mRotated = rotated;
        mMat = mat.clone();
        new Thread(this).start();
    }

    /**
     * 可否跟随手指移动
     * @param touch 标志位
     */
    public void touch(boolean touch) {
        mTouch = touch;
    }

}
