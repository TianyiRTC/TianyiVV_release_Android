package com.sip.rtcclient.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.sip.rtcclientouter.R;


public class ImageDownloadTask extends AsyncTask<Object, Object, Bitmap> {

	private String url = null;
	private ImageView imageView = null;
	private Context context;
	private String LOGTAG = "ImageDownloadTask";
	
	public static String EFAULT_IMAGE_NAME = "image.png";	//TODO 默认图片名称
	
	public ImageDownloadTask(Context context) {
		this.context = context;
	}

	@Override
	protected Bitmap doInBackground(Object... params) {
		Bitmap bitmap = null;
		imageView = (ImageView) params[0];
		url = (String) params[1];
		try {
			if (url.equals(null) || url == null || url.equals("")
					|| url == "" || url.equals("null") || url == "null") {
				//TODO 如果URL为空 设置默认图片
				bitmap = ImageUtil.getLocalImage(context, EFAULT_IMAGE_NAME);
			} else {
				bitmap = ImageUtil.getNetImage(url);
			}
			if (bitmap == null) {
				//TODO 如果获取的bitmap为空设置默认图片
				bitmap = ImageUtil.getLocalImage(context, EFAULT_IMAGE_NAME);
			}
		} catch (Exception e) {
			//
			bitmap = ImageUtil.getLocalImage(context, EFAULT_IMAGE_NAME);
			CommFunc.PrintLog(1,LOGTAG,context.getString(R.string.exception_image_download));
		}
		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		//TODO 保存图片到SDCard中，path根据需求自己设置
		//ImageUtil.savePictrueToSDCard(path, bitmap);
		imageView.setImageBitmap(bitmap);
	}
}