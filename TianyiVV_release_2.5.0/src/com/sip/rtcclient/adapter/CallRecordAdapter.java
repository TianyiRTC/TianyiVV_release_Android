package com.sip.rtcclient.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.bean.TCallRecordInfo;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.db.SQLiteManager;

public class CallRecordAdapter extends BaseAdapter {

	private Context mContext;
	private List<TCallRecordInfo> list;
	private LayoutInflater mLayoutInflater;

	public CallRecordAdapter(Context pContext, List<TCallRecordInfo> list) {
		mContext = pContext;
		this.list = list;
		mLayoutInflater = LayoutInflater.from(mContext);
	}
	
	public List<TCallRecordInfo> getList() {
		return list;
	}

	public void setList(List<TCallRecordInfo> list) {
		this.list = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			holder = new Holder();
			convertView = mLayoutInflater.inflate(
					R.layout.activity_yishitong_listview, null);
			holder.titleLayout = (LinearLayout) convertView
					.findViewById(R.id.yishitong_item_title_layout);
			holder.title = (TextView) convertView
					.findViewById(R.id.yishitong_item_title);
			holder.head = (ImageView) convertView
					.findViewById(R.id.yishitong_item_img);
			holder.startTime = (TextView) convertView
					.findViewById(R.id.yishitong_item_starttime);
			holder.number = (TextView) convertView
					.findViewById(R.id.yishitong_item_number);
			holder.count = (TextView) convertView
					.findViewById(R.id.yishitong_item_count);
			holder.img_type = (ImageView) convertView
					.findViewById(R.id.yishitong_item_call_img);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		// 获取数据
		TCallRecordInfo info = (TCallRecordInfo) getItem(position);
		holder.head.setBackgroundResource(R.drawable.call_video_default_avatar);
		holder.startTime.setText(info.getStartTime());
		TContactInfo contactInfo = SQLiteManager.getInstance().getContactInfoByNumber(info.getToUser());
		if (contactInfo != null) {
			holder.number.setText(contactInfo.getName());
		}else {
			holder.number.setText(info.getToUser());
		}
		
	    //  TContactInfo contactInfo = SQLiteManager.getInstance().getContactInfoByNumber(info.getToUser());
	  //  holder.number.setText(info.getToUser());
		int count = SQLiteManager.getInstance().getCallRecordCount(info.getFromUser(), info.getToUser());
		holder.count.setText(String.valueOf(count));
		// TODO 备用
		/*if (info.getType() == Call.CT_Audio) {
			holder.img_type.setBackgroundResource(R.drawable.phone_list_item);
		}else {
			holder.img_type.setBackgroundResource(R.drawable.phone_list_item_video);
		}*/

		if (needTitle(position)) {
			// 显示标题并设置内容
			holder.title.setText(info.getDate());
			holder.titleLayout.setVisibility(View.VISIBLE);
			holder.title.setVisibility(View.VISIBLE);
		} else {
			// 内容项隐藏标题
			holder.title.setVisibility(View.GONE);
			holder.titleLayout.setVisibility(View.GONE);
		}

		return convertView;
	}

	@Override
	public int getCount() {
		if (null != list) {
			return list.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (null != list && position < getCount()) {
			return list.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * 判断是否需要显示标题
	 * 
	 * @param position
	 * @return
	 */
	private boolean needTitle(int position) {
		// 第一个肯定是分类
		if (position == 0) {
			return true;
		}

		// 边界处理
		if (position < 0) {
			return false;
		}

		// 当前 // 上一个
		TCallRecordInfo currentEntity = (TCallRecordInfo) getItem(position);
		TCallRecordInfo previousEntity = (TCallRecordInfo) getItem(position - 1);
		if (null == currentEntity || null == previousEntity) {
			return false;
		}

		String currentTitle = currentEntity.getDate();
		String previousTitle = previousEntity.getDate();
		if (null == previousTitle || null == currentTitle) {
			return false;
		}

		// 当前item分类名和上一个item分类名不同，则表示两item属于不同分类
		if (currentTitle.equals(previousTitle)) {
			return false;
		}

		return true;
	}

	private class Holder {
		TextView title, number, count, startTime;
		ImageView head, img_type;
		LinearLayout titleLayout;
	}

}
