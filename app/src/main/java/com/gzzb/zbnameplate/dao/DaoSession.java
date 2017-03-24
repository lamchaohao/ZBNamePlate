package com.gzzb.zbnameplate.dao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.gzzb.zbnameplate.bean.Account;
import com.gzzb.zbnameplate.bean.Device;

import com.gzzb.zbnameplate.dao.AccountDao;
import com.gzzb.zbnameplate.dao.DeviceDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig accountDaoConfig;
    private final DaoConfig deviceDaoConfig;

    private final AccountDao accountDao;
    private final DeviceDao deviceDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        accountDaoConfig = daoConfigMap.get(AccountDao.class).clone();
        accountDaoConfig.initIdentityScope(type);

        deviceDaoConfig = daoConfigMap.get(DeviceDao.class).clone();
        deviceDaoConfig.initIdentityScope(type);

        accountDao = new AccountDao(accountDaoConfig, this);
        deviceDao = new DeviceDao(deviceDaoConfig, this);

        registerDao(Account.class, accountDao);
        registerDao(Device.class, deviceDao);
    }
    
    public void clear() {
        accountDaoConfig.clearIdentityScope();
        deviceDaoConfig.clearIdentityScope();
    }

    public AccountDao getAccountDao() {
        return accountDao;
    }

    public DeviceDao getDeviceDao() {
        return deviceDao;
    }

}
