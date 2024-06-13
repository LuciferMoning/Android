package car.bkrc.com.car2022.ActivityView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import car.bkrc.com.car2022.MessageBean.DataRefreshBean;
import car.bkrc.com.car2022.R;
import car.bkrc.com.car2022.Utils.CameraUtile.CameraSearchService;
import car.bkrc.com.car2022.Utils.CameraUtile.XcApplication;
import car.bkrc.com.car2022.Utils.OtherUtil.CameraConnectUtil;
import car.bkrc.com.car2022.Utils.OtherUtil.ToastUtil;
import car.bkrc.com.car2022.Utils.OtherUtil.WiFiStateUtil;
import car.bkrc.com.car2022.Utils.dialog.ShowDialog;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText device_edit = null;
    private EditText login_edit = null;
    private EditText passwd_edit = null;
    private ToastUtil toastUtil;
    private LinearLayout wifi_back,uart_back;

    private Button bt_connect = null;
    private ImageView rememberbox = null,uart_state_image,wifi_state_image;
    private TextView wifi_box = null, uart_box = null;
    private boolean passwordState = false;
    private String wifiName = "";
    protected boolean allPermissionsGranted;
    private ProgressDialog dialog = null;
    private CameraConnectUtil cameraConnectUtil;

    @RequiresApi(api = Build.VERSION_CODES.M)
    void Request() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            } else {
                if (getConnectWifiSsid() == "<unknown ssid>") {
                    toastUtil.ShowToast("当前未接入到Wi-Fi网络中，请连接平台Wi-Fi");
                } else if (getConnectWifiSsid().contains("BKRC")){
                    toastUtil.ShowToast("当前连接WiFi：" + getConnectWifiSsid().replaceAll("\"",""));
                }else toastUtil.ShowToast("请检查WiFi是否连接正确！");
            }
        }
    }

    void RequestFile() {
        String[] perms = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE};
        for (String p : perms) {
            int f = ContextCompat.checkSelfPermission(LoginActivity.this, p);
            Log.d("---", String.format("%s - %d", p, f));
            if (f != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(perms, 0XCF);
                break;
            }
        }

    }

    /**
     * 判断是否首次启动
     *
     * @return
     */
    private boolean firstRun() {
        SharedPreferences sharedPreferences = getSharedPreferences("FirstRun", 0);
        String first_run = sharedPreferences.getString("First", "2022-V1.0");
        if (first_run.equals("2022-V1.0")) {
            sharedPreferences.edit().putString("First", "2022-V1.1").apply();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 应用升级提醒弹窗
     */
    private void upDialog() {
        ShowDialog showDialog = new ShowDialog();
        showDialog.show(LoginActivity.this, "应用更新说明");
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case 1:
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // TODO request success
//                    if (getConnectWifiSsid() == "<unknown ssid>") {
//                        toastUtil.ShowToast("当前未接入到Wi-Fi网络中，请连接平台Wi-Fi");
//                    } else if (getConnectWifiSsid().contains("BKRC")){
//                        toastUtil.ShowToast("当前连接WiFi：" + getConnectWifiSsid().replaceAll("\"",""));
//                    }else toastUtil.ShowToast("请检查WiFi是否连接正确！");
//                }
//                break;
//        }
//    }
    @RequiresApi(api = Build.VERSION_CODES.M)
        void requestMyPermissions() {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //没有授权，编写申请权限代码
                ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            } else {
                Log.d("LoginActivity:", "requestMyPermissions: 有写SD权限");
            }
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //没有授权，编写申请权限代码
                ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            } else {
                Log.d("LoginActivity:", "requestMyPermissions: 有读SD权限");
            }

    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏

        // 判断是否是平板
        if (isPad(this)) {
            setContentView(R.layout.activity_login);
        } else {
            setContentView(R.layout.activity_login_mobilephone);
        }
        EventBus.getDefault().register(this); // EventBus消息注册
        cameraConnectUtil = new CameraConnectUtil(this);
        findViews();  //控件初始化
        RequestFile();
        cameraConnectUtil.cameraInit();//摄像头初始化
//        Request();
        initPermission();
//        requestMyPermissions();
//        requestStoragePermission();
        if (firstRun()) {
            upDialog();
        }
    }


    /**
     * 判断当前设备是手机还是平板，代码来自 Google I/O App for Android
     *
     * @param context
     * @return 平板返回 True，手机返回 False
     */
    public static boolean isPad(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void findViews() {
        toastUtil = new ToastUtil(this);
        device_edit = findViewById(R.id.deviceid);
        login_edit = findViewById(R.id.loginname);
        passwd_edit = findViewById(R.id.loginpasswd);
        Button bt_reset = findViewById(R.id.reset);
        bt_connect = findViewById(R.id.connect);
        rememberbox = findViewById(R.id.remember);
        wifi_state_image = findViewById(R.id.wifi_image);
        uart_state_image = findViewById(R.id.uart_image);
        wifi_box = findViewById(R.id.wifi_each);
        uart_box = findViewById(R.id.uart_each);
        wifi_back = findViewById(R.id.wifi_back);
        uart_back = findViewById(R.id.uart_back);
        bt_reset.setOnClickListener(this);
        bt_connect.setOnClickListener(this);
        rememberbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!passwordState) {
                    setPasswordState(true);
                } else {
                    setPasswordState(false);
                }
            }
        });

        wifi_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifi_box.setTextColor(getResources().getColor(R.color.switch_black));
                uart_box.setTextColor(getResources().getColor(R.color.shift_color_gray));
                uart_state_image.setBackground(getResources().getDrawable(R.drawable.ic_uart_off));
                wifi_state_image.setBackground(getResources().getDrawable(R.drawable.ic_wifi));
                wifi_back.setBackground(getResources().getDrawable(R.drawable.login_switch_background_on));
                uart_back.setBackground(getResources().getDrawable(R.drawable.login_switch_background_off));
                XcApplication.isserial = XcApplication.Mode.SOCKET;
            }
        });
        uart_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uart_box.setTextColor(getResources().getColor(R.color.switch_black));
                wifi_box.setTextColor(getResources().getColor(R.color.shift_color_gray));
                uart_state_image.setBackground(getResources().getDrawable(R.drawable.ic_uart));
                wifi_state_image.setBackground(getResources().getDrawable(R.drawable.ic_wifi_off));
                wifi_back.setBackground(getResources().getDrawable(R.drawable.login_switch_background_off));
                uart_back.setBackground(getResources().getDrawable(R.drawable.login_switch_background_on));
                XcApplication.isserial = XcApplication.Mode.USB_SERIAL;
            }
        });
    }

    /**
     * 设置密码隐藏/显示状态
     *
     * @param state state = true : 显示
     *              state = false ： 隐藏
     */
    private void setPasswordState(boolean state) {
        if (state) {
            passwd_edit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            rememberbox.setBackground(getResources().getDrawable(R.drawable.ic_on));
            passwordState = true;
        } else {
            passwd_edit.setTransformationMethod(PasswordTransformationMethod.getInstance());
            rememberbox.setBackground(getResources().getDrawable(R.drawable.ic_off));
            passwordState = false;
        }
    }

    private String getConnectWifiSsid() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.d("wifiInfo", wifiInfo.toString());
        Log.d("SSID", wifiInfo.getSSID());
        return wifiInfo.getSSID();
    }

    public String getConnectWifiSsidTwo() {
        WifiManager wifiManager = ((WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE));
        assert wifiManager != null;

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String SSID = wifiInfo.getSSID();

        int networkId = wifiInfo.getNetworkId();
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : configuredNetworks) {
            if (wifiConfiguration.networkId == networkId) {
                SSID = wifiConfiguration.SSID;
            }
        }

        return SSID.replace("\"", "");
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.reset) {
            device_edit.setText("");
            login_edit.setText("");
            passwd_edit.setText("");
            setPasswordState(false);
        } else if (view.equals(bt_connect)) {
            dialog = new ProgressDialog(this);
            dialog.setMessage("撸起袖子加载中...");
            dialog.show();
            if (XcApplication.isserial == XcApplication.Mode.SOCKET) {
                useNetwork();
            } else if (XcApplication.isserial != XcApplication.Mode.SOCKET) {
                useUart();
            }
        }
    }

    /**
     * 接收Eventbus消息
     *
     * @param refresh
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DataRefreshBean refresh) {
        if (refresh.getRefreshState() == 2) {
            startFirstActivity();
        }
    }

    // 搜索摄像cameraIP
    private void search() {
        Intent intent = new Intent(LoginActivity.this, CameraSearchService.class);
        startService(intent);
    }


    private void useUart() {
        // 搜索摄像头然后启动摄像头
        search();
    }

    private void useNetwork() {
        //2.
        if (new WiFiStateUtil(this).wifiInit()) {
            //WiFi初始化成功
            search();
        } else {
            dialog.cancel();
            toastUtil.ShowToast("请确认设备已通过WiFi接入竞赛平台！");
        }
    }

    private void startFirstActivity() {
        dialog.cancel();
        startActivity(new Intent(LoginActivity.this, FirstActivity.class));
        if (FirstActivity.IPCamera.equals("null:81")) {
            toastUtil.ShowToast("摄像头没有找到，快去找找它吧");
        }
        finish();
    }

    private static final int REQUEST_PERMISSION_CODE = 123;

    // 在适当的地方调用该方法以请求权限
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        } else {
            // 已经有权限，执行相应的操作
        }
    }

    // 在Activity中重写该方法以接收权限请求的结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，执行相应的操作
            } else {
                // 权限被拒绝，无法执行相应的操作
            }
        }
    }
    private void initPermission() {
        if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
                return;
            }
        }
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CAMERA

        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
                Log.e("TAG", "权限不够" );
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this); // EventBus消息注销
        if (dialog != null) {
            dialog.cancel();
        }
        Log.e("LoginActivity", "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("LoginActivity", "onRestart");
    }

}

