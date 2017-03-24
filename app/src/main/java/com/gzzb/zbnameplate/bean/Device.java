package com.gzzb.zbnameplate.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 该bean为铭牌
 * Created by Lam on 2017/3/24.
 */

@Entity
public class Device {
    private String deviceName;
    private String ssid;
    @Generated(hash = 94852500)
    public Device(String deviceName, String ssid) {
        this.deviceName = deviceName;
        this.ssid = ssid;
    }
    @Generated(hash = 1469582394)
    public Device() {
    }
    public String getDeviceName() {
        return this.deviceName;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    public String getSsid() {
        return this.ssid;
    }
    public void setSsid(String ssid) {
        this.ssid = ssid;
    }
    
}
