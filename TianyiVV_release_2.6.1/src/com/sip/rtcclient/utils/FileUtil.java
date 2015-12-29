package com.sip.rtcclient.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;


import android.os.Environment;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;

public class FileUtil {
    
    private static String LOGTAG = "FileUtil";
	private static FileInputStream inputStream;
	/**
	 * 判断SD卡是否存在
	 * 
	 * @return
	 */
	public static boolean checkSDCard() {
		if (Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		}
		return false;
	}

	/**
	 * 获取SD卡路径
	 * 
	 * @return
	 */
	public static File getSDPath() {
		if (checkSDCard()) {
			File sdDir = Environment.getExternalStorageDirectory();
			return sdDir;
		}
		else
		    return null;
		
		//return new File(MyApplication.getInstance().getValue("default_sd"));
	}

	/**
	 * 判断文件是否存在
	 * 
	 * @param path
	 * @return
	 */
	public static boolean isExists(String path) {
		File file = new File(path);
		return isExists(file);
	}
	
	/**
	 * 根据路径获取文件
	 * @param path
	 * @return
	 */
	public static File getFile(String path) {
		File file = null;
		if (path == null) {
			return null;
		} else {
			file = new File(path);
			if (!file.exists()) {
				return null;
			}
			return new File(path);
		}
	}

	/**
	 * 判断文件是否存在
	 * 
	 * @param file
	 * @return
	 */
	public static boolean isExists(File file) {
		return file.exists();
	}

	/**
	 * 在SD卡目录下创建文件
	 * 
	 * @param path
	 */
	public static File createFile(String path) {
		if (!checkSDCard()) {
			return null;
		} else {
			File file = new File(getSDPath(), path);
			if (!file.exists()) {
				file.mkdir();
			}else {
				file.delete();
			}
			return file;
		}
	}

	/**
	 * 创建文件
	 * 
	 * @param parentFile
	 * @param path
	 * @return
	 */
	public static File createFile(File parentFile, String path) {
		if (parentFile == null) {
			return createFile(path);
		} else {
			File file = new File(parentFile, path);
			if (!file.exists()) {
				file.mkdir();
			}
			return file;
		}
	}

	/**
	 * 在SD卡目录下创建文件夹
	 * 
	 * @param fileName
	 */
	public static File createDir(String fileName) {
		if (!checkSDCard()) {
			return null;
		} else {
			File file = new File(getSDPath(), fileName);
			if (!file.exists()) {
				file.mkdirs();
			}
			return file;
		}
	}

	/**
	 * 创建文件夹
	 * 
	 * @param parentFile
	 * @param fileName
	 * @return
	 */
	public static File createDir(File parentFile, String fileName) {
		if (parentFile == null) {
			return createDir(fileName);
		} else {
			File file = new File(getSDPath(), fileName);
			if (!file.exists()) {
				file.mkdirs();
			}
			return file;
		}
	}

