package com.sip.rtcclient.services;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import jni.http.HttpManager;
import jni.http.HttpResult;
import jni.http.RtcHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import rtc.sdk.common.RtcConst;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import cn.com.chinatelecom.account.exception.TelecomAccountException;
import cn.com.chinatelecom.account.lib.apk.TelecomException;
import cn.com.chinatelecom.account.lib.apk.AuthParam;
import cn.com.chinatelecom.account.lib.apk.AuthResult;
import cn.com.chinatelecom.account.lib.apk.TelecomProcessState;
import cn.com.chinatelecom.account.lib.ct.Authorizer;


import com.oauth2.weibo.OAuthSharepreference;
import com.sip.rtcclient.HBaseApp;
import com.sip.rtcclient.MyApplication;
import com.sip.rtcclient.bean.TAccountInfo;
import com.sip.rtcclient.config.MsgKey;
import com.sip.rtcclient.config.SysConfig;
import com.sip.rtcclient.utils.CommFunc;
import com.sip.rtcclient.utils.NetWorkUtil;
import com.sip.rtcclient.utils.LoginUtil;


public class ReloginService extends Service {

    private static ReloginService instance = null;
    private String LOGTAG = "ReloginService";
    private int mCallFlags = 0;
    private TAccountInfo mAccountInfo = MyApplication.getInstance().getAccountInfo();
    public static ReloginService getInstance() {
        if (instance == null) {
            CommFunc.PrintLog(5, "ReloginService", "instance()"); 
        }
        return instance;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        instance = this;
        CommFunc.PrintLog(5, LOGTAG, "onCreate()");
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(MsgKey.key_msg_appexit);
        intentFilter.addAction(SysConfig.BROADCAST_RELOGIN_SERVICE);
        registerReceiver(receiver, intentFilter);
        super.onCreate();
    }


