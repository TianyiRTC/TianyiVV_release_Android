package com.sip.rtcclient.activity;


import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import jni.http.HttpResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import rtc.sdk.common.RtcConst;

import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.sip.rtcclient.HBaseApp;
import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.group.GroupActivity;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.config.MsgKey;
import com.sip.rtcclient.config.SysConfig;
import com.sip.rtcclient.db.SQLiteManager;
import com.sip.rtcclient.http.utils.AppHttpUtils;
import com.sip.rtcclient.http.utils.MyAutoUpdate;
import com.sip.rtcclient.utils.CommFunc;
import com.sip.rtcclient.utils.PinYinManager;
import com.sip.rtcclient.utils.ScreenUtil;
import com.sip.rtcclient.ui.Dialog_model;
import com.sip.rtcclient.ui.Dialog_model.OnDialogClickListener;
import com.sip.rtcclient.ui.NavigationView;
import com.sip.rtcclient.ui.NavigationView.NavigationListener;

@SuppressWarnings("deprecation")
public class MainActivity extends ActivityGroup implements NavigationListener {

    public final static String BROADCAST_APP_SERVICE = "com.sip.rtcclient.service";
    public final static String BROADCAST_MISS_CALL = "com.sip.rtcclient.call.miss";
    public final static String BROADCAST_DISMISS_DIALPAD = "com.sip.rtcclient.dismiss.dialpad";

    private LinearLayout container; // 内容主体部分
    private NavigationView navigationView; // 底部导航菜单

    public static final String TAG_YISHITONG = "tag_yishitong";
    public static final String TAG_Contact = "tag_contact";
    public static final String TAG_GROUP = "tag_group";
    public static final String TAG_SETTING = "tag_setting";  
    private static final String LOGTAG = "MainActivity";
    private Dialog_model dialog;
    private static int  DAYAGO = 7;
    private static MainActivity instance;
    public static boolean bShowToast = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommFunc.PrintLog(5, LOGTAG, "onCreate");
        setContentView(R.layout.activity_main);
        MyApplication.getInstance().saveSharePrefValue(MsgKey.key_isNormalExit,""+0);
        SysConfig.getInstance().setIsLoginByBtn(false);
//        curContext = this;
        instance = this;
        initData();
        initView();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(MsgKey.key_msg_appexit);
        intentFilter.addAction(MsgKey.key_msg_kickoff);
      //  intentFilter.addAction(SysConfig.BROADCAST_RELOGIN_SERVICE);
        registerReceiver(receiver, intentFilter);
      //如果登陆成功在做此动作,防止开机启动失败调用
        CommFunc.PrintLog(5, LOGTAG, "onCreate:"+SysConfig.getInstance().ismLoginOK());
        if(SysConfig.getInstance().ismLoginOK())
        {
        //    sendAccountToServer();
        //    getUserList();
            CheckUpdate();
        }
    }
    
    public static MainActivity getInstance()
    {
        return instance;
    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(receiver);
    }
