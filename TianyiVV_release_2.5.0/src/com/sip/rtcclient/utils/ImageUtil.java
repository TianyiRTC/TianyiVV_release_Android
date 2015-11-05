package com.sip.rtcclient.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;

public class ImageUtil {
    
    private static String LOGTAG = "ImageUtil";
	/**
	 * 保存图片到SDCard中
	 * 
	 * @param path
	 * @param bitmap
	 * @return
	 */
	public static String savePictrueToSDCard(String path, Bitmap bitmap) {
		try {
			File file = new File(path);
			if (!file.exists()) {
				file.createNewFile();
			} else {
				return path;
			}
			BufferedOutputStream bos;
			bos = new BufferedOutputStream(new FileOutputStream(path));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);// 采用压缩转档方法
			bos.flush();// 调用flush()方法，更新BufferStream
			bos.close();// 结束OutputStream
			return path;
		} catch (FileNotFoundException e) {
		    CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(
					R.string.exception_file_not_found));
			return null;
		} catch (IOException e) {
		    CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(
					R.string.exception_io));
			return null;
		}
	}

	/**
	 * 图片URL转换成Bitmap
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static Bitmap getNetImage(String url) {

		Bitmap bitmap = null;

		HttpURLConnection conn = null;
		InputStream is = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
		} catch (MalformedURLException e) {
			bitmap = null;
		} catch (IOException e) {
			bitmap = null;
		}
		return bitmap;
	}

	/**
	 * 图片URL转换成Bitmap 并进行缩放 size 1, 2 , 3
	 * 
	 * @param url
	 *            width
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static Bitmap getNetImage(String url, int size) {
		if (size <= 0) {
			getNetImage(url);
		}
		Bitmap bitmap = null;
		HttpURLConnection conn = null;
		InputStream is = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			is = conn.getInputStream();
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = size;
			bitmap = BitmapFactory.decodeStream(is, null, opts);
		} catch (IOException e) {
			bitmap = null;
			CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(
					R.string.exception_io));
		}
		return bitmap;
	}

	/**
	 * SD卡中图片转换为Bitmap
	 * 
	 * @param context
	 * @param imageFile
	 * @return
	 */
	public static Bitmap getSDImage(Context context, File imageFile) {
		Bitmap bitmap = null;
		try {
			FileInputStream is = new FileInputStream(imageFile);
			bitmap = BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			bitmap = null;
			CommFunc.PrintLog(1,LOGTAG,context.getString(R.string.exception_io));
		}
		return bitmap;
	}

	/**
	 * 本地Assets中的图片转换为Bitmap
	 * 
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static Bitmap getLocalImage(Context context, String fileName) {
		Bitmap bitmap = null;
		try {
			InputStream is = context.getAssets().open(fileName);
			bitmap = BitmapFactory.decodeStream(is);
		} catch (IOException e) {
			bitmap = null;
			CommFunc.PrintLog(1,LOGTAG,context.getString(R.string.exception_io));
		}
		return bitmap;
	}

	/**
	 * 下载网络图片
	 * TODO 一般用ImageDownloadTask
	 * @param imageUrl
	 * @param context
	 * @return
	 */
	public static Bitmap downloadImg(URL imageUrl, Context context) {
		Bitmap bitmap = null;
		InputStream is = null;
		byte[] b = null;
		try {
			HttpURLConnection conn = (HttpURLConnection) imageUrl
					.openConnection();
			conn.connect();
			is = conn.getInputStream();
			if (is == null)
				return null;
			b = FileUtil.getByte(is);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;// 防止溢出处理
			// 换算合适的图片缩放值，以减少对JVM太多的内存请求。
			int widthRatio = (int) Math.ceil(options.outWidth
					/ ScreenUtil.getScreenWidth(context));
			int heightRatio = (int) Math.ceil(options.outHeight
					/ ScreenUtil.getScreenHeight(context));
			if (widthRatio > 1 || heightRatio > 1) {
				if (widthRatio > heightRatio) {
					options.inSampleSize = widthRatio;
				} else {
					options.inSampleSize = heightRatio;
				}
			}
			// 2. inPurgeable 设定为 true，可以让java系统, 在内存不足时先行回收部分的内存
			options.inPurgeable = true;
			// 与inPurgeable 一起使用
			options.inInputShareable = true;
			// 3. 减少对Aphla 通道
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			// 4. inNativeAlloc 属性设置为true，可以不把使用的内存算到VM里
			BitmapFactory.Options.class.getField("inNativeAlloc").setBoolean(
					options, true);
			options.inJustDecodeBounds = false;
			bitmap = BitmapFactory.decodeByteArray(b, 0, b.length, options);
		} catch (Exception e) {
		    CommFunc.PrintLog(1,LOGTAG,context.getString(R.string.exception_image_download));
			return null;
		} catch (OutOfMemoryError err) {
		    CommFunc.PrintLog(1,LOGTAG,context.getString(R.string.error_out_of_memory));
			return null;
		} finally {
			b = null;
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				    CommFunc.PrintLog(1,LOGTAG,context.getString(R.string.exception_io));
				}
			}
		}
		return bitmap;
	}

	/**
	 * 检测Rect是否超过指定大小 5M
	 * 
	 * @param rect
	 * @return
	 */
	public static boolean makesureSizeNotTooLarge(Rect rect) {
		final int FIVE_M = 5 * 1024 * 1024; // 不能超过5M
		if (rect.width() * rect.height() * 2 > FIVE_M) {
			return false;
		}
		return true;
	}

	/**
	 * 获取Rect大小
	 * @param rect
	 * @return
	 */
	public static int getSampleSizeOfNotTooLarge(Rect rect) {
		final int FIVE_M = 5 * 1024 * 1024;
		double ratio = ((double) rect.width()) * rect.height() * 2 / FIVE_M;
		return ratio >= 1 ? (int) ratio : 1;
	}

	/**
	 * 自适应屏幕大小 得到最大的smapleSize 同时达到此目标： 自动旋转 以适应view的宽高后, 不影响界面显示效果
	 * 
	 * @param vWidth
	 *            view width
	 * @param vHeight
	 *            view height
	 * @param bWidth
	 *            bitmap width
	 * @param bHeight
	 *            bitmap height
	 * @return
	 */
	public static int getSampleSizeAutoFitToScreen(int vWidth, int vHeight,
			int bWidth, int bHeight) {
		if (vHeight == 0 || vWidth == 0) {
			return 1;
		}
		int ratio = Math.max(bWidth / vWidth, bHeight / vHeight);
		int ratioAfterRotate = Math.max(bHeight / vWidth, bWidth / vHeight);
		return Math.min(ratio, ratioAfterRotate);
	}

	/**
	 * 检测是否可以解析成位图
	 * 
	 * @param datas
	 * @return
	 */
	public static boolean verifyBitmap(byte[] datas) {
		return verifyBitmap(new ByteArrayInputStream(datas));
	}

	/**
	 * 检测是否可以解析成位图
	 * 
	 * @param input
	 * @return
	 */
	public static boolean verifyBitmap(InputStream input) {
		if (input == null) {
			return false;
		}
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		input = input instanceof BufferedInputStream ? input
				: new BufferedInputStream(input);
		BitmapFactory.decodeStream(input, null, options);
		try {
			input.close();
		} catch (IOException e) {
		    CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(R.string.exception_io));
		}
		return (options.outHeight > 0) && (options.outWidth > 0);
	}

	/**
	 * 检测是否可以解析成位图
	 * 
	 * @param path
	 * @return
	 */
	public static boolean verifyBitmap(String path) {
		try {
			return verifyBitmap(new FileInputStream(path));
		} catch (final FileNotFoundException e) {
		    CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(R.string.exception_file_not_found));
		}
		return false;
	}

}
