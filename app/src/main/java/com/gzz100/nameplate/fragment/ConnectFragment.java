package com.gzz100.nameplate.fragment;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gzz100.nameplate.App;
import com.gzz100.nameplate.R;
import com.gzz100.nameplate.activity.ConnectHelpDiagActivity;
import com.gzz100.nameplate.adapter.ConnectAdapter;
import com.gzz100.nameplate.adapter.Listener;
import com.gzz100.nameplate.bean.Device;
import com.gzz100.nameplate.dao.DeviceDao;
import com.gzz100.nameplate.global.Global;
import com.gzz100.nameplate.receiver.WiFiReceiver;
import com.gzz100.nameplate.utils.system.WifiAdmin;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.gzz100.nameplate.global.Global.JUST_STOP_ANIM;
import static com.gzz100.nameplate.global.Global.NETWORK_STATE_CHANGED;
import static com.gzz100.nameplate.global.Global.REQUST_LOCATION_PERMISSION_CODE;
import static com.gzz100.nameplate.global.Global.UPDATE_NETWORK_INFO;
import static com.gzz100.nameplate.global.Global.WIFI_AVAILABLE_ACTION;
import static com.gzz100.nameplate.global.Global.WIFI_DISABLE;
import static com.gzz100.nameplate.global.Global.WIFI_ENABLED;


public class ConnectFragment extends Fragment implements View.OnClickListener, Listener.OnItemClickListener {
    @BindView(R.id.iv_connect_round)
    ImageView mIvRoundInside;
    @BindView(R.id.iv_connect_wifiLogo)
    ImageView mIvWifiLogo;
    @BindView(R.id.tv_connect_tip)
    TextView mTvTip;
    @BindView(R.id.tv_connect_state)
    TextView mTvState;
    @BindView(R.id.tv_connect_checkResult)
    TextView mTvCheckResult;
    @BindView(R.id.iv_connect_help)
    ImageView mIvHelp;
    @BindView(R.id.rv_connect_wifi)
    RecyclerView mRecyclerView;

    private WifiAdmin mWifiAdmin;
    private List<ScanResult> mWifiList;
    private ConnectAdapter mConnectAdapter;
    private Animation mInsideAnim;

