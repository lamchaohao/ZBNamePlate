package com.gzzb.zbnameplate.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gzzb.zbnameplate.R;
import com.gzzb.zbnameplate.bean.Account;

import java.util.List;


/**
 * account 适配器
 * Created by Lam on 2017/3/12.
 */

public class SelectAccountAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<Account> mAccountList;
    private Listener.OnItemClickListener mOnItemClickListener;
    private boolean[] isChecks;
    private int oldPos;

    public void setOnItemClickListener(Listener.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public SelectAccountAdapter(Context context, List<Account> accounts) {
        mContext=context;
        mAccountList=accounts;
        isChecks=new boolean[accounts.size()];
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View screenView = inflater.from(mContext).inflate(R.layout.content_select_account,parent,false);
        viewHolder = new AccountViewHolder(screenView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final AccountViewHolder viewHolder= (AccountViewHolder) holder;
        viewHolder.tvName.setText(mAccountList.get(position).getAccountName());
        Drawable[] drawables = viewHolder.tvName.getCompoundDrawables();
        if (isChecks[position]) {
            drawables[2].setAlpha(255);
        }else {
            drawables[2].setAlpha(0);
        }
        if (mOnItemClickListener!=null){
            viewHolder.tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //拿到被点击的位置
                    int layoutPosition = viewHolder.getLayoutPosition();
                    for (int i = 0; i < isChecks.length; i++) {
                        if (i==layoutPosition) {
                            isChecks[i]=true;
                        }else {
                            isChecks[i]=false;
                        }
                    }
                    notifyItemChanged(layoutPosition);
                    notifyItemChanged(oldPos);
                    oldPos=layoutPosition;

                    mOnItemClickListener.onItemClick(v,layoutPosition);
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
        public AccountViewHolder(View itemView) {
            super(itemView);
            tvName= (TextView) itemView.findViewById(R.id.tv_account_name);
        }

    }


}
