package com.gzz100.nameplate.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.gzz100.nameplate.R;
import com.gzz100.nameplate.global.Global;

import static com.gzz100.nameplate.global.Global.KEY_MOVE_Effect;

public class SelectEffectActivity extends BaseActivity {

    private SharedPreferences.Editor mEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_effect);
        initView();
    }

    private void initView() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_effect);
        RadioButton moveLeft = (RadioButton) findViewById(R.id.rb_effect_leftmove);
        RadioButton rightAndleft = (RadioButton) findViewById(R.id.rb_effect_leftandrRight);
        mEdit = getSharedPreferences(Global.SP_SYSTEM, MODE_PRIVATE).edit();

        SharedPreferences sp = getSharedPreferences(Global.SP_SYSTEM, MODE_PRIVATE);
        if (sp.getBoolean(KEY_MOVE_Effect,false)) {
            moveLeft.setChecked(true);
        }else{
            rightAndleft.setChecked(true);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_effect_leftmove:
                        mEdit.putBoolean(KEY_MOVE_Effect,true).apply();
                        break;
                    case R.id.rb_effect_leftandrRight:
                        mEdit.putBoolean(KEY_MOVE_Effect,false).apply();
                        break;
                }
            }
        });
    }

    @Override
    public void onCreateCustomToolBar(Toolbar toolbar) {
        toolbar.setTitle(R.string.selectEffect);
    }
}
