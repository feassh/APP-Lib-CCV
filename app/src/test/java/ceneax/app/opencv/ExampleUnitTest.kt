package ceneax.app.opencv

import android.util.Log
import ceneax.lib.util.CVUtil
import com.google.gson.Gson
import org.junit.Test

import org.junit.Assert.*
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import kotlin.math.sqrt

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testTime() {
        // 当前采集的人脸数据
        val ad = Gson().fromJson<ArrayList<Double>>("[71.0,91.0,110.5,131.5,155.0,180.5,206.0,230.0,250.0,266.5,271.5,271.0,264.0,251.5,237.5,222.5,207.0,81.0,83.5,95.0,110.5,128.5,150.0,156.0,165.5,179.0,194.5,152.0,164.5,177.0,189.5,182.5,190.5,199.0,202.5,205.0,108.0,110.0,119.5,133.5,127.5,118.0,165.5,166.5,176.0,188.0,185.0,176.0,189.0,193.0,199.5,208.5,212.5,223.0,236.0,235.5,230.0,224.0,215.5,204.0,192.5,206.0,214.0,219.5,232.0,220.5,215.0,207.0]", ArrayList::class.java)

        // 构造的临时测试数据
        val tmpList = ArrayList<ArrayList<Double>>()
        for (i in 0 until 10000) {
            tmpList.add(Gson().fromJson<ArrayList<Double>>("[71.0,91.0,110.5,131.5,155.0,180.5,206.0,230.0,250.0,266.5,271.5,271.0,264.0,251.5,237.5,222.5,207.0,81.0,83.5,95.0,110.5,128.5,150.0,156.0,165.5,179.0,194.5,152.0,164.5,177.0,189.5,182.5,190.5,199.0,202.5,205.0,108.0,110.0,119.5,133.5,127.5,118.0,165.5,166.5,176.0,188.0,185.0,176.0,189.0,193.0,199.5,208.5,212.5,223.0,236.0,235.5,230.0,224.0,215.5,204.0,192.5,206.0,214.0,219.5,232.0,220.5,215.0,207.0]", ArrayList::class.java))
        }

        System.out.println("ccv: ${CVUtil.compareEDs(ad, tmpList as List<MutableList<Double>>).value}")

//        // 比对
//        for (t in tmpList.indices) {
//            var dis = 0.0
//
//            for (d in tmpList[t].indices) {
//                dis += (ad[d] - tmpList[t][d]) * (ad[d] - tmpList[t][d])
//            }
//
//            val ous = sqrt(dis)
//
//            if (ous <= 20) {
//                System.out.println("ccv: $ous")
//            }
//        }
    }
}