package com.gzz100.nameplate.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gzz100.nameplate.App;
import com.gzz100.nameplate.R;
import com.gzz100.nameplate.adapter.Listener;
import com.gzz100.nameplate.adapter.SendAdapter;
import com.gzz100.nameplate.bean.Account;
import com.gzz100.nameplate.bean.Device;
import com.gzz100.nameplate.bean.NamePlate;
import com.gzz100.nameplate.dao.AccountDao;
import com.gzz100.nameplate.dao.DeviceDao;
import com.gzz100.nameplate.global.Global;
import com.gzz100.nameplate.utils.connect.SendDataUtil;
import com.gzz100.nameplate.utils.genfile.DrawBitmapUtil;
import com.gzz100.nameplate.utils.genfile.GenFileUtil;
import com.gzz100.nameplate.utils.system.WifiAdmin;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.gzz100.nameplate.global.Global.PREPARE_FILE;
import static com.gzz100.nameplate.global.Global.RESEND;

public class MultiSendActivity extends BaseWifiActivity implements Listener.OnItemClickListener, Listener.OnPlayClickListener, View.OnClickListener {

    private static final int REQUEST_ACCOUNT_SELECT = 778;
    private static final int SORT_CODE = 665;
    @BindView(R.id.cv_send)
    CardView mCvSend;
    @BindView(R.id.send_rcv)
    RecyclerView mSendRcv;
    @BindView(R.id.fab_send_send)
    FloatingActionButton mFabSend;
    @BindView(R.id.fab_send_sort)
    FloatingActionButton mFabSort;
    @BindView(R.id.rv_multisend_tips)
    RelativeLayout mRlTips;

