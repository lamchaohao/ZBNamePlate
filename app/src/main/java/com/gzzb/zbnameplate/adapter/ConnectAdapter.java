package com.gzzb.zbnameplate.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gzzb.zbnameplate.R;
import com.gzzb.zbnameplate.bean.Device;
import com.gzzb.zbnameplate.utils.system.WifiAdmin;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Lam on 2016/10/18.
 */

public class ConnectAdapter extends RecyclerView.Adapter {



    private Context mContextAct;
    private List<ScanResult> mWifiList;
    private Listener.OnItemClickListener mOnItemClickLitener;
    private WifiAdmin mWifiAdmin;
    private List<Device> mDevices;


    public ConnectAdapter(Context context, List<ScanResult> wifiList, WifiAdmin wifiAdmin, List<Device> devices) {
        mContextAct = context;
        mWifiList = wifiList;
        mWifiAdmin = wifiAdmin;
        mDevices = devices;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContextAct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.from(mContextAct).inflate(R.layout.content_indetify_wifi, parent, false);
        WifiAdapterViewHolder viewHolder = new WifiAdapterViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int pos) {
        final WifiAdapterViewHolder viewHolder = (WifiAdapterViewHolder) holder;
        final int position=viewHolder.getLayoutPosition();
        boolean hasName=false;
        for (Device device : mDevices) {
            if (mWifiList.get(position).SSID.equals(device.getSsid())) {
                viewHolder.tvDeviceName.setText(device.getDeviceName());
                hasName=true;
                break;
            }
        }
        if (hasName) {
            viewHolder.tvDeviceName.setVisibility(View.VISIBLE);
        }else {
            viewHolder.tvDeviceName.setVisibility(View.GONE);
            viewHolder.tvWifiName.setTextSize(18);
        }
        viewHolder.tvWifiName.setText(mWifiList.get(position).SSID);
        setUplevel(viewHolder.ivLevel, position);

        // 如果设置了回调，则设置点击事件
        if (mOnItemClickLitener != null) {
            viewHolder.llItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = viewHolder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(viewHolder.itemView, pos);
                }
            });
        }
        if (mWifiAdmin.getWifiInfo().getSSID().equals("\"" + mWifiList.get(position).SSID + "\"")) {
            viewHolder.ivCheck.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivCheck.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * 设置WiFi信号图标，是否需要密码等
     *
     * @param iv_wifiLevel
     * @param position
     */
    private void setUplevel(ImageView iv_wifiLevel, int position) {
        ScanResult wifiInfo = mWifiList.get(position);
        if (wifiInfo.level < 0 && wifiInfo.level >= -50)
            iv_wifiLevel.setImageResource(R.drawable.ic_signal_wifi_4_bar_lock_light_blue_500_36dp);
        else if (wifiInfo.level < -50 && wifiInfo.level >= -70)
            iv_wifiLevel.setImageResource(R.drawable.ic_signal_wifi_3_bar_lock_light_blue_500_36dp);
        else if (wifiInfo.level < -70 && wifiInfo.level >= -85)
            iv_wifiLevel.setImageResource(R.drawable.ic_signal_wifi_2_bar_lock_light_blue_500_36dp);
        else if (wifiInfo.level < -85 && wifiInfo.level >= -100)
            iv_wifiLevel.setImageResource(R.drawable.ic_signal_wifi_1_bar_lock_light_blue_500_36dp);
    }


    @Override
    public int getItemCount() {
        return mWifiList.size();
    }


    public void setItemOnClickListener(Listener.OnItemClickListener itemOnClickListener) {
        this.mOnItemClickLitener = itemOnClickListener;
    }

    class WifiAdapterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_Conn_deviceName)
        TextView tvDeviceName;
        @BindView(R.id.tv_Conn_wifiName)
        TextView tvWifiName;
        @BindView(R.id.ll_Conn_item)
        LinearLayout llItem;
        @BindView(R.id.iv_Conn_level)
        ImageView ivLevel;
        @BindView(R.id.iv_Conn_check)
        ImageView ivCheck;
        public WifiAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(WifiAdapterViewHolder.this,itemView);
        }
    }

}

