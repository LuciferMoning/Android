package car.bkrc.com.car2022.FragmentView;

import static car.bkrc.com.car2022.ActivityView.FirstActivity.Connect_Transport;
import static car.bkrc.com.car2022.ActivityView.FirstActivity.toastUtil;
import static car.bkrc.com.car2022.FragmentView.LeftFragment.bitmap;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bkrcl.control_car_video.camerautil.CameraCommandUtil;
import com.frank.plate.activity.PlantMainActivity;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import car.bkrc.com.car2022.ActivityView.FirstActivity;
import car.bkrc.com.car2022.ActivityView.LoginActivity;
import car.bkrc.com.car2022.DataProcessingModule.ConnectTransport;
import car.bkrc.com.car2022.MessageBean.DataRefreshBean;
import car.bkrc.com.car2022.MessageBean.StateChangeBean;
import car.bkrc.com.car2022.R;
import car.bkrc.com.car2022.Utils.CameraUtile.XcApplication;
import car.bkrc.com.car2022.Utils.OtherUtil.WiFiStateUtil;
import car.bkrc.com.car2022.bar.Text.sumResult;
import car.bkrc.com.car2022.bar.binqr.RE;
import car.bkrc.com.car2022.bar.binqr.qrcolouer;
import car.bkrc.com.car2022.bar.ocr.TestInferOcrTask;
import car.bkrc.com.car2022.bar.opencv4camera.MainActivity;
import car.bkrc.com.car2022.bar.yolov5.YoloV5Ncnn;
public class RightFragment1 extends Fragment {

    TensorFlowInferenceInterface inferenceInterface;
    private YoloV5Ncnn yolov5ncnn = new YoloV5Ncnn();
    private TestInferOcrTask testInferOcrTask = new TestInferOcrTask();


    String Camera_show_ip = null;
    private CameraCommandUtil cameraCommandUtil;
    private String[] stringList = new String[]{"unmasked","masking"};
    public static int arr = 0;
    public static int[] arr1 = {0,0,0,0};
    public static int[] arr2 = {0,0,0,0,0,0,0,0,0};
    private String[] stringList1 = new String[]{"car","bicycle","truck", "motorcycle"};
    private String[] stringList2 = new String[]{"Speed", "left", "noleft", "noright", "notravel", "right", "travel", "turn","noturn"};
    private TextView Data_show = null;
    private EditText speededit = null;
    private EditText coded_discedit = null;
    private EditText angle_dataedit = null;
    private TextView stateTV, psStatusTV, codedDiskTV, lightTV, ultraSonicTV;
    private Button btnShape;
    private Button btnTrafficLight;
    private Button btnqr;
    private Button btnText;
    private static Button btn_license_plate;
    private Button bt_traffic_sign;
    private static int reds = 0,yellows = 0,greens = 0;
    private Button bt_set_initial,bt_start_initial,bt_left,bt_right,bt_up,bt_down,bt_set_left,bt_start_left,bt_set_right,bt_start_right,btn_position,btn_Carshape;

    public static final String TAG = "RightFragment1";
    private View view = null;

    private boolean dateGetState = true; // 主从车接收状态切换
    private static car.bkrc.com.car2022.bar.binqr.qrcolouer qrcolouer = new qrcolouer();
    private static RE re = new RE();
    private int stop_flag;

    Dialog dia;

    sumResult sumResult = new sumResult();

    ConnectTransport connectTransport = new ConnectTransport();

    /*
        比赛时更改需要定义的条件
     */
    String strtxt = "";
    int inColor = 0;
    String longestSubstring = "";
    boolean qr = true,plan = true,tux = true,po = true,jt = true;
    int ju_num = 0;
    int yuan_num = 0;
    int sj_num = 0;
    int lin_num=0;
    int wuj_num = 0;
    int cns2 = 0;
    String cn = "",cout = "01",AB = "0",CD = "0";
    char[] planss  = new char[10];
    //
    public int getStop_flag() {
        return stop_flag;
    }

