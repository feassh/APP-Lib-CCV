package ceneax.app.opencv

import android.graphics.Bitmap
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import ceneax.lib.ccv.util.ObjectUtil
import kotlinx.android.synthetic.main.activity_pic_proc.*
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.abs

class PicProcActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pic_proc)

        ivMain.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                showImage(Point(event.x.toDouble(), event.y.toDouble()))
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }

        Toast.makeText(this, "分析图片...", Toast.LENGTH_SHORT).show()
        showImage()
    }

    private fun showImage(centerPoint: Point? = null) {
        Global.mat?.let {
            if (centerPoint != null) {
                centerPoint.x = centerPoint.x - it.width()
                centerPoint.y = centerPoint.y - it.height()
            }

            val mat = it.clone()
            processMat(mat, centerPoint)
            val bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.RGB_565)
            Utils.matToBitmap(mat, bitmap)
            ivMain.setImageBitmap(bitmap)
        }
    }

    private fun processMat(src: Mat, centerPoint: Point? = null):ArrayList<MatOfPoint> {
        val mat = src.clone()

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2GRAY)
        Imgproc.blur(mat, mat, Size(3.0, 3.0))
        Imgproc.Canny(mat, mat, 100.0, 100.0 * 3)

        // 中心点坐标
        val centerPos = centerPoint ?: Point(mat.cols().toDouble() / 2, mat.rows().toDouble() / 2)
//        Imgproc.drawMarker(src, centerPos, Scalar(255.0, 0.0, 0.0))

        val contours = ArrayList<MatOfPoint>()
        val hier = Mat()
        Imgproc.findContours(mat, contours, hier, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE)

        // 去除无效的边框轮廓
        val iteratorUseless = contours.iterator()
        while (iteratorUseless.hasNext()) {
            val item = iteratorUseless.next()
            // 暂时不用area()和size()
            if (item.width() < 200 && item.height() < 200) {
                iteratorUseless.remove()
            }
        }

        // 去除包含的边框轮廓
        val iteratorContain = contours.iterator()
        while (iteratorContain.hasNext()) {
            val item = iteratorContain.next()

            // TODO 这里有问题，不应该包含iterator自身
            for (it in contours) {
                if (ObjectUtil.isContainsOtherRect(
                        ObjectUtil.matOfPointToRect(item), ObjectUtil.matOfPointToRect(it))) {
                    iteratorContain.remove()
                    break
                }
            }
        }

        var minRectIndex = -1
        var minRectDis = 99999.9
        contours.forEachIndexed { index, matOfPoint ->
//            Imgproc.drawContours(src, contours, index, Scalar(0.0, 255.0, 0.0))

            // 绘制边框轮廓
            val rect = ObjectUtil.matOfPointToRect(matOfPoint)
            Imgproc.rectangle(src, rect, Scalar(0.0, 255.0, 0.0))

            // 取最靠近原图中心坐标的边框轮廓对象
            val br = rect.br()
            val tl = rect.tl()
            val x = (br.x - tl.x) / 2 + tl.x
            val y = (br.y - tl.y) / 2 + tl.y
            val rectPos = Point(x, y)
            val dis = ObjectUtil.distance(centerPos, rectPos)
            if (abs(dis) < abs(minRectDis)) {
                minRectIndex = index
                minRectDis = dis
            }
        }
        if (minRectIndex != -1) {
            val rect = ObjectUtil.matOfPointToRect(contours[minRectIndex])
            Imgproc.rectangle(src, rect, Scalar(0.0, 0.0, 255.0), 2)
        }

        return contours
    }

}