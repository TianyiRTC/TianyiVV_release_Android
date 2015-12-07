package com.sip.rtcclient.config;

public class MsgKey {
    public static final int KEY_RESULT_SUCCESS = 0; //用户登录 成功
    public static final int KEY_RESULT_FAIL = -1; //失败
    public static final int KEY_STATUS_200 = 200; //失败
    
    public static final int SLN_CallClosed = 1001;
    public static final int SLN_CallFailed = 1002;   
    public static final int SLN_180Ring = 1003;
    public static final int SLN_CallAccepted = 1004;
    public static final int SLN_CallVideo = 1005;
    public static final int SLN_NetWorkChange = 1006;
    public static final int SLN_WebRTCStatus = 1007;//by cpl
    public static final int SLN_CallHasAccepted = 1008;
    
    
    public static String KEY_ACODEC="key_acodec";
    public static String KEY_VCODEC="key_vcodec";
    public static String KEY_VFORMAT="key_vformat"; //视频格式
    public static String KEY_VFRAMES="key_vframes";//视频帧数 
    public static String KEY_VOGL="key_vogl";//opengl开关
    public static String KEY_AUTOACP="key_autoacp";
    //prefkey
    public static final String key_version_name = "version_name";
    public static final String key_recycleflag = "recycleflag"; 
    public static final String key_isNormalExit = "isNormalExit";
    public static final String key_tianyi_userid = "tianyi_userid";
    public static final String key_telnumber = "tianyi_telnumber";
    public static final String key_logintype = "0"; //0为天翼帐号登陆 ；1 为新浪微博
    public static final String key_userid = "userid";//天翼账号id
    public static final String key_weibo_userid = "weibo_userid";
    public static final String key_weibo_accesstoken = "weibo_accesstoken";
    public static final String key_getuserlist_time = "getuserlist_time";
    public static final String key_rtc_token = "rtc_token";
    
    
    //start intent key
    public static final String key_servicestartflag = "servicestartflag";
    public static String STARTFLAG_NORMAL = "startflag_normal";
    public static String STARTFLAG_ABNORMAL = "startflag_abnormal";
    
    public static String intent_key_object = "intent_key_object";
    public static String key_msg_appexit = "appexit";
    public static String key_msg_kickoff = "kickoff";
    
    public static int ACODEC_ILBC = 0;
    public static int ACODEC_OPUS = 1;
    
    public static int VCODEC_VP8 = 0;
    public static int VCODEC_H264 = 1;
    
    public static int VIDEO_SD = 0;
    public static int VIDEO_FL = 1;
    public static int VIDEO_HD = 2;
    
    
    public static int broadmsg_sip = 2000;
    public static int grpv_listener_onResponse = 2001;
    public static int grpv_listener_onRequest = 2002;
    public static int grpv_listener_onCreate = 2003;
    
    
    public static String pref_uetype = "pref_uetype";
    public static String pref_addcfg = "pref_addcfg";
    //wwyue0425
    public static String pref_addcfg2 = "pref_addcfg2";

}
