package com.frank.plate;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.frank.plate.thread.RecognizeThread;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import car.bkrc.com.car2022.ActivityView.FirstActivity;

public class PlateManager {

    private static final String TAG = PlateManager.class.getSimpleName();
    private PlateRecognition plateRecognition;
    private RecognizeThread recognizeThread;
    private Mat dstMat;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case PlateRecognition.MSG_RESULT://recognize finish
                    String result = (String) msg.obj;
                    Toast.makeText(FirstActivity.getInstance(), result, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    public void init(){
        plateRecognition = new PlateRecognition(FirstActivity.getInstance(), mHandler);
        new Thread(new Runnable() {
            @Override
            public void run() {
                plateRecognition.initRecognizer("pr");
            }
        }).start();
        recognizeThread = new RecognizeThread(plateRecognition);
        recognizeThread.start();
        initOpenCV();

    }

    private void initOpenCV() {
        boolean result = OpenCVLoader.initDebug();
        if(result){
            Log.i(TAG, "initOpenCV success...");
        }else {
            Log.e(TAG, "initOpenCV fail...");
        }
    }

    public void chunkMat(Bitmap bitmap){
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap,mat);
        if(recognizeThread != null){
            recognizeThread.addMat(mat);
        }
    }
}
