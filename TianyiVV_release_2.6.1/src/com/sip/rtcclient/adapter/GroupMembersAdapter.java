package com.sip.rtcclient.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.bean.TContactInfo;

public class GroupMembersAdapter extends BaseAdapter implements OnClickListener{

	private List<TContactInfo> list = new ArrayList<TContactInfo>();
	private HashSet<String> extSet = new HashSet<String>();
	private OnClickConfAddPerson onClickConfAddPerson;
	
	/** 添加联系人按钮监听 */
	public void setOnClickConfAddPerson(OnClickConfAddPerson onClickConfAddPerson) {
		this.onClickConfAddPerson = onClickConfAddPerson;
	}

	/** 添加人员 */
	public void add(TContactInfo info) {
		if (info != null && !extSet.contains(info.getContactId())) {
			extSet.add(info.getContactId());
			list.add(info);
		}
	}

	/** 删除人员 */
	public void remove(int position) {
		extSet.remove(list.remove(position).getContactId());
	}

	public String[] getList() {
		String[] el = new String[list.size()];
		for (int i = 0, len = list.size(); i < len; i++) {
			el[i] = list.get(i).getContactId();
		}
		return el;
	}
	public List<TContactInfo> getArrList()
	{
	    return list;
	}
	
	public String getContactIds() {
		StringBuffer result = new StringBuffer();
		for (int i = 0, len = list.size(); i < len; i++) {
			if (i== len -1 ) {
				result.append(list.get(i).getContactId());
			}else {
				result.append(list.get(i).getContactId()).append(";");
			}
		}
		return result.toString();
	}

	@Override
	public int getCount() {
		return list == null ? 1 : list.size()+1;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			holder = new Holder();
			convertView = LayoutInflater.from(MyApplication.getInstance())
					.inflate(R.layout.grid_item_conf_reserve, null);
			holder.img_photo = (ImageView) convertView
					.findViewById(R.id.conf_reserve_item_img);
			holder.tv_name = (TextView) convertView
					.findViewById(R.id.conf_reserve_item_name_tv);
			holder.ll_person_bg = (LinearLayout) convertView.findViewById(R.id.conf_reserve_person_bg);
			holder.img_add_btn = (ImageView) convertView.findViewById(R.id.conf_reserve_item_add_img);
			holder.img_add_btn.setOnClickListener(this);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		if (getCount() == 0) {
			holder.ll_person_bg.setVisibility(View.INVISIBLE);
			holder.img_add_btn.setVisibility(View.VISIBLE);
		} else {
			if (position == getCount()-1) {//显示加人按钮
				holder.ll_person_bg.setVisibility(View.INVISIBLE);
				holder.img_add_btn.setVisibility(View.VISIBLE);
			}else {//显示选中人员
				holder.ll_person_bg.setVisibility(View.VISIBLE);
				holder.img_add_btn.setVisibility(View.INVISIBLE);
				holder.img_photo.setBackgroundResource(R.drawable.user_metting_add_online);
				
				String name = list.get(position).getName();
				if (name != null && !name.equals("")) {
					holder.tv_name.setText(name);
				}else {
					holder.tv_name.setText(list.get(position).getPhoneNum());
				}
				
			}
		}
		return convertView;
	}
	
	class Holder {
		TextView tv_name;// 用户名
		ImageView img_photo; // 头像
		LinearLayout ll_person_bg;//显示人名layout
		ImageView img_add_btn;//加人按钮
	}

	public interface OnClickConfAddPerson{
		void onAddPerson();
	}

	@Override
	public void onClick(View v) {
		if (onClickConfAddPerson != null) {
			onClickConfAddPerson.onAddPerson();
		}
	}

}