    public static RightFragment1 getInstance() {
        return RightFragment1Holder.sInstance;
    }
    private static class RightFragment1Holder {
        private static final RightFragment1 sInstance = new RightFragment1();
    }
    int k  =0;
    // 接受显示设备发送的数据
    @SuppressLint("HandlerLeak")
    private Handler rehHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        public void handleMessage(Message msg) {

            if (msg.what == 1) {
                byte[] mByte = (byte[]) msg.obj;
                if (mByte[0] == 0x55) {
                    show_ID(mByte[10] & 0xff);// 获取标志位
                    // 光敏状态
                    long psStatus = 0;
                    if ((mByte[2] & 0xff) != 0xA7) {
                        psStatus = mByte[3] & 0xff;
                    }
                    // 超声波数据
                    long ultraSonic = mByte[5] & 0xff;
                    ultraSonic = ultraSonic << 8;
                    ultraSonic += mByte[4] & 0xff;
                    // 光照强度
                    long light = mByte[7] & 0xff;
                    light = light << 8;
                    light += mByte[6] & 0xff;
                    // 码盘
                    long codedDisk = mByte[9] & 0xff;
                    codedDisk = codedDisk << 8;
                    codedDisk += mByte[8] & 0xff;
                    Camera_show_ip = FirstActivity.IPCamera;
                    if (mByte[1] == (byte) 0xaa) {  //主车
                        if (dateGetState) {
                            // 显示数据
                            stateTV.setText(mByte[2] + "");           // 运行状态
                            psStatusTV.setText(psStatus + "");        // 光敏电阻
                            codedDiskTV.setText(codedDisk + "");      // 码盘
                            lightTV.setText(light + " lx");           // 光照度
                            ultraSonicTV.setText(ultraSonic + " mm"); // 超声波
                        }
                    }
                    if (mByte[1] == (byte) 0x02) //从车
                    {
                        if (!dateGetState) {
                            if (mByte[2] == -110) {
                                byte[] newData = new byte[50];
                                Log.e("data", "" + mByte[4]);
                                newData = Arrays.copyOfRange(mByte, 5, mByte[4] + 5);
                                Log.e("data", "" + "长度" + newData.length);
                                try {
                                    String str = new String(newData, "ascii");//第二个参数指定编码方式
                                    Toast.makeText(getActivity(), "" + str, Toast.LENGTH_LONG).show();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // 显示数据
                                stateTV.setText(mByte[2] + "");              // 运行状态
                                psStatusTV.setText(psStatus + "");        // 光敏电阻
                                codedDiskTV.setText(codedDisk + "");      // 码盘
                                lightTV.setText(light + " lx");           // 光照度
                                ultraSonicTV.setText(ultraSonic + " mm"); // 超声波
                            }
                        }
                    }
                    if (mByte[1] == (byte) 0x03) {
                        if (mByte[2] == (byte) 0x01) {
                            control_init();
                        } else if (mByte[2] == (byte) 0x02) {
                            control_init();
                        }
                    }
                }
            }else if (msg.what == 2){
                toastUtil.ShowToast("当前未连接到设备，请连接后重试！");
            }
            if (msg.what == 11){
                byte[] mByte = (byte[]) msg.obj;
                if(mByte[2] == 0x02){//二维码识别

                    connectTransport.receive( 2);
                    connectTransport.yanchi(1000);
                    qr(5);
                    qr(5);

                }else if(mByte[2] == 0x04){//车牌识别

                    connectTransport.receive( 2);
                    plan(mByte[3]);

                }else if(mByte[2] == 0x03){//信号灯识别
                    bt_start_initial.performClick();
                    bt_up.performClick();
                    bt_up.performClick();
                    connectTransport.yanchi(3000);
                    connectTransport.receive( 2);
                    trafficLightDis(bitmap);
                    iniColor(bitmap);

                    if(inColor!=0){
                        switch (mByte[3] ){
                            case 1:
                                Connect_Transport.traffic_control(0x0E, 0x02, inColor);
                                break;
                            case 2:
                                Connect_Transport.traffic_control(0x0F, 0x02, inColor);
                                break;
                            case 3:
                                Connect_Transport.traffic_control(0x13, 0x02, inColor);
                                break;
                            case 4:
                                Connect_Transport.traffic_control(0x14, 0x02, inColor);
                                break;
                        }
                    }
                    bt_down.performClick();
                    bt_down.performClick();
                    connectTransport.receive2();

                }else if(mByte[2] == 0x05){//行人戴口罩
                    connectTransport.receive( 2);
                    position();
                }else if(mByte[2] == 0x06){//交通标志物
                    connectTransport.receive( 2);
                    Traffic_Sign();
                    jtbz();
                }else if(mByte[2] == 0x07){//车型识别
                    connectTransport.receive( 2);
                    shapCar();
                }else if(mByte[2] == 0x08){//图像识别
                    connectTransport.receive( 2);
                    TuxiangShibie();
                }else if(mByte[2] == 0x09){//多识别
                    connectTransport.receive( 2);
                    tft();
                }
                else if(mByte[2] == 0x0A){//汉字识别
                    connectTransport.receive( 2);
                    text();
                    connectTransport.receive( 2);
                }else if(mByte[2] == 0x11){ //打开道闸
                    connectTransport.receive( 2);
                    Connect_Transport.gate(0x10, planss[0], planss[1], planss[2]);
                    Connect_Transport.yanchi(500);
                    Connect_Transport.gate(0x11, planss[3], planss[4], planss[5]);
                    Connect_Transport.yanchi(500);
                    Connect_Transport.gate(0x01, 0x01, 0x00, 0x00);

                }else if(mByte[2] == 0x10){ //立体显示标志物
                    connectTransport.receive( 2);
                    String str = "A"+ju_num+"B"+lin_num+"C"+sj_num+""+cns2+""+5+""+arr;
                    toastUtil.ShowToast(str);

                    if(str == null){
                        return;
                    }else {
                        try {
                            byte[] bytes = str.getBytes("gbk");
                            System.out.println(Arrays.toString(bytes));
                            connectTransport.zigbeeSendData(bytes, bytes.length);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else if(mByte[2] == 0x12){//报警台
                    connectTransport.receive( 2);
                    connectTransport.yanchi(500);
                    connectTransport.infrared((byte) 0x03, (byte) 0x05,
                            (byte) 0x14, (byte) 0x45, (byte) 0xDE,
                            (byte) 0x92);
                    connectTransport.yanchi(500);
                }
                else if(mByte[2] == 0x13){//发送八位数据
                    connectTransport.receive( 2);
//                    Connect_Transport.sendData();


                }

            }

        }
    };

    private void chushi(){

        bt_start_initial.performClick();
    }
    private void jtbz(){
        while(jt){
            Traffic_Sign();
            connectTransport.yanchi(300);
            for (int i = 0; i <arr2.length ; i++) {
                if(arr2[i] !=0 ){

                }
            }
        }
    }
    private void text(){
//        chushi();
//        bt_start_left.performClick();
        String str = null;
        str = testInferOcrTask.ocr();

        if(str != null){
            strtxt = str;
            try {
                byte[] bytes = bytesend(str.getBytes("gbk"));
                Log.e("text_send_ok", str);
                Connect_Transport.send_voice(bytes);
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }else{
//            FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x10, 0x02, 0x00, 0x00);
        }
//        bt_start_initial.performClick();

    }
    private void tft(){
        int num = 0,cns = 0;
        while(tux && po ){
            if(arr > 0){
                po = false;
            }
            num = 0;
            while (num < 2){
                switch (num){
                    case 1:
                        if(tux){
                            TuxiangShibie();
                        }
                        num++;
                        break;
                    case 0:
                        if(po){
                            position();
                        }
                        num++;
                        break;
                }
            }
            Connect_Transport.TFT_LCD(0x08, 0x10, 0x02, 0x00, 0x00);
        }

        return;
    }
    public void resultplan(String result1 ){
        System.out.println("识别车牌结果为"+result1);
        planss = new char[10];
        planss = result1.toCharArray();

    }
    /*
    1 识别绿色二维码
    2 识别红色二维码
    3 识别黄色二维码
    4 识别黑色二维码
    5 识别全部二维码
    -1 红色识别不出来
     */
    private void qr(int i){//二维码识别
//        bt_start_left.performClick();
        int[] arr = new int[10];
        setQr();
        for(int j = 0;j<k ;j++){
            arr[j]= qrcolouer.coler(bitmap,rect[j]);
        }
        System.out.println(Arrays.toString(arr));
        if(i == 5){
            Qr_recognition(bitmap);
            for(int j = 0; j < k;j++){
                System.out.println("次数"+j);
                Bitmap bmp = Bitmap.createBitmap(bitmap,(int)(rect[j].x),(int)(rect[j].y),(int)(rect[j].width),(int)(rect[j].height));
                Qr_recognition(bmp);
//                Qr_recognition(bmp);
            }

        }else{
            for(int j = 0; j < k;j++){
                if(arr[j] == i){
                    Bitmap bmp = Bitmap.createBitmap(bitmap,rect[i].x,rect[i].y,rect[i].width,rect[i].height);
                    Qr_recognition(bmp);
//                    Qr_recognition(bmp);
                }

            }
        }
    }


    public static void plan(int i){//车牌识别

        if(i == 1){
            Plate_ShiBie2();
        }else{
            Plate_ShiBie();
        }
        btn_license_plate.performClick();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        } else {
            if (LoginActivity.isPad(getActivity()))
                view = inflater.inflate(R.layout.right_fragment1, container, false);
            else
                view = inflater.inflate(R.layout.right_fragment1_mobilephone, container, false);
        }

        
        FirstActivity.recvhandler = rehHandler;
        cameraCommandUtil = new CameraCommandUtil();
        // 获取当前上下文对象.
        Context context = getContext().getApplicationContext();


        EventBus.getDefault().register(this); // EventBus消息注册
        control_init();
        connect_Open();
        PlantMainActivity.initPlate(getActivity());

        return view;
    }

    /**
     * 页面初始化
     */
    private void control_init() {
        Data_show = view.findViewById(R.id.rvdata);
        speededit = view.findViewById(R.id.speed_data);
        coded_discedit = view.findViewById(R.id.coded_disc_data);
        angle_dataedit = view.findViewById(R.id.angle_data);
        stateTV = view.findViewById(R.id.stateTV);
        psStatusTV = view.findViewById(R.id.psStatusTV);
        codedDiskTV = view.findViewById(R.id.codedDiskTV);
        lightTV = view.findViewById(R.id.lightTV);
        ultraSonicTV = view.findViewById(R.id.ultraSonicTV);
        btnShape = view.findViewById(R.id.btn_shape);
        btnTrafficLight = view.findViewById(R.id.btn_traffic_light);
        btnqr = view.findViewById(R.id.btn_qr);
        btnText = view.findViewById(R.id.btn_text);
        btn_license_plate = view.findViewById(R.id.btn_plate);
        bt_traffic_sign = view.findViewById(R.id.bt_traffic_sign);
        bt_set_initial = view.findViewById(R.id.bt_set_initial);
        bt_start_initial = view.findViewById(R.id.bt_start_initial);
        bt_left =  view.findViewById(R.id.bt_left);
        bt_right =  view.findViewById(R.id.bt_right);
        bt_up =  view.findViewById(R.id.bt_up);
        bt_down =  view.findViewById(R.id.bt_down);
        bt_set_left =  view.findViewById(R.id.bt_set_left);
        bt_start_left =  view.findViewById(R.id.bt_start_left);
        bt_set_right =  view.findViewById(R.id.bt_set_right);
        bt_start_right =  view.findViewById(R.id.bt_start_right);
        btn_position = view.findViewById(R.id.btn_position);
        btn_Carshape = view.findViewById(R.id.btn_Carshape);

        ImageButton up_bt = view.findViewById(R.id.up_button);
        ImageButton blew_bt = view.findViewById(R.id.below_button);
        ImageButton stop_bt = view.findViewById(R.id.stop_button);
        ImageButton left_bt = view.findViewById(R.id.left_button);
        ImageButton right_bt = view.findViewById(R.id.right_button);

        bt_set_initial.setOnClickListener(new CameralClickListener());
        bt_start_initial.setOnClickListener(new CameralClickListener());
        bt_left.setOnClickListener(new CameralClickListener());
        bt_right.setOnClickListener(new CameralClickListener());
        bt_up.setOnClickListener(new CameralClickListener());
        bt_down.setOnClickListener(new CameralClickListener());
        bt_set_left.setOnClickListener(new CameralClickListener());
        bt_start_left.setOnClickListener(new CameralClickListener());
        bt_set_right.setOnClickListener(new CameralClickListener());
        bt_start_right.setOnClickListener(new CameralClickListener());
        bt_traffic_sign.setOnClickListener(new onClickListener2());
        btn_license_plate.setOnClickListener(new onClickListener2());
        btnText.setOnClickListener(new onClickListener2());
        btnqr.setOnClickListener(new onClickListener2());
        btnTrafficLight.setOnClickListener(new onClickListener2());
        btnShape.setOnClickListener(new onClickListener2());
        btn_position.setOnClickListener(new onClickListener2());
        btn_Carshape.setOnClickListener(new onClickListener2());
        up_bt.setOnClickListener(new onClickListener2());
        blew_bt.setOnClickListener(new onClickListener2());
        stop_bt.setOnClickListener(new onClickListener2());
        left_bt.setOnClickListener(new onClickListener2());
        right_bt.setOnClickListener(new onClickListener2());
        up_bt.setOnLongClickListener(new onLongClickListener2());
    }

    /**
     * 接收平台连接相关的Eventbus消息
     *
     * @param refresh
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DataRefreshBean refresh) {
        if (refresh.getRefreshState() == 1 && new WiFiStateUtil(getActivity()).wifiInit()) {
            connect_Open();
        } else if (refresh.getRefreshState() == 3) {
            toastUtil.ShowToast("平台已连接");
        } else if (refresh.getRefreshState() == 4) {
            toastUtil.ShowToast("平台连接失败！");
        } else toastUtil.ShowToast("请检查WiFi连接状态！");
    }

    /**
     * 接收主从车信息接收状态Eventbus消息
     *
     * @param stateChangeBean
     */
    @SuppressLint("ResourceAsColor")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventStateThread(StateChangeBean stateChangeBean) {
        if (stateChangeBean.getStateChange() == 0) {
            dateGetState = true;// 显示主车数据
            stateTV.setTextColor(getResources().getColor(R.color.black));                // 运行状态
            psStatusTV.setTextColor(getResources().getColor(R.color.black));             // 光敏电阻
            codedDiskTV.setTextColor(getResources().getColor(R.color.black));            // 码盘
            lightTV.setTextColor(getResources().getColor(R.color.black));                // 光照度
            ultraSonicTV.setTextColor(getResources().getColor(R.color.black));           // 超声波
            toastUtil.ShowToast("相关数据显示为黑色");
        } else if (stateChangeBean.getStateChange() == 1) {
            dateGetState = false;// 显示主车数据
            stateTV.setTextColor(getResources().getColor(R.color.white));             // 运行状态
            psStatusTV.setTextColor(getResources().getColor(R.color.white));         // 光敏电阻
            codedDiskTV.setTextColor(getResources().getColor(R.color.white));        // 码盘
            lightTV.setTextColor(getResources().getColor(R.color.white));             // 光照度
            ultraSonicTV.setTextColor(getResources().getColor(R.color.white));       // 超声波
            toastUtil.ShowToast("相关数据显示为白色");
        } else if (stateChangeBean.getStateChange() == 2) {
            Data_show.setText("主  车");
            speededit.setTextColor(getResources().getColor(R.color.black));
            coded_discedit.setTextColor(getResources().getColor(R.color.black));
            angle_dataedit.setTextColor(getResources().getColor(R.color.black));
            Data_show.setTextColor(getResources().getColor(R.color.black));
        } else if (stateChangeBean.getStateChange() == 3) {
            Data_show.setText("从  车");
            speededit.setTextColor(getResources().getColor(R.color.white));
            coded_discedit.setTextColor(getResources().getColor(R.color.white));
            angle_dataedit.setTextColor(getResources().getColor(R.color.white));
            Data_show.setTextColor(getResources().getColor(R.color.white));
        }
    }

    /**
     * 获取标志位
     * @param id 坐标信息
     */
    private void show_ID(int id) {
        stop_flag = id;
        if (id > 0xA0) {
            toastUtil.ShowToast("随机救援坐标为：" + Integer.toHexString(id).toUpperCase()); // 显示坐标信息
        }
    }

    /**
     * 车辆连接状态判断
     */
    private void connect_Open() {
        if (XcApplication.isserial == XcApplication.Mode.SOCKET) {
            connect_thread();                            //开启网络连接线程
        } else if (XcApplication.isserial == XcApplication.Mode.SERIAL) {
            serial_thread();   //使用纯串口uart4
        }
    }

    /**
     * 建立Socket连接
     */
    private void connect_thread() {
        XcApplication.executorServicetor.execute(new Runnable() {
            @Override
            public void run() {
                Connect_Transport.connect(rehHandler, FirstActivity.IPCar);
            }
        });
    }

    private void serial_thread() {
        XcApplication.executorServicetor.execute(new Runnable() {
            @Override
            public void run() {
                Connect_Transport.serial_connect(rehHandler);
            }
        });
    }

    // 速度和码盘方法
    private int getSpeed() {
        String src = speededit.getText().toString();
        int speed = 90;
        if (!src.equals("")) {
            speed = Integer.parseInt(src);
        } else {
            toastUtil.ShowToast("请输入设备运行速度！");
        }
        return speed;
    }

    private int getEncoder() {
        String src = coded_discedit.getText().toString();
        int encoder = 20;
        if (!src.equals("")) {
            encoder = Integer.parseInt(src);
        } else {
            toastUtil.ShowToast("请输入码盘值！");
        }
        return encoder;
    }

    private int getAngle() {
        String src = angle_dataedit.getText().toString();
        int angle = 50;
        if (!src.equals("")) {
            angle = Integer.parseInt(src);
        } else {
            toastUtil.ShowToast("请输入循迹速度值！");
        }
        return angle;
    }


    // 速度与码盘值
    private int sp_n;

    private class onClickListener2 implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            sp_n = getSpeed();

            switch (v.getId()) {
                case R.id.up_button:
                    int en_n = getEncoder();
                    Connect_Transport.go(sp_n, en_n);
                    break;
                case R.id.left_button:
                    Connect_Transport.left(sp_n);
                    break;
                case R.id.right_button:
                    Connect_Transport.right(sp_n);
                    break;
                case R.id.below_button:
                    en_n = getEncoder();
                    Connect_Transport.back(sp_n, en_n);
                    break;
                case R.id.stop_button:
                    Connect_Transport.stop();
                    break;
                case R.id.btn_shape:
                    TuxiangShibie();
                    break;
                case R.id.btn_traffic_light:

                    trafficLightDis(bitmap);
                    iniColor(bitmap);
                    break;
                case R.id.btn_qr:
                    qr(5);
                    break;
                case R.id.btn_text:
                    String str = null;
                    str = testInferOcrTask.ocr();

                    Log.e(TAG, "汉字识别结果: "+"1"+str );
                    toastUtil.ShowToast(str);

//                    try {
//                        String str1 = "显示标志物";
//                        byte[] bytes = str1.getBytes("gbk");
//                        System.out.println(Arrays.toString(bytes));
//                        connectTransport.zigbeeSendData(bytes,bytes.length);
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }



                        break;
                case R.id.btn_plate:

                    Plate_ShiBie2();

                    break;
                case R.id.bt_traffic_sign:

                    Traffic_Sign();
                    break;
                case R.id.btn_position:
                    position();
                    break;
                case R.id.btn_Carshape:
                    shapCar();
                    break;


            }
        }
    }

