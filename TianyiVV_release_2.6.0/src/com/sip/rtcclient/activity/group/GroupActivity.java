package com.sip.rtcclient.activity.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.BaseActivity;
import com.sip.rtcclient.bean.TGroupInfo;
import com.sip.rtcclient.db.SQLiteManager;
import com.sip.rtcclient.ui.RoundAngleImageView;
import com.sip.rtcclient.ui.TitleViewSimple;
import com.sip.rtcclient.ui.TitleViewSimple.OnSimpleTitleActed;
import com.sip.rtcclient.utils.CommFunc;

/**
 * 群组Activity
 * @author Administrator
 *
 */
public class GroupActivity extends BaseActivity implements OnSimpleTitleActed, Observer{
	
	private String LOGTAG = "GroupActivity";
	
	private TitleViewSimple titleView;
	private TextView txt_none;
	private ListView listView;
	private List<TGroupInfo> list;
	private GroupAdapter adapter;
	
	private int currPostion;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group);
		initView();
		initData();
		confiMainView();
	}
	
	/**
	 * 初始化View
	 */
	private void initView() {
		titleView=(TitleViewSimple)findViewById(R.id.titleView);
		txt_none = (TextView) findViewById(R.id.activity_group_none);
		listView = (ListView) findViewById(R.id.activity_group_listview);
		
		titleView.setTitle(-1, R.drawable.btn_contact_add, getString(R.string.title_group));
		titleView.setOnTitleActed(this);
	}
	
	/**
	 * 初始化数据
	 */
	private void initData() {
		titleView.setTitle(-1, R.drawable.btn_contact_add, getString(R.string.title_group));
		titleView.setOnTitleActed(this);
		
		//TODO 测试我加入的群组,等服务端接口调试完毕，再删除此段代码
		/*TGroupInfo groupInfo = new TGroupInfo();
		groupInfo.setGroupId(String.valueOf(System.currentTimeMillis()));
		groupInfo.setGroupName("群组123");
		groupInfo.setGroupMembers("1111;2222;3333");
		groupInfo.setGroupPhoto(null);
		groupInfo.setGroupCreator("2222");
		groupInfo.setGroupCreateTime(String.valueOf(System.currentTimeMillis()));
		groupInfo.setGroupType(TGroupInfo.GROUP_TYPE_JOIN);
		SQLiteManager.getInstance().saveGroupInfo(groupInfo, true);*/
		
		list = SQLiteManager.getInstance().getAllGroupInfo();
		adapter = new GroupAdapter(list);
		listView.setAdapter(adapter);
	}
	
	/**
	 * 是否显示无群组的提示信息
	 */
	private void confiMainView(){
		if (list.size() > 0 && adapter != null) {
			txt_none.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}else {
			txt_none.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClickLeftButton() {
	}

	@Override
	public void onClickRightButton() {
		Intent intent = new Intent(this, GroupCreateActivity.class);
		startActivity(intent);
	}
	
	/**
	 * 进入群组详情界面
	 * @param groupId
	 */
	private void toGroupDetailActivity(String groupId) {
		TGroupInfo groupInfo = SQLiteManager.getInstance().getGroupById(groupId);
		Intent intent = new Intent(this, CheckGroupActivity.class);
		intent.putExtra("groupInfo", groupInfo);
	    CommFunc.PrintLog(5, LOGTAG, "toGroupDetailActivity: creator"+groupInfo.getGroupCreator());
		CommFunc.PrintLog(5, LOGTAG, "toGroupDetailActivity: members"+groupInfo.getGroupMembers());
		startActivity(intent);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		String groupId = list.get(currPostion).getGroupId();
		switch (item.getItemId()) {
		case 1:	//查看群组
			toGroupDetailActivity(groupId);
			break;
		case 2: // 删除群组
			SQLiteManager.getInstance().deleteGroupInfoById(groupId, true);
			break;
		case 3:	//屏蔽群组
			SQLiteManager.getInstance().updateGroupInfo(groupId, TGroupInfo._GROUP_SHIELD, TGroupInfo.GROUP_SHIELD_YES, true);
			CommFunc.DisplayToast(getApplicationContext(), getString(R.string._group_item_shield_success));
			break;
		case 4:	//取消屏蔽
			SQLiteManager.getInstance().updateGroupInfo(groupId, TGroupInfo._GROUP_SHIELD, TGroupInfo.GROUP_SHIELD_NO, true);
			break;
		case 5:	//批量删除
			Intent intent = new Intent(this, GroupDeleteMoreActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
		return super.onContextItemSelected(item);
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
		if (data != null) {
			list = SQLiteManager.getInstance().getAllGroupInfo();
			confiMainView();
		}
		if (adapter != null && list != null) {
			adapter.setList(list);
			adapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * 群组适配器
	 * @author Administrator
	 *
	 */
	class GroupAdapter extends BaseAdapter {

		private List<TGroupInfo> list = new ArrayList<TGroupInfo>();
		
		public GroupAdapter(List<TGroupInfo> list) {
			this.list = list;
		}

		public List<TGroupInfo> getList() {
			return list;
		}

		public void setList(List<TGroupInfo> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			return list == null ? 0 : list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if (convertView == null) {
				holder = new Holder();
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.grid_item_group, null);
				
				holder.layout_type = (RelativeLayout) convertView.findViewById(R.id.group_item_type_layout);
				holder.tv_type = (TextView) convertView.findViewById(R.id.group_item_type_txt);
				                                                                                                                                                                                                                                                      
				holder.img_photo = (RoundAngleImageView) convertView.findViewById(R.id.group_item_img);
				holder.tv_name = (TextView) convertView.findViewById(R.id.group_item_name);
				holder.tv_creator = (TextView) convertView.findViewById(R.id.group_item_creator);
				holder.tv_create_date = (TextView) convertView.findViewById(R.id.group_item_create_date);
				
				holder.layout_item = (RelativeLayout) convertView.findViewById(R.id.group_item_layout);
				
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			
			final TGroupInfo groupInfo = list.get(position);
			
			String creator = groupInfo.getGroupCreator();
			String creatorName = (creator.length()>0) ? SQLiteManager.getInstance().getContactInfoByNumber(creator).getName():"?";
			String createTime = groupInfo.getGroupCreateTime();
			int type = groupInfo.getGroupType();
			
			if (needType(position)) {
				holder.layout_type.setVisibility(View.VISIBLE);
				if (type == 0) {
					holder.tv_type.setText(getApplicationContext().getString(R.string._group_my_create));
				}else if(type == 1) {
					holder.tv_type.setText(getApplicationContext().getString(R.string._group_my_join));
				}else {
					holder.tv_type.setText(getApplicationContext().getString(R.string._group_my_join));
				}
			} else {
				holder.tv_type.setText("");
				holder.layout_type.setVisibility(View.GONE);
			}
			
			holder.tv_name.setText(groupInfo.getGroupName());
			holder.tv_creator.setText(creatorName);
			holder.tv_create_date.setText(CommFunc.toDate(Long.parseLong(createTime)));
			if (groupInfo.getGroupShieldTag() == TGroupInfo.GROUP_SHIELD_YES) {
				holder.img_photo.setBackgroundColor(getResources().getColor(R.color.gray));
			}else {
				holder.img_photo.setBackgroundColor(getResources().getColor(R.color.lightgray));
			}
			
			holder.layout_item.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String groupId = list.get(position).getGroupId();
					toGroupDetailActivity(groupId);
				}
			});
			
			holder.layout_item.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				@Override
				public void onCreateContextMenu(ContextMenu menu,View v, ContextMenuInfo menuInfo) {
					currPostion = position;
					menu.setHeaderTitle(groupInfo.getGroupName());
					menu.add(0, 1, 0, getString(R.string._group_item_look));
					if (groupInfo.getGroupType() == TGroupInfo.GROUP_TYPE_CREATE) {
						menu.add(0, 2, 0, getString(R.string._group_item_delete));
					}
					if (groupInfo.getGroupShieldTag() == TGroupInfo.GROUP_SHIELD_NO) {
						menu.add(0, 3, 0, getString(R.string._group_item_shield));
					}else {
						menu.add(0, 4, 0, getString(R.string._group_item_shield_cancel));
					}
					if (groupInfo.getGroupType() == TGroupInfo.GROUP_TYPE_CREATE) {
						menu.add(0, 5, 0, getString(R.string._group_item_delete_more));
					}
				}
			});
			
			return convertView;
		}
		
		/**
		 * 是否需要显示类型
		 * 
		 * @param position
		 * @return
		 */
		private boolean needType(int position) {
			if (position == 0) {// 第一个肯定是分类
				return true;
			}
			if (position < 0) {// 边界处理
				return false;
			}

			TGroupInfo currentEntity = (TGroupInfo) getItem(position);
			TGroupInfo previousEntity = (TGroupInfo) getItem(position - 1);
			if (null == currentEntity || null == previousEntity) {
				return false;
			}

			int currentType = currentEntity.getGroupType();
			int previousType = previousEntity.getGroupType();
			
			if (null == String.valueOf(currentType) || null == String.valueOf(previousType)) {
				return false;
			}
			if (currentType == previousType) {
				return false;
			}
			return true;
		}
		
		public void convert2Gray() {
			
		}
		
		class Holder {
			RelativeLayout layout_type;
			TextView tv_type;
			
			TextView tv_name;// 群组名称
			RoundAngleImageView img_photo; // 群组头像
			TextView tv_creator; //创建者
			TextView tv_create_date; //创建时间
			
			RelativeLayout layout_item;
		}
	}
	
}
