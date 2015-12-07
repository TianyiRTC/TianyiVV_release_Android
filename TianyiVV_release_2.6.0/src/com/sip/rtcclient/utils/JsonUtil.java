package com.sip.rtcclient.utils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;



public class JsonUtil {
	 private static String LOGTAG = "JsonUtil";

	/**
	 * 获取整数 带默认值
	 * @param json
	 * @param key
	 * @param nDef
	 * @return
	 */
	public static int getInt(JSONObject json,String key,int nDef) {
		try {
			nDef = json.getInt(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return nDef;
	}
	
	/**
	 * 获取boolean 带默认值
	 * @param json
	 * @param key
	 * @param nDef
	 * @return
	 */
	public static long getLong(JSONObject json,String key,long nDef) {
		try {
			nDef = json.getLong(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return nDef;
	}

	/**
	 * 获取boolean 带默认值
	 * @param json
	 * @param key
	 * @param bDef
	 * @return
	 */
	public static boolean getBoolean(JSONObject json,String key,boolean bDef) {
		try {
			bDef = json.getBoolean(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return bDef;
	}

	/**
	 * 获取double 带默认值
	 * @param json
	 * @param key
	 * @param fDef
	 * @return
	 */
	public static double getDouble(JSONObject json,String key,double fDef) {
		try {
			fDef = json.getDouble(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return fDef;
	}

	/**
	 * 获取字符串 带默认值
	 * @param json
	 * @param key
	 * @param sDef
	 * @return
	 */
	public static String getString(JSONObject json,String key,String sDef) {
		try {
			sDef = json.getString(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return sDef;
	}

	/**
	 * 获取对象 带默认值
	 * @param json
	 * @param key
	 * @param jDef
	 * @return
	 */
	public static JSONObject getObject(JSONObject json,String key,JSONObject jDef) {
		try {
			jDef = json.getJSONObject(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jDef;
	}
	
	/**
	 * 获取数组 带默认值
	 * @param json
	 * @param key
	 * @param jDef
	 * @return
	 */
	public static JSONArray getArray(JSONObject json,String key,JSONArray jDef) {
		try {
			jDef = json.getJSONArray(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jDef;
	}
	
	/**
	 * 判断一个jsonarray是否为null
	 * 
	 * @param json
	 * @param data
	 * @return
	 */
	public static boolean isNullJson(JSONObject json, String data) {
		String obj;
		try {
			obj = json.getString(data);
			if (obj != null && obj.equals("null") == false) {
				return false;
			}
		} catch (JSONException e) {
		    CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(
					R.string.exception_json));
			return true;
		}
		return true;
	}

}