    //行人
    private void position(){
        arr = 0;
        boolean ret_init = yolov5ncnn.Init2(getContext().getAssets());
        if (!ret_init)
        {
            Log.e("MainActivity", "yolov5ncnn Init failed");
        }
        if (bitmap == null)
            return;

        YoloV5Ncnn.Obj[] objects = yolov5ncnn.Detect2(bitmap, false);

        showObjects(objects);

        toastUtil.ShowToast("行人戴口罩有"+arr+"人");
    }
    //行人和车型
    private void shapCar() {
        arr1=new int[4];
        boolean ret_init = yolov5ncnn.Init(getContext().getAssets());
        if (!ret_init)
        {
            Log.e("MainActivity", "yolov5ncnn Init failed");
        }
        if (bitmap == null)
            return;

        YoloV5Ncnn.Obj[] objects = yolov5ncnn.Detect(bitmap, false);

        showObjects(objects);
        toastUtil.ShowToast("汽车有"+arr1[0]+"辆,"+"自行车有"+arr1[1]+"辆,"+"货车有"+arr1[2]+"辆,"+"，摩托车有"+arr1[3]+"辆,");
    }
    // 交通标志物
    private void Traffic_Sign() {
        arr2=new int[9];
        boolean ret_init = yolov5ncnn.Init1(getContext().getAssets());
        if (!ret_init)
        {
            Log.e("MainActivity", "yolov5ncnn Init failed");
        }
        if (bitmap == null)
            return;

        YoloV5Ncnn.Obj[] objects = yolov5ncnn.Detect1(bitmap, false);

        showObjects(objects);
        toastUtil.ShowToast("限速"+arr2[0]+"左转"+arr2[1]+"禁止左转"+arr2[2]+"掉头"+arr2[3]+"禁止掉头"+arr2[4]+"右转"+arr2[5]+"禁止右转"+arr2[6]+"直行"+arr2[7]+"禁止直行"+arr2[8]);

    }
    //二维码
    private void setQr(){
        k = 0;
        boolean ret_init = yolov5ncnn.Init3(getContext().getAssets());
        if (!ret_init)
        {
            Log.e("MainActivity", "yolov5ncnn Init failed");
        }
        if (bitmap == null)
            return;

        YoloV5Ncnn.Obj[] objects = yolov5ncnn.Detect3(bitmap, false);

        showObjects(objects);
    }


