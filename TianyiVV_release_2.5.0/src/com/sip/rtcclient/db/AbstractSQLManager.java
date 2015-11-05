package com.sip.rtcclient.db;

import java.util.Observable;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclient.bean.TCallRecordInfo;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.bean.TGroupInfo;
import com.sip.rtcclient.bean.TGroupRecordInfo;
import com.sip.rtcclient.utils.CommFunc;


public abstract class AbstractSQLManager extends Observable {
    public static final int VERSION = 6;
    public static final String TAG = AbstractSQLManager.class.getName();
    private DatabaseHelper databaseHelper;
    public SQLiteDatabase sqliteDB;
    public String TABLE_USER_INFO = "table_user_info";// 用户信息表 系统通讯录
    public String TABLE_GROUP_INFO= "table_group_info";
    public String TABLE_CALL_RECORD = "table_call_record"; //通讯记录
    public String TABLE_CONTACT_INFO = "table_contact_info"; //联系人
    public String TABLE_GROUP_RECORD_INFO = "table_group_record_info";	//群组通话记录表

    private String[] tables = { TABLE_USER_INFO,TABLE_GROUP_INFO,TABLE_CALL_RECORD,TABLE_CONTACT_INFO, TABLE_GROUP_RECORD_INFO }; // 数据库表数组

    public AbstractSQLManager() {
        openDatabase(MyApplication.getInstance(), VERSION);
    }

