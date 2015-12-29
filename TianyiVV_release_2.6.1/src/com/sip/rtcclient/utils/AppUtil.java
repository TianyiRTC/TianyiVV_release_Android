package com.sip.rtcclient.utils;


import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import java.util.Enumeration;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;

public class AppUtil {


	private static TelephonyManager telephonyManager;
    private static String LOGTAG = "AppUtil";

	

	/**
	 * 初始化
	 */
	public void init() {
		if (!FileUtil.checkSDCard()) {
			return;
		}
	}



	/**
	 * 获取手机信息管理
	 * 
	 * @return
	 */
	public static TelephonyManager getTelephonyManager() {
		if (telephonyManager == null) {
			telephonyManager = (TelephonyManager) MyApplication.getInstance()
					.getSystemService(MyApplication.TELEPHONY_SERVICE);
		}
		return telephonyManager;
	}

	/**
	 * 获取手机串号
	 * 
	 * @return
	 */
	public static String getIMEI() {
		return getTelephonyManager().getDeviceId();
	}

	/**
	 * 获取设备名
	 * 
	 * @return
	 */
	public static String getDevice() {
		return Build.MODEL;
	}

	/**
	 * 获取设备厂商
	 * 
	 * @return
	 */
	public static String getVendor() {
		return Build.BRAND;
	}

	/**
	 * 获取设备SDK版本号
	 * 
	 * @return
	 */
	public static int getSDKVersion() {
		return Build.VERSION.SDK_INT;
	}

	/**
	 * 获取OS版本号
	 * 
	 * @return
	 */
	public static String getAndroidVersion() {
		return Build.VERSION.RELEASE;
	}

	/**
	 * 获取设备序列号
	 * 
	 * @return
	 */
	public static String getSerialNumber() {
		String serial = null;
		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);
			serial = (String) get.invoke(c, "ro.serialno");
		} catch (Exception e) {
		    CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(
					R.string.exception_serialnumber));
		}
		return serial;
	}

	/**
	 * 获取设备信息
	 * 
	 * @param context
	 */
	public static String ReadDeviceInfo(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		StringBuilder sb = new StringBuilder();
		sb.append("\nDeviceId(IMEI) = " + tm.getDeviceId());
		sb.append("\nDeviceSoftwareVersion = " + tm.getDeviceSoftwareVersion());
		sb.append("\nLine1Number = " + tm.getLine1Number());
		sb.append("\nNetworkCountryIso = " + tm.getNetworkCountryIso());
		sb.append("\nNetworkOperator = " + tm.getNetworkOperator());
		sb.append("\nNetworkOperatorName = " + tm.getNetworkOperatorName());
		sb.append("\nNetworkType = " + tm.getNetworkType());
		sb.append("\nPhoneType = " + tm.getPhoneType());
		sb.append("\nSimCountryIso = " + tm.getSimCountryIso());
		sb.append("\nSimOperator = " + tm.getSimOperator());
		sb.append("\nSimOperatorName = " + tm.getSimOperatorName());
		sb.append("\nSimSerialNumber = " + tm.getSimSerialNumber());
		sb.append("\nSimState = " + tm.getSimState());
		sb.append("\nSubscriberId(IMSI) = " + tm.getSubscriberId());
		sb.append("\nVoiceMailNumber = " + tm.getVoiceMailNumber());

		return sb.toString();
	}

	/**
	 * 获取设备IP
	 */
	public static String getIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException e) {
		    CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(
					R.string.exception_ip_address));
			return null;
		}
		return null;
	}

}
