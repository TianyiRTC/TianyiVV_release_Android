package com.sip.rtcclient.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.config.SysConfig;
import com.sip.rtcclient.tools.LocalActManager;
import com.sip.rtcclient.utils.CommFunc;
import com.sip.rtcclient.utils.NetWorkUtil;

/**
 * 
 * @author Administrator
 * 
 */
public class BaseActivity extends Activity {

	//public String lOGTAG = getClass().getName();
	public String LOGTAG = getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1);
	
//	protected ProgressDialog loadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LocalActManager.getInstance().addActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalActManager.getInstance().removeActivity(this);
	}

	/**
	 * 显示加载Dialog
	 * 
	 * @param showtext
	 */
//	protected void loadingDialog(String showtext) {
//		if (loadingDialog == null) {
//			loadingDialog = new ProgressDialog(this);
//			loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//			if (showtext == null) {
//				loadingDialog.setMessage(getString(R.string.processing));
//			} else {
//				loadingDialog.setMessage(showtext);
//			}
//			loadingDialog.setIndeterminate(false);
//			loadingDialog.setCancelable(true);
//			loadingDialog.setCanceledOnTouchOutside(false);
//		}
//		loadingDialog.show();
//	}
//
//	/**
//	 * 销毁加载Dialog
//	 */
//	protected void dismissLoadDialog() {
//		if (loadingDialog != null) {
//			loadingDialog.dismiss();
//		}
//	}

	/**
	 * 网络检测
	 * 
	 * @return
	 */
	public boolean checkNet() {
		if (NetWorkUtil.isNetConnect(this) == false) {
			CommFunc.DisplayToast(this, R.string.net_cannot_use);
			CommFunc.PrintLog(5, LOGTAG, "checkNet()  isNetConnect net_cannot_use ismLoginOK==false");
			return false;
		}
		if (!SysConfig.getInstance().ismLoginOK()) {
			CommFunc.DisplayToast(this, R.string.unLogin);
			CommFunc.PrintLog(5, LOGTAG, "checkNet() unLogin ismLoginOK==false");
			return false;
		}
		return true;
	}

	/**
	 * 隐藏软键盘
	 */
	protected void goneKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(getWindow().getDecorView()
					.getWindowToken(), 0);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

}
