package com.sip.rtcclient.activity;

import java.util.Calendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

import jni.sip.Call;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.calling.CallingActivity;
import com.sip.rtcclient.adapter.ContactDetailCallAdapter;
import com.sip.rtcclient.bean.TCallRecordInfo;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.config.MsgKey;
import com.sip.rtcclient.config.SysConfig;
import com.sip.rtcclient.db.SQLiteManager;
import com.sip.rtcclient.ui.TitleViewTianyi;
import com.sip.rtcclient.ui.TitleViewTianyi.OnTianyiTitleActed;
import com.sip.rtcclient.utils.CommFunc;


public class ContactCallDetailActivity extends BaseActivity implements OnTianyiTitleActed, Observer{

    private TitleViewTianyi titleView;
    private ListView listView;
    private ContactDetailCallAdapter adapter;
    private List<TCallRecordInfo> list;	
    private TextView userName;	
    private String fromUser;
    private String toUser;
    private TCallRecordInfo callrecordinfo= null;
    private ImageView img_tianyi,img_weibo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_call_detail);
        getExtras();
        initView();
        initData();
    }

    /**
     * 获取参数
     */
    private void getExtras() {
    	Bundle bundle = getIntent().getExtras();
    	if (bundle != null) {
    		callrecordinfo = (TCallRecordInfo)getIntent().getSerializableExtra(MsgKey.intent_key_object);
		}
    }

    /**
     * 
     */
    private void initView() {
        titleView = (TitleViewTianyi) findViewById(R.id.titleView);
        userName = (TextView) findViewById(R.id.contact_detail_name);
        listView = (ListView) findViewById(R.id.contact_detail_listview);

        titleView.setOnTitleActed(this);
        listView.setOnCreateContextMenuListener(onCreateContextMenuListener);
        img_tianyi = (ImageView)findViewById(R.id.contact_detail_tianyi);
        img_weibo = (ImageView)findViewById(R.id.contact_detail_sina);
    }

    /**
     * 
     */
    private void initData() {
        CommFunc.PrintLog(5, "ContactCallDetailActivity", "initData");

        fromUser =  callrecordinfo.getFromUser();
        toUser = callrecordinfo.getToUser();
        titleView.setTitle(-1, getString(R.string._contact_detail_call_title));
        
        TContactInfo info = null;
        if (fromUser.equals(SysConfig.userid)) {
			info = SQLiteManager.getInstance().getContactInfoByNumber(toUser);
		}else {
			info = SQLiteManager.getInstance().getContactInfoByNumber(fromUser);
		}
        CommFunc.PrintLog(5, "ContactCallDetailActivity", "initData  info:"+info);
        if (info != null) {
			userName.setText(info.getName());
		    int usertype = info.getUsertype();
		    CommFunc.PrintLog(5, "ContactCallDetailActivity", "info != null name:"+userName +"  type:"+usertype );
		    if(usertype == SysConfig.USERTYPE_TIANYI)
		    {
		        img_tianyi.setVisibility(View.VISIBLE);
		    }
		    else if(usertype == SysConfig.USERTYPE_WEIBO)
		        img_weibo.setVisibility(View.VISIBLE);
		        
		}else {
        userName.setText(fromUser.equals(SysConfig.userid)?toUser:fromUser);
		}
        

        list = SQLiteManager.getInstance().getCallRecordInfo(fromUser, toUser);
        adapter = new ContactDetailCallAdapter(getApplication(), list);
        listView.setAdapter(adapter);
    }

    @Override
    public void onClickLeftButton() {
        finish();
    }

    @Override
    public void onClickRightButton() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        SQLiteManager.getInstance().addObserver(this);
    }
    
    @Override
	protected void onDestroy() {
		SQLiteManager.getInstance().deleteObserver(this);
		super.onDestroy();
	}

    @Override
    public void update(Observable observable, Object data) {
        if (data != null && adapter != null) {
            list = SQLiteManager.getInstance().getCallRecordInfo(fromUser, toUser);
            adapter.setList(list);
            adapter.notifyDataSetChanged();
        }
    }
    
    /**
     * 
     */
    OnCreateContextMenuListener onCreateContextMenuListener = new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(getString(R.string._call_record_del));
            menu.add(0, 1, 0, "删除");
        }
    };

    /**
     * TODO 暂未添加
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                //删除通话记录
                CommFunc.DisplayToast(getApplicationContext(), "删除通话记录功能暂未开放");
                break;
            case 3:
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * 音频呼叫
     * @param view
     */
    public void onAudio(View view) {
    	String userid = MyApplication.getInstance().getUserID();
    	String callNumber = fromUser.equals(userid)?toUser:fromUser;
		if (checkCall(callNumber)) 
		{
			String callRecordId = UUID.randomUUID().toString();
			saveCallRecordInfo(callRecordId, callNumber, Call.CT_Audio);
			
			Intent intent = new Intent(this,CallingActivity.class);
			AlarmManager am = (AlarmManager) MyApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
			intent.putExtra("callNumber",callNumber);
			intent.putExtra("inCall", false);
			intent.putExtra("isVideo", false);
			intent.putExtra("callRecordId", callRecordId);
			PendingIntent pendingIntent = PendingIntent.getActivity(MyApplication.getInstance(), 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            long time = Calendar.getInstance().getTimeInMillis();
            CommFunc.PrintLog(5, LOGTAG, "pendingIntent time:"+time);
            am.set(AlarmManager.RTC_WAKEUP,100, pendingIntent);		}
    }

    /**
     * 视频呼叫
     * @param view
     */
    public void onVideo(View view) {
    	String userid = MyApplication.getInstance().getUserID();
    	String callNumber = fromUser.equals(userid)?toUser:fromUser;
		if (checkCall(callNumber)) {
			String callRecordId = UUID.randomUUID().toString();
			saveCallRecordInfo(callRecordId, callNumber, Call.CT_AudioVideo);
			
			Intent intent = new Intent(this,CallingActivity.class);
			AlarmManager am = (AlarmManager) MyApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
			intent.putExtra("callNumber",callNumber);
			intent.putExtra("inCall", false);
			intent.putExtra("isVideo", true);
			intent.putExtra("callRecordId", callRecordId);
			PendingIntent pendingIntent = PendingIntent.getActivity(MyApplication.getInstance(), 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            long time = Calendar.getInstance().getTimeInMillis();
            CommFunc.PrintLog(5, LOGTAG, "pendingIntent time:"+time);
            am.set(AlarmManager.RTC_WAKEUP,100, pendingIntent);		}
    }
    
    /**
	 * 判断号码是否满足呼叫条件
	 * TODO需要更改
	 * @param numberString
	 * @return
	 */
    private boolean checkCall(String numberString){
        if (numberString == null || numberString.equals("")) {
            CommFunc.DisplayToast(this,R.string._calling_number_null);
            return false;
        } else {
            if (checkNet()) {
                if (numberString.equals(MyApplication.getInstance().getUserID())) {
                    CommFunc.DisplayToast(this,R.string._calling_cannot_dial_self);
                    return false;
                }
                return true;
            }else {
                return false;
            }
        }
    }

    /**
     * 入库操作
     * @param toUser
     * @param callType
     */
    private void saveCallRecordInfo(String callRecordId, String callNumber, int callType) {
        TCallRecordInfo info = new TCallRecordInfo();
        info.setCallRecordId(callRecordId);
        info.setDate(CommFunc.getStartDate());
        info.setStartTime(CommFunc.getStartTime());
        info.setEndTime("");
        info.setTotalTime("");
        String userid = MyApplication.getInstance().getUserID();
        info.setFromUser(userid); // TODO 需要更改
        info.setToUser(callNumber);
        info.setType(callType);
        info.setResult(TCallRecordInfo.CALL_RESULT_SUCCESS);
        info.setDirection(TCallRecordInfo.CALL_DIRECTION_OUT);
        SQLiteManager.getInstance().saveCallRecordInfo(info, true);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
    	return super.onKeyDown(keyCode, event);
    }

}