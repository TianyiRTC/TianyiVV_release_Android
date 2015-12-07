package com.sip.rtcclient;


import java.util.Calendar;
import java.util.UUID;

import jni.http.HttpManager;
import jni.sip.Call;
import jni.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import rtc.sdk.clt.RtcClientImpl;
import rtc.sdk.common.RtcConst;
import rtc.sdk.common.SdkSettings;
import rtc.sdk.core.RtcRules;
import rtc.sdk.iface.ClientListener;
import rtc.sdk.iface.Connection;
import rtc.sdk.iface.ConnectionListener;
import rtc.sdk.iface.Device;
import rtc.sdk.iface.DeviceListener;
import rtc.sdk.iface.GroupMgr;
import rtc.sdk.iface.GroupCallListener;
import rtc.sdk.iface.RtcClient;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceView;

import com.oauth2.weibo.OAuthSharepreference;
import com.sip.rtcclient.activity.ContactActivity;
import com.sip.rtcclient.activity.calling.CallingActivity;
import com.sip.rtcclient.activity.calling.CallingMultiChatActivity;
import com.sip.rtcclient.activity.calling.CallingMultiSpeakActivity;
import com.sip.rtcclient.activity.calling.CallingShowActivity;
import com.sip.rtcclient.activity.calling.CallingVideoConfActivity;
import com.sip.rtcclient.activity.calling.CallingTVActivity;
import com.sip.rtcclient.bean.TAccountInfo;
import com.sip.rtcclient.config.MsgKey;
import com.sip.rtcclient.config.SysConfig;
import com.sip.rtcclient.db.SQLiteManager;
import com.sip.rtcclient.services.ReloginService;
import com.sip.rtcclient.tools.LocalActManager;
import com.sip.rtcclient.utils.CommFunc;
import com.sip.rtcclient.utils.NotifyManager;

//String[] pinyin = PinYinManager.toPinYin(name);
//phoneBookInfo.setSortKey(pinyin[1]);
//String[] pin=BaseUtil.getPingYin(name);
//phoneBookInfo.setPinyinAll(pin[0]);
//phoneBookInfo.setPinyinFirstAll(pin[1]);
//./tcpdump -p -vv -s 0 -w /sdcard/msg20130904_480.pcap

public class MyApplication extends HBaseApp {
	private SQLiteManager sqLiteManager;
	private SysConfig sysconfig;
	private NotifyManager notifymanager;
	private LocalActManager localactmanager = null;

	private HttpManager httpManager = null;
	private TAccountInfo accountinfo = null;
	private String LOGTAG = "MyApplication";
	private RtcClient mClt;
	private String fileFolder = "vv"; // 日志存储目录 //mnt/sdcard/logfolder/log
	public boolean SdkInit = false; // sdk初始化存在获取服务器地址失败的可能 ，如果 sdkinit
									// 在重连接时需要继续初始化。

	@Override
	public void onCreate() {
		super.onCreate();
		// RtcConst.bWapNetSupport = true;
		initSdkLog();
		init();

	}

	/**
	 * 开启Service //开启服务是否启动登陆
	 */
	private void startService(boolean bStartLogin) {

		CommFunc.PrintLog(5, LOGTAG, "startService() bStartLogin:"
				+ bStartLogin);
		Intent intent = new Intent(this, ReloginService.class);
		intent.putExtra("key_bstartLogin", bStartLogin);
		startService(intent);
	}

	public void setUeTypeVal(int type) {
		switch (type) {
		case 0:
			RtcConst.UEType_Current = RtcConst.UEType_TV;
			break;
		case 1:
			RtcConst.UEType_Current = RtcConst.UEType_Phone;
			break;
		case 2:
			RtcConst.UEType_Current = RtcConst.UEType_PC;
			break;
		case 3:
			RtcConst.UEType_Current = RtcConst.UEType_Browser;
			break;
		case 4:
			RtcConst.UEType_Current = RtcConst.UEType_Pad;
			break;
		default:
			RtcConst.UEType_Current = RtcConst.UEType_Other;
			break;
		}
	}

	/**
	 * 初始化
	 */
	private void init() {
		getSqlManager();
		// 需要根据登陆类型取值
		if (getVersionName() != null
				&& getVersionName().equals(getAppVersionName())) {
			SysConfig.login_type = getIntSharedXml(MsgKey.key_logintype,
					SysConfig.USERTYPE_TIANYI);
			if (SysConfig.login_type == SysConfig.USERTYPE_TIANYI) {
				String defaultVal = SysConfig.bDEBUG == true ? "0"
						: SysConfig.userid;
				SysConfig.userid = getSharePrefValue(MsgKey.key_userid,
						defaultVal);
			} else
				SysConfig.userid = OAuthSharepreference.getUid(this);
			MyApplication.getInstance().getAccountInfo()
					.setUserid(SysConfig.userid);
			int nNormalExit = getIntSharedXml(MsgKey.key_isNormalExit, 0);

			CommFunc.PrintLog(5, LOGTAG, "init() nNormalExit:" + nNormalExit);
			if (nNormalExit == 1)// 正常退出时
				startService(false);
			else {
				// 从非登录页面进入登陆
				if (RtcConst.bNewVersion) {
					int uetype = MyApplication.getInstance().getIntSharedXml(
							MsgKey.pref_uetype, 0);
					setUeTypeVal(uetype);
					Utils.PrintLog(5, LOGTAG, "initView uetype:" + uetype);
				}

				if (RtcConst.bAddressCfg) {

					String addr = MyApplication.getInstance()
							.getStringSharedXml(MsgKey.pref_addcfg,
									"http://cloud.chinartc.com:8090");
					if (addr != null && !addr.equals("")) {
						RtcConst.getServerDomain = addr;

					}
				}
				startService(true);
			}
		} else {
			startService(false);
		}
		CommFunc.PrintLog(5, LOGTAG, "init() userid:" + SysConfig.userid
				+ "logintype:" + SysConfig.login_type);
	}

