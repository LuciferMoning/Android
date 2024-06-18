package car.bkrc.com.car2022.bar.ocr;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;

import com.baidu.ai.edge.core.base.BaseException;
import com.baidu.ai.edge.core.base.CallException;
import com.baidu.ai.edge.core.infer.InferConfig;
import com.baidu.ai.edge.core.infer.InferManager;
import com.baidu.ai.edge.core.ocr.OcrResultModel;

import java.util.ArrayList;
import java.util.List;

import car.bkrc.com.car2022.FragmentView.LeftFragment;
import car.bkrc.com.car2022.Utils.CameraUtile.XcApplication;


/**
 * 通用arm 开源OCR模型
 */
public class TestInferOcrTask {
    private static final int NUM_OF_RUNS = 1;
    private static final int NUM_OF_API_CALLS = 1;
    private static final float CONFIDENCE = 0.5f;
    private static final String SERIAL_NUM = " ";
    public static String restr="";


    public String ocr()  {
            restr = "";
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        InferConfig config = new InferConfig(XcApplication.getContext().getAssets(), "infer");
                        InferManager manager = new InferManager(XcApplication.getContext(), config, SERIAL_NUM);
                        Bitmap image = LeftFragment.bitmap;

                        /* 2.2 推理图片及解析结果 */
                        List<OcrResultModel> results = new ArrayList<>();
                        String resStr = "";

                        for (int j = 0; j < NUM_OF_API_CALLS; j++) {
                            // 在模型销毁前可以不断调用。但是不支持多线程
                            try {
                                results = manager.ocr(image, CONFIDENCE);
                            } catch (BaseException e) {
                                e.printStackTrace();
                            }
                            for (int m = 0; m < results.size(); m++) {
                                // 解析结果
                                if (results != null) {
                                    resStr = "{size:" + results.size() + ", firstRes:{";
                                    if (results.size() > 0) {
                                        resStr += "labelName:" + results.get(m).getLabel() + ", box:{";
                                        restr += "" + results.get(m).getLabel();
                                        for (Point point : results.get(m).getPoints()) {
                                            resStr += "[" + point.x + "," + point.y + "]";
                                        }
                                        resStr += "}";
                                    }
                                    resStr += "}}";
                                } else {
                                    resStr = "{}";
                                }
                                Log.e("文字识别", "Predict " + j + ": " + resStr + "\n");
                                Log.e("文字识别", "Predict " + j + ": " + restr + "\n");
                            }

                            manager.destroy();

                        }
                    } catch (CallException e) {
                        e.printStackTrace();
                    } catch (BaseException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            /* 1. 准备配置类，初始化Manager类。可以在onCreate或onResume中触发，请在非UI线程里调用 */


        return restr;
    }
}
