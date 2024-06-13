package car.bkrc.com.car2022.bar.opencv4camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.opencv.imgproc.Imgproc.INTER_CUBIC;

/**
 * 描述：添加类的描述
 *
 * @author 百科荣创-攻城狮
 * @time Created on 2021/4/14
 * @update 颜色分割 + 形状识别
 */
public class ShapeRecognizeUtil {
    public static Context m_Context;
    private static final String TAG = "ShapeRecognizeUtil";
    Mat mat = new Mat();
    Mat closed = new Mat();
    List<Shape> shapeList = new ArrayList<>();

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


    public ShapeRecognizeUtil(Context context, Bitmap bitmap){
        m_Context=context;
         mat = Utils.bitmapToMat(bitmap, mat);
        imgCapture();
        Imgproc.cvtColor(mat,mat, Imgproc.COLOR_RGB2BGR);
    }

    public Mat getClosed() {
        return closed;
    }

    public List<Shape> getShapeList() {
        return shapeList;
    }

    public void ShapeListClear(){
        shapeList.clear();
    }

    public void run(){
        colorDivision("绿色");
        colorDivision("红色");
        colorDivision("黄色");
        colorDivision("橙色");
        colorDivision("青色");
        colorDivision("蓝色");
        colorDivision("紫色");
        colorDivision("黑色");
    }

    public void colorDivision(String name){
        if ("绿色".equals(name))
            colorDivision("绿色", LOWER_GREEN, UPPER_GREEN);
        else if ("红色".equals(name))
            colorDivision("红色", LOWER_RED, UPPER_RED);
        else if ("黄色".equals(name))
            colorDivision("黄色", LOWER_YELLOW, UPPER_YELLOW);
        else if ("橙色".equals(name))
            colorDivision("橙色", LOWER_ORANGE, UPPER_ORANGE);
        else if ("青色".equals(name))
            colorDivision("青色", LOWER_CYAN, UPPER_CYAN);
        else if ("蓝色".equals(name))
            colorDivision("蓝色", LOWER_BLUE, UPPER_BLUE);
        else if ("紫色".equals(name))
            colorDivision("紫色", LOWER_PURPLE, UPPER_PURPLE);
        else if ("黑色".equals(name))
            colorDivision("黑色", LOWER_BLACK, UPPER_BLACK);
    }

