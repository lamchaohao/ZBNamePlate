package com.gzz100.nameplate.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gzz100.nameplate.R;
import com.gzz100.nameplate.adapter.MainFragmentAdapter;
import com.gzz100.nameplate.fragment.AccountFragment;
import com.gzz100.nameplate.fragment.ConnectFragment;
import com.gzzb.nameplate.fragment.SettingFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,ViewPager.OnPageChangeListener {

    private LinearLayout mLlAccount;
    private LinearLayout mLlConnet;
    private ImageView mIvAccount;
    private ImageView mIvConnet;
    private TextView mTvAccount;
    private TextView mTvConnet;
    private ViewPager mViewPager;
    private TextView mToolbarTitle;

    private List<Fragment> mFragmentList;
    private FragmentManager mSupportFragmentManager;
    private boolean backFlag;
    private long firstTime;
    private long lastTime;
    private LinearLayout mLlSetting;
    private ImageView mIvSetting;
    private TextView mTvSetting;
    private AccountFragment mAccountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        initView();
    }

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_main);

        mToolbarTitle = (TextView) findViewById(R.id.tv_toolbar_title);
        setSupportActionBar(mToolbar);
    }

    private void initView() {
        initBottom();
        mFragmentList = new ArrayList<>();
        mViewPager = (ViewPager) findViewById(R.id.vp_main);
        mSupportFragmentManager = getSupportFragmentManager();
        mAccountFragment = new AccountFragment();
        ConnectFragment connectFragment = new ConnectFragment();
        SettingFragment settingFragment = new SettingFragment();
        mFragmentList.add(mAccountFragment);
        mFragmentList.add(connectFragment);
        mFragmentList.add(settingFragment);
        MainFragmentAdapter adapter =new MainFragmentAdapter(mSupportFragmentManager,mFragmentList);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setCurrentItem(0);//先设置一个
        mIvAccount.setImageResource(R.drawable.ic_account_box_light_blue_500_36dp);
        mTvAccount.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
    }

    private void initBottom() {
        mLlAccount = (LinearLayout) findViewById(R.id.ll_main_account);
        mLlConnet = (LinearLayout) findViewById(R.id.ll_main_connect);
        mLlSetting = (LinearLayout)findViewById(R.id.ll_main_setting);
        mIvAccount = (ImageView) findViewById(R.id.iv_main_account);
        mIvConnet = (ImageView) findViewById(R.id.iv_main_connect);
        mIvSetting = (ImageView) findViewById(R.id.iv_main_setting);
        mTvAccount = (TextView) findViewById(R.id.tv_main_account);
        mTvConnet = (TextView) findViewById(R.id.tv_main_connect);
        mTvSetting = (TextView) findViewById(R.id.tv_main_setting);
        mLlAccount.setOnClickListener(this);
        mLlConnet.setOnClickListener(this);
        mLlSetting.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_main_account:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.ll_main_connect:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.ll_main_setting:
                mViewPager.setCurrentItem(2);
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        resetImg();
        switch (position) {
            case 0:
                mIvAccount.setImageResource(R.drawable.ic_account_box_light_blue_500_36dp);
                mTvAccount.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                mToolbarTitle.setText(R.string.edit);
                break;
            case 1:
                mIvConnet.setImageResource(R.drawable.ic_wifi_light_blue_500_36dp);
                mTvConnet.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                mToolbarTitle.setText(R.string.connectWifi);
                break;
            case 2:
                mIvSetting.setImageResource(R.drawable.ic_settings_light_blue_500_36dp);
                mTvSetting.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                mToolbarTitle.setText(R.string.setting);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
    private void resetImg() {
        mIvAccount.setImageResource(R.drawable.ic_account_box_grey_700_36dp);
        mTvAccount.setTextColor(getResources().getColor(R.color.textPrimary));
        mIvConnet.setImageResource(R.drawable.ic_wifi_grey_700_36dp);
        mTvConnet.setTextColor(getResources().getColor(R.color.textPrimary));
        mIvSetting.setImageResource(R.drawable.ic_settings_grey_700_36dp);
        mTvSetting.setTextColor(getResources().getColor(R.color.textPrimary));
    }

    @Override
    public void onBackPressed() {
        if (mAccountFragment.isDeleteMode()) {
            mAccountFragment.cancleDeleteMode();
            return;
        }

        if (!backFlag){//第一次点击
            Toast.makeText(this, R.string.pressToexit, Toast.LENGTH_SHORT).show();
            firstTime = System.currentTimeMillis();
            backFlag=true;
        }else{
            //第二次点击
            lastTime = System.currentTimeMillis();
            long gapTime=lastTime-firstTime;

            if (gapTime<2000){
                finish();
            }else{
                //防止时间过长再次点击没反应
                Toast.makeText(this, R.string.pressToexit, Toast.LENGTH_SHORT).show();
                firstTime = System.currentTimeMillis();

            }
        }
    }
}
