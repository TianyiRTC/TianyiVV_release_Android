package com.sip.rtcclient.account;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Base64Encoder

{

	private static final char[] legalChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
			.toCharArray();

	public static String encode(byte[] data) {
		return encode(data, data.length);
	}
	
	public static String encode(byte[] data, int len) {

		int start = 0;

		StringBuffer buf = new StringBuffer(len * 3 / 2);

		int end = len - 3;

		int i = start;

		int n = 0;

		while (i <= end) {

			int d = ((((int) data[i]) & 0x0ff) << 16)

			| ((((int) data[i + 1]) & 0x0ff) << 8)

			| (((int) data[i + 2]) & 0x0ff);

			buf.append(legalChars[(d >> 18) & 63]);

			buf.append(legalChars[(d >> 12) & 63]);

			buf.append(legalChars[(d >> 6) & 63]);

			buf.append(legalChars[d & 63]);

			i += 3;

			if (n++ >= 14) {

				n = 0;

				buf.append(" ");

			}

		}

		if (i == start + len - 2) {

			int d = ((((int) data[i]) & 0x0ff) << 16)

			| ((((int) data[i + 1]) & 255) << 8);

			buf.append(legalChars[(d >> 18) & 63]);

			buf.append(legalChars[(d >> 12) & 63]);

			buf.append(legalChars[(d >> 6) & 63]);

			buf.append("=");

		} else if (i == start + len - 1) {

			int d = (((int) data[i]) & 0x0ff) << 16;

			buf.append(legalChars[(d >> 18) & 63]);

			buf.append(legalChars[(d >> 12) & 63]);

			buf.append("==");

		}

		return buf.toString();

	}
	
	public static String encode(InputStream in) throws IOException {
		byte[] buffer = new byte[4096];
		int len = 0;
		StringBuffer strBuffer = new StringBuffer();
		while ((len = in.read(buffer))>0) {
			strBuffer.append(encode(buffer,len));
		}
		
		return strBuffer.toString();
	}
	
	private static int decode(char c) {

        if (c >= 'A' && c <= 'Z')

            return ((int) c) - 65;

        else if (c >= 'a' && c <= 'z')

            return ((int) c) - 97 + 26;

        else if (c >= '0' && c <= '9')

            return ((int) c) - 48 + 26 + 26;

        else

            switch (c) {

            case '+':

                return 62;

            case '/':

                return 63;

            case '=':

                return 0;

            default:

                throw new RuntimeException("unexpected code: " + c);

            }

    }


	
	 public static byte[] decode(String s) {

		 

         ByteArrayOutputStream bos = new ByteArrayOutputStream();

         try {

             decode(s, bos);

         } catch (IOException e) {

             throw new RuntimeException();

         }

         byte[] decodedBytes = bos.toByteArray();

         try {

             bos.close();

             bos = null;

         } catch (IOException ex) {

             System.err.println("Error while decoding BASE64: " + ex.toString());

         }

         return decodedBytes;

     }

 

     private static void decode(String s, OutputStream os) throws IOException {

         int i = 0;

 

         int len = s.length();

 

         while (true) {

             while (i < len && s.charAt(i) <= ' ')

                 i++;

 

             if (i == len)

                 break;

 

             int tri = (decode(s.charAt(i)) << 18)

                     + (decode(s.charAt(i + 1)) << 12)

                     + (decode(s.charAt(i + 2)) << 6)

                     + (decode(s.charAt(i + 3)));

 

             os.write((tri >> 16) & 255);

             if (s.charAt(i + 2) == '=')

                 break;

             os.write((tri >> 8) & 255);

             if (s.charAt(i + 3) == '=')

                 break;

             os.write(tri & 255);

 

             i += 4;

         }

     }


}
