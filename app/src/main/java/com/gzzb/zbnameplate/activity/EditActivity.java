package com.gzzb.zbnameplate.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.gzzb.zbnameplate.App;
import com.gzzb.zbnameplate.R;
import com.gzzb.zbnameplate.bean.Account;
import com.gzzb.zbnameplate.dao.AccountDao;
import com.gzzb.zbnameplate.global.Global;
import com.gzzb.zbnameplate.utils.genfile.DrawBitmapUtil;
import com.gzzb.zbnameplate.utils.genfile.GenFileUtil;
import com.gzzb.zbnameplate.view.photoView.PhotoView;

import java.util.List;

;

public class EditActivity extends BaseActivity implements View.OnClickListener{

    private EditText mEtName;
    private PhotoView mIvPreview;
    private float mTextSize =16;
    private final int mWidth = 64;
    private final int mHeight = 16;
    private float mBaseY = 14;
    private String mName;
    private boolean mIsBold;
    private boolean mIsItalic;
    private boolean mIsUnderline;
    private AccountDao mAccountDao;
    private Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        initView();
        loadData();
        drawText();
    }

    private void loadData() {
        long accountId = getIntent().getLongExtra(Global.EX_ACCOUNT_ID, -1);
        mAccountDao = ((App) getApplication()).getDaoSession().getAccountDao();
        List<Account> list = mAccountDao.queryBuilder().where(AccountDao.Properties.Id.eq(accountId)).list();
        if (list.size()==1) {
            mAccount = list.get(0);
            mName= mAccount.getAccountName();
            mEtName.setText(mName);
            mIsBold = mAccount.getIsBold();
            mIsItalic = mAccount.getIsItalic();
            mIsUnderline =mAccount.getIsUnderline();
        }else {
            mAccount=new Account();
        }

    }

    private void initView() {
        mIvPreview = (PhotoView) findViewById(R.id.iv_preview);
        mIvPreview.enable();
        //设置缩放倍数
        mIvPreview.setMaxScale(8);
        mIvPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);

        mEtName = (EditText) findViewById(R.id.et_nameEdit);
        Button btSend = (Button) findViewById(R.id.bt_send);
        ImageView ibSetBold = (ImageView)findViewById(R.id.ib_fgText_setBold);
        ImageView ibSetItalic = (ImageView)findViewById(R.id.ib_fgText_setItalic);
        ImageView ibSetUnderLine = (ImageView) findViewById(R.id.ib_fgText_setUnderLine);

        ibSetBold.setOnClickListener(this);
        ibSetItalic.setOnClickListener(this);
        ibSetUnderLine.setOnClickListener(this);

        btSend.setOnClickListener(this);
        mEtName.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mName = s.toString();
                mAccount.setAccountName(mName);
                if(mName.contains("y")|| mName.contains("g"))
                    mBaseY=12;
                else
                    mBaseY=14;
                drawText();
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_send:
                String name = mEtName.getText().toString();
                DrawBitmapUtil drawBitmapUtil = new DrawBitmapUtil(name);
                drawBitmapUtil.setItalic(mIsItalic);
                drawBitmapUtil.setBold(mIsBold);
                drawBitmapUtil.setUnderline(mIsUnderline);
                Bitmap bitmap = drawBitmapUtil.drawBitmap();
                GenFileUtil genFileUtil = new GenFileUtil(this,bitmap,null);
                genFileUtil.startGenFile();
                break;
            case R.id.ib_fgText_setBold:
                mIsBold = !mIsBold;
                mAccount.setIsBold(mIsBold);
                drawText();
                break;
            case R.id.ib_fgText_setItalic:
                mIsItalic = !mIsItalic;
                mAccount.setIsItalic(mIsItalic);
                drawText();
                break;
            case R.id.ib_fgText_setUnderLine:
                mIsUnderline = !mIsUnderline;
                mAccount.setIsUnderline(mIsUnderline);
                drawText();
                break;

        }

    }

    private void drawText() {
        Paint paint = new Paint();
        Canvas canvas = new Canvas();

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

        //如果图片比所设置的宽，则需加长
        paint.setColor(Color.RED);
        paint.setTextSize(mTextSize);
        if (mIsBold) {//粗体
            paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        }
        if (mIsItalic) {//斜体
            paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.ITALIC));
        }
        if (mIsBold&& mIsItalic){//粗斜体
            paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD_ITALIC));
        }
        if (mIsUnderline) {//下划线
            paint.setUnderlineText(true);
        }else {
            paint.setUnderlineText(false);
        }
        paint.setTextAlign(Paint.Align.LEFT);
        Bitmap bitmap = Bitmap.createBitmap(mWidth,mHeight, Bitmap.Config.ARGB_4444);

        float drawWidth = computeWidth(mName, paint);
        int tempWidth=0;
        if (drawWidth > mWidth) {
            tempWidth = (int) drawWidth;
            bitmap = Bitmap.createBitmap(tempWidth, mHeight, Bitmap.Config.ARGB_4444);
            if (bitmap != null)
                canvas.setBitmap(bitmap);
        }else {
            tempWidth = mWidth;
            if (bitmap != null)
                canvas.setBitmap(bitmap);
        }
        drawBG(tempWidth,mHeight,canvas);
        //文本
        if (tempWidth<=mWidth){
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(mName, mWidth/2, mBaseY, paint);
        }else {
            canvas.drawText(mName, 0, mBaseY, paint);
        }
        mIvPreview.setImageBitmap(bitmap);
    }

    private void drawBG(float width,float height,Canvas canvas) {
        Paint bgPaint=new Paint();
        bgPaint.setColor(Color.BLACK);
        canvas.drawRect(0,0,width,height,bgPaint);
    }


    private float computeWidth(String text, Paint paint) {
        float drawWidth = 0;
        float[] widths = new float[text.length()];
        paint.getTextWidths(text, widths);
        for (int i = 0; i < widths.length; i++) {
            drawWidth += widths[i];
        }
        return drawWidth;
    }


    @Override
    public void onBackPressed() {
        mAccountDao.insertOrReplace(mAccount);
        Intent intent =new Intent();
        intent.putExtra(Global.EX_NEW_NAME,mAccount.getAccountName());
        setResult(RESULT_OK,intent);
        super.onBackPressed();
    }
}
