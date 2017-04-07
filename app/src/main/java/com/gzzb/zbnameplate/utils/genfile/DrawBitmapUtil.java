package com.gzzb.zbnameplate.utils.genfile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;

import com.gzzb.zbnameplate.bean.Account;
import com.gzzb.zbnameplate.global.Global;

import java.io.File;


public class DrawBitmapUtil {
    private boolean mIsMoveLeft;
    private String mName;
    private int mBaseX = 0;
    private int mBaseY = 14;
    private final int mTextSize=16;
    private final int mWidth =64;
    private final int mHeight=16;
    private boolean mIsBold;
    private boolean mIsItalic;
    private boolean mIsUnderline;
    private File mTypeface;
    public DrawBitmapUtil(Context context,String name) {
        mName=name;
        mIsMoveLeft = context.getSharedPreferences(Global.SP_SYSTEM, Context.MODE_PRIVATE).getBoolean(Global.KEY_MOVE_Effect, false);
        if (mName.contains("g")||mName.contains("y")){
            mBaseY=12;
        }else {
            mBaseY = 14;
        }
    }

    public DrawBitmapUtil(Context context,Account account) {
        mName = account.getAccountName();
        mIsBold = account.getIsBold();
        mIsItalic = account.getIsItalic();
        mIsUnderline = account.getIsUnderline();
        mTypeface = account.getTypeface();
        mIsMoveLeft = context.getSharedPreferences(Global.SP_SYSTEM, Context.MODE_PRIVATE).getBoolean(Global.KEY_MOVE_Effect, false);
        if (mName.contains("g")||mName.contains("y")){
            mBaseY=12;
        }else {
            mBaseY = 14;
        }

    }

    public Bitmap drawBitmap() {
        Bitmap bitmap = drawText();
        int width = bitmap.getWidth();
        if (width>mWidth&&mIsMoveLeft){
            Bitmap space = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_4444);
            Bitmap leftBitmap = mergeBitmap_LR(space, bitmap);
            return mergeBitmap_LR(leftBitmap, leftBitmap);
        }else {
            return bitmap;
        }

    }

    private Bitmap drawText() {
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
        }
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
        //文本
        if (tempWidth<=mWidth){
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(mName, mWidth/2, mBaseY, paint);
        }else {
            canvas.drawText(mName, mBaseX, mBaseY, paint);
        }

        return bitmap;
    }

    private float computeWidth(String text, Paint paint) {
        float drawWidth = mBaseX;
        float[] widths = new float[text.length()];
        paint.getTextWidths(text, widths);
        for (int i = 0; i < widths.length; i++) {
            drawWidth += widths[i];
        }
        return drawWidth;
    }

    /**
     * 把两个位图覆盖合成为一个位图，左右拼接
     * @param leftBitmap
     * @param rightBitmap
     * @return
     */
    public Bitmap mergeBitmap_LR(Bitmap leftBitmap, Bitmap rightBitmap) {

        if (leftBitmap == null || leftBitmap.isRecycled()
                || rightBitmap == null || rightBitmap.isRecycled()) {
            Log.w("bitmaperro", "leftBitmap=" + leftBitmap + ";rightBitmap=" + rightBitmap);
            return null;
        }
        
        // 拼接后的宽度
        int width = leftBitmap.getWidth() + rightBitmap.getWidth();

        // 定义输出的bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, mHeight, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);

        // 缩放后两个bitmap需要绘制的参数
        Rect leftRect = new Rect(0, 0, leftBitmap.getWidth(), leftBitmap.getHeight());
        Rect rightRect  = new Rect(0, 0, rightBitmap.getWidth(), rightBitmap.getHeight());

        // 右边图需要绘制的位置，往右边偏移左边图的宽度，高度是相同的
        Rect rightRectT  = new Rect(leftBitmap.getWidth(), 0, width, mHeight);

        canvas.drawBitmap(leftBitmap, leftRect, leftRect, null);
        canvas.drawBitmap(rightBitmap, rightRect, rightRectT, null);
        return bitmap;
    }


    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setBold(boolean bold) {
        mIsBold = bold;
    }

    public void setItalic(boolean italic) {
        mIsItalic = italic;
    }

    public void setUnderline(boolean underline) {
        mIsUnderline = underline;
    }

    public void setTypeface(File typeface) {
        mTypeface = typeface;
    }
}
