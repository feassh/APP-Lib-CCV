package ceneax.app.opencv

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityCompat
import ceneax.lib.core.Detector
import kotlinx.android.synthetic.main.activity_init.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

class InitActivity : BaseActivity() {

    /**
     * 申请权限需要的code
     */
    private val ACTION_REQUEST_PERMISSIONS = 0x001

    /**
     * 所需的所有权限信息
     */
    private val NEEDED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)

        btScanPic.setOnClickListener {
            startActivity(Intent(this, PicActivity::class.java))
        }
        btPicProc.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean("takePhoto", true)
            startActivityForResult(Intent(this, MainActivity::class.java).putExtras(bundle), 500)
        }
        btScanFace.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // 检查必要的权限
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS)
        } else {
            initOpenCV()
        }
    }

    /**
     * 权限申请后获取返回结果
     */
    override fun afterRequestPermission(requestCode: Int, isAllGranted: Boolean) {
        super.afterRequestPermission(requestCode, isAllGranted)
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                initOpenCV()
            } else {
                showToast("APP权限不足，需要获取相应权限")
            }
        }
    }

    /**
     * 初始化OpenCV
     */
    private fun initOpenCV() {
        Detector.init(applicationContext)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            500 -> {
                startActivity(Intent(this, PicProcActivity::class.java))
            }
        }
    }

}