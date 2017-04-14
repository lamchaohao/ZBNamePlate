package com.gzz100.nameplate.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.gzz100.nameplate.global.Global;
import com.gzz100.nameplate.receiver.WiFiReceiver;
import com.gzz100.nameplate.utils.system.WifiAdmin;

import java.util.ArrayList;
import java.util.List;

import static com.gzz100.nameplate.global.Global.CONNECT_NORESPONE;
import static com.gzz100.nameplate.global.Global.GENFILE_DONE;
import static com.gzz100.nameplate.global.Global.PAUSE_FAILE;
import static com.gzz100.nameplate.global.Global.PREPARE_FILE;
import static com.gzz100.nameplate.global.Global.REQUST_LOCATION_PERMISSION_CODE;
import static com.gzz100.nameplate.global.Global.SEND_DONE;
import static com.gzz100.nameplate.global.Global.UPDATE_NETWORK_INFO;
import static com.gzz100.nameplate.global.Global.UPDATE_PROGRESS;
import static com.gzz100.nameplate.global.Global.WIFI_AVAILABLE_ACTION;
import static com.gzz100.nameplate.global.Global.WIFI_DISABLE;
import static com.gzz100.nameplate.global.Global.WIFI_ENABLED;
import static com.gzz100.nameplate.global.Global.WIFI_ERRO;


public abstract class BaseWifiActivity extends BaseActivity {

    private WiFiReceiver mWiFiReceiver;
    protected Handler mHandler=new WifiHandler();
    protected WifiAdmin mWifiAdmin ;
    protected List<ScanResult> mWifiList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RequestPermission();
        registerWifiReciver();
        loadData();
    }

    private void loadData() {
        mWifiAdmin = new WifiAdmin(this);
        mWifiList = mWifiAdmin.startScan();
    }


    protected void registerWifiReciver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION); // ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.setPriority(Integer.MAX_VALUE); // 设置优先级，最高为1000
        mWiFiReceiver = new WiFiReceiver(mHandler);
        registerReceiver(mWiFiReceiver, intentFilter);
    }


    class WifiHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_DONE:
                    onSendDone();
                    break;
                case GENFILE_DONE:
                    onGenFileDone(msg);
                    break;
                case UPDATE_PROGRESS:
                    onUpdateProgress(msg);
                    break;
                case PREPARE_FILE:
                    onPrepareFile(msg);
                    break;
                case WIFI_ERRO:
                    onWiFiErro(msg);
                    break;
                case CONNECT_NORESPONE:
                   onConnectNoRespone(msg);
                    break;
                case WIFI_AVAILABLE_ACTION:
                    onScanAvailable(msg);
                    break;
                case WIFI_DISABLE:
                    onWifiDisable(msg);
                    break;
                case PAUSE_FAILE:
                    onPauseFail();
                    break;
                case WIFI_ENABLED:
                   onWifiEnable(msg);
                    break;
                case UPDATE_NETWORK_INFO:
                    NetworkInfo networkInfo = msg.getData().getParcelable(Global.EXTRA_NETWORKSTATE);
                    onUpdateNetworkInfo(networkInfo);
                    break;
            }
        }
    }

    protected void onPauseFail() {

    }

    protected void onUpdateNetworkInfo(NetworkInfo networkInfo){}

    protected void onWifiEnable(Message msg){}

    protected void onWifiDisable(Message msg){}

    protected void onScanAvailable(Message msg){}

    protected void onConnectNoRespone(Message msg){}

    protected void onWiFiErro(Message msg){}

    protected void onPrepareFile(Message msg){}

    protected void onUpdateProgress(Message msg){}

    protected void onGenFileDone(Message msg){}

    protected void onSendDone(){}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWiFiReceiver);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void RequestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // 给出一个提示，告诉用户为什么需要这个权限
                Toast.makeText(this, "we need this to detect wifi", Toast.LENGTH_SHORT).show();
            } else {
                // 用户没有拒绝，直接申请权限
                String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions(this, permissions, REQUST_LOCATION_PERMISSION_CODE);
                //用户授权的结果会回调到FragmentActivity的onRequestPermissionsResult
               onPermissionGrant();
            }
        } else {
            //已经拥有授权
            onPermissionGrant();
        }
    }

    protected void onPermissionGrant() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onPermissionGrant();
        } else {
            // 权限拒绝了
            onPermissionDeny();
            Toast.makeText(this, "需要权限才能正常运行", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
    }

    protected void onPermissionDeny(){

    }
}
