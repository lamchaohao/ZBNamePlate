package com.gzz100.nameplate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.gzz100.nameplate.R;
import com.gzz100.nameplate.adapter.TypefaceAdapter;
import com.gzz100.nameplate.bean.TypefaceFile;
import com.gzz100.nameplate.global.Global;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class SelectFontActivity extends BaseActivity {

    private List<TypefaceFile> mFileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_account);
        loadData();
        initView();
    }

    private void loadData() {
        File file =new File(Global.FL_SYSTEMFONT);
        File[] files = file.listFiles();
        File downloadFontDir = new File(Environment.getExternalStorageDirectory()+Global.FL_FONTS_FOLDER);
        if (!downloadFontDir.exists()){
            downloadFontDir.mkdirs();
        }
        File[] downloadFonts = downloadFontDir.listFiles();
        mFileList = new ArrayList<>();
        if(downloadFonts!=null)
            for (File downloadFont : downloadFonts) {
                mFileList.add(new TypefaceFile(downloadFont,false));
            }
        for (int i=0;i<files.length;i++){
            String name = files[i].getName();
            if(name.contains("-Regular")&&!name.contains("MiuiEx")){
                mFileList.add(new TypefaceFile(files[i],false));
            }
        }
    }

    private void initView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rcv_selectAccount);
        TypefaceAdapter adapter = new TypefaceAdapter(mFileList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnItemClickListener(new TypefaceAdapter.OnItemOnClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //1.先获取选择了哪个字体
                File file = mFileList.get(position).getFile();
                Intent intent = new Intent();
                intent.putExtra(Global.EX_setelctFont,file.getAbsolutePath());
                setResult(RESULT_OK,intent);
                SelectFontActivity.this.finish();
            }
        });
    }

    @Override
    public void onCreateCustomToolBar(Toolbar toolbar) {
        toolbar.setTitle(R.string.selectFont);
    }
}
