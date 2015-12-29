package com.sip.rtcclient.adapter;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.bean.TContactInfo;

/**
 * PopupGridAdapter
 * @author Zhp
 *
 */
@SuppressWarnings("unused")
public class PopupGridAdapter extends BaseAdapter {

	private List<TContactInfo> list;
	private LayoutInflater mInflater;
	private Context context;

	public PopupGridAdapter(Context context,
			List<TContactInfo> list) {
		this.context = context;
		this.mInflater = LayoutInflater.from(context);
		this.list = list;
	}

	public int getCount() {
		return list.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		GridHolder holder;
		if (convertView == null) {
			holder = new GridHolder();
			convertView = mInflater.inflate(R.layout.popup_grid_item, null);
			holder.imageView = (ImageView) convertView.findViewById(R.id.head_grid_imgview);
			holder.text = (TextView) convertView.findViewById(R.id.head_grid_text);
			convertView.setTag(holder);
		} else {
			holder = (GridHolder) convertView.getTag();
		}
		
		TContactInfo info = list.get(position);
		if (info != null) {
			holder.text.setText((info.getName().equals(null)?info.getPhoneNum():info.getName()));
		}

		
		return convertView;
	}

	final class GridHolder {
		private ImageView imageView;
		public TextView text;
	}

}
