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
import com.gzzb.zbnameplate.bean.Account;

import java.util.List;


/**
 * account 适配器
 * Created by Lam on 2017/3/12.
 */

public class AccountAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<Account> mAccountList;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnEditClickListener mOnEditClickListener;
    private OnSendClickListener mOnSendClickListener;


    public AccountAdapter(Context context, List<Account> accounts) {
        mContext=context;
        mAccountList=accounts;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View screenView = inflater.from(mContext).inflate(R.layout.content_account,parent,false);
        viewHolder = new AccountViewHolder(screenView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        AccountViewHolder viewHolder= (AccountViewHolder) holder;
        viewHolder.tvName.setText(mAccountList.get(position).getAccountName());
        if (mOnItemClickListener!=null){
            viewHolder.flMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onClick(v,position);
                }
            });
        }
        if (mOnItemLongClickListener!=null){
            viewHolder.flMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemLongClickListener.onLongClick(v,position);
                    return true;
                }
            });
        }
        if (mOnEditClickListener!=null){
            viewHolder.ivEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnEditClickListener.onEditClick(v,position);
                }
            });
        }
        if(mOnSendClickListener!=null){
            viewHolder.ivSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnSendClickListener.onSendClick(v,position);
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
        ImageView ivEdit;
        ImageView ivSend;
        LinearLayout flMain;
        public AccountViewHolder(View itemView) {
            super(itemView);
            flMain= (LinearLayout) itemView.findViewById(R.id.ll_account);
            ivEdit = (ImageView) itemView.findViewById(R.id.iv_account_edit);
            ivSend = (ImageView) itemView.findViewById(R.id.iv_account_send);
            tvName = (TextView) itemView.findViewById(R.id.tv_account_name);
        }

    }



    public interface OnItemClickListener{
        void onClick(View v, int position);
    }

    public interface OnItemLongClickListener{
        void onLongClick(View v, int position);
    }
    public interface OnEditClickListener{
        void onEditClick(View v, int position);
    }
    public interface OnSendClickListener{
        void onSendClick(View v, int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener){
        mOnItemClickListener=listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener){
        mOnItemLongClickListener=onItemLongClickListener;
    }

    public void setOnEditClickListener(OnEditClickListener onEditClickListener) {
        mOnEditClickListener = onEditClickListener;
    }

    public void setOnSendClickListener(OnSendClickListener onSendClickListener) {
        mOnSendClickListener = onSendClickListener;
    }
}
