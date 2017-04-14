package com.gzz100.nameplate.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gzz100.nameplate.R;

import java.util.List;

/**
 * 可添加的设备
 * Created by Lam on 2016/3/27.
 */

public class AvaliAdapter extends RecyclerView.Adapter {


    private Context mContextAct;
    private List<ScanResult> mWifiList;
    private Listener.OnItemClickListener mOnItemClickLitener;
    private Listener.OnAddOnClickListener mOnAddOnClickListener;


    public AvaliAdapter(Context context, List<ScanResult> wifiList) {
        mContextAct=context;
        mWifiList=wifiList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContextAct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view =inflater.from(mContextAct).inflate(R.layout.content_avaliable,parent,false);
        WifiViewHolder viewHolder =new WifiViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int pos) {
        final WifiViewHolder viewHolder= (WifiViewHolder) holder;
        final int position=viewHolder.getLayoutPosition();
        viewHolder.tvWifiName.setText(mWifiList.get(position).SSID);
        // 如果设置了回调，则设置点击事件
        if (mOnItemClickLitener != null) {
            viewHolder.llAvali.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    int pos = viewHolder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(viewHolder.itemView, pos);
                }
            });
        }
        if (mOnAddOnClickListener!=null){
            viewHolder.ivAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = viewHolder.getLayoutPosition();
                    mOnAddOnClickListener.onAddItemClick(v,pos);
                }
            });
        }

    }


    @Override
    public int getItemCount() {
        return mWifiList.size();
    }


    public void setItemOnClickListener(Listener.OnItemClickListener itemOnClickListener){
        this.mOnItemClickLitener=itemOnClickListener;
    }

    public void setOnAddOnClickListener(Listener.OnAddOnClickListener addOnClickListener){
        mOnAddOnClickListener=addOnClickListener;
    }

    class WifiViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llAvali;
        TextView tvWifiName;
        ImageView ivAdd;

        public WifiViewHolder(View itemView) {
            super(itemView);
            llAvali = (LinearLayout) itemView.findViewById(R.id.ll_avaliItem);
            tvWifiName = (TextView) itemView.findViewById(R.id.tv_avali_name);
            ivAdd= (ImageView) itemView.findViewById(R.id.iv_avali_add);
        }
    }

}

