package com.sip.rtcclient.activity.calling;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

import org.json.JSONException;
import org.json.JSONObject;

import rtc.sdk.common.RtcConst;
import rtc.sdk.iface.Connection;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Color;

import com.sip.rtcclient.HBaseApp;
import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.BaseActivity;
import com.sip.rtcclient.bean.TCallRecordInfo;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.config.MsgKey;
import com.sip.rtcclient.config.SysConfig;
import com.sip.rtcclient.db.SQLiteManager;
import com.sip.rtcclient.utils.CommFunc;

public class CallingActivity extends BaseActivity implements OnCheckedChangeListener {

    public final static String BROADCAST_CALLING_ACTION = "com.yishitong.calling";

    private RelativeLayout layout_audio; // 音频整体布局
    private TextView txt_caller; // 主叫、被叫
    private ImageView img_type; // 类别
    private TextView txt_call_status; // 呼叫状态(呼叫中、通话中)
    private LinearLayout layout_called_bottom; // 被叫-呼叫中底部布局
    private RelativeLayout layout_caller_bottom; // 主叫-呼叫中底部布局
    private RelativeLayout layout_calling_layout_audio;//音频通话底部布局

    // 视频通话
    private RelativeLayout layout_video;
    private Chronometer chronometer_audio;
    private Chronometer chronometer_video;
    private RelativeLayout layout_other;
    private RelativeLayout layout_our;

    private SurfaceView mvLocal = null;
    private SurfaceView mvRemote = null;

    private CheckBox switchCamera,callRecord; // 前后摄像头切换,fillwnd 发送黑图,通话录制
    private RelativeLayout layout_mute;
    private RelativeLayout layout_hangup;
    private RelativeLayout layout_speaker;

    private TextView tv_mute;
    private TextView tv_hangup;
    private TextView tv_speaker;

    private boolean isMute = false;
    private boolean isSpeaker = false;

    boolean inCall; // 标识是否为来电
    private boolean isVideo; // 是否视频呼叫、通话
    private String callNumber; // 主叫、被叫电话号码
    private String callRecordId; //通话ID
    private int calltype = RtcConst.CallType_Audio;

    private int count;
    private MediaPlayer mediaPlayer;// 铃音播放
    private Vibrator vibrator;// 震动
    private AudioManager mAudioManager;// 系统声音控制
    private int musicVolume;// 记录当前媒体音量
    private int audiomode;// 记录当前audio mode
    private static int initiative_time = 500;

    private boolean isFail;//标识是否为呼叫失败,如果呼叫失败再HangUp应该将呼叫结果设置为Fail

    private Timer noTimer;
    private boolean notimerFlag = false;
    public PowerManager pm;
    private PowerManager.WakeLock wakeLock;
    KeyguardManager mKeyguardManager;

    KeyguardManager.KeyguardLock mKeyguardLock;
    private String LOGTAG = "CallingActivity";

    private boolean unregistFlag = false;
    private boolean bHangupFlag = false; // 如果主动操作置为true

    private TextView calling_layout_netstatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        musicVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audiomode = mAudioManager.getMode();

        initData();

        initView();

