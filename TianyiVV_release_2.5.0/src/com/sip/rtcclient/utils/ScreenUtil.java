package com.sip.rtcclient.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.sip.rtcclientouter.R;


public class ScreenUtil {

    private static String LOGTAG="ScreenUtil";
	/**
	 * 获取屏幕宽度
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenWidth(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getApplicationContext().getResources().getDisplayMetrics();
		int width = dm.widthPixels;
		return width;
	}

	/**
	 * 获取屏幕高度
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenHeight(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getApplicationContext().getResources().getDisplayMetrics();
		int height = dm.heightPixels;
		return height;
	}

	/**
	 * 获取屏幕密度
	 * 
	 * @param context
	 * @return
	 */
	public static float getScreenDensity(Context context) {
		try {
			DisplayMetrics dm = new DisplayMetrics();
			WindowManager manager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			manager.getDefaultDisplay().getMetrics(dm);
			return dm.density;
		} catch (Exception e) {
		    CommFunc.PrintLog(1,LOGTAG,context.getString(R.string.exception_screen_ensity));
		}
		return 1.0f;
	}

	/**
	 * 获取屏幕尺寸
	 * 
	 * @param context
	 * @return
	 */
	public static String getScreenResolution(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getApplicationContext().getResources().getDisplayMetrics();
		StringBuffer sb = new StringBuffer(Integer.toString(dm.widthPixels))
				.append("*").append(Integer.toString(dm.heightPixels));
		return sb.toString();

	}

}
