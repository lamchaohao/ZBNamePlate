package com.gzzb.zbnameplate.utils.connect;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.os.Handler;

import com.gzzb.zbnameplate.global.Global;
import com.gzzb.zbnameplate.utils.system.WifiAdmin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gzzb.zbnameplate.global.Global.CONNECT_NORESPONE;
import static com.gzzb.zbnameplate.global.Global.PAUSE_FAILE;
import static com.gzzb.zbnameplate.global.Global.SEND_DONE;
import static com.gzzb.zbnameplate.global.Global.WIFI_ERRO;
import static com.gzzb.zbnameplate.utils.genfile.ByteUtil.intToByteArray;
import static com.gzzb.zbnameplate.utils.genfile.ByteUtil.setInbyteArray;

/**
 * 发送设屏参设置
 * Created by Lam on 2017/3/13.
 */

public class SendBrightnessUtil {

    private Context mContext;
    private Handler mHandler;
    private int mBrightness;

    public SendBrightnessUtil(Context context, Handler handler,int brightness) {
        mContext=context;
        mHandler = handler;
        mBrightness = brightness;
    }

    public void startSendData(){
        new Thread(){
            @Override
            public void run() {
                sendSetData();
            }
        }.start();
    }

    private void sendSetData(){
        Socket socket = null;
        OutputStream os =null;
        InputStream fis = null;
        WifiAdmin wifiAdmin =new WifiAdmin(mContext);
        WifiInfo wifiInfo = wifiAdmin.getWifiInfo();
        String ssid = wifiInfo.getSSID();
        String macStr = "";
        boolean startFlag = ssid.contains(Global.SSID_START);
        boolean endFlag = ssid.contains(Global.SSID_END);
        if (startFlag&&endFlag){
            macStr = ssid.substring(ssid.indexOf("[")+1, ssid.indexOf("]"));
        }else {
            mHandler.sendEmptyMessage(WIFI_ERRO);
            return;
        }
        String regEx = "[0-9a-fA-F]{6}";
        Pattern pat = Pattern.compile(regEx);
        Matcher mat = pat.matcher(macStr);
        //旧的8位
        String regExEight = "[0-9a-fA-F]{8}";
        Pattern patEight = Pattern.compile(regExEight);
        Matcher matcEight = patEight.matcher(macStr);
        String mac1;
        String mac2;
        String mac3;
        String mac4;
        if(mat.matches()){
            mac1 = "80";
            mac2 = macStr.substring(0, 2);
            mac3 = macStr.substring(2, 4);
            mac4 = macStr.substring(4, 6);

        }else if (matcEight.matches()){
            mac1 = macStr.substring(0, 2);
            mac2 = macStr.substring(2, 4);
            mac3 = macStr.substring(4, 6);
            mac4 = macStr.substring(6, 8);
        } else{
            mHandler.sendEmptyMessage(WIFI_ERRO);
            return;
        }
        int macInt1 = Integer.parseInt(mac1, 16);
        int macInt2 = Integer.parseInt(mac2, 16);
        int macInt3 = Integer.parseInt(mac3, 16);
        int macInt4 = Integer.parseInt(mac4, 16);
        try {
            socket = new Socket(Global.SERVER_IP, Global.SERVER_PORT);

            byte[] pauseCMD = new byte[16];
            pauseCMD[0]= (byte) macInt1;
            pauseCMD[1]= (byte) macInt2;
            pauseCMD[2]= (byte) macInt3;
            pauseCMD[3]= (byte) macInt4;
            pauseCMD[4]= 16;
            pauseCMD[11]= 8; //cmd

            byte[] resetCMD=new byte[16];
            resetCMD[0]= (byte) macInt1;
            resetCMD[1]= (byte) macInt2;
            resetCMD[2]= (byte) macInt3;
            resetCMD[3]= (byte) macInt4;
            resetCMD[4]= 16;
            resetCMD[11]= 4; //cmd

            byte[] writeCMD = new byte[16];
            writeCMD[0]= (byte) macInt1;
            writeCMD[1]= (byte) macInt2;
            writeCMD[2]= (byte) macInt3;
            writeCMD[3]= (byte) macInt4;
            writeCMD[4]= 16;
            writeCMD[11]= 21; //read cmd 0x00010001  write cmd=0x00010101;

            os = socket.getOutputStream();
            byte[] readMsg = new byte[16];

            //执行暂停指令
            os.write(pauseCMD);
            socket.getInputStream().read(readMsg);//读取返回
            boolean pauseSuccess = true;
            for (int i = 0; i < readMsg.length; i++) {
                if(pauseCMD[i]!=readMsg[i]){
                    mHandler.sendEmptyMessageDelayed(PAUSE_FAILE,1500);
                    pauseSuccess = false;
                }
            }
            if (pauseSuccess) {
                //暂停成功，开始写入
                byte[] feedbackcmd=new byte[16];
                int length =512;
                int serialNum = 4;
                int flashAddress=129024;

                fis= mContext.getAssets().open("trace/line004.dot");

                byte[] traceFileBytes=new byte[512];
                int read = fis.read(traceFileBytes);

                for (int i = 0; i < 4; i++) {
                    serialNum--;
                    byte[] sendPack = new byte[16+512];
                    byte[] dataPackLength = intToByteArray(length, 3);
                    byte[] serialNumBytes = intToByteArray(serialNum, 3);
                    byte[] flashAddBytes = intToByteArray(flashAddress, 4);
                    setInbyteArray(5,dataPackLength,writeCMD);//包长度
                    setInbyteArray(8,serialNumBytes,writeCMD); //包序
                    setInbyteArray(12,flashAddBytes,writeCMD); //flash地址
                    byte[] dataBytes = null;
                    if (i==0){
                        dataBytes=genData();//写入第一区设置内容
                    }else if (i==1){
                        dataBytes=traceFileBytes;//写入走线记录表
                    }else {
                        dataBytes = new byte[512];
                    }
                    setInbyteArray(0,writeCMD,sendPack);
                    setInbyteArray(16,dataBytes,sendPack);
                    os.write(sendPack);
                    socket.getInputStream().read(feedbackcmd);
                    flashAddress += length;

                }
                os.write(resetCMD);
                socket.getInputStream().read(feedbackcmd);
                mHandler.sendEmptyMessage(SEND_DONE);
            }

        }catch (IOException e){
            e.printStackTrace();
            mHandler.sendEmptyMessage(CONNECT_NORESPONE);
        }finally {
            if (os!=null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (socket!=null&&!socket.isClosed()){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }




    private byte[] genData(){
        byte[] dataBytes =new byte[512];
        int height=16;
        int width=64;

        int picture =width*height;
        int foldCount = 1;
        int scanCount = 16;
        int line = width*foldCount;
        int output = height/(scanCount*foldCount);
        byte[] scanOrderArray=new byte[16];
        for (int i = 0; i < scanOrderArray.length; i++) {
            scanOrderArray[i]= (byte) i;
        }
        int route = 64;
        int batH = scanCount*foldCount;
        int batW = 64;

        String fileName = Global.HC1_FILENAME;
        byte[] fileBytes = fileName.getBytes();     //0-11 文件名
        setInbyteArray(0,fileBytes,dataBytes);
        dataBytes[19] = 97; //19 版本号
        dataBytes[24] = (byte) 195; //24 data&set C3

        byte[] pictureArray = intToByteArray(picture, 3);//实像素  32-34
        setInbyteArray(32,pictureArray,dataBytes);
        byte[] widthArray = intToByteArray(width, 2); //35-36 width
        setInbyteArray(35,widthArray,dataBytes);
        dataBytes[37]= (byte) height;  //37 height

        dataBytes[38]= (byte) scanCount; //38 扫描次数
        byte[] lineArray = intToByteArray(line, 2); //39-40 线带点数
        setInbyteArray(39,lineArray,dataBytes);
        dataBytes[41] = (byte) output; //41 输出端口
        dataBytes[42] = (byte) 0; //data相位
        dataBytes[43] = (byte) 0;  //oe

        int brightness=0;
        if (mBrightness<=33){
            brightness = -1;
        }else if (mBrightness>33&&mBrightness<66){
            brightness = 0;
        }else if (mBrightness>66&&mBrightness<=100){
            brightness = 1;
        }
        dataBytes[47] = (byte) brightness; //亮度

        setInbyteArray(48,scanOrderArray,dataBytes); //48-63扫行次序
        byte[] routeArray = intToByteArray(route, 2); //64-65 走线表点数
        setInbyteArray(64,routeArray,dataBytes);
        dataBytes[66] = (byte) batH; // 66 1个端口带高度
        dataBytes[67] = (byte) batW; // 67 一个模组宽度

        //----software part-----

        return dataBytes;
    }

    public void setBrightness(int brightness) {
        mBrightness = brightness;
    }
}
