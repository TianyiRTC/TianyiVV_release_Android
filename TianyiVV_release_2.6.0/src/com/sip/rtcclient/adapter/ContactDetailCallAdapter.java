package com.sip.rtcclient.adapter;

import java.util.List;

import jni.sip.Call;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.bean.TCallRecordInfo;

public class ContactDetailCallAdapter extends BaseAdapter {

	private Context mContext;
	private List<TCallRecordInfo> list;
	private LayoutInflater mLayoutInflater;

	public ContactDetailCallAdapter(Context pContext, List<TCallRecordInfo> list) {
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
					R.layout.activity_contact_call_detail_listview, null);
			holder.day = (TextView) convertView
					.findViewById(R.id.contact_detail_list_day);
			holder.startTime = (TextView) convertView
					.findViewById(R.id.contact_detail_list_srarttime);
			holder.totalTime = (TextView) convertView
					.findViewById(R.id.contact_detail_list_total_time);
			holder.result = (ImageView) convertView
					.findViewById(R.id.contact_detail_list_tag);
			holder.type = (ImageView) convertView
					.findViewById(R.id.contact_detail_call_type);
			holder.typeTxt = (TextView) convertView
					.findViewById(R.id.contact_detail_call_type_txt);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		// 获取数据
		TCallRecordInfo info = (TCallRecordInfo) getItem(position);
		holder.day.setText(info.getDate());
		holder.startTime.setText(info.getStartTime());
		holder.totalTime.setText(info.getTotalTime());

		int result = info.getResult();
		int type = info.getType();
		int direction = info.getDirection();

		if (type == Call.CT_Audio) {
			holder.type.setBackgroundResource(R.drawable.phone_list_item);
			holder.typeTxt.setText(mContext
					.getString(R.string._contact_detail_call_audio));
		} else {
			holder.type.setBackgroundResource(R.drawable.phone_list_item_video);
			holder.typeTxt.setText(mContext
					.getString(R.string._contact_detail_call_viodeo));
		}

		if (result == TCallRecordInfo.CALL_RESULT_SUCCESS) {
			if (direction == TCallRecordInfo.CALL_DIRECTION_IN) {
				holder.result
						.setBackgroundResource(R.drawable.icon_call_in_success);
			} else {
				holder.result
						.setBackgroundResource(R.drawable.icon_call_out_success);
			}
		} else {
			if (direction == TCallRecordInfo.CALL_DIRECTION_IN) {
				holder.result
						.setBackgroundResource(R.drawable.icon_call_in_fails);
			} else {
				holder.result
						.setBackgroundResource(R.drawable.icon_call_out_fail);
			}
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

	private class Holder {
		TextView day, totalTime, startTime, typeTxt;
		ImageView result, type;
	}

}
