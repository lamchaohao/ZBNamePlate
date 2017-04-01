package com.gzzb.zbnameplate.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gzzb.zbnameplate.R;

import java.util.List;

/**
 * Created by Lam on 2017/3/24.
 */

public class NameAdapter extends RecyclerView.Adapter {

    private List<String> mReadNames;
    private Context mContext;
    private Listener.OnItemClickListener mItemListener;

    public void setItemListener(Listener.OnItemClickListener itemListener) {
        mItemListener = itemListener;
    }


    public NameAdapter(List<String> names, Context context) {
        mReadNames = names;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.from(mContext).inflate(R.layout.content_textview, parent, false);
        NameHolder viewHolder = new NameHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final NameHolder nameHolder= (NameHolder) holder;
        if (!TextUtils.isEmpty(mReadNames.get(position))){
            nameHolder.textView.setText(mReadNames.get(position));
        }
        if (mItemListener!=null){
            nameHolder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = nameHolder.getLayoutPosition();
                    mItemListener.onItemClick(v,layoutPosition);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mReadNames.size();
    }

    class NameHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public NameHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textView);
        }

    }



}