    private class CameralClickListener implements View.OnClickListener{

        @Override
        public void onClick(View vi) {
            switch (vi.getId()){
                case R.id.bt_set_initial:
                    setCamera(FirstActivity.IPCamera,34,0);
                    Toast.makeText(getActivity(),bt_set_initial.getText(),Toast.LENGTH_SHORT).show();
                    break;
                case R.id.bt_start_initial:
                    setCamera(FirstActivity.IPCamera,35,0);
                    Toast.makeText(getActivity(),bt_start_initial.getText(),Toast.LENGTH_SHORT).show();
                    break;
                case R.id.bt_left:
                    setCamera(FirstActivity.IPCamera,4,1);
                    Toast.makeText(getActivity(),bt_left.getText(),Toast.LENGTH_SHORT).show();
                    break;
                case R.id.bt_right:
                    setCamera(FirstActivity.IPCamera,6,1);
                    Toast.makeText(getActivity(),bt_right.getText(),Toast.LENGTH_SHORT).show();
                    break;
                case R.id.bt_up:
                    setCamera(FirstActivity.IPCamera,0,1);
                    Toast.makeText(getActivity(),bt_up.getText(),Toast.LENGTH_SHORT).show();
                    break;
                case R.id.bt_down:
                    setCamera(FirstActivity.IPCamera,2,1);
                    Toast.makeText(getActivity(),bt_down.getText(),Toast.LENGTH_SHORT).show();
                    break;
                case R.id.bt_set_left:
                    setCamera(FirstActivity.IPCamera,32,0);
                    Toast.makeText(getActivity(), bt_set_left.getText(),Toast.LENGTH_SHORT).show();
                    break;
                case R.id.bt_start_left:
                    setCamera(FirstActivity.IPCamera,33,0);
                    Toast.makeText(getActivity(),bt_start_left.getText(),Toast.LENGTH_SHORT).show();
                    break;
                case R.id.bt_set_right:
                    setCamera(FirstActivity.IPCamera,36,0);
                    Toast.makeText(getActivity(),bt_set_right.getText(),Toast.LENGTH_SHORT).show();
                    break;
                case R.id.bt_start_right:
                    setCamera(FirstActivity.IPCamera,37,0);
                    Toast.makeText(getActivity(),bt_start_right.getText(),Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
    // 设置初始，设置预设位
    public void setCamera(final String IPCamera, final int command, final int step){
        XcApplication.executorServicetor.execute(new Runnable() {
            @Override
            public void run() {
                cameraCommandUtil.postHttp(IPCamera, command, step);
            }
        });
    }
    private class onLongClickListener2 implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View view) {
            if (view.getId() == R.id.up_button) {
                sp_n = getAngle();
                Connect_Transport.line(sp_n);
            }
    /*如果将onLongClick返回false，那么执行完长按事件后，还有执行单击事件。
    如果返回true，只执行长按事件*/
            return true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    Rect[] rect = new Rect[10];




    //信号灯识别
    public static void trafficLightDis(Bitmap bip) {
//
        int width = bip.getWidth();
        int height = bip.getHeight();
        int[] pixels = new int[width * height];
        bip.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] pl = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                int pixel = pixels[offset + x];
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;
                int bright = (int) (0.3 * r + 0.59 * g + 0.11 * b);
                if (bright < 256/2)
                    pl[offset + x] = 0xff000000;
                else
                    pl[offset + x] = pixel;
            }
        }
        bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);//把颜色值重新赋给新建的图片 图片的宽高为以前图片的值
        bitmap.setPixels(pl, 0, width, 0, 0, width, height);
        LeftFragment.image_show.setImageBitmap(bitmap);
        // return bitmap;
    }
    private void iniColor(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixel = new int[width*height];
        bitmap.getPixels(pixel,0,width,0,0,width,height);
        for (int y = 0;y < height;y++){
            int offset = y*width;
            for (int x = 0;x < width;x++){
                int pixels = pixel[offset + x];

                int r = (pixels >> 16)&0xff;
                int g = (pixels >> 8)&0xff;
                int b = pixels&0xff;

                if(g < 220 && b < 220 && r > 240){
                    reds++;
                }
                if (r < 220 && b < 220 && g > 220){
                    greens++;
                }
                if (g >240 && r > 240 && b <220){
                    yellows++;
                }
            }

        }
        if (reds >greens && reds > yellows){
            Toast.makeText(getActivity(),"红灯",Toast.LENGTH_SHORT).show();
            Log.e("TAG", "红灯" );
//            FirstActivity.Connect_Transport.traffic_control(0x0E, 0x02, 0x01);
//            FirstActivity.Connect_Transport.traffic_control(0x0F, 0x02, 0x01);
            inColor = 1;
//            FirstActivity.Connect_Transport.shibie(1);

            reds = 0;
            greens = 0;
            yellows = 0;
        }
        if (yellows > reds && yellows > greens){
            Toast.makeText(getActivity(),"黄灯",Toast.LENGTH_SHORT).show();
            Log.e("TAG", "黄灯: ");
//            connectTransport.traffic(3);
//            FirstActivity.Connect_Transport.shibie(3);
//            FirstActivity.Connect_Transport.traffic_control(0x0E, 0x02, 0x03);
//            FirstActivity.Connect_Transport.traffic_control(0x0F, 0x02, 0x03);
            inColor = 3;
            reds = 0;
            greens = 0;
            yellows = 0;
        }
        if (greens > reds && greens > yellows){
            Toast.makeText(getActivity(),"绿灯",Toast.LENGTH_SHORT).show();
            Log.e("TAG", "绿灯: " );
//            connectTransport.traffic(2);
//            FirstActivity.Connect_Transport.shibie(2);
//            FirstActivity.Connect_Transport.traffic_control(0x0E, 0x02, 0x02);
//            FirstActivity.Connect_Transport.traffic_control(0x0F, 0x02, 0x02);
            inColor = 2;
            reds = 0;
            greens = 0;
            yellows = 0;
        }
    }


