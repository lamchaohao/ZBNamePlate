package com.gzzb.zbnameplate;

import android.app.Application;

import com.gzzb.zbnameplate.dao.DaoMaster;
import com.gzzb.zbnameplate.dao.DaoSession;
import com.gzzb.zbnameplate.global.Global;

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
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? Global.DAO_ENCRYPT :Global.DAO_DATABASENAME);
        mDb = ENCRYPTED ? helper.getEncryptedWritableDb(Global.DAO_SUPERSECRET) : helper.getWritableDb();

        daoSession = new DaoMaster(mDb).newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public Database getDb() {
        return mDb;
    }
}
