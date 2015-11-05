package com.sip.rtcclient.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.ImageView;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.config.MsgKey;
import com.sip.rtcclient.utils.CommFunc;

/**
 * 欢迎、动画Activity
 * 
 * @author Zhp
 * 
 */
public class WelcomeActivity extends BaseActivity {

    private String LOGTAG = "WelcomeActivity";

    private int key_back_num = 0;// 记录点击返回键的次数
    private final int TIME_OUT = 1001;
    private final int MSG_CHECKUPDATE = 1002;
    private Timer timer;
    private Intent intent;

    private AnimationDrawable animDrawable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome);
        //   curContext = this;
        initStart();
        configView();
        timer = new Timer(true);
        timer.schedule(task, 1000);
    }

    /**
     * 
     */
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            CommFunc.PrintLog(5, LOGTAG, "Timeout");
            animDrawable.stop();
            Message message = new Message();
            message.what = TIME_OUT;
            handler.sendMessage(message);
            timer.cancel();
            this.cancel();
        }
    };

    /**
     * 
     */
    private void initStart() {
        CommFunc.PrintLog(5, LOGTAG, "initStart()");
        intent = new Intent();
        int isNormalExit = MyApplication.getInstance().getIntSharedXml(MsgKey.key_isNormalExit, 0);
        //        boolean isRecycleFlag = MyApplication.getInstance()
        //        .getBooleanSharedXml(MsgKey.key_recycleflag, false);
        if (MyApplication.getInstance().getVersionName() != null
                && !MyApplication
                .getInstance()
                .getVersionName()
                .equals(MyApplication.getInstance().getAppVersionName())) {
            CommFunc.PrintLog(5, LOGTAG, "version is change LoginActivity");
            intent.setClass(this, LoginActivity.class);
        }
        //        else if (isRecycleFlag) {
        //            CommFunc.PrintLog(5, LOGTAG, "recycle is true MainActivity");
        //            intent.setClass(this, MainActivity.class);
        //        } 
        else if (isNormalExit == 1) {
            // 第一次登陆进入此分支 如果是切换帐户或非正常退出则进入登陆页面直接进入登陆页面
            CommFunc.PrintLog(1, LOGTAG, "  NormalExitFlag:" + isNormalExit
                    + "    LoginActivity");
            intent.setClass(this, LoginActivity.class);
        } 
        else {
            CommFunc.PrintLog(1, LOGTAG, "setClass MainActivity");
            intent.setClass(this, MainActivity.class);
        }
        //  	intent.setClass(this, MainActivity.class); // test 直接进入主页面
    }

    /**
     * 
     */
    private void configView() {
        ImageView view = (ImageView) findViewById(R.id.splash_imgview);
        animDrawable = (AnimationDrawable) view.getDrawable();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        animDrawable.start();
    };

    /**
     * 
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIME_OUT:
                    CommFunc.PrintLog(5, LOGTAG, "TIME_OUT startActivity");
                    startActivity(intent);
                    finish();
                    break;
                    //                case MSG_CHECKUPDATE:
                    //                    OnDisCheckUpdate((HttpResult)msg.obj);
                    //                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (key_back_num == 0) {
                    key_back_num++;
                } else {
                    key_back_num = 0;
                    timer.cancel();
                    finish();
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    //	public void CheckUpdate()
    //	{
    //	    HBaseApp.post2WorkRunnable(new Runnable(){
    //
    //            @Override
    //            public void run() {
    //                // TODO Auto-generated method stub
    //                String version = "version:"+MyApplication.getInstance().getAppVersionName();
    //                HttpResult ret = AppHttpUtils.getInstance().apppost(AppHttpUtils.updateurl, version);
    //                //OnDisCheckUpdate(ret);
    //               Message msg = new Message();
    //               msg.obj = ret;
    //               msg.what = MSG_CHECKUPDATE;
    //               handler.sendMessage(msg);
    //            }
    //	        
    //	    });
    //	}
    //public static Context curContext = null;
    //    private void OnDisCheckUpdate(HttpResult ret)
    //    {
    //        if(ret.getStatus()==HttpURLConnection.HTTP_OK)
    //        {   
    //            String sResp = (String)ret.getObject();
    //            try {
    //                if(sResp!=null && sResp.equals("")==false)
    //                {
    //                    JSONObject obj = new JSONObject(sResp); 
    //                    int needupdate =  obj.getInt("needupdate");
    //                    String newVersion = obj.getString("ver");
    //                    String updateurl = obj.getString("updateurl");
    //                    String desc = obj.getString("desc");
    //                    String errorcode = obj.getString("errorcode");
    //                    String errormessage = obj.getString("errormessage");
    //                    CommFunc.PrintLog(5, LOGTAG, "sResp:"+sResp);
    // 
    //                    if(needupdate==1)
    //                    {
    //                        MyAutoUpdate myupdate= new MyAutoUpdate(curContext);
    //                      //  String str = "系统检测到了天翼VV发布了\r\n最新版本,请及时更新！\r\n版本号:"+newVersion;
    //                        //showUpdateDialog(str,updateurl);
    //                        myupdate.setDownLoadInfo(updateurl, newVersion,desc);
    //                        if(myupdate.check())
    //                            myupdate.showUpdateDialog();
    //                    }
    //                }
    //
    //            } catch (JSONException e) {
    //                // TODO Auto-generated catch block
    //                e.printStackTrace();
    //            }
    //        }
    // 
    //    }


}