package com.oauth2.weibo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class OAuthSharepreference {
	private final static String SHAREPREFERENCE_NAME = "OAuthSharePreference";
	private final static String KEY_TOKEN = "token";
	private final static String KEY_EXPIRES = "expires_in";
	private final static String KEY_UID= "uid";
	private final static String KEY_UNAME= "uname";
	public OAuthSharepreference(){
		
	}
	public static boolean setToken(Context context, String token)
	{
		SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREPREFERENCE_NAME, 0);
		Editor editor = sharedPreferences.edit();
		editor.putString(KEY_TOKEN, token);
		return editor.commit();
	}
	public static String getToken(Context context)
	{
		SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREPREFERENCE_NAME, 0);
		return sharedPreferences.getString(KEY_TOKEN, "");
	}

	public static boolean setExpires(Context context, String expires)
	{
		SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREPREFERENCE_NAME, 0);
		Editor editor = sharedPreferences.edit();
		editor.putString(KEY_EXPIRES, expires);
		
		return editor.commit();
	}
	public static String getExpires(Context context)
	{
		SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREPREFERENCE_NAME, 0);
		return sharedPreferences.getString(KEY_EXPIRES, "");
	}

	public static boolean setUid(Context context, String uid)
	{
		SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREPREFERENCE_NAME, 0);
		Editor editor = sharedPreferences.edit();
		editor.putString(KEY_UID, uid);
		return editor.commit();
	}
	public static String getUid(Context context)
	{
		SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREPREFERENCE_NAME, 0);
		return sharedPreferences.getString(KEY_UID, "");
	}
	
	   public static boolean setUname(Context context, String uname)
	    {
	        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREPREFERENCE_NAME, 0);
	        Editor editor = sharedPreferences.edit();
	        editor.putString(KEY_UNAME, uname);
	        return editor.commit();
	    }
	    public static String getUname(Context context)
	    {
	        SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREPREFERENCE_NAME, 0);
	        return sharedPreferences.getString(KEY_UNAME, "");
	    }
}