    private void openDatabase(Context context, int databaseVersion) {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(context, databaseVersion);
        }
        if (sqliteDB == null) {
            sqliteDB = databaseHelper.getWritableDatabase();
        }
    }

    public void destroy() {
        try {
            if (databaseHelper != null) {
                databaseHelper.close();
            }
            if (sqliteDB != null) {
                sqliteDB.close();
            }
        } catch (Exception e) {
            
        }
    }

    private void open(boolean isReadonly) {
        if (sqliteDB == null) {
            if (isReadonly) {
                sqliteDB = databaseHelper.getReadableDatabase();
            } else {
                sqliteDB = databaseHelper.getWritableDatabase();
            }
        }
    }
    

    public final void reopen() {
        closeDB();
        open(false);
        
    }

    private void closeDB() {
        if (sqliteDB != null) {
            sqliteDB.close();
            sqliteDB = null;
        }
    }

    protected final SQLiteDatabase sqliteDB() {
        open(false);
        return sqliteDB;
    }
    private class DatabaseHelper extends SQLiteOpenHelper {
        
        public DatabaseHelper(Context context, int version) {
            super(context, MyApplication.getInstance().getAppAccountID()+".db", 
                    null, version);//SysConfig.getInstance().EQ_Id+"_"+SysConfig.getInstance().User_ExtNumber+".db"
            // TODO Auto-generated constructor stub
            CommFunc.PrintLog(5, "DatabaseHelper--dbname", MyApplication.getInstance().getAppAccountID()+".db");
  //          CommFunc.DisplayToast(context, "DatabaseHelper"+MyApplication.getInstance().getAppAccountID());
        }

        /**
         * 重写oncreate方法建表
         */

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTables(db);
        }
        
        private void createTables(SQLiteDatabase db) {
            createTableForContactInfo(db);
            createTableForCallRecordInfo(db);
            createTableForGroup(db);
            createTableForGroupRecord(db);
        }

        /**
         * 创建联系人表
         * @param db
         */
        private void createTableForContactInfo(SQLiteDatabase db) {
        	String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_CONTACT_INFO + " ("
            + TContactInfo._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," // 自增
            + TContactInfo._CONTACT_ID + " VARCHAR UNIQUE,"//UNIQUE
            + TContactInfo._CONTACT_NAME + " VARCHAR,"
            + TContactInfo._CONTACT_NUMBER + " VARCHAR,"
            + TContactInfo._CONTACT_SORT_KEY + " VARCHAR,"
            + TContactInfo._CONTACT_PHOTO_ID + " LONG,"
            + TContactInfo._CONTACT_LOOK_UP_KEY + " VARCHAR,"
            + TContactInfo._CONTACT_USERTYPE + " INTEGER"
            + ")";
        	db.execSQL(sql);
        }
        
        /**
         * 创建通讯记录表
         * @param db
         */
        public void createTableForCallRecordInfo(SQLiteDatabase db) {
        	String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_CALL_RECORD + " ("
            + TCallRecordInfo._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," // 自增
            + TCallRecordInfo._CALL_RECORD_ID + " VARCHAR UNIQUE,"//UNIQUE
            + TCallRecordInfo._DATE + " VARCHAR,"
            + TCallRecordInfo._START_TIME + " VARCHAR,"
            + TCallRecordInfo._END_TIME + " VARCHAR,"
            + TCallRecordInfo._TOTAL_TIME + " VARCHAR,"
            + TCallRecordInfo._FROM_USER + " VARCHAR,"
            + TCallRecordInfo._TO_USER + " VARCHAR,"
            + TCallRecordInfo._TYPE + " INTEGER,"
            + TCallRecordInfo._RESULT + " INTEGER,"
            + TCallRecordInfo._DIRECTION + " INTEGER"
            + ")";
        	db.execSQL(sql);
    	}
        
        /**
         * 创建群组信息表
         * @param db
         */
        private void createTableForGroup(SQLiteDatabase db) {
        	String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_GROUP_INFO + " ("
            + TGroupInfo._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," // 自增
            + TGroupInfo._GROUP_ID + " VARCHAR UNIQUE,"//UNIQUE
            + TGroupInfo._GROUP_NAME + " VARCHAR,"
            + TGroupInfo._GROUP_MEMBERS + " VARCHAR,"
            + TGroupInfo._GROUP_CREATE_TIME + " VARCHAR,"
            + TGroupInfo._GROUP_CREATOR + " VARCHAR,"
            + TGroupInfo._GROUP_PHOTO + " VARCHAR,"
            + TGroupInfo._GROUP_TYPE + " INTEGER,"
            + TGroupInfo._GROUP_SHIELD + " INTEGER"
            + ")";
        	db.execSQL(sql);
		}
        
        /**
         * 创建群组通话记录表
         * @param db
         */
        private void createTableForGroupRecord(SQLiteDatabase db) {
        	String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_GROUP_RECORD_INFO + " ("
            + TGroupRecordInfo._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," // 自增
            + TGroupRecordInfo._GROUP_CALL_ID + " VARCHAR UNIQUE,"
            + TGroupRecordInfo._GROUP_ID + " VARCHAR,"
            + TGroupRecordInfo._CONF_TYPE + " INTEGER,"
            + TGroupRecordInfo._STARTTIME + " VARCHAR,"
            + TGroupRecordInfo._ENDTIME + " VARCHAR,"
            + TGroupRecordInfo._TIME + " VARCHAR,"
            + TGroupRecordInfo._START_DATE + " VARCHAR,"
            + TGroupRecordInfo._END_DATE + " VARCHAR,"
            + TGroupRecordInfo._JOIN_RESULT + " INTEGER,"
            + TGroupRecordInfo._DURATION + " INTEGER"
            + ")";
        	db.execSQL(sql);
        }

        /**
         * 重写onupgrade方法，当数据库版本号更新时执行
         */

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            CommFunc.PrintLog(5, TAG, "onUpgrade oldversion:"+oldVersion +"newVersion:"+newVersion);
            // TODO Auto-generated method stub
            dropTableByTableName(db,tables);
            createTables(db);
            //
//            MyApplication.getInstance().clearConfigInfo();
//            MyApplication.getInstance().cleanSnapConfigInfo();
//             MyApplication.getInstance().saveDataToSharedXml(new String[]{RegistInfo.VER_ADDR,RegistInfo.VER_FAX,FaxWeb.KEY_VERSION}
//             , new String[]{"0","0","0"});
        }
        /**
         * Drop表
         * 
         * @param db
         * @param TABLENAME
         */
        void dropTableByTableName(SQLiteDatabase db, String[] TABLENAME) {
            StringBuffer sql = new StringBuffer("DROP TABLE IF EXISTS ");
            int len = sql.length();
            for (String name : TABLENAME) {
                try {
                    sql.append(name);
                    db.execSQL(sql.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    sql.delete(len, sql.length());
                }
            }
        }

    }

}
