package ceneax.app.opencv

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.core.os.EnvironmentCompat
import androidx.core.view.get
import ceneax.app.opencv.bean.PicInfo
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_pic.*
import kotlinx.android.synthetic.main.item_pic.view.*
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.features2d.DescriptorMatcher
import org.opencv.features2d.SIFT
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPOutputStream
import kotlin.collections.ArrayList


class PicActivity : BaseActivity() {

    private val mList = ArrayList<PicInfo>()
    private lateinit var mAdapter: PicAdapter

    //用于保存拍照图片的uri
    private var mCameraUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pic)

        // 初始化适配器
        mAdapter = PicAdapter(mList)
        lvMain.adapter = mAdapter

        cvPreview.touch(true)

        // 采集图片
        btTake.setOnClickListener {
//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            mCameraUri = createImageUri()
//            if (mCameraUri != null) {
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri)
//                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//                startActivityForResult(intent, 100)
//            }

            val bundle = Bundle()
            bundle.putBoolean("takePhoto", true)
            startActivityForResult(Intent(this, MainActivity::class.java).putExtras(bundle), 300)
        }
        // 拍照对比listView中的图片
        btCompare.setOnClickListener {
//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            mCameraUri = createImageUri()
//            if (mCameraUri != null) {
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri)
//                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//                startActivityForResult(intent, 200)
//            }

            val bundle = Bundle()
            bundle.putBoolean("takePhoto", true)
            startActivityForResult(Intent(this, MainActivity::class.java).putExtras(bundle), 400)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            100 -> {
//                Glide.with(this)
//                    .asBitmap()
//                    .load(mCameraUri)
//                    .override(100, 100)
//                    .into(object : SimpleTarget<Bitmap>() {
//                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                        val mat = Mat()
//                        Utils.bitmapToMat(resource, mat)
//                        Log.e("///", mat.dump().toByteArray().size.toString())
//
//                        // 转化为YCrCb
//                        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2YCrCb)
//                        // 进行通道分离
//                        val matList = ArrayList<Mat>()
//                        Core.split(mat, matList)
//                        // 分别计算LBP直方图
//                        val matHist = Mat()
//                        Imgproc.calcHist(
//                            matList, // 分离后的单通道图像集合
//                            MatOfInt(0), // 需要统计的图像通道数，0表示只有一个通道
//                            Mat(), // 输入mask，可选
//                            matHist, // 输出的直方图数据
//                            MatOfInt(255), // 直方图级数
//                            MatOfFloat(0f, 256f) // 值域范围
//                        )
//                        Log.e("///", matHist.dump().toByteArray().size.toString())
//
//                        mList.add(PicInfo(mCameraUri!!, matHist.dump().replace("\n", ""), matHist.clone()))
//                        mAdapter.notifyDataSetChanged()
//                    }
//                })
            }
            200 -> {
//                Glide.with(this)
//                    .asBitmap()
//                    .load(mCameraUri)
//                    .override(100, 100)
//                    .into(object : SimpleTarget<Bitmap>() {
//                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                            mList.forEachIndexed { index, picInfo ->
//                                lvMain[index].setBackgroundColor(Color.WHITE)
//                            }
//
//                            val mat = Mat()
//                            Utils.bitmapToMat(resource, mat)
//                            Log.e("///", mat.dump().toByteArray().size.toString())
//
//                            // 转化为YCrCb
//                            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2YCrCb)
//                            // 进行通道分离
//                            val matList = ArrayList<Mat>()
//                            Core.split(mat, matList)
//                            // 分别计算LBP直方图
//                            val matHist = Mat()
//                            Imgproc.calcHist(
//                                matList, // 分离后的单通道图像集合
//                                MatOfInt(0), // 需要统计的图像通道数，0表示只有一个通道
//                                Mat(), // 输入mask，可选
//                                matHist, // 输出的直方图数据
//                                MatOfInt(255), // 直方图级数
//                                MatOfFloat(0f, 256f) // 值域范围
//                            )
//                            Log.e("///", matHist.dump().toByteArray().size.toString())
//
//                            var i = -1
//                            var currentLarge = 0.0
//                            mList.forEachIndexed { index, it ->
//                                val value = Imgproc.compareHist(matHist, it.mat, Imgproc.HISTCMP_CORREL)
//                                Log.e("///", value.toString())
//                                if (value > currentLarge) {
//                                    currentLarge = value
//                                    i = index
//                                }
//                            }
//                            if (i != -1) {
//                                if (currentLarge >= 0.8) {
//                                    lvMain[i].setBackgroundColor(Color.GREEN)
//                                    Toast.makeText(this@PicActivity, "相似度高：${currentLarge}", Toast.LENGTH_SHORT).show()
//                                } else {
//                                    lvMain[i].setBackgroundColor(Color.YELLOW)
//                                    Toast.makeText(this@PicActivity, "相似度低：${currentLarge}", Toast.LENGTH_SHORT).show()
//                                }
//                            }
//                        }
//                    })
            }
            300 -> {
                Global.mat?.let {
                    val mat = it.clone()
//                    Log.e("///", mat.dump().toByteArray().size.toString())

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
                    Log.e("///", matHist.dump().toByteArray().size.toString())

                    val bitmap = Bitmap.createBitmap(it.width(), it.height(), Bitmap.Config.RGB_565)
                    Utils.matToBitmap(it, bitmap)
                    mList.add(PicInfo(bitmap, matHist.dump().replace("\n", ""), it.clone()))
                    mAdapter.notifyDataSetChanged()
                }
            }
            400 -> {
                Global.mat?.let {
                    mList.forEachIndexed { index, picInfo ->
                        lvMain[index].setBackgroundColor(Color.WHITE)
                    }

                    val mat = it.clone()
//                    Log.e("///", mat.dump().toByteArray().size.toString())

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
                    Log.e("///", matHist.dump().toByteArray().size.toString())
                    // 归一化
                    Core.normalize(matHist, matHist, 0.0, 1.0, Core.NORM_MINMAX)

                    var i = -1
                    var currentLarge = 0.0
                    mList.forEachIndexed { index, item ->
                        if (matchImage(it, item.mat)) {
                            lvMain[index].setBackgroundColor(Color.GREEN)
                            Toast.makeText(this@PicActivity, "相似度高：${index + 1}", Toast.LENGTH_SHORT).show()
                            return
                        }

//                        val value = Imgproc.compareHist(matHist, item.mat, Imgproc.HISTCMP_CORREL)
//                        Log.e("///", value.toString())
//                        if (value > currentLarge) {
//                            currentLarge = value
//                            i = index
//                        }
                    }
                    Toast.makeText(this@PicActivity, "无匹配", Toast.LENGTH_SHORT).show()
//                    if (i != -1) {
//                        if (currentLarge >= 0.8) {
//                            lvMain[i].setBackgroundColor(Color.GREEN)
//                            Toast.makeText(this@PicActivity, "相似度高：${currentLarge}", Toast.LENGTH_SHORT).show()
//                        } else {
//                            lvMain[i].setBackgroundColor(Color.YELLOW)
//                            Toast.makeText(this@PicActivity, "相似度低：${currentLarge}", Toast.LENGTH_SHORT).show()
//                        }
//                    }
                }
            }
        }
    }

    private fun matchImage(base: Mat, com: Mat): Boolean {
        val resT = Mat()
        val resO = Mat()

        val sift = SIFT.create()

        val baseKeyPoints = MatOfKeyPoint()
        val comKeyPoints = MatOfKeyPoint()

        // 获取特征点
        sift.detect(base, baseKeyPoints)
        sift.detect(com, comKeyPoints)

        // 获取特征描述
        sift.compute(base, baseKeyPoints, resT)
        sift.compute(com, comKeyPoints, resO)

        // 展示原图和描述图
        // ...
        cvPreview.preview(resO, true)

//        val a = resT.dump()
//        val b = resO.dump()
//        Log.e("-opencv-", "resT: ${a.length}, resO: ${b.length}")
//        Log.e("-opencv-", "resT gzip: ${gzip(a).length}, resO gzip: ${gzip(b).length}")

        // 描述匹配
        val matchList = ArrayList<MatOfDMatch>()
        val descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED)
        // knnMatch方法的作用就是在给定特征描述集合中寻找最佳匹配
        // 使用KNN-matching算法，令K=2，则每个match得到两个最接近的descriptor，然后计算最接近距离和次接近距离之间的比值，当比值大于既定值时，才作为最终match。
        descriptorMatcher.knnMatch(resT, resO, matchList, 2)

        // 计算匹配结果
        val similarMatchList = LinkedList<DMatch>()
        // 对匹配结果进行筛选，依据distance进行筛选
        matchList.forEach {
            val dMatchArray = it.toArray()
            val dm1 = dMatchArray[0]
            val dm2 = dMatchArray[1]

            if (dm1.distance <= dm2.distance * 0.7f) {
                similarMatchList.addLast(dm1)
            }
        }

        // 当匹配后的特征点大于等于 30 个，则认为模板图在原图中，该值可以自行调整
        Log.e("-opencv-", "matchList.size: ${matchList.size}, similarMatchList.size: ${similarMatchList.size}, " +
                "percent: ${similarMatchList.size.toDouble() / matchList.size.toDouble()}")
        return similarMatchList.size.toDouble() / matchList.size.toDouble() >= 0.1 // similarMatchList.size >= 30
    }

    /**
     * 使用gzip进行压缩
     */
    fun gzip(primStr: String): String {
        if (primStr.isEmpty()) {
            return primStr
        }

        val out = ByteArrayOutputStream()
        var gzip: GZIPOutputStream? = null

        try {
            gzip = GZIPOutputStream(out)
            gzip.write(primStr.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (gzip != null) {
                try {
                    gzip.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
    }


    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     */
    private fun createImageUri(): Uri {
        val status: String = Environment.getExternalStorageState()
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        return if (status == Environment.MEDIA_MOUNTED) {
            contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues()
            )!!
        } else {
            contentResolver.insert(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                ContentValues()
            )!!
        }
    }

    /**
     * 创建保存图片的文件
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val imageName: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
        val tempFile = File(storageDir, imageName)
        return if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            File("")
        } else tempFile
    }

    // 适配器
    class PicAdapter(private val list: ArrayList<PicInfo>) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var conView = convertView
            if (conView == null) {
                conView = LayoutInflater.from(parent.context).inflate(R.layout.item_pic, null, false)
            }

            val item = list[position]

            conView?.apply {
                Glide.with(parent.context).load(item.img).into(ivMain)
//                ivMain.setImageURI(item.imgUri)
                tvInfo.text = "特征值：\n${item.data}"
            }

            return conView!!
        }

        override fun getItem(position: Int): Any = list[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getCount(): Int = list.size
    }

}