	/**
	 * 递归方式删除文件夹/文件夹
	 * 
	 * @param file
	 */
	public static void delete(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}
		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
				return;
			}
			for (int i = 0; i < childFiles.length; i++) {
				delete(childFiles[i]);
			}
			file.delete();
		}
	}

	/**
	 * 获取文件名
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getFileName(String filePath) {
		int start = filePath.lastIndexOf("/");
		int end = filePath.lastIndexOf(".");
		if (start != -1 && end != -1) {
			return filePath.substring(start + 1, end);
		} else {
			return null;
		}
	}

	/**
	 * 获取文件完整名称 带后缀
	 * 
	 * @param path
	 * @return
	 */
	public static String getFileFullName(String path) {
		int start = path.lastIndexOf("/");
		int end = path.length();
		if (start != -1 && end != -1) {
			return path.substring(start + 1, end);
		} else {
			return null;
		}
	}

	/**
	 * 拷贝文件
	 * 
	 * @param srcPath
	 * @param tarPath
	 */
	public static void photoCopy(String srcPath, String tarPath) {
		try {
			FileInputStream fi = new FileInputStream(srcPath);
			BufferedInputStream in = new BufferedInputStream(fi);
			FileOutputStream fo = new FileOutputStream(new File(tarPath));
			BufferedOutputStream out = new BufferedOutputStream(fo);

			byte[] buf = new byte[1024];
			int len = in.read(buf);// 读文件，将读到的内容放入到buf数组中，返回的是读到的长度
			while (len != -1) {
				out.write(buf, 0, len);
				len = in.read(buf);
			}
			out.close();
			fo.close();
			in.close();
			fi.close();
		} catch (IOException e) {
		    CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(
					R.string.exception_io));
		}
	}

	/**
	 * 将数值转换为文件的标准表示
	 * 
	 * @param size
	 * @return
	 */
	public static String caculateSize(int size) {
		String result = "";
		DecimalFormat dFormat = new DecimalFormat("#0.00");
		if (size / 1024 / 1024 > 0) {
			result += dFormat.format(size / (1024 * 1024.0)) + "M";
		} else {
			result += size / 1024 + "K";
		}
		return result;
	}

	/**
	 * 判断文件类型是否为图片
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean checkWheatherPhoto(String filePath) {
		try {
			FileInputStream inputStream = new FileInputStream(filePath);// 从SDCARD下读取一个文件
			byte[] buffer = new byte[2];
			String filecode = "";// 文件类型代码
			@SuppressWarnings("unused")
			String fileType = "";// 文件类型
			if (inputStream.read(buffer) != -1) {// 通过读取出来的前两个字节来判断文件类型
				for (int i = 0; i < buffer.length; i++) {
					// 获取每个字节与0xFF进行与运算来获取高位，这个读取出来的数据不是出现负数
					// 并转换成字符串
					filecode += Integer.toString((buffer[i] & 0xFF));
				}
				switch (Integer.parseInt(filecode)) {// 把字符串再转换成Integer进行类型判断
				case 7790:
					fileType = "exe";
					filePath = null;
					break;
				case 7784:
					fileType = "midi";
					filePath = null;
					break;
				case 8297:
					fileType = "rar";
					filePath = null;
					break;
				case 8075:
					fileType = "zip";
					filePath = null;
					break;
				case 255216:
					fileType = "jpg";
					break;
				case 7173:
					fileType = "gif";
					filePath = null;
					break;
				case 6677:
					fileType = "bmp";
					break;
				case 13780:
					fileType = "png";
					break;
				default:
					filePath = null;
					fileType = "unknown type: " + filecode;
				}
			}
		} catch (FileNotFoundException e) {
			filePath = null;
			CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(
					R.string.exception_file_not_found));
		} catch (IOException e) {
			filePath = null;
			CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(
					R.string.exception_io));
		}
		return filePath == null ? false : true;
	}

	/**
	 * 判断文件是否文档
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean checkWheatherDocumnet(String filePath) {
		if ((filePath.endsWith("doc") || filePath.endsWith("docx")
				|| filePath.endsWith("xls") || filePath.endsWith("xlsx")
				|| filePath.endsWith("ppt") || filePath.endsWith("pptx") || filePath
				.endsWith("pdf"))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * InputStream转换为byte[]
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static byte[] getByte(InputStream is) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int len = 0;
			while ((len = is.read(b, 0, 1024)) != -1) {
				baos.write(b, 0, len);
				baos.flush();
			}
			byte[] bytes = baos.toByteArray();
			return bytes;
		} catch (IOException e) {
		    CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(
					R.string.exception_io));
		}
		return null;
	}

	/**
	 * 下载文件
	 * 
	 * @param urlStr
	 * @param path
	 * @param fileName
	 * @return
	 */
	public static int downFile(String urlStr, String path, String fileName) {
		InputStream inputStream = null;

		try {
			URL url = new URL(urlStr);// 创建一个URL对象
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.connect();
			inputStream = conn.getInputStream();
			File file = createFile(path + fileName);
			byte[] data = new byte[inputStream.available()];
			FileOutputStream fos = new FileOutputStream(file);
			int length = 0;
			while ((length = inputStream.read(data)) != -1) {
				fos.write(data, 0, length);
			}
			fos.flush();
			fos.close();
		} catch (IOException e) {
		    CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(
					R.string.exception_io));
			return -1;
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
			    CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(
						R.string.exception_io));
			}
		}
		return 0;
	}

}
