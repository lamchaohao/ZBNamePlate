package com.gzz100.nameplate.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gzz100.nameplate.R;
import com.gzz100.nameplate.bean.Account;

import java.util.List;


/**
 * account 适配器
 * Created by Lam on 2017/3/12.
 */

public class SortAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<Account> mAccountList;
    private Listener.OnItemClickListener mOnItemClickListener;
    private Listener.OnDownListener mOnDownListener;
    private Listener.OnUpwardListener mOnUpwardListener;
    private int mDevicesCount;

    public SortAdapter(Context context, List<Account> accounts,int devicesCount) {
        mContext=context;
        mAccountList=accounts;
        mDevicesCount=devicesCount;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View screenView = inflater.from(mContext).inflate(R.layout.content_sort_account,parent,false);
        viewHolder = new AccountViewHolder(screenView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int pos) {
        final AccountViewHolder viewHolder= (AccountViewHolder) holder;
        final int position =viewHolder.getLayoutPosition();
        if (position>=mAccountList.size()){
            return;
        }
        viewHolder.tvName.setText(mAccountList.get(position).getAccountName());
        if (position<mDevicesCount){
            viewHolder.ivUpward.setImageResource(R.drawable.ic_arrow_upward_indigo_700_36dp);
            viewHolder.ivDownward.setImageResource(R.drawable.ic_arrow_downward_red_700_36dp);
        }else{
            viewHolder.ivUpward.setImageResource(R.drawable.ic_account_box_light_blue_500_36dp);
            viewHolder.ivDownward.setImageResource(R.drawable.ic_add_circle_green_a700_36dp);
        }

        if (mOnItemClickListener!=null){
            viewHolder.llItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v,position);
                }
            });
        }

        if (mOnDownListener!=null&&position<mDevicesCount){
            viewHolder.ivDownward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnDownListener.onDownTap(mAccountList.get(position),position);
                }
            });
        }
        if (mOnUpwardListener!=null&&position<mDevicesCount){
            viewHolder.ivUpward.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnUpwardListener.onUpTap(mAccountList.get(position),position);
                }
            });

        }

    }



    @Override
    public int getItemCount() {
        return mAccountList.size();
    }

    private class AccountViewHolder extends RecyclerView.ViewHolder{
        TextView tvName;
        ImageView ivUpward;
        ImageView ivDownward;
        View llItem;
        public AccountViewHolder(View itemView) {
            super(itemView);
            llItem = itemView.findViewById(R.id.ll_sort);
            ivUpward = (ImageView) itemView.findViewById(R.id.iv_upward);
            ivDownward= (ImageView) itemView.findViewById(R.id.iv_accountIcon);
            tvName= (TextView) itemView.findViewById(R.id.tv_account_name);
        }

    }

    public void setOnDownListener(Listener.OnDownListener onDownListener) {
        mOnDownListener = onDownListener;
    }

    public void setOnUpwardListener(Listener.OnUpwardListener onUpwardListener) {
        mOnUpwardListener = onUpwardListener;
    }

    public void setOnItemClickListener(Listener.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

}
