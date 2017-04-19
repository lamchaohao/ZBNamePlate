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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
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

import static com.gzz100.nameplate.global.Global.GENFILE_DONE;
import static com.gzz100.nameplate.global.Global.PREPARE_FILE;

public class MultiSendActivity extends BaseWifiActivity implements Listener.OnItemClickListener, Listener.OnPlayClickListener, View.OnClickListener {

    private static final int REQUEST_ACCOUNT_SELECT = 778;
    private static final int SORT_CODE = 665;
    @BindView(R.id.tv_sendName)
    TextView mTvSendName;
    @BindView(R.id.iv_sendWifi)
    ImageView mIvSendWifi;
    @BindView(R.id.tv_sendDevice)
    TextView mTvSendDevice;
    @BindView(R.id.tv_sendWifi)
    TextView mTvSendWifi;
    @BindView(R.id.ll_sendWifi)
    LinearLayout mLlSendWifi;
    @BindView(R.id.tv_sendTip)
    TextView mTvSendTip;
    @BindView(R.id.ll_sendItem)
    LinearLayout mLlSendItem;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_send);
        ButterKnife.bind(this);
        loadData();
        initView();
    }


    private void loadData() {
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
                    mHandler.sendEmptyMessageDelayed(PREPARE_FILE, 3000);//3秒后开始发送;
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
        GenFileUtil genFileUtil = new GenFileUtil(this, bitmap, mHandler);
        genFileUtil.startGenFile();
        mNamePlate.setState(getString(R.string.prepareFile));
        mAdapter.notifyItemChanged(mPosIndex);
    }

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

    @Override   //开始发送
    protected void onGenFileDone(Message msg) {

        if (!mNamePlate.getDevice().getIsOnline()){
            retryTime=4;
            Snackbar.make(mSendRcv,mNamePlate.getDevice().getDeviceName()+getString(R.string.offLine),Snackbar.LENGTH_SHORT).show();
            onSendFail(false);
            return;
        }

        SendDataUtil sendDataUtil = new SendDataUtil(this, mHandler);
        sendDataUtil.send();
        mNamePlate.setState(getString(R.string.startSend));
        mAdapter.notifyItemChanged(mPosIndex);
    }



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
            retryTime=4;
            onSendFail(false);
            return;
        }

        mNamePlate.setFocus(true);
        mNamePlate.setProgress(0);
        connectWifi(position);
        mAdapter.notifyItemChanged(position);
        mNeedSend = true;
        if (mWifiAdmin.getWifiInfo().getSSID().equals("\"" + mNamePlate.getDevice().getSsid() + "\"")) {
            mHandler.sendEmptyMessageDelayed(PREPARE_FILE, 1000);//1秒后开始发送;
        }
    }


    //发送成功
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

    //发送失败
    private void onSendFail(boolean isOnline) {
        //发送失败,重试几次
        retryTime++;
        if (retryTime < 4) {
            switch (retryTime) {
                case 1:
                    Snackbar.make(mSendRcv, R.string.tos_retry5, Snackbar.LENGTH_SHORT).show();
                    mNamePlate.setState(getString(R.string.start_try));
                    mAdapter.notifyItemChanged(mPosIndex);
                    mHandler.sendEmptyMessageDelayed(GENFILE_DONE, 5000);
                    break;
                default:
                    Snackbar.make(mSendRcv, R.string.tos_retry3, Snackbar.LENGTH_SHORT).show();
                    mNamePlate.setState(retryTime + getString(R.string.tos_retrytime));
                    mAdapter.notifyItemChanged(mPosIndex);
                    mHandler.sendEmptyMessageDelayed(GENFILE_DONE, 3000);
                    break;
            }
        } else {
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
        mSendRcv.smoothScrollToPosition(mPosIndex);
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(this, SelectAccountActivity.class);
        startActivityForResult(intent, REQUEST_ACCOUNT_SELECT);
        mPositionClick = position;
    }

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
        isSendAll = false;
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
