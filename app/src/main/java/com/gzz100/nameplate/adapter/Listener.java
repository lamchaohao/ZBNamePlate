package com.gzz100.nameplate.adapter;

import android.view.View;
import android.widget.SeekBar;

import com.gzz100.nameplate.bean.Account;


/**
 * Created by Lam on 2017/3/28.
 */

public class Listener {
    /**
     * 点击事件监听接口
     */
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
    public interface OnPlayClickListener{
        void onPlayClick(View view, int position);
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

    public interface OnAddOnClickListener{
        void onAddItemClick(View view, int position);
    }

    public interface OnProgressChangedListener{
        void onProgressChanged(SeekBar seekBar,int position, int progress);
    }

    public interface OnUpwardListener{
        void onUpTap(Account account, int position);
    }
    public interface OnDownListener{
        void onDownTap(Account account,int position);
    }
}
