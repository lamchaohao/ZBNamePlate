package com.gzz100.nameplate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gzz100.nameplate.R;
import com.gzz100.nameplate.adapter.MessageAdapter;
import com.gzz100.nameplate.bean.SocketMessage;
import com.gzz100.nameplate.utils.connect.SendCmdUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.gzz100.nameplate.global.Global.PAUSE_OK;
import static com.gzz100.nameplate.global.Global.RESET_OK;
import static com.gzz100.nameplate.global.Global.RESUME_OK;
import static com.gzz100.nameplate.global.Global.SOCKET_ERRO;
import static com.gzz100.nameplate.global.Global.TEST_OK;
import static com.gzz100.nameplate.global.Global.View_Cool;
import static com.gzz100.nameplate.global.Global.WIFI_ERRO;


public class ConnectActivity extends BaseActivity {

    @BindView(R.id.rv_message)
    RecyclerView mRecyclerView;
    @BindView(R.id.socket_test)
    Button mSocketTest;
    @BindView(R.id.socket_rest)
    Button mSocketRest;
    @BindView(R.id.socket_pause)
    Button mSocketPause;
    @BindView(R.id.socket_resume)
    Button mSocketResume;

    private RecievMsgHandler msgHandler;
    private ArrayList<SocketMessage> mMessageList;
    private MessageAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private SendCmdUtil mSendCmdUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        msgHandler = new RecievMsgHandler();
        mMessageList = new ArrayList<>();
        mAdapter = new MessageAdapter(this, mMessageList);
        mSendCmdUtil = new SendCmdUtil(this,msgHandler);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

    }

    @OnClick({R.id.socket_test, R.id.socket_rest, R.id.socket_pause, R.id.socket_resume})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.socket_pause:
                mSendCmdUtil.sendCmd(SendCmdUtil.Cmd.Pause);
                SocketMessage pauseMSG = new SocketMessage(getString(R.string.pause),System.currentTimeMillis(),true,false);
                mMessageList.add(pauseMSG);
                break;
            case R.id.socket_rest:
                mSendCmdUtil.sendCmd(SendCmdUtil.Cmd.Reset);
                SocketMessage reSetMSG = new SocketMessage(getString(R.string.reset),System.currentTimeMillis(),true,false);
                mMessageList.add(reSetMSG);
                msgHandler.sendEmptyMessageDelayed(View_Cool,1000);
                mSocketRest.setEnabled(false);
                break;
            case R.id.socket_resume:
                mSendCmdUtil.sendCmd(SendCmdUtil.Cmd.Resume);
                SocketMessage resumeMSG = new SocketMessage(getString(R.string.resume),System.currentTimeMillis(),true,false);
                mMessageList.add(resumeMSG);
                break;
            case R.id.socket_test:
                mSendCmdUtil.sendCmd(SendCmdUtil.Cmd.Test);
                SocketMessage testMSG = new SocketMessage(getString(R.string.test),System.currentTimeMillis(),true,false);
                mMessageList.add(testMSG);
                break;
        }
        mAdapter.notifyItemInserted(mMessageList.size());
        mRecyclerView.smoothScrollToPosition(mMessageList.size());
    }

    class RecievMsgHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            SocketMessage smsg =null;
            switch (msg.what){
                case SOCKET_ERRO:
                    String errMsg=null;
                    String err = (String) msg.obj;
                    if (err!=null){
                        if (err.contains("ECONNREFUSED")) {
                            errMsg= getString(R.string.refuse_connect);//android.system.ErrnoException: connect failed: ECONNREFUSED (Connection refused)
                        }else {
                            errMsg= getString(R.string.tos_wifi_timeout);
                        }
                        SocketMessage error=new SocketMessage(errMsg,System.currentTimeMillis(),false,true);
                        mMessageList.add(error);
                        mAdapter.notifyItemInserted(mMessageList.size());
                    }

                    break;
                case WIFI_ERRO:
                    Toast.makeText(ConnectActivity.this,R.string.tos_wifi_switch,Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    break;
                case TEST_OK:
                    smsg=new SocketMessage(getString(R.string.testSuccess),System.currentTimeMillis(),true,true);
                    mMessageList.add(smsg);
                    mAdapter.notifyItemInserted(mMessageList.size());
                    break;
                case PAUSE_OK:
                    smsg=new SocketMessage(getString(R.string.pauseSuccess),System.currentTimeMillis(),true,true);
                    mMessageList.add(smsg);
                    mAdapter.notifyItemInserted(mMessageList.size());
                    break;
                case RESUME_OK:
                    smsg=new SocketMessage(getString(R.string.resumeSuccess),System.currentTimeMillis(),true,true);
                    mMessageList.add(smsg);
                    mAdapter.notifyItemInserted(mMessageList.size());
                    break;
                case RESET_OK:
                    smsg=new SocketMessage(getString(R.string.resetSuccess),System.currentTimeMillis(),true,true);
                    mMessageList.add(smsg);
                    mAdapter.notifyItemInserted(mMessageList.size());
                    break;
                case View_Cool:
                    mSocketRest.setEnabled(true);
                    break;
            }
            mAdapter.notifyDataSetChanged();
            mRecyclerView.smoothScrollToPosition(mMessageList.size());
        }
    }

    @Override
    public void onCreateCustomToolBar(Toolbar toolbar) {
        toolbar.setTitle(R.string.connect);
    }
}
