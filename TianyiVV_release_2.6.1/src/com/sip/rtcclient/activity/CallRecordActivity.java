package com.sip.rtcclient.activity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

import jni.sip.Call;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.sip.rtcclient.HBaseApp;
import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.calling.CallingActivity;
import com.sip.rtcclient.adapter.CallRecordAdapter;
import com.sip.rtcclient.bean.TCallRecordInfo;
import com.sip.rtcclient.config.MsgKey;
import com.sip.rtcclient.db.SQLiteManager;
import com.sip.rtcclient.utils.CommFunc;

public class CallRecordActivity extends BaseActivity implements
Observer, TextWatcher, OnItemClickListener, OnClickListener,
OnLongClickListener {

    public final static String BROADCAST_DISMISS_DIALPAD = "com.sim.rtcclient.dismiss.dialpad";

    private ListView listView;
    private CallRecordAdapter adapter;
    private List<TCallRecordInfo> list;

    private EditText searchText; // 搜索
    //wwyue
    private boolean dialpadFlag = true; // 标识键盘是否显示

    private LinearLayout dialpadLayout;
    private ImageView img_clear;
    private EditText et_dial;
    private ImageButton img_0;
    private ImageButton img_1;
    private ImageButton img_2;
    private ImageButton img_3;
    private ImageButton img_4;
    private ImageButton img_5;
    private ImageButton img_6;
    private ImageButton img_7;
    private ImageButton img_8;
    private ImageButton img_9;
    private ImageButton img_pound;
    private ImageButton img_star;

    private static boolean mDTMFToneEnabled;
    private ToneGenerator mToneGenerator; // 声音产生器
    private Object mToneGeneratorLock = new Object(); // 监视器对象锁
    private static final int DTMF_DURATION_MS = 120; // 声音的播放时间
    private int edit_position;
    private String LOGTAG = "CallRecordActivity";

    private static final HashMap<Character, Integer> mToneMap = new HashMap<Character, Integer>();

    private static final HashMap<Integer, Character> mDisplayMap = new HashMap<Integer, Character>();

    static {
        mToneMap.put('1', ToneGenerator.TONE_DTMF_1);
        mToneMap.put('2', ToneGenerator.TONE_DTMF_2);
        mToneMap.put('3', ToneGenerator.TONE_DTMF_3);
        mToneMap.put('4', ToneGenerator.TONE_DTMF_4);
        mToneMap.put('5', ToneGenerator.TONE_DTMF_5);
        mToneMap.put('6', ToneGenerator.TONE_DTMF_6);
        mToneMap.put('7', ToneGenerator.TONE_DTMF_7);
        mToneMap.put('8', ToneGenerator.TONE_DTMF_8);
        mToneMap.put('9', ToneGenerator.TONE_DTMF_9);
        mToneMap.put('0', ToneGenerator.TONE_DTMF_0);
        mToneMap.put('#', ToneGenerator.TONE_DTMF_P);
        mToneMap.put('*', ToneGenerator.TONE_DTMF_S);

        mDisplayMap.put(R.id.one, '1');
        mDisplayMap.put(R.id.two, '2');
        mDisplayMap.put(R.id.three, '3');
        mDisplayMap.put(R.id.four, '4');
        mDisplayMap.put(R.id.five, '5');
        mDisplayMap.put(R.id.six, '6');
        mDisplayMap.put(R.id.seven, '7');
        mDisplayMap.put(R.id.eight, '8');
        mDisplayMap.put(R.id.nine, '9');
        mDisplayMap.put(R.id.zero, '0');
        mDisplayMap.put(R.id.pound, '#');
        mDisplayMap.put(R.id.star, '*');
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yishitong);
        configTone();
        initView();
        initData();
        initViewsDialpad();
        registerReceiver(receiver, new IntentFilter(BROADCAST_DISMISS_DIALPAD));
        //wwyue默认显示拨号盘
        dialpadLayout.setVisibility(View.VISIBLE);
    }

    private void configTone() {
        try {
            // 获取系统参数“按键操作音”是否开启
            mDTMFToneEnabled = Settings.System.getInt(getContentResolver(),
                    Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;
            synchronized (mToneGeneratorLock) {
                if (mDTMFToneEnabled && mToneGenerator == null) {
                    mToneGenerator = new ToneGenerator(
                            AudioManager.STREAM_DTMF, 40); // 设置声音的大小
                    setVolumeControlStream(AudioManager.STREAM_DTMF);
                }
            }
        } catch (Exception e) {

            mDTMFToneEnabled = false;
            mToneGenerator = null;
        }
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.yishitong_listview);
        searchText = (EditText) findViewById(R.id.yishitong_search_txt);
        dialpadLayout = (LinearLayout) findViewById(R.id.dialpad_layout);

        listView.setOnScrollListener(loadListener);
        listView.setOnItemClickListener(this);

        listView.setOnCreateContextMenuListener(onCreateContextMenuListener);
    }

    private void initData() {

        // TODO 需要更改
        list = SQLiteManager.getInstance().getCallRecordInfo(MyApplication.getInstance().getUserID());
        adapter = new CallRecordAdapter(getApplication(), list);
        listView.setAdapter(adapter);
    }

    private void initViewsDialpad() {
        img_clear = (ImageView) findViewById(R.id.img_clear);
        et_dial = (EditText) findViewById(R.id.et_dial);
        // 字体加粗
        TextPaint paint = et_dial.getPaint();
        paint.setFakeBoldText(true);
        // 设置光标不显示,但不能设置光标颜色
        et_dial.setCursorVisible(false);
        et_dial.setInputType(InputType.TYPE_NULL);
        et_dial.setOnClickListener(this);
        img_clear.setOnClickListener(this);
        img_clear.setOnLongClickListener(this);
        img_0 = (ImageButton) findViewById(R.id.zero);
        img_1 = (ImageButton) findViewById(R.id.one);
        img_2 = (ImageButton) findViewById(R.id.two);
        img_3 = (ImageButton) findViewById(R.id.three);
        img_4 = (ImageButton) findViewById(R.id.four);
        img_5 = (ImageButton) findViewById(R.id.five);
        img_6 = (ImageButton) findViewById(R.id.six);
        img_7 = (ImageButton) findViewById(R.id.seven);
        img_8 = (ImageButton) findViewById(R.id.eight);
        img_9 = (ImageButton) findViewById(R.id.nine);
        img_pound = (ImageButton) findViewById(R.id.pound);
        img_star = (ImageButton) findViewById(R.id.star);
        et_dial.addTextChangedListener(this);
        img_0.setOnClickListener(onClickListener);
        img_1.setOnClickListener(onClickListener);
        img_2.setOnClickListener(onClickListener);
        img_3.setOnClickListener(onClickListener);
        img_4.setOnClickListener(onClickListener);
        img_5.setOnClickListener(onClickListener);
        img_6.setOnClickListener(onClickListener);
        img_7.setOnClickListener(onClickListener);
        img_8.setOnClickListener(onClickListener);
        img_9.setOnClickListener(onClickListener);
        img_pound.setOnClickListener(onClickListener);
        img_star.setOnClickListener(onClickListener);
        et_dial.setLongClickable(false); // 屏蔽长按时出现全选剪切菜单

    }

    /**
     * 
     */
    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mDisplayMap.containsKey(v.getId())) {
                playtone(v.getId());
                et_dial.requestFocus();
                StringBuffer dial = new StringBuffer(et_dial.getText()
                        .toString().trim());
                edit_position = et_dial.getSelectionEnd();// 光标的结束为止添加
                edit_position++;
                et_dial.setText(dial.insert(edit_position - 1,
                        mDisplayMap.get(v.getId())));
            }
        }
    };

    /**
     * 
     */
    OnCreateContextMenuListener onCreateContextMenuListener = new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(getString(R.string._call_record_del));
            menu.add(0, 1, 0, getString(R.string._group_more_delete_ok));
        }
    };

    /**
     * TODO 暂未添加
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
    	String callId = list.get(position).getCallRecordId();
        switch (item.getItemId()) {
            case 1:	//删除一条通话记录
                SQLiteManager.getInstance().delCallRecordInfo(callId, true);
            	//CommFunc.DisplayToast(getApplicationContext(), "删除通话记录功能暂未开放");
                break;
            case 3:
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * 
     * @param id
     */
    protected void playtone(int id) {
        int tone = mToneMap.get(mDisplayMap.get(id));
        if (!mDTMFToneEnabled) {
            return;
        }

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode();
        if (ringerMode == AudioManager.RINGER_MODE_SILENT
                || ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            // 静音或者震动时不发出声音
            return;
        }
        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                return;
            }
            mToneGenerator.startTone(tone, DTMF_DURATION_MS); // 发出声音
        }
    }

    /**
     * 呼叫-右上角从右面数第一个图标
     * @param view
     */
    public void onCall(View view){
        if (dialpadFlag) { // 收起键盘
            dialpadLayout.setVisibility(View.GONE);
            dialpadFlag = false;
        }else {
            dialpadLayout.setVisibility(View.VISIBLE);
            dialpadFlag = true;
        }
    }

    /**
     * 添加好友-右上角从右面数第二个图标
     * @param view
     */
    public void onFriendAdd(View view){
        Intent intent = new Intent(this, AddAddressActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void onClick(View v) {
        String numberString = "";
        switch (v.getId()) {
            case R.id.img_clear:
                numberString = et_dial.getText().toString().trim();
                if (numberString != null && !numberString.equals("")) {
                    edit_position = et_dial.getSelectionStart();
                    StringBuffer string = new StringBuffer(numberString);
                    if (edit_position > 0) {
                        edit_position--;
                        string.deleteCharAt(edit_position);
                    }
                    et_dial.setText(string);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 获取listview滚动监听，当滚动时隐藏拨号盘页面
     */
    OnScrollListener loadListener = new OnScrollListener() {
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                int visibleItemCount, int totalItemCount) {
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // listview滚动时会执行这个方法，这儿调用加载数据的方法。
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        if (dialpadFlag) { // 收起键盘
            dialpadLayout.setVisibility(View.GONE);
            dialpadFlag = false;
            return;
        }else {
            TCallRecordInfo info = list.get(position);
            Intent intent = new Intent(this, ContactCallDetailActivity.class);
            intent.putExtra(MsgKey.intent_key_object, info);
            startActivity(intent);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        et_dial.requestFocus();
        if (edit_position < 0) {
            et_dial.setSelection(0);
        } else {
            et_dial.setSelection(edit_position);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO Auto-generated method stub

    }

    /**
     * 
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BROADCAST_DISMISS_DIALPAD)) {
                dialpadFlag = !dialpadFlag;
                if (dialpadFlag) {
                    dialpadLayout.setVisibility(View.VISIBLE);
                } else {
                    dialpadLayout.setVisibility(View.GONE);
                }
            }
        }
    };

    /**
     * 音频呼叫
     * @param view
     */
    public void onAudio(View view) {
        String callNumber = et_dial.getText().toString().trim();
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
            am.set(AlarmManager.RTC_WAKEUP,100, pendingIntent);
        }
    }

    /**
     * 视频呼叫
     * @param view
     */
    public void onVideo(View view) {
        String callNumber = et_dial.getText().toString().trim();
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
            am.set(AlarmManager.RTC_WAKEUP,100, pendingIntent);        }
    }

    /**
     * 判断号码是否满足呼叫条件
     * TODO需要更改
     * @param numberString
     * @return
     */
    private boolean checkCall(String numberString){
        if (numberString == null || numberString.equals("")) {
            CommFunc.DisplayToast(CallRecordActivity.this,R.string._calling_number_null);
            return false;
        } else {
            if (checkNet()) {
                if (numberString.contains("*")) {// 判断特殊字符的电话逻辑
                    return true;
                } else if (numberString.contains("#")) {
                    int index = numberString.indexOf("#");
                    if (index > 0) {
                        numberString = numberString.substring(0, index);
                    }
                }
                if (numberString.equals(MyApplication
                        .getInstance().getAccountInfo().getUserid())) {
                    CommFunc.DisplayToast(CallRecordActivity.this,R.string._calling_cannot_dial_self);
                    return false;
                } else {
                    edit_position = 0;
                    et_dial.setText("");                  
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
        info.setFromUser(MyApplication.getInstance().getAccountInfo().getUserid()); // TODO 需要更改
        info.setToUser(callNumber);
        info.setType(callType);
        info.setResult(TCallRecordInfo.CALL_RESULT_SUCCESS);
        info.setDirection(TCallRecordInfo.CALL_DIRECTION_OUT);
        SQLiteManager.getInstance().saveCallRecordInfo(info, true);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data != null && adapter != null && data instanceof TCallRecordInfo) {
            HBaseApp.post2WorkRunnable(new Runnable(){

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    list = SQLiteManager.getInstance().getCallRecordInfo(MyApplication.getInstance().getUserID());
                    CommFunc.PrintLog(5, LOGTAG, "update callrecordsize:"+list.size());
  
                }
                
            });
             adapter.setList(list);
            adapter.notifyDataSetChanged();
        }
    }

}