    private void imgCapture(){
        // 将原图像灰度化
        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);
        // 平均滤波
        Mat blur = new Mat();
        Imgproc.blur(gray, blur, new Size(5, 5));
        Log.e("TAG11","原参宽："+mat.width()+","+"原参高："+mat.height());
        // 简单阈值的二值化
        Mat thresh = new Mat();
        Imgproc.threshold(blur, thresh, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_TRIANGLE);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        double area_max = 0;
        MatOfPoint _cnt = null;
        for (MatOfPoint cnt:contours) {
            double area = Imgproc.contourArea(cnt);
            if (area > area_max){
                area_max = area;
                _cnt = cnt;
            }
        }
        if (_cnt == null)
            return;
        Rect rect = Imgproc.boundingRect(_cnt);
//        Rect rect2=rect;
//        rect2 = new Rect(100, 100, 80, 80);
        //mat = cutImage(mat, rect);
        //Imgcodecs.imwrite("F:\\cutResult1.jpg", cutImage1);
        //图片裁剪
        mat = new Mat(mat, rect);
        double fx = 1;
        double fy = 1;
        //存储图片
//        File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"cutresult.jpg");
//        if (!file.exists()) {
//            try {
//                file.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        //Mat dst =new Mat();
        //Path path = Environment.getExternalStorageState().
        Imgproc.resize(mat,mat,new Size(0,0), fx, fy,INTER_CUBIC);
        //Imgcodecs.imwrite(file.getPath(), mat);
        //cs
        // 加载需要被蒙太奇的图片，原图
//        Mat dst = new Mat();
//        Imgproc.pyrUp(mat, dst);
//        mat = dst;
        //
        Log.e("TAG11","宽："+mat.width()+","+"高："+mat.height());
    }


    private void colorDivision(String name, Scalar lowerb, Scalar upperb){
        Mat roi_hsv = new Mat();
        Imgproc.cvtColor(mat, roi_hsv, Imgproc.COLOR_BGR2HSV);
        Mat mask = new Mat();
        Core.inRange(roi_hsv, lowerb, upperb, mask);
        if (lowerb == LOWER_RED){
            Scalar lowerb_red = new Scalar(156, 120, 100);
            Scalar upper_red = new Scalar(180, 255, 255);
            Mat mask2 = new Mat();
            Core.inRange(roi_hsv, lowerb_red, upper_red, mask2);
            Core.add(mask, mask2, mask);
        }
        // 黑白图
        Mat threshold = new Mat();
        Imgproc.threshold(mask, threshold, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_TRIANGLE);
        Log.e(TAG, name + " cols:"+ threshold.cols() + " rows:" + threshold.rows() + " w:" + threshold.width() + " h:" + threshold.height());

        // 闭运算
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3));
        Imgproc.morphologyEx(threshold, closed, Imgproc.MORPH_CLOSE, kernel);
        // 查找二进制图像中的轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(closed, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Log.e(TAG, "裁剪后轮廓识别个数为：" + contours.size());
        // MatOfPoint 2通道整数向量（可以认为是具有整数坐标的2D点列表）
        for (MatOfPoint mop: contours) {
            // 得到子图像
            Rect mrect = Imgproc.boundingRect(mop);
            // 计算面积
            double area = Imgproc.contourArea(mop);
            int height = mat.height();
            int width = mat.width();
            Log.e("TAG11","面积是："+area);
            // 过滤点面积比较小的轮廓
            //原参50，3000
            if (area < 30 || area > 4000){
                continue;
            }
            // 过滤边框(0,0,w,h)
            if (mrect.x < 5 || mrect.y < 5 || (mrect.x >= width - 5 && mrect.x <= width
                    || (mrect.y >= height - 5 && mrect.y <= height))){
                Log.e(TAG, "object size  colorDivision: " + mrect.x + " " + mrect.y + " " + width + " " + height);
                continue;
            }
            String shape = getShape(mop);

            if (shape != null){
                Log.e(TAG, "shape: " + shape + ", 面积: " + area);
                shapeList.add(new Shape(name, shape, mrect.x, mrect.y, mrect.width, mrect.height));

            }
        }
    }

    private String getShape(MatOfPoint mop){

        double area = Imgproc.contourArea(mop);
        MatOfPoint2f mop2f = new MatOfPoint2f(mop.toArray());
        // 计算弧长
        double arcLength = Imgproc.arcLength(mop2f, true);
        // 以指定的精度近似多边形曲线
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        Imgproc.approxPolyDP(mop2f, approxCurve, 0.03 * arcLength, true);
        int count = approxCurve.toArray().length;
        // 绘制
//        Mat image = Mat.zeros(m.size(), CvType.CV_8UC3);
//        Imgproc.drawContours(image , list, 0, new Scalar(255,255,0));
        Log.e(TAG, "顶点数Count: " + count);

        Point[] points = approxCurve.toArray();
        // 三角形判断
        if (count == 3){
            // 三个顶点，返回结果是[[507 408]]
            Point a = points[0];
            Point b = points[1];
            Point c = points[2];
            // 三个顶点对应的角度 （单位：度）
            int angleA = calculatingAngle(b,c,a);
            int angleB = calculatingAngle(a,c,b);
            int angleC = calculatingAngle(a,b,c);
            // 最大角
            Integer[] numbers = {angleA, angleB, angleC};
            int angleMax = (int) Collections.max(Arrays.asList(numbers));
            // 若cosA>0 或 tanA>0（A为最大角），则为锐角三角形，84-96
            // cos函数是以弧度作为参数
            if (Math.cos(Math.toRadians(angleMax)) > 0.05)
                return "锐角三角形";
            else if (Math.cos(Math.toRadians(angleMax)) < -0.05)
                return "钝角三角形";
            else return "直角三角形";
        }
        // 四边形判断
        else if (count == 4){
            // 四个顶点
            Point a = points[0];
            Point b = points[1];
            Point c = points[2];
            Point d = points[3];
            // 四个顶点对应的角度 （单位：度）
            int angleA = calculatingAngle(b, d, a);
            int angleB = calculatingAngle(a, c, b);
            int angleC = calculatingAngle(b, d, c);
            int angleD = calculatingAngle(a, c, d);
            //cs
            Log.e("TAG111","四个角："+angleA+","+angleB+","+angleC+","+angleD+"。");
            //
            // 直线ab边长 （单位：像素）
            int ab = calculatingDistance(a,b);
            // 直线bc边长 （单位：像素）
            int bc = calculatingDistance(b,c);
            // 最大角
            Integer[] numbers = {angleA, angleB, angleC, angleD};
            int angleMax = (int) Collections.max(Arrays.asList(numbers));
            //cs
            Log.e("TAG11", "最大角："+angleMax+"临边差："+ Math.abs(ab - bc));
            //
            //原参0.07
//            if (Math.cos(Math.toRadians(angleMax)) < -0.07 && Math.abs(ab - bc) < 5){
//                Log.e(TAG, "菱形: " + angleMax);
//                return "菱形";
//            }
            //原参0.07
            if (Math.cos(Math.toRadians(angleMax)) < -0.09){
                //原参5
                if(Math.abs(ab - bc) < 7)
                {
                    Log.e(TAG, "菱形: " + angleMax);
                    return "菱形";
                }else return "平行四边形";
            }
            else if (Math.abs(ab - bc) < 5)
                return "正方形";
            else return "长方形";
        }
        // 五角星
        else if (count == 10)
            return "五角星";
        else {
            // 弧长和面积的比值
            // 筛选出圆形
            // 圆半径
            double r = arcLength / (2* Math.PI);
            double pi = area / (r*r);
            if (Math.abs(pi - Math.PI) < 1.0)
                return "圆形";
            else {
                // N边形
                return null;
            }
        }
    }

    public Mat cv2ImgAddRect(){
        Mat mat1 = mat.clone();
        Imgproc.cvtColor(mat1, mat1, Imgproc.COLOR_BGR2RGB);
        for (Shape shape: shapeList) {
            Log.e(TAG, "cv2ImgAddRect: " + shape.x + " " + shape.y + " " +  shape.w + " " + shape.h);
            if (shape.name != null){
                Imgproc.rectangle(mat1, new Point(shape.x, shape.y),
                        new Point(shape.x + shape.w, shape.y + shape.h),
                        new Scalar(225,0,0),1);
            }
        }
        return mat1;
    }

    private int calculatingAngle(Point p1, Point p2, Point p0){
        // 从三个坐标点中计算角度
        //  p0 是交点
        double x1 = p1.x - p0.x;
        double y1 = p1.y - p0.y;
        double a = (int)(Math.sqrt(x1*x1 + y1*y1));
        double x2 = p2.x - p0.x;
        double y2 = p2.y - p0.y;
        double b = (int)(Math.sqrt(x2*x2 + y2*y2));
        double x3 = p2.x - p1.x;
        double y3 = p2.y - p1.y;
        double c = (int)(Math.sqrt(x3*x3 + y3*y3));
//cs
        Log.e("TAG11","三边长："+a+","+b+","+c);
        Log.e("TAG11","三个点坐标："+p1.x+","+p1.y+","+p2.x+","+p2.y+"；"+p0.x+","+p0.y+";");
        double angle1 = (a*a+b*b-c*c)/(2*a*b);
        double angle = (x1*x2 + y1*y2)/ Math.sqrt((x1*x1 + y1*y1)*(x2*x2 + y2*y2));
        Log.e("TAG11","原参："+angle+","+"改参："+angle1);
        return (int)(Math.acos(angle)*180/ Math.PI);
    }

    private int calculatingDistance(Point p0, Point p1){
        // 从已知道的两个点计算两点之间距离
        double x1 = p1.x - p0.x;
        double y1 = p1.y - p0.y;
        int dis = (int)(Math.sqrt(x1*x1 + y1*y1));
        return dis;
    }


    public Mat cutImage(Mat src, Rect rect) {
        //图片裁剪
        Mat src_roi = new Mat(src, rect);
        Mat cutImage = new Mat();
        src_roi.copyTo(cutImage);
        return cutImage;
    }


    public class Shape{

        public String name, shape;
        public int x, y, w, h;


        public Shape(String name, String shape, int x, int y, int w, int h){
            this.name = name;
            this.shape = shape;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        @Override
        public String toString() {
            return "Shape{" +
                    "name='" + name + '\'' +
                    ", shape='" + shape + '\'' +
                    ", x=" + x +
                    ", y=" + y +
                    ", w=" + w +
                    ", h=" + h +
                    '}';
        }
    }
}