    @SuppressWarnings("deprecation")
	@Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        if(intent == null)
        {
            CommFunc.PrintLog(5, LOGTAG, "onStart intent == null");
            return;
        }
        Bundle bundle = intent.getExtras();
        boolean bstarttimer = false;
        if(bundle!=null)
            bstarttimer= bundle.getBoolean("key_bstartLogin");
        CommFunc.PrintLog(5, LOGTAG, "startId():"+startId+"   reLoginAlarm:"+reLoginAlarm+"   bstarttimer:"+bstarttimer);
        if(bstarttimer==true)
        {
            StartAlarmTimer(); //初始从非登陆页面及网路异常等情况进入开启定时器
            CommFunc.PrintLog(5, LOGTAG, "onStart StartAlarmTimer()");
        }
        else if(reLoginAlarm != null) //定时器调用
        {
            CommFunc.PrintLog(5, LOGTAG, "onStart reLoginAlarm != null  InitLogin");
            InitLogin();
        }
    }
    public void StartAlarmTimer()
    {   
        if(SysConfig.getInstance().isLoginByBtn())
            return;
        CommFunc.PrintLog(5, LOGTAG, "StartAlarmTimer");
        Relogincount = 0;
        removeLoginAlarm();
        reLoginAlarm();  
    }
    public void InitLogin()
    {  
        CommFunc.PrintLog(5, LOGTAG, "InitLogin() ismLoginOK:"+SysConfig.getInstance().ismLoginOK());
        if (!SysConfig.getInstance().ismLoginOK())
        {   
            boolean bret = NetWorkUtil.isNetConnect(MyApplication.getInstance());
            CommFunc.PrintLog(5, LOGTAG, "InitLogin() isNetConnect:"+bret);
            if (bret) { // 如果有网络连接
                if (MyApplication.getInstance().getRtcClient() != null) {
                    CommFunc.PrintLog(5, LOGTAG, "RestartLogin()");
                    ReloginService.getInstance().RestartLogin();
                } else {
                    CommFunc.PrintLog(5, LOGTAG, "CallSdk()");
                    ReloginService.getInstance().CallSdk();
                }
            } 
            else
            {
                removeLoginAlarm();
                CommFunc.PrintLog(5, LOGTAG, "InitLogin() removeLoginAlarm()--nonet");
            }
        }else
        {
            removeLoginAlarm();
            CommFunc.PrintLog(5, LOGTAG, "InitLogin() removeLoginAlarm()");

        }
    }

    /**
     * 带值通知界面处理广播
     * 
     * @param msg
     */
    private static void returnValueBroadcast(int nCmdID, int nCmdArg,
            String sExtra) {
        Intent intent = new Intent(SysConfig.BROADCAST_RELOGIN_SERVICE);
        intent.putExtra("what", nCmdID);
        intent.putExtra("arg1", nCmdArg);
        intent.putExtra("arg2", sExtra);
        MyApplication.getInstance().sendBroadcast(intent);
    }

    public void RestartLogin() {  
        CommFunc.PrintLog(5, LOGTAG, " RestartLogin()"); 
        HBaseApp.post2WorkRunnable(new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                //opt_getServerAddress(); 
                //如果天翼登陆走此流程
                if(SysConfig.login_type==SysConfig.USERTYPE_TIANYI)
                {
                    //如果非登陆页面连接有userid直接走获取token
                    String userid = MyApplication.getInstance().getSharePrefValue(LoginUtil.LOGIN_UID, "");
                    if(userid.equals("")==false)
                    {
                        SysConfig.userid = userid;
                        restGetToken(SysConfig.userid);
                    }
                }
                else if(SysConfig.login_type==SysConfig.USERTYPE_WEIBO)
                    //否则直接走获取token接口
                {
                    String uid = OAuthSharepreference.getUid(MyApplication.getInstance().getApplicationContext());
                    mAccountInfo.setUserid(uid);
                    mAccountInfo.setUsername(OAuthSharepreference.getUname(MyApplication.getInstance().getApplicationContext()));
                    SysConfig.userid = uid;
                    restGetToken(uid);////userid / 获取sip注册token
                }
            }

        });

        // requestVerify(); //不走获取服务器接口时直接走此处
    }
    public void CallSdk()//只有调用sdk获取服务器地址成功后才能走sip注册流程
    {
        // if(SysConfig.getInstance().isLoginByBtn())
        MyApplication.getInstance().InitSdk(); //initSdk 
    }
    public int getCallFlags() {
        return mCallFlags;
    }

    // 以下为帐号登陆相关
    AuthResult ckResult;
    String localuserid;
    // private MsgBroadcastReciver telecomReciver=null;
    final static String APPID = "Chinavv";
    final static String SECRET = "ohOeugfjOxDmnlvqcIOwRrAIDQJJrgdX";
    final static SimpleDateFormat TIMESTAMP_FORMATER = new SimpleDateFormat(
    "yyyy-MM-dd HH:mm:ss");
    final static Calendar CALENDAR = Calendar.getInstance();
    //账号校验 
    public void requestVerify() {
        new AsyncTask<Object, Object, Object>() {
            protected Object doInBackground(Object... arg0) {
                Authorizer auth = new Authorizer(MyApplication.getInstance());
                AuthParam param = new AuthParam();
                param.appID = APPID;
                param.appSecret = SECRET;
                param.extension = "";
                param.webLoginSupport = true;
                AuthResult result = null;
                try {
                    result = auth.eAccountLogin(param);

                } catch (Exception tae) {
                    CommFunc.PrintLog(5, LOGTAG, "Exception:"+tae.getMessage());
                    return tae;
                };
                return result;
            };

            @Override
            protected void onPostExecute(Object execResult) {
                // progressDialog2.dismiss();
                if (execResult instanceof TelecomAccountException) {
                    TelecomAccountException ex = ((TelecomAccountException) execResult);
                    //                    Toast.makeText(MyApplication.getInstance(),
                    //                            "天翼帐号接口错误，请检查天翼帐号和系统的配置！", Toast.LENGTH_LONG)
                    //                            .show();
                    //                    returnValueBroadcast(SysConfig.MSG_TIANYI_VERIFY, -1, "天翼帐号接口错误，请检查天翼帐号和系统的配置！");
                    return ; 
                }
                ckResult = (AuthResult) execResult;
                CommFunc.PrintLog(5, LOGTAG, ckResult.result + " " + ckResult.errorDescription);

                if(SysConfig.bDEBUG)
                    ckResult.result = 0; // 模拟数据测试

                switch (ckResult.result) {
                    case 0: {// 表示登录成功
                        if(SysConfig.bDEBUG)
                            mAccountInfo.setUserid(SysConfig.userid); //userid / 模拟数据测试
                        else
                        {
                            String userid = ckResult.accountInfo.userName;
                            mAccountInfo.setUserid(userid);
                            SysConfig.userid = userid;
                        }

                        CommFunc.PrintLog(5, LOGTAG, "帐号认证成功 userid:"+SysConfig.userid);
                        //                       returnValueBroadcast(SysConfig.MSG_TIANYI_VERIFY_SUCCESS, 0, "天翼认证成功");

                        HBaseApp.post2WorkRunnable(new Runnable() {
                            @Override
                            public void run() {
                                restGetToken(SysConfig.userid);////userid / 模拟数据测试
                            }
                        });

                        break;
                    }

                    case TelecomProcessState.TelecomStateUserOnLoginFlag: {// 表示需要等待
                        CommFunc.PrintLog(5, LOGTAG, "如果发现进入到这里，请等待");
                        break;
                    }

                    case TelecomException.TelecomCheckVersionFlag: { // 天翼帐号APK版本过低；
                        Authorizer auth = new Authorizer(MyApplication.getInstance());
                        try {
                            auth.getLatestAccountAPK();
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                    }

                    default: {// 如果非零非等待的其他返回，表示出错了
                        returnValueBroadcast(SysConfig.MSG_TIANYI_VERIFY, -2, "错误描述！"+ckResult.errorDescription+"\r\n");
                        CommFunc.PrintLog(5, LOGTAG, "其他情况下都是出现错误，请查看错误代码和错误描述"+ckResult.result);
                        break;
                    }				
                }
            }

        }.execute();
    }

    //获取token
    public void restGetToken(String userid) {
        CommFunc.PrintLog(5, LOGTAG, "userid:" + userid);
        MyApplication.getInstance().saveSharePrefValue(MsgKey.key_tianyi_userid, userid);
        String capabailitytoken = MyApplication.getInstance().getSharePrefValue(MsgKey.key_rtc_token, "invalid");
        if(!capabailitytoken.equals("invalid")) {
            mAccountInfo.setResttoken(capabailitytoken);
            mAccountInfo.setUserid(userid);
            CommFunc.PrintLog(5, LOGTAG, "获取token成功 userid:"+userid +"  token:"+capabailitytoken);
            returnValueBroadcast(SysConfig.MSG_GETTOKEN_SUCCESS, 0, "使用已获取过的token");
            return;
        }
        //  final String userID = userid;
        CommFunc.PrintLog(5, LOGTAG, "restGetToken:"+userid);
        JSONObject jsonobj = HttpManager.getInstance().CreateTokenJson(0,userid,RtcHttpClient.grantedCapabiltyID,"");
        HttpResult  httpresult = HttpManager.getInstance().getCapabilityToken(jsonobj, SysConfig.APP_ID, SysConfig.APP_KEY); 
        JSONObject jsonrsp = (JSONObject)httpresult.getObject();
        if (jsonrsp!=null && jsonrsp.isNull("code")==false) {
            String code;
            try {
                code = jsonrsp.getString(RtcConst.kcode);
                String reason = jsonrsp.getString(RtcConst.kreason);
                CommFunc.PrintLog(5, LOGTAG, "restGetToken code:"+code +"reason:"+reason);
                if(code!=null && code.equals("0"))
                {
                    capabailitytoken =jsonrsp.getString(RtcConst.kcapabilityToken);
                    mAccountInfo.setResttoken(capabailitytoken);
                    mAccountInfo.setUserid(userid);
                    CommFunc.PrintLog(5, LOGTAG, "获取token成功 userid:"+userid +"  token:"+capabailitytoken);
                    returnValueBroadcast(SysConfig.MSG_GETTOKEN_SUCCESS, httpresult.getStatus(), "获取token成功:"+reason);
                    MyApplication.getInstance().saveSharePrefValue(MsgKey.key_rtc_token, capabailitytoken);
                    //如果非登陆页面网络切换后自动重连接需要发起sip注册
                }
                else
                {  
                    returnValueBroadcast(SysConfig.MSG_GETTOKEN_ERROR, Integer.parseInt(code), reason);
                    CommFunc.PrintLog(5, LOGTAG,
                            "获取token错误 原因:" + reason+ "code:"+code); 
                }
                return;
            }

            catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            returnValueBroadcast(SysConfig.MSG_GETTOKEN_ERROR, httpresult.getStatus(), "获取token失败:"+httpresult.getErrorMsg());
            CommFunc.PrintLog(5, LOGTAG,
                    "获取token错误 httpresult:" + httpresult.toString());  
        }
        else
        {
            returnValueBroadcast(SysConfig.MSG_GETTOKEN_ERROR, httpresult.getStatus(), "获取token失败:"+httpresult.getErrorMsg());
            CommFunc.PrintLog(5, LOGTAG,
                    "获取token错误 httpresult:" + httpresult.toString());   
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        // TODO Auto-generated method stub
//       // flags = START_STICKY;
//        //  CommFunc.PrintLog(5, LOGTAG, "onStartCommand:"+startId);
//        return super.onStartCommand(intent, flags, startId);
//        // return START_REDELIVER_INTENT;
//    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        removeLoginAlarm();
        unregisterReceiver(receiver);
        CommFunc.PrintLog(5, LOGTAG, "onDestroy()");
        //     MyApplication.getInstance().saveSharePrefValue(MsgKey.key_isNormalExit,""+1);
        //        if(MyApplication.getInstance().getIntSharedXml(MsgKey.key_isNormalExit,0) == 0)
        //            startService(new Intent(this, ReloginService.class));  //异常杀死重新启动
    }

    private BroadcastReceiver receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(SysConfig.BROADCAST_RELOGIN_SERVICE))
            {

                switch (intent.getIntExtra("what", -1)) {
                    case SysConfig.MSG_SIP_REGISTER:
                    {     
                        if(SysConfig.getInstance().isLoginByBtn()==false)
                        {
                            CommFunc.PrintLog(5, LOGTAG, "MSG_SIP_REGISTER");
                            OnLoginResult(intent);
                        }
                        break;
                    }
                    case SysConfig.MSG_TIANYI_VERIFY:
                    {
                        CommFunc.PrintLog(5, LOGTAG, "MSG_TIANYI_VERIFY");
                        break;
                    }
                    case SysConfig.MSG_SDKInitOK:
                    {
                        if(SysConfig.getInstance().isLoginByBtn()==false)
                            RestartLogin();
                    }
                    break;
                    case SysConfig.MSG_GETTOKEN_ERROR:
                    {   
                        CommFunc.PrintLog(5, LOGTAG, "MSG_GETTOKEN_ERROR");
                        break;
                    }
                    case SysConfig.MSG_GETTOKEN_SUCCESS:
                    { 
                        if(SysConfig.getInstance().isLoginByBtn()==false)
                        {
                            CommFunc.PrintLog(5, LOGTAG, "get token ok prepare sip register");
                            //testQueryStaus();
                            MyApplication.getInstance().disposeSipRegister();

                        }
                        break;
                    }
                    default:
                    {  
                        CommFunc.PrintLog(5,LOGTAG,"登陆失败:错误未知");
                        break;
                    }
                }

            }

        }
    };

    //hehl 201403
    public void testQueryStaus()
    {
        CommFunc.PrintLog(5, LOGTAG, "testQueryStaus");
         SysConfig.getInstance();
		String tempID = "10-5662,"+RtcConst.UEAPPID_Current+"-"+SysConfig.userid;
//        String userid = RtcRules.userApp2Rtc_new(SysConfig.getInstance().userid, RtcHttpClient.InApplicationID, RtcConst.UEType_Current);
//        String userid2 = RtcRules.userApp2Rtc_new("5662", RtcHttpClient.InApplicationID, RtcConst.UEType_Current);
//        String tempID = userid+","+userid2;
        //        tempID.replace(';',',');
//        HBaseApp.post2WorkRunnable(new Runnable(){
//
//            @Override
//            public void run() {
//                // TODO Auto-generated method stub
//                HttpResult ret = HttpManager.getInstance().QueryUserStatus(HttpManager.getInstance().CreateQueryUserStatus(tempID, 0)); 
//                if(ret!=null&&ret.getObject()!=null)
//                    CommFunc.PrintLog(5, LOGTAG, "testQueryStaus:"+ret.getObject().toString());
//            }
//
//        });
         MyApplication.getInstance().QueryStatus(HttpManager.getInstance().CreateQueryUserStatus(tempID, 0).toString());
    }
    private void OnLoginResult(Intent intent)
    {  
        CommFunc.PrintLog(5, LOGTAG, "OnLoginResult loginbtn:"+SysConfig.getInstance().isLoginByBtn());
        if(SysConfig.getInstance().isLoginByBtn()) //如果登陆页面进入 直接返回
            return;
        int result = intent.getIntExtra("arg1", -1);
        String desc = intent.getStringExtra("arg2");
        CommFunc.PrintLog(5, LOGTAG, "OnLoginResult:"+result+" desc"+desc+" curloginstate:"+SysConfig.getInstance().ismLoginOK());

        if (result == MsgKey.KEY_STATUS_200 || result == MsgKey.KEY_RESULT_SUCCESS) {
            removeLoginAlarm();
            Relogincount = 0;
            if(SysConfig.getInstance().ismLoginOK()==false) //防止注销处理
            {
                SysConfig.getInstance().setmLoginOK(true); 
                CommFunc.PrintLog(5, LOGTAG, "OnLoginResult 登陆成功");
            }
            if(!RtcConst.bNewVersion)
                testQueryStaus();
        }
        else
        {  
            Relogincount++;
            SysConfig.getInstance().setmLoginOK(false);
            CommFunc.PrintLog(5, LOGTAG, "OnLoginResult 登陆失败 错误码[:"+result+"]desc:"+desc);
        }
    }

    //  public static int regFailNum = 0; // 连续重注册次数 连续注册失败3次以上 发起定时重注册
    //  public static boolean bIsreLoginAlarm = false; // 是否在做重注册
    private  AlarmManager reLoginAlarm = null;// 重登陆定时器
    private  PendingIntent reLoginPI = null;
    private int Relogincount = 0;
    // 重登录定时器 time 单位 ms 毫秒 bRepeating 是否重复设置
    private void reLoginAlarm() {
        CommFunc.PrintLog(5, LOGTAG, "reLoginAlarm   reLoginAlarm:"+reLoginAlarm);
        if (reLoginAlarm == null) {
            Intent intent = new Intent(this, ReloginService.class);
            reLoginPI = PendingIntent.getService(MyApplication.getInstance(), 0, intent, 0);
            reLoginAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        }
        CommFunc.PrintLog(5, LOGTAG, "reLoginAlarm() Relogincount:"+Relogincount);

        //前三次每隔20秒启动一次。
        if (Relogincount < 3  ) {
            //miui手机定时器不准
            reLoginAlarm.setRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis()+1000, 8*1000, reLoginPI);
        } else if(Relogincount == 3){
            reLoginAlarm.cancel(reLoginPI);  
            reLoginAlarm
            .setRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(), 3 * 60 * 1000,
                    reLoginPI);

        }
    }

    // 销毁重登陆定时器
    public void removeLoginAlarm() {
        CommFunc.PrintLog(5, LOGTAG, "removeLoginAlarm()");
        Relogincount =0;
        if (reLoginAlarm != null) {
            reLoginAlarm.cancel(reLoginPI);
            CommFunc.PrintLog(5, LOGTAG, "reLoginAlarm()---remove");
        }
        reLoginAlarm = null;
        reLoginPI = null;
    }
}
