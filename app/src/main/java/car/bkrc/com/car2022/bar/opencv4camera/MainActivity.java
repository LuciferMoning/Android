   package car.bkrc.com.car2022.bar.opencv4camera;


import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

   public class MainActivity {
       public static Context m_Context;



       public static void initOpenCV(Context context) {
           m_Context=context;
           if (OpenCVLoader.initDebug()) {
               // 使用JniLibs文件夹下的动态库初始化OpenCV
               mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
           }
       }


       public static BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(m_Context) {
           @Override
           public void onManagerConnected(int status) {
           }
       };

       public static StringBuilder sb;
       public static ShapeRecognizeUtil sru;
       public static void getshape(Context context, Bitmap bitmap){
           initOpenCV(m_Context);
           sru = new ShapeRecognizeUtil(context, bitmap);
           sru.run();
           sb = new StringBuilder();

           for (ShapeRecognizeUtil.Shape shape:MainActivity.sru.getShapeList()) {
               sb.append(shape.name + shape.shape +"1个"+ "\n");
           }
           Toast.makeText(context,sb, Toast.LENGTH_LONG).show();
       }
   }
