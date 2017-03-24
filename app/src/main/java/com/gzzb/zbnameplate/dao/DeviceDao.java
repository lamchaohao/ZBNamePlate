package com.gzzb.zbnameplate.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.gzzb.zbnameplate.bean.Device;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DEVICE".
*/
public class DeviceDao extends AbstractDao<Device, Void> {

    public static final String TABLENAME = "DEVICE";

    /**
     * Properties of entity Device.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property DeviceName = new Property(0, String.class, "deviceName", false, "DEVICE_NAME");
        public final static Property Ssid = new Property(1, String.class, "ssid", false, "SSID");
    }


    public DeviceDao(DaoConfig config) {
        super(config);
    }
    
    public DeviceDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DEVICE\" (" + //
                "\"DEVICE_NAME\" TEXT," + // 0: deviceName
                "\"SSID\" TEXT);"); // 1: ssid
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DEVICE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Device entity) {
        stmt.clearBindings();
 
        String deviceName = entity.getDeviceName();
        if (deviceName != null) {
            stmt.bindString(1, deviceName);
        }
 
        String ssid = entity.getSsid();
        if (ssid != null) {
            stmt.bindString(2, ssid);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Device entity) {
        stmt.clearBindings();
 
        String deviceName = entity.getDeviceName();
        if (deviceName != null) {
            stmt.bindString(1, deviceName);
        }
 
        String ssid = entity.getSsid();
        if (ssid != null) {
            stmt.bindString(2, ssid);
        }
    }

    @Override
    public Void readKey(Cursor cursor, int offset) {
        return null;
    }    

    @Override
    public Device readEntity(Cursor cursor, int offset) {
        Device entity = new Device( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // deviceName
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1) // ssid
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Device entity, int offset) {
        entity.setDeviceName(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setSsid(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
     }
    
    @Override
    protected final Void updateKeyAfterInsert(Device entity, long rowId) {
        // Unsupported or missing PK type
        return null;
    }
    
    @Override
    public Void getKey(Device entity) {
        return null;
    }

    @Override
    public boolean hasKey(Device entity) {
        // TODO
        return false;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}