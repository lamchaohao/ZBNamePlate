package com.gzzb.zbnameplate.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.gzzb.zbnameplate.fragment.ConnectFragment;
import com.gzzb.zbnameplate.global.Global;

public class WiFiReceiver extends BroadcastReceiver {
    private Handler mHandler;
    public WiFiReceiver(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            mHandler.sendEmptyMessage(ConnectFragment.WIFI_AVAILABLE_ACTION);
        }else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            // 这个监听wifi的打开与关闭，与wifi的连接无关
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED://wifi closed
                    mHandler.sendEmptyMessage(ConnectFragment.WIFI_DISABLE);
                    break;
                case WifiManager.WIFI_STATE_ENABLED://wifi enable
                    mHandler.sendEmptyMessage(ConnectFragment.WIFI_ENABLED);
                    break;
            }
        }else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            //可以监听更精确的状态
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo!=null){
                Message message = mHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putParcelable(Global.EXTRA_NETWORKSTATE,networkInfo);
                message.setData(bundle);
                message.what=ConnectFragment.UPDATE_NETWORK_INFO;
                mHandler.sendMessage(message);

            }

        }
    }
}
