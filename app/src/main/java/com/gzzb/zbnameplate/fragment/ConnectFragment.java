package com.gzzb.zbnameplate.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gzzb.zbnameplate.R;
import com.gzzb.zbnameplate.activity.ConnectHelpDiagActivity;
import com.gzzb.zbnameplate.adapter.ConnectAdapter;
import com.gzzb.zbnameplate.global.Global;
import com.gzzb.zbnameplate.receiver.WiFiReceiver;
import com.gzzb.zbnameplate.utils.WifiAdmin;

import java.util.ArrayList;
import java.util.List;


public class ConnectFragment extends Fragment implements View.OnClickListener,ConnectAdapter.OnItemOnClickListener{

    public static final int REQUST_LOCATION_PERMISSION_CODE = 301;
    public static final int WIFI_AVAILABLE_ACTION = 401;
    public static final int WIFI_DISABLE = 501;
    public static final int WIFI_ENABLED = 601;
    public static final int NETWORK_STATE_CHANGED = 701;
    public static final int UPDATE_NETWORK_INFO = 801;
    private static final int JUST_STOP_ANIM = 901;
    private RecyclerView mRecyclerView;
    private WifiAdmin mWifiAdmin;
    private List<ScanResult> mWifiList;
    private ConnectAdapter mConnectAdapter;
    private ImageView mIvRoundInside;
    private Animation mInsideAnim;

