package com.gzzb.zbnameplate.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.gzzb.zbnameplate.R;
import com.gzzb.zbnameplate.adapter.DeviceTabAdapter;
import com.gzzb.zbnameplate.fragment.AvailableDeviceFragment;
import com.gzzb.zbnameplate.fragment.DevicesFragment;

import java.util.ArrayList;
import java.util.List;

public class DeviceManageActivity extends AppCompatActivity {

    private DeviceTabAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manage);
        initView();
    }

    private void initView() {
        setActionBar();
        TabLayout tabLayout = (TabLayout) findViewById(R.id.dmTabLayout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.dmViewPager);

        List<String> titleList = new ArrayList<>();
        titleList.add("设备");
        titleList.add("在线");
        //创建标签
        tabLayout.addTab(tabLayout.newTab().setText("设备"));
        tabLayout.addTab(tabLayout.newTab().setText("在线"));
        //初始化ViewPager控件，用于填充好友概述内容

        List<Fragment> mFragmentList = new ArrayList<>();
        DevicesFragment devicesFragment=new DevicesFragment();
        AvailableDeviceFragment availableFragment =new AvailableDeviceFragment();
        mFragmentList.add(devicesFragment);
        mFragmentList.add(availableFragment);

        mAdapter = new DeviceTabAdapter(this.getSupportFragmentManager(), mFragmentList, titleList);

        //给ViewPager设置适配器
        viewPager.setAdapter(mAdapter);
        //将TabLayout和ViewPager关联起来。
        tabLayout.setupWithViewPager(viewPager);
        //给TabLayout设置适配器
        tabLayout.setTabsFromPagerAdapter(mAdapter);
    }

    private void setActionBar() {
        ActionBar mActionBar=getSupportActionBar();
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("设备管理");
    }
}
