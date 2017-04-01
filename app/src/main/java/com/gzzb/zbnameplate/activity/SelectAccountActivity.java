package com.gzzb.zbnameplate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.gzzb.zbnameplate.App;
import com.gzzb.zbnameplate.R;
import com.gzzb.zbnameplate.adapter.Listener;
import com.gzzb.zbnameplate.adapter.SelectAccountAdapter;
import com.gzzb.zbnameplate.bean.Account;
import com.gzzb.zbnameplate.global.Global;

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

}
