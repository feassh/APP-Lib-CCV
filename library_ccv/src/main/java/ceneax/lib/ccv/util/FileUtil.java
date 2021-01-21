package ceneax.lib.ccv.util;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Description: 文件操作 工具类
 * @Date: 2021/1/19 15:42
 * @Author: ceneax
 */
public class FileUtil {

    /**
     * 将 assets 文件夹下的指定文件写入到APP的内部缓存目录中
     */
    public static String assetsToFile(Context context, String fileName) {
        File file = new File(context.getExternalCacheDir(), fileName);

        if (file.exists()) {
            return file.getAbsolutePath();
        }

        try {
            InputStream inputStream = context.getResources().getAssets().open(fileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int byteRead;
            while ((byteRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteRead);
            }
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        return file.getAbsolutePath();
    }

}
