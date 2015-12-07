package com.sip.rtcclient.config;

import android.os.Environment;

import com.sip.rtcclient.MyApplication;

public class SysConfig {

    public static  String APP_ID = "70038";//
    public static  String APP_KEY ="MTQxMDkyMzU1NTI4Ng==";//
    public final static int USERTYPE_TIANYI = 0;
    public final static int USERTYPE_WEIBO = 1;
    public final static int USERTYPE_SYSTEM =100;
    public static int login_type=0;//0天翼账号登陆  1 新浪微博登录;
 //   public static int LOG_FILE = 0; // 0打开log日志记录文件 1关闭
 //   public static String homeDir = "rtcClient";
    public static String userid = "8100"; //
    public static boolean bDEBUG = false; //bDEBUG == true debug开关用于menu设置帐号  ；bDEBUG == false否则走实际天翼帐号登陆
    public static int callType; // 临时变量，记录呼叫类型 1:音频 2:视频 3:音频+视频

    //commdef：
    public final static String SHARE_NAME = "rtcpref_info";
    //broadcast define
    public final static String BROADCAST_RELOGIN_SERVICE="com.sip.rtcclient.services.ReloginService";

    //message define
    public static final int MSG_SIP_REGISTER=1001;
    public static final int MSG_TIANYI_VERIFY=1002;
    public static final int MSG_GETTOKEN_ERROR=1003;
    public static final int MSG_GETTOKEN_SUCCESS=1004;
    public static final int MSG_GETSERVERADRR_FAILED=1005;
    public static final int MSG_TIANYI_VERIFY_SUCCESS=1006;
    public static final int MSG_TIANYI_WEBRTC_STATUS=1007;//by cpl
    public static final int MSG_SDKInitOK=1008;//by cpl
    
    //用于全局使用的变量定义 
    private boolean isCalling=false;
    private boolean mLoginOK = false;
    private boolean isLoginByBtn=false; //是否登陆页面登入
    boolean bIncoming = false; //呼叫方向 去电  来电 
    private int mCallType =0; //呼叫类型 音频0 视频1

    public boolean isbIncoming() {
        return bIncoming;
    }

    public void setbIncoming(boolean bIncoming) {
        this.bIncoming = bIncoming;
    }

    public int getCallType() {
        return mCallType;
    }

    public void setCallType(int mCallType) {
        this.mCallType = mCallType;
    }

    public boolean isLoginByBtn() {
        return isLoginByBtn;
    }

    public void setIsLoginByBtn(boolean isLoginByBtn) {
        this.isLoginByBtn = isLoginByBtn;
    }

    public static SysConfig getInstance() {
        return MyApplication.getInstance().getSysConfig();
    }
    
    public boolean isCalling() {
        return isCalling;
    }
    public void setCalling(boolean isCalling) {
        this.isCalling = isCalling;
    }
    public boolean ismLoginOK() {
        return mLoginOK;
    }
    public void setmLoginOK(boolean mLoginOK) {
        this.mLoginOK = mLoginOK;
    }
    //日志路径
    public String getLogFolder()
    {
        return Environment.getExternalStorageDirectory()+"/" + MyApplication.getInstance().getFileFolder() + "/log/";
    }
    /** 头像路径 */
    public String getAvatarFolder() { return Environment.getExternalStorageDirectory()+"/" + MyApplication.getInstance().getFileFolder() + "/avatar/"
            +MyApplication.getInstance().getAppAccountID()+"/"; }
    
    /** 本地录制文件存放路径 */
    public String getRecordFolder() {
        return Environment.getExternalStorageDirectory()+"/" + MyApplication.getInstance().getFileFolder() + "/record/"
                +MyApplication.getInstance().getAppAccountID();
    }
    /** 本地图像文件存放路径 */
    public String getImageFolder() {
        return Environment.getExternalStorageDirectory()+"/" + MyApplication.getInstance().getFileFolder() + "/image/"
                +MyApplication.getInstance().getAppAccountID()+"/";
    }
    public String getCaptureFolder() {
        String path = Environment.getExternalStorageDirectory()+"/" + MyApplication.getInstance().getFileFolder() + "/capture/"
        +MyApplication.getInstance().getAppAccountID()+"/";
        return path;
    }
    public String getAppFolder() {
        String path = Environment.getExternalStorageDirectory()+"/" + MyApplication.getInstance().getFileFolder() + "/app/";
        return path;  
    }
}
