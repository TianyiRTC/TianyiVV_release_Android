package com.sip.rtcclient.activity.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.BaseActivity;
import com.sip.rtcclient.adapter.ContactAdapter;
import com.sip.rtcclient.adapter.ContactAdapter.OnContactSelectListener;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.config.SysConfig;
import com.sip.rtcclient.db.SQLiteManager;
import com.sip.rtcclient.ui.TitleViewTianyi;
import com.sip.rtcclient.ui.TitleViewTianyi.OnTianyiTitleActed;
import com.sip.rtcclient.utils.CommFunc;

/**
 * 创建群组选择、添加联系人Activity
 * @author ThinkPad
 *
 */
public class SelectContactActivity extends BaseActivity implements OnTianyiTitleActed, OnContactSelectListener, TextWatcher {
	
	private TitleViewTianyi titleView;
	
	private ListView listView;
	private ContactAdapter adapter;
	private Map<Integer, TContactInfo> contactIdMap = null;
	private AsyncQueryHandler asyncQuery;
	private List<TContactInfo> list;
	private TextView txtNone;
	private EditText searchText;
	private RelativeLayout layoutBottom;
	private TextView txtCount;
	private List<String> contactList = new ArrayList<String>();
	private String[] els;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_select);
		asyncQuery = new MyAsyncQueryHandler(getContentResolver());
		getExtras();
		initView();
		initListView();
	}
	
	/*
	 * 获取Extras
	 */
	private void getExtras() {
		Bundle bundle =getIntent().getExtras();
		if (bundle != null) {
			els = bundle.getStringArray("el");
		}
	}

	/*
	 * 初始化View
	 */
	private void initView() {
		titleView=(TitleViewTianyi)findViewById(R.id.titleView);
		listView = (ListView) findViewById(R.id.listview);
		txtNone = (TextView) findViewById(R.id.select_contact_none);
		layoutBottom = (RelativeLayout) findViewById(R.id.group_create_sel_members_ok_layout);
		txtCount = (TextView) findViewById(R.id.group_create_sel_members_count_txt);
		
		titleView.setTitle(-1, getString(R.string._group_create_sel_members_title));
		titleView.setOnTitleActed(this);
		searchText = (EditText) findViewById(R.id.et_search);
		searchText.addTextChangedListener(this);
	}

	/**
	 * 初始化ListView
	 */
	private void initListView() {
		if (els != null) {
			StringBuffer members = new StringBuffer();
			for (int i = 0; i < els.length; i++) {
				if (i== els.length -1) {
					members.append(els[i]);
				}else {
					members.append(els[i]).append(",");
				}
			}
			list = SQLiteManager.getInstance().getContactInfo(members.toString());
		} else {
			list = SQLiteManager.getInstance().getAllContactInfo();
		}
		if (list.size() > 0) {
			setAdapter(list);
		}else {
			listView.setVisibility(View.GONE);
			txtNone.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 获取系统通讯录
	 * 
	 * @return
	 */
	public void getContact() {
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
		String[] projection = { 
				ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1,"sort_key",
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
		}; // 查询的列
		asyncQuery.startQuery(0, null, uri, projection, null, null,
				"sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询
		
	}
	private class MyAsyncQueryHandler extends AsyncQueryHandler {

		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}

		/**
		 * 查询结束的回调函数
		 */
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor != null && cursor.getCount() > 0) {
				
				contactIdMap = new HashMap<Integer, TContactInfo>();
				
				list = new ArrayList<TContactInfo>();
				cursor.moveToFirst();
				for (int i = 0; i < cursor.getCount(); i++) {
					cursor.moveToPosition(i);
					String name = cursor.getString(1);
					String number = cursor.getString(2);
					String sortKey = cursor.getString(3);
					int contactId = cursor.getInt(4);
					Long photoId = cursor.getLong(5);
					String lookUpKey = cursor.getString(6);

					if (contactIdMap.containsKey(contactId)==false) {
						
						TContactInfo cb = new TContactInfo();
						cb.setName(name);
                        if(number!=null)
                        {   
                            CommFunc.PrintLog(5, LOGTAG, "number have space before:"+number );
                            number = number.replace(" ", "");
                            CommFunc.PrintLog(5, LOGTAG, "number have space after:"+number );
                        }
                        if (number.startsWith("+86")) {// 去除多余的中国地区号码标志，对这个程序没有影响。
                            cb.setPhoneNum(number.substring(3));
                            cb.setContactId(number.substring(3));
                            
                        } else {
                            cb.setPhoneNum(number);
                            cb.setContactId(number);
                        }
						cb.setFirstChar(sortKey);
						cb.setPhotoId(photoId);
						cb.setLookUpKey(lookUpKey);
						cb.setUsertype(SysConfig.USERTYPE_SYSTEM);
						list.add(cb);
						
						contactIdMap.put(contactId, cb);
						
					}
				}
				if (list.size() > 0) {
					setAdapter(list);
				}
			}
			if(cursor!=null)
			{
			    cursor.close();
			    cursor = null;
			}
		}

	}

	/**
	 * 
	 * @param list
	 */
	private void setAdapter(List<TContactInfo> list) {
		adapter = new ContactAdapter(this, list, true);
		adapter.setOnConfSelectListener(this);
		listView.setAdapter(adapter);
	}
	
	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		if (adapter == null)
			return;
		search(s.toString().trim());
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
	 * 根据条件搜索联系人
	 * 
	 * @param text
	 */
	private void search(String text) {

		// wwyue
		if (text.equals("")) {
			adapter.setList(list);
			adapter.notifyDataSetChanged();
			return;
		}

		List<TContactInfo> list = SQLiteManager.getInstance()
		.getContactInfo_Search(text);
		if (adapter != null) {
			adapter.setList(list);
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * 点击确定
	 * @param view
	 */
	public void onAddMembers(View view) {
		String el = "";
		for(int i=0,len = contactList.size();i<len;i++){   
			if (i == 0) 
				el = contactList.get(i);
			else 
				el += "," + contactList.get(i);
		}
		Intent intent = new Intent();
		intent.putExtra("el", el);
		setResult(RESULT_OK,intent);
		finish();
	}
	
	/**
	 * 更新底部确定按钮以及选中的人员数量
	 */
	private void updateView() {
		if (contactList.size() > 0) {
			layoutBottom.setVisibility(View.VISIBLE);
			txtCount.setText("(" + contactList.size() + ")");
		}else {
			txtCount.setText("");
			layoutBottom.setVisibility(View.GONE);
		}
	}
	
	@Override
	public boolean onAdd(String contactId) {
		boolean isAdd = contactList.add(contactId);
		updateView();
		return isAdd;
	}

	@Override
	public boolean onDelete(String contactId) {
		boolean isDelete = contactList.remove(contactId);
		updateView();
		return isDelete;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onClickLeftButton() {
		finish();
	}

	@Override
	public void onClickRightButton() {

	}
	
}
