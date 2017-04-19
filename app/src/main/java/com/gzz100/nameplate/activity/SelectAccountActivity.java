package com.gzz100.nameplate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.gzz100.nameplate.App;
import com.gzz100.nameplate.R;
import com.gzz100.nameplate.adapter.Listener;
import com.gzz100.nameplate.adapter.SelectAccountAdapter;
import com.gzz100.nameplate.bean.Account;
import com.gzz100.nameplate.global.Global;

import java.util.List;

public class SelectAccountActivity extends BaseActivity {

    private List<Account> mAccountList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_account);
        initView();
    }

    private void initView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rcv_selectAccount);
        mAccountList = ((App) getApplication()).getDaoSession().getAccountDao().queryBuilder().list();
        SelectAccountAdapter adapter=new SelectAccountAdapter(this, mAccountList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new Listener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent=new Intent();
                intent.putExtra(Global.EX_ACCOUNT_ID, mAccountList.get(position).getId());
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }

    @Override
    public void onCreateCustomToolBar(Toolbar toolbar) {
        toolbar.setTitle(R.string.selectAccount);
    }
}
