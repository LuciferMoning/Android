package car.bkrc.com.car2022.bar.utils;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;


/**
 * @author JZ
 * @time 18/12/2020
 */
public class OpenCVUtils {
    private static OpenCVUtils openCVUtils = null;
    public Bitmap srcBitmap;

    public static OpenCVUtils getInstance(){
        if (null == openCVUtils){
            synchronized (OpenCVUtils.class){
                if (null == openCVUtils){
                    openCVUtils = new OpenCVUtils();
                }
            }
        }
        return openCVUtils;
    }

    /**
     * 图像处理
     * 二值化
     * 腐蚀
     */
    public void proSrc2Gray() {
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        Mat binaryMat = new Mat();
        Mat cannyMat = new Mat();

        //获取彩色图像所对应的像素数据
        Utils.bitmapToMat(srcBitmap, rgbMat);
        //图像灰度化,将彩色图像数据转换为灰度图像数据并存储到grayMat中
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        //得到边缘图,这里最后两个参数控制着选择边缘的阀值上限和下限
//        Imgproc.Canny(grayMat, cannyMat, 50, 300);
        //二值化 ADAPTIVE_THRESH_MEAN_C    THRESH_BINARY
        Imgproc.threshold(grayMat, binaryMat, Imgproc.THRESH_BINARY_INV, 255, 7);
        //获取自定义核,参数MORPH_RECT表示矩形的卷积核，当然还可以选择椭圆形的、交叉型的
        Mat strElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                new Size(2, 2));
        //腐蚀
        Imgproc.erode(binaryMat,cannyMat,strElement);
        Imgproc.dilate(binaryMat,cannyMat,strElement);

        //Hough变换倾斜校正
        Imgproc.HoughLinesP(binaryMat,cannyMat,1,3.14/180,1);

        //创建一个图像
        ImageUtils.getInstance().mBitmap = Bitmap.createBitmap(grayMat.cols(), grayMat.rows(),
                Bitmap.Config.RGB_565);
        //将矩阵binaryMat转换为图像

        Utils.matToBitmap(grayMat, ImageUtils.getInstance().mBitmap);
    }
    public static String str_ocr = "";
    public static int flag_txt;

    /**
     * 识别图像
     * @param activity
     * @param bitmap
     */
    public void recognition(final AppCompatActivity activity, final Bitmap bitmap) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                /**
                 * 检测sd卡是否存在语言库
                 * 若不存在，从assets获取到本地sd卡
                 */
                if (!checkTrainedDataExists()) {
                    SDUtils.assets2SD(activity.getApplicationContext(), PathUtils.LANGUAGE_PATH, PathUtils.DEFAULT_LANGUAGE_NAME);
                }


                TessBaseAPI tessBaseAPI = new TessBaseAPI();
                tessBaseAPI.setDebug(true);
                tessBaseAPI.init(PathUtils.DATAPATH, PathUtils.DEFAULT_LANGUAGE);

                //识别的图片
                tessBaseAPI.setImage(bitmap);

                //获得识别后的字符串
                String text = "";
                text = tessBaseAPI.getUTF8Text();
                str_ocr = "";
                int zhongwen = 0;
                int yingwen = 0;
                int shuzi = 0;
                for (int i = 0; i <text.length(); i++) {
                    if(isChineseCharacter(text.charAt(i))||(text.charAt(i)>='0'&&text.charAt(i)<='9')||(text.charAt(i)>='A'&&text.charAt(i)<='z')){
                        if(isChineseCharacter(text.charAt(i))){
                            zhongwen++;
                        }
                        if((text.charAt(i)>='0'&&text.charAt(i)<='9')){
                            shuzi++;
                        }
                        if((text.charAt(i)>='A'&&text.charAt(i)<='z')){
                            yingwen++;
                        }

                        str_ocr+=text.charAt(i);
                    }
                }
                if(zhongwen!=0&&yingwen!=0&&shuzi!=0){
                    flag_txt  = 1;
                }


                String s =text;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

//                        String str = "";
//                        for (int i = 0; i < finalText.length(); i++) {
//                            if ((finalText.charAt(i) >= 48 && finalText.charAt(i) <= 57) ||
//                                    (finalText.charAt(i) >= 65 && finalText.charAt(i) <= 90)) {
//                                str += finalText.charAt(i);
//                            }
//                        }
                        Log.e("dasda", s);

                    }
                });
                tessBaseAPI.end();
            }
        }).start();
    }



    public  boolean isChineseCharacter(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;// 根据字节码判断
    }

    /**
     * 检测sd卡是否存在语言库
     * 若不存在，从assets获取到本地sd卡
     */
    private boolean checkTrainedDataExists() {
        File file = new File(PathUtils.LANGUAGE_PATH);
        return file.exists();
    }
}
