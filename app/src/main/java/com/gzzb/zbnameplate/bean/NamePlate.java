package com.gzzb.zbnameplate.bean;

/**
 * Created by Lam on 2017/3/29.
 */

public class NamePlate {
    private Account account;
    private Device device;
    private String state;
    private String wifiState;

    public NamePlate(Account account, Device device, String state, String wifiState) {
        this.account = account;
        this.device = device;
        this.state = state;
        this.wifiState = wifiState;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getWifiState() {
        return wifiState;
    }

    public void setWifiState(String wifiState) {
        this.wifiState = wifiState;
    }
}
