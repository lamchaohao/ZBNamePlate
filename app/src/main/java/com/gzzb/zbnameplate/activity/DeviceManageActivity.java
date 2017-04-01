package com.gzzb.zbnameplate.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.gzzb.zbnameplate.R;
import com.gzzb.zbnameplate.adapter.DeviceTabAdapter;
import com.gzzb.zbnameplate.bean.Device;
import com.gzzb.zbnameplate.fragment.AvailableDeviceFragment;
import com.gzzb.zbnameplate.fragment.DevicesFragment;

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
        titleList.add("设备");
        titleList.add("可添加");
        //创建标签
        tabLayout.addTab(tabLayout.newTab().setText("设备"));
        tabLayout.addTab(tabLayout.newTab().setText("可添加"));
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
}
