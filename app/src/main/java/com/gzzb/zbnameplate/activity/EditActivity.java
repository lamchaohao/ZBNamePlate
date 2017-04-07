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
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.gzzb.zbnameplate.App;
import com.gzzb.zbnameplate.R;
import com.gzzb.zbnameplate.bean.Account;
import com.gzzb.zbnameplate.dao.AccountDao;
import com.gzzb.zbnameplate.global.Global;
import com.gzzb.zbnameplate.utils.connect.SendDataUtil;
import com.gzzb.zbnameplate.utils.genfile.DrawBitmapUtil;
import com.gzzb.zbnameplate.utils.genfile.GenFileUtil;
import com.gzzb.zbnameplate.view.photoView.PhotoView;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.gzzb.zbnameplate.global.Global.CONNECT_NORESPONE;
import static com.gzzb.zbnameplate.global.Global.CONN_ERRO;
import static com.gzzb.zbnameplate.global.Global.GENFILE_DONE;
import static com.gzzb.zbnameplate.global.Global.UPDATE_PROGRESS;
import static com.gzzb.zbnameplate.global.Global.View_Cool;
import static com.gzzb.zbnameplate.global.Global.WIFI_ERRO;

;

public class EditActivity extends BaseActivity implements View.OnClickListener {

    private static final int SELECT_FONT_CODE = 770;
    @BindView(R.id.et_nameEdit)
    EditText mEtNameEdit;
    @BindView(R.id.bt_fgText_font)
    Button btSetFont;
    @BindView(R.id.ib_fgText_setBold)
    ImageButton mIbSetBold;
    @BindView(R.id.ib_fgText_setItalic)
    ImageButton mIbSetItalic;
    @BindView(R.id.ib_fgText_setUnderLine)
    ImageButton mIbSetUnderLine;
    @BindView(R.id.bt_send)
    Button mBtSend;
    private EditText mEtName;
    private PhotoView mIvPreview;
    private float mTextSize = 16;
    private final int mWidth = 64;
    private final int mHeight = 16;
    private float mBaseY = 14;
    private String mName;
    private boolean mIsBold;
    private boolean mIsItalic;
    private boolean mIsUnderline;
    private AccountDao mAccountDao;
    private Account mAccount;
    private File mTypeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);
        initView();
        loadData();
        drawText();
    }

    private void loadData() {
        long accountId = getIntent().getLongExtra(Global.EX_ACCOUNT_ID, -1);
        mAccountDao = ((App) getApplication()).getDaoSession().getAccountDao();
        List<Account> list = mAccountDao.queryBuilder().where(AccountDao.Properties.Id.eq(accountId)).list();
        if (list.size() == 1) {
            mAccount = list.get(0);
            mName = mAccount.getAccountName();
            mEtName.setText(mName);
            mIsBold = mAccount.getIsBold();
            mIsItalic = mAccount.getIsItalic();
            mIsUnderline = mAccount.getIsUnderline();
            mTypeface = mAccount.getTypeface();
            if (mTypeface!=null&& !TextUtils.isEmpty(mTypeface.getAbsolutePath())&&mTypeface.exists()){
                int lastIndexOf = mTypeface.getName().lastIndexOf(".");
                String fontFileName = mTypeface.getName().substring(0, lastIndexOf);
                btSetFont.setText(fontFileName);
            }
        } else {
            mAccount = new Account();
        }

    }

    private void initView() {
        mIvPreview = (PhotoView) findViewById(R.id.iv_preview);
        mIvPreview.enable();
        //设置缩放倍数
        mIvPreview.setMaxScale(8);
        mIvPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);

        mEtName = (EditText) findViewById(R.id.et_nameEdit);
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
                if (mName.contains("y") || mName.contains("g"))
                    mBaseY = 12;
                else
                    mBaseY = 14;
                drawText();
            }
        });
    }

    @OnClick({R.id.ib_fgText_setBold, R.id.ib_fgText_setItalic, R.id.ib_fgText_setUnderLine, R.id.bt_send,R.id.bt_fgText_font})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_send:
                DrawBitmapUtil drawBitmapUtil = new DrawBitmapUtil(this, mAccount);
                Bitmap bitmap = drawBitmapUtil.drawBitmap();
                GenFileUtil genFileUtil = new GenFileUtil(this, bitmap, mHandler);
                genFileUtil.startGenFile();
                Snackbar.make(mEtName, "开始发送 ", Snackbar.LENGTH_SHORT).show();
                mBtSend.setEnabled(false);
                mHandler.sendEmptyMessageDelayed(View_Cool, 1000);
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
            case R.id.bt_fgText_font:
                Intent intent = new Intent(this,SelectFontActivity.class);
                startActivityForResult(intent,SELECT_FONT_CODE);
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
        if (mTypeface!=null&& !TextUtils.isEmpty(mTypeface.getAbsolutePath())&&mTypeface.exists()) {
            Typeface typeface = Typeface.createFromFile(mTypeface);
            paint.setTypeface(typeface);
            if (mIsBold) {//粗体
                paint.setTypeface(Typeface.create(typeface,Typeface.BOLD));
            }
            if (mIsItalic) {//斜体
                paint.setTypeface(Typeface.create(typeface,Typeface.ITALIC));
            }
            if (mIsBold && mIsItalic){//粗斜体
                paint.setTypeface(Typeface.create(typeface,Typeface.BOLD_ITALIC));
            }
        }else {
            if (mIsBold) {//粗体
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            }
            if (mIsItalic) {//斜体
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
            }
            if (mIsBold && mIsItalic) {//粗斜体
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
            }
            if (mIsUnderline) {//下划线
                paint.setUnderlineText(true);
            } else {
                paint.setUnderlineText(false);
            }
        }

        paint.setTextAlign(Paint.Align.LEFT);
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_4444);

        float drawWidth = computeWidth(mName, paint);
        int tempWidth = 0;
        if (drawWidth > mWidth) {
            tempWidth = (int) drawWidth;
            bitmap = Bitmap.createBitmap(tempWidth, mHeight, Bitmap.Config.ARGB_4444);
            if (bitmap != null)
                canvas.setBitmap(bitmap);
        } else {
            tempWidth = mWidth;
            if (bitmap != null)
                canvas.setBitmap(bitmap);
        }
        drawBG(tempWidth, mHeight, canvas);
        //文本
        if (tempWidth <= mWidth) {
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(mName, mWidth / 2, mBaseY, paint);
        } else {
            canvas.drawText(mName, 0, mBaseY, paint);
        }
        mIvPreview.setImageBitmap(bitmap);
    }

    private void drawBG(float width, float height, Canvas canvas) {
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.BLACK);
        canvas.drawRect(0, 0, width, height, bgPaint);
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

    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GENFILE_DONE:
                    SendDataUtil sendDataUtil = new SendDataUtil(EditActivity.this, this);
                    sendDataUtil.send();
                    break;
                case UPDATE_PROGRESS:
                    int arg1 = msg.arg1;
                    if (arg1 == 100) {
                        Snackbar.make(mEtName, "已发送 ", Snackbar.LENGTH_SHORT).show();
                    }

                    break;
                case WIFI_ERRO:
                    Toast.makeText(EditActivity.this, "所连接WiFi非本公司产品，请切换WiFi", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    break;
                case CONN_ERRO:
                    Snackbar.make(mEtName, "连接超时，请重试 ", Snackbar.LENGTH_SHORT).show();
                    break;
                case CONNECT_NORESPONE:
                    Snackbar.make(mEtName, "无响应，请稍后重试 ", Snackbar.LENGTH_LONG).show();
                    break;
                case View_Cool:
                    mBtSend.setEnabled(true);
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==RESULT_OK&&requestCode==SELECT_FONT_CODE) {
            String fileName = data.getStringExtra(Global.EX_setelctFont);
            File fontFile = new File(fileName);
            if (fontFile.exists()) {
                mTypeface = fontFile;
                int lastIndexOf = fontFile.getName().lastIndexOf(".");
                String fontFileName = fontFile.getName().substring(0, lastIndexOf);
                btSetFont.setText(fontFileName);
                mAccount.setTypeface(fontFile);
                drawText();
            }
        }
    }

    @Override
    public void onBackPressed() {
        mAccountDao.insertOrReplace(mAccount);
        Intent intent = new Intent();
        intent.putExtra(Global.EX_NEW_NAME, mAccount.getAccountName());
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }


}
