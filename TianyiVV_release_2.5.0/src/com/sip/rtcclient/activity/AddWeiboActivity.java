package com.sip.rtcclient.activity;

import java.util.List;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.adapter.AddContactAdapter;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.ui.TitleViewSimple;
import com.sip.rtcclient.ui.TitleViewSimple.OnSimpleTitleActed;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class AddWeiboActivity extends Activity implements OnSimpleTitleActed {
	private TitleViewSimple titleView;
	private ListView listView;
	private AddContactAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_address);
		initViews();
		initListView();
	}
	private void initViews() {
		// TODO Auto-generated method stub
		titleView = (TitleViewSimple) findViewById(R.id.titleView);
		
		titleView.setTitle(R.drawable.btn_arr_left, -1,
				getString(R.string.title_add_weibo));
		titleView.setOnTitleActed(this);
		listView = (ListView) findViewById(R.id.listview);
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
	private void initListView() {
		// TODO Auto-generated method stub
		adapter = new AddContactAdapter(AddWeiboActivity.this, getContact());
		listView.setAdapter(adapter);
	}
	public List<TContactInfo> getContact() {
		// Uri uri=ContactsContract.Data.CONTENT_URI;
		// Cursor cursor=getContentResolver().query(uri, null, null, null,
		// "display_name");
		// cursor.moveToFirst();
	    
	    
//		List<TContactInfo> list = new ArrayList<TContactInfo>();
//		for (int i = 0; i < 5; i++) {
//			TContactInfo info = new TContactInfo();
//			info.setName("测试" + i);
//			info.setFirstChar("c");
//			info.setUsertype(2);
//			info.setPhoneType(2);
//			list.add(info);
//		}
	    
	    
//		for (int i = 0; i < 5; i++) {
//			ContactInfo info = new ContactInfo();
//			info.setName("Group" + i);
//			info.setFirstChar("g");
//			info.setType(2);
//			info.setPhoneType(2);
//			list.add(info);
//		}
		
		
		return null;
	}

}
