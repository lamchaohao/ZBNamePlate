package com.gzz100.nameplate.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gzz100.nameplate.R;
import com.gzz100.nameplate.bean.Account;
import com.gzz100.nameplate.bean.Device;
import com.gzz100.nameplate.bean.NamePlate;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SendAdapter extends RecyclerView.Adapter{

    private Context mContextAct;
    private Listener.OnItemClickListener mOnItemClickLitener;
    private Listener.OnPlayClickListener mOnPlayClickListener;
    private List<NamePlate> mNamePlateList;

    public SendAdapter(Context context, List<NamePlate> namePlates) {
        mContextAct = context;
        mNamePlateList = namePlates;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContextAct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.from(mContextAct).inflate(R.layout.content_send, parent, false);
        SendViewHolder viewHolder = new SendViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final SendViewHolder viewHolder = (SendViewHolder) holder;
        NamePlate namePlate = mNamePlateList.get(position);
        Device device = namePlate.getDevice();
        Account account = namePlate.getAccount();
        viewHolder.tvSendDevice.setText(device.getDeviceName());
        viewHolder.tvSendWifi.setText(device.getSsid());

        if (account!=null){
            viewHolder.tvSendName.setText(account.getAccountName());
        }
        if (!TextUtils.isEmpty(namePlate.getState())){
            viewHolder.tvSendState.setVisibility(View.VISIBLE);
            viewHolder.tvSendState.setText(namePlate.getState());
        }else {
            viewHolder.tvSendState.setVisibility(View.GONE);
        }
        if (namePlate.isFocus()){
            viewHolder.llSendItem.setBackgroundResource(R.drawable.focus_bg);
        }else {
            if (device.getIsOnline()){
                viewHolder.llSendItem.setBackgroundResource(R.drawable.recycler_bg);
            }else {
                viewHolder.llSendItem.setBackgroundResource(R.drawable.un_focus_bg);
            }
        }

        if (device.getIsOnline()) {
            viewHolder.ivSendWifi.setImageResource(R.drawable.ic_cast_connected_green_a700_36dp);
        }else {
            viewHolder.ivSendWifi.setImageResource(R.drawable.ic_cast_light_blue_500_36dp);
        }

        if (mOnItemClickLitener!=null){
            viewHolder.llSendItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = viewHolder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(v,layoutPosition);
                }
            });
        }
        if (mOnPlayClickListener!=null){
            viewHolder.ivSendSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = viewHolder.getLayoutPosition();
                    mOnPlayClickListener.onPlayClick(v,layoutPosition);
                }
            });
        }
        viewHolder.progressBar.setProgress(namePlate.getProgress());
    }



    @Override
    public int getItemCount() {
        return mNamePlateList.size();
    }


    public void setItemOnClickListener(Listener.OnItemClickListener itemOnClickListener) {
        this.mOnItemClickLitener = itemOnClickListener;
    }

    public void setOnPlayClickListener(Listener.OnPlayClickListener onPlayClickListener) {
        mOnPlayClickListener = onPlayClickListener;
    }


    public class SendViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_sendName)
        public TextView tvSendName;
        @BindView(R.id.iv_sendWifi)
        public ImageView ivSendWifi;
        @BindView(R.id.tv_sendDevice)
        public TextView tvSendDevice;
        @BindView(R.id.tv_sendWifi)
        public TextView tvSendWifi;
        @BindView(R.id.ll_sendWifi)
        public LinearLayout llSendWifi;
        @BindView(R.id.iv_sendSend)
        public ImageView ivSendSend;
        @BindView(R.id.ll_sendItem)
        public LinearLayout llSendItem;
        @BindView(R.id.tv_sendState)
        public TextView tvSendState;
        @BindView(R.id.pb_send)
        public ProgressBar progressBar;

        public SendViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(SendViewHolder.this, itemView);
        }

    }

}