    //图像识别
    public void TuxiangShibie(){
        MainActivity.getshape(getContext(), bitmap);
//        Log.e(TAG, "shape:"+ MainActivity.sru.getShapeList());

        //cs

        String[] str = new String[]{"绿色","红色","黄色","橙色","青色","蓝色","紫色","黑色"};
        int[] arr2 = new int[8];
        for (int i = 0; i < MainActivity.sru.getShapeList().size(); i++) {
            String s  = String.valueOf(MainActivity.sru.getShapeList().get(i).shape);
            String cooler = String.valueOf(MainActivity.sru.getShapeList().get(i).name);
            for (int j = 0; j < 8; j++) {
                if(cooler.equals(str[j])){
                    arr2[j]++;
                }
            }
            Arrays.sort(arr2);
            cns2 = arr2[4];
            if(s == "长方形" || s=="正方形"){
                ju_num++;
            }else if (s == "圆形" && cooler.equals("绿色")){
                yuan_num++;
            }else if(s == "锐角三角形" || s == "钝角三角形" || s == "直角三角形"){
                sj_num++;
            }else if(s == "菱形" && cooler.equals("黄色")){
                lin_num++;
            }else if(s =="五角星"){
                wuj_num++;
            }
            if((ju_num+yuan_num+lin_num+wuj_num) > 8){
                tux = false;
            }
        }
//        FirstActivity.Connect_Transport.tuxiang(ju_num,yuan_num,wuj_num);
        //FirstActivity.Connect_Transport.tuxinang2(lin_num,wuj_num);
        //
        Mat mat3 = MainActivity.sru.cv2ImgAddRect();
        Bitmap bitmap3 = Bitmap.createBitmap(mat3.width(),mat3.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat3, bitmap3);
        dia(bitmap3,MainActivity.sb.toString());
//        dia.show();
    }
    private void dia(Bitmap bitmap,String text){
        dia = new Dialog(getContext(),R.style.edit_AlertDialog_style);
        dia.setContentView(R.layout.activity_start_dialog);
        ImageView imageView = (ImageView) dia.findViewById(R.id.start_img);
        TextView textView = (TextView) dia.findViewById(R.id.start_text);
        imageView.setImageBitmap(bitmap);
        textView.setText(text);
        //选择true的话点击其他地方可以使dialog消失，为false的话不会消失
        dia.setCanceledOnTouchOutside(true); // Sets whether this dialog is
        Window w = dia.getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.x = 0;
        lp.y = 40;
        dia.onWindowAttributesChanged(lp);
        imageView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dia.dismiss();
                    }
                });
    }

    private Timer timer;  // 识别次数限定
    private String result_qr;  // 识别结果统计
    private String result_qr1; //要的数据
    private String result_qr2; //要的数据
    private int qr_flag = 0; // 识别次数
    private List<String> listqr = new ArrayList<>();
    private Set<String> set = new HashSet<>();
    /**
     * 多二维码识别函数，输入带有多二维码的bitmap即可输出相应结果
     * @param bitmap1
     */
    private void Qr_recognition(final Bitmap bitmap1)
    {

        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Result[] result;
                        result_qr = "";
                        int width = bitmap1.getWidth();
                        int height = bitmap1.getHeight();
                        int[] pixels = new int[width * height];
                        bitmap1.getPixels(pixels, 0, width, 0, 0, width, height);
                        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
                        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
                        // 新建一个RGBLuminanceSource对象
                        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                        // 将图片转换成二进制图片
                        BinaryBitmap binaryBitmap = new BinaryBitmap(new
                                GlobalHistogramBinarizer(source));
                        QRCodeMultiReader reader = new QRCodeMultiReader();// 初始化解析对象
                        try {
                            result = reader.decodeMultiple(binaryBitmap,
                                    null);// 解析获取一个Result数组
                            if (result != null) {
                                for (Result kp : result) {
                                    if (set.add("/home/" + kp.toString())) {
                                        listqr.add("/home/" + kp.toString());
                                        qrHandler.sendEmptyMessage(20);
                                    }
                                    Log.e("二维码信息", kp.toString());
                                }
                                qrHandler.sendEmptyMessage(55);  //                     检测到二维码
                                timer.cancel();
                            } else {
                                qr_flag++;
                                qrHandler.sendEmptyMessage(15);  // 没检测到
                                if (qr_flag >= 3) {  // 识别次数设置
                                    timer.cancel();
                                    qrHandler.sendEmptyMessage(25);
                                }
                            }
                        } catch (NotFoundException e) {

                            e.printStackTrace();
                        }
                    }
                }, 100);
            }
        }).start();
    }
