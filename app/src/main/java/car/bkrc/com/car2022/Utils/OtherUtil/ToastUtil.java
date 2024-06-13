package car.bkrc.com.car2022.Utils.OtherUtil;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
    private Context context;

    public ToastUtil(Context context) {
        this.context = context;
    }

    public void ShowToast(String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setText(msg);
        toast.show();
    }
}
