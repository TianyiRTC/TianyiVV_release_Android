package com.sip.rtcclient.account;

public class Base64Encrypt {
	public static String getBASE64(String s) {
		if (s == null)
			return null;
		return Base64Encoder.encode(s.getBytes());
	}

	public static String getBASE64_byte(byte[] s) {
		if (s == null)
			return null;
		return Base64Encoder.encode(s);
	}
	
	public static byte[] getByteArrFromBase64(String s) throws Exception{
		if (s == null)
			return null;
		return Base64Encoder.decode(s);
	}
}