	private void onIncomingCall(Connection call) {
		// {"id":0,"dir":2,"uri":"<sip:18601305661_123@chinartc.com>","t":1}
		JSONObject json;
		try {
			json = new JSONObject(call.info().toString());
			String uri = json.getString(RtcConst.kCallRemoteUri);
			int calltype = json.getInt(RtcConst.kCallType);
			CommFunc.PrintLog(5, LOGTAG, "onIncomingCall:" + uri
					+ "  calltype:" + calltype);
			SysConfig.getInstance().setCallType(calltype);

			Intent intent = new Intent(MyApplication.getInstance(),
					CallingActivity.class);
			intent.putExtra("callNumber", uri);
			intent.putExtra("inCall", true);
			intent.putExtra("isVideo", (calltype == Call.CT_Audio) ? false
					: true);
			intent.putExtra("callRecordId", UUID.randomUUID().toString());
			AlarmManager am = (AlarmManager) MyApplication.getInstance()
					.getSystemService(Context.ALARM_SERVICE);
			PendingIntent pendingIntent = PendingIntent.getActivity(
					MyApplication.getInstance(), 2, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			long time = Calendar.getInstance().getTimeInMillis();
			CommFunc.PrintLog(5, LOGTAG, "pendingIntent time:" + time);
			am.set(AlarmManager.RTC_WAKEUP, 100, pendingIntent);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getFileFolder() {
		return fileFolder;
	}

	public TAccountInfo getAccountInfo() {
		if (accountinfo == null) {
			accountinfo = new TAccountInfo();
		}
		return accountinfo;
	}

	public String getUserID() {
		if (accountinfo != null) {
			return accountinfo.getUserid();
		}
		return "";
	}

	public String getAppAccountID() {
		if (accountinfo != null) {
			return accountinfo.getUserid() + "_" + SysConfig.APP_ID;
		}
		return "";
	}

	public void initAudioCodec() {
		final int audiocodec = MyApplication.getInstance().getIntSharedXml(
				MsgKey.KEY_ACODEC, MsgKey.ACODEC_OPUS);
		CommFunc.PrintLog(5, LOGTAG, "initAudioCodec:" + audiocodec);
		if(mClt == null)
			return;
		HBaseApp.post2WorkRunnable(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				switch (audiocodec) {
				case 1: // opus
					mClt.setAudioCodec(RtcConst.ACodec_OPUS);
					break;
				default: // ilbc
					mClt.setAudioCodec(RtcConst.ACodec_ILBC);
					break;
				}
			}

		});
	}

	public void initVideoCodec() {
		int videocodec = MyApplication.getInstance().getIntSharedXml(
				MsgKey.KEY_VCODEC, MsgKey.VCODEC_VP8);
		JSONObject jsonobj = new JSONObject();
		CommFunc.PrintLog(5, LOGTAG, "initVideoCodec:" + videocodec); //
		if (mClt == null)
			return;
		switch (videocodec) {
		case 1: // H264
			mClt.setVideoCodec(RtcConst.VCodec_H264);
			break;
		default: // Vp8
			mClt.setVideoCodec(RtcConst.VCodec_VP8);
			break;
		}
		CommFunc.PrintLog(5, LOGTAG, "initVideoCodec:" + jsonobj.toString());
	}

	public void initSdkLog() {
		// ils.initlogFolder(getFileFolder()); //sd卡路径 /mnt/sdcard/vv/log/
		// 需要有读写权限
		WriteLog.getInstance().startLog();
	}

	public RtcClient getRtcClient() {
		return mClt;
	}

	public RtcClient InitSdk() {
		if (mClt == null) {
			CommFunc.PrintLog(1, LOGTAG, "getRtcClientSip()");
			mClt = new RtcClientImpl();
			mClt.initialize(getApplicationContext(), new ClientListener() {
				@Override
				// 初始化结果回调
				public void onInit(int result) {
					CommFunc.PrintLog(5, LOGTAG, "getRtcClientSip,result="
							+ result);
					if (result == 0) {
						Intent intent = new Intent(
								SysConfig.BROADCAST_RELOGIN_SERVICE);
						intent.putExtra("what", SysConfig.MSG_SDKInitOK);
						intent.putExtra("arg1", result);
						intent.putExtra("arg2", "sdk init ok");
						MyApplication.getInstance().sendBroadcast(intent);

						SdkInit = true;
						initVideoAttr();
						initAudioCodec();
						initVideoCodec();
					} else {
						mClt.release();
						mClt = null; // 如果中间登陆过程获取服务器地址失败 在继续登陆不走获取服务器地址了
						SdkInit = false;
						CommFunc.PrintLog(5, LOGTAG,
								"sdk initialize failed,result=" + result);
						// 需要上报失败处理
						Intent intent = new Intent(
								SysConfig.BROADCAST_RELOGIN_SERVICE);
						intent.putExtra("what",
								SysConfig.MSG_GETSERVERADRR_FAILED);
						intent.putExtra("arg1", result);
						intent.putExtra("arg2", "登陆失败,sdk初始化失败！");
						MyApplication.getInstance().sendBroadcast(intent);

						// returnValueBroadcast(SysConfig.MSG_TIANYI_VERIFY, -2,
						// "登陆失败请查看错误代码和错误描述！"+ckResult.result);
					}
				}
			});
		}
		return mClt;
	}

	public void initVideoAttr() {
		if (mClt == null)
			return;

		int formate = MyApplication.getInstance().getIntSharedXml(
				MsgKey.KEY_VFORMAT, MsgKey.VIDEO_HD);

		switch (formate) {
			case 1:
				mClt.setVideoAttr(RtcConst.Video_FL);
				break;
			case 2:
				mClt.setVideoAttr(RtcConst.Video_HD);
				break;
			default:
				mClt.setVideoAttr(RtcConst.Video_SD);
				break;
		}

	}

	private void destroyRtcClient() {
		if (mCall != null) {
			mCall.disconnect();
			mCall = null;
		}
		if (mAcc != null) {
			mAcc.release();
			mAcc = null;
		}
		if (mClt != null) {
			mClt.release();
			mClt = null;
		}
	}

	/**
	 * 
	 * @return
	 */
	public static MyApplication getInstance() {
		return (MyApplication) HBaseApp.getInstance();
	}

	public LocalActManager getLocalActManager() {
		if (localactmanager == null) {
			localactmanager = new LocalActManager();
		}
		return localactmanager;
	}

	public HttpManager getHttpManager() {
		if (httpManager == null) {
			httpManager = new HttpManager();
		}
		return httpManager;
	}

	public void destroyHttpManager() {
		httpManager = null;
	}

	public NotifyManager getNotifyManager() {
		if (notifymanager == null) {
			notifymanager = new NotifyManager();
			notifymanager.init();
		}
		return notifymanager;
	}

	/**
	 * 获取SQLiteManager
	 * 
	 * @return
	 */
	public synchronized SQLiteManager getSqlManager() {
		if (sqLiteManager == null) {
			sqLiteManager = new SQLiteManager();
		}
		return sqLiteManager;
	}

	/**
	 * 销毁SQLiteManager
	 */
	public synchronized void destroySqlManager() {
		if (sqLiteManager != null) {
			sqLiteManager.clearInstance();
			sqLiteManager = null;
		}
	}

	public void clearSysConfig() {
		sysconfig = null;
	}

	/**
	 * 
	 * @return
	 */
	public SysConfig getSysConfig() {
		if (sysconfig == null) {
			sysconfig = new SysConfig();
		}
		return sysconfig;
	}

	public void exit() {
		CommFunc.PrintLog(5, LOGTAG, "exit()");
		try {
			localactmanager.finishActivity();
			mAListener = null;
			mCListener = null;
			stopService(new Intent().setClass(this, ReloginService.class));

			destroyRtcClient();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			HBaseApp.post2WorkDelayed(new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					sysconfig = null;
					notifymanager = null;
					localactmanager = null;
					System.exit(0);
					int pid = android.os.Process.myPid();
					android.os.Process.killProcess(pid);
					}
			}, 1000);
		}
	}

	public void saveSharePrefValue(String key, String value) {
		SharedPreferences sp = getSharedPreferences(SysConfig.SHARE_NAME,
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public String getSharePrefValue(String key, String defaultvalue) {
		SharedPreferences sp = getSharedPreferences(SysConfig.SHARE_NAME,
				Activity.MODE_PRIVATE);
		if (key.equals(MsgKey.KEY_ACODEC))
			return sp.getString(key, "" + MsgKey.ACODEC_ILBC);
		else if (key.equals(MsgKey.KEY_VCODEC))
			return sp.getString(key, "" + MsgKey.VCODEC_VP8);
		else if (key.equals(MsgKey.KEY_VFRAMES))
			return sp.getString(key, "8");
		else
			return sp.getString(key, defaultvalue);

	}

	public void saveDataToSharedXml(String[] key, Object[] value) {
		SharedPreferences sharedPreferences_share = getSharedPreferences(
				SysConfig.SHARE_NAME, Activity.MODE_PRIVATE);
		SharedPreferences.Editor shared_editor = sharedPreferences_share.edit();

		for (int i = 0; i < key.length; i++) {
			if (value[i] instanceof String) {
				shared_editor.putString(key[i], (String) value[i]);
			} else if (value[i] instanceof Boolean) {
				shared_editor.putBoolean(key[i], (Boolean) value[i]);
			} else if (value[i] instanceof Integer) {
				shared_editor.putInt(key[i], (Integer) value[i]);
			}
		}
		shared_editor.commit();
	}

	/**
	 * 获取xml中的boolean类型
	 * 
	 * @param key
	 * @param defValue
	 *            没有值时给的默认值
	 * @return
	 */
	// public boolean getBooleanSharedXml(String key, boolean defValue) {
	// SharedPreferences sp = getSharedPreferences(SysConfig.SHARE_NAME,
	// Activity.MODE_PRIVATE);
	// String ret = sp.getString(key, (defValue==false)?"0":"1");
	// if(ret!=null && ret.equals("1"))
	// {
	// return true;
	// }
	// return false;
	// }

	/**
	 * 获取xml中的int类型
	 * 
	 * @param key
	 * @param defValue
	 *            没有取到值时给的默认值
	 * @return
	 */
	public int getIntSharedXml(String key, int defValue) {
		SharedPreferences sp = getSharedPreferences(SysConfig.SHARE_NAME,
				Activity.MODE_PRIVATE);
		String ret = sp.getString(key, "" + defValue);
		if (ret != null && ret.equals("") == false)
			return Integer.parseInt(ret);
		return defValue;
	}

	/**
	 * 获取xml中的String类型
	 * 
	 * @param key
	 * @param defValue
	 *            没有值时给的默认值
	 * @return
	 */
	public String getStringSharedXml(String key, String defValue) {
		SharedPreferences sp = getSharedPreferences(SysConfig.SHARE_NAME,
				Activity.MODE_PRIVATE);
		return sp.getString(key, defValue);
	}

	public String getShare() {
		if (sysconfig == null) {
			return "temp";
		} else {
			return getAppAccountID();
		}
	}

	public String getVersionName() {
		SharedPreferences sp = getSharedPreferences(SysConfig.SHARE_NAME,
				Activity.MODE_PRIVATE);
		return sp.getString(MsgKey.key_version_name, "1.0");
	}

	public void setVersionName(String versionName) {
		SharedPreferences sp = getSharedPreferences(SysConfig.SHARE_NAME,
				Activity.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString(MsgKey.key_version_name, versionName);
		editor.commit();
	}

	public String getAppVersionName() {
		String version = "1.0.0";
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			version = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return version;
	}

	DeviceListener mAListener = new DeviceListener() {
		@Override
		public void onDeviceStateChanged(int result) {
			CommFunc.PrintLog(5, LOGTAG, "onDeviceStateChanged,result=" + result);
			if (result == RtcConst.CallCode_Success) { // 此处需要区分注册还是注销

				sendBroadcastValue(SysConfig.MSG_SIP_REGISTER, result, " 注册成功");
				if (mAcc != null) {
					grpmgr = mAcc.getGroup();
					grpmgr.setGroupCallListener(mGrpVoiceListener);
				}
			} else if (result == RtcConst.NoNetwork) {
				CommFunc.PrintLog(5, LOGTAG, "onDeviceStateChanged onNoNetWork");
				onNoNetWork();
				// getRtcClientSip(); //销毁完后需要重新启动监听保留
				sendBroadNetWorkChange(result); // 用于关闭会议或电话页面
			} else if (result == RtcConst.ChangeNetwork) {
				CommFunc.PrintLog(5, LOGTAG, "onDeviceStateChanged ChangeNetWork");
				// ChangeNetWork();
				sendBroadNetWorkChange(result); // 用于关闭会议或电话页面
			} else if (result == RtcConst.PoorNetwork) {
				CommFunc.PrintLog(5, LOGTAG, "onDeviceStateChanged PoorNetwork");

			} else if (result == RtcConst.ReLoginNetwork) {
				CommFunc.PrintLog(5, LOGTAG, "onDeviceStateChanged ReLoginNetwork");
				ReloginNetWork();
			} else if (result == RtcConst.DeviceEvt_KickedOff) {
				CommFunc.PrintLog(5, LOGTAG, "onDeviceStateChanged DeviceEvt_KickedOff");
				OnKickedOff();
			} else if (result == RtcConst.DeviceEvt_MultiLogin) {
				CommFunc.PrintLog(5, LOGTAG, "onDeviceStateChanged DeviceEvt_MultiLogin");
				HBaseApp.post2UIRunnable(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						CommFunc.DisplayToast(MyApplication.this, "该账号在其他终端类型登陆");
					}
				});
			} else {
				sendBroadcastValue(SysConfig.MSG_SIP_REGISTER, result, "注册失败");
			}
		}

		@Override
		public void onSendIm(int nStatus) {
		}
		@Override
		public void onReceiveIm(String from,String mime,String content) {
		}

		private void OnKickedOff() {
			destroyRtcClient();
			Intent intent = new Intent(MsgKey.key_msg_kickoff);
			sendBroadcast(intent);

		}

		private void sendBroadNetWorkChange(int result) {
			Intent intent = new Intent(CallingActivity.BROADCAST_CALLING_ACTION);
			intent.putExtra("what", MsgKey.SLN_NetWorkChange);
			intent.putExtra("arg1", result); //
			intent.putExtra("arg2", MsgKey.broadmsg_sip); // 用作多人区分消息
			sendBroadcast(intent);
		}

		private void onNoNetWork() {
			CommFunc.PrintLog(5, LOGTAG, "onNoNetWork");
			ReloginService.getInstance().removeLoginAlarm();
			SysConfig.getInstance().setmLoginOK(false);
			SysConfig.getInstance().setCalling(false);
			SysConfig.getInstance().setbIncoming(false);
			// 当前没有呼叫 呼叫标志位销毁
			// 断网销毁
			if (mCall != null) {
				mCall.disconnect();
				mCall = null;
			}
			if (mGroupCall != null) {
				mGroupCall.disconnect();
				mGroupCall = null;
			}

		}

		public void ChangeNetWork() {
			CommFunc.PrintLog(5, LOGTAG, "ChangeNetWork");
			// onNoNetWork();
			// 账户销毁如果放在无网络时销毁将不能监听到网络状态通知，监听后在销毁处理
			// if (mAcc!=null) {
			// mAcc.release();
			// mAcc = null;
			// }
			// ReloginService.getInstance().StartAlarmTimer();

		}

		private void ReloginNetWork() {
			onNoNetWork();
			// 账户销毁如果放在无网络时销毁将不能监听到网络状态通知，监听后在销毁处理
			if (mAcc != null) {
				mAcc.release();
				mAcc = null;
			}
			if (SysConfig.getInstance().isLoginByBtn() == false)
				ReloginService.getInstance().StartAlarmTimer();
		}

		@Override
		public void onNewCall(Connection call) {
			CommFunc.PrintLog(5, LOGTAG, "DeviceListener onNewCall,call="
					+ call.info());
			if (mCall != null || mGroupCall != null) {
				call.reject();
				call = null;
				CommFunc.PrintLog(5, LOGTAG,
						"DeviceListener onNewCall,reject call");
				return;
			}

			SysConfig.getInstance().setbIncoming(true);
			mCall = call;
			call.setIncomingListener(mCListener);
			onIncomingCall(mCall);

		}

		@Override
		public void onQueryStatus(int status, String paramers) {
			// TODO Auto-generated method stub

			/*
			 * { "userStatusList": [ { "presenceTime": "2014-03-10",
			 * "othOnlineInfoList": null, "status": -1, "appAccountId":
			 * "10-13691261873~123~any" }, { "presenceTime": "2014-03-20",
			 * "othOnlineInfoList": null, "status": -1, "appAccountId":
			 * "10-5662~123~any" } ], "reason": "查询成功", "code": "0",
			 * "requestId": "2014-03-20 14:04:12:012" }
			 */
			//wwyue
        	if(MyApplication.this.act != null)
        	{
        		Message msg = mHandler.obtainMessage();
        		msg.obj = paramers;
        		msg.arg1 = status;
        		mHandler.sendMessage(msg);
        	}
			CommFunc.PrintLog(5, LOGTAG, "DeviceListener onQueryStatus,status:"
					+ status);
			CommFunc.PrintLog(5, LOGTAG,
					"DeviceListener onQueryStatus,paramers:" + paramers);
		}
	};

	private Device mAcc = null; // sipreg
	private GroupMgr grpmgr;

	public GroupMgr getGrpMgr() {
		return grpmgr;
	}

	public void setConfMgr(GroupMgr confMgr) {
		grpmgr = confMgr;
	}

	public DeviceListener getDeviceListener() {
		return mAListener;
	}

	private Connection mCall;

	public Connection getMCall() // 获取点对点呼叫对象
	{
		return mCall;
	}

	ConnectionListener mCListener = new ConnectionListener() {
		@Override
		public void onConnecting() {
			CommFunc.PrintLog(5, LOGTAG, "mCListener onConnecting");
			Intent intent = new Intent(CallingActivity.BROADCAST_CALLING_ACTION);
			intent.putExtra("what", MsgKey.SLN_180Ring);
			intent.putExtra("arg2", MsgKey.broadmsg_sip);
			MyApplication.getInstance().sendBroadcast(intent);
		}

		@Override
		public void onConnected() {
			SysConfig.getInstance().setCalling(true);
			CommFunc.PrintLog(5, LOGTAG, "mCListener onConnected");
			Intent intent = new Intent(CallingActivity.BROADCAST_CALLING_ACTION);
			intent.putExtra("what", MsgKey.SLN_CallAccepted);
			intent.putExtra("arg2", MsgKey.broadmsg_sip);
			MyApplication.getInstance().sendBroadcast(intent);
		}

		@Override
		public void onDisconnected(int code) {
			if (mCall != null) {
				CommFunc.PrintLog(5, LOGTAG,
						"onCallHangup timerDur" + mCall.getCallDuration());
				CommFunc.PrintLog(5, LOGTAG, "onCallHangup timerDur"
						+ FormatTime(mCall.getCallDuration()));
			}
			SysConfig.getInstance().setCalling(false);
			SysConfig.getInstance().setbIncoming(false);
			CommFunc.PrintLog(5, LOGTAG, "mCListener onDisconnect,code=" + code);
			Intent intent = new Intent(CallingActivity.BROADCAST_CALLING_ACTION);
			if (code == 200) // 此处需要确认disconnect 状态 正常挂断和呼叫失败
			{
				intent.putExtra("what", MsgKey.SLN_CallClosed);
			} else if (code == RtcConst.CallCode_hasAccepted) {
				intent.putExtra("what", MsgKey.SLN_CallHasAccepted);
			} else {
				intent.putExtra("what", MsgKey.SLN_CallFailed);
			}
			intent.putExtra("arg1", code);
			intent.putExtra("arg2", MsgKey.broadmsg_sip);
			MyApplication.getInstance().sendBroadcast(intent);
			mCall = null;

		}

		@Override
		public void onVideo() {
			CommFunc.PrintLog(5, LOGTAG, "onVideo");
			Intent intent = new Intent(CallingActivity.BROADCAST_CALLING_ACTION);
			intent.putExtra("what", MsgKey.SLN_CallVideo);
			intent.putExtra("arg2", MsgKey.broadmsg_sip);
			MyApplication.getInstance().sendBroadcast(intent);

		}

		@Override
		public void onNetStatus(int msg, String info) {
			// TODO Auto-generated method stub
			// CommFunc.PrintLog(5, LOGTAG,
			// "onNetStatus msg:"+msg+" info:"+info);
			// Log.e("Application", "onNetStatus msg:"+msg+" info:"+info);
			// by cpl
			Intent intent = new Intent(CallingActivity.BROADCAST_CALLING_ACTION);
			intent.putExtra("what", MsgKey.SLN_WebRTCStatus);
			intent.putExtra("arg1", msg); //
			intent.putExtra("arg2", 0);
			intent.putExtra("info", info);
			MyApplication.getInstance().sendBroadcast(intent);
		}
	};

	public void buildVideo(SurfaceView mvRemote) {
		if (mCall != null) {
			mCall.buildVideo(mvRemote);
		}
	}

	public boolean OnLoginFailReLogin() {
		if (SysConfig.getInstance().ismLoginOK() == false) {
			CommFunc.DisplayToast(this, "连接已断开正在登陆中");
			ReloginService.getInstance().StartAlarmTimer();
			return true;
		}
		return false;
	}

	public void CreateConf(String params) {
		CommFunc.PrintLog(5, LOGTAG, "CreateConf: " + params);
		// CommFunc.PrintLog(1, LOGTAG, "CreateConf:"+params);
		if (OnLoginFailReLogin())
			return;
		if (mAcc == null) {
			CommFunc.PrintLog(5, LOGTAG, "CreateConf  mAcc == null");
			return;
		}
		grpmgr = mAcc.getGroup();
		grpmgr.setGroupCallListener(mGrpVoiceListener);
		grpmgr.groupCall(RtcConst.groupcall_opt_create, params);

	}

	public void QueryStatus(String parameters) {
		CommFunc.PrintLog(5, LOGTAG, "parameters:" + parameters);
		if (mAcc != null)
			mAcc.queryStatus(parameters);
	}

	public void MakeCall(String remoteuser, int calltype) {
		if (OnLoginFailReLogin())
			return;
		String remoteuri = "";
		remoteuri = RtcRules.UserToRemoteUri_new(remoteuser, RtcConst.UEType_Any);
		CommFunc.PrintLog(5, LOGTAG, "MakeCall user:" + remoteuser
				+ "calltype:" + calltype + " remoteuri: " + remoteuri);
		SysConfig.getInstance().setbIncoming(false);
		JSONObject jinfo = new JSONObject();
		try {
			jinfo.put(RtcConst.kCallRemoteUri, remoteuri);
			jinfo.put(RtcConst.kCallType, calltype);
			mCall = mAcc.connect(jinfo.toString(), mCListener);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void onCallAccept() {

		int calltype = SysConfig.getInstance().getCallType();
		CommFunc.PrintLog(5, LOGTAG, "onCallAccept():" + calltype); // SysConfig.getInstance().isbIncoming()&&
		if (mCall != null) {
			SysConfig.getInstance().setbIncoming(false);
			mCall.accept(SysConfig.getInstance().getCallType());
			CommFunc.PrintLog(5, LOGTAG, "onBtnCall mIncoming accept:"
					+ calltype);
		}
	}

	public void onCallHangup() {
		CommFunc.PrintLog(5, LOGTAG, "onCallHangup()");
		if (mCall != null) {
			mCall.disconnect();

			CommFunc.PrintLog(5, LOGTAG,
					"onCallHangup timerDur" + mCall.getCallDuration());
			CommFunc.PrintLog(5, LOGTAG, "onCallHangup timerDur"
					+ FormatTime(mCall.getCallDuration()));
			mCall = null;
			CommFunc.PrintLog(5, LOGTAG, "onCallHangup disconnect");
		}
	}

	public String FormatTime(long date) {
		if (date != 0) { // kk:mm:ss
			long l = date;
			long day = l / (24 * 60 * 60 * 1000);
			long hour = (l / (60 * 60 * 1000) - day * 24);
			long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
			long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
			System.out.println("" + day + "天" + hour + "小时" + min + "分" + s
					+ "秒");
			String time = "hh:" + hour + "mm:" + min + "ss:" + s;
			return time;
		}
		return null;
	}

	public int srtp_mode = 1;
	public void disposeSipRegister() {

		if (mAcc != null) {
			mAcc.release();
			mAcc = null;
		}
		CommFunc.PrintLog(5, LOGTAG, "disposeSipRegister:"
				+ getAccountInfo().getUserid() + "token:"
				+ getAccountInfo().getResttoken());
		try {

			final JSONObject jargs = SdkSettings.defaultDeviceSetting();
			jargs.put(RtcConst.kAccAppID, SysConfig.APP_ID);
			jargs.put(RtcConst.kAccPwd, getAccountInfo().getResttoken());
			jargs.put(RtcConst.kAccUser, getUserID());
			jargs.put(RtcConst.kAccType, RtcConst.UEType_Current);
			jargs.put(RtcConst.kAccSrtp, srtp_mode);

			if (mClt != null)
				mAcc = mClt.createDevice(jargs.toString(), mAListener); // 注册
			else {
				CommFunc.PrintLog(5, LOGTAG,
						"disposeSipRegister fail mcl == null");
				InitSdk().createDevice(jargs.toString(), mAListener);
			}
			if(mAcc == null)
			{
			      mClt.release();
			      mClt = null; 
      		      SdkInit = false;
				CommFunc.PrintLog(5, LOGTAG,
								"sdk initialize failed");
				Intent intent = new Intent(
								SysConfig.BROADCAST_RELOGIN_SERVICE);
				intent.putExtra("what",
								SysConfig.MSG_GETSERVERADRR_FAILED);
				intent.putExtra("arg1", -1);
				intent.putExtra("arg2", "登陆失败,sdk初始化失败！");
				MyApplication.getInstance().sendBroadcast(intent);
			}

		} catch (JSONException e) {
			CommFunc.PrintLog(5, LOGTAG, "disposeSipRegister():JSONException:"
					+ e.getMessage());
			e.printStackTrace();
		}
	}

	private void sendBroadcastValue(int nCmdID, int nCmdArg, String sExtra) {
		Intent intent = new Intent(SysConfig.BROADCAST_RELOGIN_SERVICE);
		intent.putExtra("what", nCmdID);
		intent.putExtra("arg1", nCmdArg);
		intent.putExtra("arg2", sExtra);
		sendBroadcast(intent);
	}

	public Connection mGroupCall;

	public Connection getMGroupCall() // 获取点对点呼叫对象
	{
		return mGroupCall;
	}

	private ConnectionListener mConfListener = new ConnectionListener() {
		@Override
		public void onConnecting() {
			CommFunc.PrintLog(5, LOGTAG,
					"ConnectionListener mConfListener :onConnecting ");
			CommFunc.PrintLog(5, LOGTAG, "onConnecting");
			Intent intent = new Intent(CallingActivity.BROADCAST_CALLING_ACTION);
			intent.putExtra("what", MsgKey.SLN_180Ring);
			intent.putExtra("arg2", MsgKey.broadmsg_sip);
			MyApplication.getInstance().sendBroadcast(intent);
		}

		@Override
		public void onConnected() {
			CommFunc.PrintLog(5, LOGTAG,
					"ConnectionListener mConfListener onConnected");
			CommFunc.PrintLog(5, LOGTAG, "onConnected");
			Intent intent = new Intent(CallingActivity.BROADCAST_CALLING_ACTION);
			intent.putExtra("what", MsgKey.SLN_CallAccepted);
			intent.putExtra("arg2", MsgKey.broadmsg_sip);
			MyApplication.getInstance().sendBroadcast(intent);

		}

		@Override
		public void onDisconnected(int code) {
			CommFunc.PrintLog(5, LOGTAG, "mConfListener  onDisconnect,code="
					+ code);
			Intent intent = new Intent(CallingActivity.BROADCAST_CALLING_ACTION);
			if (code == 200) // 此处需要确认disconnect 状态 正常挂断和呼叫失败
			{
				intent.putExtra("what", MsgKey.SLN_CallClosed);
			} else {
				intent.putExtra("what", MsgKey.SLN_CallFailed);
			}
			intent.putExtra("arg1", code);
			intent.putExtra("arg2", MsgKey.broadmsg_sip);
			MyApplication.getInstance().sendBroadcast(intent);
			mGroupCall = null;
			// //此处处理会议挂断

		}

		@Override
		public void onVideo() {
			CommFunc.PrintLog(5, LOGTAG, "ConnectionListener:onVideo");
			CommFunc.PrintLog(5, LOGTAG, "onVideo");
			Intent intent = new Intent(CallingActivity.BROADCAST_CALLING_ACTION);
			intent.putExtra("what", MsgKey.SLN_CallVideo);
			intent.putExtra("arg2", MsgKey.broadmsg_sip);
			MyApplication.getInstance().sendBroadcast(intent);
			// 视频会议处理
		}

		@Override
		public void onNetStatus(int msg, String info) {
			// TODO Auto-generated method stub
			// CommFunc.PrintLog(5, LOGTAG,
			// "onNetStatus msg:"+msg+" info:"+info);
			// Log.e("MyApplication", "onNetStatus msg:"+msg+" info:"+info);
			// by cpl
			Intent intent = new Intent(CallingActivity.BROADCAST_CALLING_ACTION);
			intent.putExtra("what", MsgKey.SLN_WebRTCStatus);
			intent.putExtra("arg1", msg); //
			intent.putExtra("arg2", 0);
			MyApplication.getInstance().sendBroadcast(intent);
		}
	};

	public void onConfCallAccept() {
		int calltype = SysConfig.getInstance().getCallType();
		CommFunc.PrintLog(5, LOGTAG, "onConfCallAccept():" + calltype
				+ "mGroupCall:" + mGroupCall); // SysConfig.getInstance().isbIncoming()&&
		if (mGroupCall != null) {
			SysConfig.getInstance().setbIncoming(false);
			CommFunc.PrintLog(5, LOGTAG, "onConfCallAccept:"
					+ SysConfig.getInstance().getCallType());
			mGroupCall.accept(SysConfig.getInstance().getCallType());
		}
	}

	public void onConfCallHangup() {
		CommFunc.PrintLog(5, LOGTAG, "onConfCallHangup()" + "mGroupCall:"
				+ mGroupCall);
		if (mGroupCall != null) {
			mGroupCall.setIncomingListener(null);
			mGroupCall.disconnect();
			mGroupCall = null;
			CommFunc.PrintLog(5, LOGTAG, "onConfCallHangup disconnect");
		}
	}

	private void OnInconmingCallConf(String confid, int conftype,
			String creator, String gvcname) {
		CommFunc.PrintLog(5, LOGTAG, "Conf  OnInconmingCallConf：" + conftype
				+ "confname:" + gvcname + " creator:" + creator);
		Intent intent = null;
		switch (conftype) {
		case RtcConst.grouptype_multigrpchatA: // 聊天室
			intent = new Intent(MyApplication.this,
					CallingMultiChatActivity.class);
			break;
		case RtcConst.grouptype_multigrpspeakA: // 群对讲
			intent = new Intent(MyApplication.this,
					CallingMultiSpeakActivity.class);
			break;
		case RtcConst.grouptype_multitwoA: // vv秀场 多方两人语音
			intent = new Intent(MyApplication.this, CallingShowActivity.class);
			break;
		case RtcConst.grouptype_multigrpchatAV: // 视频会议
			intent = new Intent(MyApplication.this, CallingVideoConfActivity.class);
			break;
		case RtcConst.grouptype_microliveAV: // 现场直播
			intent = new Intent(MyApplication.this, CallingTVActivity.class);
			break;
		default:
			return;
			// break;
		}
		// intent.putExtra("groupInfo", groupInfo);
		intent.putExtra("inCall", true);
		intent.putExtra(RtcConst.kgvcname, gvcname);
		intent.putExtra(RtcConst.kgvccreator, creator);
		intent.putExtra(RtcConst.kGrpID, confid);
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getActivity(
				MyApplication.this, 2, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		long time = Calendar.getInstance().getTimeInMillis();
		CommFunc.PrintLog(5, LOGTAG, "pendingIntent time:" + time);
		am.set(AlarmManager.RTC_WAKEUP, 200, pendingIntent);

	}

	GroupCallListener mGrpVoiceListener = new GroupCallListener() {

		@Override
		// 用于处理会议结果返回的提示
		public void onResponse(int action, String parameters) {
			// TODO Auto-generated method stub
			// parameters 为请求和返回的json
			CommFunc.PrintLog(5, "GroupVoiceListener", "onResponse action["
					+ action + "]  parameters:" + parameters);
			Intent intent = new Intent(CallingActivity.BROADCAST_CALLING_ACTION);
			intent.putExtra("what", action);
			intent.putExtra("arg1", parameters);
			intent.putExtra("arg2", MsgKey.grpv_listener_onResponse); // 多种消息广播类型区分
			sendBroadcast(intent);
		}

		@Override
		//
		public void onCreate(Connection call) {

			// TODO Auto-generated method stub
			if (mGroupCall != null || mCall != null) // 当前有会议或者呼叫则挂断新来呼叫
			{
				call.reject();
				call = null;
				CommFunc.PrintLog(5, LOGTAG,
						"mGrpVoiceListener onCreate,reject call");
				return;
			}
			mGroupCall = call;
			mGroupCall.setIncomingListener(mConfListener);
			Utils.PrintLog(5, "GroupVoiceListener onCreate", "onCreate info"
					+ call.info().toString());
			try {
				boolean isviter;
				JSONObject json = new JSONObject(call.info());
				isviter = json.getBoolean(RtcConst.kGrpInviter);
				final int conftype = json.getInt(RtcConst.kGrpType);
				final String gvccreator = json.has(RtcConst.kgvccreator) ? json
						.getString(RtcConst.kgvccreator) : ""; // 创建者可以等到notify过来时刷新
				final String gvcname = json.has(RtcConst.kGrpname) ? json
						.getString(RtcConst.kGrpname) : "";
				final String confid = json.getString(RtcConst.kGrpID);
				int calltype = json.getInt(RtcConst.kCallType);
				SysConfig.getInstance().setCallType(calltype);
				Utils.PrintLog(5, LOGTAG, "GroupVoiceListener onCreate info:"
						+ call.info());
				if (isviter == false) // 非创建者接听 创建者 在底层自动接听
				{
					// HBaseApp.post2UIRunnable(new Runnable() {
					// @Override
					// public void run() {
					OnInconmingCallConf(confid, conftype, gvccreator, gvcname);
					// }
					// });
					Utils.PrintLog(5, LOGTAG,
							"有会议邀请自动加入" + RtcConst.getGrpType(conftype)
									+ "calltype:" + calltype);
				} else {
					Utils.PrintLog(
							5,
							LOGTAG,
							"会议创建成功  bCreater is true  conftype："
									+ RtcConst.getGrpType(conftype)
									+ "calltype:" + calltype);
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onNotify(String parameters) {
			// TODO Auto-generated method stub
			// Utils.PrintLog(5, LOGTAG,
			// "GroupVoiceListener onRequest action"+RtcConst.getConfType(action));
			// Utils.PrintLog(5, LOGTAG, "  parameters:"+parameters);

			Intent intent = new Intent(CallingActivity.BROADCAST_CALLING_ACTION);
			intent.putExtra("arg1", parameters);
			intent.putExtra("arg2", MsgKey.grpv_listener_onRequest); // 多种消息广播类型区分
			sendBroadcast(intent);
			// notify信息通知给ui层发送广播
		}
	};

	// wwyue
	private Activity act;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String paramers = msg.obj.toString();
			int status = msg.arg1;
			((ContactActivity) act).freshConStatus(status, paramers);
		}
	};

	public void QueryStatus(Activity act, String parameters) {
		this.act = act;
		CommFunc.PrintLog(5, LOGTAG, "parameters:" + parameters);
		if (mAcc != null)
		{
			mAcc.queryStatus(parameters);
		}
	}

}
