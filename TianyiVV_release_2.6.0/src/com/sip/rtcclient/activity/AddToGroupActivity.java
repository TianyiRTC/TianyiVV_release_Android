package com.sip.rtcclient.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.adapter.GroupAdapter;
import com.sip.rtcclient.bean.TGroupInfo;
import com.sip.rtcclient.ui.TitleViewSimple;
import com.sip.rtcclient.ui.TitleViewSimple.OnSimpleTitleActed;

public class AddToGroupActivity extends BaseActivity implements OnItemClickListener,OnSimpleTitleActed{
	private TitleViewSimple titleView;
	private ListView listView;
	private GroupAdapter adapter;
	private String number;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_to_group);
		Bundle bundle=getIntent().getExtras();
		if(bundle!=null){
			number=bundle.getString("number");
		}
		initDatas();
	}
	private void initDatas() {
		// TODO Auto-generated method stub
		titleView=(TitleViewSimple)findViewById(R.id.titleView);
		listView=(ListView)findViewById(R.id.listview);
		titleView.setTitle(R.drawable.btn_arr_left, -1,
				getString(R.string.title_add_to_group));
		titleView.setOnTitleActed(this);
		adapter=new GroupAdapter(this, getList());
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
	}
	private List<TGroupInfo> getList(){
		List<TGroupInfo> list=new ArrayList<TGroupInfo>();
		for(int i=0;i<5;i++){
			TGroupInfo info=new TGroupInfo();
			info.setGroupId(""+i);
			info.setGroupName("分组"+i);
			list.add(info);
		}
		return list;
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Toast.makeText(this, number+"已加入到"+adapter.getItem(arg2).getGroupName(), Toast.LENGTH_LONG).show();
		finish();
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
}
