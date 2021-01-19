package ceneax.app.opencv.bean

import android.graphics.Bitmap
import android.net.Uri
import org.opencv.core.Mat

data class PicInfo(
    // 图片数据
    var img: Bitmap = Bitmap.createBitmap(0, 0, Bitmap.Config.RGB_565),
//    var imgUri: Uri = Uri.EMPTY,
    // 特征数据
    var data: String = "",
    var mat: Mat = Mat()
)