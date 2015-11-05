package com.sip.rtcclient.activity.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.BaseActivity;
import com.sip.rtcclient.bean.TGroupInfo;
import com.sip.rtcclient.db.SQLiteManager;
import com.sip.rtcclient.ui.Dialog_model;
import com.sip.rtcclient.ui.Dialog_model.OnDialogClickListener;
import com.sip.rtcclient.ui.RoundAngleImageView;
import com.sip.rtcclient.ui.TitleViewTianyi;
import com.sip.rtcclient.ui.TitleViewTianyi.OnTianyiTitleActed;

/**
 * <p>desc: 
 * @data 2013-8-31
 * @time 下午02:59:14
 */

/**
 * 群组批量删除
 */
public class GroupDeleteMoreActivity extends BaseActivity implements OnTianyiTitleActed, OnCheckedChangeListener, Observer {
	
	private String LOGTAG = "GroupDeleteMoreActivity";
	
	private TitleViewTianyi titleView;
	private TextView txt_none;
	private ListView listView;
	private List<TGroupInfo> list;
	private GroupAdapter adapter;
	
	private LinearLayout layout_bottom;
	private TextView txt_count;
	private CheckBox cb_select_all;
	
	private Set<String> selectSet;
	private Dialog_model dialog;
	private boolean isPupup = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_delete_more);
		
		initView();
		initData();
		confiMainView();
	}
	
	/**
	 * 
	 */
	private void initView() {
		titleView=(TitleViewTianyi)findViewById(R.id.titleView);
		cb_select_all = (CheckBox) findViewById(R.id.activity_group_more_cb_layout_cb);
		txt_none = (TextView) findViewById(R.id.activity_group_more_none);
		listView = (ListView) findViewById(R.id.activity_group_more_listview);
		layout_bottom = (LinearLayout) findViewById(R.id.activity_group_more_bottom);
		txt_count = (TextView) findViewById(R.id.activity_group_more_bottom_count);
	}

	/**
	 * 
	 */
	private void initData() {
		titleView.setOnTitleActed(this);
		titleView.setTitle(-1, getString(R.string._group_item_delete_more));
		cb_select_all.setOnCheckedChangeListener(this);
		list = SQLiteManager.getInstance().getAllGroupInfo();
		adapter = new GroupAdapter(list);
		listView.setAdapter(adapter);
		
		selectSet = new HashSet<String>();
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
	
	/**
	 * 更新已经选择的群组
	 */
	private void updateView() {
		if (selectSet.size() > 0) {
			layout_bottom.setVisibility(View.VISIBLE);
			txt_count.setText("(" + (selectSet.size()) + ")");
		}else {
			layout_bottom.setVisibility(View.GONE);
			txt_count.setText("");
		}
	}
	
	
	/**
	 * 全选点击事件-扩大点击范围
	 * @param view
	 */
	public void onCheckChanged(View view){
		if (cb_select_all.isChecked()) {
			cb_select_all.setChecked(false);
		}else {
			cb_select_all.setChecked(true);
		}
	}
	
	/**
	 * 删除
	 * @param view
	 */
	public void onDelete(View view){
		isPupup = true;
		if(dialog==null){
            dialog = new Dialog_model(this, R.style.FloatDialog);
        }
        dialog.setMessageText(getString(R.string.ok),getString(R.string.cancel),getString(R.string._group_more_delete_tip));
        dialog.setOnDialogClickListener(new OnDialogClickListener() {
            @Override
            public void onClickRightButton() {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    isPupup = false;
                }
            }
            @Override
            public void onClickLeftButton() {
            	dialog.dismiss();
            	isPupup = false;
                onGroupDelete();
            }
        });
        dialog.show();
	}
	
	/**
	 * 删除所选分组
	 */
	protected void onGroupDelete() {
		if (selectSet.size() > 0) {
			SQLiteManager.getInstance().deleteGroupInfoList(selectSet);
		}
		selectSet.clear();
		updateView();
	}

	/**
	 * 取消
	 * @param view
	 */
	public void onCancel(View view){
		finish();
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
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int count = listView.getChildCount();  
        for (int i = 0; i < count; i++) {  
            ((CheckBox) ((listView.getChildAt(i)).findViewById(R.id.group_item_cb))).setChecked(isChecked); 
        }  
        ((GroupAdapter) listView.getAdapter()).selectAll(isChecked);
        if (adapter != null && list != null) {
        	adapter.notifyDataSetChanged();
		}
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if (isPupup) {
				dialog.dismiss();
				isPupup = false;
			}else {
				onCancel(null);
			}
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
	 * 群组适配器
	 * @author Administrator
	 *
	 */
	class GroupAdapter extends BaseAdapter {

		private List<TGroupInfo> list = new ArrayList<TGroupInfo>();
		private HashMap<String, Boolean> isSelected = null;
		private LayoutInflater mLayoutInflater;
		
		public GroupAdapter(List<TGroupInfo> list) {
			this.list = list;
			isSelected = new HashMap<String, Boolean>();  
	        selectAll(false);
			mLayoutInflater = LayoutInflater.from(getApplicationContext());
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
				convertView = mLayoutInflater.inflate(R.layout.grid_item_group, null);
				
				holder.layout_type = (RelativeLayout) convertView.findViewById(R.id.group_item_type_layout);
				holder.tv_type = (TextView) convertView.findViewById(R.id.group_item_type_txt);
				
				holder.img_photo = (RoundAngleImageView) convertView.findViewById(R.id.group_item_img);
				holder.tv_name = (TextView) convertView.findViewById(R.id.group_item_name);
				holder.tv_creator = (TextView) convertView.findViewById(R.id.group_item_creator);
				holder.tv_create_date = (TextView) convertView.findViewById(R.id.group_item_create_date);
				holder.layout_creator = (LinearLayout) convertView.findViewById(R.id.group_item_creator_layout);
				
				holder.layout_item = (RelativeLayout) convertView.findViewById(R.id.group_item_layout);
				
				holder.cb_select = (CheckBox) convertView.findViewById(R.id.group_item_cb);
				holder.tv_more_name = (TextView) convertView.findViewById(R.id.group_item_more_name);
				holder.tv_more_type = (TextView) convertView.findViewById(R.id.group_item_more_type);
				
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			
			holder.layout_type.setVisibility(View.GONE);
			holder.tv_type.setVisibility(View.GONE);
			holder.tv_name.setVisibility(View.GONE);
			holder.tv_creator.setVisibility(View.GONE);
			holder.tv_create_date.setVisibility(View.GONE);
			holder.layout_creator.setVisibility(View.GONE);
			
			holder.cb_select.setVisibility(View.VISIBLE);
			holder.tv_more_name.setVisibility(View.VISIBLE);
			holder.tv_more_type.setVisibility(View.VISIBLE);
			
			TGroupInfo groupInfo = list.get(position);
			String groupId = groupInfo.getGroupId();
			int type = groupInfo.getGroupType();
			
			holder.tv_more_name.setText(groupInfo.getGroupName());
			if (type == TGroupInfo.GROUP_TYPE_CREATE) {
				holder.tv_more_type.setText(getString(R.string._group_more_delete_create));
			}else {
				holder.tv_more_type.setText(getString(R.string._group_more_delete_join));
			}
			if (groupInfo.getGroupShieldTag() == TGroupInfo.GROUP_SHIELD_YES) {
				holder.img_photo.setBackgroundColor(getResources().getColor(R.color.gray));
			}else {
				holder.img_photo.setBackgroundColor(getResources().getColor(R.color.lightgray));
			}
			
			final Holder viewHolder = holder; 
			final String id = groupId;

			holder.cb_select.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						viewHolder.layout_item.setBackgroundColor(getResources().getColor(R.color.contact_sel_bg));
					} else {
						viewHolder.layout_item.setBackgroundResource(R.drawable.bg_yishitong_item_sel);
					}
					select(id, isChecked);
					updateView();
				}
			});
			holder.layout_item.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (viewHolder.cb_select.isChecked()) {
						viewHolder.cb_select.setChecked(false);
					}else {
						viewHolder.cb_select.setChecked(true);
					}
				}
			});
			
			holder.cb_select.setChecked(isSelected.get(groupId));
			
			return convertView;
		}
		
		/**
		 * 
		 * 
		 * @param postion
		 * @param isChecked
		 */
		public void select(String groupId, boolean isChecked) {  
	        isSelected.put(groupId, isChecked);  
	        if (isChecked) {
	        	selectSet.add(groupId);
			}else {
				if (selectSet != null && selectSet.size() > 0) {
					selectSet.remove(groupId);
				}
			}
	    }  
	  
		/**
		 * 
		 * @param isChecked
		 */
	    public void selectAll(boolean isChecked) {  
	        int count = getCount();  
	        for (int i = 0; i < count; i++) {  
	            select(list.get(i).getGroupId(), isChecked);  
	        }  
	    } 
	    
		class Holder {
			RelativeLayout layout_type;
			TextView tv_type;
			
			TextView tv_name;// 群组名称
			RoundAngleImageView img_photo; // 群组头像
			TextView tv_creator; //创建者
			TextView tv_create_date; //创建时间
			
			LinearLayout layout_creator;
			
			CheckBox cb_select;	//
			TextView tv_more_name;	//群组名称
			TextView tv_more_type;	//群组类型
			
			RelativeLayout layout_item;
		}
	}

}