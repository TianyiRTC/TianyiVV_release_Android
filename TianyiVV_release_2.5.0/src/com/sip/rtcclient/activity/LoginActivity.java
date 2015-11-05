package com.sip.rtcclient.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jni.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rtc.sdk.common.RtcConst;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.chinatelecom.account.lib.apk.MsgBroadcastReciver;
import cn.com.chinatelecom.account.lib.apk.TelecomProcessState;

import com.oauth2.weibo.GetUserAPI;
import com.oauth2.weibo.OAuthSharepreference;
import com.oauth2.weibo.WeiboConstParam;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.WeiboParameters;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.RequestListener;
import com.sip.rtcclient.HBaseApp;
import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.config.MsgKey;
import com.sip.rtcclient.config.SysConfig;
import com.sip.rtcclient.db.SQLiteManager;
import com.sip.rtcclient.services.ReloginService;
import com.sip.rtcclient.utils.LoginUtil;
import com.sip.rtcclient.utils.CommFunc;
import com.sip.rtcclient.utils.NetWorkUtil;
import com.sip.rtcclient.utils.PinYinManager;

public class LoginActivity extends BaseActivity {

	private String LOGTAG = "LoginActivity";
	private boolean bRegreveiver = false;
	GetUserRequestListener mGetUserRequestListener = new GetUserRequestListener();

	// private MsgBroadcastReciver telecomReciver=null;
	final static String DEVICENO = "3500000000404304";
	final static String KEY = "48EFC6131AD1B94011BB69CB699FB893BED69C88BE403850";
	final static SimpleDateFormat TIMESTAMP_FORMATER = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	final static Calendar CALENDAR = Calendar.getInstance();

	private LinearLayout layout_bottom;
	private EditText et_account, et_addresscfg;

