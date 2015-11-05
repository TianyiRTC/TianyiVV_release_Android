package com.sip.rtcclient.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;

import com.sip.rtcclient.HBaseApp;
import com.sip.rtcclient.MyApplication;
import com.sip.rtcclient.bean.TCallRecordInfo;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.bean.TGroupInfo;
import com.sip.rtcclient.bean.TGroupRecordInfo;
import com.sip.rtcclient.config.SysConfig;
import com.sip.rtcclient.utils.CommFunc;
import com.sip.rtcclient.utils.PinYinManager;

public class SQLiteManager extends AbstractSQLManager {

    private String LOGTAG = "SQLiteManager";
    public static SQLiteManager getInstance() {
        return MyApplication.getInstance().getSqlManager();
    }

    /**
     * 保存一条通话记录
     * @param info
     * @param dbFlag
     */
    public void saveCallRecordInfo(TCallRecordInfo info, boolean dbFlag){

        ContentValues values = new ContentValues();
        values.put(TCallRecordInfo._CALL_RECORD_ID, info.getCallRecordId());
        values.put(TCallRecordInfo._DATE, info.getDate());
        values.put(TCallRecordInfo._START_TIME, info.getStartTime());
        values.put(TCallRecordInfo._END_TIME, info.getEndTime());
        values.put(TCallRecordInfo._TOTAL_TIME, info.getTotalTime());
        values.put(TCallRecordInfo._FROM_USER, info.getFromUser());
        values.put(TCallRecordInfo._TO_USER, info.getToUser());
        values.put(TCallRecordInfo._TYPE, info.getType());
        values.put(TCallRecordInfo._RESULT, info.getResult());
        values.put(TCallRecordInfo._DIRECTION, info.getDirection());

        long ret = sqliteDB().replaceOrThrow(TABLE_CALL_RECORD, null, values);
        CommFunc.PrintLog(5, LOGTAG, "saveCallRecordInfo:"+ret);
        if (dbFlag) {
            dbChanged(info);
        }
    }

