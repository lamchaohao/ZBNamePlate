package com.gzzb.zbnameplate.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 该bean为铭牌
 * Created by Lam on 2017/3/24.
 */

@Entity
public class Device {
    @Id(autoincrement = true)
    private long id;
    private String deviceName;
    private String ssid;
    private int brightness;
    @Transient
    private String state;
    @Transient
    private boolean isOnline;
    @Generated(hash = 402824446)
    public Device(long id, String deviceName, String ssid, int brightness) {
        this.id = id;
        this.deviceName = deviceName;
        this.ssid = ssid;
        this.brightness = brightness;
    }
    @Generated(hash = 1469582394)
    public Device() {
    }
    public long getId() {
        return this.id;
    }
    public void setId(long id) {
        this.id = id;
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
    public int getBrightness() {
        return this.brightness;
    }
    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean getIsOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
