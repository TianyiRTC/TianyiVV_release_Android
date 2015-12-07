package com.sip.rtcclient.account;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CTAUtils {

	public static String getIMSI(Context context) {

		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = tm.getSubscriberId();
		return imsi;
	}

	public static boolean isNull(String string) {
		if (string == null || "".equals(string.trim())) {
			return true;
		}
		return false;
	}

	public static byte[] getBytes(InputStream in) throws IOException {
		byte[] buffer = new byte[4096];
		int len = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while ((len = in.read(buffer)) > 0) {
			out.write(buffer, 0, len);
		}

		buffer = out.toByteArray();
		out.close();
		return buffer;
	}

	public static String getIpAddress() {
		try {
			for (Enumeration<?> en = NetworkInterface.getNetworkInterfaces(); en
					.hasMoreElements();) {
				NetworkInterface intf = (NetworkInterface) en.nextElement();
				for (Enumeration<?> enumIpAddr = intf.getInetAddresses(); enumIpAddr
						.hasMoreElements();) {
					InetAddress inetAddress = (InetAddress) enumIpAddr
							.nextElement();
					if (!inetAddress.isLoopbackAddress()) {

						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("system", ex.toString());
		}
		return null;
	}
	public static String getEAccountDeviceNo(){
		return "3500000000408101";
	}
	public static String getKey(){
		return "62AAB900DFE4BDD34B6F8B1032BB69B8395CA7EC54BA3335";
	}
	public static String getTimeStamp()
	{
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
	}
}
