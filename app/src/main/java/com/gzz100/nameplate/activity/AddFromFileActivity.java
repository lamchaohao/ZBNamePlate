package com.gzz100.nameplate.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gzz100.nameplate.App;
import com.gzz100.nameplate.R;
import com.gzz100.nameplate.adapter.NameAdapter;
import com.gzz100.nameplate.bean.Account;
import com.gzz100.nameplate.dao.AccountDao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.gzz100.nameplate.global.Global.FL_QQFILE;
import static com.gzz100.nameplate.global.Global.FL_WECHAT;

public class AddFromFileActivity extends BaseActivity {

    private static final int SELECT_FILE = 220;
    @BindView(R.id.tv_add_fromQQ)
    TextView mTvFromQQ;
    @BindView(R.id.tv_add_fromWechat)
    TextView mTvFromWechat;
    @BindView(R.id.tv_add_fromOther)
    TextView mTvFromOther;
    @BindView(R.id.tv_add_selectResult)
    TextView mTvSelectResult;
    @BindView(R.id.tv_add_decode)
    TextView mTvDecode;
    @BindView(R.id.rcv_showName)
    RecyclerView mRcvShowName;
    @BindView(R.id.fab_add_send)
    FloatingActionButton mFabAddSend;
    private List<String> mNameList;
    private NameAdapter mAdapter;
    private AccountDao mAccountDao;
    private Charset[] mCharsets={Charset.forName("GBK"),Charset.forName("Unicode"),Charset.forName("ISO-8859-1"),Charset.forName("UTF-8")};
    private int pressCount;
    private String mFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_from_file);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mNameList = new ArrayList<>();
        mRcvShowName.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new NameAdapter(mNameList,this);
        mRcvShowName.setAdapter(mAdapter);
        mAccountDao = ((App) getApplication()).getDaoSession().getAccountDao();
    }

    @OnClick({R.id.tv_add_fromQQ, R.id.tv_add_fromWechat, R.id.tv_add_fromOther, R.id.fab_add_send,R.id.tv_add_decode})
    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.tv_add_fromQQ:
                File parentFile = new File(Environment.getExternalStorageDirectory() + FL_QQFILE);
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setDataAndType(Uri.fromFile(parentFile), "file/*");
                startActivityForResult(intent, SELECT_FILE);
                break;
            case R.id.tv_add_fromWechat:
                File wechatFile = new File(Environment.getExternalStorageDirectory() + FL_WECHAT);
                if (!wechatFile.exists()) {
                    wechatFile.mkdirs();
                }
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setDataAndType(Uri.fromFile(wechatFile), "file/*");
                startActivityForResult(intent, SELECT_FILE);
                break;
            case R.id.tv_add_fromOther:
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("file/*");
                startActivityForResult(intent, SELECT_FILE);
                break;
            case R.id.fab_add_send:
                int i=0;
                List<Account> accountList=new ArrayList<>();
                for (String s : mNameList) {
                    Account account = new Account();
                    account.setId(System.currentTimeMillis()+i);
                    account.setAccountName(s);
                    accountList.add(account);
                    i++;
                }
                if (accountList.size()!=0){
                    mAccountDao.insertInTx(accountList);
                    Toast.makeText(this,R.string.tos_added,Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                }else {
                    Toast.makeText(this,R.string.tos_addedNone,Toast.LENGTH_SHORT).show();
                }
                finish();
                break;
            case R.id.tv_add_decode:
                pressCount++;
                if (pressCount>=4) {
                    pressCount-=4;
                }
                if (mFilePath!=null)
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            readName(mFilePath,mCharsets[pressCount]);
                        }
                    }).start();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == SELECT_FILE) {
            String path = Uri.decode(data.getDataString());
            if (path.length()>7)
                mFilePath = path.substring(7);
            if (mFilePath.endsWith(".doc")||mFilePath.endsWith(".docx")||mFilePath.endsWith(".xls")){
                mTvSelectResult.setText(R.string.noSupportDocm);
            }else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        readName(mFilePath,mCharsets[0]);
                    }
                }).start();
            }
        }
    }

    private void readName(String path,Charset charset){
        FileInputStream fis=null;
        BufferedReader reader=null;
        String name;
        mNameList.clear();
        try {
            fis=new FileInputStream(new File(path));
            InputStreamReader rd=new InputStreamReader(fis,charset);
            reader=new BufferedReader(rd);
            int i=0;
            while((name = reader.readLine())!=null) {
                if (i<200&&!TextUtils.isEmpty(name))
                    mNameList.add(name);
                i++;
            }
            mHandler.sendEmptyMessage(200);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fis!=null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader!=null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {

            mTvSelectResult.setText(getString(R.string.read_record)+mNameList.size());
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onCreateCustomToolBar(Toolbar toolbar) {
        toolbar.setTitle(R.string.add);
    }
}