//    private void onDelLog()
//    {  
//        CommFunc.PrintLog(5, LOGTAG,  "dellog onDelLog");
//        HBaseApp.post2WorkDelayed(new Runnable(){
//
//            @Override
//            public void run() {
//                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) 
//                {
//                    CommFunc.PrintLog(5, LOGTAG,  "dellog onDelLog nosdcard");
//                    return;
//                }
//                CommFunc.PrintLog(5, LOGTAG,  "dellog onDelLog sdcard");
//               // File sdcard = Environment.getExternalStorageDirectory();
//                File logDir = new File(SysConfig.getInstance().getLogFolder());
////                CommFunc.PrintLog(5, LOGTAG,  "dellog onDelLog sdcard:"+SysConfig.getInstance().getLogFolder());
////                CommFunc.PrintLog(5, LOGTAG,  "dellog onDelLog logDir.exists():"+logDir.exists());
////                CommFunc.PrintLog(5, LOGTAG,  "dellog onDelLog logDir.isDirectory:"+logDir.isDirectory());
//                if (logDir.exists() && logDir.isDirectory()) {
//                    File[] files = logDir.listFiles();
//                    String deadline = CommFunc.getDayAgoDate(DAYAGO);
//                 //   CommFunc.PrintLog(5, LOGTAG,  "dellog deadline:"+deadline);
//                    for (int i = 0; i < files.length; i++) {
//                    	File logFile = files[i];
//                        String logName = logFile.getName(); //获取日志文件名称 20131212-075822_rtcsdk.log
//                   //     CommFunc.PrintLog(5, LOGTAG,  "dellog logName:"+logName);
//                        if (logName != null && logName.contains("-")) {
//                            String logTime = logName.split("-")[0];	//截取日志文件日期 20131212
//                      //      CommFunc.PrintLog(5, LOGTAG,  "dellog logTime:"+logTime);
//                            if (logTime.compareTo(deadline) < 0) {	//判断logTime是否小于deadLine
//                                if (logFile.isFile() && logFile.exists()) {
//                                    try {
//                                    	boolean bret = logFile.delete();
//                                    	CommFunc.PrintLog(5, LOGTAG,  "dellog bret:"+bret+" logName:"+logName);
//                                    } catch (Exception e) {
//                                        // do nothing
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                return;
//            }
//
//        }, 10000);
//             
//    }
//    
    private void sendAccountToServer()
    {
        CommFunc.PrintLog(5, LOGTAG, "sendAccountToServer");
        HBaseApp.post2WorkRunnable(new Runnable()
        {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                JSONObject jsonobj = new JSONObject();
                try {
                    jsonobj.put("userid", MyApplication.getInstance().getUserID());
                    jsonobj.put("username", MyApplication.getInstance().getAccountInfo().getUsername());
                    jsonobj.put("password", MyApplication.getInstance().getAccountInfo().getResttoken());
                    jsonobj.put("type", SysConfig.login_type);
                    CommFunc.PrintLog(5, LOGTAG, "sendAccountToServer:"+jsonobj.toString());
                    AppHttpUtils.getInstance().apppost(AppHttpUtils.loginurl,jsonobj.toString());

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
        });
    }

    private BroadcastReceiver receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(MsgKey.key_msg_appexit))
            {
                showExitTips();
            }
            else if(intent.getAction().equals(MsgKey.key_msg_kickoff))
            {
                showKickOffTips();
            }
        }
    };
    private void initData()
    {
        MyApplication.getInstance().saveSharePrefValue(MsgKey.key_recycleflag,""+1);
        MyApplication.getInstance().saveSharePrefValue(MsgKey.key_isNormalExit,""+0);
        //    MyApplication.getInstance().saveChangeAccountFlag(false);
    }

    /**
     * 初始化View
     */
    private void initView() {
        CommFunc.PrintLog(5, LOGTAG, "initView");
        container = (LinearLayout) findViewById(R.id.main_container);
        navigationView = (NavigationView) findViewById(R.id.main_navigation);
        configNavigationView();
        changeActivity(1, TAG_Contact); //避免首次登陆通讯录未获取
        changeActivity(0, TAG_YISHITONG);
      //  onClick(1, TAG_Contact) ;
    }

    /**
     * 配置导航菜单View
     */
    private void configNavigationView() {
        int width = ScreenUtil.getScreenWidth(getApplicationContext());

        navigationView.setNumCoum(4); // 设置导航菜单个数
        // 配置导航菜单宽度,确保导航菜单充满屏幕且菜单项所占宽度平均
        if (4 * 88 > width) {
            navigationView.setListWidth(4 * 88);
        } else {
            navigationView.setListWidth(width - 12);
            navigationView.visibleScalingBtn();
        }
        navigationView.refreshNavigationView(); // 刷新导航菜单项

        navigationView.addNavigationCell(R.drawable.main_tab_main, 0,
                TAG_YISHITONG);
        navigationView.addNavigationCell(R.drawable.main_tab_contact, 1,
                TAG_Contact);
        navigationView.addNavigationCell(R.drawable.main_tab_group, 2,
                TAG_GROUP);
        navigationView.addNavigationCell(R.drawable.main_tab_setting, 3,
                TAG_SETTING);
        navigationView.setOnNavigationListener(this);

        navigationView.visibleScalingBtn();
    }

    /**
     * 根据菜单ID切换Activity
     * 
     * @param i
     * @param tag
     */
    private void changeActivity(int id, String tag) {
        container.removeAllViews(); // 移除现有View
        CommFunc.PrintLog(1,LOGTAG,"changeActivity:id:" + id + " tag:"
                + tag);
        Class<?> activity = null;
        switch (id) {
            case 0:
                activity = CallRecordActivity.class;
                Intent intent = new Intent(CallRecordActivity.BROADCAST_DISMISS_DIALPAD);
                MyApplication.getInstance().sendBroadcast(intent);
                break;
            case 1:
                activity = ContactActivity.class;
                break;
            case 2:
                activity = GroupActivity.class;
                break;
            case 3:
                activity = SettingActivity.class;
                break;
        }

        Intent intent = new Intent(MainActivity.this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        LocalActivityManager localActivityManager = getLocalActivityManager();
		View view = localActivityManager.startActivity(tag, intent).getDecorView();
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        container.addView(view);
    }

    /**
     * 重写NavigationView的OnClick方法
     * 
     * @param requestCode
     *            导航菜单的位置0代表第一个 1代表第二个 以此类推
     * @param tag
     *            导航菜单的标识
     */
    @Override
    public void onClick(int requestCode, String tag) {
        changeActivity(requestCode, tag);
    }
    //boolean bShowKick = false;
    private void showKickOffTips() {
        
       // bShowKick = true;
        CommFunc.PrintLog(5, LOGTAG, "showKickOffTips");
        if(dialog==null){
            dialog = new Dialog_model(this, R.style.FloatDialog);
        }
        dialog.setMessageText(getString(R.string.ok),null,getString(R.string.dialog_kickoff));
        dialog.setOnDialogClickListener(new OnDialogClickListener() {
           @Override
            public void onClickRightButton() {
//                if (dialog != null && dialog.isShowing())
//                    dialog.dismiss();
               CommFunc.PrintLog(5, LOGTAG, "onClickRightButton");
            }
            @Override
            public void onClickLeftButton() {
                CommFunc.PrintLog(5, LOGTAG, "onClickLeftButton");
                OnExit();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }
    private void showExitTips() {
        if(dialog==null){
            dialog = new Dialog_model(this, R.style.FloatDialog);
        }
        dialog.setMessageText(getString(R.string.ok),getString(R.string.cancel),getString(R.string.dialog_exit));
        dialog.setOnDialogClickListener(new OnDialogClickListener() {
            @Override
            public void onClickRightButton() {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
            }
            @Override
            public void onClickLeftButton() {
                OnExit();
            }
        });
        dialog.show();
    }
    public void OnExit()
    {
        CommFunc.PrintLog(5, LOGTAG, "OnExit()");
        MyApplication.getInstance().saveSharePrefValue(MsgKey.key_isNormalExit,""+1);
        
        int nNormalExit = MyApplication.getInstance().getIntSharedXml(MsgKey.key_isNormalExit,0);
        CommFunc.PrintLog(5, LOGTAG, "OnExit() nNormalExit:"+nNormalExit);          

        //天翼账号保存 如果微博账号启动不从此处读取
        if(SysConfig.login_type==SysConfig.USERTYPE_TIANYI)
        MyApplication.getInstance().saveSharePrefValue(MsgKey.key_userid, SysConfig.userid);
        String userid = MyApplication.getInstance().getSharePrefValue(MsgKey.key_userid, SysConfig.userid);
        CommFunc.PrintLog(5, LOGTAG, "OnExit():"+userid);
        MyApplication.getInstance().exit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            CommFunc.PrintLog(5, LOGTAG, "KEYCODE_BACK() moveTaskToBack");
            moveTaskToBack(true);
            return true;//后台运行
        } 
        return super.onKeyDown(keyCode, event);
    }
    
    public final static int MSG_GETUSERLIST = 1000;
    public void getUserList()
    {
        HBaseApp.post2WorkRunnable(new Runnable(){
            @Override
            public void run() {
                
                HBaseApp.post2WorkRunnable(new Runnable()
                {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        JSONObject jsonobj = new JSONObject();
                        try {
                            String time = MyApplication.getInstance().getSharePrefValue(MsgKey.key_getuserlist_time, "0");
                            CommFunc.PrintLog(5, LOGTAG, "getuserlist:"+time);
                            jsonobj.put("time", time);
                            HttpResult httpret = AppHttpUtils.getInstance().apppost(AppHttpUtils.getlisturl, jsonobj.toString());
                            Message msg = new Message();
                            msg.what = MSG_GETUSERLIST;
                            msg.obj = httpret;
                            myhandler.sendMessage(msg);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } 
                    }
                    
                });
                // TODO Auto-generated method stub

            }
            });
     }
    Handler myhandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch(msg.what)
            {
                case MSG_GETUSERLIST: 
                {   
                    CommFunc.PrintLog(5, LOGTAG, "handleMessage MSG_GETUSERLIST");
                     final HttpResult result =(HttpResult) msg.obj;
                    HBaseApp.post2WorkRunnable(new Runnable(){
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            disposeGetUserList(result);
                        }                       
                    });
                }
                break;
                case MSG_CHECKUPDATE:
                    OnDisCheckUpdate((HttpResult)msg.obj);
                    break;
                default:
                break;
            }
            super.handleMessage(msg);
            
        }
    };
    private void disposeGetUserList(HttpResult result)
    {      
      if(result.getStatus() == HttpURLConnection.HTTP_OK)
      {
          String str = result.getObject().toString();
          CommFunc.PrintLog(5, LOGTAG, "disposeGetUserList HTTP_OK:"+str); 

          try {
            JSONObject jsonobj = new JSONObject(str);
            String errcode = jsonobj.getString("errorcode");
            String errmsg = jsonobj.getString("errormessage");
            if(errcode!=null && errcode.equals("0"))
            {
                String oldtime = MyApplication.getInstance().getSharePrefValue(MsgKey.key_getuserlist_time, "0");
                String time = jsonobj.getString("time");
                CommFunc.PrintLog(5, LOGTAG, "saveSharePrefValue getuserlist oldtime:"+oldtime+ " newtime:"+time);

                if((oldtime.equals(time)|| time.length()==0)&& bShowToast)
                {
                    CommFunc.DisplayToast(MainActivity.this,"您当前已经是最新数据!");
                    return;
                }
                MyApplication.getInstance().saveSharePrefValue(MsgKey.key_getuserlist_time, time);
                JSONArray arr = jsonobj.getJSONArray("resultlist"); 
                if(arr!=null )
                {
                    if(bShowToast)
                    CommFunc.DisplayToast(MainActivity.this, "获取天翼VV好友成功!");
                    List<TContactInfo> listAcc = new ArrayList<TContactInfo>();
                    for(int i=0;i<arr.length();i++)
                    {
                        TContactInfo info= new TContactInfo();
                        JSONObject obj = arr.getJSONObject(i);
                        String uuid = obj.getString("uuid");
                        String username = obj.getString("nick");
                        int type = SysConfig.USERTYPE_TIANYI;
                        if(obj.has("type"))
                            type= obj.getInt("type"); 
                        String[] pinyin = PinYinManager.toPinYin(username);
                        info.setFirstChar(pinyin[0]);
                        info.setLookUpKey(pinyin[1]);
                         //判断是否数据库表已经保存了此账号信息（）
                        info.setUsertype(type);
                        info.setContactId(uuid);
                        info.setName(username);
                        info.setPhoneNum(uuid);
                       // CommFunc.PrintLog(5, LOGTAG, "disposeGetUserList name:"+ username +"uuid:"+uuid+"  type:"+type);
                        listAcc.add(info);      
                    }
                    //保存用户信息
                    SQLiteManager.getInstance().save_updateContactList(listAcc);
                }
            }   
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
      }
      else
      {
          CommFunc.PrintLog(5, LOGTAG, "disposeGetUserList failed: error"+result.getErrorMsg()+"status:"+result.getStatus()); 
      } 
    }
    private final int MSG_CHECKUPDATE = 1002;
    public boolean bHandUpdate = false; //是否为手动升级
    public void CheckUpdate()
    {  
        CommFunc.PrintLog(5, LOGTAG, "CheckUpdate");
        HBaseApp.post2WorkRunnable(new Runnable(){

            @Override
            public void run() {
                // TODO Auto-generated method stub
                JSONObject jsonobj = new JSONObject();
                try {
                    String version = MyApplication.getInstance().getAppVersionName();
                    jsonobj.put("version", version);
                    HttpResult ret = AppHttpUtils.getInstance().apppost(AppHttpUtils.updateurl, jsonobj.toString());
                    CommFunc.PrintLog(5, LOGTAG, "CheckUpdate version:"+version);
                   Message msg = new Message();
                   msg.obj = ret;
                   msg.what = MSG_CHECKUPDATE;
                   myhandler.sendMessage(msg);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } 
            }
        });
    }
   // public static Context curContext = null;
    private String downapkurl;
    
    private void OnDisCheckUpdate(HttpResult ret)
    {
        
        CommFunc.PrintLog(5, LOGTAG, "OnDisCheckUpdate sResp  getStatus:"+ret.getStatus());
        if(ret.getStatus()==HttpURLConnection.HTTP_OK)
        {
            String sResp = (String)ret.getObject();
            CommFunc.PrintLog(5, LOGTAG, "OnDisCheckUpdate sResp:"+sResp  +"  needupdate:"+bHandUpdate);
            try {
                if(sResp!=null && sResp.equals("")==false)
                {
                    JSONObject obj = new JSONObject(sResp); 
                    int needupdate =  obj.getInt("needupdate");
                    String newVersion = obj.getString("ver");
                    String updateurl = obj.getString("updateurl");
                    String desc = obj.getString("desc");
                    int errorcode = obj.getInt("errorcode");
                    String errormessage = obj.getString("errormessage");
                    downapkurl = updateurl;
                     if(needupdate==1)
                    {
                       MyAutoUpdate myupdate= new MyAutoUpdate(MainActivity.this);
                      //  String str = "系统检测到了天翼VV发布了\r\n最新版本,请及时更新！\r\n版本号:"+newVersion;
                        //showUpdateDialog(str,updateurl);
                        myupdate.setDownLoadInfo(updateurl, newVersion,desc);
                        if(myupdate.check())
                            myupdate.showUpdateDialog();
                        
//                        DownloadProgress.getInstance().addDownLoadApp(updateurl);
//                        DownloadProgress.getInstance().setPercent(downapkurl, 0);
//                        DownLoadApkAsyncTask async = new DownLoadApkAsyncTask(updateurl,newVersion,newVersion, handler);
//                        async.execute("0");// 执行doInBackground方法
                    }
                    else if(bHandUpdate)
                    {
                        //如果为手动升级需要提示当前已经是最新版本
                        if(errorcode==0)
                        {  
                            CommFunc.DisplayToast(MainActivity.this, "当前已经是最新版本");
                        }
                        else 
                        {
                            CommFunc.DisplayToast(MainActivity.this, "升级失败 :"+errormessage+" code["+errorcode+"]");
                        }
                        
                    }
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
 
    }
}
