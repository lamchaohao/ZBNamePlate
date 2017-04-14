package com.gzz100.nameplate.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gzz100.nameplate.App;
import com.gzz100.nameplate.R;
import com.gzz100.nameplate.activity.DeviceManageActivity;
import com.gzz100.nameplate.adapter.AvaliAdapter;
import com.gzz100.nameplate.adapter.Listener;
import com.gzz100.nameplate.bean.Device;
import com.gzz100.nameplate.dao.DeviceDao;
import com.gzz100.nameplate.global.Global;
import com.gzz100.nameplate.receiver.WiFiReceiver;
import com.gzz100.nameplate.utils.system.WifiAdmin;

import java.util.ArrayList;
import java.util.List;

import static com.gzz100.nameplate.global.Global.REQUST_LOCATION_PERMISSION_CODE;
import static com.gzz100.nameplate.global.Global.WIFI_AVAILABLE_ACTION;


public class AvailableDeviceFragment extends Fragment implements Listener.OnAddOnClickListener,Listener.OnItemClickListener{

    private WifiAdmin mWifiAdmin;
    private List<ScanResult> mWifiList;
    private WiFiReceiver mWiFiReceiver;


    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WIFI_AVAILABLE_ACTION:
                    refreshWifiList();
                    break;
            }
        }
    };
    private AvaliAdapter mAdapter;
    private DeviceDao mDeviceDao;
    private List<Device> mAddedList;
    private View mTipsView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RequestPermission();
        registerWifiReciver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_available_device, container, false);
        loadData();
        initView(view);
        return view;
    }

    private void initView(View view) {
        RecyclerView rvAvail = (RecyclerView) view.findViewById(R.id.rvAvail);
        mTipsView = view.findViewById(R.id.rv_avail_tips);
        rvAvail.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new AvaliAdapter(getActivity(),mWifiList);
        rvAvail.setHasFixedSize(true);//设定高度固定,可提高效率
        rvAvail.setAdapter(mAdapter);
        mAdapter.setItemOnClickListener(this);
        mAdapter.setOnAddOnClickListener(this);

        if (mWifiList.size()==0) {
            mTipsView.setVisibility(View.VISIBLE);
        }else {
            mTipsView.setVisibility(View.GONE);
        }

    }
    private void loadData() {
        mDeviceDao = ((App) getActivity().getApplication()).getDaoSession().getDeviceDao();
        mAddedList = mDeviceDao.queryBuilder().list();
        mWifiAdmin = new WifiAdmin(getContext());
        List<ScanResult> scanResults = mWifiAdmin.startScan();
        mWifiList=new ArrayList<>();
        for (ScanResult scanResult : scanResults) {
            boolean startFlag = scanResult.SSID.startsWith(Global.SSID_START);
            boolean endFlag = scanResult.SSID.endsWith(Global.SSID_END);
            boolean isAdded=false;
            for (Device device : mAddedList) {
                String ssid = device.getSsid();
                if (scanResult.SSID.equals(ssid)) {
                    isAdded=true;
                    break;
                }
            }
            if (startFlag&&endFlag&&!isAdded){
                mWifiList.add(scanResult);
            }
        }

    }
    private void registerWifiReciver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION); //
        intentFilter.setPriority(Integer.MAX_VALUE); // 设置优先级，最高为1000
        mWiFiReceiver = new WiFiReceiver(mHandler);
        getContext().registerReceiver(mWiFiReceiver,intentFilter);
    }

    private void refreshWifiList() {

        List<ScanResult> scanResults = mWifiAdmin.startScan();
        mWifiList.clear();
        mAddedList.clear();
        mAddedList = mDeviceDao.queryBuilder().list();
        for (ScanResult scanResult : scanResults) {
            boolean startFlag = scanResult.SSID.startsWith(Global.SSID_START);
            boolean endFlag = scanResult.SSID.endsWith(Global.SSID_END);
            boolean isAdded=false;
            for (Device device : mAddedList) {
                String ssid = device.getSsid();
                if (scanResult.SSID.equals(ssid)) {
                    isAdded=true;
                    break;
                }
            }
            if (startFlag&&endFlag&&!isAdded){
                mWifiList.add(scanResult);
            }
        }
        mAdapter.notifyDataSetChanged();
        if (mWifiList.size()==0) {
            mTipsView.setVisibility(View.VISIBLE);
        }else {
            mTipsView.setVisibility(View.GONE);
        }
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
            Toast.makeText(getContext(), R.string.tos_needPermission, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(mWiFiReceiver);//取消监听广播
    }

    @Override
    public void onItemClick(View view, int position) {
        onAddItemClick(view,position);
    }

    @Override
    public void onAddItemClick(View view, int position) {
        ScanResult scanResult = mWifiList.get(position);
        String substring = scanResult.SSID.substring(6);
        Device device=new Device(System.currentTimeMillis(),substring,scanResult.SSID,66);
        mAddedList.add(device);
        mWifiList.remove(position);
        mDeviceDao.insertOrReplace(device);
        mAdapter.notifyItemRemoved(position);
        Snackbar.make(mTipsView,R.string.addedToMyDevices,Snackbar.LENGTH_SHORT).show();
        ((DeviceManageActivity)getActivity()).addNewDevice(device);
        if (mWifiList.size()==0) {
            mTipsView.setVisibility(View.VISIBLE);
        }else {
            mTipsView.setVisibility(View.GONE);
        }
    }
}
