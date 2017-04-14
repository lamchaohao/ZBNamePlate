package com.gzzb.nameplate.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gzz100.nameplate.R;
import com.gzz100.nameplate.activity.AboutActivity;
import com.gzz100.nameplate.activity.BrightnessActivity;
import com.gzz100.nameplate.activity.ConnectActivity;
import com.gzz100.nameplate.activity.DeviceManageActivity;
import com.gzz100.nameplate.activity.SelectEffectActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment {


    @BindView(R.id.tvSetConfig)
    TextView mTvSetConfig;
    @BindView(R.id.tvSetBrightness)
    TextView mTvSetBrightness;
    @BindView(R.id.tvSetDevice)
    TextView mTvSetDevice;
    @BindView(R.id.tvSetConnect)
    TextView mTvSetConnect;
    @BindView(R.id.tvSetAbout)
    TextView mTvSetAbout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @OnClick({R.id.tvSetConfig, R.id.tvSetBrightness, R.id.tvSetDevice, R.id.tvSetConnect,R.id.tvSetAbout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvSetConfig:
                startActivity(new Intent(getContext(), SelectEffectActivity.class));
                break;
            case R.id.tvSetBrightness:
                startActivity(new Intent(getContext(), BrightnessActivity.class));
                break;
            case R.id.tvSetDevice:
                startActivity(new Intent(getContext(), DeviceManageActivity.class));
                break;
            case R.id.tvSetConnect:
                startActivity(new Intent(getContext(), ConnectActivity.class));
                break;
            case R.id.tvSetAbout:
                startActivity(new Intent(getContext(), AboutActivity.class));
                break;
        }
    }
}
