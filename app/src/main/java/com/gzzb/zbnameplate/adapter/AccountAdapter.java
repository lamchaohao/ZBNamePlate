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
    private boolean[] isSelected;
    private boolean isMutilChoiceMode;
    private Listener.OnItemClickListener mOnItemClickListener;
    private Listener.OnItemLongClickListener mOnItemLongClickListener;
    private Listener.OnEditClickListener mOnEditClickListener;
    private Listener.OnSendClickListener mOnSendClickListener;


    public AccountAdapter(Context context, List<Account> accounts) {
        mContext=context;
        mAccountList=accounts;
        isSelected = new boolean[mAccountList.size()];
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
        //是否选择删除
        if (isMutilChoiceMode){
            viewHolder.ivDelete.setVisibility(View.VISIBLE);
            if (isSelected[position])
                viewHolder.ivDelete.setImageResource(R.drawable.ic_remove_circle_red_a700_36dp);
            else
                viewHolder.ivDelete.setImageResource(R.drawable.ic_remove_circle_outline_red_200_36dp);
        }else {
            viewHolder.ivDelete.setVisibility(View.GONE);
        }

        if (mOnItemClickListener!=null){
            viewHolder.flMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isMutilChoiceMode()) {
                        isSelected[position]= !isSelected[position];
                        notifyItemChanged(position);
                    }else {
                        mOnItemClickListener.onItemClick(v,position);
                    }
                }
            });
        }
        if (mOnItemLongClickListener!=null){
            viewHolder.flMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!isMutilChoiceMode()){
                        mOnItemLongClickListener.onLongClick(v,position);
                    }
                    return true;
                }
            });
        }
        if (mOnEditClickListener!=null){
            viewHolder.ivEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  if (!isMutilChoiceMode()) {
                    mOnEditClickListener.onEditClick(v,position);
                  }
                }
            });
        }
        if(mOnSendClickListener!=null){
            viewHolder.ivSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isMutilChoiceMode()) {
                        mOnSendClickListener.onSendClick(v,position);
                    }
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
        ImageView ivDelete;
        public AccountViewHolder(View itemView) {
            super(itemView);
            flMain= (LinearLayout) itemView.findViewById(R.id.ll_account);
            ivEdit = (ImageView) itemView.findViewById(R.id.iv_account_edit);
            ivSend = (ImageView) itemView.findViewById(R.id.iv_account_send);
            tvName = (TextView) itemView.findViewById(R.id.tv_account_name);
            ivDelete= (ImageView) itemView.findViewById(R.id.iv_account_delete);
        }

    }


    public void setOnItemClickListener(Listener.OnItemClickListener listener){
        mOnItemClickListener=listener;
    }

    public void setOnItemLongClickListener(Listener.OnItemLongClickListener onItemLongClickListener){
        mOnItemLongClickListener=onItemLongClickListener;
    }

    public void setOnEditClickListener(Listener.OnEditClickListener onEditClickListener) {
        mOnEditClickListener = onEditClickListener;
    }

    public void setOnSendClickListener(Listener.OnSendClickListener onSendClickListener) {
        mOnSendClickListener = onSendClickListener;
    }

    public void setMutilChoiceMode(boolean mutilChoiceMode) {
        isMutilChoiceMode = mutilChoiceMode;
    }

    public boolean isMutilChoiceMode() {
        return isMutilChoiceMode;
    }

    public void setIsSelected(boolean[] isSelected) {
        this.isSelected = isSelected;
    }

    public boolean[] getIsSelected() {
        return isSelected;
    }
}