    private Handler connHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case JUST_STOP_ANIM:
                    mTvTip.setText(R.string.tapTocheck);
                    mInsideAnim.cancel();
                    break;
                case WIFI_AVAILABLE_ACTION:
                    refreshWifiList();
                    break;
                case WIFI_DISABLE:
                    mWifiList.clear();
                    mTvState.setText(R.string.tos_enable_wifi);
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
                    if (networkInfo != null) {
                        if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                            mTvState.setText(getString(R.string.connectedTo) + mWifiAdmin.getWifiInfo().getSSID());
                            refreshWifiList();
                            connHandler.sendEmptyMessageDelayed(JUST_STOP_ANIM, 3000);
                            updateView();
                        } else if (networkInfo.getState().equals(NetworkInfo.State.CONNECTING)) {
                            mTvState.setText(R.string.connecting);
                        } else if (networkInfo.getState().equals(NetworkInfo.State.DISCONNECTING)) {
                            mTvState.setText(R.string.disconnecting);
                        } else if (networkInfo.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                            mTvState.setText(R.string.disconnected);
                            updateView();
                        }
                    }
                    break;
            }
        }
    };
    private WiFiReceiver mWiFiReceiver;
    private long mOldTime;
    private DeviceDao mDeviceDao;
    private List<Device> mAddedList;
    private long mUpdateTime;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connect, null);
        ButterKnife.bind(this, view);
        RequestPermission();
        loadData();
        initView();
        registerWifiReciver();//监听WiFi状态广播
        return view;
    }


    private void initView() {
        mInsideAnim = AnimationUtils.loadAnimation(getContext(), R.anim.search_round);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mConnectAdapter = new ConnectAdapter(getContext(), mWifiList, mWifiAdmin,mAddedList);
        mRecyclerView.setAdapter(mConnectAdapter);
        mRecyclerView.setHasFixedSize(true);//设定高度固定,可提高效率
        mIvRoundInside.setOnClickListener(this);
        mConnectAdapter.setItemOnClickListener(this);
        mTvTip.setText(R.string.checking);
    }

    private void connectWifi(int position) {
        //检查是否已连接过
        WifiConfiguration exsitsConfig = mWifiAdmin.isExsits(mWifiList.get(position).SSID);//这里的SSID 打印出来没有双引号包括
        if (exsitsConfig != null) {
            // 1.已连接过，直接使用该配置进行连接
            mWifiAdmin.setMaxPriority(exsitsConfig);//已经连接过的，需要设置优先级为最大的才能连上
            mWifiAdmin.connectWifi(exsitsConfig.networkId);
        } else {
            WifiConfiguration wifiInfo2 = mWifiAdmin.createWifiInfo2(mWifiList.get(position), Global.CARD_PASSWORD);
            mWifiAdmin.addNetWork(wifiInfo2);
        }
    }


    private void loadData() {
        mWifiAdmin = new WifiAdmin(getContext());
        mDeviceDao = ((App) getActivity().getApplication()).getDaoSession().getDeviceDao();
        List<ScanResult> scanResults = mWifiAdmin.startScan();
        mWifiList = new ArrayList<>();
        mAddedList=mDeviceDao.queryBuilder().list();
        for (ScanResult scanResult : scanResults) {
            boolean startFlag = scanResult.SSID.startsWith(Global.SSID_START);
            boolean endFlag = scanResult.SSID.endsWith(Global.SSID_END);
            if (startFlag&&endFlag){
                mWifiList.add(scanResult);
            }
        }

    }

    private void registerWifiReciver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.setPriority(Integer.MAX_VALUE); // 设置优先级，最高为1000
        mWiFiReceiver = new WiFiReceiver(connHandler);
        getContext().registerReceiver(mWiFiReceiver, intentFilter);
    }


    private void refreshWifiList() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mOldTime < 2000) {
            return;
        }
        mOldTime = currentTime;
        List<ScanResult> scanResults = mWifiAdmin.mWifiManager.getScanResults();
        mWifiList.clear();
        mAddedList.clear();
        List<Device> list = mDeviceDao.queryBuilder().list();
        mAddedList.addAll(list);
        for (ScanResult scanResult : scanResults) {
            boolean startFlag = scanResult.SSID.startsWith(Global.SSID_START);
            boolean endFlag = scanResult.SSID.endsWith(Global.SSID_END);
            if (startFlag&&endFlag){
                mWifiList.add(scanResult);
                mConnectAdapter.notifyItemInserted(mWifiList.size());
                mRecyclerView.smoothScrollToPosition(mWifiList.size());
            }
        }
        String result = getString(R.string.detectResult) + mWifiList.size();
        mTvCheckResult.setText(result);
        mConnectAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mConnectAdapter);
    }

    private void updateView(){
        long currentTime = System.currentTimeMillis();
        if (currentTime-mUpdateTime<1000){
            return;
        }
        mUpdateTime = currentTime;
        String ssid = mWifiAdmin.getWifiInfo().getSSID();
        boolean startFlag = ssid.contains(Global.SSID_START);
        boolean endFlag = ssid.contains(Global.SSID_END);
        if (startFlag&&endFlag){
            mTvState.setTextColor(Color.parseColor("#00C853"));
            mIvWifiLogo.setImageResource(R.drawable.ic_wifi_green_a700_svg);
            mIvRoundInside.setImageResource(R.drawable.connect_view_completed);
        }else {
            mTvState.setTextColor(Color.parseColor("#757575"));
            mIvWifiLogo.setImageResource(R.drawable.ic_wifi_green_a700_svg_uncomplete);
            mIvRoundInside.setImageResource(R.drawable.connect_view_uncomplete);
        }

        String result = getString(R.string.detectResult) + mWifiList.size();
        mTvCheckResult.setText(result);
        mConnectAdapter.notifyDataSetChanged();
    }

    private void roundClick() {
        updateView();
        mWifiAdmin.startScan();
        if (mWifiList.size() == 1) {
            if (mWifiAdmin.getWifiInfo().getSSID().equals("\"" + mWifiList.get(0).SSID + "\"")) {
                return;
            }
            mIvRoundInside.startAnimation(mInsideAnim);
            mTvTip.setText(R.string.checking);
            connectWifi(0);
        } else {
            if (mWifiAdmin.checkWifiState()) {
                mIvRoundInside.startAnimation(mInsideAnim);
                mTvTip.setText(R.string.checking);
                refreshWifiList();
                connHandler.sendEmptyMessageDelayed(JUST_STOP_ANIM, 3000);
            } else {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                Toast.makeText(getContext(), R.string.tos_enable_wifi, Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onItemClick(View view, int position) {
        //点击，开始动画旋转
        //先判断当前连接是不是这个，如果是则不进行操作，如果不是才进行连接
        if (mWifiAdmin.getWifiInfo().getSSID().equals("\"" + mWifiList.get(position).SSID + "\"")) {
            return;
        }
        mIvRoundInside.startAnimation(mInsideAnim);
        connectWifi(position);
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void RequestPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        } else {
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
            Toast.makeText(getContext(), R.string.tos_needPermission, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(mWiFiReceiver);//取消监听广播
    }

    @OnClick({R.id.iv_connect_round, R.id.iv_connect_help})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_connect_help:
                startActivity(new Intent(getContext(), ConnectHelpDiagActivity.class));
                break;
            case R.id.iv_connect_round:
                roundClick();
                break;
        }
    }
}
