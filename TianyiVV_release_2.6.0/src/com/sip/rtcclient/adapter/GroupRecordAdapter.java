package com.sip.rtcclient.adapter;

import java.util.List;

import rtc.sdk.common.RtcConst;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.bean.TGroupRecordInfo;

public class GroupRecordAdapter extends BaseAdapter {

	private List<TGroupRecordInfo> list;
	private Context context;
	private LayoutInflater inflater;

	public GroupRecordAdapter(Context context, List<TGroupRecordInfo> list) {
		this.context = context;
		this.list = list;
		inflater = LayoutInflater.from(context);

	}

	public List<TGroupRecordInfo> getList() {
		return list;
	}

	public void setList(List<TGroupRecordInfo> list) {
		this.list = list;
	}

	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;

		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.list_item_group_record,null);
			viewHolder.tv_runtime = (TextView) convertView.findViewById(R.id.group_call_tv_run_time);
			viewHolder.img_result = (ImageView) convertView.findViewById(R.id.group_call_result);
			viewHolder.tv_date = (TextView) convertView.findViewById(R.id.group_call_date);
			viewHolder.tv_time = (TextView) convertView.findViewById(R.id.group_call_time);
			viewHolder.img_type = (ImageView) convertView.findViewById(R.id.group_call_type_img);
			viewHolder.tv_type = (TextView) convertView.findViewById(R.id.group_call_type_tv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		TGroupRecordInfo info = list.get(position);
		
		switch (info.getJoinResult()) {
		case TGroupRecordInfo.CALL_TYPE_IN_FAIL:
			viewHolder.img_result.setBackgroundResource(R.drawable.icon_call_in_fails);
			break;
		case TGroupRecordInfo.CALL_TYPE_IN_SUCCESS:
			viewHolder.img_result.setBackgroundResource(R.drawable.icon_call_in_success);
			break;
		case TGroupRecordInfo.CALL_TYPE_OUT_FAIL:
			viewHolder.img_result.setBackgroundResource(R.drawable.icon_call_out_fail);
			break;
		case TGroupRecordInfo.CALL_TYPE_OUT_SUCCESS:
			viewHolder.img_result.setBackgroundResource(R.drawable.icon_call_out_success);
			break;
		default:
			break;
		}
		switch (info.getConftype()) {
		case RtcConst.grouptype_multigrpchatA:
			viewHolder.img_type.setBackgroundResource(R.drawable.group_record_chat);
			viewHolder.tv_type.setText(context.getString(R.string.chat_room_name));
			break;
		case RtcConst.grouptype_multigrpspeakA:
			viewHolder.img_type.setBackgroundResource(R.drawable.group_record_intercom);
			viewHolder.tv_type.setText(context.getString(R.string.intercom_name));
			break;
		case RtcConst.grouptype_multitwoA:
			viewHolder.img_type.setBackgroundResource(R.drawable.group_record_show);
			viewHolder.tv_type.setText(context.getString(R.string.vv_show_name));
			break;
		case RtcConst.grouptype_microliveAV:
			viewHolder.img_type.setBackgroundResource(R.drawable.group_record_live);
			viewHolder.tv_type.setText(context.getString(R.string.tv_name));
			break;

		default:
			break;
		}
		viewHolder.tv_runtime.setText(info.getTime());
		viewHolder.tv_date.setText(info.getStartDate());
		viewHolder.tv_time.setText(info.getStartTime());
		return convertView;
	}

	private class ViewHolder {

		private ImageView img_result;
		private TextView tv_runtime;
		private TextView tv_date;
		private TextView tv_time;
		private ImageView img_type;
		private TextView tv_type;
	}

}
