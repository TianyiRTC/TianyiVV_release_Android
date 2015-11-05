package com.sip.rtcclient.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sip.rtcclientouter.R;

public class AddrAdapter extends BaseAdapter{

	List<String> list;
	Context context;
	LayoutInflater inflater;
	public AddrAdapter(Context context,List<String> list) {
		this.context = context;
		this.list = list;
		inflater = LayoutInflater.from(context);
	}
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return arg0;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		LinearLayout view = (LinearLayout)inflater.inflate(R.layout.list_item_addr, null);
		TextView txt =(TextView)view.findViewById(R.id.list_item_addr_txt);
		txt.setText(list.get(arg0));
		return view;
	}

}
