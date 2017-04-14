package com.gzz100.nameplate.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.gzz100.nameplate.R;


public class HelpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
    }

    @Override
    public void onCreateCustomToolBar(Toolbar toolbar) {
        toolbar.setTitle(R.string.use_help);
    }
}
