package com.sip.rtcclient.utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;


import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;


public class UploadUtil {
    
    private static String LOGTAG = "UploadUtil";
	private static final String BOUNDARY = UUID.randomUUID().toString(); // 随机生成的边界标识
	private static final String PREFIX = "--";
	private static final String LINE_END = "\r\n";
	private static final String CONTENT_TYPE = "multipart/form-data"; // 内容类型

	private static int readTimeOut = 10 * 1000; // 读取超时时限
	private static int connectTimeout = 10 * 1000; // 超时时限
	private static int requestTime = 0; // 请求使用时间
	private static final String CHARSET = "utf-8"; // 编码格式

	public static final int UPLOAD_SUCCESS_CODE = 1;// 上传成功
	public static final int UPLOAD_FILE_NOT_EXISTS_CODE = 2; // 文件不存在
	protected static final int WHAT_TO_UPLOAD = 1;// 服务器出错 1
	protected static final int WHAT_UPLOAD_DONE = 2;// 服务器出错 2
	public static final int UPLOAD_SERVER_ERROR_CODE = 3; // 服务器出错 3

	private static OnUploadProcessListener onUploadProcessListener;

	/**
	 * android上传文件到服务器
	 * 
	 * @param filePath
	 *            需要上传的文件的路径
	 * @param fileKey
	 *            在网页上<input type=file id=xxx/> xxx就是这里的fileKey
	 * @param RequestURL
	 *            请求的URL
	 */
	public static void uploadFile(String filePath, String fileKey,
			String RequestURL, Map<String, String> param) {
		File file = FileUtil.getFile(filePath);
		if (file == null) {
			sendMessage(UPLOAD_FILE_NOT_EXISTS_CODE, MyApplication
					.getInstance().getString(R.string.exception_file_not_found));
			return;
		}
		uploadFile(file, fileKey, RequestURL, param);
	}

	/**
	 * android上传文件到服务器
	 * 
	 * @param file
	 *            需要上传的文件
	 * @param fileKey
	 *            在网页上<input type=file name=xxx/> xxx就是这里的fileKey
	 * @param RequestURL
	 *            请求的URL
	 */
	public static void uploadFile(final File file, final String fileKey,
			final String RequestURL, final Map<String, String> param) {
		new Thread(new Runnable() { // 开启线程上传文件
					@Override
					public void run() {
						toUploadFile(file, fileKey, RequestURL, param);
					}
				}).start();
	}

