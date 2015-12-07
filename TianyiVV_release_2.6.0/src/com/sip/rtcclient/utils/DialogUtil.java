package com.sip.rtcclient.utils;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.sip.rtcclientouter.R;


public class DialogUtil {

	private Dialog requestDialog; // 请求、加载Dialog

	/**
	 * 普通ShortToast
	 * 
	 * @param activity
	 * @param message
	 * @param style
	 */
	public static void showShortToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * 普通ShortToast
	 * 
	 * @param activity
	 * @param message
	 * @param style
	 */
	public static void showShortToast(Context context, int resId) {
		showShortToast(context, context.getString(resId));
	}

	/**
	 * 普通LongToast
	 * 
	 * @param context
	 * @param message
	 */
	public static void showLongToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * 普通LongToast
	 * 
	 * @param context
	 * @param message
	 */
	public static void showLongToast(Context context, int resId) {
		showLongToast(context, context.getString(resId));
	}
	

	/**
	 * 退出Dialog
	 * 
	 * @param activity
	 */
	public void showExitDialog(final Activity activity) {
		Builder builder = new Builder(activity);
		builder.setTitle(R.string.info_title);
		builder.setMessage(R.string.info_exit_msg);
		builder.setPositiveButton(R.string.info_btn_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// TODO 确认退出的逻辑处理
						activity.finish();
					}
				});
		builder.setNegativeButton(R.string.info_btn_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		Dialog exitDialog = builder.create();
		exitDialog.show();

		/* 设置exitDialog大小 */
		LayoutParams layoutParams = exitDialog.getWindow().getAttributes();
		int width = ScreenUtil.getScreenWidth(activity.getApplicationContext());
		layoutParams.width = (int) (0.8 * width);
		exitDialog.getWindow().setAttributes(layoutParams);
	}

	/**
	 * 请求Dialog
	 */
	public void showReqDialog(Activity activity) {
		if (requestDialog != null) {
			requestDialog.dismiss();
			requestDialog = null;
		}
		requestDialog = creatRequestDialog(activity, null);
		requestDialog.show();
	}

	/**
	 * 注销请求Dialog
	 */
	public void dismissReqDialog() {
		if (requestDialog != null) {
			requestDialog.dismiss();
		}
		requestDialog = null;
	}

	/**
	 * 创建请求Dialog
	 * TODO Dialog只显示进度条未添加文字提示
	 * @param context
	 * @param tip
	 * @return
	 */
	private Dialog creatRequestDialog(final Context context, String tip) {
		final Dialog dialog = new Dialog(context, R.style.dialog);
		dialog.setContentView(R.layout.dialog_request);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		// TODO 请求Dialog的宽度、高度可在此更改
		int width = ScreenUtil.getScreenWidth(context);
		lp.alpha = 0.85f;
		lp.width = (int) (0.6 * width);

		return dialog;
	}

}