        configCallingView();

        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "My Tag");
        wakeLock.acquire();
        wakeLock.setReferenceCounted(false);
        CommFunc.PrintLog(5, LOGTAG, "wakeLock 保持唤醒");
        registerReceiver(receiver, new IntentFilter(BROADCAST_CALLING_ACTION));

        int bAutoAccept = MyApplication.getInstance().getIntSharedXml(MsgKey.KEY_AUTOACP, 1);

        if (inCall && bAutoAccept == 1) {
        	bAccept = true;
        	MyApplication.getInstance().onCallAccept();
        	initCallingMode();
        }
    }
    /**
     * 屏幕解锁
     */
    // 屏幕加锁解锁参数设置
    long enabletime;
    boolean enabled;
    void disableKeyguard() {
        if (mKeyguardManager == null) {
            mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            mKeyguardLock = mKeyguardManager.newKeyguardLock("Sipdroid");
            enabled = true;
        }
        if (enabled) {
            mKeyguardLock.disableKeyguard();
            enabled = false;
            enabletime = SystemClock.elapsedRealtime();
        }
    }
    /**
     * 判断屏幕是否调用解锁，如果调用过则加锁
     */
    void reenableKeyguard() {
        if (!enabled) {
            try {
                if (Build.VERSION.SDK_INT < 5)
                    Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            mKeyguardLock.reenableKeyguard();
            enabled = true;
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT < 5
                || Build.VERSION.SDK_INT > 7)
            disableKeyguard();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT < 5
                || Build.VERSION.SDK_INT > 7)
            reenableKeyguard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Connection mCall = MyApplication.getInstance().getMCall();
        if(mCall != null)
        {
            CommFunc.PrintLog(5, LOGTAG, "onResume()");
            if(mvLocal != null) {
                layout_our.removeAllViews();
                layout_our.addView(mvLocal);
            }
            mCall.resetVideoViews();
        }
    }

    @Override
    protected void onDestroy() {
        enableSpeaker(false);
        super.onDestroy();
        SysConfig.getInstance().setCalling(false);
        if (unregistFlag == false) {
            unregistFlag = true;
            unregisterReceiver(receiver);
        }
    }

    /**
     * 
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("NewApi")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BROADCAST_CALLING_ACTION)) {
                Bundle bundle = intent.getExtras();
                //              CommFunc.PrintLog(5, LOGTAG, "onReceive");
                if (bundle != null) {
                    Message msg = new Message();
                    msg.what = bundle.getInt("what", 0);
                    msg.arg1 = bundle.getInt("arg1", 0);

                    if(bundle.containsKey("info"))
                        msg.obj = bundle.getString("info");
                    //                    if(bundle.containsKey("info"))
                    //                    msg.obj = bundle.getString(,"");//by cpl

                    handleCallMessage(msg);
                } else {
                    // do something
                }
            }
        }
    };

    /**
     * 
     * @param msg
     */
    private void handleCallMessage(Message msg) {
        Log.e(LOGTAG, "handleCallMessage msg.what: " + msg.what);
        switch (msg.what) {
            case MsgKey.SLN_180Ring:
                if (notimerFlag == false) {
                    notimerFlag = true;
                    noTimer = new Timer(true);
                    playRing(R.raw.ring180, false); // 正在呼叫铃声
                }
                break;
            case MsgKey.SLN_CallAccepted:
                initCallingMode();
                break;
            case MsgKey.SLN_CallVideo:
                initVideoView();
                MyApplication.getInstance().buildVideo(mvRemote);
                setVideoSurfaceVisibility(View.VISIBLE);
                break;
            case MsgKey.SLN_CallClosed:
                playRing(R.raw.ring_bye, false); // 无应答铃声
                updateCallRecordInfo(TCallRecordInfo.CALL_RESULT_SUCCESS);
                closeUI();
                Connection mCall = MyApplication.getInstance().getMCall();
                if(mCall != null)
                {
                    int ret = mCall.callRecordStop();
                    CommFunc.PrintLog(5, LOGTAG, "onCallRecord stop ret ( 0 success):"+ret);
                }
                if (unregistFlag == false) {
                    unregistFlag = true;
                    unregisterReceiver(receiver);
                }
                break;
            case MsgKey.SLN_CallHasAccepted:
            {
                CommFunc.DisplayToast(this, "呼叫已在其他终端处理");
                closeUI();
                if (unregistFlag == false) {
                    unregistFlag = true;
                    unregisterReceiver(receiver);
                }
                break;
            }
            case MsgKey.SLN_CallFailed:
                CommFunc.DisplayToast(CallingActivity.this, RtcConst.getCallStatus(msg.arg1));
                isFail = true;
                updateCallRecordInfo(TCallRecordInfo.CALL_RESULT_FAIL);
                //487 如果被叫未接听前主叫挂断 则被叫自动挂断不做任何处理
                CommFunc.PrintLog(5, LOGTAG, "SLN_CallFailed inCall:"+inCall+"  isCalling:"+SysConfig.getInstance().isCalling()+"arg1:"+msg.arg1);
                if(inCall&& SysConfig.getInstance().isCalling()==false) //inCall && 
                {
                    CommFunc.PrintLog(5, LOGTAG, "未接听前呼叫失败:"+msg.arg1);
                    closeUI();
                    break;
                }
                if(bAccept)
                {
                    CommFunc.PrintLog(5, LOGTAG, "通话中失败:"+msg.arg1);
                    closeUI();
                    break;
                }
                //如果呼叫过程中出现失败不应该提示
                if(SysConfig.getInstance().isCalling()==false)
                {
                    if (msg.arg1 == RtcConst.CallCode_Busy) { // 用户忙486
                        playRing(R.raw.ring486, true);
                    } else if (msg.arg1 == RtcConst.CallCode_Reject) { // 正在通话 603
                        playRing(R.raw.ring486, true);
                    } else if (msg.arg1 >= RtcConst.CallCode_RequestErr 
                            && msg.arg1 < RtcConst.CallCode_ServerErr) { // 480铃声-对方无法接通
                        playRing(R.raw.ring4xx, true);
                    } else if (msg.arg1 >=  RtcConst.CallCode_ServerErr 
                            && msg.arg1 < RtcConst.CallCode_GlobalErr) {
                        playRing(R.raw.ring4xx, true);
                    } else if (msg.arg1 >= RtcConst.CallCode_GlobalErr) {
                        playRing(R.raw.ring4xx, true);
                    }
                }
                CommFunc.PrintLog(5,LOGTAG,"呼叫失败:["+msg.arg1 +"]");
                CommFunc.DisplayToast(CallingActivity.this, "呼叫失败:["+msg.arg1 +"]");
                if (unregistFlag == false) {
                    unregistFlag = true;
                    unregisterReceiver(receiver);
                }
                break;
            case MsgKey.SLN_NetWorkChange:
            {
                OnChangeNetWork(msg);
                break;
            }
            case MsgKey.SLN_WebRTCStatus:	//by cpl
                onNetStatus((String)msg.obj);
                break;
            default:
                break;
        }
    }
    private void OnChangeNetWork(Message msg)
    {
        if (unregistFlag == false) {
            unregistFlag = true;
            unregisterReceiver(receiver);
        }
        //updateCallRecordInfo(TCallRecordInfo.CALL_RESULT_SUCCESS);
        String str = "";
        if(msg.arg1 == RtcConst.NoNetwork)
            str = "NoNetwork";
        else
            str = "ChangeNetwork";
        CommFunc.PrintLog(5, LOGTAG, "MsgKey.SLN_NetWorkChange:"+str);
        CommFunc.DisplayToast(this, "网络已断开");
        closeUI();
    }

    /**
     * 
     */
    private void initVideoView() {
        if (mvLocal != null) {
            return;
        }

        if( MyApplication.getInstance().getMCall()!=null)
        {
            Connection call = MyApplication.getInstance().getMCall();
            mvLocal = (SurfaceView)call.createVideoView(true, this,true);
            mvLocal.setVisibility(View.INVISIBLE);
            layout_our.addView(mvLocal);
            mvLocal.setZOrderMediaOverlay(true);
            mvLocal.setZOrderOnTop(true);

            int use_OpenGL = MyApplication.getInstance().getIntSharedXml(MsgKey.KEY_VOGL, 1);
            CommFunc.PrintLog(5, LOGTAG, "use_OpenGL:"+use_OpenGL);
            mvRemote = (SurfaceView)call.createVideoView(false, this, use_OpenGL==1?true:false);
            mvRemote.setVisibility(View.INVISIBLE);
            layout_other.addView(mvRemote);
        }

    }

    /**
     * 
     * @param visible
     */
    void setVideoSurfaceVisibility(int visible) {
        if(mvLocal == null) {
            return;
        }
        mvLocal.setVisibility(visible);
        mvRemote.setVisibility(visible);
    }

    /**
     * 播放资源铃音
     * 
     * @param resId
     * @param hanguped
     */
    private void playRing(int resId, boolean hanguped) {
        playRing(resId, false, hanguped);
    }

    private void playRing(int resId, boolean bSpeaker, boolean hanguped) {
        stopRing();
        count = 0;
        if ((mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT)) {

        } else if (bSpeaker
                && (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE)) {
            long[] number = { 0, 1000, 1000 };
            vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(number, 1);
        } else {
            mediaPlayer = MediaPlayer.create(CallingActivity.this, resId);
            if (mediaPlayer == null) {
                return;
            }
            if (mAudioManager != null) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 
                    (int) (mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 0.7), 0);
                mAudioManager.setMode(AudioManager.MODE_RINGTONE);
            }
            if (hanguped == true) {
                mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        count++;
                        if (count >= 2) {
                            count = 0;
                            closeUI();
                        } else {
                            mAudioManager.setMode(AudioManager.MODE_RINGTONE);
                            mediaPlayer.start();
                        }
                    }
                });
            } else {
                mediaPlayer.setLooping(true);
            }
            try {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
                mediaPlayer.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mAudioManager.setMode(AudioManager.MODE_RINGTONE);
            mediaPlayer.start();
        }
    }
    //  TimerTask h_exit = new TimerTask() {
    //  @Override
    //  public void run() {
    //      stopRing();
    //      finish();
    //  }
    //};
    //Timer t_exit = new Timer(true);
    //t_exit.schedule(h_exit, initiative_time);
    /**
     * 
     */
    protected void closeUI() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (chronometer_audio != null) {
            chronometer_audio.stop();
        }
        if (chronometer_video != null) {
            chronometer_video.stop();
        }
        HBaseApp.post2UIDelayed(new Runnable(){

            @Override
            public void run() {
                // TODO Auto-generated method stub
                stopRing();
                finish();
            }

        }, initiative_time);
    }

    /**
     * 关闭铃音
     */
    private void stopRing() {
        // 还原媒体播放音量大小
        if (mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, musicVolume, 0);
            mAudioManager.setMode(audiomode);
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
    }

    /**
     * 
     */
    private void initData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            callNumber = bundle.getString("callNumber");
            inCall = bundle.getBoolean("inCall");
            isVideo = bundle.getBoolean("isVideo");
            callRecordId = bundle.getString("callRecordId");
            CommFunc.PrintLog(5, LOGTAG, "initData() callNumber:" + callNumber +   " inCall:"+inCall+"  isVideo:"+isVideo);
            if(isVideo)
                calltype = RtcConst.CallType_A_V;
            else
                calltype = RtcConst.CallType_Audio;
            //收到来电时在此保存
            if(inCall)
                saveCallRecordInfo(callRecordId,callNumber,calltype);
        }
    }
    private void saveCallRecordInfo(String callRecordId, String callNumber, int callType) {
        CommFunc.PrintLog(5, LOGTAG, "saveCallRecordInfo:"+callNumber+"  callRecordId:"+callRecordId);
        TCallRecordInfo info = new TCallRecordInfo();
        info.setCallRecordId(callRecordId);
        info.setDate(CommFunc.getStartDate());
        info.setStartTime(CommFunc.getStartTime());
        info.setEndTime("");
        info.setTotalTime("");
        info.setFromUser(MyApplication.getInstance().getAccountInfo().getUserid()); // TODO 需要更改
        info.setToUser(callNumber);
        if(inCall == false)
        {
            info.setDirection(TCallRecordInfo.CALL_DIRECTION_IN);
        }
        else
        {
            info.setDirection(TCallRecordInfo.CALL_DIRECTION_OUT);
        }

        info.setType(callType);
        info.setResult(TCallRecordInfo.CALL_RESULT_SUCCESS);
        SQLiteManager.getInstance().saveCallRecordInfo(info, true);
    }
    /**
     * 
     */
    private void initView() {
        layout_audio = (RelativeLayout) findViewById(R.id.calling_layout_audio);
        txt_caller = (TextView) findViewById(R.id.calling_txt_caller);
        img_type = (ImageView) findViewById(R.id.calling_img_type);
        txt_call_status = (TextView) findViewById(R.id.calling_txt_status);
        layout_called_bottom = (LinearLayout) findViewById(R.id.called_layout_bottom);
        layout_caller_bottom = (RelativeLayout) findViewById(R.id.caller_layout_bottom);

        calling_layout_netstatus = (TextView) findViewById(R.id.calling_layout_netstatus);
        calling_layout_netstatus.setVisibility(View.GONE);
        layout_calling_layout_audio = (RelativeLayout) findViewById(R.id.calling_layout_audio_bottom);

        switchCamera = (CheckBox) findViewById(R.id.cb_calling_video_switch);
        callRecord = (CheckBox)findViewById(R.id.btn_call_record);

        chronometer_audio = (Chronometer) findViewById(R.id.chronometer_audio);

        layout_video = (RelativeLayout) findViewById(R.id.calling_layout_video);
        chronometer_video = (Chronometer) findViewById(R.id.chronometer_video);
        layout_other = (RelativeLayout) findViewById(R.id.calling_layout_video_other);
        layout_our = (RelativeLayout) findViewById(R.id.calling_layout_video_our);

        layout_mute = (RelativeLayout) findViewById(R.id.layout_calling_img_audio_mute);
        layout_hangup = (RelativeLayout) findViewById(R.id.layout_calling_img_audio_hangup);
        layout_speaker = (RelativeLayout) findViewById(R.id.layout_calling_img_audio_speaker);

        tv_mute = (TextView) findViewById(R.id.calling_tv_audio_mute);
        tv_hangup = (TextView) findViewById(R.id.calling_tv_audio_hangup);
        tv_speaker = (TextView) findViewById(R.id.calling_tv_audio_speaker);

        switchCamera.setOnCheckedChangeListener(this);
        callRecord.setOnCheckedChangeListener(this);

    }

    /**
     * TODO 需要更改 layout_caller_bottom visible作为主叫 inCall来电作被叫
     */
    private void configCallingView() {

        TContactInfo info = SQLiteManager.getInstance().getContactInfoByNumber(callNumber);
        if(info==null) // || ( info!=null && info.getName().equals(callNumber))
            txt_caller.setText(callNumber);
        else if(info!=null&& (info.getName().equals(callNumber)))
        {
            txt_caller.setText(callNumber);
        }
        else
            txt_caller.setText(info.getName()+"["+callNumber+"]");

        if (isVideo) {
            img_type.setBackgroundResource(R.drawable.call_type_video);
        }else {
            img_type.setBackgroundResource(R.drawable.call_type_audio);
        }

        if (inCall) {
            layout_called_bottom.setVisibility(View.VISIBLE);
            layout_caller_bottom.setVisibility(View.GONE);
            initIncallMode();
        } else {
            layout_called_bottom.setVisibility(View.GONE);
            layout_caller_bottom.setVisibility(View.VISIBLE);
            if (isVideo) {
                MyApplication.getInstance().MakeCall(callNumber, RtcConst.CallType_A_V);
            }else {
                MyApplication.getInstance().MakeCall(callNumber, RtcConst.CallType_Audio);
            }
        }
    }

    /**
     * 初始化来电 TODO 需要更改
     */
    private void initIncallMode() {
        // initCallIn();
        if ((mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT)) {
            // do something
        } else if ((mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE)) {
            long[] number = { 0, 1000, 1000 };
            vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(number, 1);
        } else {
            stopRing();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();
            mAudioManager.setMode(AudioManager.MODE_RINGTONE);
            try {
                mediaPlayer
                .setDataSource(CallingActivity.this, Uri
                        .parse(Settings.System.DEFAULT_RINGTONE_URI
                                .toString()));
                // 设置当铃音循环播放时，设置两次播放间隔为1.5秒
                mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mAudioManager.setMode(AudioManager.MODE_RINGTONE);
                        mediaPlayer.start();
                    }
                });
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                mediaPlayer.prepare();
                mAudioManager.setMode(AudioManager.MODE_RINGTONE);
                mediaPlayer.start();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (notimerFlag == false) {
            notimerFlag = true;
            noTimer = new Timer(true);
         }
    }

    private void enableSpeaker(boolean bSpeaker) {
        CommFunc.PrintLog(5, LOGTAG, "enableSpeaker bSpeaker:"+bSpeaker);
        MyApplication.getInstance().getRtcClient().enableSpeaker(mAudioManager, bSpeaker);
        //mAudioManager.setSpeakerphoneOn(bSpeaker);
    }

    /**
     * 通话中状态 TODO 需要更改
     */
    private boolean bAccept = false;
    private void initCallingMode() {
        bAccept = true;
        if (noTimer != null) {
            noTimer.cancel();
            noTimer = null;
        }
        stopRing(); // 停止铃声

        layout_calling_layout_audio.setVisibility(View.VISIBLE);
        if (!isVideo) {
            layout_video.setVisibility(View.GONE);
            layout_audio.setVisibility(View.VISIBLE);
            txt_call_status.setText(getString(R.string._calling));
            layout_called_bottom.setVisibility(View.GONE);
            layout_caller_bottom.setVisibility(View.GONE);
            chronometer_audio.setVisibility(View.VISIBLE);
            chronometer_audio.setBase(SystemClock.elapsedRealtime());
            chronometer_audio.setFormat("%s");
            chronometer_audio
            .setOnChronometerTickListener(new OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer ch) {
                }
            });
            chronometer_audio.start();
        } else {
            layout_audio.setVisibility(View.GONE);
            layout_called_bottom.setVisibility(View.GONE);
            layout_caller_bottom.setVisibility(View.GONE);
            layout_video.setVisibility(View.VISIBLE);
            chronometer_video.setBase(SystemClock.elapsedRealtime());
            chronometer_video.setFormat("%s");
            chronometer_video
            .setOnChronometerTickListener(new OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer ch) {
                    // 如果从开始计时到现在超过了20s。
                    //							callDuration = SystemClock.elapsedRealtime()
                    //									- ch.getBase();
                }
            });
            chronometer_video.start();

        }

    }

    int getInCallStream() {
        /* Archos 5IT */
        if ((android.os.Build.BRAND.equalsIgnoreCase("archos")
		 && android.os.Build.DEVICE.equalsIgnoreCase("g7a")) 
		 || (android.os.Build.BRAND.equalsIgnoreCase("Huawei")
		 && android.os.Build.DEVICE.equalsIgnoreCase("hwC8813Q"))
		 || (android.os.Build.BRAND.equalsIgnoreCase("Huawei")
		 && android.os.Build.DEVICE.equalsIgnoreCase("hwc8813"))		 
		 || (android.os.Build.BRAND.equalsIgnoreCase("Xiaomi")
		 && android.os.Build.MODEL.contains("MI 3"))
		 || (android.os.Build.BRAND.equalsIgnoreCase("Xiaomi")
		 && android.os.Build.MODEL.contains("MI 2"))
		 || (android.os.Build.BRAND.equalsIgnoreCase("samsung")
		 && android.os.Build.DEVICE.equalsIgnoreCase("klte"))
		 || (android.os.Build.BRAND.equalsIgnoreCase("samsung")
		 && android.os.Build.DEVICE.equalsIgnoreCase("klteduosctc"))
		 || (android.os.Build.BRAND.equalsIgnoreCase("samsung")
		 && android.os.Build.DEVICE.equalsIgnoreCase("hllte"))
		 || (android.os.Build.BRAND.equalsIgnoreCase("samsung")
		 && android.os.Build.DEVICE.equalsIgnoreCase("trltechn"))	
		 || (android.os.Build.BRAND.equalsIgnoreCase("Huawei")
		 && android.os.Build.DEVICE.equalsIgnoreCase("hwB199"))
        || (android.os.Build.BRAND.equalsIgnoreCase("TCL")
		 && android.os.Build.DEVICE.equalsIgnoreCase("Diablo_LTE"))		 
		 ||(android.os.Build.BRAND.equalsIgnoreCase("Xiaomi")
		 && android.os.Build.MODEL.contains("MI 4"))) {
               // Since device has no voice call capabilities, voice call stream is
               // not implemented
               // So we have to choose the good stream tag, which is by default
               // falled back to music
               return AudioManager.STREAM_MUSIC;
        }
        // return AudioManager.STREAM_MUSIC;
       return AudioManager.STREAM_VOICE_CALL;
   }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int mode;
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mode = getInCallStream();
        Log.e(LOGTAG, "Current audio mode sssss : " + mode);
        int currentVolume = mAudioManager.getStreamVolume(mode);


        switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:// 音量增大
                        mAudioManager.setStreamVolume(mode, currentVolume+1, 1);
                        Log.e(LOGTAG, "Current audio volume up sssss");
                break;
                case KeyEvent.KEYCODE_VOLUME_DOWN:// 音量减小
                        mAudioManager.setStreamVolume(mode, currentVolume-1, 1);
                        Log.e(LOGTAG, "Current audio volume up sssss");
                break;


                case KeyEvent.KEYCODE_BACK:// 返回键
                return true;


                default:
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_calling_video_switch:
                onSwitchCamera(isChecked);
                break;
            case R.id.btn_call_record:
                onCallRecord(isChecked);
                break;
        }
    }
    /**
     * 通话录制
     * 
     * @param isChecked 开始/停止
     */
    public void onCallRecord(boolean isChecked)
    {
    	int ret;
    	if(isChecked) {
            String folder = SysConfig.getInstance().getRecordFolder();
            File path = new File(folder);
            // 文件 
            if (!path.exists()) {
                path.mkdirs();
            }
            CommFunc.PrintLog(5, LOGTAG, "onCallRecord path:"+path);
            CommFunc.DisplayToast(this, "Start CallRecord:"+path);
            ret = MyApplication.getInstance().getMCall().callRecordStart(folder); //0 成功 1失败
            CommFunc.PrintLog(5, LOGTAG, "onCallRecord start ret ( 0 success):"+ret);
        }
    	else {
    		CommFunc.DisplayToast(this, "Stop CallRecord");
            ret = MyApplication.getInstance().getMCall().callRecordStop();
            CommFunc.PrintLog(5, LOGTAG, "onCallRecord stop ret ( 0 success):"+ret);
    	}
    }

    public void onFillWnd(boolean isChecked)
    {
        CommFunc.PrintLog(5, LOGTAG, "onFillWnd nFill:"+isChecked);
        MyApplication.getInstance().getMCall().fillSend((isChecked==true)?1:0);
    }
    /**
     * 前后置摄像头切换
     * TODO 需要更改
     * 
     * @param isChecked
     */
    private void onSwitchCamera(boolean isChecked) {
        CommFunc.PrintLog(5, LOGTAG, "onSwitchCamera:"+isChecked);
        if( MyApplication.getInstance().getMCall()!=null)
            MyApplication.getInstance().getMCall().setCamera(isChecked ? 0 : 1);
    }
    // private final String sdcardPath_capture = Environment.getExternalStorageDirectory()+"/";
    public void onCaptureWnd(View view)
    {
        String path = getCaptureFilePath();
        CommFunc.PrintLog(5, LOGTAG, "onCaptureWnd path:"+path);
        CommFunc.DisplayToast(this, "onCaptureWnd:"+path);
        int ret =  MyApplication.getInstance().getMCall().takeRemotePicture(path); //0 成功 1失败
        CommFunc.PrintLog(5, LOGTAG, "onCaptureWnd path ret ( 0 success):"+ret);
    }

    private String getCaptureFilePath()
    {
        String folder = SysConfig.getInstance().getCaptureFolder();
        File path = new File(folder);
        // 文件 
        if (!path.exists()) {
            path.mkdirs();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = format.format(new Date());
        String filepath = folder+"capture_"+date+".jpg";
        File file = new File(filepath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return filepath;
    }

    /** 

     * 获取和保存当前屏幕的截图 

     */  
    //
    //    private void GetandSaveCurrentImage() {  
    //        // 1.构建Bitmap  
    //        WindowManager windowManager = getWindowManager();  
    //        Display display = windowManager.getDefaultDisplay();  
    //        int w = display.getWidth();  
    //        int h = display.getHeight();  
    //        Bitmap Bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);  
    //        // 2.获取屏幕  
    //        View decorview = this.getWindow().getDecorView();  
    //        decorview.setDrawingCacheEnabled(true);  
    //        Bmp = decorview.getDrawingCache();  
    //        try {  
    //
    //            FileOutputStream fos = null;  
    //            fos = new FileOutputStream(getCaptureFilePath());  
    //            if (null != fos) {  
    //
    //                Bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);  
    //                fos.flush();  
    //                fos.close();  
    //
    //            }  
    //        } catch (Exception e) {  
    //
    //            e.printStackTrace();  
    //
    //        }  
    //
    //    }  

    /**
     * 接受呼叫
     * 
     * @param view
     */
    public void onAccept(View view) {
        bAccept = true;
        MyApplication.getInstance().onCallAccept();
        initCallingMode();
    }

    /**
     * 拒绝呼叫
     * 
     * @param view
     */
    public void onRefused(View view) {
        MyApplication.getInstance().onCallHangup();
        updateCallRecordInfo(TCallRecordInfo.CALL_RESULT_FAIL);
        closeUI();
    }


    /**
     * 结束通话
     * 
     * @param view
     */
    public void onHangUp(View view) {
        layout_hangup.setBackgroundColor(getResources().getColor(R.color.lightgray));
        tv_hangup.setTextColor(getResources().getColor(R.color.tx_hangup_checked));
        MyApplication.getInstance().onCallHangup();
        int result = 0;
        if (isFail) {
            result = TCallRecordInfo.CALL_RESULT_FAIL;
        }else {
            result = TCallRecordInfo.CALL_RESULT_SUCCESS;
            Connection mCall = MyApplication.getInstance().getMCall();
            if(mCall != null)
            {
                int ret = mCall.callRecordStop();
                CommFunc.PrintLog(5, LOGTAG, "onCallRecord stop ret ( 0 success):"+ret);
            }
        }
        updateCallRecordInfo(result);
        closeUI();
    }
    /**
     * 静音
     * mute true 静音 mute false 非静音
     * @param view
     */
    public void onMute(View view) {
        if (isMute) {
            layout_mute.setBackgroundColor(getResources().getColor(R.color.calling_bottom_bg));
            tv_mute.setTextColor(getResources().getColor(R.color.calling_mute));
        }else {
            layout_mute.setBackgroundColor(getResources().getColor(R.color.lightgray));
            tv_mute.setTextColor(getResources().getColor(R.color.tx_mute_checked));
        }
        isMute = !isMute;
        CommFunc.PrintLog(5, LOGTAG, "onMute:"+isMute);
        if( MyApplication.getInstance().getMCall()!=null)
            MyApplication.getInstance().getMCall().setMuted(isMute);
    }

    /**
     * 扬声器
     * 
     * @param view
     */
    public void onSpeaker(View view) {
        if (isSpeaker) {
            layout_speaker.setBackgroundColor(getResources().getColor(R.color.calling_bottom_bg));
            tv_speaker.setTextColor(getResources().getColor(R.color.calling_speaker));
        }else {
            layout_speaker.setBackgroundColor(getResources().getColor(R.color.lightgray));
            tv_speaker.setTextColor(getResources().getColor(R.color.tx_speaker_checked));
        }
        isSpeaker = !isSpeaker;
        CommFunc.PrintLog(5, LOGTAG, "enableSpeaker:"+isSpeaker);
        enableSpeaker(isSpeaker);
    }

    /**
     * 更新通话记录
     * TODO 需要更改
     */
    private void updateCallRecordInfo(int result) {
        TCallRecordInfo info = SQLiteManager.getInstance().getCallRecordInfoById(callRecordId);
        info.setResult(result);
        info.setEndTime(CommFunc.getEndTime());
        if (result == TCallRecordInfo.CALL_RESULT_FAIL) {
            info.setTotalTime("00:00");
        } else {
            //TODO 暂时禁用(备用)
            //			String startTime = new StringBuffer().append(info.getDate()).append(" ").append(info.getStartTime()).toString();
            //			String endTime = new StringBuffer().append(info.getDate()).append(" ").append(info.getEndTime()).toString();
            //			long totalTime = CommFunc.getTotalTime(startTime, endTime);
            //			double time = totalTime / 60 /60;
            //			Log.e(LOGTAG, "startTime: " + startTime);
            //			Log.e(LOGTAG, "endTime: " + endTime);
            //			Log.e(LOGTAG, "totalTime: " + totalTime);
            //			Log.e(LOGTAG, "time: " + time);
            //			info.setTotalTime(String.valueOf(time));
            if (chronometer_audio.getVisibility() == View.VISIBLE) {
                info.setTotalTime(chronometer_audio.getText().toString());
            }else {
                info.setTotalTime(chronometer_video.getText().toString());
            }
        }
        SQLiteManager.getInstance().updateCallRecordInfo(callRecordId, info, true);
    }

    //by cpl
    private VideoInfo videoInfo = new VideoInfo();
    private final int SB_LEVEL_1 = 99360;
	private final int SB_LEVEL_2 = 40360;
	private final int RTT_LEVEL_1 = 500;
	private final int RTT_LEVEL_2 = 1000;
    private long lastTime ;
    //显示网络不良提示信息
    private void onNetStatus(String info){
        if(info==null || TextUtils.isEmpty(info))return;
        try {
            JSONObject json = new JSONObject(info);
            if(json!=null && json.has("msg")){
                videoInfo.msg = json.getString("msg");
                videoInfo.codec = json.getInt("codec");
                videoInfo.w = json.getInt("w");
                videoInfo.h = json.getInt("h");
                videoInfo.rf = json.getInt("rf");
                videoInfo.rb = json.getInt("rb");
                videoInfo.rtt = json.getInt("rtt");
                videoInfo.sf= json.getInt("sf");
                videoInfo.sb= json.getInt("sb");			
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /*if(videoInfo.sb > SB_LEVEL)
            return;
        if(System.currentTimeMillis() - lastTime < 10000)
            return;
        lastTime = System.currentTimeMillis();*/
		
		if(videoInfo.sb == 0 || videoInfo.rb == 0 || videoInfo.rtt == 0)
			return;
        //显示5秒
		CommFunc.PrintLog(5, LOGTAG, "onNetStatus: send_bitrate="+videoInfo.sb/1000+"kbps, rtt:"+videoInfo.rtt+"ms");
		
		if(videoInfo.sb>SB_LEVEL_1 && videoInfo.rtt<RTT_LEVEL_1 && videoInfo.rb>SB_LEVEL_1){
			calling_layout_netstatus.setVisibility(View.VISIBLE);
			calling_layout_netstatus.setText("发送速率:"+videoInfo.sb/1000+"kbps, 接收速率:"+videoInfo.rb/1000+"kbps\n rtt:"
											 +videoInfo.rtt+"ms");
			calling_layout_netstatus.setTextColor(Color.GREEN);
		}
		else if (videoInfo.sb>SB_LEVEL_2 && videoInfo.rtt<RTT_LEVEL_2 && videoInfo.rb>SB_LEVEL_2){
			calling_layout_netstatus.setVisibility(View.VISIBLE);
			calling_layout_netstatus.setText("网络不稳定\n 发送速率:"+videoInfo.sb/1000+"kbps, 接收速率:"+videoInfo.rb/1000+"kbps\n rtt:"
											 +videoInfo.rtt+"ms");
			calling_layout_netstatus.setTextColor(Color.YELLOW);
			HBaseApp.post2UIDelayed(new Runnable(){

            @Override
            public void run() {
              
            }

        }, 5000);
		}
		else{
			calling_layout_netstatus.setVisibility(View.VISIBLE);
			if(videoInfo.rb<SB_LEVEL_2 && videoInfo.sb>SB_LEVEL_2){
				calling_layout_netstatus.setText("对方网络很差，无法保证正常视频\n 发送速率:"+videoInfo.sb/1000+"kbps, 接收速率:"+videoInfo.rb/1000+"kbps\n rtt:"
											 +videoInfo.rtt+"ms");
			calling_layout_netstatus.setTextColor(Color.RED);
			}
			else{
			calling_layout_netstatus.setText("网络很差，无法保证正常视频\n 发送速率:"+videoInfo.sb/1000+"kbps, 接收速率:"+videoInfo.rb/1000+"kbps\n rtt:"
											 +videoInfo.rtt+"ms");
			calling_layout_netstatus.setTextColor(Color.RED);
			}
			HBaseApp.post2UIDelayed(new Runnable(){

            @Override
            public void run() {
        
            }

        }, 5000);
		}       
    }

    //by cpl. 
    class VideoInfo{
        //{ "msg": 102, "codec": 1211250228, "w": 352, "h": 288, "rf": 4, "rb": 406810, "lost": 0, "sf": 9, "sb": 69375 }
        public String msg;
        public int codec;
        public int w;
        public int h;
        public int rf;
        public int rb;
        public int rtt;
        public int sf;
        public int sb;
    }
}
