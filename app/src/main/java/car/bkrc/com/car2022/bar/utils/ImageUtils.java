package car.bkrc.com.car2022.bar.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.EnvironmentCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author JZ
 * @time 18/12/2020
 */
public class ImageUtils {
    private static ImageUtils imageUtils = null;
    public Bitmap mBitmap;


    public static ImageUtils getInstance(){
        if (null == imageUtils){
            synchronized (ImageUtils.class){
                if (null == imageUtils){
                    imageUtils = new ImageUtils();
                }
            }
        }
        return imageUtils;
    }

    /**
     * 创建保存图片的路径名
     */
    public File createImageFile() throws IOException {
        String imageName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File tempFile = new File(PathUtils.DATAPATH+ "/DCIM/Camera/", "OCV_"+imageName+".jpg");
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }
        return tempFile;
    }

//    /**
//     * 压缩图片
//     * @param activity
//     */
//    public void showPicFileByLuban(final Activity activity) {
//        try {
//            Luban.with(activity)
//                    .load(new File(createImageFile().getPath()))
//                    .setCompressListener(new OnCompressListener() {
//                        @Override
//                        public void onStart() {
//                            // TODO 压缩开始前调用，可以在方法内启动 loading UI
//                        }
//
//                        @Override
//                        public void onSuccess(File file) {
//                            // TODO 压缩成功后调用，返回压缩后的图片文件
//                            mBitmap = BitmapFactory.decodeFile(file.getPath());
//                            Toast.makeText(activity,
//                                    file.length() / 1024 + "K", Toast.LENGTH_LONG).show();
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                            // TODO 当压缩过去出现问题时调用
//                        }
//                    }).launch();//启动压缩
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 将content类型的Uri转化为文件类型的Uri
     * @param activity
     * @param uri
     * @return
     */
    public Uri convertUri(AppCompatActivity activity, Uri uri){
        InputStream is;
        try {
            //Uri ----> InputStream
            is = activity.getContentResolver().openInputStream(uri);
            //InputStream ----> Bitmap
            Bitmap bm = BitmapFactory.decodeStream(is);
            //关闭流
            is.close();
            return ImageUtils.getInstance().saveBitmap(bm);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将Bitmap写入SD卡中的一个文件中
     * 并返回写入文件的Uri
     * @param bm
     * @return
     */
    public Uri saveBitmap(Bitmap bm) {
        //新建文件夹用于存放裁剪后的图片
        File appDir = new File(PathUtils.DATAPATH + "/DCIM/Camera/");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        try {
            File file = createImageFile();
            //打开文件输出流
            FileOutputStream fos = new FileOutputStream(file);
            //将bitmap压缩后写入输出流(参数依次为图片格式、图片质量和输出流)
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            //刷新输出流
            fos.flush();
            //关闭输出流
            fos.close();
            //返回File类型的Uri
            return Uri.fromFile(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
