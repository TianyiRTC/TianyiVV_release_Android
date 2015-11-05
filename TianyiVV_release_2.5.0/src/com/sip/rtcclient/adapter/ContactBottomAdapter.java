package com.sip.rtcclient.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.db.SQLiteManager;

public class ContactBottomAdapter extends BaseAdapter {
	
	private List<TContactInfo> list = new ArrayList<TContactInfo>();
	
	private HashSet<String> contactIdSet = new HashSet<String>();
	
	/** 添加数据 */
	public boolean addList(String ext){
		if (!contactIdSet.contains(ext)) {
			contactIdSet.add(ext);
			list.add(SQLiteManager.getInstance().getContactInfoById(ext));
			notifyDataSetChanged();
			return true;
		}
		return false;
	}
	
	/** 删除数据 */
	public boolean deleteList(String ext){
		if (contactIdSet.contains(ext)) {
			contactIdSet.remove(ext);
			for (int i = 0; i < list.size(); i++) {
				if (ext.equals(list.get(i).getContactId())) {
					list.remove(i);
					break;
				}
			}
			notifyDataSetChanged();
			return true;
		}
		return false;
	}
	
	/** 更新数据 */
	public void dataChange(){
		if (list.size() < 0) 
			return;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				int len = list.size();
				for (int i = 0; i < len; i++) {
					list.set(i, SQLiteManager.getInstance().getContactInfoById(list.get(i).getContactId()));
				}
				new Message();
				handler.sendMessage(Message.obtain());
			}
		}).start();
	}
	
	/** 去除末尾num个数据 */
	public void removeLastData(int num){
		if (num > list.size()) {
			list.clear();
			return;
		}
		int len = list.size() -1;
		for (int i = len; i > len - num ; i--) {
			list.remove(i);
		}
	}
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			notifyDataSetChanged();
			super.handleMessage(msg);
		}
		
	};
	
	/** 获取选中数据 */
	public List<TContactInfo> getList() {
		return list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list == null ? 0 : list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return Long.valueOf(list.get(position).getContactId());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(MyApplication.getInstance()).inflate(R.layout.grid_item_conf_bottom,null);
			viewHolder.icon = (ImageView) convertView.findViewById(R.id.grid_item_conf_bottom_img);
			viewHolder.name = (TextView) convertView.findViewById(R.id.grid_item_conf_bottom_txt);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		//AvatarManager.getInstance().setBitmap(viewHolder.icon, list.get(position));
		viewHolder.name.setText(list.get(position).getName());
		return convertView;
	}
	
	class ViewHolder{
		ImageView icon;
		TextView name;
	}
}
