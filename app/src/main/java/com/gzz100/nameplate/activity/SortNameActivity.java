package com.gzz100.nameplate.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.gzz100.nameplate.App;
import com.gzz100.nameplate.R;
import com.gzz100.nameplate.adapter.DeviceTabAdapter;
import com.gzz100.nameplate.bean.Account;
import com.gzz100.nameplate.dao.AccountDao;
import com.gzz100.nameplate.dao.DeviceDao;
import com.gzz100.nameplate.fragment.CandidateAccountFragment;
import com.gzz100.nameplate.fragment.SelectedFragment;
import com.gzz100.nameplate.utils.system.DensityUtil;

import java.util.ArrayList;
import java.util.List;

public class SortNameActivity extends BaseActivity {

    private SelectedFragment mSelectedFragment;
    private DeviceDao mDeviceDao;
    private AccountDao mAccountDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort_name);
        addSaveBt();
        loadData();
        initView();
    }

    private void addSaveBt() {
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
        mSelectedFragment.saveSortNum();
        setResult(RESULT_OK);
        finish();
    }

    private void loadData() {
        mAccountDao = ((App) getApplication()).getDaoSession().getAccountDao();
        mDeviceDao = ((App) getApplication()).getDaoSession().getDeviceDao();
    }

    private void initView() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sortTabLayout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.sortViewPager);

        List<String> titleList = new ArrayList<>();
        titleList.add(getString(R.string.selected));
        titleList.add(getString(R.string.candidate));
        //创建标签
        tabLayout.addTab(tabLayout.newTab().setText(R.string.selected));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.candidate));
        //初始化ViewPager控件，用于填充好友概述内容

        List<Fragment> mFragmentList = new ArrayList<>();
        mSelectedFragment = new SelectedFragment();
        CandidateAccountFragment candidateAccountFragment =new CandidateAccountFragment();
        mFragmentList.add(mSelectedFragment);
        mFragmentList.add(candidateAccountFragment);

        DeviceTabAdapter mAdapter = new DeviceTabAdapter(this.getSupportFragmentManager(), mFragmentList, titleList);

        //给ViewPager设置适配器
        viewPager.setAdapter(mAdapter);
        //将TabLayout和ViewPager关联起来
        tabLayout.setupWithViewPager(viewPager);
        //给TabLayout设置适配器
        tabLayout.setTabsFromPagerAdapter(mAdapter);
    }

    public void addNewAccount(Account account){
        mSelectedFragment.addNewAccount(account);
    }

    public DeviceDao getDeviceDao() {
        return mDeviceDao;
    }

    public AccountDao getAccountDao() {
        return mAccountDao;
    }

    @Override
    public void onCreateCustomToolBar(Toolbar toolbar) {
        toolbar.setTitle(R.string.sort);
    }
}
