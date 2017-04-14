package com.gzz100.nameplate.activity;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.gzz100.nameplate.App;
import com.gzz100.nameplate.R;
import com.gzz100.nameplate.adapter.BrightnessAdapter;
import com.gzz100.nameplate.adapter.Listener;
import com.gzz100.nameplate.bean.Device;
import com.gzz100.nameplate.dao.DeviceDao;
import com.gzz100.nameplate.global.Global;
import com.gzz100.nameplate.utils.connect.SendBrightnessUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.gzz100.nameplate.global.Global.GENFILE_DONE;


public class BrightnessActivity extends BaseWifiActivity implements Listener.OnProgressChangedListener, Listener.OnPlayClickListener {

    @BindView(R.id.rl_brightness_tips)
    RelativeLayout mRlBrightnessTips;
    private List<Device> mDeviceList;
    private BrightnessAdapter mAdapter;
    private Device mDevice;
    private boolean mNeedSend;
    private int mPosition;
    private DeviceDao mDeviceDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brightness);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rcv_brightness);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDeviceDao = ((App) getApplication()).getDaoSession().getDeviceDao();
        mDeviceList = mDeviceDao.queryBuilder().list();
        if (mDeviceList.size()==0) {
            mRlBrightnessTips.setVisibility(View.VISIBLE);
        }else {
            mRlBrightnessTips.setVisibility(View.GONE);
        }
        mAdapter = new BrightnessAdapter(this, mDeviceList);
        onScanAvailable(null);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setProgressChangedListener(this);
        mAdapter.setOnPlayClickListener(this);
    }

    @Override
    protected void onUpdateNetworkInfo(NetworkInfo networkInfo) {
        if (networkInfo != null && mDevice != null) {
            if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                mDevice.setState(getString(R.string.connected));
                if (mNeedSend) {
                    mDevice.setState(getString(R.string.sending));
                    mHandler.sendEmptyMessageDelayed(GENFILE_DONE, 3000);//3秒后开始发送;
                    mNeedSend = false;
                }
            } else if (networkInfo.getState().equals(NetworkInfo.State.CONNECTING)) {
                mDevice.setState(getString(R.string.connecting));
            } else if (networkInfo.getState().equals(NetworkInfo.State.DISCONNECTING)) {
                mDevice.setState(getString(R.string.disconnecting));
            } else if (networkInfo.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                mDevice.setState(getString(R.string.disconnected));
            }
            mAdapter.notifyItemChanged(mPosition);
        }
    }

    @Override
    protected void onWifiEnable(Message msg) {

    }

    @Override
    protected void onSendDone() {
        Toast.makeText(this, mDevice.getDeviceName() + getString(R.string.sent), Toast.LENGTH_SHORT).show();
        mDevice.setState(getString(R.string.sent));
        mAdapter.notifyItemChanged(mPosition);
    }

    @Override
    protected void onGenFileDone(Message msg) {

        SendBrightnessUtil brightnessUtil = new SendBrightnessUtil(this, mHandler, mDevice.getBrightness());
        brightnessUtil.startSendData();
    }

    @Override
    protected void onWifiDisable(Message msg) {

    }

    @Override
    protected void onScanAvailable(Message msg) {
        List<ScanResult> scanList = new ArrayList<>();
        for (ScanResult scanResult : mWifiAdmin.startScan()) {
            boolean startFlag = scanResult.SSID.startsWith(Global.SSID_START);
            boolean endFlag = scanResult.SSID.endsWith(Global.SSID_END);
            if (startFlag && endFlag) {
                scanList.add(scanResult);
            }
        }
        for (Device device : mDeviceList) {
            device.setOnline(false);
        }

        for (ScanResult wifi : scanList) {
            for (Device device : mDeviceList) {
                if (device.getSsid().equals(wifi.SSID)) {
                    device.setOnline(true);
                    break;
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPauseFail() {
        Toast.makeText(this, mDevice.getDeviceName() + getString(R.string.pauseFail), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onConnectNoRespone(Message msg) {
        Toast.makeText(this, mDevice.getDeviceName() + getString(R.string.pauseFailAndRetry), Toast.LENGTH_LONG).show();
        mDevice.setState(getString(R.string.retry));
        mAdapter.notifyItemChanged(mPosition);
    }

    @Override
    protected void onWiFiErro(Message msg) {
        Toast.makeText(this, mDevice.getDeviceName() + getString(R.string.offLine), Toast.LENGTH_LONG).show();
        mDevice.setState(getString(R.string.offLine));
        mAdapter.notifyItemChanged(mPosition);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int position, int progress) {
        int brightness = 33;
        if (progress <= 33) {
            brightness = 33;
        } else if (progress > 33 && progress <= 66) {
            brightness = 66;
        } else if (progress > 66 && progress <= 100) {
            brightness = 100;
        }
        mDeviceList.get(position).setBrightness(brightness);
    }

    @Override
    public void onPlayClick(View view, int position) {
        mDevice = mDeviceList.get(position);
        mPosition = position;
        mDevice.setState(getString(R.string.readySend));

        if (mWifiAdmin.getWifiInfo().getSSID().equals("\"" + mDevice.getSsid() + "\"")) {
            mDevice.setState(getString(R.string.sending));
            mHandler.sendEmptyMessageDelayed(GENFILE_DONE, 1000);//1秒后开始发送;
        }
        mAdapter.notifyItemChanged(mPosition);
        mNeedSend = true;
        connectWifi(position);
    }

    private void connectWifi(int position) {

        mWifiList = mWifiAdmin.startScan();
        //检查是否已连接过
        WifiConfiguration exsitsConfig = mWifiAdmin.isExsits(mDeviceList.get(position).getSsid());//这里的SSID 打印出来没有双引号包括
        if (exsitsConfig != null) {
            // 1.已连接过，直接使用该配置进行连接
            mWifiAdmin.setMaxPriority(exsitsConfig);//已经连接过的，需要设置优先级为最大的才能连上
            mWifiAdmin.connectWifi(exsitsConfig.networkId);
        } else {
            String ssid = mDeviceList.get(position).getSsid();
            for (ScanResult scanResult : mWifiList) {
                if (scanResult.SSID.equals(ssid)) {
                    WifiConfiguration wifiInfo2 = mWifiAdmin.createWifiInfo2(scanResult, Global.CARD_PASSWORD);
                    mWifiAdmin.addNetWork(wifiInfo2);
                    break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        mDeviceDao.insertOrReplaceInTx(mDeviceList);
        super.onBackPressed();
    }

    @OnClick(R.id.rl_brightness_tips)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_brightness_tips:
                startActivity(new Intent(this, DeviceManageActivity.class));
                break;
        }
    }

    @Override
    public void onCreateCustomToolBar(Toolbar toolbar) {
        toolbar.setTitle(R.string.brightness);
    }
}