//    /*
//       多个二维码识别
//       */
//    public void QrShibie(Bitmap bMap){
//        new Thread(() -> {
////            Bitmap bMap = bitmap;
//            int[] data2 = new int[bMap.getWidth() * bMap.getHeight()];
//            bMap.getPixels(data2, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());
//            RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(bMap.getWidth(),bMap.getHeight(),data2);
//
//            LuminanceSource source = rgbLuminanceSource;
//            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
//            Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>    ();
//            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
//            MultiFormatReader mreader = new MultiFormatReader();
//            GenericMultipleBarcodeReader multireader = new GenericMultipleBarcodeReader(mreader);
//
//            try {
//                result2 = multireader.decodeMultiple(bitmap,hints);
//                Log.e("TAG","" +result2.length );
//
//                qrHandler.sendEmptyMessage(55);
//                System.out.println("正在识别");
//
//
//
//            } catch (NotFoundException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//                qrHandler.sendEmptyMessage(20);
//            }
//        }).start();
//
//    }



    // 二维码、车牌处理
    @SuppressLint("HandlerLeak")
    Handler qrHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    System.out.println("Handler已经接收");
                    break;
                case 15:
                    Log.e(TAG, "正在进行第"+qr_flag+"次识别");
                    break;
                case 20:
                    if(listqr.size() != 0){
                        String[] strings = new String[listqr.size()];
                        for (int i = 0; i < listqr.size(); i++) {
                            strings[i] = listqr.get(i);
                        }
                        longestSubstring = car.bkrc.com.car2022.bar.Text.sumResult.findLongestCommonSubstring(strings);
                        Log.e(TAG, "PLANT_handleMessage: " + longestSubstring);
//                        longestSubstring = longestCommonSubstring(strings);
                        if(listqr.size() == 4){

                            chushi();
                        }
                    }

                    result_qr1 = null;
                    result_qr = null;
                    break;
                case 25:
                    Log.e(TAG, "未能识别成功");
                    qr_flag = 0;
                    break;

                default:
                    break;
            }
        }
    };

    //绿色车牌识别
    public static void Plate_ShiBie()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PlantMainActivity.doRecognizePlate(replaceBitmapColor2(bitmap,0XFF3F3F3F,0XFF060086));

                }catch (Exception e)
                {

                    e.printStackTrace();
                }
