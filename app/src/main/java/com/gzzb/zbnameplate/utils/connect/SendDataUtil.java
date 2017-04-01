package com.gzzb.zbnameplate.utils.connect;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.widget.Toast;

import com.gzzb.zbnameplate.global.Global;
import com.gzzb.zbnameplate.utils.system.WifiAdmin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gzzb.zbnameplate.global.Global.CONNECT_NORESPONE;
import static com.gzzb.zbnameplate.global.Global.UPDATE_PROGRESS;
import static com.gzzb.zbnameplate.global.Global.WIFI_ERRO;

/**
 * Created by Lam on 2017/2/15.
 */

public class SendDataUtil {
    private Context mContext;
    private Handler mHandler;

    public SendDataUtil(Context context,Handler handler) {
        this.mContext=context;
        if (handler!=null){
            mHandler=handler;
        }else {
            mHandler=new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what){
                        case UPDATE_PROGRESS:
                            int arg1 = msg.arg1;
                            if (arg1==100){
                                Toast.makeText(mContext,"已发送",Toast.LENGTH_LONG).show();
                            }

                            break;
                        case WIFI_ERRO:
                            Toast.makeText(mContext,"所连接WiFi非本公司产品，请切换WiFi",Toast.LENGTH_LONG).show();
                            mContext.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            break;
                        case CONNECT_NORESPONE:
                            Toast.makeText(mContext,"连接错误，请重新连接屏幕",Toast.LENGTH_LONG).show();
                            break;
                    }

                }
            };
        }
    }

    public void send(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                sendFile();
            }
        }.start();
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

    private void sendFile(){
        Socket socket = null;
        FileInputStream fis = null;
        OutputStream os =null;
        WifiAdmin wifiAdmin =new WifiAdmin(mContext);
        WifiInfo wifiInfo = wifiAdmin.getWifiInfo();
        String ssid = wifiInfo.getSSID();
        String macStr = "";
        if (ssid.contains("HC-LED")){
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
        int macInt1 = 0;
        int macInt2 = 0;
        int macInt3 = 0;
        int macInt4 = 0;
        if(mat.matches()){
            String mac0 = "80";
            String mac1 = macStr.substring(0, 2);
            String mac2 = macStr.substring(2, 4);
            String mac3 = macStr.substring(4, 6);

            macInt1 = Integer.parseInt(mac0, 16);
            macInt2 = Integer.parseInt(mac1, 16);
            macInt3 = Integer.parseInt(mac2, 16);
            macInt4 = Integer.parseInt(mac3, 16);
        }else if (matcEight.matches()){
            String mac1 = macStr.substring(0, 2);
            String mac2 = macStr.substring(2, 4);
            String mac3 = macStr.substring(4, 6);
            String mac4 = macStr.substring(6, 8);

            macInt1 = Integer.parseInt(mac1, 16);
            macInt2 = Integer.parseInt(mac2, 16);
            macInt3 = Integer.parseInt(mac3, 16);
            macInt4 = Integer.parseInt(mac4, 16);
        } else{
            mHandler.sendEmptyMessage(WIFI_ERRO);
            return;
        }
        try {
            socket = new Socket(Global.SERVER_IP,Global.SERVER_PORT);
            File file =new File(mContext.getFilesDir()+"/color.prg");
            fis = new FileInputStream(file);
            os = socket.getOutputStream();
            byte[] buf = new byte[512];
            int len = 0 ;
            float progress=0;
            long fileLength = file.length();

            byte[] testCMD=new byte[16];
            testCMD[0]= (byte) macInt1;
            testCMD[1]= (byte) macInt2;
            testCMD[2]= (byte) macInt3;
            testCMD[3]= (byte) macInt4;
            testCMD[4]= 16;

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

            //执行测试指令
            os.write(testCMD);
            byte[] readMsg = new byte[16];
            socket.getInputStream().read(readMsg);//读取返回


            for (int i = 0; i < readMsg.length; i++) {
                if(testCMD[i]!=readMsg[i]){
                    mHandler.sendEmptyMessage(CONNECT_NORESPONE);
                }else {
                }
            }

            //执行暂停指令
            os.write(pauseCMD);
            socket.getInputStream().read(readMsg);//读取返回

            boolean pauseSuccess = true;
            for (int i = 0; i < readMsg.length; i++) {
                if(pauseCMD[i]!=readMsg[i]){
                    pauseSuccess = false;
                }
            }
            if (pauseSuccess) {
                int serialNum = (int) (file.length()/512);
                if (file.length()%512==0){
                    serialNum--;
                }
                boolean isFirstPack = true;
                byte[] firstPackage = null;
                byte[] feedBackData = new byte[16];
                int flashAddress = 0;
                boolean writeSuccess = true;

                byte[] writeCMD=new byte[16];
                writeCMD[0]= (byte) macInt1;
                writeCMD[1]= (byte) macInt2;
                writeCMD[2]= (byte) macInt3;
                writeCMD[3]= (byte) macInt4;
                writeCMD[4]= 16;
                writeCMD[11]= 22; //cmd

                while((len=fis.read(buf))!=-1&&writeSuccess){

                    if (isFirstPack){
                        isFirstPack = false;
                        firstPackage = new byte[len];
                        for (int i = 0;i<len;i++){
                            firstPackage[i] = buf[i];
                        }
                        flashAddress = len;
                        continue;
                    }

                    byte[] sendPack = new byte[16+512];
                    byte[] dataPackLength = intToByteArray(512, 3);
                    byte[] serialNumBytes = intToByteArray(serialNum, 3);
                    byte[] flashAddBytes = intToByteArray(flashAddress, 4);
                    setInbyteArray(5,dataPackLength,writeCMD);//包长度
                    setInbyteArray(8,serialNumBytes,writeCMD); //包序
                    setInbyteArray(12,flashAddBytes,writeCMD); //flash地址

                    setInbyteArray(0,writeCMD,sendPack);//写指令
                    setInbyteArray(16,buf,sendPack);//文件数据

                    os.write(sendPack,0,sendPack.length);
                    flashAddress += 512;
                    serialNum --;
                    socket.getInputStream().read(feedBackData);//读取返回的数据

                    for(int startIndex =5;startIndex<8;startIndex++ ){
                        if(feedBackData[startIndex]==0){
                            writeSuccess=true;
                        }else {
                            writeSuccess=false;
                            continue;
                        }
                    }
                    progress+=len;
                    float sumProgress = (progress / fileLength)*100;
                    Message msg=mHandler.obtainMessage();
                    msg.arg1= (int) sumProgress;
                    msg.what=UPDATE_PROGRESS;
                    mHandler.sendMessage(msg);
                }

                byte[] sendPack = new byte[16+512];
                byte[] dataPackLength = intToByteArray(512, 3);
                byte[] serialNumBytes = intToByteArray(0, 3);
                byte[] flashAddBytes = intToByteArray(0, 4);
                setInbyteArray(5,dataPackLength,writeCMD);//包长度
                setInbyteArray(8,serialNumBytes,writeCMD); //包序
                setInbyteArray(12,flashAddBytes,writeCMD); //flash地址

                setInbyteArray(0,writeCMD,sendPack);//暂停指令
                setInbyteArray(16,firstPackage,sendPack);//文件数据
                os.write(sendPack,0,sendPack.length);
                progress+=firstPackage.length;
                float sumProgress = (progress / fileLength)*100;
                Message msg=mHandler.obtainMessage();
                msg.arg1= (int) sumProgress;
                msg.what=UPDATE_PROGRESS;
                mHandler.sendMessage(msg);
                socket.getInputStream().read(feedBackData);

                os.write(resetCMD);
                socket.getInputStream().read(feedBackData);
            }

        } catch (IOException e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(CONNECT_NORESPONE);
        }catch (Exception e){
            e.printStackTrace();
            mHandler.sendEmptyMessage(WIFI_ERRO);
        }finally {
            if (socket!=null&&!socket.isClosed()){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }if (fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }if (os!=null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
