package com.gzz100.nameplate.bean;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.File;

/**
 * Created by Lam on 2017/3/12.
 */
@Entity
public class Account {
    @Id(autoincrement = true)
    private long id;
    private int sortNumber;
    private String accountName;
    private boolean isBold;
    private boolean isItalic;
    private boolean isUnderline;
    @Convert(converter = FileConverter.class,columnType = String.class)
    private File typeface;
    @Generated(hash = 770264233)
    public Account(long id, int sortNumber, String accountName, boolean isBold,
            boolean isItalic, boolean isUnderline, File typeface) {
        this.id = id;
        this.sortNumber = sortNumber;
        this.accountName = accountName;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isUnderline = isUnderline;
        this.typeface = typeface;
    }
    @Generated(hash = 882125521)
    public Account() {
    }
    public long getId() {
        return this.id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public int getSortNumber() {
        return this.sortNumber;
    }
    public void setSortNumber(int sortNumber) {
        this.sortNumber = sortNumber;
    }
    public String getAccountName() {
        return this.accountName;
    }
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    public boolean getIsBold() {
        return this.isBold;
    }
    public void setIsBold(boolean isBold) {
        this.isBold = isBold;
    }
    public boolean getIsItalic() {
        return this.isItalic;
    }
    public void setIsItalic(boolean isItalic) {
        this.isItalic = isItalic;
    }
    public boolean getIsUnderline() {
        return this.isUnderline;
    }
    public void setIsUnderline(boolean isUnderline) {
        this.isUnderline = isUnderline;
    }
    public File getTypeface() {
        return this.typeface;
    }
    public void setTypeface(File typeface) {
        this.typeface = typeface;
    }
    
   
}
