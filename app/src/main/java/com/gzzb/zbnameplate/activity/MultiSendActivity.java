package com.gzzb.zbnameplate.activity;

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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gzzb.zbnameplate.App;
import com.gzzb.zbnameplate.R;
import com.gzzb.zbnameplate.adapter.Listener;
import com.gzzb.zbnameplate.adapter.SendAdapter;
import com.gzzb.zbnameplate.bean.Account;
import com.gzzb.zbnameplate.bean.Device;
import com.gzzb.zbnameplate.bean.NamePlate;
import com.gzzb.zbnameplate.dao.AccountDao;
import com.gzzb.zbnameplate.dao.DeviceDao;
import com.gzzb.zbnameplate.global.Global;
import com.gzzb.zbnameplate.utils.connect.SendDataUtil;
import com.gzzb.zbnameplate.utils.genfile.DrawBitmapUtil;
import com.gzzb.zbnameplate.utils.genfile.GenFileUtil;
import com.gzzb.zbnameplate.utils.system.WifiAdmin;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.gzzb.zbnameplate.global.Global.GENFILE_DONE;
import static com.gzzb.zbnameplate.global.Global.PREPARE_FILE;

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
    private Account mAccount;
    private Device mDevice;
    private int next = 0;
    private int retryTime;
    private boolean isSendAll;
    private int mPositionClick;
    private List<Account> mAccountList;
    private AccountDao mAccountDao;

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
        List<ScanResult> scanResults = mWifiAdmin.startScan();
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
        onScanAvailable(null);
        mSendRcv.setAdapter(mAdapter);
        mAdapter.setItemOnClickListener(this);
        mAdapter.setOnPlayClickListener(this);

    }

    @Override
    protected void onUpdateNetworkInfo(NetworkInfo networkInfo) {
        if (networkInfo != null) {
            if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                mTvSendTip.setText("已连接");
                if (mNeedSend) {
                    mHandler.sendEmptyMessageDelayed(PREPARE_FILE, 3000);//3秒后开始发送;
                    mNeedSend = false;
                }
            } else if (networkInfo.getState().equals(NetworkInfo.State.CONNECTING)) {
                mTvSendTip.setVisibility(View.VISIBLE);
                mTvSendTip.setText("正在连接");
            } else if (networkInfo.getState().equals(NetworkInfo.State.DISCONNECTING)) {
                mTvSendTip.setText("正在断开");
            } else if (networkInfo.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                mTvSendTip.setText("已断开");
            }
        }
    }

    @Override
    protected void onWifiEnable(Message msg) {

    }

    @Override
    protected void onWifiDisable(Message msg) {
        refreshWifiAndState();
    }

    @Override
    protected void onScanAvailable(Message msg) {
        List<ScanResult> scanList=new ArrayList<>();
        List<ScanResult> wifiList = mWifiAdmin.startScan();
        for (ScanResult scanResult : wifiList) {
            boolean startFlag = scanResult.SSID.startsWith("HC-LED[");
            boolean endFlag = scanResult.SSID.endsWith("]");
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
        onSendFail();
    }

    @Override
    protected void onWiFiErro(Message msg) {
//        retryTime = 4;
        onSendFail();
//        Toast.makeText(MultiSendActivity.this, "铭牌不在线,请启动铭牌", Toast.LENGTH_LONG).show();
    }

    @Override   //生成文件
    protected void onPrepareFile(Message msg) {
        DrawBitmapUtil drawBitmapUtil = new DrawBitmapUtil(this,mAccount);
        Bitmap bitmap = drawBitmapUtil.drawBitmap();
        GenFileUtil genFileUtil = new GenFileUtil(this, bitmap, mHandler);
        genFileUtil.startGenFile();
        mTvSendTip.setText("准备文件");
    }

    @Override
    protected void onUpdateProgress(Message msg) {
        int progress = msg.arg1;
        if (progress == 100) {
            onSendSuccess();
        } else
            mTvSendTip.setText("正在发送" + progress + "%");
    }

    @Override   //开始发送
    protected void onGenFileDone(Message msg) {

        if (!mDevice.getIsOnline()){
            retryTime=4;
            Snackbar.make(mSendRcv,mDevice.getDeviceName()+"不在线",Snackbar.LENGTH_SHORT).show();
            onSendFail();
            return;
        }

        SendDataUtil sendDataUtil = new SendDataUtil(this, mHandler);
        sendDataUtil.send();
        mTvSendTip.setText("开始发送");
    }


    private void refreshWifiAndState() {
        long currentTime = System.currentTimeMillis();
        if (!(currentTime - mOldTime > 5000)) {
            return;
        }
        mOldTime = currentTime;

        List<ScanResult> scanResults = mWifiAdmin.startScan();
        mWifiList.clear();
        for (ScanResult scanResult : scanResults) {
            boolean startFlag = scanResult.SSID.startsWith(Global.SSID_START);
            boolean endFlag = scanResult.SSID.endsWith(Global.SSID_END);
            if (startFlag && endFlag) {
                mWifiList.add(scanResult);
            }
        }
    }


    private void sendByStep(int position) {
        //把之前的还原
        mCvSend.setVisibility(View.VISIBLE);
        if (mTvSendTip.getVisibility() != View.VISIBLE) {
            mTvSendTip.setVisibility(View.VISIBLE);
        }
        mDevice = mNamePlateList.get(position).getDevice();
        mAccount = mNamePlateList.get(position).getAccount();

        connectWifi(position);
        mNamePlateList.remove(position);
        mAdapter.notifyItemRemoved(position);

        mTvSendName.setText(mAccount.getAccountName());
        mTvSendDevice.setText(mDevice.getDeviceName());
        mTvSendWifi.setText(mDevice.getSsid());
        mNeedSend = true;
        if (mWifiAdmin.getWifiInfo().getSSID().equals("\"" + mDevice.getSsid() + "\"")) {
            mHandler.sendEmptyMessageDelayed(PREPARE_FILE, 1000);//1秒后开始发送;
        }
    }


    //发送成功
    private void onSendSuccess() {
        retryTime = 0;
        mTvSendTip.setText("已发送");
        Snackbar.make(mSendRcv, mAccount.getAccountName() + "已发送", Snackbar.LENGTH_SHORT).show();
        NamePlate np = new NamePlate(mAccount, mDevice, "发送成功", "");
        mNamePlateList.add(np);
        mAdapter.notifyItemInserted(mNamePlateList.size());
        //一键发送的情况下
        if (isSendAll) {
            next++;
            if (next < mOriginalNameplates.size())
                sendByStep(0);
            else if (next == mOriginalNameplates.size()) {
                Toast.makeText(this, "全部发送完毕", Toast.LENGTH_LONG).show();
                next = 0;
            }
        }
        mSendRcv.smoothScrollToPosition(mNamePlateList.size());
    }

    //发送失败
    private void onSendFail() {
        // TODO: 2017/3/28 发送失败的原因,是因为在主界面的时候一连接到WiFi的时候自动发送test指令,在那时候就已经创立了一个tcp,如果再次又发送的话就起了冲突.所以造成发送失败
        //发送失败,重试几次
        retryTime++;
        if (retryTime < 4) {
            switch (retryTime) {
                case 1:
                    Snackbar.make(mSendRcv, "WiFi异常,将于5秒后自动重试", Snackbar.LENGTH_SHORT).show();
                    mTvSendTip.setText("准备重试");
                    mHandler.sendEmptyMessageDelayed(GENFILE_DONE, 5000);
                    break;
                default:
                    Snackbar.make(mSendRcv, "WiFi异常,将于3秒后自动重试", Snackbar.LENGTH_SHORT).show();
                    mTvSendTip.setText(retryTime + "次重试");
                    mHandler.sendEmptyMessageDelayed(GENFILE_DONE, 3000);
                    break;
            }
        } else {
            NamePlate np = new NamePlate(mAccount, mDevice, "发送失败", "");
            mNamePlateList.add(np);
            mAdapter.notifyItemInserted(mNamePlateList.size());
            next++;
            if (next < mOriginalNameplates.size() && isSendAll) {
                sendByStep(0);
            }
        }
        mSendRcv.smoothScrollToPosition(mNamePlateList.size());
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
        if (!isOnline) {
            Snackbar.make(mSendRcv,mNamePlateList.get(position).getDevice().getDeviceName()+"不在线",Snackbar.LENGTH_SHORT).show();
            return;
        }
        isSendAll = false;
        mCvSend.setVisibility(View.VISIBLE);
        if (mTvSendTip.getVisibility() != View.VISIBLE) {
            mTvSendTip.setVisibility(View.VISIBLE);
        }
        mDevice = mNamePlateList.get(position).getDevice();
        mAccount = mNamePlateList.get(position).getAccount();

        connectWifi(position);
        mNamePlateList.remove(position);
        mAdapter.notifyItemRemoved(position);
        mTvSendName.setText(mAccount.getAccountName());
        mTvSendDevice.setText(mDevice.getDeviceName());
        mTvSendWifi.setText(mDevice.getSsid());
        mNeedSend = true;
        if (mWifiAdmin.getWifiInfo().getSSID().equals("\"" + mDevice.getSsid() + "\"")) {
            mHandler.sendEmptyMessageDelayed(PREPARE_FILE, 1000);//1秒后开始发送;
        }
    }

    @OnClick({R.id.fab_send_send, R.id.fab_send_sort})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_send_send:
                if (mNamePlateList.size()>0){
                    sendByStep(0);
                    isSendAll = true;
                }else {
                    Snackbar.make(mSendRcv,"请先添加桌牌设备",Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.fab_send_sort:
                Intent intent = new Intent(this, SortActivity.class);
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
                    mAdapter.notifyItemChanged(mPositionClick);
                    break;
                }
            }
        }else if (resultCode == RESULT_OK && requestCode == SORT_CODE){
            List<Account> list = mAccountDao.queryBuilder().where(AccountDao.Properties.SortNumber.notEq(1000)).list();
            for (Account account : list) {
                mNamePlateList.get(account.getSortNumber()).setAccount(account);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private void connectWifi(int position) {

        //检查是否已连接过
        WifiConfiguration exsitsConfig = mWifiAdmin.isExsits(mNamePlateList.get(position).getDevice().getSsid());//这里的SSID 打印出来没有双引号包括
        if (exsitsConfig != null) {
            // 1.已连接过，直接使用该配置进行连接
            mWifiAdmin.setMaxPriority(exsitsConfig);//已经连接过的，需要设置优先级为最大的才能连上
            mWifiAdmin.connectWifi(exsitsConfig.networkId);
        } else {
            String ssid = mNamePlateList.get(position).getDevice().getSsid();
            for (ScanResult scanResult : mWifiList) {
                if (scanResult.SSID.equals(ssid)) {
                    WifiConfiguration wifiInfo2 = mWifiAdmin.createWifiInfo2(scanResult, Global.CARD_PASSWORD);
                    mWifiAdmin.addNetWork(wifiInfo2);
                    break;
                }
            }
        }
    }

}
