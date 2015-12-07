package com.sip.rtcclient.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.adapter.EditContactAdapter;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.config.SysConfig;
import com.sip.rtcclient.ui.TitleViewSimple;
import com.sip.rtcclient.ui.TitleViewSimple.OnSimpleTitleActed;

public class EditContactActivity extends BaseActivity implements OnSimpleTitleActed,OnItemClickListener,OnCheckedChangeListener {
	private TitleViewSimple titleView;
	private ListView listView;
	private EditContactAdapter adapter;
	@SuppressWarnings("unused")
	private WindowManager windowManager;
	@SuppressWarnings("unused")
	private Handler handler = new Handler();
	private Map<Integer, TContactInfo> contactIdMap = null;
	private AsyncQueryHandler asyncQuery;
	private List<TContactInfo> list;
	private CheckBox cb_all;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_contact);
		asyncQuery = new MyAsyncQueryHandler(getContentResolver());
		initView();
		initListView();
	}

	private void initListView() {
		// TODO Auto-generated method stub
		getContact();
		
	}

	private void initView() {
		// TODO Auto-generated method stub
		titleView = (TitleViewSimple) findViewById(R.id.titleView);
		titleView.setTitle(R.drawable.btn_arr_left, -1,
				getString(R.string.title_edit_contact));
		titleView.setOnTitleActed(this);
		listView = (ListView) findViewById(R.id.listview);
		listView.setOnItemClickListener(this);
		cb_all = (CheckBox) findViewById(R.id.cb_all);
		cb_all.setOnCheckedChangeListener(this);
	}

	@Override
	public void onClickLeftButton() {
		// TODO Auto-generated method stub
		finish();
	}

	@Override
	public void onClickRightButton() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 获取系统通讯录
	 * 
	 * @return
	 */
	public void getContact() {
		ArrayList<TContactInfo> list=new ArrayList<TContactInfo>();
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
		String[] projection = {
				ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1,
				"sort_key",
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
                          
                            number = number.replace(" ", "");
                            
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
	

	private void setAdapter(List<TContactInfo> list) {
		adapter = new EditContactAdapter(EditContactActivity.this, list);
		listView.setAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub
		if (adapter != null && adapter.getList() != null
				&& adapter.getList().size() > 0) {
			TContactInfo info = adapter.getList().get(position);
			String id = "";
			id=info.getPhoneNum();
			
			adapter.addSelect(id, info);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (isChecked == true) {
			adapter.selectAll();
		} else {
			adapter.deleteAll();
		}
	}
	
}
