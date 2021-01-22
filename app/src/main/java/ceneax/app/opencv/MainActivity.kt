package ceneax.app.opencv

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import cenax.dlib.Dlib
import ceneax.lib.ccv.core.Detector
import ceneax.lib.ccv.util.CVUtil
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.Utils
import org.opencv.core.*


class MainActivity : BaseActivity(), CameraBridgeViewBase.CvCameraViewListener2 {



    private lateinit var mat: Mat

    private var mPreview = true
    private var mCameraFront = false
    private var mCanny = false

    private val sb = StringBuilder()

    private var mTake = false

    private var tmpList = ArrayList<ArrayList<Double>>()
    private var ab = FloatArray(128)

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

        // 构造的临时测试数据
        tmpList = ArrayList()
        for (i in 0 until 10000) {
            tmpList.add(Gson().fromJson<ArrayList<Double>>("[71.0,91.0,110.5,131.5,155.0,180.5,206.0,230.0,250.0,266.5,271.5,271.0,264.0,251.5,237.5,222.5,207.0,81.0,83.5,95.0,110.5,128.5,150.0,156.0,165.5,179.0,194.5,152.0,164.5,177.0,189.5,182.5,190.5,199.0,202.5,205.0,108.0,110.0,119.5,133.5,127.5,118.0,165.5,166.5,176.0,188.0,185.0,176.0,189.0,193.0,199.5,208.5,212.5,223.0,236.0,235.5,230.0,224.0,215.5,204.0,192.5,206.0,214.0,219.5,232.0,220.5,215.0,207.0]", ArrayList::class.java))
        }

