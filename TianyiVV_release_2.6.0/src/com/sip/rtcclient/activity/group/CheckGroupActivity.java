package com.sip.rtcclient.activity.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.BaseActivity;
import com.sip.rtcclient.activity.calling.CallingMultiChatActivity;
import com.sip.rtcclient.activity.calling.CallingMultiSpeakActivity;
import com.sip.rtcclient.activity.calling.CallingShowActivity;
import com.sip.rtcclient.activity.calling.CallingTVActivity;
import com.sip.rtcclient.activity.calling.CallingVideoConfActivity;
import com.sip.rtcclient.adapter.GroupRecordAdapter;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.bean.TGroupInfo;
import com.sip.rtcclient.bean.TGroupRecordInfo;
import com.sip.rtcclient.db.SQLiteManager;
import com.sip.rtcclient.ui.GroupNavigationView;
import com.sip.rtcclient.ui.GroupNavigationView.NavigationListener;
import com.sip.rtcclient.utils.CommFunc;
import com.sip.rtcclient.utils.ScreenUtil;

public class CheckGroupActivity extends BaseActivity implements NavigationListener, OnLongClickListener,  Observer {

	private ImageView img_avatar;
	private TextView tv_name;
	private ListView listView;
	private GroupRecordAdapter adapter;
	private GroupNavigationView navigationView;
	private List<TContactInfo> list;
	private List<TGroupRecordInfo> recordList;
	private TGroupInfo groupInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check_group);
		getExtras();
		initViews();
		initData();
		initListViews();
	}

	private void getExtras() {
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			groupInfo = (TGroupInfo) bundle.getSerializable("groupInfo");
		}
	}

	private void initListViews() {
		adapter = new GroupRecordAdapter(this, getList());
		listView.setAdapter(adapter);
	}

	private List<TGroupRecordInfo> getList() {
		if (groupInfo != null) {
			recordList = SQLiteManager.getInstance().getGroupRecordInfo(groupInfo.getGroupId());
		}
		return recordList; 
	}

	private void initViews() {
		img_avatar = (ImageView) findViewById(R.id.img_avatar);
		tv_name = (TextView) findViewById(R.id.tv_name);
		listView = (ListView) findViewById(R.id.listview);
		navigationView = (GroupNavigationView) findViewById(R.id.navigation_view);
		
		listView.setOnCreateContextMenuListener(onCreateContextMenuListener);
		
	}

	private void initData() {
		tv_name.setText(groupInfo.getGroupName());
		list = getContactInfo();
		configNavigationView();
	}

	private List<TContactInfo> getContactInfo() {
		List<TContactInfo> list = new ArrayList<TContactInfo>();
		if(groupInfo!=null && groupInfo.getGroupMembers()!=null || groupInfo.getGroupMembers().equals("")==false)
		{
		       String[] members = groupInfo.getGroupMembers().split(";");
		        for (int i = 0; i < members.length; i++) {
		            TContactInfo info = SQLiteManager.getInstance().getContactInfoById(
		                    members[i]);
		            if(info!=null){
	                    CommFunc.PrintLog(5, LOGTAG, "getContactInfo:" + info.getName());
	                    list.add(info);		                
		            }

		        } 
		}

		return list;
	}

	/**
	 * 配置导航菜单View
	 */
	private void configNavigationView() {
		int width = ScreenUtil.getScreenWidth(getApplicationContext());
		navigationView.setNumCoum(list.size()); // 设置导航菜单个数
		int weight = 88 * width / 480;
		// 配置导航菜单宽度,确保导航菜单充满屏幕且菜单项所占宽度平均
		if (list.size() * weight > width) {
			navigationView.setListWidth(list.size() * weight);
		} else {
			navigationView.setListWidth(weight * list.size());
			navigationView.visibleScalingBtn();
		}
		navigationView.refreshNavigationView(false); // 刷新导航菜单项
		navigationView.setOnNavigationListener(null);
		navigationView.cleanList(); // 刷新导航菜单项
		for (int i = 0; i < list.size(); i++) {
			navigationView.addNavigationCell(list.get(i));
		}
		navigationView.notityChange();
		navigationView.visibleScalingBtn();
	}

	/**
	 * 点击聊天室
	 * 
	 * @param view
	 */
	public void click_chatroom(View view) {
		CommFunc.PrintLog(5, LOGTAG, "聊天室click_chatroom");
		if(checkNet()==false)
		{
			return;
		}
		Intent intent = new Intent(this, CallingMultiChatActivity.class);//CallingChatRoomActivity
		intent.putExtra("groupInfo", groupInfo);
		intent.putExtra("inCall", false);
		startActivity(intent);
		CommFunc.PrintLog(5, LOGTAG, "click_chatroom");

	}

	/**
	 * 点击群对讲
	 * 
	 * @param view
	 */
	public void click_intercom(View view) {
		CommFunc.PrintLog(5, LOGTAG, "群对讲click_intercom");
		if(checkNet()==false)
		{
			return;
		}
		Intent intent = new Intent(this, CallingMultiSpeakActivity.class);
		intent.putExtra("groupInfo", groupInfo);
		intent.putExtra("inCall", false);
		startActivity(intent);
	}

	/**
	 * 点击vv秀场 多人两方语音
	 * 
	 * @param view
	 */
	public void click_show(View view) {
		CommFunc.PrintLog(5, LOGTAG, "VV秀场click_show");
		if(checkNet()==false)
		{
			return;
		}
		Intent intent = new Intent(this, CallingShowActivity.class);
		intent.putExtra("groupInfo", groupInfo);
		intent.putExtra("inCall", false);
		startActivity(intent);
	}

	/**
	 * 点击现场直播
	 * 
	 * @param view
	 */
	public void click_live(View view) {
		CommFunc.PrintLog(5, LOGTAG, "现场直播click_tvlive");
		if(checkNet()==false)
		{
			return;
		}
		Intent intent = new Intent(this, CallingTVActivity.class);
		intent.putExtra("groupInfo", groupInfo);
		intent.putExtra("inCall", false);
		startActivity(intent);
	}

	/**
	 * 点击视频会议
	 * 
	 * @param view
	 */
	public void click_vconf(View view) {
		CommFunc.PrintLog(5, LOGTAG, "视频会议click_vconf");
		if(checkNet()==false)
		{
			return;
		}
		Intent intent = new Intent(this, CallingVideoConfActivity.class);
		intent.putExtra("groupInfo", groupInfo);
		intent.putExtra("inCall", false);
		startActivity(intent);
	}

	@Override
	public void onClick(int requestCode, String tag) {
		CommFunc.PrintLog(5, LOGTAG, "onClick" + requestCode);
		list.remove(requestCode);
		configNavigationView();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
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
    
    /**
     * 长按菜单
     */
    OnCreateContextMenuListener onCreateContextMenuListener = new OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(getString(R.string._call_group_record_del));
            menu.add(0, 1, 0, getString(R.string._group_more_delete_ok));
        }
    };
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
    	String groupCallID = recordList.get(position).getCallId();
        switch (item.getItemId()) {
            case 1:	//删除一条群组通话记录
            	SQLiteManager.getInstance().deleteGroupRecordInfo(groupCallID, true);
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

	@Override
	public void update(Observable observable, Object data) {
		if (data != null && adapter != null && data instanceof TGroupRecordInfo) {
			recordList = SQLiteManager.getInstance().getGroupRecordInfo(groupInfo.getGroupId());
            adapter.setList(recordList);
            adapter.notifyDataSetChanged();
        }
	}

	@Override
	public boolean onLongClick(View v) {
		return false;
	}
}
