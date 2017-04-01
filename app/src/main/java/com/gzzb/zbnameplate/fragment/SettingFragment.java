package com.gzzb.zbnameplate.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gzzb.zbnameplate.R;
import com.gzzb.zbnameplate.activity.BrightnessActivity;
import com.gzzb.zbnameplate.activity.DeviceManageActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment {


    @BindView(R.id.tvSetConfig)
    TextView mTvConfig;
    @BindView(R.id.tvSetBrightness)
    TextView mTvBrightness;
    @BindView(R.id.tvSetDevice)
    TextView mTvDevice;

    public SettingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.tvSetConfig, R.id.tvSetBrightness, R.id.tvSetDevice})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvSetConfig:
                break;
            case R.id.tvSetBrightness:
                startActivity(new Intent(getContext(), BrightnessActivity.class));
                break;
            case R.id.tvSetDevice:
                startActivity(new Intent(getContext(), DeviceManageActivity.class));
                break;
        }
    }
}
