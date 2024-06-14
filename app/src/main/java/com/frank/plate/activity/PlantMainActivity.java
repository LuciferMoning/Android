package com.frank.plate.activity;

import static car.bkrc.com.car2022.ViewAdapter.InfrareAdapter.context;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.frank.plate.PlateRecognition;
import com.frank.plate.thread.RecognizeThread;
import com.frank.plate.util.ImageUtil;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import car.bkrc.com.car2022.ActivityView.FirstActivity;
import car.bkrc.com.car2022.Main.Action;


//import car.bkrc.com.FirstActivity;
//
//import static car.bkrc.right.fragment.RightFragment1.Plate_ShiBie;

public class PlantMainActivity {

    private static final String TAG = PlantMainActivity.class.getSimpleName();

    public static ImageUtil imageUtil;
    public static PlateRecognition plateRecognition;
    public static RecognizeThread recognizeThread;
    public static Mat mat;
    public  static boolean loadedOpenCV;
    public static Context m_context;
    public static int falg_plant = 0;
    private static Action action = new Action();
    static int cns = 0;
    private static ImageView img_plate;


    public static String ss1 ;

    //load openCV library
    static {
        if(OpenCVLoader.initDebug()) {
            Log.d(TAG, "openCV load_success...");
            loadedOpenCV = true;
        } else {
            Log.d(TAG, "openCV  load fail...");
            loadedOpenCV = false;
        }
    }

    @SuppressLint("HandlerLeak")
    public static Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.e("消息",  ""+msg.what);
            //super.handleMessage(msg);
            switch (msg.what){
                case PlateRecognition.MSG_RESULT://recognize finish

                    falg_plant = 1;
                    String result = (String) msg.obj;
                    Toast.makeText(m_context, "车牌识别结果："+result, Toast.LENGTH_LONG).show();
                    String ss = "";

                    int num = 0;
                    int m=0;
                    char[] charArray = result.toCharArray();
                    for(int i =0;i<result.length();i++) {
                        if(isChinese(result.charAt(i)) || (result.charAt(0) >= '0')) {
                            m=i;
                            for (int j = m; j < charArray.length; j++) {
                                if ((charArray[j] >= 'A' && charArray[j] <= 'Z') || (charArray[j] >= '0' && charArray[j] <= '9')) {
                                    num++;
                                    if (num <= 6) {
                                        ss += "" + charArray[j];
                                    }
                                }
                            }
                            break;//让其从第一个中文开始索引
                        }
                    }
                    Log.e("TAG1", "最后处理plantresult:"+ss);
                    action.resultplan(ss);
//                    if(TextUtils.isEmpty(ss)){
//                        Log.e("TAG11","车牌识别为空");
//
//
//                        //Plate_ShiBie();
//                    }else{
//                        char[] charArray1 = ss.toCharArray();
//                        for (int i = 0; i < 3; i++) {
////                           FirstActivity.Connect_Transport.chepai((byte)charArray1[4],(byte)charArray1[5],(byte)charArray1[6],(byte)charArray1[7],(byte)charArray1[8],(byte)charArray1[9]);
//                            // FirstActivity.Connect_Transport.chepai((byte)'A',(byte)'1',(byte)'2',(byte)'3',(byte)'4',(byte)'5');
////                            FirstActivity.Connect_Transport.chepaitext((byte)charArray1[7],(byte)charArray1[8],(byte)charArray1[9],(byte)charArray1[9]);
//                            Log.i("cccc",(byte)charArray1[4]+"");
//
//                        }
//                        FirstActivity.Connect_Transport.plan((byte)charArray1[4],(byte)charArray1[5],(byte)charArray1[6],(byte)charArray1[7],(byte)charArray1[8],(byte)charArray1[9]);
//                    }
//                    ss1 = ss;
//                    FirstActivity.Connect_Transport.TFT_LCD(0x0B,0x10, 0x02, 0x00, 0x00);
                    ss = null;
//                    num = 0;
                    break;
                case 333:
                    FirstActivity.Connect_Transport.TFT_LicensePlate_PageDown('B');
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x10, 0x02, 0x00, 0x00);
                    for(int j=0; j<10; j++)
                    {
                        for(int i=0; i<500; i++);
                    }
//                    FirstActivity.Connect_Transport.TFT_LCD(0xAA, 0x51, 0x00, 0x00, 0x00);
                    System.out.println("车牌识别次数"+cns);

                case 222:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    startRecognize(bitmap);
                    break;
            }
        }
    };



    public static boolean isChinese(char c) {
        String regEx = "[\u4e00-\u9fa5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(c + "");
        if (m.find())
            return true;
        return false;
    }

    public static void initPlate(Context context){
        plateRecognition = new PlateRecognition(context, mHandler);
        m_context=context;
        //init plate recognizer
        new Thread(new Runnable() {
            @Override
            public void run() {
                plateRecognition.initRecognizer("pr");
            }
        }).start();
    }

    public static void doRecognizePlate(Bitmap bitmap){
        bitmap = BitmapFactory.decodeResource(context.getResources(),1);
        img_plate.setImageBitmap(bitmap);
        Message msg = Message.obtain();
        msg.what = 222;
        msg.obj = bitmap;
        mHandler.sendMessageDelayed(msg, 3000);
    }

    public static void startRecognize(Bitmap bitmap){
        //start plate recognizing
        if(plateRecognition != null){
            if(recognizeThread == null){
                recognizeThread = new RecognizeThread(plateRecognition);
                recognizeThread.start();
            }
            if(mat == null){
                mat = new Mat();
            }
            Utils.bitmapToMat(bitmap, mat);
            recognizeThread.addMat(mat);
        }
    }

}

