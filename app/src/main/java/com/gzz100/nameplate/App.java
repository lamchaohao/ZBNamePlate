package com.gzz100.nameplate;

import android.app.Application;

import com.gzz100.nameplate.dao.DaoMaster;
import com.gzz100.nameplate.dao.DaoSession;

import org.greenrobot.greendao.database.Database;


/**
 * Created by Lam on 2017/3/12.
 */

public class App extends Application {
    public static final boolean ENCRYPTED = false;
    private DaoSession daoSession;
    private Database mDb;

    @Override
    public void onCreate() {
        super.onCreate();
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? "account-db-encrypted" :"account-db");
        mDb = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();

        daoSession = new DaoMaster(mDb).newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public Database getDb() {
        return mDb;
    }
}