    /**
     * 获取本帐号相关的呼叫记录-分组
     * @return
     */
    public List<TCallRecordInfo> getCallRecordInfo(String userNumber) {
        List<TCallRecordInfo> list = new ArrayList<TCallRecordInfo>();
        //		String sql = "select * from " + TABLE_CALL_RECORD + " where " + CallRecordInfo._FROM_USER + " ='" + userNumber + "' or " + CallRecordInfo._TO_USER + " = '" + userNumber + "' group by " + CallRecordInfo._FROM_USER + " , " + CallRecordInfo._TO_USER + " order by " + CallRecordInfo._DATE + " desc";
        String sql = "select * from " + TABLE_CALL_RECORD + " where " + TCallRecordInfo._FROM_USER + " ='" + userNumber + "' group by " + TCallRecordInfo._TO_USER + " order by " + TCallRecordInfo._DATE + " desc, " + TCallRecordInfo._START_TIME + " desc";
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{
            while(cursor!=null && cursor.moveToNext()){
                TCallRecordInfo info = new TCallRecordInfo();
                info.setCallRecordId(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._CALL_RECORD_ID)));
                info.setDate(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._DATE)));
                info.setStartTime(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._START_TIME)));
                info.setEndTime(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._END_TIME)));
                info.setTotalTime(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._TOTAL_TIME)));
                info.setFromUser(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._FROM_USER)));
                info.setToUser(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._TO_USER)));

                info.setType(cursor.getInt(cursor.getColumnIndex(TCallRecordInfo._TYPE)));
                info.setResult(cursor.getInt(cursor.getColumnIndex(TCallRecordInfo._RESULT)));
                info.setDirection(cursor.getInt(cursor.getColumnIndex(TCallRecordInfo._DIRECTION)));
                list.add(info);
            } 
        }

        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }

        return list;
    }

    /**
     * 获取本帐号相关的呼叫记录-分组
     * @return
     */
    public List<TCallRecordInfo> getAllCallRecordInfoByUser(String userNumber) {
        List<TCallRecordInfo> list = new ArrayList<TCallRecordInfo>();
        String sql = "select * from " + TABLE_CALL_RECORD + " where " + TCallRecordInfo._FROM_USER + " ='" + userNumber + "' order by " + TCallRecordInfo._DATE + " desc, " + TCallRecordInfo._START_TIME + " desc";
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{
            while(cursor!=null && cursor.moveToNext()){
                TCallRecordInfo info = new TCallRecordInfo();
                info.setCallRecordId(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._CALL_RECORD_ID)));
                info.setDate(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._DATE)));
                info.setStartTime(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._START_TIME)));
                info.setEndTime(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._END_TIME)));
                info.setTotalTime(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._TOTAL_TIME)));
                info.setFromUser(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._FROM_USER)));
                info.setToUser(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._TO_USER)));

                info.setType(cursor.getInt(cursor.getColumnIndex(TCallRecordInfo._TYPE)));
                info.setResult(cursor.getInt(cursor.getColumnIndex(TCallRecordInfo._RESULT)));
                info.setDirection(cursor.getInt(cursor.getColumnIndex(TCallRecordInfo._DIRECTION)));
                list.add(info);
            }   
        }
        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }

    /**
     * 获取呼叫记录详情
     * @return
     */
    public List<TCallRecordInfo> getCallRecordInfo(String fromUser, String toUser) {
        List<TCallRecordInfo> list = new ArrayList<TCallRecordInfo>();
        String sql = "select * from " + TABLE_CALL_RECORD + " where (" + TCallRecordInfo._FROM_USER + " = '" + fromUser +  "' and " + TCallRecordInfo._TO_USER + " ='" + toUser + "') or (" +  TCallRecordInfo._FROM_USER + " = '" + toUser +  "' and " + TCallRecordInfo._TO_USER + " ='" + fromUser + "') order by " + TCallRecordInfo._DATE + " desc, " + TCallRecordInfo._START_TIME + " desc";
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{
            while(cursor!=null && cursor.moveToNext()){
                TCallRecordInfo info = new TCallRecordInfo();
                info.setCallRecordId(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._CALL_RECORD_ID)));
                info.setDate(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._DATE)));
                info.setStartTime(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._START_TIME)));
                info.setEndTime(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._END_TIME)));
                info.setTotalTime(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._TOTAL_TIME)));
                info.setFromUser(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._FROM_USER)));
                info.setToUser(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._TO_USER)));

                info.setType(cursor.getInt(cursor.getColumnIndex(TCallRecordInfo._TYPE)));
                info.setResult(cursor.getInt(cursor.getColumnIndex(TCallRecordInfo._RESULT)));
                info.setDirection(cursor.getInt(cursor.getColumnIndex(TCallRecordInfo._DIRECTION)));
                list.add(info);
            }     
        }
        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }


    /**
     * 删除一条通话记录
     * @param callId
     */
    public void delCallRecordInfo(String callId, boolean dbFlag){
        sqliteDB().delete(TABLE_CALL_RECORD, TCallRecordInfo._CALL_RECORD_ID + " = '" + callId + "'", null);
        if (dbFlag) {
            dbChanged(new TCallRecordInfo());
        }
    }

    /**
     * 获取呼叫次数
     * @param fromeUser
     * @param toUser
     * @return
     */
    public int getCallRecordCount(String fromeUser, String toUser) {
        String sql = "select * from " + TABLE_CALL_RECORD + " where (" + TCallRecordInfo._FROM_USER + " = '" + fromeUser +  "' and " + TCallRecordInfo._TO_USER + " ='" + toUser + "') or (" +  TCallRecordInfo._FROM_USER + " = '" + toUser +  "' and " + TCallRecordInfo._TO_USER + " ='" + fromeUser + "')";
        //		String sql = "select * from " + TABLE_CALL_RECORD + " where (" + CallRecordInfo._FROM_USER + " = '" + fromeUser +  "' or " + CallRecordInfo._TO_USER + " ='" + fromeUser + "') and (" +  CallRecordInfo._FROM_USER + " = '" + toUser +  "' or " + CallRecordInfo._TO_USER + " ='" + toUser + "')";
        Cursor cursor =  sqliteDB().rawQuery(sql, null);
        int count = 0;
        if(cursor!=null)
        {
            count= cursor.getCount();
            cursor.close();
            cursor= null;
        }

        return count;
    }

    /**
     * 更新通话记录
     * @param id
     * @param info
     */
    public void updateCallRecordInfo(String id, TCallRecordInfo info, boolean dbFlag) {
        ContentValues values = new ContentValues();
        values.put(TCallRecordInfo._TOTAL_TIME, info.getTotalTime());
        values.put(TCallRecordInfo._END_TIME, info.getEndTime());
        values.put(TCallRecordInfo._RESULT, info.getResult());
        sqliteDB().update(TABLE_CALL_RECORD, values,TCallRecordInfo._CALL_RECORD_ID + " = '" + id + "'", null);
        if (dbFlag) {
            dbChanged(new TCallRecordInfo());
        }
    }

    /**
     * 更新通话记录
     * @param id
     * @param info
     */
    public void updateCallRecordInfo(TCallRecordInfo info, boolean dbFlag) {
        ContentValues values = new ContentValues();
        values.put(TCallRecordInfo._TOTAL_TIME, info.getTotalTime());
        values.put(TCallRecordInfo._END_TIME, info.getEndTime());
        values.put(TCallRecordInfo._RESULT, info.getResult());
        sqliteDB().update(TABLE_CALL_RECORD, values,TCallRecordInfo._CALL_RECORD_ID + " = '" + info.getCallRecordId() + "'", null);
        if (dbFlag) {
            dbChanged(new TCallRecordInfo());
        }
    }

    /**
     * 通过呼叫记录ID获取呼叫记录
     * @param id
     * @return
     */
    public TCallRecordInfo getCallRecordInfoById(String callRecordId) {
        String sql = "select * from " + TABLE_CALL_RECORD + " where " + TCallRecordInfo._CALL_RECORD_ID + " = '" + callRecordId + "'";
        TCallRecordInfo info = new TCallRecordInfo();
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{
            if(cursor!=null && cursor.moveToNext()){
                info.setCallRecordId(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._CALL_RECORD_ID)));
                info.setDate(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._DATE)));
                info.setStartTime(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._START_TIME)));
                info.setEndTime(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._END_TIME)));
                info.setTotalTime(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._TOTAL_TIME)));
                info.setFromUser(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._FROM_USER)));
                info.setToUser(cursor.getString(cursor.getColumnIndex(TCallRecordInfo._TO_USER)));
                info.setType(cursor.getInt(cursor.getColumnIndex(TCallRecordInfo._TYPE)));
                info.setResult(cursor.getInt(cursor.getColumnIndex(TCallRecordInfo._RESULT)));
                info.setDirection(cursor.getInt(cursor.getColumnIndex(TCallRecordInfo._DIRECTION)));
            }   
        }
        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        return info;
    }

    /**
     * 更新int类型的通话记录某一项
     * TODO 暂时无用
     * @param result
     */
    public void updateCallRecordInfo(String id, String key, int value, boolean dbFlag) {
        ContentValues values = new ContentValues();
        values.put(key, value);
        sqliteDB().update(TABLE_CALL_RECORD, values,TCallRecordInfo._CALL_RECORD_ID + " = '" + id + "'", null);
        if (dbFlag) {
            dbChanged(new TCallRecordInfo());
        }
    }

    /**
     * 更新字符串类型的通话记录某一项
     * TODO 暂时无用
     * @param id
     * @param totalTime
     * @param dbFlag
     */
    public void updateCallRecordInfo(String id, String key, String value, boolean dbFlag) {
        ContentValues values = new ContentValues();
        values.put(key, value);
        sqliteDB().update(TABLE_CALL_RECORD, values,TCallRecordInfo._CALL_RECORD_ID + " = '" + id + "'", null);
        if (dbFlag) {
            dbChanged(new TCallRecordInfo());
        }
    }

    /**
     * 保存一条群组信息
     * @param info
     * @param dbFlag
     */
    public void saveGroupInfo(TGroupInfo info, boolean dbFlag){
        ContentValues values = new ContentValues();
        values.put(TGroupInfo._GROUP_ID, info.getGroupId());
        values.put(TGroupInfo._GROUP_NAME, info.getGroupName());
        values.put(TGroupInfo._GROUP_MEMBERS, info.getGroupMembers());
        values.put(TGroupInfo._GROUP_CREATE_TIME, info.getGroupCreateTime());
        values.put(TGroupInfo._GROUP_CREATOR, info.getGroupCreator());
        values.put(TGroupInfo._GROUP_PHOTO, info.getGroupPhoto());
        values.put(TGroupInfo._GROUP_TYPE, info.getGroupType());
        values.put(TGroupInfo._GROUP_SHIELD, info.getGroupShieldTag());		
        sqliteDB().replaceOrThrow(TABLE_GROUP_INFO, null, values);
        if (dbFlag) {
            dbChanged(info);
        }
    }

    /**
     * 获取所有群组信息
     * @return
     */
    public List<TGroupInfo> getAllGroupInfo() {
        List<TGroupInfo> list = new ArrayList<TGroupInfo>();
        String sql = "select * from " + TABLE_GROUP_INFO + " order by " + TGroupInfo._GROUP_TYPE + " asc, " + TGroupInfo._GROUP_NAME + " desc";
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{
            while(cursor!=null && cursor.moveToNext()){
                TGroupInfo info = new TGroupInfo();
                info.setGroupId(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_ID)));
                info.setGroupName(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_NAME)));
                info.setGroupMembers(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_MEMBERS)));
                info.setGroupCreateTime(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_CREATE_TIME)));
                info.setGroupCreator(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_CREATOR)));
                info.setGroupPhoto(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_PHOTO)));
                info.setGroupType(cursor.getInt(cursor.getColumnIndex(TGroupInfo._GROUP_TYPE)));
                info.setGroupShieldTag(cursor.getInt(cursor.getColumnIndex(TGroupInfo._GROUP_SHIELD)));
                list.add(info);
            }            
        }

        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }

    /**
     * 获取不包含某联系人的所有群组信息
     */
    public List<TGroupInfo> getAllGroupInfoAExept(String number) {
        List<TGroupInfo> list = new ArrayList<TGroupInfo>();
        String sql = "select * from " + TABLE_GROUP_INFO + " where " + TGroupInfo._GROUP_MEMBERS + " not like " + "'%" + number + "%'" +  " order by " + TGroupInfo._GROUP_TYPE + " asc, " + TGroupInfo._GROUP_NAME + " desc";
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{
            while(cursor!=null && cursor.moveToNext()){
                TGroupInfo info = new TGroupInfo();
                info.setGroupId(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_ID)));
                info.setGroupName(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_NAME)));
                info.setGroupMembers(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_MEMBERS)));
                info.setGroupCreateTime(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_CREATE_TIME)));
                info.setGroupCreator(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_CREATOR)));
                info.setGroupPhoto(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_PHOTO)));
                info.setGroupType(cursor.getInt(cursor.getColumnIndex(TGroupInfo._GROUP_TYPE)));
                info.setGroupShieldTag(cursor.getInt(cursor.getColumnIndex(TGroupInfo._GROUP_SHIELD)));
                list.add(info);
            }            
        }

        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }

    /**
     * 获取某一群组的成员信息
     * @param groupId
     * @return
     */
    public List<String> getGroupMembersById(String groupId) {
        List<String> list = new ArrayList<String>();
        String sql = "select " + TGroupInfo._GROUP_MEMBERS + " from " + TABLE_GROUP_INFO + " where " + TGroupInfo._GROUP_ID + " ='" + groupId + "'";
        String[] members = null;
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{
            while(cursor!=null && cursor.moveToNext()){
                members = cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_MEMBERS)).split(";");
            }            
        }
        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        for (int i = 0; i < members.length; i++) {
            list.add(members[i]);
        }
        return list;
    }

    /**
     * 获取某一群组的详细信息
     * @param groupId
     * @return
     */
    public TGroupInfo getGroupById(String groupId) {

        TGroupInfo info = null;
        String sql = "select * from " + TABLE_GROUP_INFO + " where " + TGroupInfo._GROUP_ID + " ='" + groupId + "'";
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{
            if(cursor!=null && cursor.moveToNext()){
                info = new TGroupInfo();
                info.setGroupId(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_ID)));
                info.setGroupName(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_NAME)));
                info.setGroupMembers(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_MEMBERS)));
                info.setGroupCreateTime(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_CREATE_TIME)));
                info.setGroupCreator(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_CREATOR)));
                info.setGroupPhoto(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_PHOTO)));
                info.setGroupType(cursor.getInt(cursor.getColumnIndex(TGroupInfo._GROUP_TYPE)));
                info.setGroupShieldTag(cursor.getInt(cursor.getColumnIndex(TGroupInfo._GROUP_SHIELD)));
            }  
        }
        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        return info;
    }
    public String getGroupCreatorByGrpName(String grpname)
    {
        String grpcreator = "";
        TGroupInfo info = null;
        String sql = "select * from " + TABLE_GROUP_INFO + " where " + TGroupInfo._GROUP_NAME + " ='" + grpname + "'";
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{
            if(cursor!=null && cursor.moveToNext()){
                info = new TGroupInfo();
                info.setGroupId(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_ID)));
                info.setGroupName(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_NAME)));
                info.setGroupMembers(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_MEMBERS)));
                info.setGroupCreateTime(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_CREATE_TIME)));
                info.setGroupCreator(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_CREATOR)));
                info.setGroupPhoto(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_PHOTO)));
                info.setGroupType(cursor.getInt(cursor.getColumnIndex(TGroupInfo._GROUP_TYPE)));
                info.setGroupShieldTag(cursor.getInt(cursor.getColumnIndex(TGroupInfo._GROUP_SHIELD)));
            }   
        }

        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        if(info!=null)
            grpcreator =  info.getGroupCreator();
        CommFunc.PrintLog(5, LOGTAG, "getGroupCreatorByGrpName grpname:"+ grpname +"grpcreator:"+grpcreator);
        return grpcreator;

    }
    /**
     * 获取某一群组的详细信息
     * @param groupId
     * @return
     */
    public TGroupInfo getGroupByName(String groupName, String creator) {

        TGroupInfo info = null;
        String sql = "select * from " + TABLE_GROUP_INFO + " where " + TGroupInfo._GROUP_NAME + " ='" + groupName + "' and " + TGroupInfo._GROUP_CREATOR + " ='" + creator + "'";
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try
        {
            if(cursor!=null && cursor.moveToNext()){
                info = new TGroupInfo();
                info.setGroupId(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_ID)));
                info.setGroupName(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_NAME)));
                info.setGroupMembers(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_MEMBERS)));
                info.setGroupCreateTime(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_CREATE_TIME)));
                info.setGroupCreator(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_CREATOR)));
                info.setGroupPhoto(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_PHOTO)));
                info.setGroupType(cursor.getInt(cursor.getColumnIndex(TGroupInfo._GROUP_TYPE)));
                info.setGroupShieldTag(cursor.getInt(cursor.getColumnIndex(TGroupInfo._GROUP_SHIELD)));
            }  
        }

        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        return info;
    }

    /**
     * 根据群组名称获取某一群组的详细信息
     * @param groupId
     * @return
     */
    public TGroupInfo getGroupByGroupName(String groupName) {

        TGroupInfo info = null;
        String sql = "select * from " + TABLE_GROUP_INFO + " where " + TGroupInfo._GROUP_NAME + " ='" + groupName + "'";
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{
            if(cursor!=null && cursor.moveToNext()){
                info = new TGroupInfo();
                info.setGroupId(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_ID)));
                info.setGroupName(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_NAME)));
                info.setGroupMembers(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_MEMBERS)));
                info.setGroupCreateTime(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_CREATE_TIME)));
                info.setGroupCreator(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_CREATOR)));
                info.setGroupPhoto(cursor.getString(cursor.getColumnIndex(TGroupInfo._GROUP_PHOTO)));
                info.setGroupType(cursor.getInt(cursor.getColumnIndex(TGroupInfo._GROUP_TYPE)));
                info.setGroupShieldTag(cursor.getInt(cursor.getColumnIndex(TGroupInfo._GROUP_SHIELD)));
            }  
        }
        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        return info;
    }

    /**
     * TODO 根据群组名称与创建者判断群组是否存在-需要更改
     * @return
     */
    public boolean checkGroupExist(String groupName, String creator) {
        String sql = "select * from " + TABLE_GROUP_INFO + " where " + TGroupInfo._GROUP_NAME + " ='" + groupName + "'" + " and " + TGroupInfo._GROUP_CREATOR + " ='" + creator + "'";
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        int count = 0;
        try{
            if(cursor != null)
                count = cursor.getCount();
        }

        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        if (count > 0) {
            return true;
        }else {
            return false;
        }
    }

    /**
     * 根据群组名称判断群组是否存在
     * @param groupName
     * @param creator
     * @return
     */
    public boolean checkGroupExistByName(String groupName) {
        String sql = "select * from " + TABLE_GROUP_INFO + " where " + TGroupInfo._GROUP_NAME + " ='" + groupName + "'";
        int count = 0;
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{
            if(cursor!=null)
                count = cursor.getCount();  
        }

        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        if (count > 0) {
            return true;
        }else {
            return false;
        }
    }

    /**
     * 更新某一群组信息
     */
    public void updateGroupByGrpName(TGroupInfo info, boolean dbFlag) {
        CommFunc.PrintLog(5, LOGTAG, "updateGroupByGrpName grpname:"+info.getGroupName()+" members:"+info.getGroupMembers());

        ContentValues values = new ContentValues();
        values.put(TGroupInfo._GROUP_NAME, info.getGroupName());
        values.put(TGroupInfo._GROUP_MEMBERS, info.getGroupMembers());
        values.put(TGroupInfo._GROUP_CREATE_TIME, info.getGroupCreateTime());
        values.put(TGroupInfo._GROUP_CREATOR, info.getGroupCreator());
        values.put(TGroupInfo._GROUP_PHOTO, info.getGroupPhoto());
        values.put(TGroupInfo._GROUP_TYPE, info.getGroupType());
        values.put(TGroupInfo._GROUP_SHIELD, info.getGroupShieldTag());

        int ret = sqliteDB().update(TABLE_GROUP_INFO, values,TGroupInfo._GROUP_NAME + " = '" + info.getGroupName() + "'", null);
        CommFunc.PrintLog(5, LOGTAG, "updateGroupByGrpName ret:"+ret+" members:"+info.getGroupMembers());
        if (dbFlag) {
            dbChanged(new TGroupInfo());
        }
    }
    /**
     * 更新某一群组信息
     */
    public void updateGroup(TGroupInfo info, boolean dbFlag) {
        ContentValues values = new ContentValues();
        values.put(TGroupInfo._GROUP_NAME, info.getGroupName());
        values.put(TGroupInfo._GROUP_MEMBERS, info.getGroupMembers());
        values.put(TGroupInfo._GROUP_CREATE_TIME, info.getGroupCreateTime());
        values.put(TGroupInfo._GROUP_CREATOR, info.getGroupCreator());
        values.put(TGroupInfo._GROUP_PHOTO, info.getGroupPhoto());
        values.put(TGroupInfo._GROUP_TYPE, info.getGroupType());
        values.put(TGroupInfo._GROUP_SHIELD, info.getGroupShieldTag());

        int ret = sqliteDB().update(TABLE_GROUP_INFO, values,TGroupInfo._GROUP_NAME + " = '" + info.getGroupName() + "' and " + TGroupInfo._GROUP_CREATOR + " = '" + info.getGroupCreator() + "'", null);
        CommFunc.PrintLog(5, LOGTAG, "updateGroup ret:"+ret+" members:"+info.getGroupMembers());
        if (dbFlag) {
            dbChanged(new TGroupInfo());
        }
    }

    /**
     * 更新某一群组信息的某一项
     * @param groupId
     * @param key
     * @param value
     * @param dbFlag
     */
    public void updateGroupInfo(String groupId, String key, String value, boolean dbFlag) {

        ContentValues values = new ContentValues();
        values.put(key, value);
        {
            CommFunc.PrintLog(5, LOGTAG, "updateGroupInfo key:"+key+" value:"+value);
        }
        int ret = sqliteDB().update(TABLE_GROUP_INFO, values, TGroupInfo._GROUP_ID + " = '" + groupId + "'", null);
        CommFunc.PrintLog(5, LOGTAG, "updateGroupInfo ret:"+ret);
        if (dbFlag) {
            dbChanged(new TGroupInfo());
        }
    }

    /**
     * 更新某一群组信息的某一项
     * @param groupId
     * @param key
     * @param value
     * @param dbFlag
     */
    public void updateGroupInfo(String groupId, String key, int value, boolean dbFlag) {
        /*String sql = "update " + TABLE_GROUP_INFO + " set " + key + " = " + value + " where " + TGroupInfo._GROUP_ID + " = '" + groupId + "'";
		sqliteDB().execSQL(sql);
		if (dbFlag)
			dbChanged(new TGroupInfo());*/
        ContentValues values = new ContentValues();
        values.put(key, value);
        sqliteDB().update(TABLE_GROUP_INFO, values, TGroupInfo._GROUP_ID + " = '" + groupId + "'", null);
        if (dbFlag) {
            dbChanged(new TGroupInfo());
        }
    }

    /**
     * 添加联系人到多个群组
     * @param set
     * @param number
     */
    public void addMemberToGroups(Set<String> set, String number) {
        Iterator<String> it = set.iterator();  
        while (it.hasNext()) {
            TGroupInfo groupInfo = getGroupById(it.next());
            groupInfo.setGroupMembers(groupInfo.getGroupMembers() + ";" + number);
            updateGroup(groupInfo, false);
            //updateGroupInfo(groupInfo.getGroupId(), TGroupInfo._GROUP_MEMBERS, groupInfo.getGroupMembers(), false);
        }
        dbChanged(new TGroupInfo());
    }

    /**
     * 删除某一群组
     * @param groupId
     * @param dbFlag
     */
    public void deleteGroupInfoById(String groupId, boolean dbFlag){
        sqliteDB().delete(TABLE_GROUP_INFO, TGroupInfo._GROUP_ID + " = '" + groupId + "'", null);
        if (dbFlag) {
            dbChanged(new TGroupInfo());
        }
    }

    /**
     * 删除组名相同、创建者相同且群组id不是groupId 的群组
     * @param groupId
     * @param dbFlag
     */
    public void deleteGroupInfo(String groupName, String creator, String groupId, boolean dbFlag){
        sqliteDB().delete(TABLE_GROUP_INFO, TGroupInfo._GROUP_NAME + " = '" + groupName + "' and " + TGroupInfo._GROUP_CREATOR + " = '" + creator + "' and " + TGroupInfo._GROUP_ID + " <> '" + groupId + "'", null);
        if (dbFlag) {
            dbChanged(new TGroupInfo());
        }
    }

    /**
     * 删除多个群组
     * @param list
     */
    public void deleteGroupInfoList(Set<String> set) {
        Iterator<String> it = set.iterator();  
        while (it.hasNext()) {  
            deleteGroupInfoById(it.next(), false);
        } 
        dbChanged(new TGroupInfo());
    }

    /**
     * 删除所有群组信息
     * @param dbFlag
     */
    public void deleteAllGroupInfo(boolean dbFlag) {
        sqliteDB().delete(TABLE_GROUP_INFO, null, null);
        if (dbFlag) {
            dbChanged(new TGroupInfo());
        }
    }

    //    public int getContactCount()
    //    { 
    //        // TODO Auto-generated method stub
    //        int count = 0;
    //        String sql = "select * from " + TABLE_CONTACT_INFO + " order by " + TContactInfo._CONTACT_SORT_KEY + " desc";
    //        Cursor cursor = sqliteDB().rawQuery(sql, null);
    //        try{
    //            if(cursor!=null)
    //            {
    //                count =cursor.getCount();
    //            }
    //
    //        }
    //        finally {
    //            if(cursor!=null)
    //            {
    //                cursor.close();
    //                cursor = null;
    //            }
    //        }
    //        return count;
    //    }
    /**
     * 获取所联系人信息
     * @return
     */
    public List<TContactInfo> getAllContactInfo() {
        List<TContactInfo> list = new ArrayList<TContactInfo>();
        // TODO Auto-generated method stub
        String sql = "select * from " + TABLE_CONTACT_INFO;// + " order by " + TContactInfo._CONTACT_SORT_KEY + " asc";
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{
            while( cursor != null && cursor.moveToNext()){
                TContactInfo info = new TContactInfo();
                info.setContactId(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_ID)));
                info.setName(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_NAME)));
                info.setPhoneNum(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_NUMBER)));
                info.setFirstChar(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_SORT_KEY)));
                info.setPhotoId(cursor.getLong(cursor.getColumnIndex(TContactInfo._CONTACT_PHOTO_ID)));
                info.setLookUpKey(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_LOOK_UP_KEY)));
                info.setUsertype(cursor.getInt(cursor.getColumnIndex(TContactInfo._CONTACT_USERTYPE)));
                list.add(info);
            }            
        }
        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }

    public List<TContactInfo> getContactInfo_Search(String key)
    {
        List<TContactInfo> list = new ArrayList<TContactInfo>(); 

        String causeString = "";
        String sql = "";

        if(key==null|| key.equals("")==true)
        {
            sql = "select * from " + TABLE_CONTACT_INFO + " order by " + TContactInfo._CONTACT_SORT_KEY + " asc";
        }
        else 
        {
            causeString += "(" + TContactInfo._CONTACT_NAME + " like '%" + key
            + "%' or " + TContactInfo._CONTACT_NUMBER + " like '%" + key
            + "%' or " + TContactInfo._CONTACT_SORT_KEY + " like '%" + key
            + "%')";
            sql = "select * from " + TABLE_CONTACT_INFO + " where "+ causeString;
        }

        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{
            while(cursor!=null && cursor.moveToNext()){
                TContactInfo info = new TContactInfo();
                info.setContactId(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_ID)));
                info.setName(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_NAME)));
                info.setPhoneNum(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_NUMBER)));
                info.setFirstChar(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_SORT_KEY)));
                info.setPhotoId(cursor.getLong(cursor.getColumnIndex(TContactInfo._CONTACT_PHOTO_ID)));
                info.setLookUpKey(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_LOOK_UP_KEY)));
                info.setUsertype(cursor.getInt(cursor.getColumnIndex(TContactInfo._CONTACT_USERTYPE)));
                list.add(info);
            }   
        }

        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }
    /**
     * 获取除了数组中的联系人以外的所有联系人
     * @return
     */
    public List<TContactInfo> getContactInfo(String members) {
        List<TContactInfo> list = new ArrayList<TContactInfo>();
        String sql = "select * from " + TABLE_CONTACT_INFO + " where " + TContactInfo._CONTACT_NUMBER + " not in(" + members  + ") order by " + TContactInfo._CONTACT_SORT_KEY + " asc";
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{
            while(cursor!=null && cursor.moveToNext()){
                TContactInfo info = new TContactInfo();
                info.setContactId(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_ID)));
                info.setName(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_NAME)));
                info.setPhoneNum(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_NUMBER)));
                info.setFirstChar(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_SORT_KEY)));
                info.setPhotoId(cursor.getLong(cursor.getColumnIndex(TContactInfo._CONTACT_PHOTO_ID)));
                info.setLookUpKey(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_LOOK_UP_KEY)));
                info.setUsertype(cursor.getInt(cursor.getColumnIndex(TContactInfo._CONTACT_USERTYPE)));
                list.add(info);
            }    
        }

        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }

    /**
     * 根据联系人ID获取联系人
     * @param contactId
     * @return
     */
    public TContactInfo getContactInfoById(String contactId) {
        TContactInfo info = null;
        String sql = "select * from " + TABLE_CONTACT_INFO + " where " + TContactInfo._CONTACT_ID + " ='" + contactId + "'";
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{
            if(cursor!=null && cursor.moveToNext()){
                info = new TContactInfo();
                info.setContactId(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_ID)));
                info.setName(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_NAME)));
                info.setPhoneNum(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_NUMBER)));
                info.setFirstChar(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_SORT_KEY)));
                info.setPhotoId(cursor.getLong(cursor.getColumnIndex(TContactInfo._CONTACT_PHOTO_ID)));
                info.setLookUpKey(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_LOOK_UP_KEY)));
                info.setUsertype(cursor.getInt(cursor.getColumnIndex(TContactInfo._CONTACT_USERTYPE)));
            }  
        }

        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        return info;
    }

    /**
     * 根据联系人号码获取联系人
     * @param contactId
     * @return
     */
    public TContactInfo getContactInfoByNumber(String number) {
        TContactInfo info = null;
        String sql = "select * from " + TABLE_CONTACT_INFO + " where " + TContactInfo._CONTACT_NUMBER + " ='" + number + "'";
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{

            if(cursor!=null && cursor.moveToNext()){
                info = new TContactInfo();
                info.setContactId(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_ID)));
                info.setName(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_NAME)));
                info.setPhoneNum(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_NUMBER)));
                info.setFirstChar(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_SORT_KEY)));
                info.setPhotoId(cursor.getLong(cursor.getColumnIndex(TContactInfo._CONTACT_PHOTO_ID)));
                info.setLookUpKey(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_LOOK_UP_KEY)));
                info.setUsertype(cursor.getInt(cursor.getColumnIndex(TContactInfo._CONTACT_USERTYPE)));
            } 

        }

        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        if (info == null) {
            String[] pinyin = PinYinManager.toPinYin(number);
            info = new TContactInfo();
            info.setName(number);
            info.setPhoneNum(number);
            info.setContactId(number);
            info.setPhotoId(null);
            info.setFirstChar(pinyin[0]);
            info.setLookUpKey(pinyin[1]);
            info.setUsertype(SysConfig.USERTYPE_SYSTEM);
            saveContactInfo(info,true); 
        }

        return info;
    }

    /**
     * 根据联系人名称获取联系人
     * TODO 需要更改
     * @param contactId
     * @return
     */
    public TContactInfo getContactInfoByName(String name) {
        TContactInfo info = new TContactInfo();
        String sql = "select * from " + TABLE_CONTACT_INFO + " where " + TContactInfo._CONTACT_NAME + " ='" + name + "'";
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{
            if(cursor!=null && cursor.moveToNext()){
                info.setContactId(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_ID)));
                info.setName(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_NAME)));
                info.setPhoneNum(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_NUMBER)));
                info.setFirstChar(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_SORT_KEY)));
                info.setPhotoId(cursor.getLong(cursor.getColumnIndex(TContactInfo._CONTACT_PHOTO_ID)));
                info.setLookUpKey(cursor.getString(cursor.getColumnIndex(TContactInfo._CONTACT_LOOK_UP_KEY)));
                info.setUsertype(cursor.getInt(cursor.getColumnIndex(TContactInfo._CONTACT_USERTYPE)));
            }    
        }

        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        return info;
    }

    /**
     * 添加多条联系人
     * @param list
     */
    public void saveContactInfoList(final List<TContactInfo> list, final boolean dbFlag) {
        HBaseApp.post2WorkRunnable(new Runnable (){

            @Override
            public void run() {
                // TODO Auto-generated method stub
                for (int i = 0; i < list.size(); i++) {
                    saveContactInfo(list.get(i), dbFlag);
                } 
            }

        });

    }


    public void SaveContactByID(String userId) {
        TContactInfo info = getContactInfoByNumber(userId);
        if (info == null) {
            String[] pinyin = PinYinManager.toPinYin(userId);
            TContactInfo contactInfo = new TContactInfo();
            contactInfo.setName(userId);
            contactInfo.setPhoneNum(userId);
            contactInfo.setContactId(userId);
            contactInfo.setPhotoId(null);
            contactInfo.setFirstChar(pinyin[0]);
            contactInfo.setLookUpKey(pinyin[1]);
            contactInfo.setUsertype(SysConfig.USERTYPE_SYSTEM);
            saveContactInfo(contactInfo,true); 
        }
    }

    //从服务器返回的用户信息进行刷新操作
    //如果系统通讯录有 天翼账号类型 登陆 算作天翼账号 但天翼账号的name 如果系统通讯录有采用系统通讯录。（天翼账号没有返回name）
    //新浪微博可以取到name
    public void save_updateContactList(List<TContactInfo> list)
    {
        for (int i = 0; i < list.size(); i++) {

            saveContactInfo(list.get(i), false);
        }
        dbChanged(new TContactInfo()); //全部保存后刷新
    }
    /**
     * 添加一条联系人信息
     * @param info
     */
    public void saveContactInfo(TContactInfo info, boolean dbFlag){
        //  CommFunc.PrintLog(5, LOGTAG, "name:" + info.getName());
        if(info.getPhoneNum().length()==0 || info.getPhoneNum() == null)
            return;
        {
            ContentValues values = new ContentValues();
            values.put(TContactInfo._CONTACT_ID, info.getContactId());
            values.put(TContactInfo._CONTACT_NAME, info.getName());
            values.put(TContactInfo._CONTACT_NUMBER, info.getPhoneNum());
            values.put(TContactInfo._CONTACT_SORT_KEY, info.getFirstChar());
            values.put(TContactInfo._CONTACT_PHOTO_ID, info.getPhotoId());
            values.put(TContactInfo._CONTACT_LOOK_UP_KEY, info.getLookUpKey());
            values.put(TContactInfo._CONTACT_USERTYPE, info.getUsertype());

            //CommFunc.PrintLog(5, LOGTAG, "saveContactInfo name:"+info.getName()+" number:"+info.getPhoneNum());
            sqliteDB().replaceOrThrow(TABLE_CONTACT_INFO, null, values);
            //   CommFunc.PrintLog(5, LOGTAG, "getContactId:"+info.getContactId() +" name:"+info.getName()+"  number:"+info.getPhoneNum());

        }
//        if (dbFlag) {
//            dbChanged(new TContactInfo());
//        }
    }

    /**
     * 更新联系人信息
     * @param info
     */
    public void updateContactInfo(TContactInfo info, boolean dbFlag){
        ContentValues values = new ContentValues();
        values.put(TContactInfo._CONTACT_ID, info.getContactId());
        values.put(TContactInfo._CONTACT_NAME, info.getName());
        values.put(TContactInfo._CONTACT_NUMBER, info.getPhoneNum());
        values.put(TContactInfo._CONTACT_SORT_KEY, info.getFirstChar());
        values.put(TContactInfo._CONTACT_PHOTO_ID, info.getPhotoId());
        values.put(TContactInfo._CONTACT_LOOK_UP_KEY, info.getLookUpKey());
        values.put(TContactInfo._CONTACT_USERTYPE, info.getUsertype());
        sqliteDB().update(TABLE_CONTACT_INFO, values,TContactInfo._CONTACT_ID + " = '" + info.getContactId() + "'", null);
        if (dbFlag) {
            dbChanged(new TContactInfo());
        }
    }

    /**
     * 更新联系人信息的某一项 value为字符串类型
     * @param id
     * @param key
     * @param value
     * @param dbFlag
     */
    public void updateContactInfo(String contactId, String key, String value, boolean dbFlag){
        ContentValues values = new ContentValues();
        values.put(key, value);
        sqliteDB().update(TABLE_CONTACT_INFO, values, key + " = '" + value + "'", null);
        if (dbFlag) {
            dbChanged(new TContactInfo());
        }
    }

    /**
     * 根据联系人ID删除联系人信息
     * @param contactId
     */
    public void deltetContactInfo(String contactId, boolean dbFlag){
        sqliteDB().delete(TABLE_CONTACT_INFO, TContactInfo._CONTACT_ID + " = '" + contactId + "'", null);
        if (dbFlag) {
            dbChanged(new TContactInfo());
        }
    }

    /**
     * 删除所有联系人信息
     */
    public void deleteAllContactInfo(boolean dbFlag) {
        sqliteDB().delete(TABLE_CONTACT_INFO, null, null);
        if (dbFlag) {
            dbChanged(new TContactInfo());
        }
    }

    /**
     * 保存一条群组通话记录
     * @param info
     * @param dbFlag
     */
    public void saveGroupRecordInfo(TGroupRecordInfo info, boolean dbFlag) {
        ContentValues values = new ContentValues();
        values.put(TGroupRecordInfo._GROUP_CALL_ID, info.getCallId());
        values.put(TGroupRecordInfo._GROUP_ID, info.getGroupId());
        values.put(TGroupRecordInfo._STARTTIME, info.getStartTime());
        values.put(TGroupRecordInfo._ENDTIME, info.getEndTime());
        values.put(TGroupRecordInfo._TIME, info.getTime());
        values.put(TGroupRecordInfo._CONF_TYPE, info.getConftype());
        values.put(TGroupRecordInfo._JOIN_RESULT, info.getJoinResult());
        values.put(TGroupRecordInfo._DURATION, info.getDuration());
        values.put(TGroupRecordInfo._START_DATE, info.getStartDate());
        values.put(TGroupRecordInfo._END_DATE, info.getEndDate());
        sqliteDB().replaceOrThrow(TABLE_GROUP_RECORD_INFO, null, values);
        if (dbFlag) {
            dbChanged(info);
        }
    }

    /**
     * 根据群组ID获取所有通话记录
     * @param groupoId
     */
    public List<TGroupRecordInfo> getGroupRecordInfo(String groupoId) {
        List<TGroupRecordInfo> list = new ArrayList<TGroupRecordInfo>();
        String sql = "select * from " + TABLE_GROUP_RECORD_INFO + " where " + TGroupRecordInfo._GROUP_ID + " ='" + groupoId + "'" + " order by " + TGroupRecordInfo._START_DATE + " desc, " +  TGroupRecordInfo._STARTTIME + " desc";
        Cursor cursor = sqliteDB().rawQuery(sql, null);
        try{
            while(cursor!=null && cursor.moveToNext()){
                TGroupRecordInfo info = new TGroupRecordInfo();
                info.setGroupId(cursor.getString(cursor.getColumnIndex(TGroupRecordInfo._GROUP_ID)));
                info.setCallId(cursor.getString(cursor.getColumnIndex(TGroupRecordInfo._GROUP_CALL_ID)));
                info.setStartTime(cursor.getString(cursor.getColumnIndex(TGroupRecordInfo._STARTTIME)));
                info.setEndTime(cursor.getString(cursor.getColumnIndex(TGroupRecordInfo._ENDTIME)));
                info.setTime(cursor.getString(cursor.getColumnIndex(TGroupRecordInfo._TIME)));
                info.setJoinResult(cursor.getInt(cursor.getColumnIndex(TGroupRecordInfo._JOIN_RESULT)));
                info.setConftype(cursor.getInt(cursor.getColumnIndex(TGroupRecordInfo._CONF_TYPE)));
                info.setDuration(cursor.getInt(cursor.getColumnIndex(TGroupRecordInfo._DURATION)));
                info.setStartDate(cursor.getString(cursor.getColumnIndex(TGroupRecordInfo._START_DATE)));
                info.setEndDate(cursor.getString(cursor.getColumnIndex(TGroupRecordInfo._END_DATE)));
                list.add(info);
            }   
        }

        finally {
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }
        return list;
    }

    /**
     * 更新一条群组通话记录
     * @param info
     * @param dbFlag
     */
    public void updateGroupRecordInfo(TGroupRecordInfo info, boolean dbFlag) {
        ContentValues values = new ContentValues();
        values.put(TGroupRecordInfo._GROUP_CALL_ID, info.getCallId());
        values.put(TGroupRecordInfo._GROUP_ID, info.getGroupId());
        values.put(TGroupRecordInfo._STARTTIME, info.getStartTime());
        values.put(TGroupRecordInfo._ENDTIME, info.getEndTime());
        values.put(TGroupRecordInfo._TIME, info.getTime());
        values.put(TGroupRecordInfo._CONF_TYPE, info.getConftype());
        values.put(TGroupRecordInfo._JOIN_RESULT, info.getJoinResult());
        values.put(TGroupRecordInfo._DURATION, info.getDuration());
        values.put(TGroupRecordInfo._START_DATE, info.getStartDate());
        values.put(TGroupRecordInfo._END_DATE, info.getEndDate());

        sqliteDB().update(TABLE_GROUP_RECORD_INFO, values,TGroupRecordInfo._GROUP_CALL_ID + " = '" + info.getCallId() + "'", null);
        if (dbFlag) {
            dbChanged(new TGroupRecordInfo());
        }
    }

    /**
     * 根据通话记录ID更新一条字符串类型的通话记录数据
     * @param groupId
     * @param values
     * @param dbFlag
     */
    public void updateGroupRecordInfo(String groupCallID, String key, String value, boolean dbFlag) {
        ContentValues values = new ContentValues();
        values.put(key, value);
        sqliteDB().update(TABLE_GROUP_RECORD_INFO, values, key + " = '" + value + "'", null);
        if (dbFlag) {
            dbChanged(new TGroupRecordInfo());
        }
    }

    /**
     * 根据通话记录ID更新一条整形的通话记录数据
     * @param groupCallID
     * @param values
     * @param dbFlag
     */
    public void updateGroupRecordInfo(String groupCallID, String key, int value, boolean dbFlag) {
        ContentValues values = new ContentValues();
        values.put(key, value);
        sqliteDB().update(TABLE_GROUP_RECORD_INFO, values, key + " = " + value, null);
        if (dbFlag) {
            dbChanged(new TGroupRecordInfo());
        }
    }

    /**
     * 根据通话记录ID删除一条通话记录
     * @param info
     * @param dbFlag
     */
    public void deleteGroupRecordInfo(String groupCallID, boolean dbFlag) {
        sqliteDB().delete(TABLE_GROUP_RECORD_INFO, TGroupRecordInfo._GROUP_CALL_ID + " = '" + groupCallID + "'", null);
        if (dbFlag) {
            dbChanged(new TGroupRecordInfo());
        }
    }

    /**
     * 根据群组ID删除此群组的所有通话记录
     * @param dbFlag
     */
    public void delteAllGroupRecordInfo(String groupID, boolean dbFlag) {
        sqliteDB().delete(TABLE_GROUP_RECORD_INFO, TGroupRecordInfo._GROUP_ID + " = '" + groupID + "'", null);
        if (dbFlag) {
            dbChanged(new TGroupRecordInfo());
        }
    }

    /**
     * 关闭SQLiteDatabase
     */
    public void clearInstance() {
        if (sqliteDB != null)
            sqliteDB.close();
    }

    public void dbChanged(final Object object) {
        HBaseApp.post2UIRunnable(new Runnable(){

            @Override
            public void run() {
                // TODO Auto-generated method stub
                setChanged();
                notifyObservers(object); 
            }

        });

    }

}
