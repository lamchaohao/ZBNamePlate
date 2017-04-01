package com.gzzb.zbnameplate.utils.genfile;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;


public class DrawBitmapUtil {
    private String mName;
    private int mBaseX = 0;
    private int mBaseY = 14;
    private final int mTextSize=16;
    private final int mWidth =64;
    private final int mHeight=16;
    private boolean mIsBold;
    private boolean mIsItalic;
    private boolean mIsUnderline;

    public DrawBitmapUtil(String name) {
        mName=name;
        if (mName.contains("g")||mName.contains("y")){
            mBaseY=12;
        }else {
            mBaseY = 14;
        }
    }

    public Bitmap drawBitmap() {

        return drawText();
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
}
