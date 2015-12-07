package com.sip.rtcclient.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.bean.TGroupInfo;

public class GroupAdapter extends BaseAdapter {
	private List<TGroupInfo> list;
	private Context context;
	private LayoutInflater inflater;
	
	public GroupAdapter(Context context,List<TGroupInfo> list) {
		this.context=context;
		this.list=list;
		inflater=LayoutInflater.from(context);
		
	}
	 
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list==null?0:list.size();
	}

	@Override
	public TGroupInfo getItem(int position) {
		// TODO Auto-generated method stub
		return (list==null)?null:list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@SuppressWarnings("deprecation")
	public View getView(int position, View convertView, ViewGroup parent)//implements android.widget.Adapter.getView
	{//Get a View that displays the data at the specified position in the data set. 
		ViewHolder viewHolder = null;
		
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.list_item_group, null);		
			viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			viewHolder.img_avatar = (ImageView) convertView.findViewById(R.id.img_avatar);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		TGroupInfo info=list.get(position);
		viewHolder.tv_name.setText(info.getGroupName());
		viewHolder.img_avatar.setBackgroundResource(R.drawable.call_video_default_avatar);
		
		
		return convertView;
	}
	private class ViewHolder{
		private TextView tv_name;		
		private ImageView img_avatar;
	}
	
}
