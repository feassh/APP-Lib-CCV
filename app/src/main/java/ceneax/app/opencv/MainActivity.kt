package ceneax.app.opencv

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.Utils
import org.opencv.core.*
import ceneax.lib.util.DetectUtil
import ceneax.lib.util.ObjectUtil
import ceneax.lib.util.OpenCVUtil
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.sqrt


class MainActivity : BaseActivity(), CameraBridgeViewBase.CvCameraViewListener2 {



    private lateinit var mat: Mat

    private var mPreview = true
    private var mCameraFront = false
    private var mCanny = false

    private val sb = StringBuilder()

    private var mTake = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 拍照按钮显示隐藏
        intent?.extras?.apply {
            btTakePhoto.visibility = if (getBoolean("takePhoto", false)) View.VISIBLE else View.GONE
        }
        btTakePhoto.setOnClickListener {
            mTake = true
        }

        initView()
    }

    /**
     * 初始化View
     */
    fun initView() {
        cvView.setCameraPermissionGranted()
        cvView.enableView()
        cvView.setCvCameraViewListener(this)
        cvPreview.touch(true)
        cvPreview2.touch(true)

        DetectUtil.getInstance().init(applicationContext)

        btSwitch.setOnCheckedChangeListener { compoundButton, b ->
            mCameraFront = b
            cvView.disableView()
            cvView.setCameraIndex(if (b) CameraBridgeViewBase.CAMERA_ID_FRONT else CameraBridgeViewBase.CAMERA_ID_BACK)
            cvView.enableView()
        }
        btSwitchCanny.setOnCheckedChangeListener { buttonView, isChecked ->
            mCanny = isChecked
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
//        showToast("Camera Started")
    }

    override fun onCameraViewStopped() {
//        showToast("Camera Stopped")
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val src = inputFrame.rgba()
        OpenCVUtil.rotate(src, src, if (mCameraFront) 270 else 90)
        if (mCameraFront) {
            Core.flip(src, src, 1)
        }

        if (mPreview) {
            mat = src.clone()

            // 边缘检测
            if (mCanny) {
                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2GRAY)
                Imgproc.blur(mat, mat, Size(3.0, 3.0))
                Imgproc.Canny(mat, mat, 100.0, 100.0 * 3)
//                Imgproc.Canny(mat, mat, 70.0, 70.0 * 1)

                // 中心点坐标
                val centerPos = Point(mat.cols().toDouble() / 2, mat.rows().toDouble() / 2)
                Imgproc.drawMarker(src, centerPos, Scalar(255.0, 0.0, 0.0))

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
//                    Imgproc.drawContours(src, contours, index, Scalar(0.0, 255.0, 0.0))

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

                    try {
                        cvPreview.preview(src.submat(rect), true)
                        cvPreview2.preview(mat, true)
                    } catch (e: Exception) {}
                }

                return src
            }

            if (mTake) {
                Global.mat = mat
                setResult(Activity.RESULT_OK, intent.putExtras(Bundle()))
                finish()
                return src
            }

            // 中心点坐标
            val centerPos = Point(src.cols().toDouble() / 2, src.rows().toDouble() / 2)
            Imgproc.drawMarker(src, centerPos, Scalar(255.0, 0.0, 0.0))

            val faces = MatOfRect()
            DetectUtil.getInstance().detectFaces(src, faces)
            val rect = faces.toArray()
//            val eyes = MatOfRect()
//            DetectUtil.getInstance().detectEyes(src, eyes)
//            val rectEyes = eyes.toArray()

//            rect.forEach {
//                Imgproc.rectangle(src, it, Scalar(0.0, 0.0, 255.0), 0)
//            }
//            rectEyes.forEach {
//                Imgproc.rectangle(src, it, Scalar(0.0, 0.0, 255.0), 0)
//            }

            if (rect.size > 0) {
                // 已经检测到人脸，截取人脸方形图片
                mat = src.submat(rect.first())

                // 检测眼睛
                val eyes = MatOfRect()
                DetectUtil.getInstance().detectEyes(mat, eyes)
                val rectEyes = eyes.toArray()
                // 如果检测到眼睛数量为2个，则将本次人脸视为有效人脸
                if (rectEyes.size != 2) {
                    Imgproc.putText(src, "invalidate eye", Point(rect.first().x.toDouble(), rect.first().y.toDouble()),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 0.0), 2)
                    return src
                }

//                // 检测是否戴了眼镜
//                val glasses = MatOfRect()
//                DetectUtil.getInstance().detectGlasses(mat, glasses)
//                val rectGlasses = glasses.toArray()
//                // 如果检测到有带眼镜，则警告并结束本次检测
//                if (rectGlasses.size > 0) {
//                    Imgproc.putText(src, "put down glasses" + rectGlasses.size, Point(rect.first().x.toDouble(), rect.first().y.toDouble()),
//                        Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 0.0), 2)
//                    return src
//                }

                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2GRAY)
                // dlib
                val bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.RGB_565)
                Utils.matToBitmap(mat, bitmap)
                val result = DetectUtil.getInstance().detect(bitmap)
                result?.forEach {
                    // 画矩形
                    Imgproc.rectangle(src, rect.first(), Scalar(0.0, 0.0, 255.0), 2)
//                    Imgproc.rectangle(src, Point(it.left.toDouble(), it.top.toDouble()), Point(it.right.toDouble(), it.bottom.toDouble()), Scalar(0.0, 0.0, 255.0))

                    val landmarks = it.faceLandmarks
                    // 坐标平均值
                    val ad = ArrayList<Double>()
                    landmarks.forEach { item ->
                        Imgproc.drawMarker(mat, Point(item.x.toDouble(), item.y.toDouble()), Scalar(0.0, 255.0, 0.0))
                        // 取点的平均值
                        ad.add((item.x.toDouble() + item.y.toDouble()) / 2)
                    }

                    // 输出
                    Log.e("-opencv-", Gson().toJson(ad))

                    // 测试数据
                    val tmpList = Gson().fromJson<ArrayList<Double>>("[71.0,91.0,110.5,131.5,155.0,180.5,206.0,230.0,250.0,266.5,271.5,271.0,264.0,251.5,237.5,222.5,207.0,81.0,83.5,95.0,110.5,128.5,150.0,156.0,165.5,179.0,194.5,152.0,164.5,177.0,189.5,182.5,190.5,199.0,202.5,205.0,108.0,110.0,119.5,133.5,127.5,118.0,165.5,166.5,176.0,188.0,185.0,176.0,189.0,193.0,199.5,208.5,212.5,223.0,236.0,235.5,230.0,224.0,215.5,204.0,192.5,206.0,214.0,219.5,232.0,220.5,215.0,207.0]", ArrayList::class.java)
                    var dis = 0.0
                    ad.forEachIndexed { index, d ->
                        dis += (d - tmpList[index]) * (d - tmpList[index])
                    }
                    val ous = sqrt(dis)
                    if (ous <= 20) {
                        Imgproc.putText(src, "Yes Face!!!", Point(rect.first().x.toDouble(), rect.first().y.toDouble()),
                            Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(0.0, 255.0, 0.0), 2)
                    }
                    runOnUiThread {
                        setTitle("欧式距离：$ous")
                    }

                    try {
                        cvPreview.preview(mat, true) // .submat(Rect(it.left, it.top, it.right - it.left, it.bottom - it.top))
                    } catch (e: Exception) {}
                }

                return src
            } else {
                return src
            }

            // 转化为YCrCb
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2YCrCb)
            // 进行通道分离
            val matList = ArrayList<Mat>()
            Core.split(mat, matList)
            // 分别计算LBP直方图
            val matHist = Mat()
            Imgproc.calcHist(
                    matList, // 分离后的单通道图像集合
                    MatOfInt(0), // 需要统计的图像通道数，0表示只有一个通道
                    Mat(), // 输入mask，可选
                    matHist, // 输出的直方图数据
                    MatOfInt(255), // 直方图级数
                    MatOfFloat(0f, 256f) // 值域范围
            )

            // 绘制直方图
            val histImg = Mat.zeros(150, MatOfInt(255).get(0, 0)[0].toInt(), CvType.CV_8UC1)
            // 归一化
            Core.normalize(matHist, matHist, 1.0, histImg.rows().toDouble(), Core.NORM_MINMAX, -1, Mat())
            // 绘制线
            for (i in 1 until MatOfInt(255).get(0, 0)[0].toInt()) {
                Imgproc.line(histImg, Point(i.toDouble(), histImg.rows().toDouble()),
                        Point(i.toDouble(), histImg.rows() - round(matHist.get(i, 0)[0])),
                        Scalar(255.0, 255.0, 255.0), 1, 8, 0
                )
            }

            // 在原图上画方框来标记
            Imgproc.rectangle(src, rect.first(), Scalar(255.0, 0.0, 0.0), 2)
            Imgproc.putText(src, "not alive", Point(rect.first().x.toDouble(), rect.first().y.toDouble() - 10),
                Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 0.0), 2)

            cvPreview.preview(mat, true) // 转化为YCrCb的结果
            cvPreview2.preview(histImg, false) // 直方图展示

            runOnUiThread {
                sb.clear()
                sb.append("通道数: ${mat.channels()}\n")
                tvInfo.text = sb.toString()
            }
        }

        return src
    }

}