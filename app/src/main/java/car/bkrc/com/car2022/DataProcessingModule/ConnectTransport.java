package car.bkrc.com.car2022.DataProcessingModule;

import static car.bkrc.com.car2022.FragmentView.RightFragment1.TAG;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import car.bkrc.com.car2022.ActivityView.FirstActivity;
import car.bkrc.com.car2022.MessageBean.DataRefreshBean;
import car.bkrc.com.car2022.Utils.CameraUtile.XcApplication;
import car.bkrc.com.car2022.Utils.OtherUtil.SerialPort;
import car.bkrc.com.car2022.ViewAdapter.InfrareAdapter;

/**
 * Socket数据处理类
 */
public class ConnectTransport {
    public static DataInputStream bInputStream = null;
    public static DataOutputStream bOutputStream = null;
    public static Socket socket = null;
    public byte[] rbyte = new byte[50];
    private Handler reHandler;
    public short TYPE = 0xAA;
    public short TYPE2 = 0xBB;
    public short TYPE3 = 0X00;
    public short MAJOR = 0x00;
    public short FIRST = 0x00;
    public short SECOND = 0x00;
    public short THRID = 0x00;
    public short CHECKSUM = 0x00;
    public short determine = 0x00;

    private static OutputStream SerialOutputStream;
    private InputStream SerialInputStream;
    private boolean Firstdestroy = false;  ////Firstactivity 是否已销毁了