    private Handler connHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case JUST_STOP_ANIM:
                    mTvTip.setText("点击查找");
                    mInsideAnim.cancel();
                    break;
                case WIFI_AVAILABLE_ACTION:
                    Log.w("connectFrag","WIFI_AVAILABLE_ACTION()");
                    refreshWifiList();
                    break;
                case WIFI_DISABLE:
                    mWifiList.clear();
                    mTvState.setText("未开启Wi-Fi");
                    mConnectAdapter.notifyDataSetChanged();
                    break;
                case WIFI_ENABLED:
                    refreshWifiList();
                    break;
                case NETWORK_STATE_CHANGED:
                    refreshWifiList();
                    break;
                case UPDATE_NETWORK_INFO:
                    NetworkInfo networkInfo = msg.getData().getParcelable(Global.EXTRA_NETWORKSTATE);
                    if (networkInfo!=null){
                        if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)){
                            mTvState.setText("已连接到"+mWifiAdmin.getWifiInfo().getSSID());
                            connHandler.sendEmptyMessageDelayed(JUST_STOP_ANIM,3000);
                        }else if (networkInfo.getState().equals(NetworkInfo.State.CONNECTING)){
                            mTvState.setText("正在连接");
                        }else if (networkInfo.getState().equals(NetworkInfo.State.DISCONNECTING)){
                            mTvState.setText("正在断开");
                        }else if (networkInfo.getState().equals(NetworkInfo.State.DISCONNECTED)){
                            mTvState.setText("已断开");
                        }
                        Log.w("connectFrag","UPDATE_NETWORK_INFO()");
                        // 刷新状态显示
                        refreshWifiList();
                    }
                    break;
            }
        }
    };
    private ImageView mIvWifiLogo;
    private TextView mTvTip;
    private TextView mTvState;
    private TextView mTvCheckResult;
    private WiFiReceiver mWiFiReceiver;
    private long mOldTime;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connect, null);
        RequestPermission();
        loadData();
        initView(view);
        registerWifiReciver();//监听WiFi状态广播
        return view;
    }


    private void initView(View view) {
        mIvRoundInside = (ImageView) view.findViewById(R.id.iv_connect_round);
        mIvWifiLogo = (ImageView) view.findViewById(R.id.iv_connect_wifiLogo);
        mTvTip = (TextView) view.findViewById(R.id.tv_connect_tip);
        mTvState = (TextView) view.findViewById(R.id.tv_connect_state);
        mTvCheckResult = (TextView) view.findViewById(R.id.tv_connect_checkResult);
        ImageView ivHelp= (ImageView) view.findViewById(R.id.iv_connect_help);
        mInsideAnim = AnimationUtils.loadAnimation(getContext(), R.anim.search_round);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_connect_wifi);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mConnectAdapter = new ConnectAdapter(getContext(), mWifiList, mWifiAdmin);
        mRecyclerView.setAdapter(mConnectAdapter);
        mIvRoundInside.setOnClickListener(this);
        mConnectAdapter.setItemOnClickListener(this);
        ivHelp.setOnClickListener(this);
        mTvTip.setText("正在查找");
    }

    private void connectWifi(int position) {
        //检查是否已连接过
        WifiConfiguration exsitsConfig = mWifiAdmin.isExsits(mWifiList.get(position).SSID);//这里的SSID 打印出来没有双引号包括
        if (exsitsConfig!=null){
            // 1.已连接过，直接使用该配置进行连接
            mWifiAdmin.setMaxPriority(exsitsConfig);//已经连接过的，需要设置优先级为最大的才能连上
            mWifiAdmin.connectWifi(exsitsConfig.networkId);
        }else {
            WifiConfiguration wifiInfo2 = mWifiAdmin.createWifiInfo2(mWifiList.get(position), Global.CARD_PASSWORD);
            mWifiAdmin.addNetWork(wifiInfo2);
        }
    }


    private void loadData() {
        mWifiAdmin = new WifiAdmin(getContext());
        List<ScanResult> scanResults = mWifiAdmin.startScan();
        mWifiList=new ArrayList<>();
        for (ScanResult scanResult : scanResults) {
            boolean startFlag = scanResult.SSID.startsWith("HC-LED[");
            boolean endFlag = scanResult.SSID.endsWith("]");
            if (startFlag&&endFlag){
                mWifiList.add(scanResult);
            }
        }

    }
    private void registerWifiReciver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION); // ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.setPriority(Integer.MAX_VALUE); // 设置优先级，最高为1000
        mWiFiReceiver = new WiFiReceiver(connHandler);
        getContext().registerReceiver(mWiFiReceiver,intentFilter);
    }


    private void refreshWifiList() {
        long currentTime = System.currentTimeMillis();
        if (!(currentTime-mOldTime>5000)){
            return;
        }
        mOldTime = currentTime;

        Log.i("connectFrag","refreshWifiList()");
        List<ScanResult> scanResults = mWifiAdmin.startScan();
        mWifiList.clear();

        for (ScanResult scanResult : scanResults) {
            boolean startFlag = scanResult.SSID.contains("HC-LED[");
            boolean endFlag = scanResult.SSID.contains("]");
            if (startFlag&&endFlag){
                mWifiList.add(scanResult);
            }
        }
        mConnectAdapter.notifyDataSetChanged();
        String ssid = mWifiAdmin.getWifiInfo().getSSID();
        boolean startFlag = ssid.contains("HC-LED[");
        boolean endFlag = ssid.contains("]");
        if (startFlag&&endFlag){
            mTvState.setTextColor(Color.parseColor("#00C853"));
            mIvWifiLogo.setImageResource(R.drawable.ic_wifi_green_a700_svg);
            mIvRoundInside.setImageResource(R.drawable.connect_view_completed);
        }else {
            mTvState.setTextColor(Color.parseColor("#757575"));
            mIvWifiLogo.setImageResource(R.drawable.ic_wifi_green_a700_svg_uncomplete);
            mIvRoundInside.setImageResource(R.drawable.connect_view_uncomplete);
        }
        String result="检测到"+mWifiList.size()+"个铭牌";
        mTvCheckResult.setText(result);

    }

    private void roundClick() {
        if (mWifiList.size()==1){
            if (mWifiAdmin.getWifiInfo().getSSID().equals("\""+mWifiList.get(0).SSID+"\"")){
                return;
            }
            mIvRoundInside.startAnimation(mInsideAnim);
            mTvTip.setText("正在查找");
            connectWifi(0);
        }else {
            if (mWifiAdmin.checkWifiState()) {
                mIvRoundInside.startAnimation(mInsideAnim);
                mTvTip.setText("正在查找");
                refreshWifiList();
                connHandler.sendEmptyMessageDelayed(JUST_STOP_ANIM,3000);
            }else {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                Toast.makeText(getContext(), "请开启Wi-Fi", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_connect_help:
                startActivity(new Intent(getContext(), ConnectHelpDiagActivity.class));
                break;
            case R.id.iv_connect_round:
                roundClick();
                break;
        }
    }



    @Override
    public void onItemClick(View view, int position) {
        //点击，开始动画旋转
        //先判断当前连接是不是这个，如果是则不进行操作，如果不是才进行连接
        if (mWifiAdmin.getWifiInfo().getSSID().equals("\""+mWifiList.get(position).SSID+"\"")){
            return;
        }
        mIvRoundInside.startAnimation(mInsideAnim);
        connectWifi(position);
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void RequestPermission() {
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // 给出一个提示，告诉用户为什么需要这个权限

            } else {
                // 用户没有拒绝，直接申请权限
                String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions(getActivity(), permissions, REQUST_LOCATION_PERMISSION_CODE);
                //用户授权的结果会回调到FragmentActivity的onRequestPermissionsResult
                loadData();
            }
        }else {
            //已经拥有授权
            loadData();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadData();
        } else {
            // 权限拒绝了
            loadData();
            Toast.makeText(getContext(), "需要权限才能开启WIFI", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(mWiFiReceiver);//取消监听广播
    }

}
