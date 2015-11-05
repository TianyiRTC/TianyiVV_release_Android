package com.sip.rtcclient.activity.calling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import jni.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rtc.sdk.common.RtcConst;
import rtc.sdk.iface.GroupMgr;
import rtc.sdk.iface.Connection;
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

import com.sip.rtcclient.HBaseApp;
import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.BaseActivity;
import com.sip.rtcclient.activity.group.SelectContactActivity;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.bean.TGroupInfo;
import com.sip.rtcclient.bean.TGroupRecordInfo;
import com.sip.rtcclient.config.MsgKey;
import com.sip.rtcclient.db.SQLiteManager;
import com.sip.rtcclient.ui.Dialog_model;
import com.sip.rtcclient.ui.GroupNavigationView;
import com.sip.rtcclient.ui.GroupNavigationView.NavigationListener;
import com.sip.rtcclient.ui.RoundAngleImageView;
import com.sip.rtcclient.utils.CommFunc;
import com.sip.rtcclient.utils.ScreenUtil;
import com.sip.rtcclient.ui.Dialog_model.OnDialogClickListener;
/**
 * <p>
 * desc:现场直播Activity TODO 需要修改画面为本地摄像头画面
 * 
 * @data 2013-9-1
 * @time 下午10:19:25
 */

public class CallingTVActivity extends BaseActivity implements
NavigationListener, OnCheckedChangeListener{

    private RelativeLayout layout_call_center;
    private ImageView img_head; // 现场直播头像
    private TextView tv_call_name; // 主叫、被叫显示的呼叫名称
    private ImageView img_call_status; // 呼叫状态
    private TextView tv_call_status; // 呼叫状态
    private LinearLayout layout_bottom_called; // 呼叫中被叫底部布局
    private RelativeLayout layout_bottom_caller; // 呼叫中主叫底部布局
    private RelativeLayout layout_bottom_calling; // 通话中底部布局
    private LinearLayout layout_calling_caller; // 通话中主叫布局
    private RoundAngleImageView img_group; // 群组头像
    private TextView tv_group_name; // 群组名称
    private TextView tv_group_members_count; // 群组成员人数
    private GroupNavigationView navigationView; // 群组成员布局
    private Chronometer chronometer; // 通话时间
    private ImageView img_status; // 正在通话与结束通话图标
    private TextView tv_status; // 正在通话与结束通话文字
    private CheckBox cb_camera_caller; // 主叫摄像头控制CheckBox
    private Chronometer chronometer_called; // 被叫显示的通话时间
    private ImageView img_called_status; // 被叫显示的通话状态图标
    private TextView tv_called_status; // 被叫显示的通话状态文字
    private CheckBox cb_camera_called; // 被叫摄像头控制CheckBox
    private TextView tv_called_name; // 被叫端显示的Name
    private ImageView img_members_add; // 增加人员

    private LinearLayout layout_called; // 被叫主画面

    /* 主叫 */
    private SurfaceView callerLocal; 
    private SurfaceView callerRemote; 
    private LinearLayout otherSideSF_caller;// 对端视频
    private LinearLayout ourSideSF_caller;// 自己视频

    /* 被叫 */
    private SurfaceView calledLocal; 
    private SurfaceView calledRemote;
    private LinearLayout otherSideSF_called;// 对端视频
    private LinearLayout ourSideSF_called;// 自己视频

    private RelativeLayout layout_mute;
    private RelativeLayout layout_hangup;
    private RelativeLayout layout_exit;
    private RelativeLayout layout_speaker;

    private TextView tv_mute;
    private TextView tv_hangup;
    private TextView tv_exit;
    private TextView tv_speaker;

    private boolean isMute = false;
    private boolean isSpeaker = false;


    private boolean inCall; // 标识是否为主叫 ture为被叫(来点) false为主叫
    private TGroupInfo groupInfo;
    private List<TContactInfo> list;

    private AudioManager mAudioManager;// 系统声音控制
    private int musicVolume;// 记录当前媒体音量
    private int audiomode;// 记录当前audio mode
    private Vibrator vibrator;// 震动
    private MediaPlayer mediaPlayer;// 铃音播放
    private Timer noTimer;
    private boolean notimerFlag = false;
    private static int initiative_time = 500;

    public PowerManager pm;
    private PowerManager.WakeLock wakeLock;
    KeyguardManager mKeyguardManager;
    KeyguardManager.KeyguardLock mKeyguardLock;

    /* 记录群组通话记录的部分信息 */
    private String startDate;
    private String startTime;
    private int duration;
    private TGroupRecordInfo info;
    // 如果非创建者被邀请加入时需要使用以下数据
    private String m_gvcname; // 群组名称
    private String m_creator; // 创建者名称
    public static final int ADD_MENBER = 1;// 添加成员
    private int joinResult;
    private Dialog_model dialog; //放弃发言权对话框
    
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommFunc.PrintLog(1, LOGTAG, "onCreate");
        setContentView(R.layout.activity_calling_tv);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        musicVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audiomode = mAudioManager.getMode();

        getExtrasData();
        initView();
        initData();
        configView();
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "My Tag");
        wakeLock.acquire();
        wakeLock.setReferenceCounted(false);
        CommFunc.PrintLog(5, LOGTAG, "wakeLock 保持唤醒");
        registerReceiver(receiver, new IntentFilter(
                CallingActivity.BROADCAST_CALLING_ACTION));
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

    /**
     * 获取参数
     */
    private void getExtrasData() {
        Bundle bundle = getIntent().getExtras();
        CommFunc.PrintLog(1, LOGTAG, "getExtrasData bundle:" + bundle);
        if (bundle != null) {
            inCall = bundle.getBoolean("inCall");
            if(inCall==false) //创建时
            {
                duration = TGroupRecordInfo.CONF_CALL;
                groupInfo = (TGroupInfo) bundle.getSerializable("groupInfo");
                m_creator = MyApplication.getInstance().getUserID();
                m_gvcname = groupInfo.getGroupName();
                CommFunc.PrintLog(5, LOGTAG, "getExtrasData inCall==false m_creator:"+m_creator+" gvcname:"+m_gvcname);
            }
            else
            {
                duration = TGroupRecordInfo.CONF_RECEIVE;
                m_gvcname = bundle.getString(RtcConst.kgvcname);
                m_creator = bundle.getString(RtcConst.kgvccreator);

                CommFunc.PrintLog(1, LOGTAG, "getExtrasData gvcname:"+m_gvcname +" creator:"+m_creator+" loginuserid:"+MyApplication.getInstance().getUserID());
                TContactInfo info = SQLiteManager.getInstance().getContactInfoByNumber(MyApplication.getInstance().getUserID());
                if(info!=null)
                {
                    CommFunc.PrintLog(5, LOGTAG, "getExtrasData saveGroupInfo: num:"+info.getPhoneNum()+" name:"+info.getName());
                }
                TGroupInfo grpInfo = new TGroupInfo();
                grpInfo.setGroupId(String.valueOf(System.currentTimeMillis()));
                grpInfo.setGroupName(m_gvcname);
                grpInfo.setGroupMembers(info.getPhoneNum());
                grpInfo.setGroupPhoto(null);
                grpInfo.setGroupCreator(m_creator);
                grpInfo.setGroupCreateTime(String.valueOf(System.currentTimeMillis()));
                grpInfo.setGroupType(TGroupInfo.GROUP_TYPE_JOIN);

                boolean isExist = SQLiteManager.getInstance().checkGroupExistByName(grpInfo.getGroupName());
                if (isExist) {// 如果存在相同的两个群组，删除原群组
                    TGroupInfo oldInfo = SQLiteManager.getInstance().getGroupByGroupName(grpInfo.getGroupName());
                    grpInfo.setGroupId(oldInfo.getGroupId());
                    SQLiteManager.getInstance().updateGroup(grpInfo, true);
                }else {
                    SQLiteManager.getInstance().saveGroupInfo(grpInfo, true);
                }

                groupInfo = grpInfo;
            }
        } else {
            CommFunc.PrintLog(5, LOGTAG, "getExtrasData():closeUI");
            closeUI();
        }
    }

    /**
     * 初始化界面
     */
    private void initView() {
        layout_call_center = (RelativeLayout) findViewById(R.id.calling_layout_audio);
        img_head = (ImageView) findViewById(R.id.calling_img_head);
        tv_call_name = (TextView) findViewById(R.id.calling_txt_caller);
        img_call_status = (ImageView) findViewById(R.id.calling_img_type);
        tv_call_status = (TextView) findViewById(R.id.calling_txt_status);
        layout_bottom_called = (LinearLayout) findViewById(R.id.called_layout_bottom);
        layout_bottom_caller = (RelativeLayout) findViewById(R.id.caller_layout_bottom);
        layout_bottom_calling = (RelativeLayout) findViewById(R.id.calling_layout_video_bottom);
        layout_calling_caller = (LinearLayout) findViewById(R.id.calling_caller_layout);
        img_group = (RoundAngleImageView) findViewById(R.id.calling_group_img);
        tv_group_name = (TextView) findViewById(R.id.calling_group_name);
        tv_group_members_count = (TextView) findViewById(R.id.calling_group_members_count);
        navigationView = (GroupNavigationView) findViewById(R.id.navigation_view);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        img_status = (ImageView) findViewById(R.id.calling_show_img);
        tv_status = (TextView) findViewById(R.id.calling_show_status);
        cb_camera_caller = (CheckBox) findViewById(R.id.cb_calling_camera);

        ourSideSF_caller = (LinearLayout) findViewById(R.id.surfaceview_our);
        otherSideSF_caller = (LinearLayout) findViewById(R.id.surfaceview_other);
        ourSideSF_called = (LinearLayout) findViewById(R.id.calling_tv_sf_called_our);
        otherSideSF_called = (LinearLayout) findViewById(R.id.calling_tv_sf_called_other);

        layout_called = (LinearLayout) findViewById(R.id.calling_called_layout);
        chronometer_called = (Chronometer) findViewById(R.id.chronometer_called);
        img_called_status = (ImageView) findViewById(R.id.calling_called_show_img);
        tv_called_status = (TextView) findViewById(R.id.calling_called_show_status);
        cb_camera_called = (CheckBox) findViewById(R.id.cb_calling_called_camera);
        tv_called_name = (TextView) findViewById(R.id.calling_called_name);

        layout_mute = (RelativeLayout) findViewById(R.id.layout_calling_img_audio_mute);
        layout_hangup = (RelativeLayout) findViewById(R.id.layout_calling_img_audio_hangup);
        layout_exit = (RelativeLayout) findViewById(R.id.calling_layout_exit);
        layout_speaker = (RelativeLayout) findViewById(R.id.layout_calling_img_audio_speaker);

        tv_mute = (TextView) findViewById(R.id.calling_tv_audio_mute);
        tv_hangup = (TextView) findViewById(R.id.calling_tv_audio_hangup);
        tv_exit = (TextView) findViewById(R.id.calling_tv_audio_exit);
        tv_speaker = (TextView) findViewById(R.id.calling_tv_audio_speaker);

        img_members_add = (ImageView) findViewById(R.id.conf_members_add_img);

        cb_camera_caller.setOnCheckedChangeListener(this);
        cb_camera_called.setOnCheckedChangeListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        list = new ArrayList<TContactInfo>();
        if (groupInfo != null) {
            if (groupInfo.getGroupPhoto() != null) {
                // TODO 设置头像
            }
            tv_call_name.setText(groupInfo.getGroupName());
        }
        info = new TGroupRecordInfo();
        startDate = CommFunc.getStartDate();
        startTime = CommFunc.getStartTime();
    }
    private String getMembers()
    {
        if(groupInfo==null)
            return null;
        String[] membersid = groupInfo.getGroupMembers().split(";");
        String members = new String();
        for(int i=0;i<membersid.length;i++){
            TContactInfo info = SQLiteManager.getInstance().getContactInfoById(membersid[i]);
            if(info == null)
                return null;
            CommFunc.PrintLog(5, LOGTAG, "getContactInfo:"+info.getName());
            if(i>0)
                members+=",";
            if(info!=null)
                members+=info.getPhoneNum();
        }
        return members;
    }

    /**
     * 配置主叫、被叫View
     */
    private void configView() {
        if (inCall) {
            layout_bottom_called.setVisibility(View.VISIBLE);
            tv_call_status.setText(getString(R.string.vv_show_call_invitation));
            initIncallMode();
        } else {
            layout_bottom_caller.setVisibility(View.VISIBLE);
            HBaseApp.post2WorkRunnable(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObj = new JSONObject();
                    try {
                        jsonObj.put(RtcConst.kgvctype, RtcConst.grouptype_microliveAV); //yes
                        jsonObj.put(RtcConst.kgvcname, groupInfo.getGroupName()); //yes
                        jsonObj.put(RtcConst.kgvcinvitedList, getMembers());
                        Utils.PrintLog(5, LOGTAG, "createGroupCallJson:"+jsonObj.toString());

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    MyApplication.getInstance().CreateConf(jsonObj.toString());
                }
            });
        }
    }

    /**
     * 初始化来电
     */
    private void initIncallMode() {
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
                .setDataSource(this, Uri
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
            noTimer.schedule(timerTask, 1 * 60 * 1000); // 1分钟
        }
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
     * @param bSpeaker
     */
    private void enableSpeaker(boolean bSpeaker) {
        MyApplication.getInstance().getRtcClient().enableSpeaker(mAudioManager, bSpeaker);
    }

    /**
     * 通话中界面布局
     */
    private void initCallingView() {
        layout_call_center.setVisibility(View.GONE);
        layout_bottom_caller.setVisibility(View.GONE);
        layout_bottom_called.setVisibility(View.GONE);            
        layout_bottom_calling.setVisibility(View.VISIBLE);
        if (inCall) {
            layout_hangup.setVisibility(View.GONE);
            layout_exit.setVisibility(View.VISIBLE);
            layout_called.setVisibility(View.VISIBLE);
            img_members_add.setVisibility(View.GONE);
        } else {
            layout_exit.setVisibility(View.GONE);
            layout_hangup.setVisibility(View.VISIBLE);
            layout_calling_caller.setVisibility(View.VISIBLE);
            img_members_add.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置通话中显示的数据
     */
    private void initCallingData() {
        if (inCall) {
            //TContactInfo info = SQLiteManager.getInstance().getContactInfoByNumber(groupInfo.getGroupCreator());
            if (info != null) {
                tv_called_name.setText(groupInfo.getGroupName());
            } else {
                tv_called_name.setText("群组名称");
            }
            chronometer_called.setBase(SystemClock.elapsedRealtime());
            chronometer_called.setFormat("%s");
            chronometer_called
            .setOnChronometerTickListener(new OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer ch) {
                }
            });
            chronometer_called.start();
        } else {
            String[] groupMembers = groupInfo.getGroupMembers().split(";");
            if (groupInfo.getGroupPhoto() != null) {
                // TODO 设置头像
                // img_group.setBackgroundDrawable();
            }
            tv_group_name.setText(groupInfo.getGroupName());
      //      tv_group_members_count.setText("(" + groupMembers.length + ")");
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.setFormat("%s");
            chronometer
            .setOnChronometerTickListener(new OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer ch) {
                }
            });
            chronometer.start();
        }
    }

    /**
     * 配置群组成员导航
     */
    private void configNavigationView(String[] members) {
        int width = ScreenUtil.getScreenWidth(getApplicationContext());
        navigationView.setNumCoum(members.length); // 设置导航菜单个数
        int weight = 88 * width / 480;
        list = new ArrayList<TContactInfo>();
        navigationView.setNumCoum(members.length); // 设置导航菜单个数
        // 配置导航菜单宽度,确保导航菜单充满屏幕且菜单项所占宽度平均
        if (members.length * weight > width) {
            navigationView.setListWidth(members.length * weight);
        } else {
            navigationView.setListWidth(weight*members.length);
            navigationView.visibleScalingBtn();
        }
        if (inCall) {
            navigationView.refreshNavigationView(false); // 刷新导航菜单项
        }else {
            navigationView.refreshNavigationView(true); // 刷新导航菜单项
            navigationView.setOnNavigationListener(this);
        }
        navigationView.cleanList(); // 刷新导航菜单项
        for(int i=0;i<members.length;i++){
            TContactInfo contactInfo = SQLiteManager.getInstance().getContactInfoByNumber(members[i]);
            if (contactInfo != null) {
                navigationView.addNavigationCell(contactInfo);
                list.add(contactInfo);
            }
        }
        navigationView.notityChange();
        navigationView.setOnNavigationListener(this);
        navigationView.visibleScalingBtn();
    }

    /**
     * 配置SurfaceView 配置主叫视频与被叫视频的入口 TODO 因为现在无视频流，所以双方现在只显示本地摄像头采集的视频
     */
    private void initSurfaceView() {
        if( MyApplication.getInstance().getMGroupCall()!=null)
        {
            Connection call = MyApplication.getInstance().getMGroupCall();
            if (inCall) { // 被叫
                if(calledLocal == null)
                {
                    calledLocal = (SurfaceView)call.createVideoView(true, this,true);
                    calledLocal.setVisibility(View.VISIBLE);
                    ourSideSF_called.removeAllViews();
                    ourSideSF_called.addView(calledLocal);
                    //              calledLocal.setZOrderMediaOverlay(true);
                    //              calledLocal.setZOrderOnTop(true);  
                }
                if(calledRemote == null)
                {
                    calledRemote = (SurfaceView)call.createVideoView(false, this,true);
                    calledRemote.setVisibility(View.VISIBLE);
                    otherSideSF_called.setBackgroundColor(0xFF0000);
                    otherSideSF_called.removeAllViews();
                    otherSideSF_called.addView(calledRemote);

                }
                call.buildVideo(calledRemote);
            } else { // 主叫
                if (callerLocal == null) {
                    callerLocal = (SurfaceView)call.createVideoView(true, this,true);
                    callerLocal.setVisibility(View.VISIBLE);
                    ourSideSF_caller.setBackgroundColor(0x00ff00);
                    ourSideSF_caller.removeAllViews();
                    ourSideSF_caller.addView(callerLocal);
                }
                if (callerRemote == null) {
                    callerRemote = (SurfaceView)call.createVideoView(false, this,true);
                    callerRemote.setVisibility(View.VISIBLE);
                    otherSideSF_caller.setBackgroundColor(0xFF0000);
                    otherSideSF_caller.removeAllViews();
                    otherSideSF_caller.addView(callerRemote);
                }
                call.buildVideo(callerRemote);
            }            
        }

    }

    /**
     * 关闭UI结束Activity
     */
    private void closeUI() {
        if (chronometer != null) {
            chronometer.stop();
        }
        if (chronometer_called != null) {
            chronometer_called.stop();
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
     * 结束呼叫-主叫
     * 
     * @param view
     */
    public void hangUp(View view) {
        MyApplication.getInstance().onConfCallHangup();
        img_call_status.setBackgroundResource(R.drawable.icon_tv_hangup);
        tv_call_status.setText(getString(R.string.vv_show_call_hangup_tip));
        saveGroupRecordInfo();
        closeUI();
    }


    /**
     * 静音
     * 
     * @param view
     */
    public void onMute(View view) {
        if (isMute) {
            layout_mute.setBackgroundColor(getResources().getColor(
                    R.color.calling_bottom_bg));
            tv_mute.setTextColor(getResources().getColor(R.color.calling_mute));
        } else {
            layout_mute.setBackgroundColor(getResources().getColor(
                    R.color.lightgray));
            tv_mute.setTextColor(getResources().getColor(
                    R.color.tx_mute_checked));
        }
        isMute = !isMute;
        MyApplication.getInstance().getMGroupCall().setMuted(isMute);
    }

    /**
     * 退出现场直播
     * 
     * @param view
     */
    public void onExit(View view) {
        MyApplication.getInstance().onConfCallHangup();
        layout_exit.setBackgroundColor(getResources().getColor(
                R.color.lightgray));
        tv_exit.setTextColor(getResources().getColor(R.color.tx_hangup_checked));

        img_status.setBackgroundResource(R.drawable.icon_vv_show_hangup);
        tv_status.setText(getString(R.string.vv_show_call_exit));
        saveGroupRecordInfo();
        closeUI();
    }

    /**
     * 结束现场直播
     * 
     * @param view
     */
    public void onHangUp(View view) {
        MyApplication.getInstance().onConfCallHangup();

        layout_hangup.setBackgroundColor(getResources().getColor(
                R.color.lightgray));
        tv_hangup.setTextColor(getResources().getColor(
                R.color.tx_hangup_checked));
        img_status.setBackgroundResource(R.drawable.icon_chat_room_hangup);
        tv_status.setText(getString(R.string.vv_show_call_onhangup_tip));
        saveGroupRecordInfo();

        closeUI();
    }

    /**
     * 扬声器
     * 
     * @param view
     */
    public void onSpeaker(View view) {
        if (isSpeaker) {
            layout_speaker.setBackgroundColor(getResources().getColor(
                    R.color.calling_bottom_bg));
            tv_speaker.setTextColor(getResources().getColor(
                    R.color.calling_speaker));
        } else {
            layout_speaker.setBackgroundColor(getResources().getColor(
                    R.color.lightgray));
            tv_speaker.setTextColor(getResources().getColor(
                    R.color.tx_speaker_checked));
        }
        isSpeaker = !isSpeaker;

        enableSpeaker(isSpeaker);
    }

    /**
     * 接受呼叫-被叫 TODO 模拟需要修改
     * 
     * @param view
     */
    private boolean bAccept = false;//用于维护是否触发接听操作未接听在在呼叫失败时不播放音乐
    public void onAccept(View view){
        bAccept = true;
        stopRing();
        CommFunc.PrintLog(5, LOGTAG, "onAccept");
        MyApplication.getInstance().onConfCallAccept();
        initCallingView();
        initCallingData();
        joinResult = TGroupRecordInfo.CALL_TYPE_IN_SUCCESS;
    }



    /**
     * 拒绝呼叫-被叫
     * 
     * @param view
     */
    public void onRefused(View view) {
        CommFunc.PrintLog(5, LOGTAG, "onRefused");
        MyApplication.getInstance().onConfCallHangup();
        img_call_status.setBackgroundResource(R.drawable.icon_tv_hangup);
        tv_call_status.setText(getString(R.string.vv_show_call_refuse));
        joinResult = TGroupRecordInfo.CALL_TYPE_IN_FAIL;
        saveGroupRecordInfo();
        closeUI();
    }

    /**
     * TODO 主叫与被叫隐藏、显示直播画面的入口
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_calling_camera:// 主叫
                if (isChecked) { // 隐藏主叫画面
                    CommFunc.DisplayToast(getApplicationContext(), "主叫隐藏直播画面");
                } else { // 显示主叫画面
                    CommFunc.DisplayToast(getApplicationContext(), "主叫显示直播画面");
                }
                break;
            case R.id.cb_calling_called_camera: // 被叫
                if (isChecked) { // 隐藏被叫画面
                    CommFunc.DisplayToast(getApplicationContext(), "被叫隐藏直播画面");
                } else { // 显示被叫画面
                    CommFunc.DisplayToast(getApplicationContext(), "被叫显示直播画面");
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        if(callerRemote!=null || calledRemote!=null) {
            if(MyApplication.getInstance().mGroupCall!=null)
                MyApplication.getInstance().mGroupCall.resetVideoViews();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unregistFlag == false) {
            unregistFlag = true;
            unregisterReceiver(receiver);
        }
        enableSpeaker(false);
    }

    private void ParseNotifyInfo(String infos) {
        // 如果数据库中没有该成员需要更新该成员。
        try {
            JSONArray mebmberArray = new JSONArray(infos);
            // CommFunc.PrintLog(5,
            // LOGTAG,"parseNotifyInfo callID:"+callid);
            for (int i = 0; i < mebmberArray.length(); i++) {
                JSONObject itemObject = mebmberArray.getJSONObject(i);
                String userid = itemObject.getString("appAccountID");
                SQLiteManager.getInstance().SaveContactByID(userid);
                if (itemObject.has("memberStatus")) 
                {
                    int status = itemObject.getInt("memberStatus");
                    //如果创建者自己则不进行提示
                    if(userid.equals(MyApplication.getInstance().getUserID()) == false)
                    {
                        CommFunc.PrintLog(5,LOGTAG,"parseNotifyInfo nStatus="+status+RtcConst.getMemberStatus(status)+"userid="+userid);
                        CommFunc.DisplayToast(this, "用户:["+userid+"]"+RtcConst.getMemberStatus(status)); 
                        //此处刷新成员加入 、踢出状态 如果创建者自己导航栏也不刷新

                    }
                    //创建者方用户id不是自己则刷新 //对于创建者自己不显示
                  //  if(inCall==true||(inCall==false && userid.equals(MyApplication.getInstance().getUserID()) == false))
                    if(inCall==false && userid.equals(MyApplication.getInstance().getUserID()) == false) //创建者方刷新且不刷新自己
                        UpdateNavigationMember(status,userid);
                }
                if (itemObject.has("dowVideoState")) {
                    int videoState = itemObject.getInt("dowVideoState"); // 0：关闭
                    // 1：
                    // 开启
                    CommFunc.PrintLog(5, LOGTAG,
                            "parseNotifyInfo videoState=" + videoState
                            + "userid=" + userid);
                    String sStatus = (videoState == 0) ? "关闭" : "开启";
                    CommFunc.DisplayToast(this, "用户:[" + userid + "]"
                            + "视频:" + sStatus);

                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void BroadMsg_OnRequest(String params) {
        CommFunc.PrintLog(5, LOGTAG, "BroadMsg_OnRequest:" + "  params:" + params);
        // 需要解析notify 刷新成员状态
        // // parameters:{"req":1,"members":[{"s":2,"m":"1665"}]}
        ParseNotifyInfo(params);

    }

    private void BroadMsg_OnResponse(int action, String params) {
        CommFunc.PrintLog(1, LOGTAG,
                "BroadMsg_OnResponse actionn:" + RtcConst.getStringID(action)
                + "params:" + params);
        switch (action) {
            // rest 相关;
            case RtcConst.groupcall_opt_create: {
                onResponse_grpcreate(params);
                break;
            }
            case RtcConst.groupcall_opt_getmemberlist: {
                onResponse_grpgetMemberList(params);
                break;
            }
            case RtcConst.groupcall_opt_invitedmemberlist: {
                onResponse_grpInvitedMemberList(params);
                break;
            }
            case RtcConst.groupcall_opt_kickedmemberlist: {
                onResponse_grpkickedMemberList(params);
                break;
            }
            case RtcConst.groupcall_opt_close: {
                onResponse_grpClose(params);
                break;
            }
            default: {
                CommFunc.PrintLog(5, LOGTAG, "BroadMsg_OnResponse unknownmsg"
                        + params);
                break;
            }
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    CallingActivity.BROADCAST_CALLING_ACTION)) {
                Bundle bundle = intent.getExtras();
                // CommFunc.PrintLog(5, LOGTAG, "onReceive");
                if (bundle != null) {
                    Message msg = new Message();
                    msg.what = bundle.getInt("what", 0);
                    int msgtype = bundle.getInt("arg2",
                            MsgKey.grpv_listener_onResponse); //
                    if (msgtype == MsgKey.grpv_listener_onRequest) {
                        BroadMsg_OnRequest(bundle.getString("arg1"));
                    } else if (msgtype == MsgKey.grpv_listener_onResponse) {
                        BroadMsg_OnResponse(msg.what, bundle.getString("arg1"));
                    } else if (msgtype == MsgKey.broadmsg_sip) {
                        msg.arg1 = bundle.getInt("arg1", 0);
                        handleCallMessage(msg);
                    } else {
                        CommFunc.PrintLog(5, LOGTAG, "unknowmsg broad");
                    }

                }
            }

        }
    };

    private void onResponse_grpcreate(String params) {
        CommFunc.PrintLog(5, LOGTAG, "onResponse_grpvcreate resopnse:" + params);
        try {
            if (params == null || params.equals("")) {
                CommFunc.DisplayToast(CallingTVActivity.this, "创建现场直播失败参数为空");
                CommFunc.PrintLog(5, LOGTAG,
                        "OnRestgrpvOptCreate resopnse strResponse:"
                        + params);
                return;
            }
            JSONObject jsonrsp = new JSONObject(params);
            CommFunc.PrintLog(5, LOGTAG, "OnRestgrpvOptCreate resopnse:"
                    + jsonrsp.toString());
            String code = jsonrsp.getString("code");
            String reason = jsonrsp.getString("reason");
            CommFunc.PrintLog(5, LOGTAG, "OnRestgrpvOptCreate code:" + code
                    + " reason:" + reason);
            if (code.equals("0")) {
                CommFunc.DisplayToast(CallingTVActivity.this, "现场直播创建成功");
                initCallingView();
                initCallingData();

                joinResult = TGroupRecordInfo.CALL_TYPE_OUT_SUCCESS;
            } else {
                CommFunc.PrintLog(5, LOGTAG, "现场直播创建失败:" + code + " reason:"
                        + reason);
                CommFunc.DisplayToast(CallingTVActivity.this, "现场直播创建失败:" + code
                        + " reason:" + reason);

                joinResult = TGroupRecordInfo.CALL_TYPE_OUT_SUCCESS;
                saveGroupRecordInfo();
                finish();
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void OnParserGetMemberList(JSONObject response) {
        CommFunc.PrintLog(5, LOGTAG, "OnParserGetMemberList:");
        JSONObject jsonrsp = response;
        JSONArray jsonArr;
        try {
            jsonArr = jsonrsp.getJSONArray("memberInfoList");
            StringBuffer members = new StringBuffer();
            for (int i = 0; i < jsonArr.length(); i++) 
            {  
                JSONObject itemObject = jsonArr.getJSONObject(i);
                String userid = itemObject.getString("appAccountID");
                int memberstatus = itemObject.getInt("memberStatus");      //2代表已加入 3代表未加入  4代表被删除出
                SQLiteManager.getInstance().SaveContactByID(userid);
                CommFunc.PrintLog(5, LOGTAG, "OnParserGetMemberList: userid"+itemObject.getString("appAccountID"));
                CommFunc.PrintLog(5, LOGTAG, "OnParserGetMemberList: role"+itemObject.getInt("role"));
                if(itemObject.getInt("role")==1)
                {

                 //   tv_group_creator.setText(userid); //
                    //先判断群组创建者有没有，有不刷新，没有刷新(第一次作为普通成员，第二次从此组发起则为创建者，此时不刷新)
                    String oldcreator = SQLiteManager.getInstance().getGroupCreatorByGrpName(m_gvcname);
                    CommFunc.PrintLog(5, LOGTAG, "oldcreator:"+oldcreator);
                    groupInfo.setGroupCreator(userid); //设置创建者
                    if(oldcreator.equals(""))
                    {   
                        CommFunc.PrintLog(5, LOGTAG, "OnParserGetMemberList setGroupCreator:"+userid);                      
                        SQLiteManager.getInstance().updateGroupInfo(groupInfo.getGroupId(),TGroupInfo._GROUP_CREATOR,userid,false);
                   
                    }
                    if(userid.equals(MyApplication.getInstance().getUserID())==false) //创建者不是自己在成员中加入
                        members.append(userid).append(";"); //添加成员，多个成员用";"隔开
                        
                } else {
                    if(userid.equals(MyApplication.getInstance().getUserID())==false) //如果是自己就不加入到群员中
                    members.append(userid).append(";"); //添加成员，多个成员用";"隔开
                }
                if(inCall==false)//作为创建者时刷新
                UpdateNavigationMember(memberstatus,userid);
                //如果自己为非创建者 需要刷新mic状态
            }
            members = members.deleteCharAt(members.lastIndexOf(";"));
            CommFunc.PrintLog(5, LOGTAG, "OnParserGetMemberList members:"+members);
            groupInfo.setGroupMembers(members.toString());

          //  SQLiteManager.getInstance().updateGroup(groupInfo, true);
            SQLiteManager.getInstance().updateGroupInfo(groupInfo.getGroupId(),TGroupInfo._GROUP_MEMBERS,members.toString(),false);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private void UpdateNavigationMember(int memberStatus,String userid)
    {
        //如果普通成员作为发起方，此时原有创建者作为普通成员处理。
        if (userid.equals(m_creator)) {
            return;
        }  	
        Set<String> phoneNumbers = new HashSet<String>();
        for (int j = 0; j < list.size(); j++) {
            phoneNumbers.add(list.get(j).getPhoneNum());
        }
        switch (memberStatus) {
            case 2:	//加入
                phoneNumbers.add(userid);
                break;
            case 3: //未加入。非创建者自己退出会走此处。
            case 4:	//删除
                phoneNumbers.remove(userid);
            default:
                break;
        }

        String[] members = new String[phoneNumbers.size()];
        phoneNumbers.toArray(members);    
        configNavigationView(members);
    }
    private void onResponse_grpgetMemberList(String parameters) {
        CommFunc.PrintLog(5, LOGTAG, "onResponse_grpvgetMemberLis resopnse:"
                + parameters);
        try {
            if (parameters == null || parameters.equals("")) {
                Utils.PrintLog(5, LOGTAG,
                "onResponse_grpvgetMemberLis fail result: null");
                return;
            }
            JSONObject jsonrsp = new JSONObject(parameters);
            String code = jsonrsp.getString("code");
            String reason = jsonrsp.getString("reason");
            Utils.PrintLog(5, LOGTAG, "onResponse_grpvgetMemberLis code:"
                    + code + " reason:" + reason);
            if (code.equals("0")) {
                OnParserGetMemberList(jsonrsp);
            } else {
                CommFunc.PrintLog(5, LOGTAG, "获取成员列表失败:"+code+" reason:"+reason);
                CommFunc.DisplayToast(CallingTVActivity.this, "获取成员列表失败:"+code+" reason:"+reason);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void onResponse_grpInvitedMemberList(String parameters) {
        CommFunc.PrintLog(5, LOGTAG,
                "onResponse_grpvInvitedMemberList resopnse:" + parameters);

        try {
            if (parameters == null || parameters.equals(""))
                return;
            JSONObject jsonrsp = new JSONObject(parameters);
            if(jsonrsp.isNull("code")==false)
            {
                String code = jsonrsp.getString("code");
                String reason = jsonrsp.getString("reason");
                // Utils.PrintLog(5, LOGTAG,
                // Utils.PrintLog(5, LOGTAG, "onResponse_grpvInvitedMemberList code:"+code+" reason:"+reason);
                if(code.equals("0"))
                {
                    CommFunc.PrintLog(5, LOGTAG, "邀请成员参与群组会话成功");
                    CommFunc.DisplayToast(CallingTVActivity.this, "邀请成员参与群组会话成功:"+code+" reason:"+reason);
                }
                else
                {
                    CommFunc.PrintLog(5, LOGTAG, "邀请成员参与群组会话失败:"+code+" reason:"+reason);
                    CommFunc.DisplayToast(CallingTVActivity.this, "邀请成员参与群组会话失败:"+code+" reason:"+reason);
                }
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            ;// setStatusText("邀请成员参与群组会话失败 JSONException:"+e.getMessage());
            e.printStackTrace();
        }
    }

    private void onResponse_grpkickedMemberList(String parameters) {
        CommFunc.PrintLog(5, LOGTAG,
                "onResponse_grpvkickedMemberList resopnse :" + parameters);

        try {
            if (parameters == null || parameters.equals(""))
                return;
            JSONObject jsonrsp = new JSONObject(parameters);
            if(jsonrsp.isNull("code")==false)
            {
                String code = jsonrsp.getString("code");
                String reason = jsonrsp.getString("reason");
                //    Utils.PrintLog(5, LOGTAG, "onResponse_grpvkickedMemberList code:"+code+" reason:"+reason);
                if(code.equals("0") || code.equals("200"))
                {
                    CommFunc.PrintLog(5, LOGTAG, "踢出成员成功");
                    CommFunc.DisplayToast(CallingTVActivity.this, "踢出成员成功");
                    if(list.size()>0)
                        list.remove(kickposition);
                }
                else
                {
                    CommFunc.DisplayToast(CallingTVActivity.this, "踢出成员失败:"+code+" reason:"+reason);
                    CommFunc.PrintLog(5, LOGTAG, "踢出成员失败:"+code+" reason:"+reason);
                }
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            // setStatusText("踢出成员失败 JSONException:"+e.getMessage());
            e.printStackTrace();
        }
    }

    private void onResponse_grpClose(String parameters) {
        CommFunc.PrintLog(5, LOGTAG,
                "onResponse_grpvCloseGrpv resopnse strResponse:" + parameters);

        try {
            if (parameters == null || parameters.equals(""))
                return;
            JSONObject jsonrsp = new JSONObject(parameters);
            if(jsonrsp.isNull("code")==false)
            {
                String code = jsonrsp.getString("code");
                String reason = jsonrsp.getString("reason");
                //     Utils.PrintLog(5, LOGTAG, "onResponse_grpvCloseGrpv code:"+code+" reason:"+reason);
                if(code.equals("0") || code.equals("200"))
                {
                    CommFunc.PrintLog(5, LOGTAG, "关闭群组成功");
                }
                else
                {
                    CommFunc.PrintLog(5, LOGTAG, "关闭群组失败:"+code+" reason:"+reason);
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param msg
     */
    private boolean isFail;// 标识是否为呼叫失败,如果呼叫失败再HangUp应该将呼叫结果设置为Fail

    private void handleCallMessage(Message msg) {
        switch (msg.what) {
            case MsgKey.SLN_180Ring:
                CommFunc.PrintLog(1,LOGTAG, "handleCallMessage SLN_180Ring ");
                if (notimerFlag == false) {
                    notimerFlag = true;
                    noTimer = new Timer(true);
                    noTimer.schedule(timerTask, 1 * 60 * 1000); // 1分钟
                    playRing(R.raw.ring180, false); // 正在呼叫铃声
                }
                break;
            case MsgKey.SLN_CallAccepted:
            {      
                CommFunc.PrintLog(1,LOGTAG, "handleCallMessage SLN_CallAccepted ");
                break;
            }
            case MsgKey.SLN_CallVideo: {
                CommFunc.PrintLog(5, LOGTAG, "handleCallMessage SLN_CallVideo");
                HBaseApp.post2UIRunnable(new Runnable() {
                    @Override
                    public void run() {
                        initSurfaceView();
                    }			    
                });
            }
            break;
            case MsgKey.SLN_CallClosed:
            {   
                CommFunc.PrintLog(1,LOGTAG, "handleCallMessage SLN_CallClosed ");
                playRing(R.raw.ring_bye, false); // 无应答铃声

                joinResult = TGroupRecordInfo.CALL_TYPE_IN_SUCCESS;
                saveGroupRecordInfo();
                closeUI();
                break;
            }
            case MsgKey.SLN_CallFailed:
                CommFunc.PrintLog(1,LOGTAG, "handleCallMessage SLN_CallFailed ");
                isFail = true;
                joinResult = TGroupRecordInfo.CALL_TYPE_OUT_FAIL;
                saveGroupRecordInfo();
                
                if(inCall&& bAccept ==false)
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
                if (msg.arg1 == RtcConst.CallCode_Busy) { // 用户忙
                    playRing(R.raw.ring486, true);
                }  else if (msg.arg1 == RtcConst.CallCode_Reject) { // 正在通话
                    playRing(R.raw.ring603, true);
                } else if (msg.arg1 >= RtcConst.CallCode_RequestErr &&
                        msg.arg1 < RtcConst.CallCode_ServerErr) { // 480铃声-对方无法接通
                    playRing(R.raw.ring4xx, true);
                } else if (msg.arg1 >= RtcConst.CallCode_ServerErr 
                        && msg.arg1 < RtcConst.CallCode_GlobalErr) {
                    playRing(R.raw.ring4xx, true);
                } else if (msg.arg1 >= RtcConst.CallCode_GlobalErr ) {
                    playRing(R.raw.ring4xx, true);
                }                CommFunc.PrintLog(5, LOGTAG, "呼叫失败:[" + msg.arg1 + "]");
                CommFunc.DisplayToast(CallingTVActivity.this, "呼叫失败:[" + msg.arg1
                        + "]");
                // closeUI();
                break;
            case MsgKey.SLN_NetWorkChange:
            { 
                OnChangeNetWork(msg);
                break;
            }
            default:
            {   
                CommFunc.PrintLog(1,LOGTAG, "handleCallMessage unknown message:"+ msg.what);
                break;
            }
        }
    }
    private boolean unregistFlag = false;
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
     * 播放资源铃音
     * 
     * @param resId
     * @param hanguped
     */
    private int count;

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
            mediaPlayer = MediaPlayer.create(CallingTVActivity.this, resId);
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
		 || (android.os.Build.BRAND.equalsIgnoreCase("samsung")
		 && android.os.Build.DEVICE.equalsIgnoreCase("klte"))
		 || (android.os.Build.BRAND.equalsIgnoreCase("samsung")
		 && android.os.Build.DEVICE.equalsIgnoreCase("klteduosctc"))
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
    
    /**
     * 踢人入口在这里 requestCode为被踢人在list中的下标值
     * 创建者增加发言权的入口也在这里，如果isAddSpeaker为true表示为增加发言权，false为踢人
     */
    @Override
    public void onClick(int requestCode, String tag) {
        CommFunc.PrintLog(5, LOGTAG, "onClick requestCode:"+requestCode);
        try{
            if (!inCall && list.size()!=0) {  //只允许创建者点击有响应的事件响应
                TContactInfo info = list.get(requestCode);
                if(info == null)
                {  
                    CommFunc.PrintLog(5, LOGTAG, "onClick 踢人info == null");
                    return;
                }
                showKickMemberDialog(requestCode,info.getPhoneNum());
            } 
        }
        catch(Exception e)
        {
            CommFunc.PrintLog(5, LOGTAG, "onClick exception:"+e.getMessage());
        }
    }
    private int kickposition =0;
    private void showKickMemberDialog(int requestCode,String userid) {
        final int position = requestCode;
        kickposition = position;
        final String id = userid;
        CommFunc.PrintLog(5, LOGTAG, "showKickMemberDialog()");
        if(dialog==null){
            dialog = new Dialog_model(this, R.style.FloatDialog);
        }
        dialog.setMessageText(getString(R.string.ok),getString(R.string.cancel),"您确定要剔除成员"+userid+" 吗？");
        dialog.setOnDialogClickListener(new OnDialogClickListener() {
            @Override
            public void onClickRightButton() {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
            }
            @Override
            public void onClickLeftButton() {
                //弹出提示框提示是否要删除
                dialog.dismiss();
                onOpt_KickMember(id);
               // list.remove(position);  //

            }
        });
        dialog.show();
    }
    /** 添加成员 */
    public void onAddMember(View v) {
        Intent intent = new Intent(this, SelectContactActivity.class);
        if (list != null && list.size() > 0) {
            String[] members = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                members[i] = list.get(i).getPhoneNum();
            }
            intent.putExtra("el", members);// 目前人员总数
        }
        startActivityForResult(intent, ADD_MENBER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case ADD_MENBER:// 添加人员结果
                String el = data.getStringExtra("el");
                onOpt_InvitedMebmer(el);
                break;
            default:
                break;
        }
    }

    public void onOpt_KickMember(String  remoteuri) //踢出成员
    {
        CommFunc.PrintLog(5, LOGTAG, "onOpt_KickMember:"+remoteuri);
        GroupMgr grpmgr = MyApplication.getInstance().getGrpMgr();
        if(grpmgr==null)
            return;

        grpmgr.groupCall(RtcConst.groupcall_opt_kickedmemberlist,remoteuri); //多人列表取被叫 逗号 间隔 
    }
    public void onOpt_InvitedMebmer(String  remoteuri) //邀请成员加入
    {
        CommFunc.PrintLog(5, LOGTAG, "onOpt_InvitedMebmer:"+remoteuri);
        GroupMgr grpmgr = MyApplication.getInstance().getGrpMgr();
        if(grpmgr==null)
            return;

        grpmgr.groupCall(RtcConst.groupcall_opt_invitedmemberlist, remoteuri); //多人列表取被叫 逗号 间隔 
    }

    public void OnOpt_getMemberlist() // 获取成员列表
    {
        CommFunc.PrintLog(5, LOGTAG, "OnOpt_getMemberlist:");
        GroupMgr grpmgr = MyApplication.getInstance().getGrpMgr();

        if (grpmgr == null)
            return;

        grpmgr.groupCall(RtcConst.groupcall_opt_getmemberlist, null);
    }
    public void OnOpt_CloseConf() //关闭会话
    {
        CommFunc.PrintLog(5, LOGTAG, "OnOpt_CloseConf");
        GroupMgr grpmgr =  MyApplication.getInstance().getGrpMgr();
        if(grpmgr==null)
            return;

        grpmgr.groupCall(RtcConst.groupcall_opt_close,null); //多人列表取被叫 逗号 间隔 
    }
    /**
     * 保存群组通话记录
     */
    public void saveGroupRecordInfo() {
        info.setGroupId(String.valueOf(groupInfo.getGroupId()));
        info.setCallId(String.valueOf(CommFunc.dateToLang(startDate + " "
                + startTime)));
        info.setStartDate(startDate);
        info.setEndDate(CommFunc.getEndDate());
        info.setStartTime(startTime);
        info.setEndTime(CommFunc.getEndTime());
        // info.setTime(String.valueOf(CommFunc.getTotalTime(startTime,
        // info.getEndTime())));
        info.setTime(chronometer.getText().toString());
        info.setDuration(duration);
        info.setConftype(TGroupRecordInfo.TYPE_SHOW);
        info.setJoinResult(joinResult);
        SQLiteManager.getInstance().saveGroupRecordInfo(info, false);
    }

}