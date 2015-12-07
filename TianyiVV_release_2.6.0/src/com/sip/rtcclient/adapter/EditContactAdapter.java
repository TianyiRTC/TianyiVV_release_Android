package com.sip.rtcclient.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.bean.TContactInfo;

public class EditContactAdapter extends BaseAdapter {
	Map<String, TContactInfo> selectMap = null;
	private OnSelectChangeListener onSelectChangeListener;
	private List<TContactInfo> list;
	protected LayoutInflater mInflater;
	private Context context;
	public EditContactAdapter(Context context, List<TContactInfo> data) {
		this.list=data;
		this.context= context;
		mInflater=LayoutInflater.from(context);
		// TODO Auto-generated constructor stub
	}

	public List<TContactInfo> getList() {
		return list;
	}

	public void setList(List<TContactInfo> list) {
		this.list = list;
	}
	
    public Map<String, TContactInfo> getSelectMap() {
        if (selectMap == null)
            selectMap = new HashMap<String, TContactInfo>();
        return selectMap;
    }
    public void setOnSelectListener(OnSelectChangeListener onSelectChangeListener) {
		this.onSelectChangeListener = onSelectChangeListener;
	}
    
    /** 删除选中人员 */
    public void deleteSelect(String ext){
    	if (selectMap != null){
    		selectMap.remove(ext);
    		notifyDataSetChanged();
    	}
    }
    
	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		super.notifyDataSetChanged();
		if(onSelectChangeListener!=null){
			onSelectChangeListener.onSelectChange();
		}
	}

	public void refresh(List<TContactInfo> data) {
		this.list = data;
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
	}

	@Override
	public TContactInfo getItem(int position) {
		return list == null ? null : list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public TContactInfo removeItem(int position) {
		TContactInfo info = list.remove(position);
		notifyDataSetChanged();
		return info;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final TContactInfo bean = list.get(position);
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_edit_contact, null);		
			viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			viewHolder.img_avatar = (QuickContactBadge) convertView.findViewById(R.id.img_avatar);
			viewHolder.img_tianyi = (ImageView) convertView.findViewById(R.id.img_tianyi);
			viewHolder.img_weibo = (ImageView) convertView.findViewById(R.id.img_weibo);
			viewHolder.ll_title = (LinearLayout) convertView.findViewById(R.id.ll_title);
			viewHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
			viewHolder.rl_right = (RelativeLayout) convertView.findViewById(R.id.rl_right);
			viewHolder.ll_icon = (LinearLayout) convertView.findViewById(R.id.ll_icon);
			viewHolder.cb_select = (CheckBox) convertView
					.findViewById(R.id.cb_select);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		TContactInfo info=list.get(position);
		viewHolder.ll_title.setVisibility(View.GONE);
		viewHolder.tv_name.setText(info.getName());
		viewHolder.img_avatar.setBackgroundResource(R.drawable.call_video_default_avatar);
//		viewHolder.img_avatar.assignContactUri(ContactsContract.Contacts.getLookupUri(Long.parseLong(info.getContactId()), info.getLookUpKey()));
//		if(0 == info.getPhotoId()){
//			viewHolder.img_avatar.setImageResource(R.drawable.call_video_default_avatar);
//		}else{
//			Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(info.getContactId()));
//			InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri); 
//			Bitmap contactPhoto = BitmapFactory.decodeStream(input);
//			viewHolder.img_avatar.setImageBitmap(contactPhoto);
//		}
		viewHolder.rl_right.setVisibility(View.GONE);
		String number=info.getPhoneNum();
		if(getSelectMap().containsKey(number)){
			viewHolder.cb_select.setChecked(true);
		}else{
			viewHolder.cb_select.setChecked(false);
		}
		return convertView;
	}

	private class ViewHolder {
		private TextView tv_name;
		private QuickContactBadge img_avatar;
		@SuppressWarnings("unused")
		private ImageView img_tianyi;
		@SuppressWarnings("unused")
		private ImageView img_weibo;
		private LinearLayout ll_title;
		@SuppressWarnings("unused")
		private TextView tv_title;
		private RelativeLayout rl_right;
		@SuppressWarnings("unused")
		private LinearLayout ll_icon;
		public CheckBox cb_select;
	}
	public void addSelect(String id,TContactInfo info){
		if(getSelectMap().containsKey(id)){
			deleteSelect(id);
		}else{
			getSelectMap().put(id, info);
		}
		notifyDataSetChanged();
	}
	public void selectAll(){
		if(list==null)
			return;
		for(int i=0;i<list.size();i++){
			TContactInfo info=list.get(i);
			getSelectMap().put(info.getPhoneNum(), info);
						
		}		
		notifyDataSetChanged();
	}
	public void deleteAll(){
		getSelectMap().clear();
		selectMap=null;
		notifyDataSetChanged();
	}
	public interface OnSelectChangeListener{
		void onSelectChange();
	}
}
