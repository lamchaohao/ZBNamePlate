package com.gzz100.nameplate.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.gzz100.nameplate.bean.Device;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DEVICE".
*/
public class DeviceDao extends AbstractDao<Device, Long> {

    public static final String TABLENAME = "DEVICE";

    /**
     * Properties of entity Device.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, long.class, "id", true, "_id");
        public final static Property DeviceName = new Property(1, String.class, "deviceName", false, "DEVICE_NAME");
        public final static Property Ssid = new Property(2, String.class, "ssid", false, "SSID");
        public final static Property Brightness = new Property(3, int.class, "brightness", false, "BRIGHTNESS");
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
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ," + // 0: id
                "\"DEVICE_NAME\" TEXT," + // 1: deviceName
                "\"SSID\" TEXT," + // 2: ssid
                "\"BRIGHTNESS\" INTEGER NOT NULL );"); // 3: brightness
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DEVICE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Device entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
 
        String deviceName = entity.getDeviceName();
        if (deviceName != null) {
            stmt.bindString(2, deviceName);
        }
 
        String ssid = entity.getSsid();
        if (ssid != null) {
            stmt.bindString(3, ssid);
        }
        stmt.bindLong(4, entity.getBrightness());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Device entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
 
        String deviceName = entity.getDeviceName();
        if (deviceName != null) {
            stmt.bindString(2, deviceName);
        }
 
        String ssid = entity.getSsid();
        if (ssid != null) {
            stmt.bindString(3, ssid);
        }
        stmt.bindLong(4, entity.getBrightness());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    @Override
    public Device readEntity(Cursor cursor, int offset) {
        Device entity = new Device( //
            cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // deviceName
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // ssid
            cursor.getInt(offset + 3) // brightness
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Device entity, int offset) {
        entity.setId(cursor.getLong(offset + 0));
        entity.setDeviceName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setSsid(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setBrightness(cursor.getInt(offset + 3));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Device entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Device entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Device entity) {
        throw new UnsupportedOperationException("Unsupported for entities with a non-null key");
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