//                Log.e(TAG, "onClick: plate" );
            }
        }).start();
    }
    //普通车牌识别
    public static void Plate_ShiBie2()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PlantMainActivity.doRecognizePlate(bitmap);
                }catch (Exception e)
                {

                    e.printStackTrace();
                }
//                Log.e(TAG, "onClick: plate" );
            }
        }).start();
    }

    //FUNCTION TO COMPUTE THE MAXIMUM PREDICTION AND ITS CONFIDENCE
    public Object[] argmax(float[] array){

        int best = -1;
        float best_confidence = 0.0f;
        for(int i = 0;i < array.length;i++){
            float value = array[i];
            if (value > best_confidence){
                best_confidence = value;
                best = i;
            }
        }
        return new Object[]{best,best_confidence};
    }

    public static Bitmap replaceBitmapColor2(Bitmap oldBitmap,int oldColor,int newColor)

    {

//相关说明可参考 http://xys289187120.blog.51cto.com/3361352/657590/

        Bitmap mBitmap = oldBitmap.copy(Bitmap.Config.ARGB_8888, true);

//循环获得bitmap所有像素点

        int mBitmapWidth = mBitmap.getWidth();

        int mBitmapHeight = mBitmap.getHeight();

        int mArrayColorLengh = mBitmapWidth * mBitmapHeight;

        int[] mArrayColor = new int[mArrayColorLengh];

        int count = 0;

        for (int i = 0; i < mBitmapHeight; i++) {

            for (int j = 0; j < mBitmapWidth; j++) {

//获得Bitmap 图片中每一个点的color颜色值

//将需要填充的颜色值如果不是

//在这说明一下 如果color 是全透明 或者全黑 返回值为 0

//getPixel()不带透明通道 getPixel32()才带透明部分 所以全透明是0x00000000

//而不透明黑色是0xFF000000 如果不计算透明部分就都是0了

                int color = mBitmap.getPixel(j, i);


//将颜色值存在一个数组中 方便后面修改

                if ( oldColor<=color) {

                    mBitmap.setPixel(j, i, newColor); //将白色替换成透明色

                }
                if(color<=0xff606060){
                    mBitmap.setPixel(j, i, Color.WHITE); //将白色替换成透明色
                }

            }

        }
        return mBitmap;
    }
    //yolov5返回的标记结果
    private void showObjects(YoloV5Ncnn.Obj[] objects) {
        if (objects == null)
        {
            return;
        }

        // draw objects on bitmap
        Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        final int[] colors = new int[] {
                Color.rgb( 54,  67, 244),
                Color.rgb( 99,  30, 233),
                Color.rgb(176,  39, 156),
                Color.rgb(183,  58, 103),
                Color.rgb(181,  81,  63),
                Color.rgb(243, 150,  33),
                Color.rgb(244, 169,   3),
                Color.rgb(212, 188,   0),
                Color.rgb(136, 150,   0),
                Color.rgb( 80, 175,  76),
                Color.rgb( 74, 195, 139),
                Color.rgb( 57, 220, 205),
                Color.rgb( 59, 235, 255),
                Color.rgb(  7, 193, 255),
                Color.rgb(  0, 152, 255),
                Color.rgb( 34,  87, 255),
                Color.rgb( 72,  85, 121),
                Color.rgb(158, 158, 158),
                Color.rgb(139, 125,  96)
        };

        Canvas canvas = new Canvas(rgba);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);

        Paint textbgpaint = new Paint();
        textbgpaint.setColor(Color.WHITE);
        textbgpaint.setStyle(Paint.Style.FILL);

        Paint textpaint = new Paint();
        textpaint.setColor(Color.BLACK);
        textpaint.setTextSize(26);
        textpaint.setTextAlign(Paint.Align.LEFT);

        for (int i = 0; i < objects.length; i++)
        {
            paint.setColor(colors[i % 19]);

            canvas.drawRect(objects[i].x, objects[i].y, objects[i].x + objects[i].w, objects[i].y + objects[i].h, paint);

            // draw filled text inside image
            {
                if(objects[i].label.equals("masked")){
                    arr++;
                }
                for (int j = 0; j < arr1.length; j++) {
                    if(objects[i].label.equals(stringList1[j])){
                        arr1[j]++;
                    }
                }
                for (int j = 0; j < arr2.length; j++) {
                    if(objects[i].label.equals(stringList2[j])){
                        arr2[j]++;
                        jt = false;
                    }
                }
                if(objects[i].label.equals("qr")){
                    rect[k] = new Rect((int)(objects[i].x), (int)(objects[i].y), (int)(objects[i].w), (int) (objects[i].h));
                    k++;
                }
                String text = objects[i].label + " = " + String.format("%.1f", objects[i].prob * 100) + "%";
                Log.e("TAG", text );
            }
        }
    }
    private static byte[] bytesend(byte[] sbyte) {
        byte[] textbyte = new byte[sbyte.length + 5];
        textbyte[0] = (byte) 0xFD;
        textbyte[1] = (byte) (((sbyte.length + 2) >> 8) & 0xff);
        textbyte[2] = (byte) ((sbyte.length + 2) & 0xff);
        textbyte[3] = 0x01;// 合成语音命令
        textbyte[4] = (byte) 0x01;// 编码格式
        for (int i = 0; i < sbyte.length; i++) {
            textbyte[i + 5] = sbyte[i];
        }
        return textbyte;
    }
    public static String longestCommonSubstring(String[] strings) {
        if (strings == null || strings.length == 0) {
            return "";
        }

        int minLength = Integer.MAX_VALUE;
        int maxLength = 0;
        String shortestString = "";

        // 找到最短的字符串
        for (String s : strings) {
            if (s.length() < minLength) {
                minLength = s.length();
                shortestString = s;
            }
        }

        // 从最短字符串的长度开始递减，依次判断每个子串是否是所有字符串的公共子串
        for (int length = minLength; length > 0; length--) {
            for (int start = 0; start <= minLength - length; start++) {
                String substr = shortestString.substring(start, start + length);
                boolean isCommonSubstring = true;

                // 判断该子串是否是所有字符串的公共子串
                for (String s : strings) {
                    if (!s.contains(substr)) {
                        isCommonSubstring = false;
                        break;
                    }
                }

                // 如果是公共子串，则返回该子串
                if (isCommonSubstring) {
                    return substr;
                }
            }
        }

        return "";
    }
}