	/**
	 * 上传文件
	 * 
	 * @param file
	 * @param fileKey
	 * @param RequestURL
	 * @param param
	 */
	private static void toUploadFile(File file, String fileKey,
			String RequestURL, Map<String, String> param) {
		String result = null;
		requestTime = 0;

		long requestTime = System.currentTimeMillis();
		long responseTime = 0;

		try {
			URL url = new URL(RequestURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(readTimeOut);
			conn.setConnectTimeout(connectTimeout);
			conn.setDoInput(true); // 允许输入流
			conn.setDoOutput(true); // 允许输出流
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("POST"); // 请求方式
			conn.setRequestProperty("Charset", CHARSET); // 设置编码
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
					+ BOUNDARY);
			// conn.setRequestProperty("Content-Type",
			// "application/x-www-form-urlencoded");

			/*
			 * 当文件不为空，把文件包装并且上传
			 */
			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
			StringBuffer sb = null;
			String params = "";

			/*
			 * 以下是用于上传参数
			 */
			if (param != null && param.size() > 0) {
				Iterator<String> it = param.keySet().iterator();
				while (it.hasNext()) {
					sb = null;
					sb = new StringBuffer();
					String key = it.next();
					String value = param.get(key);
					sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
					sb.append("Content-Disposition: form-data; name=\"")
							.append(key).append("\"").append(LINE_END)
							.append(LINE_END);
					sb.append(value).append(LINE_END);
					params = sb.toString();
					CommFunc.PrintLog(1,LOGTAG,key + "=" + params + "##");
					dos.write(params.getBytes());
				}
			}

			sb = null;
			params = null;
			sb = new StringBuffer();
			/*
			 * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
			 * filename是文件的名字，包含后缀名的 比如:abc.png
			 */
			sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
			sb.append("Content-Disposition:form-data; name=\"" + fileKey
					+ "\"; filename=\"" + file.getName() + "\"" + LINE_END);
			//TODO 更改Content-Type类型 可上传其他文件，如果更改为通用,最好将Content-Type作为参数处理
			sb.append("Content-Type:image/pjpeg" + LINE_END); // 这里配置的Content-type很重要的
																// ，用于服务器端辨别文件的类型的
			sb.append(LINE_END);
			params = sb.toString();
			sb = null;
			CommFunc.PrintLog(1,LOGTAG,file.getName() + "=" + params + "##");
			dos.write(params.getBytes());
			// 上传文件
			InputStream is = new FileInputStream(file);
			onUploadProcessListener.initUpload((int) file.length());
			byte[] bytes = new byte[1024];
			int len = 0;
			int curLen = 0;
			while ((len = is.read(bytes)) != -1) {
				curLen += len;
				dos.write(bytes, 0, len);
				onUploadProcessListener.onUploadProcess(curLen);
			}
			is.close();

			dos.write(LINE_END.getBytes());
			byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
					.getBytes();
			dos.write(end_data);
			dos.flush();

			// 获取响应码 200=成功 当响应成功，获取响应的流
			int res = conn.getResponseCode();
			responseTime = System.currentTimeMillis();
			requestTime = (int) ((responseTime - requestTime) / 1000);
			CommFunc.PrintLog(1,LOGTAG,"Upload response code:" + res);
			if (res == 200) {
			    CommFunc.PrintLog(1,LOGTAG,"request success");
				InputStream input = conn.getInputStream();
				StringBuffer sb1 = new StringBuffer();
				int ss;
				while ((ss = input.read()) != -1) {
					sb1.append((char) ss);
				}
				result = sb1.toString();
				CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(
						R.string.file_upload_success)
						+ " result : " + result);
				sendMessage(UPLOAD_SUCCESS_CODE, MyApplication.getInstance()
						.getString(R.string.file_upload_success));
				return;
			} else {
			    CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(
						R.string.file_upload_fail));
				sendMessage(UPLOAD_SERVER_ERROR_CODE, MyApplication
						.getInstance().getString(R.string.file_upload_fail)
						+ " code=" + res);
				return;
			}
		} catch (MalformedURLException e) {
		    CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(
					R.string.file_upload_fail));
			sendMessage(UPLOAD_SERVER_ERROR_CODE, MyApplication.getInstance()
					.getString(R.string.file_upload_fail));
			return;
		} catch (IOException e) {
		    CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(
					R.string.file_upload_fail));
			sendMessage(UPLOAD_SERVER_ERROR_CODE, MyApplication.getInstance()
					.getString(R.string.file_upload_fail));
			return;
		}
	}

	/**
	 * 发送上传结果
	 * 
	 * @param responseCode
	 * @param responseMessage
	 */
	private static void sendMessage(int responseCode, String responseMessage) {
		onUploadProcessListener.onUploadDone(responseCode, responseMessage);
	}


	public static interface OnUploadProcessListener {
		/**
		 * 上传响应
		 * 
		 * @param responseCode
		 * @param message
		 */
		void onUploadDone(int responseCode, String message);

		/**
		 * 上传中
		 * 
		 * @param uploadSize
		 */
		void onUploadProcess(int uploadSize);

		/**
		 * 准备上传
		 * 
		 * @param fileSize
		 */
		void initUpload(int fileSize);
	}

	public void setOnUploadProcessListener(
			OnUploadProcessListener onUploadProcessListener1) {
		onUploadProcessListener = onUploadProcessListener1;
	}

	public int getReadTimeOut() {
		return readTimeOut;
	}

	public void setReadTimeOut(int readTimeOut1) {
		readTimeOut = readTimeOut1;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout1) {
		connectTimeout = connectTimeout1;
	}

	/**
	 * 获取上传使用的时间
	 * 
	 * @return
	 */
	public static int getRequestTime() {
		return requestTime;
	}

}
