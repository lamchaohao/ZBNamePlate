package com.gzz100.nameplate.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.gzz100.nameplate.bean.FileConverter;
import java.io.File;

import com.gzz100.nameplate.bean.Account;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ACCOUNT".
*/
public class AccountDao extends AbstractDao<Account, Long> {

    public static final String TABLENAME = "ACCOUNT";

    /**
     * Properties of entity Account.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, long.class, "id", true, "_id");
        public final static Property SortNumber = new Property(1, int.class, "sortNumber", false, "SORT_NUMBER");
        public final static Property AccountName = new Property(2, String.class, "accountName", false, "ACCOUNT_NAME");
        public final static Property IsBold = new Property(3, boolean.class, "isBold", false, "IS_BOLD");
        public final static Property IsItalic = new Property(4, boolean.class, "isItalic", false, "IS_ITALIC");
        public final static Property IsUnderline = new Property(5, boolean.class, "isUnderline", false, "IS_UNDERLINE");
        public final static Property Typeface = new Property(6, String.class, "typeface", false, "TYPEFACE");
    }

    private final FileConverter typefaceConverter = new FileConverter();

    public AccountDao(DaoConfig config) {
        super(config);
    }
    
    public AccountDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ACCOUNT\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ," + // 0: id
                "\"SORT_NUMBER\" INTEGER NOT NULL ," + // 1: sortNumber
                "\"ACCOUNT_NAME\" TEXT," + // 2: accountName
                "\"IS_BOLD\" INTEGER NOT NULL ," + // 3: isBold
                "\"IS_ITALIC\" INTEGER NOT NULL ," + // 4: isItalic
                "\"IS_UNDERLINE\" INTEGER NOT NULL ," + // 5: isUnderline
                "\"TYPEFACE\" TEXT);"); // 6: typeface
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ACCOUNT\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Account entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
        stmt.bindLong(2, entity.getSortNumber());
 
        String accountName = entity.getAccountName();
        if (accountName != null) {
            stmt.bindString(3, accountName);
        }
        stmt.bindLong(4, entity.getIsBold() ? 1L: 0L);
        stmt.bindLong(5, entity.getIsItalic() ? 1L: 0L);
        stmt.bindLong(6, entity.getIsUnderline() ? 1L: 0L);
 
        File typeface = entity.getTypeface();
        if (typeface != null) {
            stmt.bindString(7, typefaceConverter.convertToDatabaseValue(typeface));
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Account entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
        stmt.bindLong(2, entity.getSortNumber());
 
        String accountName = entity.getAccountName();
        if (accountName != null) {
            stmt.bindString(3, accountName);
        }
        stmt.bindLong(4, entity.getIsBold() ? 1L: 0L);
        stmt.bindLong(5, entity.getIsItalic() ? 1L: 0L);
        stmt.bindLong(6, entity.getIsUnderline() ? 1L: 0L);
 
        File typeface = entity.getTypeface();
        if (typeface != null) {
            stmt.bindString(7, typefaceConverter.convertToDatabaseValue(typeface));
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    @Override
    public Account readEntity(Cursor cursor, int offset) {
        Account entity = new Account( //
            cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // sortNumber
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // accountName
            cursor.getShort(offset + 3) != 0, // isBold
            cursor.getShort(offset + 4) != 0, // isItalic
            cursor.getShort(offset + 5) != 0, // isUnderline
            cursor.isNull(offset + 6) ? null : typefaceConverter.convertToEntityProperty(cursor.getString(offset + 6)) // typeface
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Account entity, int offset) {
        entity.setId(cursor.getLong(offset + 0));
        entity.setSortNumber(cursor.getInt(offset + 1));
        entity.setAccountName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setIsBold(cursor.getShort(offset + 3) != 0);
        entity.setIsItalic(cursor.getShort(offset + 4) != 0);
        entity.setIsUnderline(cursor.getShort(offset + 5) != 0);
        entity.setTypeface(cursor.isNull(offset + 6) ? null : typefaceConverter.convertToEntityProperty(cursor.getString(offset + 6)));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Account entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Account entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Account entity) {
        throw new UnsupportedOperationException("Unsupported for entities with a non-null key");
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
