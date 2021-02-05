# APP-Lib-CCV
**Android人脸检测与识别库**
基于 [OpenCV](https://github.com/opencv/opencv) 和 [Dlib](https://github.com/davisking/dlib)

需要看Demo的话，可以Clone该仓库到本地，然后用Android Studio直接作为工程打开运行即可

### 集成方法：

 - 在线集成
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

 - 离线集成
   直接在 Android Studio 中把 “library_ccv” 文件夹作为模块引入到你的工程即可

### 使用方法：