        ab = Gson().fromJson<FloatArray>("[-0.101698674, 0.13185862, -0.018859316, -0.03697396, -0.06313801, -0.048449367, -0.026614746, -0.1395575, 0.1543801, -0.09604482, 0.24826673, -0.09726117, -0.24537478, -0.06711103, -0.03370415, 0.17365937, -0.23957944, -0.048314396, -0.051792197, -0.0011096541, 0.0761536, 0.049710646, 0.026736416, 0.08478188, -0.058534347, -0.3279084, -0.12114144, -0.1836083, 0.051822767, -0.083266884, -0.041987117, -0.02282236, -0.086688116, -0.0803823, 0.028988335, 0.004567325, -0.07254288, -0.07087374, 0.1982949, 0.017360825, -0.1879285, 0.0076300628, 0.048332926, 0.23045664, 0.1794013, 0.10813583, -0.0032506865, -0.16027983, 0.15103701, -0.16151288, 0.029995536, 0.11521808, 0.089242086, 0.10847391, 0.039534736, -0.14181048, 0.03669966, 0.14520422, -0.20545647, 0.03992342, 0.051381968, -0.07306227, 0.026397713, 0.007913874, 0.21906671, 0.073203556, -0.10421711, -0.11079192, 0.13029972, -0.19981766, -0.13115723, 0.055016704, -0.10992485, -0.21081713, -0.24753594, -0.0018008687, 0.36891437, 0.15027112, -0.1571914, 0.007527398, -0.015892984, -0.09252606, 0.14403452, 0.19412026, -0.016793046, -0.069715574, -0.05919407, -0.07682353, 0.1999592, -0.07886673, -8.098368E-4, 0.25783667, -0.020635657, 0.08712941, 0.017748725, -0.014107146, -0.11397704, 0.018971644, -0.06050942, -0.080102086, 0.028528629, -0.036081437, -0.029284228, 0.07357086, -0.17476337, 0.1552151, 0.010190974, -0.0026817652, 0.0038313274, -0.036652453, -0.028127035, -0.014978241, 0.16856109, -0.19294131, 0.24644406, 0.18525553, 0.06426914, 0.14019275, 0.0606359, 0.049463283, 0.0017689718, -0.03740639, -0.22703226, -0.04658958, 0.1245096, -0.029183544, 0.07153589, -0.050444413]", FloatArray::class.java)
    }

    /**
     * 初始化View
     */
    fun initView() {
//        cvView.setCameraPermissionGranted()
        cvView.enableView()
        cvView.setCvCameraViewListener(this)
        cvPreview.touch(true)
        cvPreview2.touch(true)

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
        CVUtil.rotate(src, src, if (mCameraFront) 270 else 90)
        if (mCameraFront) {
            Core.flip(src, src, 1)
        }

        if (mPreview) {
            mat = src.clone()

//            // 边缘检测
//            if (mCanny) {
//                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2GRAY)
//                Imgproc.blur(mat, mat, Size(3.0, 3.0))
//                Imgproc.Canny(mat, mat, 100.0, 100.0 * 3)
////                Imgproc.Canny(mat, mat, 70.0, 70.0 * 1)
//
//                // 中心点坐标
//                val centerPos = Point(mat.cols().toDouble() / 2, mat.rows().toDouble() / 2)
//                Imgproc.drawMarker(src, centerPos, Scalar(255.0, 0.0, 0.0))
//
//                val contours = ArrayList<MatOfPoint>()
//                val hier = Mat()
//                Imgproc.findContours(mat, contours, hier, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE)
//
//                // 去除无效的边框轮廓
//                val iteratorUseless = contours.iterator()
//                while (iteratorUseless.hasNext()) {
//                    val item = iteratorUseless.next()
//                    // 暂时不用area()和size()
//                    if (item.width() < 200 && item.height() < 200) {
//                        iteratorUseless.remove()
//                    }
//                }
//
//                // 去除包含的边框轮廓
//                val iteratorContain = contours.iterator()
//                while (iteratorContain.hasNext()) {
//                    val item = iteratorContain.next()
//
//                    // TODO 这里有问题，不应该包含iterator自身
//                    for (it in contours) {
//                        if (ObjectUtil.isContainsOtherRect(
//                                ObjectUtil.matOfPointToRect(item), ObjectUtil.matOfPointToRect(it))) {
//                            iteratorContain.remove()
//                            break
//                        }
//                    }
//                }
//
//                var minRectIndex = -1
//                var minRectDis = 99999.9
//                contours.forEachIndexed { index, matOfPoint ->
////                    Imgproc.drawContours(src, contours, index, Scalar(0.0, 255.0, 0.0))
//
//                    // 绘制边框轮廓
//                    val rect = ObjectUtil.matOfPointToRect(matOfPoint)
//                    Imgproc.rectangle(src, rect, Scalar(0.0, 255.0, 0.0))
//
//                    // 取最靠近原图中心坐标的边框轮廓对象
//                    val br = rect.br()
//                    val tl = rect.tl()
//                    val x = (br.x - tl.x) / 2 + tl.x
//                    val y = (br.y - tl.y) / 2 + tl.y
//                    val rectPos = Point(x, y)
//                    val dis = ObjectUtil.distance(centerPos, rectPos)
//                    if (abs(dis) < abs(minRectDis)) {
//                        minRectIndex = index
//                        minRectDis = dis
//                    }
//                }
//                if (minRectIndex != -1) {
//                    val rect = ObjectUtil.matOfPointToRect(contours[minRectIndex])
//                    Imgproc.rectangle(src, rect, Scalar(0.0, 0.0, 255.0), 2)
//
//                    try {
//                        cvPreview.preview(src.submat(rect), true)
//                        cvPreview2.preview(mat, true)
//                    } catch (e: Exception) {}
//                }
//
//                return src
//            }
//
//            if (mTake) {
//                Global.mat = mat
//                setResult(Activity.RESULT_OK, intent.putExtras(Bundle()))
//                finish()
//                return src
//            }
//
//            // 中心点坐标
//            val centerPos = Point(src.cols().toDouble() / 2, src.rows().toDouble() / 2)
//            Imgproc.drawMarker(src, centerPos, Scalar(255.0, 0.0, 0.0))

            val faces = MatOfRect()
            Detector.detectFaces(src, faces)
            val rect = faces.toArray()

            if (rect.size > 0) {
                if (rect.first().width < src.width() - (src.width() / 2)) {
                    runOnUiThread {
                        tvWarn.visibility = View.VISIBLE
                        tvWarn.text = "请拉近镜头"
                    }
                    return src
                }
                if (rect.first().width > src.width() - (src.width() / 4)) {
                    runOnUiThread {
                        tvWarn.visibility = View.VISIBLE
                        tvWarn.text = "请拉远镜头"
                    }
                    return src
                }
                runOnUiThread {
                    tvWarn.visibility = View.GONE
                    tvWarn.text = ""
                }

                // 已经检测到人脸，截取人脸方形图片
                mat = mat.submat(rect.first())

                CVUtil.mat2gray(mat, mat)

                // dlib
//                val result = Detector.detect(mat)
//                result?.forEach {
//                    // 画矩形
//                    Imgproc.rectangle(src, rect.first(), Scalar(0.0, 0.0, 255.0), 2)
////                    Imgproc.rectangle(src, Point(it.left.toDouble(), it.top.toDouble()), Point(it.right.toDouble(), it.bottom.toDouble()), Scalar(0.0, 0.0, 255.0))
//
//                    val landmarks = it.faceLandmarks
//                    // 坐标平均值
//                    val ad = ArrayList<Double>()
//                    landmarks.forEach { item ->
//                        Imgproc.drawMarker(mat, Point(item.x.toDouble(), item.y.toDouble()), Scalar(0.0, 255.0, 0.0))
//                        // 取点的平均值
//                        ad.add((item.x.toDouble() + item.y.toDouble()) / 2)
//                    }
//
//                    // 输出
////                    Log.e("-opencv-", Gson().toJson(ad))
//
//                    // 测试数据
////                    val tmpList = Gson().fromJson<ArrayList<Double>>("[71.0,91.0,110.5,131.5,155.0,180.5,206.0,230.0,250.0,266.5,271.5,271.0,264.0,251.5,237.5,222.5,207.0,81.0,83.5,95.0,110.5,128.5,150.0,156.0,165.5,179.0,194.5,152.0,164.5,177.0,189.5,182.5,190.5,199.0,202.5,205.0,108.0,110.0,119.5,133.5,127.5,118.0,165.5,166.5,176.0,188.0,185.0,176.0,189.0,193.0,199.5,208.5,212.5,223.0,236.0,235.5,230.0,224.0,215.5,204.0,192.5,206.0,214.0,219.5,232.0,220.5,215.0,207.0]", ArrayList::class.java)
//
//                    val ous = CVUtil.compareEDs(ad, tmpList as List<MutableList<Double>>)
//                    if (ous.value <= 20) {
//                        Imgproc.putText(src, "Yes Face!!!", Point(rect.first().x.toDouble(), rect.first().y.toDouble()),
//                            Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(0.0, 255.0, 0.0), 2)
//                    }
//                    runOnUiThread {
//                        setTitle("欧氏距离：${ous.value}")
//                    }
//
//                    try {
//                        cvPreview.preview(mat, true) // .submat(Rect(it.left, it.top, it.right - it.left, it.bottom - it.top))
//                    } catch (e: Exception) {}
//                }

                val res = Detector.recgFace(mat)
                if (res != null) {
                    val comp = Detector.compute(res, ab)
                    runOnUiThread {
                        title = "欧氏距离：$comp"
                    }
                }

                return src
            } else {
                return src
            }

//            // 转化为YCrCb
//            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2YCrCb)
//            // 进行通道分离
//            val matList = ArrayList<Mat>()
//            Core.split(mat, matList)
//            // 分别计算LBP直方图
//            val matHist = Mat()
//            Imgproc.calcHist(
//                    matList, // 分离后的单通道图像集合
//                    MatOfInt(0), // 需要统计的图像通道数，0表示只有一个通道
//                    Mat(), // 输入mask，可选
//                    matHist, // 输出的直方图数据
//                    MatOfInt(255), // 直方图级数
//                    MatOfFloat(0f, 256f) // 值域范围
//            )
//
//            // 绘制直方图
//            val histImg = Mat.zeros(150, MatOfInt(255).get(0, 0)[0].toInt(), CvType.CV_8UC1)
//            // 归一化
//            Core.normalize(matHist, matHist, 1.0, histImg.rows().toDouble(), Core.NORM_MINMAX, -1, Mat())
//            // 绘制线
//            for (i in 1 until MatOfInt(255).get(0, 0)[0].toInt()) {
//                Imgproc.line(histImg, Point(i.toDouble(), histImg.rows().toDouble()),
//                        Point(i.toDouble(), histImg.rows() - round(matHist.get(i, 0)[0])),
//                        Scalar(255.0, 255.0, 255.0), 1, 8, 0
//                )
//            }
//
//            // 在原图上画方框来标记
//            Imgproc.rectangle(src, rect.first(), Scalar(255.0, 0.0, 0.0), 2)
//            Imgproc.putText(src, "not alive", Point(rect.first().x.toDouble(), rect.first().y.toDouble() - 10),
//                Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 0.0), 2)
//
//            cvPreview.preview(mat, true) // 转化为YCrCb的结果
//            cvPreview2.preview(histImg, false) // 直方图展示
//
//            runOnUiThread {
//                sb.clear()
//                sb.append("通道数: ${mat.channels()}\n")
//                tvInfo.text = sb.toString()
//            }
        }

        return src
    }

}