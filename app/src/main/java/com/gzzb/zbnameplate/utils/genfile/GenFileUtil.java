package com.gzzb.zbnameplate.utils.genfile;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import com.gzzb.zbnameplate.utils.connect.SendDataUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.gzzb.zbnameplate.global.Global.GENFILE_DONE;

/**
 * Created by Lam on 2017/2/15.
 */

public class GenFileUtil {
    private byte[] mTextAttrs = new byte[6];
    private byte[] mFileHeadPart = new byte[5];
    private byte[] mItemPart = new byte[10];
    private byte[] mTextContent;
    private Bitmap mBitmap;

    private byte picStyle;

    private List<byte[]> mTimeAxisList;

    private File mColorPRG;
    private byte[] mBlackBG;
    private int mFrameCount;
    private Context mContext;
    private byte[] mColByteCount;
    private int mScreenWidth=64;
    private int mScreenHeight=16;

    private Handler mHandler;

    public GenFileUtil(Context context, Bitmap bitmap,Handler handler) {
        mContext = context;
        mBitmap = bitmap;
        if (handler!=null) {
            mHandler = handler;
        }else {
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case GENFILE_DONE:
                            SendDataUtil sendDataUtil = new SendDataUtil(mContext,null);
                            sendDataUtil.send();
                            break;
                    }

                }
            };
        }
    }

    private void initFileHead() {
        byte fileHeadLength = 4;     //总头长度
        byte mItemCount = 1;        //节目个数
        byte mItemTableLength = 10;  //节目表长度
        byte mFrameHeadLength = 16;  //帧头长度
        byte mTextAttrsLength = 6;    //字层性长度
        mFileHeadPart[0] = fileHeadLength;
        mFileHeadPart[1] = mItemCount;
        mFileHeadPart[2] = mItemTableLength;
        mFileHeadPart[3] = mFrameHeadLength;
        mFileHeadPart[4] = mTextAttrsLength;
    }

    private void initTextContent() {

        byte[] bitmapPixels = BitmapToPixelUtil.convertBitmapToPixel(mBitmap);

        CompressAlgorithm compressAlgorithm = new CompressAlgorithm();
        List<Byte> compress = compressAlgorithm.compress(bitmapPixels, mBitmap.getWidth(), mScreenHeight);//进行压缩
        mTextContent = new byte[compress.size()];
        for (int i = 0; i < compress.size(); i++) {
            mTextContent[i] = compress.get(i);
        }
        mColByteCount = compressAlgorithm.getColByteCount();
    }

    private void initBlackBG() {
        byte count = (byte) (mScreenHeight - 8);
        byte blackColor = 64;
        mBlackBG = new byte[3 * (mScreenWidth)];
        int index = 0;
        for (int i = 0; i < mBlackBG.length; i++) {
            mBlackBG[i] = blackColor;
            index++;
            if (index % 3 == 0) {
                mBlackBG[i] = count;
            }
        }
    }

    private void initItemPart() {
        //帧数是图片的宽度加上后面张与屏宽一致的黑色图片
        mFrameCount = mBitmap.getWidth()-mScreenWidth;
        byte[] frameCountByte=null;
        if (mFrameCount<=0){
            mFrameCount= 1;
            frameCountByte = intToByteArray(mFrameCount, 2);
        }else {
            mFrameCount=mFrameCount*2;
            frameCountByte = intToByteArray(mFrameCount+16, 2);//到最后的时候停留一下
        }

        setInbyteArray(1,frameCountByte,mItemPart);
        int timeAxisAddress=mFileHeadPart.length+mItemPart.length+mTextAttrs.length+mBlackBG.length+mTextContent.length;
        setInbyteArray(6,intToByteArray(timeAxisAddress, 4),mItemPart);
    }

    private void initTextAttrs() {
        //文字属性区 6byte

        byte textStyle=1;//直贴
        byte[] screenStartAddress = intToByteArray(0, 2);
        byte[] screenWidthByte = intToByteArray(mScreenWidth, 2);
        byte screenHeightByte= (byte) mScreenHeight;

        mTextAttrs[0] = textStyle;
        setInbyteArray(1,screenStartAddress,mTextAttrs);
        setInbyteArray(3,screenWidthByte,mTextAttrs);
        mTextAttrs[5] = screenHeightByte;

    }


    private void initTimeAxis() {
        mTimeAxisList = new ArrayList<>();
        if (mFrameCount==1){
            initOneFrame();
        }else {
            initMoreFrame();
        }
    }

    private void initMoreFrame() {
        //文字属性地址 3byte
        int attrStartAddress=mFileHeadPart.length+mItemPart.length;
        //文字地址
        int textContentAddressInt=mFileHeadPart.length+mItemPart.length+mTextAttrs.length+mBlackBG.length;
        picStyle= 1;//1BIT地址指向(0=图层,1=跳转地址),7BIT未用
        int tempColbyteCount=0;

        for (int i = 0; i<mFrameCount/2; i++){
            byte[] timeAxis=new byte[16];
            //时间
            timeAxis[0]=60;
            timeAxis[1] = picStyle;
            //字属性地址
            int tempTextAddress=0;
            //字内容地址 4byte
            if (i==0) {
                tempColbyteCount=0;
            }else {
                tempColbyteCount+=mColByteCount[i-1];
            }
            tempTextAddress=textContentAddressInt+tempColbyteCount;

            byte[] picAddress = intToByteArray(textContentAddressInt-mBlackBG.length, 4);//当方式为跳向指定帧时这个地址是指向时间轴上的一个时间点(头0开始)
            byte[] atrrAddress=intToByteArray(attrStartAddress,3);
            byte[] textContentAddress = intToByteArray(tempTextAddress, 4);
            byte[] clockOrTem = new byte[3];

            setInbyteArray(2,picAddress,timeAxis);
            setInbyteArray(6,atrrAddress,timeAxis);
            setInbyteArray(9,textContentAddress,timeAxis);
            setInbyteArray(13,clockOrTem,timeAxis);
            mTimeAxisList.add(timeAxis);
            if (i==(mFrameCount/2)-1){
                //如果是最后一帧，先停留8帧，然后右移
                for (int j = 0; j < 8; j++) {
                    mTimeAxisList.add(timeAxis);
                }
            }
        }
        //右移回去
        for (int i = (mFrameCount/2)-1; i>=0; i--){
            byte[] timeAxis=new byte[16];
            //时间
            timeAxis[0]=60;
            timeAxis[1] = picStyle;
            //字属性地址
            int tempTextAddress=0;
            //字内容地址 4byte
            if (i==0) {
                tempColbyteCount=0;
            }else {
                tempColbyteCount-=mColByteCount[i-1];
            }
            tempTextAddress=textContentAddressInt+tempColbyteCount;
            byte[] picAddress = intToByteArray(textContentAddressInt-mBlackBG.length, 4);//当方式为跳向指定帧时这个地址是指向时间轴上的一个时间点(头0开始)
            byte[] atrrAddress=intToByteArray(attrStartAddress,3);
            byte[] textContentAddress = intToByteArray(tempTextAddress, 4);
            byte[] clockOrTem = new byte[3];

            setInbyteArray(2,picAddress,timeAxis);
            setInbyteArray(6,atrrAddress,timeAxis);
            setInbyteArray(9,textContentAddress,timeAxis);
            setInbyteArray(13,clockOrTem,timeAxis);

            mTimeAxisList.add(timeAxis);
            if (i==0){
                //如果是最后一帧，先停留8帧，然后左移
                for (int j = 0; j < 8; j++) {
                    mTimeAxisList.add(timeAxis);
                }
            }
        }
    }

    private void initOneFrame() {
        //文字属性地址 3byte
        int attrStartAddress=mFileHeadPart.length+mItemPart.length;
        //文字地址
        int textContentAddressInt=mFileHeadPart.length+mItemPart.length+mTextAttrs.length+mBlackBG.length;
        picStyle= 1;//1BIT地址指向(0=图层,1=跳转地址),7BIT未用
        int tempColbyteCount=0;

        for (int i = 0; i<1; i++){
            byte[] timeAxis=new byte[16];
            //时间
            timeAxis[0]=120;
            timeAxis[1] = picStyle;
            //字属性地址
            int tempTextAddress=0;
            //字内容地址 4byte
            if (i==0) {
                tempColbyteCount=0;
            }else {
                tempColbyteCount+=mColByteCount[i-1];
            }
            tempTextAddress=textContentAddressInt+tempColbyteCount;
            byte[] picAddress = intToByteArray(textContentAddressInt-mBlackBG.length, 4);//当方式为跳向指定帧时这个地址是指向时间轴上的一个时间点(头0开始)
            byte[] atrrAddress=intToByteArray(attrStartAddress,3);
            byte[] textContentAddress = intToByteArray(tempTextAddress, 4);
            byte[] clockOrTem = new byte[3];

            setInbyteArray(2,picAddress,timeAxis);
            setInbyteArray(6,atrrAddress,timeAxis);
            setInbyteArray(9,textContentAddress,timeAxis);
            setInbyteArray(13,clockOrTem,timeAxis);
            mTimeAxisList.add(timeAxis);
            for (int j = 0; j < 20; j++) {
                mTimeAxisList.add(timeAxis);
                mFrameCount++;
            }
        }
    }


    /**
     *
     * @param source 源数值
     * @param byteArrayLength 要转变成的byte数组长度
     * @return
     */
    private byte[] intToByteArray(int source,int byteArrayLength){
        byte[] result = new byte[byteArrayLength];
        for (int length = byteArrayLength,index=0; length > 0; length--,index++) {
            int bitCount = (length-1) * 8;
            int temp=source;
            temp = temp >> bitCount; //移位
            result[index] = (byte) (temp & 0xff);
        }
        return result;
    }

    /**
     *
     * @param targetStart 要赋值的目标数组的开始序列,从0开始
     * @param source 源数组
     * @param target 目标数组
     */
    private void setInbyteArray(int targetStart,byte[] source,byte[] target){
        for (int i = 0;i<source.length;i++){
            target[targetStart+i]=source[i];
        }
    }

    public void startGenFile(){
        new Thread(new GenFileThread()).start();
    }

    class GenFileThread implements Runnable{

        @Override
        public void run() {
            genFile();
        }
    }

    private void genFile(){

        initFileHead();
        initTextContent();
        initBlackBG();
        initTextAttrs();
        initItemPart();
        initTimeAxis();

        mColorPRG = new File(mContext.getFilesDir()+"/color.prg");
        if (mColorPRG.exists()) {
            mColorPRG.delete();
        }
        try {
            mColorPRG.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fos=null;
        try {
            fos=new FileOutputStream(mColorPRG,true);
            fos.write(mFileHeadPart);

            fos.write(mItemPart);

            fos.write(mTextAttrs);

            fos.write(mBlackBG);

            fos.write(mTextContent);

            for (int i = 0; i < mTimeAxisList.size(); i++) {
                fos.write(mTimeAxisList.get(i));
            }
            fos.flush();
            mHandler.sendEmptyMessage(GENFILE_DONE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fos!=null)
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

}

