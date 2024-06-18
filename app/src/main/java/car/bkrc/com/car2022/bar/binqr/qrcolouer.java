package car.bkrc.com.car2022.bar.binqr;


import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class qrcolouer {

    public qrcolouer(){
    }

    // 设定绿色阈值
    private final Scalar LOWER_GREEN = new Scalar(45, 120, 100);
    private final Scalar UPPER_GREEN = new Scalar(77, 255, 255);
    // 设定红色阈值 红色有两种阈值，H[0,10] or H[156,180]
    private final Scalar LOWER_RED = new Scalar(0, 120, 100);
    private final Scalar UPPER_RED = new Scalar(10, 255, 255);
    // 设定黄色阈值
    private final Scalar LOWER_YELLOW = new Scalar(16, 60, 60);
    private final Scalar UPPER_YELLOW = new Scalar(45, 255, 255);
    // 设定橙色阈值
    private final Scalar LOWER_ORANGE = new Scalar(11, 120, 100);
    private final Scalar UPPER_ORANGE = new Scalar(25, 255, 255);
    // 设定青色阈值
    private final Scalar LOWER_CYAN = new Scalar(78, 120, 100);
    private final Scalar UPPER_CYAN = new Scalar(99, 255, 255);
    // 设定蓝色阈值
    private final Scalar LOWER_BLUE = new Scalar(100, 120, 100);
    private final Scalar UPPER_BLUE = new Scalar(124, 255, 255);
    // 设定紫色阈值
    private final Scalar LOWER_PURPLE = new Scalar(125, 120, 100);
    private final Scalar UPPER_PURPLE = new Scalar(155, 255, 255);
    // 设定黑色阈值
    private final Scalar LOWER_BLACK = new Scalar(0, 0, 0);
    private final Scalar UPPER_BLACK = new Scalar(180, 255, 66);

    public int color(Bitmap bitmap, Rect rects){
        Mat mat1 = new Mat();
        Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, false);
//        Bitmap bitmap1 = null;
        Utils.bitmapToMat(rgba, mat1);

        Mat mat2 = new Mat(mat1, rects);


        int cons = 0;
        int cns1 = 0,cns2 = 0,cns3 = 0, cns4 = 0;
        int[] numbers = new int[4];
        while(cons < 4){
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hsv = new Mat();
            Mat mask = new Mat();
            Mat hierarchy = new Mat();
            switch (cons){
                case 0 :
                    cns1 = colorDivision("绿色",LOWER_GREEN, UPPER_GREEN,mat2);
                    cons++;
                    numbers[0] = cns1;
                    System.out.println(cns1);
                    break;
                case 1:
                    cns2 = colorDivision("红色",LOWER_RED, UPPER_RED,mat2);
                    cons++;
                    numbers[1] = cns2;
                    System.out.println(cns2);
                    break;
                case 2 :
                    cns3 = colorDivision("黄色",LOWER_YELLOW, UPPER_YELLOW,mat2);
                    cons++;
                    numbers[2] = cns3;
                    System.out.println(cns3);
                    break;
                case 3 :
                    cns4 = colorDivision("黑色",LOWER_BLACK, UPPER_BLACK,mat2);
                    cons++;
                    numbers[3] = cns4;
                    System.out.println(cns4);
                    break;
                default:break;
            }
        }



        int max = Integer.MIN_VALUE;
        int maxIndex = -1;

        for (int i = 0; i < numbers.length; i++) {
            if (numbers[i] > max && numbers[i] > 0) {
                max = numbers[i];
                maxIndex = i;
            }
        }
        switch (maxIndex){
            case 0:
                System.out.println("绿色");
                break;
            case 1:
                System.out.println("红色");
                break;
            case 2:
                System.out.println("黄色");
                break;
            case 3:
                System.out.println("黑色");
                break;
            default:
                System.out.println("没有识别到");
        }
        return maxIndex;
    }
    private int colorDivision(String name, Scalar lowerb, Scalar upperb,Mat mat1) {
        Mat closed = new Mat();
        Mat roi_hsv = new Mat();
        Imgproc.cvtColor(mat1,mat1, Imgproc.COLOR_RGB2BGR);
        Imgproc.cvtColor(mat1, roi_hsv, Imgproc.COLOR_BGR2HSV);
        Mat mask = new Mat();
        Core.inRange(roi_hsv, lowerb, upperb, mask);
        if (lowerb == LOWER_RED) {
            Scalar lowerb_red = new Scalar(156, 120, 100);
            Scalar upper_red = new Scalar(180, 255, 255);
            Mat mask2 = new Mat();
            Core.inRange(roi_hsv, lowerb_red, upper_red, mask2);
            Core.add(mask, mask2, mask);
        }
//        // 黑白图
//        Mat threshold = new Mat();
//        Imgproc.threshold(mask, threshold, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_TRIANGLE);
//        Log.e("TAG", name + " cols:"+ threshold.cols() + " rows:" + threshold.rows() + " w:" + threshold.width() + " h:" + threshold.height());

//        // 闭运算
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));
//        Imgproc.morphologyEx(mask, closed, Imgproc.MORPH_CLOSE, kernel);
        // 查找二进制图像中的轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        return contours.size();
    }


}



//    Mat image_Processing(Mat imgOriginal)
//    {
//        Mat imgHSV = new Mat();
//        Mat imgBGR = new Mat();
//        Mat imgThresholded = new Mat();
//        GaussianBlur(imgOriginal, imgOriginal, new Size(7, 7), 0, 0);
//        List<Mat> hsvSplit = new ArrayList<>();   //创建向量容器，存放HSV的三通道数据
//        cvtColor(imgOriginal, imgHSV, Imgproc.COLOR_RGB2HSV); //Convert the captured frame from BGR to HSV
//        split(imgHSV, hsvSplit);			//分类原图像的HSV三通道
//        equalizeHist(hsvSplit.get(2), hsvSplit.get(2));    //对HSV的亮度通道进行直方图均衡
//        merge(hsvSplit, imgHSV);				   //合并三种通道
//        Mat ce1 = new Mat();
//        Mat ce2 = new Mat();
//        Mat ce3 = new Mat();
//        Core.inRange(imgHSV, new Scalar(156, 43, 46), new Scalar(180, 255, 255), imgThresholded); //红色
//        Core.inRange(imgHSV, new Scalar(156, 43, 46), new Scalar(180, 255, 255), ce1); //红色
//        Core.inRange(imgHSV, new Scalar(0, 43, 46), new Scalar(3, 255, 255), ce2); //红色
//        Core.add(ce1,ce2,ce3);
//        return ce3;
//    }
//
//}