    private WifiAdmin mWifiAdmin;
    private List<NamePlate> mNamePlateList;
    private List<NamePlate> mOriginalNameplates;
    private long mOldTime;
    private List<ScanResult> mWifiList;
    private boolean mNeedSend;
    private SendAdapter mAdapter;
    private NamePlate mNamePlate;
    private int next = 0;
    private int retryTime;
    private boolean isSendAll;
    private int mPositionClick;
    private List<Account> mAccountList;
    private AccountDao mAccountDao;
    private int mPosIndex;
    private SendDataUtil sendDataUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_send);
        ButterKnife.bind(this);
        loadData();
        initView();
        onScanAvailable(null);
    }


    private void loadData() {
        sendDataUtil = new SendDataUtil(this, mHandler);
        mWifiAdmin = new WifiAdmin(this);
        mWifiList = new ArrayList<>();
        mNamePlateList = new ArrayList<>();
        mOriginalNameplates = new ArrayList<>();
        mAccountDao = ((App) getApplication()).getDaoSession().getAccountDao();
        DeviceDao deviceDao = ((App) getApplication()).getDaoSession().getDeviceDao();
        mAccountList = mAccountDao.queryBuilder().list();
        List<Device> devices = deviceDao.queryBuilder().list();
        if (devices.size()==0) {
            mRlTips.setVisibility(View.VISIBLE);
        }else {
            mRlTips.setVisibility(View.GONE);
        }
        int size = mAccountList.size();
        for (int i = 0; i < devices.size() - size; i++) {
            Account account = new Account();
            account.setAccountName("");
            mAccountList.add(account);
        }

        int i = 0;
        for (Device device : devices) {
            Account account = mAccountList.get(i);
            NamePlate np = new NamePlate(account, device, "", "");
            mNamePlateList.add(np);
            i++;
        }
        mOriginalNameplates.addAll(mNamePlateList);
        List<ScanResult> scanResults = mWifiAdmin.mWifiManager.getScanResults();
        for (ScanResult scanResult : scanResults) {
            boolean startFlag = scanResult.SSID.startsWith(Global.SSID_START);
            boolean endFlag = scanResult.SSID.endsWith(Global.SSID_END);
            if (startFlag && endFlag) {
                mWifiList.add(scanResult);
            }
        }
    }

    private void initView() {
        mCvSend.setVisibility(View.GONE);
        mAdapter = new SendAdapter(this, mNamePlateList);
        mSendRcv.setLayoutManager(new LinearLayoutManager(this));
        mSendRcv.setAdapter(mAdapter);
        mAdapter.setItemOnClickListener(this);
        mAdapter.setOnPlayClickListener(this);

    }

    @Override
    protected void onUpdateNetworkInfo(NetworkInfo networkInfo) {
        if (networkInfo != null&&mNamePlate!=null&&mAdapter!=null) {
            if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                mNamePlate.setState(getString(R.string.connected));
                mAdapter.notifyItemChanged(mPosIndex);
                if (mNeedSend) {
                    String ssid = mWifiAdmin.getWifiInfo().getSSID();
                    Log.i("updateNetwork","ssid=== "+ssid);
                    Message msg = mHandler.obtainMessage();
                    msg.what=PREPARE_FILE;
                    msg.obj=ssid;
                    mHandler.sendMessageDelayed(msg, 3000);//3秒后开始发送;
                    mNeedSend = false;
                }
            } else if (networkInfo.getState().equals(NetworkInfo.State.CONNECTING)) {
                mNamePlate.setState(getString(R.string.connecting));
                mAdapter.notifyItemChanged(mPosIndex);
            } else if (networkInfo.getState().equals(NetworkInfo.State.DISCONNECTING)) {
                mNamePlate.setState(getString(R.string.disconnecting));
                mAdapter.notifyItemChanged(mPosIndex);
            } else if (networkInfo.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                mNamePlate.setState(getString(R.string.disconnected));
                mAdapter.notifyItemChanged(mPosIndex);
            }
        }
    }

    @Override
    protected void onWifiEnable(Message msg) {

    }

    @Override
    protected void onWifiDisable(Message msg) {

    }

    @Override
    protected void onScanAvailable(Message msg) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mOldTime < 2000) {
            return;
        }
        List<ScanResult> scanList=new ArrayList<>();
        List<ScanResult> wifiList = mWifiAdmin.mWifiManager.getScanResults();
        for (ScanResult scanResult : wifiList) {
            boolean startFlag = scanResult.SSID.startsWith(Global.SSID_START);
            boolean endFlag = scanResult.SSID.endsWith(Global.SSID_END);
            if (startFlag&&endFlag){
                scanList.add(scanResult);
            }
        }
        for (NamePlate namePlate : mNamePlateList) {
            namePlate.getDevice().setOnline(false);
        }

        for (ScanResult wifi : scanList) {
            for (NamePlate namePlate : mNamePlateList) {
                if (namePlate.getDevice().getSsid().equals(wifi.SSID)) {
                    namePlate.getDevice().setOnline(true);
                    break;
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onConnectNoRespone(Message msg) {
        onSendFail(true);
    }

    @Override
    protected void onWiFiErro(Message msg) {
        onSendFail(true);
    }

    @Override   //生成文件
    protected void onPrepareFile(Message msg) {
        DrawBitmapUtil drawBitmapUtil = new DrawBitmapUtil(this,mNamePlate.getAccount());
        Bitmap bitmap = drawBitmapUtil.drawBitmap();
        String ssid = (String) msg.obj;
        GenFileUtil genFileUtil = new GenFileUtil(this, bitmap, mHandler,ssid);
        genFileUtil.startGenFile();
        mNamePlate.setState(getString(R.string.prepareFile));
        mAdapter.notifyItemChanged(mPosIndex);
    }

    /**
     * 发送进度
     * @param msg
     */
    @Override
    protected void onUpdateProgress(Message msg) {
        int progress = msg.arg1;
        if (progress == 100) {
            mNamePlate.setState( getString(R.string.sent_success));
            mNamePlate.setFocus(false);
            mNamePlate.setProgress(progress);
            mAdapter.notifyItemChanged(mPosIndex);
            onSendSuccess();
        } else{
            mNamePlate.setState(getString(R.string.sending));
            mNamePlate.setProgress(progress);
            mAdapter.notifyItemChanged(mPosIndex);
        }
    }

    /**
     * 文件生成成功
     * @param msg
     */
    @Override   //开始发送
    protected void onGenFileDone(Message msg) {

        if (!mNamePlate.getDevice().getIsOnline()){
            retryTime=5;
            Snackbar.make(mSendRcv,mNamePlate.getDevice().getDeviceName()+getString(R.string.offLine),Snackbar.LENGTH_SHORT).show();
            onSendFail(false);
            return;
        }
        String filePath = (String) msg.obj;
        Log.i("genFileDone","filepath=== "+filePath);
        sendDataUtil.setDIY_Name(true);
        sendDataUtil.setmFilePath(filePath);
        sendDataUtil.send();
        mNamePlate.setState(getString(R.string.startSend));
        mAdapter.notifyItemChanged(mPosIndex);
    }

    @Override
    protected void onResend() {
        sendDataUtil.send();
        mNamePlate.setState(getString(R.string.startSend));
        mAdapter.notifyItemChanged(mPosIndex);
    }

    /**
     * 群发
     * @param position 对应到哪个位置
     */
    private void sendByStep(int position) {
        isSendAll = true;
        boolean isOnline = mNamePlateList.get(position).getDevice().getIsOnline();
        if (mNamePlate!=null){
            mNamePlate.setFocus(false);
            mAdapter.notifyItemChanged(mPosIndex);
        }
        mNamePlate = mNamePlateList.get(position);
        mPosIndex=position;
        if (!isOnline) {
            Snackbar.make(mSendRcv,mNamePlateList.get(position).getDevice().getDeviceName()+getString(R.string.offLine),Snackbar.LENGTH_SHORT).show();
            retryTime=5;
            onSendFail(false);
            return;
        }

        mNamePlate.setFocus(true);
        mNamePlate.setProgress(0);
        connectWifi(position);//连接至该桌牌
        mAdapter.notifyItemChanged(position);
        mNeedSend = true;
        if (mWifiAdmin.getWifiInfo().getSSID().equals("\"" + mNamePlate.getDevice().getSsid() + "\"")) {
            mHandler.sendEmptyMessageDelayed(PREPARE_FILE, 1000);//1秒后开始发送;
        }
    }


    /**
     * 发送成功回调
     */
    private void onSendSuccess() {

        retryTime = 0;
        Snackbar.make(mSendRcv, mNamePlate.getAccount().getAccountName() + getString(R.string.sent), Snackbar.LENGTH_SHORT).show();
        //一键发送的情况下
        if (isSendAll) {
            next++;
            if (next < mOriginalNameplates.size() && isSendAll) {
                sendByStep(next);
            } else if (next == mOriginalNameplates.size()) {
                Toast.makeText(this, R.string.tos_sendAll_done, Toast.LENGTH_LONG).show();
                next = 0;
            }
        }
        mSendRcv.smoothScrollToPosition(mPosIndex);
    }


    /**
     * 发送失败回调
     * @param isOnline 是否在线
     *                 如果在线会执行重试
     *                 不在线则会直接显示发送失败
     */
    private void onSendFail(boolean isOnline) {
        //发送失败,重试几次
        retryTime++;
        if (retryTime < 5) {
            switch (retryTime) {
                case 1:
                    Snackbar.make(mSendRcv, R.string.tos_retry10, Snackbar.LENGTH_SHORT).show();
                    mNamePlate.setState(getString(R.string.start_try));
                    mAdapter.notifyItemChanged(mPosIndex);
                    connectWifi(mPosIndex);
                    mHandler.sendEmptyMessageDelayed(RESEND, 10000);
                    break;
                default:
                    Snackbar.make(mSendRcv, R.string.tos_retry5, Snackbar.LENGTH_SHORT).show();
                    mNamePlate.setState(retryTime + getString(R.string.tos_retrytime));
                    mAdapter.notifyItemChanged(mPosIndex);
                    connectWifi(mPosIndex);
                    mHandler.sendEmptyMessageDelayed(RESEND, 5000);
                    break;
            }
        } else {//重试次数达到上限,则发送失败,进行下一个发送
            if (mNamePlate!=null){
                if (isOnline) {
                    mNamePlate.setState(getString(R.string.sent_fail));
                }else{
                    mNamePlate.setState(getString(R.string.offLine));
                }
                mNamePlate.setFocus(false);
                mAdapter.notifyItemChanged(mPosIndex);
            }
            next++;
            if (next < mOriginalNameplates.size() && isSendAll) {
                sendByStep(next);
            } else if (next == mOriginalNameplates.size()) {
                Toast.makeText(this, R.string.tos_sendAll_done, Toast.LENGTH_LONG).show();
                next = 0;
            }
        }
        mSendRcv.smoothScrollToPosition(mPosIndex);//若有多个屏幕,需滑动才能查看到该铭牌,则滑动到指定item中
    }

    /**
     * 点击listview条目
     * @param view
     * @param position
     */
    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(this, SelectAccountActivity.class);
        startActivityForResult(intent, REQUEST_ACCOUNT_SELECT);
        mPositionClick = position;
    }

    /**
     * recycleview条目中的发送按钮
     * @param view
     * @param position position
     */
    @Override
    public void onPlayClick(View view, int position) {
        boolean isOnline = mNamePlateList.get(position).getDevice().getIsOnline();
        if (mNamePlate!=null){
            mNamePlate.setFocus(false);
            mAdapter.notifyItemChanged(mPosIndex);
        }
        if (!isOnline) {
            Snackbar.make(mSendRcv,mNamePlateList.get(position).getDevice().getDeviceName()+getString(R.string.offLine),Snackbar.LENGTH_SHORT).show();
            return;
        }
        isSendAll = false;//取消发送全部功能
        mNamePlate = mNamePlateList.get(position);
        mPosIndex=position;
        mNamePlate.setFocus(true);
        mNamePlate.setProgress(0);
        mAdapter.notifyItemChanged(position);
        connectWifi(position);
        mNeedSend = true;
        if (mWifiAdmin.getWifiInfo().getSSID().equals("\"" + mNamePlate.getDevice().getSsid() + "\"")) {
            mHandler.sendEmptyMessageDelayed(PREPARE_FILE, 1000);//1秒后开始发送;
        }
    }

    @OnClick({R.id.fab_send_send, R.id.fab_send_sort})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_send_send:
                if (mNamePlateList.size()>0){
                    mPosIndex=0;
                    sendByStep(0);
                    isSendAll = true;
                }else {
                    Snackbar.make(mSendRcv,R.string.tos_addFirst,Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.fab_send_sort:
                Intent intent = new Intent(this, SortNameActivity.class);
                startActivityForResult(intent,SORT_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_ACCOUNT_SELECT) {
            long accountId = data.getLongExtra(Global.EX_ACCOUNT_ID, -1);
            for (Account account : mAccountList) {
                if (account.getId() == accountId) {
                    mNamePlateList.get(mPositionClick).setAccount(account);
                    mNamePlateList.get(mPositionClick).setProgress(0);
                    mNamePlateList.get(mPositionClick).setState("");
                    mAdapter.notifyItemChanged(mPositionClick);
                    break;
                }
            }
        }else if (resultCode == RESULT_OK && requestCode == SORT_CODE){
            List<Account> list = mAccountDao.queryBuilder().where(AccountDao.Properties.SortNumber.notEq(1000)).list();
            mNamePlateList.clear();
            mNamePlateList.addAll(mOriginalNameplates);
            for (Account account : list) {
                mNamePlateList.get(account.getSortNumber()).setAccount(account);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private void connectWifi(int position) {
        //检查是否已连接过
        WifiConfiguration exsitsConfig = mWifiAdmin.isExsits(mOriginalNameplates.get(position).getDevice().getSsid());//这里的SSID 打印出来没有双引号包括
        if (exsitsConfig != null) {
            // 1.已连接过，直接使用该配置进行连接
            mWifiAdmin.setMaxPriority(exsitsConfig);//已经连接过的，需要设置优先级为最大的才能连上
            mWifiAdmin.connectWifi(exsitsConfig.networkId);
        } else {
            String ssid = mOriginalNameplates.get(position).getDevice().getSsid();
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
    public void onCreateCustomToolBar(Toolbar toolbar) {
        toolbar.setTitle(R.string.send);
    }
}