	/** The sp_uetype. */
	private Spinner sp_uetype;
	/** The adapter_uetype. */
	private ArrayAdapter<?> adapter_uetype;
	ListView listViewAdd;
	EditText login_et_user;
	EditText login_et_password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		initView();

	}

	private void initView() {
		//wwyue0430
		login_et_user = (EditText) findViewById(R.id.login_et_user);
		login_et_password = (EditText) findViewById(R.id.login_et_password);
		LoginUtil.initUidAndPwd(login_et_user, login_et_password);
		
		
		listViewAdd = (ListView) findViewById(R.id.list_addr);
		layout_bottom = (LinearLayout) findViewById(R.id.login_account_setting);
		et_account = (EditText) findViewById(R.id.login_account_et);
		et_addresscfg = (EditText) findViewById(R.id.login_addresscfg_et);
		if (RtcConst.bAddressCfg) {

			String addr = "https://42.123.77.35:442";
			et_addresscfg.setText(addr);
			// wwyue0428
			et_addresscfg.setVisibility(View.GONE);
			// et_addresscfg.setVisibility(View.VISIBLE);
			// wwyue0425
			et_addresscfg.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					try {
						showList(arg0);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
			CommFunc.PrintLog(5, LOGTAG, "initView:" + et_addresscfg);
		} else
			et_addresscfg.setVisibility(View.GONE);

		sp_uetype = (Spinner) findViewById(R.id.spinner_uetype);
		// sp_videocodec =
		// (Spinner)findViewById(R.id.activity_contact_sp_videocodec);
		// 将可选内容与ArrayAdapter连接起来
		adapter_uetype = ArrayAdapter.createFromResource(this, R.array.uetype,
				android.R.layout.simple_spinner_item);
		// 设置下拉列表的风格
		adapter_uetype
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 设置下拉列表的风格
		// 将adapter2 添加到spinner中
		sp_uetype.setAdapter(adapter_uetype);
		// 添加事件Spinner事件监听
		sp_uetype
				.setOnItemSelectedListener(new SpinnerXMLSelectedListener_uetype());
		// 设置默认值

		// // wwyue 设置成phone类型登陆
		if (RtcConst.bNewVersion) {
			sp_uetype.setVisibility(View.GONE);
			int uetype = MyApplication.getInstance().getIntSharedXml(
					MsgKey.pref_uetype, 1);
			sp_uetype.setSelection(uetype, true);
			Utils.PrintLog(5, LOGTAG, "initView uetype:" + uetype);
			MyApplication.getInstance().setUeTypeVal(uetype);
		}
	}

	// 使用XML形式操作
	/**
	 * The Class SpinnerXMLSelectedListener_vformat.
	 */
	class SpinnerXMLSelectedListener_uetype implements OnItemSelectedListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.widget.AdapterView.OnItemSelectedListener#onItemSelected(
		 * android.widget.AdapterView, android.view.View, int, long)
		 */
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Utils.PrintLog(5, LOGTAG, "选择终端类型:" + adapter_uetype.getItem(arg2)
					+ "  uetypeselect:" + arg2);
			MyApplication.getInstance().saveSharePrefValue(MsgKey.pref_uetype,
					"" + arg2);
			MyApplication.getInstance().setUeTypeVal(arg2);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.widget.AdapterView.OnItemSelectedListener#onNothingSelected
		 * (android.widget.AdapterView)
		 */
		public void onNothingSelected(AdapterView<?> arg0) {

		}
	}

	/**
	 * 保存帐号测试使用 debug版本 当做天翼账号使用
	 * 
	 * @param view
	 */
	public void onSaveAccount(View view) {

		if (SysConfig.bDEBUG) {
			String account = et_account.getText().toString().trim();
			layout_bottom.setVisibility(View.GONE);
			if (account != null && !account.equals("")) {
				SysConfig.userid = account;
				// CommFunc.DisplayToast(getApplicationContext(), "帐号保存成功!");
				CommFunc.PrintLog(5, LOGTAG, "onSaveAccount:account:" + account
						+ " userid:" + SysConfig.userid);
				MyApplication.getInstance().saveSharePrefValue(
						MsgKey.key_userid, account);
				return;
			}
		}
		// if(RtcConst.bAddressCfg)
		// {
		// String addcfg = et_addresscfg.getText().toString().trim();
		// layout_bottom.setVisibility(View.GONE);
		// if (addcfg != null && !addcfg.equals("")) {
		// RtcConst.getServerDomain = addcfg;
		// CommFunc.PrintLog(5, LOGTAG,
		// "getServerDomain: "+RtcConst.getServerDomain);
		// }
		// }

	}

	private void onsaveAddr() {
		if (RtcConst.bAddressCfg) {
			String addcfg = et_addresscfg.getText().toString().trim();
			if (addcfg != null && !addcfg.equals("")) {
				RtcConst.getServerDomain = addcfg;
				CommFunc.PrintLog(5, LOGTAG, "getServerDomain: "
						+ RtcConst.getServerDomain);
				MyApplication.getInstance().saveSharePrefValue(
						MsgKey.pref_addcfg, addcfg);
			}
		}
	}

	public void OnLogin_SinaWeiBo(View view) {

		onsaveAddr();
		if (NetWorkUtil.isNetConnect(this) == false) {
			CommFunc.DisplayToast(this, R.string.net_cannot_use);
			CommFunc.PrintLog(5, LOGTAG, "OnLogin_SinaWeiBo net_cannot_use:");
			return;
		}
		if ((NetWorkUtil.checkNetworkType(this) == NetWorkUtil.TYPE_CT_WAP || NetWorkUtil
				.checkNetworkType(this) == NetWorkUtil.TYPE_CM_CU_WAP)
				&& RtcConst.bWapNetSupport == false) {
			CommFunc.DisplayToast(this, "当前为wap网络,终端不支持请切换网络:"
					+ RtcConst.bWapNetSupport);
			CommFunc.PrintLog(5, LOGTAG,
					"OnLogin_SinaWeiBo当前为wap网络,终端不支持请切换网络:");
			return;
		}
		SysConfig.getInstance().setIsLoginByBtn(true);
		SysConfig.login_type = SysConfig.USERTYPE_WEIBO;
		CommFunc.PrintLog(5, LOGTAG, "OnLogin_SinaWeiBo");
		// CommFunc.DisplayToast(this, "该版本暂不支持");
		// 先读取本地微博账号，userid 如果没有走授权流程
		if (bRegreveiver == false) {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(SysConfig.BROADCAST_RELOGIN_SERVICE);
			registerReceiver(Msgreceiver, intentFilter);
			bRegreveiver = true;
		}
		loadingDialog(getString(R.string.tips_loginprocess));

		SysConfig.getInstance().setmLoginOK(false);
		if (IsNeedOAuth())
			weiboAuth();
		else {
			OnStartLogin(); // 直接走登录流程
		}

	}

	private boolean IsNeedOAuth() {
		String expires = OAuthSharepreference.getExpires(this);
		String accesstoken = OAuthSharepreference.getToken(this);
		String uid = OAuthSharepreference.getUid(this);
		if (expires.length() == 0 || accesstoken.length() == 0
				|| uid.length() == 0)
			return true;
		if (CompareDate(Long.parseLong(expires), System.currentTimeMillis())) // 未超过期限
			return false;
		return true;
	}

	// 比较两个时间大小 如果 time1 大于 time2 return true;
	private boolean CompareDate(long time1, long time2) {

		CommFunc.PrintLog(5, LOGTAG, "time1:" + time1 + "  time2:" + time2);
		if (time1 > time2) {
			CommFunc.PrintLog(5, LOGTAG, "time1>time2");
			return true;
		}
		CommFunc.PrintLog(5, LOGTAG, "time1<time2");
		return false;
	}

	protected ProgressDialog loadingDialog;

	protected void loadingDialog(String showtext) {
		if (loadingDialog == null) {
			loadingDialog = new ProgressDialog(this);
			loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			if (showtext == null) {
				loadingDialog.setMessage(getString(R.string.processing));
			} else {
				loadingDialog.setMessage(showtext);
			}
			loadingDialog.setIndeterminate(false);
			loadingDialog.setCancelable(true);
			loadingDialog.setCanceledOnTouchOutside(false);
		}
		loadingDialog.show();
	}

	/**
	 * 销毁加载Dialog
	 */
	protected void dismissLoadDialog() {
		if (loadingDialog != null) {
			loadingDialog.dismiss();
		}
	}

	// wwyue0430
	public void OnLogin_btn(View v)
	{
		if(LoginUtil.loginSuccess(this,login_et_user, login_et_password))
		{
			OnLogin_TianYi(v);
		}
	}

	public void OnLogin_TianYi(View view) {
		onsaveAddr();
		SysConfig.getInstance().setIsLoginByBtn(true);
		SysConfig.login_type = SysConfig.USERTYPE_TIANYI;
		// MyApplication.getInstance().initDeviceListener();
		CommFunc.PrintLog(5, LOGTAG, "OnLogin_TianYi");
		goneKeyboard();
		if (NetWorkUtil.isNetConnect(this) == false) {
			CommFunc.DisplayToast(this, R.string.net_cannot_use);
			return;
		}
		if ((NetWorkUtil.checkNetworkType(this) == NetWorkUtil.TYPE_CT_WAP || NetWorkUtil
				.checkNetworkType(this) == NetWorkUtil.TYPE_CM_CU_WAP)
				&& RtcConst.bWapNetSupport == false) {
			CommFunc.DisplayToast(this, "当前为wap网络,终端不支持请切换网络");
			return;
		}
		if (SysConfig.bDEBUG == false) {
			RegisterTeleReceiver();
		}
		if (bRegreveiver == false) {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(SysConfig.BROADCAST_RELOGIN_SERVICE);
			registerReceiver(Msgreceiver, intentFilter);
			bRegreveiver = true;
		}
		loadingDialog(getString(R.string.tips_loginprocess));

		OnStartLogin();
	}

	private void OnStartLogin() {
		SysConfig.getInstance().setmLoginOK(false);
		if (SysConfig.bDEBUG)
			MyApplication.getInstance().getAccountInfo()
					.setUserid(SysConfig.userid);
		ReloginService.getInstance().InitLogin();
	}

	private void saveContactAsMember() // 将登陆的帐号作为成员保存
	{
		CommFunc.PrintLog(5, LOGTAG, "saveContactAsMember");
		HBaseApp.post2WorkRunnable(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// TContactInfo cb1 =
				// SQLiteManager.getInstance().getContactInfoById(MyApplication.getInstance().getUserID());
				// if(cb1 != null)
				// {
				// return;
				// }
				CommFunc.PrintLog(5, LOGTAG, "saveContactAsMember userid:"
						+ MyApplication.getInstance().getUserID());
				TContactInfo cb = new TContactInfo();
				// 如果新浪微博登录取用户名称
				String username = MyApplication.getInstance().getUserID();
				if (SysConfig.login_type == SysConfig.USERTYPE_WEIBO) {
					username = OAuthSharepreference
							.getUname(LoginActivity.this);
					cb.setUsertype(SysConfig.USERTYPE_WEIBO);
				} else
					cb.setUsertype(SysConfig.USERTYPE_TIANYI);
				String[] pinyin = PinYinManager.toPinYin(username);
				cb.setName(username);
				cb.setPhoneNum(MyApplication.getInstance().getUserID());
				cb.setFirstChar(pinyin[0]);
				cb.setLookUpKey(pinyin[1]);
				cb.setContactId(MyApplication.getInstance().getUserID());
				cb.setPhotoId(null);
				SQLiteManager.getInstance().saveContactInfo(cb, true);
			}

		});

	}

	private void OnLoginResult(Intent intent) {
		int result = intent.getIntExtra("arg1", -1);
		String desc = intent.getStringExtra("arg2");

		if (result == MsgKey.KEY_STATUS_200
				|| result == MsgKey.KEY_RESULT_SUCCESS) {
			// wwyue0425
			saveAdd(et_addresscfg.getText().toString().trim());

			SysConfig.getInstance().setmLoginOK(true);
			CommFunc.PrintLog(5, LOGTAG, "OnLoginResult loginok  MainActivity");
			// save accountinfo
			saveContactAsMember();
			String versionName = MyApplication.getInstance().getAppVersionName();
			MyApplication.getInstance().setVersionName(versionName);
			if (SysConfig.login_type == SysConfig.USERTYPE_TIANYI)
				MyApplication.getInstance().saveSharePrefValue(MsgKey.key_userid, SysConfig.userid);

			if (!RtcConst.bNewVersion)
				ReloginService.getInstance().testQueryStaus();

			// wwyue0430

			MyApplication.getInstance().saveSharePrefValue(LoginUtil.LOGIN_UID,
					login_et_user.getText().toString().trim());
			MyApplication.getInstance().saveSharePrefValue(LoginUtil.LOGIN_PWD,
					login_et_password.getText().toString().trim());
			MyApplication.getInstance().saveSharePrefValue(
					MsgKey.key_logintype, "" + SysConfig.login_type);
			Intent intent1 = new Intent(LoginActivity.this, MainActivity.class);
			startActivity(intent1);
			finish();
		} else {
			SysConfig.getInstance().setmLoginOK(false);
			CommFunc.PrintLog(5, LOGTAG, "登陆失败 错误码[:" + result + "]desc:" + desc);
			if(result == RtcConst.CallCode_Forbidden) {
				MyApplication.getInstance().saveSharePrefValue(MsgKey.key_rtc_token, "invalid");
				CommFunc.DisplayToast(MyApplication.getInstance(), "该帐号在其他终端登录过，继续使用请再点击一次登录");
			} else {
				CommFunc.DisplayToast(MyApplication.getInstance(), "登陆失败:" + result + "错误原因:" + desc);
			}
		}
	}

	/**
	 * TODO 这里MainActivity可能被实例化多次，导致退出操作的时候又加载一遍MainActivity
	 * 将MainActivity的android:launchMode设置为singleInstance
	 */
	private BroadcastReceiver Msgreceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(SysConfig.BROADCAST_RELOGIN_SERVICE)) {
				switch (intent.getIntExtra("what", -1)) {
				case SysConfig.MSG_SIP_REGISTER: {
					dismissLoadDialog();
					CommFunc.PrintLog(5, LOGTAG, "MSG_SIP_REGISTER");
					OnLoginResult(intent);
					break;
				}
				case SysConfig.MSG_TIANYI_VERIFY: {
					dismissLoadDialog();
					CommFunc.PrintLog(5, LOGTAG, "MSG_TIANYI_VERIFY");
					LoginOtherFail(intent, "[天翼账号接口]");
					break;
				}
				case SysConfig.MSG_GETTOKEN_ERROR: {
					dismissLoadDialog();
					CommFunc.PrintLog(5, LOGTAG, "MSG_GETTOKEN_ERROR");
					LoginOtherFail(intent, "[获取token]");
					break;
				}
				case SysConfig.MSG_GETTOKEN_SUCCESS: {
					CommFunc.PrintLog(5, LOGTAG,
							"get token ok prepare sip register");
					MyApplication.getInstance().disposeSipRegister();
					break;
				}
				case SysConfig.MSG_GETSERVERADRR_FAILED: {
					dismissLoadDialog();
					CommFunc.PrintLog(5, LOGTAG, "MSG_GETSERVERADRR_FAILED");
					SysConfig.getInstance().setmLoginOK(false);
					LoginOtherFail(intent, "[获取服务器接口]");
					break;
				}
				case SysConfig.MSG_SDKInitOK:
					CommFunc.PrintLog(5, LOGTAG, "MSG_SDKInitOK");
					// sdk初始ok后继续获取token sip 注册流程
					ReloginService.getInstance().RestartLogin();
					break;
				default: {
					dismissLoadDialog();
					CommFunc.PrintLog(5, LOGTAG, "登陆失败:错误未知");
					break;
				}
				}
			}
		}
	};

	private void LoginOtherFail(Intent intent, String strTips) {
		String extra = intent.getStringExtra("arg2");
		int result = intent.getIntExtra("arg1", -1);
		CommFunc.PrintLog(5, LOGTAG, "LoginOtherFail:" + extra + "result:["
				+ result + "]");
		CommFunc.DisplayToast(LoginActivity.this, strTips + extra + "错误码["
				+ result + "]");

		SysConfig.getInstance().setmLoginOK(result == 200 || result == 0); // false
		// testGoMainActivity(); // 测试使用
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// 第一次登陆失败退出后 第二次进入 应该再次进入登陆页面
		if (bRegreveiver)
			unregisterReceiver(Msgreceiver);
		unRegisterTelReceiver();
		if (SysConfig.getInstance().ismLoginOK() == false) {
			MyApplication.getInstance().saveSharePrefValue(
					MsgKey.key_recycleflag, "1");
			MyApplication.getInstance().saveSharePrefValue(
					MsgKey.key_isNormalExit, "0");
			// MyApplication.getInstance().saveSharePrefValue(MsgKey.key_userid,
			// SysConfig.userid);
			// MyApplication.getInstance().exit(); // 实际需要放开测试使用
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			// MyApplication.getInstance().saveSharePrefValue(MsgKey.key_recycleflag,""+false);
			MyApplication.getInstance().saveSharePrefValue(
					MsgKey.key_isNormalExit, "" + 1);
			MyApplication.getInstance().exit();
			finish();
			break;
		case KeyEvent.KEYCODE_MENU:
			if (SysConfig.bDEBUG) {
				if (layout_bottom.getVisibility() == View.VISIBLE) {
					layout_bottom.setVisibility(View.GONE);
				} else {
					layout_bottom.setVisibility(View.VISIBLE);
					et_account.setText(SysConfig.userid);
				}
			}
			break;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void unRegisterTelReceiver() {
		if (telecomReciver.Registered == true) {
			telecomReciver.Registered = false;
			unregisterReceiver(telecomReciver);
		}
	}

	private void RegisterTeleReceiver() {
		if (!telecomReciver.Registered) {
			// 注册消息接收器
			IntentFilter filter = new IntentFilter(
					TelecomProcessState.TelecomBroadCastTag);
			CommFunc.PrintLog(5, LOGTAG, "RegisterTeleReceiver:"
					+ telecomReciver);
			registerReceiver(telecomReciver, filter);
			telecomReciver.Registered = true;
		}
	}

	MsgBroadcastReciver telecomReciver = new MsgBroadcastReciver() {
		@Override
		public void switchMsg(int type, Intent intent) {
			// TODO Auto-generated method stub
			switch (type) {
			case TelecomProcessState.TelecomStateUserCanceledFlag:
				CommFunc.PrintLog(5, LOGTAG, "TelecomStateUserCanceledFlag");
				break;

			case TelecomProcessState.TelecomUserFinishLoginFlag:
				CommFunc.PrintLog(5, LOGTAG, "TelecomUserFinishLoginFlag");
				break;

			default:
				break;
			}

		}
	};

	// 微博登录相关
	private static final int MSG_FETCH_CODE_SUCCESS = 0;
	private static final int MSG_FETCH_TOKEN_SUCCESS = 1;
	private static final int MSG_FETCH_TOKEN_FAILED = 2;
	private static final int MSG_FETCH_GETUSER_SUCCESS = 3;
	private static final int MSG_FETCH_GETUSER_FAILED = 4;
	private WeiboAuth mWeiboAuth;
	/** 获取到的 Code */
	private String mCode;
	/** 获取到的 Token */
	private Oauth2AccessToken mAccessToken;

	private void weiboAuth() {
		mWeiboAuth = new WeiboAuth(this, WeiboConstParam.CONSUMER_KEY,
				WeiboConstParam.REDIRECT_URL, WeiboConstParam.SCOPE);
		mWeiboAuth.authorize(new AuthListener(), WeiboAuth.OBTAIN_AUTH_CODE);
	}

	/**
	 * 微博认证授权回调类。
	 */
	class AuthListener implements WeiboAuthListener {

		@Override
		public void onComplete(Bundle values) {
			if (null == values) {

				CommFunc.PrintLog(
						5,
						LOGTAG,
						"AuthListener onComplete():"
								+ getText(R.string.weibosdk_demo_toast_obtain_code_failed));

				Toast.makeText(LoginActivity.this,
						R.string.weibosdk_demo_toast_obtain_code_failed,
						Toast.LENGTH_SHORT).show();
				dismissLoadDialog();
				return;
			}

			String code = values.getString("code");
			if (TextUtils.isEmpty(code)) {
				CommFunc.PrintLog(
						5,
						LOGTAG,
						"AuthListener onComplete():"
								+ getText(R.string.weibosdk_demo_toast_obtain_code_failed));
				Toast.makeText(LoginActivity.this,
						R.string.weibosdk_demo_toast_obtain_code_failed,
						Toast.LENGTH_SHORT).show();
				dismissLoadDialog();
				return;
			}

			mCode = code;
			mHandler.obtainMessage(MSG_FETCH_CODE_SUCCESS).sendToTarget();
			Log.e("MainActivity", "获取mCode:" + mCode);
		}

		@Override
		public void onCancel() {
			CommFunc.PrintLog(5, LOGTAG, "AuthListener onCancel():"
					+ getText(R.string.weibosdk_demo_toast_auth_canceled));
			Toast.makeText(LoginActivity.this,
					R.string.weibosdk_demo_toast_auth_canceled,
					Toast.LENGTH_LONG).show();
			dismissLoadDialog();
		}

		@Override
		public void onWeiboException(WeiboException e) {
			CommFunc.PrintLog(5, LOGTAG,
					"AuthListener Auth exception:" + e.getMessage());
			CommFunc.DisplayToast(LoginActivity.this,
					"Auth exception : " + e.getMessage());
			dismissLoadDialog();
		}
	}

	/**
	 * 该 Handler 配合 {@link RequestListener} 对应的回调来更新 UI。
	 */
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_FETCH_CODE_SUCCESS:
				fetchTokenAsync(mCode, WeiboConstParam.CONSUMER_SECRET);
				break;
			case MSG_FETCH_TOKEN_SUCCESS:
				// 显示 Token
				CommFunc.PrintLog(
						5,
						LOGTAG,
						"MSG_FETCH_TOKEN_SUCCESS mAccessToken: "
								+ mAccessToken.toString());
				OAuthSharepreference.setExpires(LoginActivity.this, ""
						+ mAccessToken.getExpiresTime());
				OAuthSharepreference.setUid(LoginActivity.this, ""
						+ mAccessToken.getUid());
				OAuthSharepreference.setToken(LoginActivity.this, ""
						+ mAccessToken.getToken());
				WeiBoShowUser(); // 获取用户信息
				break;

			case MSG_FETCH_TOKEN_FAILED:
				Toast.makeText(LoginActivity.this,
						R.string.weibosdk_demo_toast_obtain_token_failed,
						Toast.LENGTH_SHORT).show();
				CommFunc.PrintLog(
						5,
						LOGTAG,
						"mHandler MSG_FETCH_TOKEN_FAILED:"
								+ getText(R.string.weibosdk_demo_toast_obtain_token_failed));
				break;
			case MSG_FETCH_GETUSER_SUCCESS:
				CommFunc.PrintLog(5, LOGTAG, "获取新浪微博用户信息成功");
				// 用户信息获取成功发送给服务器端，并且本地保留用户信息
				OnGetUserSuccess();
				break;
			case MSG_FETCH_GETUSER_FAILED:
				CommFunc.DisplayToast(LoginActivity.this, "获取新浪微博用户信息失败");
				CommFunc.PrintLog(5, LOGTAG, "获取新浪微博用户信息失败");
				break;
			default:
				break;
			}
		};
	};

	/**
	 * 异步获取 Token。
	 * 
	 * @param authCode
	 *            授权 Code，该 Code 是一次性的，只能被获取一次 Token
	 * @param appSecret
	 *            应用程序的 APP_SECRET，请务必妥善保管好自己的 APP_SECRET，
	 *            不要直接暴露在程序中，此处仅作为一个DEMO来演示。
	 */
	public void fetchTokenAsync(String authCode, String appSecret) {
		CommFunc.PrintLog(5, LOGTAG, "fetchTokenAsync");
		WeiboParameters requestParams = new WeiboParameters();
		requestParams.add(WBConstants.AUTH_PARAMS_CLIENT_ID,
				WeiboConstParam.CONSUMER_KEY);
		requestParams.add(WBConstants.AUTH_PARAMS_CLIENT_SECRET, appSecret);
		requestParams.add(WBConstants.AUTH_PARAMS_GRANT_TYPE,
				"authorization_code");
		requestParams.add(WBConstants.AUTH_PARAMS_CODE, authCode);
		requestParams.add(WBConstants.AUTH_PARAMS_REDIRECT_URL,
				WeiboConstParam.REDIRECT_URL);

		/**
		 * 请注意： {@link RequestListener} 对应的回调是运行在后台线程中的， 因此，需要使用 Handler 来配合更新
		 * UI。
		 */
		RequestListener lis = new MyAuthRequest();
		AsyncWeiboRunner.request(WeiboConstParam.OAUTH2_ACCESS_TOKEN_URL,
				requestParams, "POST", lis);
	}

	class MyAuthRequest implements RequestListener {
		@Override
		public void onComplete(String response) {
			CommFunc.PrintLog(5, LOGTAG, "Response: " + response);

			// 获取 Token 成功
			Oauth2AccessToken token = Oauth2AccessToken
					.parseAccessToken(response);
			if (token != null && token.isSessionValid()) {
				CommFunc.PrintLog(5, LOGTAG, "Success! " + token.toString());

				mAccessToken = token;
				mHandler.obtainMessage(MSG_FETCH_TOKEN_SUCCESS).sendToTarget();
				CommFunc.PrintLog(5, LOGTAG, "mAccessToken: " + mAccessToken);
			} else {
				CommFunc.PrintLog(5, LOGTAG, "Failed to receive access token");
			}
		}

		@Override
		public void onComplete4binary(ByteArrayOutputStream responseOS) {
			CommFunc.PrintLog(5, LOGTAG, "onComplete4binary...");
			mHandler.obtainMessage(MSG_FETCH_TOKEN_FAILED).sendToTarget();
		}

		@Override
		public void onIOException(IOException e) {
			CommFunc.PrintLog(5, LOGTAG, "onIOException： " + e.getMessage());
			mHandler.obtainMessage(MSG_FETCH_TOKEN_FAILED).sendToTarget();
		}

		@Override
		public void onError(WeiboException e) {
			CommFunc.PrintLog(5, LOGTAG, "WeiboException： " + e.getMessage());
			mHandler.obtainMessage(MSG_FETCH_TOKEN_FAILED).sendToTarget();
		}
	}

	Oauth2AccessToken accessToken;

	private void InitAccessToken() {
		accessToken = new Oauth2AccessToken();
		accessToken.setToken(OAuthSharepreference.getToken(this));
		accessToken.setUid(OAuthSharepreference.getUid(this));
		accessToken.setExpiresIn(OAuthSharepreference.getExpires(this));
	}

	public void WeiBoShowUser() {
		InitAccessToken();
		new GetUserAPI(accessToken).GetUser(OAuthSharepreference.getUid(this),
				OAuthSharepreference.getToken(this),
				WeiboConstParam.CONSUMER_KEY, mGetUserRequestListener);
	}

	private String getUserResponse;

	private class GetUserRequestListener implements RequestListener {

		@Override
		public void onComplete(String response) {
			CommFunc.PrintLog(5, LOGTAG, "onComplete: " + response);
			mHandler.obtainMessage(MSG_FETCH_GETUSER_SUCCESS).sendToTarget();
			getUserResponse = response;
		}

		@Override
		public void onComplete4binary(ByteArrayOutputStream responseOS) {
			// Do nothing
			CommFunc.PrintLog(5, LOGTAG,
					"onComplete4binary: " + responseOS.toString());
		}

		@Override
		public void onIOException(IOException e) {
			CommFunc.PrintLog(5, LOGTAG, "onIOException: " + e.getMessage());
			mHandler.obtainMessage(MSG_FETCH_GETUSER_FAILED).sendToTarget();

		}

		@Override
		public void onError(WeiboException e) {
			CommFunc.PrintLog(5, LOGTAG, "onError: " + e.getMessage());
			mHandler.obtainMessage(MSG_FETCH_GETUSER_FAILED).sendToTarget();
		}
	}

	// 本地入库保存并发送给服务器端
	private void OnGetUserSuccess() {
		CommFunc.PrintLog(5, LOGTAG, "OnGetUserSuccess");
		try {
			JSONObject obj = new JSONObject(getUserResponse);
			String name = obj.getString("name");
			String uid = obj.getString("id");
			OAuthSharepreference.setUname(this, name);
			MyApplication.getInstance().getAccountInfo().setUserid(uid);
			MyApplication.getInstance().getAccountInfo().setUsername(name);
			SysConfig.userid = uid;
			// 走登陆流程
			OnStartLogin();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// wwyue0425
	public void showList(View v) throws JSONException {
		final InputMethodManager imm = (InputMethodManager) LoginActivity.this
				.getSystemService(INPUT_METHOD_SERVICE);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
						| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		JSONArray arraysave = new JSONArray();
		arraysave.put("https://test1.chinartc.com:442");
		arraysave.put("http://cloud.chinartc.com:8090");
		arraysave.put("https://cloud.chinartc.com:442");
		MyApplication.getInstance().saveSharePrefValue(MsgKey.pref_addcfg2,
				arraysave.toString());
		String addr = MyApplication.getInstance().getStringSharedXml(
				MsgKey.pref_addcfg2, "[]");
		JSONArray array = new JSONArray(addr);
		List<String> listAdd = new ArrayList<String>();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, listAdd);
		for (int i = 0; i < array.length(); i++) {
			listAdd.add(array.getString(i));
		}
		listViewAdd.setAdapter(adapter);
		listViewAdd.setVisibility(View.VISIBLE);
		listViewAdd.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				String add = ((TextView) arg1).getText().toString().trim();
				et_addresscfg.setText(add);
				if (imm.isActive()) {
					imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				}
				listViewAdd.setVisibility(View.GONE);
			}
		});
	}

	public void saveAdd(String add) {
		String addr = MyApplication.getInstance().getStringSharedXml(
				MsgKey.pref_addcfg2, "[]");
		try {
			JSONArray array = new JSONArray(addr);
			for (int i = 0; i < array.length(); i++) {
				if (array.getString(i).equals(add)) {
					return;
				}
			}
			array.put(add);
			MyApplication.getInstance().saveSharePrefValue(MsgKey.pref_addcfg2,
					array.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
