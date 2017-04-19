package com.gzz100.nameplate.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.gzz100.nameplate.App;
import com.gzz100.nameplate.R;
import com.gzz100.nameplate.adapter.Listener;
import com.gzz100.nameplate.adapter.SortAdapter;
import com.gzz100.nameplate.bean.Account;
import com.gzz100.nameplate.bean.Device;
import com.gzz100.nameplate.dao.AccountDao;
import com.gzz100.nameplate.utils.system.DensityUtil;

import java.util.List;

public class SortActivity extends BaseActivity implements Listener.OnUpwardListener,Listener.OnDownListener,Listener.OnItemClickListener{

    private int mSize;
    private List<Account> mAccountList;
    private SortAdapter mAdapter;
    private RecyclerView mView;
    private int layoutPos;
    private AccountDao mAccountDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort);
        initView();
    }


    private void initView() {
        mView = (RecyclerView) findViewById(R.id.rcv_sort);
        mView.setLayoutManager(new LinearLayoutManager(this));
        mAccountDao = ((App) getApplication()).getDaoSession().getAccountDao();
        mAccountList = mAccountDao.queryBuilder().list();
        List<Device> deviceList = ((App) getApplication()).getDaoSession().getDeviceDao().queryBuilder().list();
        mSize=mAccountList.size()<deviceList.size() ?mAccountList.size() : deviceList.size();
        mAdapter = new SortAdapter(this, mAccountList,deviceList.size());
        mView.setAdapter(mAdapter);
        mAdapter.setOnDownListener(this);
        mAdapter.setOnUpwardListener(this);
        mAdapter.setOnItemClickListener(this);
        Button button=new Button(this);
        button.setBackgroundResource(R.drawable.focus_bg);
        button.setText(R.string.save);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOkAndFinish();
            }
        });
        Toolbar.LayoutParams prams =new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        prams.gravity= Gravity.RIGHT;
        prams.rightMargin= DensityUtil.dp2px(this,10);
        toolbar.addView(button,prams);
    }

    private void setOkAndFinish() {
        for (int i = 0; i < mAccountList.size(); i++) {
            if (i<mSize){
                mAccountList.get(i).setSortNumber(i);
            }else {
                mAccountList.get(i).setSortNumber(1000);
            }
        }
        mAccountDao.insertOrReplaceInTx(mAccountList);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onDownTap(Account account, int position) {
        if (position==mSize-1){
            mAccountList.remove(position);
            mAccountList.add(0,account);
            mAdapter.notifyItemRemoved(position);
            mAdapter.notifyItemInserted(0);
            mView.smoothScrollToPosition(0);
        }else {
            Account nextAccount = mAccountList.get(position + 1);
            mAccountList.set(position,nextAccount);
            mAccountList.set(position+1,account);
            mAdapter.notifyItemChanged(position);
            mAdapter.notifyItemChanged(position+1);
        }
        mAdapter.notifyItemRangeChanged(0,mSize);
    }

    @Override
    public void onItemClick(View view, int position) {
        if (position<mSize){
            return;
        }
        if (layoutPos==mSize) {
            layoutPos-=(mSize);
        }
        Account account = mAccountList.get(position);
        Account lastOne = mAccountList.get(layoutPos);

        mAccountList.set(layoutPos,account);
        mAccountList.set(position,lastOne);
        mAdapter.notifyItemChanged(layoutPos);
        mAdapter.notifyItemChanged(position);
        layoutPos++;
    }

    @Override
    public void onUpTap(Account account, int position) {
        if (position==0){
            mAccountList.remove(0);
            mAccountList.add(mSize-1,account);
            mAdapter.notifyItemRemoved(position);
            mAdapter.notifyItemInserted(mSize-1);

        }else {
            Account lastAccount = mAccountList.get(position - 1);
            mAccountList.set(position,lastAccount);
            mAccountList.set(position-1,account);
            mAdapter.notifyItemChanged(position);
            mAdapter.notifyItemChanged(position-1);
        }
        mAdapter.notifyItemRangeChanged(0,mSize);
    }
    @Override
    public void onCreateCustomToolBar(Toolbar toolbar) {
        toolbar.setTitle(R.string.sort);
    }
}
