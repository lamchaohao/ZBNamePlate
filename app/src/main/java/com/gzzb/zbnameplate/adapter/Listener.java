package com.gzzb.zbnameplate.adapter;

import android.view.View;
import android.widget.SeekBar;

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
}