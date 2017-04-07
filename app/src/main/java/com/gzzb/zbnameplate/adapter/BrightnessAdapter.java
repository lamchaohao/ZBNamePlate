package com.gzzb.zbnameplate.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gzzb.zbnameplate.R;
import com.gzzb.zbnameplate.bean.Device;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BrightnessAdapter extends RecyclerView.Adapter {


    private Context mContextAct;
    private Listener.OnItemClickListener mOnItemClickLitener;
    private Listener.OnPlayClickListener mOnPlayClickListener;
    private Listener.OnProgressChangedListener mProgressChangedListener;
    private List<Device> mDeviceList;

    public BrightnessAdapter(Context context, List<Device> namePlates) {
        mContextAct = context;
        mDeviceList = namePlates;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContextAct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.from(mContextAct).inflate(R.layout.content_brightness, parent, false);
        SendViewHolder viewHolder = new SendViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int pos) {
        final SendViewHolder viewHolder = (SendViewHolder) holder;
        final int position=viewHolder.getLayoutPosition();
        final Device device = mDeviceList.get(position);
        viewHolder.tvDevice.setText(device.getDeviceName());

        if (device.getIsOnline()) {
            viewHolder.ivIcon.setImageResource(R.drawable.ic_cast_connected_green_a700_36dp);
        }else {
            viewHolder.ivIcon.setImageResource(R.drawable.ic_cast_light_blue_500_36dp);
        }
        viewHolder.tvWifi.setText(device.getSsid());
        viewHolder.tvState.setText(device.getState());
        if (mProgressChangedListener!=null) {
            viewHolder.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser){
                        if (progress<=33){
                            seekBar.setProgress(33);
                            viewHolder.ivLevel.setImageResource(R.drawable.ic_brightness_low_cyan_700_24dp);
                        }else if (progress>33&&progress<=66){
                            seekBar.setProgress(66);
                            viewHolder.ivLevel.setImageResource(R.drawable.ic_brightness_medium_pink_500_24dp);
                        }else if (progress>66&&progress<=100){
                            viewHolder.ivLevel.setImageResource(R.drawable.ic_brightness_high_amber_700_24dp);
                            seekBar.setProgress(100);
                        }
                        int pos = viewHolder.getLayoutPosition();
                        mProgressChangedListener.onProgressChanged(seekBar,pos,progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        viewHolder.seekbar.setProgress(device.getBrightness());
        int brightness = device.getBrightness();
        if (brightness<=33){
            viewHolder.ivLevel.setImageResource(R.drawable.ic_brightness_low_cyan_700_24dp);
        }else if (brightness>33&&brightness<=66){
            viewHolder.ivLevel.setImageResource(R.drawable.ic_brightness_medium_pink_500_24dp);
        }else if (brightness>66&&brightness<=100){
            viewHolder.ivLevel.setImageResource(R.drawable.ic_brightness_high_amber_700_24dp);
        }

        if (mOnPlayClickListener!=null){
            viewHolder.ivSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = viewHolder.getLayoutPosition();
                    mOnPlayClickListener.onPlayClick(v,pos);
                }
            });

        }

    }


    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }


    public void setItemOnClickListener(Listener.OnItemClickListener itemOnClickListener) {
        this.mOnItemClickLitener = itemOnClickListener;
    }

    public void setOnPlayClickListener(Listener.OnPlayClickListener onPlayClickListener) {
        mOnPlayClickListener = onPlayClickListener;
    }

    public void setProgressChangedListener(Listener.OnProgressChangedListener progressChangedListener) {
        mProgressChangedListener = progressChangedListener;
    }

    public class SendViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_brtnDevice)
        TextView tvDevice;
        @BindView(R.id.tv_brtnWifi)
        TextView tvWifi;
        @BindView(R.id.sb_brightness)
        SeekBar seekbar;
        @BindView(R.id.ll_brtnWifi)
        LinearLayout llWifi;
        @BindView(R.id.iv_brtnSend)
        ImageView ivSend;
        @BindView(R.id.tv_brtn_tip)
        TextView tvState;
        @BindView(R.id.iv_brgtns_level)
        ImageView ivLevel;
        @BindView(R.id.iv_brgtns_icon)
        ImageView ivIcon;
        public SendViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(SendViewHolder.this, itemView);
        }
    }

}

