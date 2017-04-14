package com.gzz100.nameplate.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.gzz100.nameplate.R;
import com.gzz100.nameplate.adapter.DeviceTabAdapter;
import com.gzz100.nameplate.bean.Device;
import com.gzz100.nameplate.fragment.AvailableDeviceFragment;
import com.gzz100.nameplate.fragment.DevicesFragment;

import java.util.ArrayList;
import java.util.List;

public class DeviceManageActivity extends BaseActivity {

    private DevicesFragment mDevicesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manage);
        initView();
    }

    private void initView() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.dmTabLayout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.dmViewPager);

        List<String> titleList = new ArrayList<>();
        titleList.add(getString(R.string.devices));
        titleList.add(getString(R.string.avaliable));
        //创建标签
        tabLayout.addTab(tabLayout.newTab().setText(R.string.devices));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.avaliable));
        //初始化ViewPager控件，用于填充好友概述内容

        List<Fragment> mFragmentList = new ArrayList<>();
        mDevicesFragment = new DevicesFragment();
        AvailableDeviceFragment availableFragment =new AvailableDeviceFragment();
        mFragmentList.add(mDevicesFragment);
        mFragmentList.add(availableFragment);

        DeviceTabAdapter mAdapter = new DeviceTabAdapter(this.getSupportFragmentManager(), mFragmentList, titleList);

        //给ViewPager设置适配器
        viewPager.setAdapter(mAdapter);
        //将TabLayout和ViewPager关联起来
        tabLayout.setupWithViewPager(viewPager);
        //给TabLayout设置适配器
        tabLayout.setTabsFromPagerAdapter(mAdapter);
    }

    public void addNewDevice(Device device){
        mDevicesFragment.addNewDevice(device);
    }

    @Override
    public void onCreateCustomToolBar(Toolbar toolbar) {
        toolbar.setTitle(R.string.deviceManage);
    }
}
