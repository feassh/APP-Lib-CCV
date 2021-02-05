# APP-Lib-CCV
Android人脸检测与识别库，基于 [OpenCV](https://github.com/opencv/opencv) 和 [Dlib](https://github.com/davisking/dlib)

需要看Demo的话，可以Clone该仓库到本地，然后用Android Studio直接作为工程打开运行即可

### 集成方法：
 - 在线集成<br/>
   1.在项目的 build.gradle 中加入以下
	``` java
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	```
   2.在 module 的 build.gradle 中加入以下
	``` java
	dependencies {
		implementation 'com.github.ceneax:APP-Lib-CCV:v1.0.1'
	}
	```

 - 离线集成<br/>
   直接在 Android Studio 中把 “library_ccv” 文件夹作为模块引入到你的工程即可

### 使用方法：

 1. 初始化：在 Application 的 onCreate 中加入以下代码
    ``` java
    @Override
    public void onCreate() {
        Detector.init(this);
    }
    ```

 2. 主要方法
    ``` java
    /**
     * Example: Utils.bitmapToMat(bmp, mat);
     * 传入 [bmp] 参数，将 [Bitmap] 对象转换为 [Mat] 对象，结果存放到 [mat] 参数中
     */
    public static void bitmapToMat(Bitmap bmp, Mat mat) {}
    
    /**
     * Example: Utils.matToBitmap(mat, bmp);
     * 传入 [mat] 参数，将 [Mat] 对象转换为 [Bitmap] 对象，结果存放到 [bmp] 参数中
     */
    public static void matToBitmap(Mat mat, Bitmap bmp) {}
    
    /**
     * Example: Detector.detectFaces(mat, faces);
     * 传入 [mat] 对象，将会对该mat进行人脸检测，会将结果存放到 [faces] 参数中
     */
    public static void detectFaces(Mat mat, MatOfRect faces) {}
    
    /**
     * Example: Detector.recgFace(Mat mat);
     * 传入 [mat] 对象，对该人脸图像进行128D特征数据提取，返回 float[] 128大小的数组
     */
    public static float[] recgFace(Mat mat) {}
    ```
