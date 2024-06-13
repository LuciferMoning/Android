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
//    private void DataLoad(byte[] data) {
//        //假定有个全局数据 SenData[]数组
//
//        SenData[0]=(byte) 0x55;
//        SenData[1]=(byte) 0xAA;
//        SenData[3]=(byte);
//        SenData[4]=(byte);
//        SenData[5]=(byte);
//        SenData[6]=(byte);
//        SenData[7]=(byte);
//        for()                           //填入数据项
//
//        SenData[data.length+8]=(byte);              //校验值计算
//        SenData[data.length+9]=(byte)0xAA;
//        SenData[data.length+10]=(byte)0x55;
//        SenData[data.length+11]=(byte)(data.length+11);   //计算数据长度
//
//    }

    private void sendSecend() {
        CHECKSUM = (short) ((MAJOR + FIRST + SECOND + THRID) % 256);

        // 发送数据字节数组

        final byte[] sbyte = {0x55, (byte) TYPE2, (byte) MAJOR, (byte) FIRST, (byte) SECOND, (byte) THRID, (byte) CHECKSUM, (byte) 0xBB};

        //

        if (XcApplication.isserial == XcApplication.Mode.SOCKET) {
            XcApplication.executorServicetor.execute(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        if (socket != null && !socket.isClosed()) {
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
            }
    }
    private void sendzhu() {

        short check = (short)((TYPE + MAJOR + FIRST + SECOND ) % 256);

        final byte[] sbyte = {0x54,(byte) TYPE3,(byte) determine, (byte) TYPE, (byte) MAJOR, (byte) FIRST, (byte) SECOND, (byte) THRID, (byte) CHECKSUM, 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,(byte)check,(byte) 0xBB};

        Log.e(TAG, "send: " + sbyte);
        if (XcApplication.isserial == XcApplication.Mode.SOCKET) {
            XcApplication.executorServicetor.execute(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        if (socket != null && !socket.isClosed()) {
                            bOutputStream.write(sbyte, 0, sbyte.length);
                            bOutputStream.flush();
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

    public void sendData(byte[] Tx_data, byte CMD) {
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

    public void Send_over(){
        byte temp = (byte) TYPE;
        TYPE = 0x0F;
        MAJOR = 0x10;
        SECOND = 0x00;
        THRID = 0x00;
        send();
        TYPE = temp;
    }


    // 前进
    public void go(int sp_n, int en_n) {
        MAJOR = 0x02;
        FIRST = (byte) (sp_n & 0xFF);
        SECOND = (byte) (en_n & 0xff);
        THRID = (byte) (en_n >> 8);
        send();
    }

    // 后退
    public void back(int sp_n, int en_n) {
        MAJOR = 0x03;
        FIRST = (byte) (sp_n & 0xFF);
        SECOND = (byte) (en_n & 0xff);
        THRID = (byte) (en_n >> 8);
        send();
    }


    //左转
    public void left(int sp_n) {
        MAJOR = 0x04;
        FIRST = (byte) (sp_n & 0xFF);
        SECOND = (byte) 0x00;
        THRID = (byte) 0x00;
        send();
    }


    // 右转
    public void right(int sp_n) {
        MAJOR = 0x05;
        FIRST = (byte) (sp_n & 0xFF);
        SECOND = (byte) 0x00;
        THRID = (byte) 0x00;
        send();
    }

    // 停车
    public void stop() {
        MAJOR = 0x01;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        send();
    }

    // 程序自动执行
    public void autoDrive() {
        MAJOR = 0xA0;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        send();
    }


    // 循迹
    public void line(int sp_n) {  //寻迹
        MAJOR = 0x06;
        FIRST = (byte) (sp_n & 0xFF);
        SECOND = 0x00;
        THRID = 0x00;
        send();
    }

    //清除码盘值
    public void clear() {
        MAJOR = 0x07;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        send();
    }

    //TFT车牌翻页
    public void fanyeshibie(char Device)
    {
            //         TFT_PageDown[8]={0x55,0x0B,0x10,0x02,0x00,0x00,0x12,0xBB};		// TFT显示器 下翻页
            //        MAJOR = 0xA0;//A2

            ////        FIRST = 0;
        if(Device == 'A') TYPE = 0X0B;
        else if(Device == 'B') TYPE = 0X08;
        else if(Device == 'C') TYPE = 0X12;
        System.out.println("OK");
        MAJOR = 0x01;
        FIRST = 0x02;
        SECOND = 0x00;
        THRID = 0x00;
        CHECKSUM = 0x00;
        send();
    }


    //返回信息表示接收到信息了
    public void receive( int o){
        TYPE3 = 0X00;
        determine = 0x00;
        TYPE = 0x00;
        MAJOR = 0x00;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        CHECKSUM = 0x00;

        TYPE3 = (short) 0x0C;

        determine =  (short) o;

        sendzhu();
    }
    //结束
    //返回信息表示接收到信息了
    public void receive2( ){
        TYPE3 = 0X00;
        determine = 0x00;
        TYPE = 0x00;
        MAJOR = 0x00;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        CHECKSUM = 0x00;

        TYPE3 = (short) 0x0D;

        determine =  (short) 0x02;

        sendzhu();
    }

    //红绿灯识别
    public void traffic(  int one){
        TYPE3 = 0X00;
        determine = 0x00;
        TYPE = 0x00;
        MAJOR = 0x00;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        CHECKSUM = 0x00;

        TYPE3 = 0X0B;
        determine = 0x03;
        TYPE = (short) one;


        sendzhu();
    }
    //二维码识别
    public void qr(  int one,  int two,  int thrid,  int four,  int five,
                    int six){
        TYPE3 = 0X00;
        determine = 0x00;
        TYPE = 0x00;
        MAJOR = 0x00;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        CHECKSUM = 0x00;

        TYPE3 = 0X0B;
        determine = 0x02;
        TYPE = (short) one;
        MAJOR = (short) two;
        FIRST = (short) thrid;
        SECOND = (short) four;
        THRID = (short) five;
        CHECKSUM = (short) six;

         sendzhu();
    }

    //车牌识别
    public void plan(  byte one,  byte two,  byte thrid,  byte four,  byte five,
                     byte six){
        TYPE3 = 0X00;
        determine = 0x00;
        TYPE = 0x00;
        MAJOR = 0x00;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        CHECKSUM = 0x00;

        TYPE3 = 0X0B;
        determine = 0x04;
        TYPE =  one;
        MAJOR =  two;
        FIRST =  thrid;
        SECOND =  four;
        THRID =  five;
        CHECKSUM =  six;

        sendzhu();
    }

    //图片形状数量传输
    public void tuxiang(int ju_num,int yuan_num,int sj_num) {
        MAJOR = 0xC0;//A6
        SECOND = (byte) ju_num;
        SECOND = (byte) yuan_num;
        THRID = (byte) sj_num;
        send();
        yanchi(500);
//        yanchi(1000);
//        MAJOR = 0xC1;//A7
//        FIRST = (byte) lin_num;
//        SECOND = (byte) wuj_num;
//        THRID = 0x00;
//        send();
//        yanchi(500);
    }
    //红绿灯
    public void shibie(int yanse) {
        MAJOR = 0xB0;
        FIRST = (byte) (yanse);
        send();
    }
    //主从车数据接收状态转换
    public void stateChange(final int i) {
        final short temp = TYPE;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (socket != null && socket.isConnected()) {
                    if (i == 1) {//从车状态
                        TYPE = 0x02;
                        MAJOR = 0x80;
                        FIRST = 0x01;
                        SECOND = 0x00;
                        THRID = 0x00;
                        send();
                        yanchi(500);
                        TYPE = (byte) 0xAA;
                        MAJOR = 0x80;
                        FIRST = 0x01;
                        SECOND = 0x00;
                        THRID = 0x00;
                        send();
                        TYPE = 0x02;
                    } else if (i == 2) {// 主车状态
                        TYPE = 0x02;
                        MAJOR = 0x80;
                        FIRST = 0x00;
                        SECOND = 0x00;
                        THRID = 0x00;
                        send();
                        yanchi(500);
                        TYPE = (byte) 0xAA;
                        MAJOR = 0x80;
                        FIRST = 0x00;
                        SECOND = 0x00;
                        THRID = 0x00;
                        send();
                        TYPE = 0xAA;
                    }
                    TYPE = temp;
                } else {
                    Message msg = new Message();
                    msg.what = 2;
                    reHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    // 红外
    public void infrared(final byte one, final byte two, final byte thrid, final byte four, final byte five,
                         final byte six) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MAJOR = 0x10;
                FIRST = one;
                SECOND = two;
                THRID = thrid;
                send();
                yanchi(500);
                MAJOR = 0x11;
                FIRST = four;
                SECOND = five;
                THRID = six;
                send();
                yanchi(500);
                MAJOR = 0x12;
                FIRST = 0x00;
                SECOND = 0x00;
                THRID = 0x00;
                send();
                yanchi(1000);
            }
        }).start();
    }

    public void sendData(final byte[] bytes,int i){
        for(int j = 0; j < i;j+=2){
            if(j+2 == i){
                MAJOR = 0x10;
                FIRST = 0xff;
                SECOND = 0x31;
                THRID = bytes[j];
                send();
                yanchi(500);
                MAJOR = 0x11;
                FIRST = bytes[j+1];
                SECOND = 0x55;
                THRID = 0x00;
                send();
                yanchi(500);
                MAJOR = 0x12;
                FIRST = 0x00;
                SECOND = 0x00;
                THRID = 0x00;
                send();
                yanchi(500);
            }else {
                MAJOR = 0x10;
                FIRST = 0xff;
                SECOND = 0x31;
                THRID = bytes[j];
                send();
                yanchi(500);
                MAJOR = 0x11;
                FIRST = bytes[j + 1];
                SECOND = 0x00;
                THRID = 0x00;
                send();
                yanchi(500);
                MAJOR = 0x12;
                FIRST = 0x00;
                SECOND = 0x00;
                THRID = 0x00;
                send();
                yanchi(500);
            }
        }


    }
    /**
     * 发送文本信息专用
     *
     * @param data
     */
    public void sendData(final short[] data) {
        MAJOR = 0x10;
        FIRST = 0xff;
        SECOND = data[0];
        THRID = data[1];
        send();
        yanchi(200);//延时 如果觉得发过去单片机接收不全 就调高一点
        MAJOR = 0x11;
        FIRST = data[2];
        SECOND = data[3];
        THRID = 0x00;
        send();
        yanchi(200);//延时 如果觉得发过去单片机接收不全 就调高一点
        MAJOR = 0x12;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        send();
        yanchi(200);//延时 如果觉得发过去单片机接收不全 就调高一点
    }

    /**
     * ZigBee发送文本信息专用
     *
     * @param bytes
     */
    public void zigbeeSendData(final byte[] bytes,int i){
        for(int j = 0; j < i;j+=2){
            if(j+2 >= i){
                TYPE = 0x11;
                MAJOR = 0x31;
                FIRST = bytes[j];
                if(j+1 == i){
                    SECOND = 0X00;
                }else{
                    SECOND = bytes[j+1];
                }
                THRID = 0x55;
                send();
                yanchi(500);
            }else {
                TYPE = 0x11;
                MAJOR = 0x31;
                FIRST = bytes[j];
                SECOND = bytes[j+1];
                THRID = 0x00;
                send();
                yanchi(500);
            }
        }


    }
    /**
     * ZigBee发送文本信息专用
     *
     * @param data
     */
    public void zigbeeSendData(final short[] data) {
        TYPE = 0x11;
        MAJOR = data[0];
        FIRST = data[1];
        SECOND = data[2];
        THRID = data[3];
        send();
        TYPE = 0xAA;
        yanchi(600);
    }

    // 程序自动执行
    public void getID() {
        TYPE = 0x07;
        MAJOR = 0x09;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        send();
        TYPE = 0xAA;
    }


    // 双色led灯
    public void lamp(byte command) {
        MAJOR = 0x40;
        FIRST = command;
        SECOND = 0x00;
        THRID = 0x00;
        send();
    }

    // 指示灯
    public void light(int left, int right) {
        if (left == 1 && right == 1) {
            MAJOR = 0x20;
            FIRST = 0x01;
            SECOND = 0x01;
            THRID = 0x00;
            send();
        } else if (left == 1 && right == 0) {
            MAJOR = 0x20;
            FIRST = 0x01;
            SECOND = 0x00;
            THRID = 0x00;
            send();
        } else if (left == 0 && right == 1) {
            MAJOR = 0x20;
            FIRST = 0x00;
            SECOND = 0x01;
            THRID = 0x00;
            send();
        } else if (left == 0 && right == 0) {
            MAJOR = 0x20;
            FIRST = 0x00;
            SECOND = 0x00;
            THRID = 0x00;
            send();
        }
    }


    // 蜂鸣器
    public void buzzer(int i) {
        if (i == 1)
            FIRST = 0x01;
        else if (i == 0)
            FIRST = 0x00;
        MAJOR = 0x30;
        SECOND = 0x00;
        THRID = 0x00;
        send();
    }

    /**
     * 从车二维码识别
     */
    public void qr_rec(int state) {
        byte temp = (byte) TYPE;
        TYPE = 0x02;
        MAJOR = 0x92;
        FIRST = (byte) state;
        SECOND = 0x00;
        THRID = 0x00;
        send();
        TYPE = temp;

    }

    /**
     * 从车摄像头俯仰角控制
     *
     * @param state
     */
    public void rb_cameraControl(int state) {
        byte temp = (byte) TYPE;
        TYPE = 0x02;
        MAJOR = 0x91;
        FIRST = 0x03;
        SECOND = (byte) state;
        THRID = 0x00;
        send();
        TYPE = temp;
    }

    /**
     * 光照档位控制
     *
     * @param gear 档位信息
     */
    public void gear(int gear) {
        if (gear == 1)
            MAJOR = 0x61;
        else if (gear == 2)
            MAJOR = 0x62;
        else if (gear == 3)
            MAJOR = 0x63;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        send();
    }

    private static boolean sendState = false; // 数据发送状态记录，为true时正在发送，为false时发送结束/关闭

    //立体显示
    public boolean infrared_stereo(final short[] data,boolean tip) {
        if (socket.isConnected()){
            if (!sendState && data != null) {
                sendState = true; // 处于发送数据状态，开启发送拦截
                XcApplication.executorServicetor.execute(() -> {
                    MAJOR = 0x10;
                    FIRST = 0xff;
                    SECOND = data[0];
                    THRID = data[1];
                    send();
                    yanchi(500);
                    MAJOR = 0x11;
                    FIRST = data[2];
                    SECOND = data[3];
                    THRID = data[4];
                    send();
                    yanchi(500);
                    MAJOR = 0x12;
                    FIRST = 0x00;
                    SECOND = 0x00;
                    THRID = 0x00;
                    send();
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
        byte temp = (byte) TYPE;
        TYPE = (short) type;
        MAJOR = (byte) major;
        FIRST = (byte) first;
        SECOND = 0x00;
        THRID = 0x00;
        send();
        TYPE = temp;
    }

    /**
     * 舵机角度控制
     *
     * @param major 左侧舵机
     * @param first 右侧舵机
     */
    public void rudder_control(int major, int first) {
        byte temp = (byte) TYPE;
        TYPE = (short) 0x0C;
        MAJOR = (byte) 0x08;
        FIRST = (byte) major;
        SECOND = (byte) first;
        THRID = 0x00;
        send();
        TYPE = temp;
    }

    //立体车库控制
    public void garage_control(int type, int major, int first) {
        byte temp = (byte) TYPE;
        TYPE = (short) type;
        MAJOR = (byte) major;
        FIRST = (byte) first;
        SECOND = 0x00;
        THRID = 0x00;
        send();
        TYPE = temp;
    }

    public void gate(int major, int first, int second, int third) {// 闸门
        byte temp = (byte) TYPE;
        TYPE = 0x03;
        MAJOR = (byte) major;
        FIRST = (byte) first;
        SECOND = (byte) second;
        THRID = (byte) third;
        send();
        TYPE = temp;
    }

    //LCD 显示标志物进入计时模式
    public void digital_close() {//数码管关闭
        byte temp = (byte) TYPE;
        TYPE = 0x04;
        MAJOR = 0x03;
        FIRST = 0x00;
        SECOND = 0x00;
        THRID = 0x00;
        send();
        TYPE = temp;
    }

    public void digital_open() {//数码管打开
        byte temp = (byte) TYPE;
        TYPE = 0x04;
        MAJOR = 0x03;
        FIRST = 0x01;
        SECOND = 0x00;
        THRID = 0x00;
        send();
        TYPE = temp;
    }

    public void digital_clear() {//数码管清零
        byte temp = (byte) TYPE;
        TYPE = 0x04;
        MAJOR = 0x03;
        FIRST = 0x02;
        SECOND = 0x00;
        THRID = 0x00;
        send();
        TYPE = temp;
    }

    public void digital_dic(int dis) {//LCD显示标志物第二排显示距离

        byte temp = (byte) TYPE;
        int a = 0, b = 0, c = 0;
        a = (dis / 100) & (0xF);
        b = (dis % 100 / 10) & (0xF);
        c = (dis % 10) & (0xF);
        b = b << 4;
        b = b | c;
        TYPE = 0x04;
        MAJOR = 0x04;
        FIRST = 0x00;
        SECOND = (short) (a);
        THRID = (short) (b);
        send();
        TYPE = temp;
    }

    public void digital(int i, int one, int two, int three) {// 数码管
        byte temp = (byte) TYPE;
        TYPE = 0x04;
        if (i == 1) {//数据写入第一排数码管
            MAJOR = 0x01;
            FIRST = (byte) one;
            SECOND = (byte) two;
            THRID = (byte) three;
        } else if (i == 2) {//数据写入第二排数码管
            MAJOR = 0x02;
            FIRST = (byte) one;
            SECOND = (byte) two;
            THRID = (byte) three;
        }
        send();
        TYPE = temp;
    }

    public void VoiceBroadcast()  //语音播报随机指令
    {
        byte temp = (byte) TYPE;
        TYPE = (short) 0x06;
        MAJOR = (short) 0x20;
        FIRST = (byte) 0x01;
        SECOND = (byte) 0x00;
        THRID = (byte) 0x00;
        send();
        TYPE = temp;
    }

    public void voiceWeather(int[] weather)  //语音播报随机指令
    {
        byte temp = (byte) TYPE;
        TYPE = (short) 0x06;
        MAJOR = (short) 0x42;
        FIRST = (byte) weather[0];
        SECOND = (byte) weather[1];
        THRID = (byte) 0x00;
        send();
        TYPE = temp;
    }

    public void TFT_LCD(int type, int MAIN, int KIND, int COMMAD, int DEPUTY)  //tft lcd
    {
        byte temp = (byte) TYPE;
        TYPE = (short) type;
        MAJOR = (short) MAIN;
        FIRST = (byte) KIND;
        SECOND = (byte) COMMAD;
        THRID = (byte) DEPUTY;
        send();
        TYPE = temp;
    }

    public void magnetic_suspension(int MAIN, int KIND, int COMMAD, int DEPUTY) //无线充电
    {
        byte temp = (byte) TYPE;
        TYPE = (short) 0x0A;
        MAJOR = (short) MAIN;
        FIRST = (byte) KIND;
        SECOND = (byte) COMMAD;
        THRID = (byte) DEPUTY;
        send();
        TYPE = temp;
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
