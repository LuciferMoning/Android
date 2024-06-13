package com.frank.plate.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.frank.plate.PlateRecognition;
import com.frank.plate.listener.OnNewFrameListener;
import com.frank.plate.thread.RecognizeThread;
import com.frank.plate.view.PlateRecognizerView;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import car.bkrc.com.car2022.R;


//import car.bkrc.com.car2018.R;


public class PreviewActivity extends AppCompatActivity implements OnNewFrameListener {

    private static final String TAG = PreviewActivity.class.getSimpleName();
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
                    Toast.makeText(PreviewActivity.this, result, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    private PlateRecognizerView recognizerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        plateRecognition = new PlateRecognition(this, mHandler);
        //init plate recognizer
        new Thread(new Runnable() {
            @Override
            public void run() {
                plateRecognition.initRecognizer("pr");
            }
        }).start();

        recognizerView = (PlateRecognizerView) findViewById(R.id.surface_view);
        recognizerView.setOnNewFrameListener(this);
        recognizeThread = new RecognizeThread(plateRecognition);
        recognizeThread.start();
        initOpenCV();
    }

    private void initOpenCV(){
        boolean loadOpenCV = getIntent().getBooleanExtra("loadOpenCV", false);
        Log.i(TAG, "loadOpenCV=" + loadOpenCV);
        if(loadOpenCV){
            recognizerView.enableView();
            return;
        }
        boolean result = OpenCVLoader.initDebug();
        if(result){
            Log.i(TAG, "initOpenCV success...");
            recognizerView.enableView();
        }else {
            Log.e(TAG, "initOpenCV fail...");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(plateRecognition != null){
            //release plate recognizer
            plateRecognition.releaseRecognizer();
        }
        if(recognizeThread != null){
            recognizeThread.setRunning(false);
            recognizeThread.interrupt();
            recognizeThread = null;
        }
    }

    @Override
    public void onNewFrame(Mat newFrame) {
        if(dstMat == null){
            dstMat = new Mat(newFrame.rows(), newFrame.cols(), CvType.CV_8UC4);
        }
        newFrame.copyTo(dstMat);
        if(recognizeThread != null){
            recognizeThread.addMat(dstMat);
        }
    }

}
