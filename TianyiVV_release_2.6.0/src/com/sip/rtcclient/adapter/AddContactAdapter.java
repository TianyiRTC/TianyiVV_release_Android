package com.sip.rtcclient.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.config.SysConfig;

public class AddContactAdapter extends BaseAdapter {
	private List<TContactInfo> list;
	@SuppressWarnings("unused")
	private Context context;
	private LayoutInflater inflater;
	
	public AddContactAdapter(Context context,List<TContactInfo> list) {
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
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent)//implements android.widget.Adapter.getView
	{//Get a View that displays the data at the specified position in the data set. 
		ViewHolder viewHolder = null;
		
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.list_item_contact, null);		
			viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			viewHolder.img_avatar = (ImageView) convertView.findViewById(R.id.img_avatar);
			viewHolder.img_tianyi = (ImageView) convertView.findViewById(R.id.img_tianyi);
			viewHolder.img_weibo = (ImageView) convertView.findViewById(R.id.img_weibo);
			viewHolder.ll_title = (LinearLayout) convertView.findViewById(R.id.ll_title);
			viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
			viewHolder.rl_right = (RelativeLayout) convertView.findViewById(R.id.rl_right);
			viewHolder.ll_icon = (LinearLayout) convertView.findViewById(R.id.ll_icon);
			viewHolder.tv_add = (TextView) convertView.findViewById(R.id.tv_add);
			viewHolder.img_add = (ImageView) convertView.findViewById(R.id.img_add);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		TContactInfo person=list.get(position);
		viewHolder.tv_name.setText(person.getName());
		viewHolder.img_avatar.setBackgroundResource(R.drawable.call_video_default_avatar);
		viewHolder.ll_title.setVisibility(View.GONE);
		viewHolder.rl_right.setVisibility(View.VISIBLE);
		switch (person.getPhoneType()) {
		case TContactInfo.TYPE_ADD:
			viewHolder.tv_add.setText(R.string.add);
			viewHolder.img_add.setVisibility(View.VISIBLE);
			break;
		case TContactInfo.TYPE_APPLY:
			viewHolder.tv_add.setText(R.string.apply);
			viewHolder.img_add.setVisibility(View.GONE);
			break;
		default:
			break;
		}
        switch (person.getUsertype()) {
		case SysConfig.USERTYPE_TIANYI:
			viewHolder.img_tianyi.setVisibility(View.VISIBLE);
			viewHolder.img_weibo.setVisibility(View.GONE);
			break;
		case SysConfig.USERTYPE_WEIBO:
			viewHolder.img_tianyi.setVisibility(View.GONE);
			viewHolder.img_weibo.setVisibility(View.VISIBLE);
			break;
		case SysConfig.USERTYPE_SYSTEM:
			viewHolder.img_tianyi.setVisibility(View.VISIBLE);
			viewHolder.img_weibo.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
		return convertView;
	}
	private class ViewHolder{
		private TextView tv_name;
		private ImageView img_avatar;
		private ImageView img_tianyi;
		private ImageView img_weibo;
		private LinearLayout ll_title;
		private TextView tv_title;
		private RelativeLayout rl_right;
		private LinearLayout ll_icon;
		private TextView tv_add;
		private ImageView img_add;
		
	}
	
}
