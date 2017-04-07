package com.gzzb.zbnameplate.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gzzb.zbnameplate.R;
import com.gzzb.zbnameplate.bean.Device;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Lam on 2017/3/24.
 */

public class DeviceAdapter extends RecyclerView.Adapter {

    private List<Device> mDevices;
    private Context mContext;
    private Listener.OnItemClickListener mItemListener;
    private Listener.OnEditClickListener mEditListener;
    private Listener.OnItemLongClickListener mLongClickListener;

    public void setEditListener(Listener.OnEditClickListener editListener) {
        mEditListener = editListener;
    }

    public void setItemListener(Listener.OnItemClickListener itemListener) {
        mItemListener = itemListener;
    }


    public void setLongClickListener(Listener.OnItemLongClickListener longClickListener) {
        mLongClickListener = longClickListener;
    }

    public DeviceAdapter(List<Device> devices, Context context) {
        mDevices = devices;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.from(mContext).inflate(R.layout.content_devices, parent, false);
        DeviceHolder viewHolder = new DeviceHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int pos) {
        final DeviceHolder deviceHolder = (DeviceHolder) holder;
        int position = deviceHolder.getLayoutPosition();
        deviceHolder.mTvDeviceName.setText(mDevices.get(position).getDeviceName());
        deviceHolder.mTvDeviceSsid.setText(mDevices.get(position).getSsid());
        if (mEditListener!=null){
            deviceHolder.mIvDeviceEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = deviceHolder.getLayoutPosition();
                    mEditListener.onEditClick(v,pos);
                }
            });
        }

        if (mItemListener!=null){
            deviceHolder.mLlDeviceItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = deviceHolder.getLayoutPosition();
                    mItemListener.onItemClick(v,pos);
                }
            });
        }
        if (mLongClickListener!=null) {
            deviceHolder.mLlDeviceItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = deviceHolder.getLayoutPosition();
                    mLongClickListener.onLongClick(v,pos);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    class DeviceHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_device_name)
        TextView mTvDeviceName;
        @BindView(R.id.tv_device_ssid)
        TextView mTvDeviceSsid;
        @BindView(R.id.iv_account_edit)
        ImageView mIvDeviceEdit;
        @BindView(R.id.ll_deviceItem)
        LinearLayout mLlDeviceItem;

        public DeviceHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(DeviceHolder.this, itemView);
        }

    }



}