    public void destory() {             //用于关闭网络连接
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                bInputStream.close();
                bOutputStream.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void connect(Handler reHandler, String IP) {     //建立网络连接
        try {
            this.reHandler = reHandler;
            Firstdestroy = false;
            int port = 60000;
            socket = new Socket(IP, port);
            bInputStream = new DataInputStream(socket.getInputStream());
            bOutputStream = new DataOutputStream(socket.getOutputStream());
            if (!inputDataState) {
                reThread();
            }
            EventBus.getDefault().post(new DataRefreshBean(3));
        } catch (SocketException ignored) {
            EventBus.getDefault().post(new DataRefreshBean(4));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void serial_connect(Handler reHandler) {       //用于串口连接
        this.reHandler = reHandler;
        try {
            int baudrate = 115200;
            String path = "/dev/ttyS4";
            SerialPort mSerialPort = new SerialPort(new File(path), baudrate, 0);
            SerialOutputStream = mSerialPort.getOutputStream();
            SerialInputStream = mSerialPort.getInputStream();
            //new Thread(new SerialRunnable()).start();
            //reThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        XcApplication.executorServicetor.execute(new SerialRunnable());
        //new Thread(new serialRunnable()).start();
    }

    byte[] serialreadbyte = new byte[50];

    class SerialRunnable implements Runnable {     //用于在单独的线程中监听串口数据并将其发送到指定的处理程序。
        @Override
        public void run() {
            while (SerialInputStream != null) {
                try {
                    int num = SerialInputStream.read(serialreadbyte);
                    // String  readserialstr =new String(serialreadbyte);
                    String readserialstr = new String(serialreadbyte, 0, num, "utf-8");
                    Log.e("----serialreadbyte----", "******" + readserialstr);
                    Message msg = new Message();
                    Log.e(TAG, "run: "+"444" );
                    msg.what = 1;
                    msg.obj = serialreadbyte;
                    reHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean inputDataState = false;

    private void reThread() {   //在单独的线程中监听网络输入流，并根据接收到的数据发送消息给指定的处理程序。
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto1-generated method stub
                while (socket != null && !socket.isClosed()) {
                    if (Firstdestroy == true)  //Firstactivity 已销毁了
                    {
                        break;
                    }
                    try {
                        inputDataState = true;
                        bInputStream.read(rbyte);
                        Message msg = new Message();
                        for(int i = 0; i<rbyte.length;i++){
                            System.out.print(rbyte[i]);
                        }
                        if(rbyte[0] == 0x54 && rbyte[1] == 0x0A){
                            Log.e(TAG, "run: "+"333" );
                            msg.what = 11;
                            msg.obj = rbyte;
                            reHandler.sendMessage(msg);
                        }
                        else{

                            msg.what = 1;
                            msg.obj = rbyte;
                            reHandler.sendMessage(msg);
                        }

                    } catch (SocketException ignored) {
                        EventBus.getDefault().post(new DataRefreshBean(4));
                        destory();
                        inputDataState = false;
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        EventBus.getDefault().post(new DataRefreshBean(4));
                        destory();
                        inputDataState = false;
                    } catch (UnsupportedOperationException ignored) {
                        inputDataState = false;
                    }
                }
            }
        }).start();

    }

    public void send() {
        CHECKSUM = (short) ((MAJOR + FIRST + SECOND + THRID) % 256);
        // 发送数据字节数组
        final byte[] sbyte = {0x55, (byte) TYPE, (byte) MAJOR, (byte) FIRST, (byte) SECOND, (byte) THRID, (byte) CHECKSUM, (byte) 0xBB};


        Log.e(TAG, "send: " + sbyte);
        if (XcApplication.isserial == XcApplication.Mode.SOCKET) {
            XcApplication.executorServicetor.execute(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        if (socket != null && !socket.isClosed()) {
//                            bOutputStream.write(sbyte, 0, sbyte.length);
                            bOutputStream.write(sbyte, 0, sbyte.length);
                            bOutputStream.flush();
                        } else {
                            Message msg = new Message();
                            msg.what = 2;
                            reHandler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (XcApplication.isserial == XcApplication.Mode.SERIAL) {

            XcApplication.executorServicetor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        SerialOutputStream.write(sbyte, 0, sbyte.length);
                        SerialOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (XcApplication.isserial == XcApplication.Mode.USB_SERIAL)
            try {
                FirstActivity.sPort.write(sbyte, 5000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException ignored) {

            }
    }

    /**
     * 计算校验和
     *
     * @param Count_data    需要计算的数组
     * @return              返回校验和
     */
    private int ChecksumCalculations(int Count_data[]){

        return ((Count_data[2] + Count_data[3] + Count_data[4] + Count_data[5])%256);
    }

    /**
     *  发送wifi数据(八位)
     *
     * @param Tx_data   发送数据
     */
    private void Send_Data(int Tx_data[]){

        Tx_data[6] = ChecksumCalculations(Tx_data);
        byte Tx_Buff[] = {(byte)Tx_data[0], (byte)Tx_data[1], (byte)Tx_data[2], (byte)Tx_data[3], (byte)Tx_data[4], (byte)Tx_data[5], (byte)Tx_data[6], (byte)Tx_data[7]};

        Log.e(TAG, "SendData: " + Tx_Buff);
        if (XcApplication.isserial == XcApplication.Mode.SOCKET) {
            XcApplication.executorServicetor.execute(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        if (socket != null && !socket.isClosed()) {
                            bOutputStream.write(Tx_Buff, 0, Tx_Buff.length);
                            bOutputStream.flush();
                        } else {
                            Message msg = new Message();
                            msg.what = 2;
                            reHandler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (XcApplication.isserial == XcApplication.Mode.SERIAL) {

            XcApplication.executorServicetor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        SerialOutputStream.write(Tx_Buff, 0, Tx_Buff.length);
                        SerialOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        else if (XcApplication.isserial == XcApplication.Mode.USB_SERIAL)
            try {
                FirstActivity.sPort.write(Tx_Buff, 5000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException ignored) {

        }
    }

    public void send_voice(final byte[] textbyte) {
        if (XcApplication.isserial == XcApplication.Mode.SOCKET) {
            XcApplication.executorServicetor.execute(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        if (socket != null && !socket.isClosed()) {
                            bOutputStream.write(textbyte, 0, textbyte.length);
                            bOutputStream.flush();
                        } else {
                            Message msg = new Message();
                            msg.what = 2;
                            reHandler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (XcApplication.isserial == XcApplication.Mode.SERIAL) {

            XcApplication.executorServicetor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        SerialOutputStream.write(textbyte, 0, textbyte.length);
                        SerialOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (XcApplication.isserial == XcApplication.Mode.USB_SERIAL)
            try {
                FirstActivity.sPort.write(textbyte, 5000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException ignored) {
                Log.e("UART:", "unline");
            }
    }

    public void Rotate_SendData_Infrared(byte[] Tx_data, byte CMD) {
        byte[] Tx_buf = new byte[8];
        byte Tx_len = 0;
        int len = Tx_data.length;
        if (len == 0) {
            return;
        }

        if (len == 1) {
            Tx_len = 1;
        } else {
            Tx_len = (byte) ((len + 1) / 2);
        }
        byte temp = (byte) TYPE;
        TYPE = (short) 0xAA;
        MAJOR = (byte) CMD;

        SECOND = 0x00;
        THRID = 0x00;
        send();
        for (byte Tx_i = 0, i = 0; Tx_i < Tx_len; Tx_i++) {
            FIRST = (i < len) ? Tx_data[i++] : (byte) 0xBB;
            SECOND = (i < len) ? Tx_data[i++] : (byte) 0xBB;
            THRID = Tx_i;
            send();
        }
        TYPE = temp;

    }

    /**
     * 小车前进
     *
     * @param sp_n  速度
     * @param en_n  码盘
     */
    public void go(int sp_n, int en_n) {

        int Tx_data[] = {0x55, 0xAA, 0x02, (sp_n & 0xFF), (en_n & 0xff), (en_n >> 8), 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * 小车后退
     *
     * @param sp_n  速度
     * @param en_n  码盘
     */
    public void back(int sp_n, int en_n) {

        int Tx_data[] = {0x55, 0xAA, 0x03, (sp_n & 0xFF), (en_n & 0xff), (en_n >> 8), 0x00, 0xBB};

        Send_Data(Tx_data);
    }


    /**
     * 小车左转
     *
     * @param sp_n  速度
     */
    public void left(int sp_n) {

        int Tx_data[] = {0x55, 0xAA, 0x04, (sp_n & 0xFF), 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * 小车右转
     *
     * @param sp_n  速度
     */
    public void right(int sp_n) {

        int Tx_data[] = {0x55, 0xAA, 0x05, (sp_n & 0xFF), 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * 小车停止
     */
    public void stop() {

        int Tx_data[] = {0x55, 0xAA, 0x01, 0x00, 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * 程序自动执行
     */
    public void autoDrive() {

        // 启动函数 Send_Data自动计算校验和
        int[] Tx_data = {0x55, 0xAA, 0xA0, 0x00, 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * 主车循迹
     *
     * @param sp_n 速度
     */
    public void line(int sp_n) {  //寻迹

        int Tx_data[] = {0x55, 0xAA, 0x06, (sp_n & 0xFF), 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * 主车码盘清空
     */
    public void clear() {

        int Tx_data[] = {0x55, 0xAA, 0x07, 0x00, 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * TFT车牌向下翻页
     *
     * @param Device    TFT型号
     */
    public void TFT_LicensePlate_PageDown(char Device)
    {
        int Model = 0x00;

        if(Device == 'A') Model = 0X0B;
        else if(Device == 'B') Model = 0X08;
        else if(Device == 'C') Model = 0X12;

        int Tx_data[] = {0x55, Model, 0x01, 0x02, 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /** 接收到主车消息 发送成功回传
     *
     * @param CMD 回传命令
     */
    public void receiveReturn(int CMD){

        int[] Tx_data = {0x55, 0xBC, CMD, 0x00, 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * 执行完主车的任务发送任务结束回传
     *
     * @param CMD 回传命令
     */
    public void receiveEnd(int CMD){

        int[] Tx_data = {0x55, 0xBD, CMD, 0x00, 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    //主从车数据接收状态转换
    public void stateChange(final int state) {
        final short temp = TYPE;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (socket != null && socket.isConnected()) {
                    if (state == 1) {//从车状态
                        int Tx_Data_1[] = {0x55, 0x02, 0x80, 0x01, 0x00, 0x00, 0x00, 0xBB};

                        Send_Data(Tx_Data_1);
                        yanchi(500);

                        int Tx_Data_2[] = {0x55, 0xAA, 0x80, 0x01, 0x00, 0x00, 0x00, 0xBB};

                        Send_Data(Tx_Data_2);
                    } else if (state == 2) {// 主车状态

                        int Tx_Data_1[] = {0x55, 0x02, 0x80, 0x00, 0x00, 0x00, 0x00, 0xBB};

                        Send_Data(Tx_Data_1);
                        yanchi(500);

                        int Tx_Data_2[] = {0x55, 0xAA, 0x80, 0x00, 0x00, 0x00, 0x00, 0xBB};

                        Send_Data(Tx_Data_2);
                    }
                } else {
                    Message msg = new Message();
                    msg.what = 2;
                    reHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    /**
     * 控制主车发送红外数据
     *
     * @param Tx_data  数据(6位)
     */
    public void infrared_Send(final int Tx_data[]) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int Tx_buff_1[] = {0x55, 0xAA, 0x10, Tx_data[0], Tx_data[1], Tx_data[2], 0x00, 0xBB};

                Send_Data(Tx_buff_1);
                yanchi(200);

                int Tx_buff_2[] = {0x55, 0xAA, 0x11, Tx_data[3], Tx_data[4], Tx_data[5], 0x00, 0xBB};
                Send_Data(Tx_buff_2);
                yanchi(200);

                int Tx_buff_3[] = {0x55, 0xAA, 0x12, 0x00, 0x00, 0x00, 0x00, 0xBB};
                Send_Data(Tx_buff_3);
                yanchi(200);
            }
        }).start();
    }

    /**
     * 控制主车发送红外数据
     *
     * @param data  数据(6位)
     */
    public void Rotate_SendData_Infrared(final short[] data) {

        int Tx_data[] = {0xff, data[0], data[1], data[2], data[3], 0x00};

        infrared_Send(Tx_data);
    }

    /**
     *
     * @param bytes     立体显示物发送文本信息
     * @param len       长度
     */
    public void Rotate_SendData_Zigbee(final byte[] bytes,int len){

        int Tx_data[] = {0x55, 0x11, 0x31, 0x00, 0x00, 0x00, 0x00, 0xBB};

        for(int j = 0; j < len;j+=2){
            if(j+2 >= len){
                Tx_data[3] = bytes[j];
                if(j+1 == len){
                    Tx_data[4] = 0X00;
                }else{
                    Tx_data[4] = bytes[j+1];
                }
                Tx_data[5] = 0x55;
                Send_Data(Tx_data);
                yanchi(50);
            }else {
                Tx_data[3] = bytes[j];
                Tx_data[4] = bytes[j+1];
                Tx_data[5] = 0x00;
                Send_Data(Tx_data);
                yanchi(50);
            }
        }
    }
    /**
     * 立体显示物发送Zigbee命令
     *
     * @param data
     */
    public void Rotate_Send_Zigbee(final short[] data) {

        int Tx_data[] = {0x55, 0x11, data[0], data[1], data[2], data[3], 0x00, 0xBB};

        Send_Data(Tx_data);
        yanchi(600);
    }

    /**
     * 烽火台获取随机坐标
     */
    public void Alarm_GetCoordinates() {

        int Tx_data[] = {0x55, 0x07, 0x09, 0x00, 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * 主车指示灯控制
     *
     * @param left      左灯亮灭开关 1 开  0 关
     * @param right     右灯亮灭开关 1 开  0 关
     */
    public void light(int left, int right) {

        int Tx_data[] = {0x55, 0xAA, 0x20, 0x00, 0x00, 0x00, 0x00, 0xBB};

        if (left == 1 && right == 1) {
            Tx_data[3] = 0x01;
            Tx_data[4] = 0x01;
        } else if (left == 1 && right == 0) {
            Tx_data[3] = 0x01;
            Tx_data[4] = 0x00;
        } else if (left == 0 && right == 1) {
            Tx_data[3] = 0x00;
            Tx_data[4] = 0x01;
            send();
        } else {
            Tx_data[3] = 0x00;
            Tx_data[4] = 0x00;
        }
        Send_Data(Tx_data);
    }

    /**
     * 蜂鸣器开关
     *
     * @param button    开关 1 开启 0 关闭
     */
    public void buzzer(int button) {

        int Switch = 0;

        if (button == 1)
            Switch = 0x01;
        else if (button == 0)
            Switch = 0x00;

        int Tx_data[] = {0x55, 0xAA, 0x30, Switch, 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * 从车二维码识别
     */
    public void qr_rec(int state) {

        int Tx_data[] = {0x55, 0x02, 0x92, state, 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * 从车摄像头俯仰角控制
     *
     * @param state 角度
     */
    public void rb_cameraControl(int state) {

        int Tx_data[] = {0x55, 0x02, 0x91, 0x03, state, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * 光照档位控制
     *
     * @param gear 档位信息
     */
    public void gear(int gear) {

        int Gears = 0;

        if (gear == 1)
            Gears = 0x61;
        else if (gear == 2)
            Gears = 0x62;
        else if (gear == 3)
            Gears = 0x63;

        int Tx_data[] = {0x55, 0xAA, Gears, 0x00, 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    private static boolean sendState = false; // 数据发送状态记录，为true时正在发送，为false时发送结束/关闭

    //立体显示
    public boolean infrared_stereo(final short[] data,boolean tip) {
        if (socket.isConnected()){
            if (!sendState && data != null) {
                sendState = true; // 处于发送数据状态，开启发送拦截
                XcApplication.executorServicetor.execute(() -> {
                    int Tx_data_1[] = {0x55, 0xAA, 0x10, 0xff, data[0], data[1], 0x00, 0xBB};
                    Send_Data(Tx_data_1);
                    yanchi(500);

                    int Tx_data_2[] = {0x55, 0xAA, 0x11, data[2], data[3], data[4], 0x00, 0xBB};
                    Send_Data(Tx_data_2);
                    yanchi(500);

                    int Tx_data_3[] = {0x55, 0xAA, 0x12, 0x00, 0x00, 0x00, 0x00, 0xBB};
                    Send_Data(Tx_data_3);
                    yanchi(500);

                    if (tip)InfrareAdapter.handler.sendEmptyMessage(40); // 数据发送完毕
                    sendState = false;
                });
            } else {
                if (tip)InfrareAdapter.handler.sendEmptyMessage(30); // 数据还未发送完毕
            }
        }else {
            Message msg = new Message();
            msg.what = 2;
            reHandler.sendMessage(msg);
        }
        return sendState;
    }


    //智能交通灯
    public void traffic_control(int type, int major, int first) {

        int Tx_data[] = {0x55, type, major, first, 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * 舵机角度控制
     *
     * @param major 左侧舵机
     * @param first 右侧舵机
     */
    public void rudder_control(int major, int first) {

        int Tx_data[] = {0x55, 0x0C, 0x08, major, first, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * 立体车库 控制
     *
     * @param type      型号
     * @param major     主命令
     * @param first     副命令
     */
    public void garage_control(int type, int major, int first) {

        int Tx_data[] = {0x55, type, major, first, 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * 闸门控制
     *
     * @param major
     * @param first
     * @param second
     * @param third
     */
    public void gate(int major, int first, int second, int third) {

        int Tx_data[] = {0x55, 0x03, major, first, second, third, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * LCD 显示标志物进入计时关闭
     */
    public void digital_close() {

        int Tx_data[] = {0x55, 0x04, 0x03, 0x00, 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * LCD 显示标志物进入计时打开
     */
    public void digital_open() {

        int Tx_data[] = {0x55, 0x04, 0x03, 0x01, 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * LCD 显示标志物进入计时清空
     */
    public void digital_clear() {//数码管清零

        int Tx_data[] = {0x55, 0x04, 0x03, 0x02, 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * LCD显示标志物第二排显示距离
     *
     * @param dis   距离
     */
    public void digital_dic(int dis) {

        int Tx_data[] = {0x55, 0x04, 0x04, 0x00, (dis/100%10), ((dis/10%10)*16+(dis%10)), 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * 数码管显示指定数据
     *
     * @param rank  第几排 1-2
     * @param one   数据1
     * @param two   数据2
     * @param three 数据3
     */
    public void digital(int rank, int one, int two, int three) {

        int Tx_data[] = {0x55, 0x04, 0x00, one, two, three, 0x00, 0xBB};

        if (rank == 1) Tx_data[2] = 0x01;
        else Tx_data[2] = 0x02;

        Send_Data(Tx_data);
    }

    /**
     * 语音播报随机指令
     */
    public void VoiceBroadcast(){
        int Tx_data[] = {0x55, 0x06, 0x20, 0x01, 0x00, 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * 设置天气和温度
     *
     * @param weather   设置数组 下标0 是天气    下标1 是温度
     */
    public void voiceWeather(int[] weather) {

        int Tx_data[] = {0x55, 0x06, 0x42, weather[0], weather[1], 0x00, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * TFT 命令控制
     *
     * @param type      TFT 型号 0x0B(A)  0x08(B) 0x12(C)
     * @param MAIN      主命令
     * @param KIND      副命令1
     * @param COMMAD    副命令2
     * @param DEPUTY    副命令3
     *
     * 命令具体参考手册
     */
    public void TFT_LCD(int type, int MAIN, int KIND, int COMMAD, int DEPUTY) {

        int Tx_data[] = {0x55, type, MAIN, KIND, COMMAD, DEPUTY, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    /**
     * 无线充电控制
     *
     * @param MAIN
     * @param KIND
     * @param COMMAD
     * @param DEPUTY
     */
    public void magnetic_suspension(int MAIN, int KIND, int COMMAD, int DEPUTY) //无线充电
    {
        int Tx_data[] = {0x55, 0x0A, MAIN, KIND, COMMAD, DEPUTY, 0x00, 0xBB};

        Send_Data(Tx_data);
    }

    // 沉睡
    public void yanchi(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
