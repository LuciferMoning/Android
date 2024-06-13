package car.bkrc.com.car2022.bar.utils;

import android.os.Environment;

import java.io.File;

/**
 * @author JZ
 * @time 18/12/2020
 */
public class PathUtils {

    //TessBaseAPI初始化用到的第一个参数，是个目录
    public static final String DATAPATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator;
    //在DATAPATH中新建这个目录，TessBaseAPI初始化要求必须有这个目录
    public static final String tessdata = DATAPATH + File.separator + "tessdata";
    //TessBaseAPI初始化测第二个参数，就是识别库的名字不要后缀名。
    public static String DEFAULT_LANGUAGE = "eng+chi_sim";
    //assets中的文件名
    public static String DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";
    //保存到SD卡中的完整文件名
    public static String LANGUAGE_PATH = tessdata + File.separator + DEFAULT_LANGUAGE_NAME;

}
