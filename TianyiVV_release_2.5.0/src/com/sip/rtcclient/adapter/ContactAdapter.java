package com.sip.rtcclient.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.config.SysConfig;

/**
 *
 */
public class ContactAdapter extends BaseAdapter {
	private List<TContactInfo> list;
	private Context context;
	private LayoutInflater inflater;
	private HashMap<String, Integer> alphaIndexer;
	private String[] sections;
	private boolean isSelect;
	private OnContactSelectListener onContactSelect;

	Map<String, TContactInfo> selectMap = null;

	public ContactAdapter(Context context, List<TContactInfo> list,
			boolean isSelect) {
		this.context = context;
		this.list = list;
		this.isSelect = isSelect;
		inflater = LayoutInflater.from(context);
		listalpha();
		selectMap = new HashMap<String, TContactInfo>();
	}

	public void setList(List<TContactInfo> list) {
		this.list = list;
	}

	public void setOnConfSelectListener(OnContactSelectListener onContactSelect) {
		this.onContactSelect = onContactSelect;
	}

	public HashMap<String, Integer> getAlphaInder() {
		return alphaIndexer;
	}

	public String[] getSections() {
		return sections;
	}

	@Override
	public void notifyDataSetChanged() {
		listalpha();
		super.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
	}

	@Override
	public TContactInfo getItem(int position) {
		return (list == null) ? null : list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent)// implements
																			// android.widget.Adapter.getView
	{// Get a View that displays the data at the specified position in the data
		// set.
		ViewHolder viewHolder = null;

		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(R.layout.list_item_contact, null);
			// wwyue
			viewHolder.tv_contact_stas = (TextView) convertView
					.findViewById(R.id.tv_contact_stas);
			
			viewHolder.tv_contact_stas = (TextView) convertView
					.findViewById(R.id.tv_contact_stas);
			viewHolder.tv_name = (TextView) convertView
					.findViewById(R.id.tv_name);
			viewHolder.img_avatar = (ImageView) convertView
					.findViewById(R.id.img_avatar);
			viewHolder.img_tianyi = (ImageView) convertView
					.findViewById(R.id.img_tianyi);
			viewHolder.img_weibo = (ImageView) convertView
					.findViewById(R.id.img_weibo);
			viewHolder.ll_title = (LinearLayout) convertView
					.findViewById(R.id.ll_title);
			viewHolder.tv_title = (TextView) convertView
					.findViewById(R.id.tv_title);
			viewHolder.rl_right = (RelativeLayout) convertView
					.findViewById(R.id.rl_right);
			viewHolder.ll_icon = (LinearLayout) convertView
					.findViewById(R.id.ll_icon);
			viewHolder.cb_contact = (CheckBox) convertView
					.findViewById(R.id.contact_select_cb);
			viewHolder.contentItem = (RelativeLayout) convertView
					.findViewById(R.id.rl_content);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		TContactInfo person = list.get(position);
		viewHolder.tv_name.setText(person.getName());
		viewHolder.img_avatar
				.setBackgroundResource(R.drawable.call_video_default_avatar);
		// if (0 == person.getPhotoId()) {
		// viewHolder.img_avatar.setImageResource(R.drawable.call_video_default_avatar);
		// } else {
		// Uri uri =
		// ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,Long.parseLong(person.getContactId()));
		// InputStream input =
		// ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(),uri);
		// Bitmap contactPhoto = BitmapFactory.decodeStream(input);
		// viewHolder.img_avatar.setImageBitmap(contactPhoto);
		// }
		viewHolder.rl_right.setVisibility(View.GONE);
		String currentStr = "";
		currentStr = getAlpha(person.getFirstChar());
		String previewStr = "";
		if (position > 0) {
			TContactInfo info = list.get(position - 1);
			previewStr = getAlpha(info.getFirstChar());
		}
		if (!previewStr.equals(currentStr) && !currentStr.equals("#")) {
			viewHolder.ll_title.setVisibility(View.VISIBLE);
			viewHolder.tv_title.setText(currentStr);
		} else {
			viewHolder.ll_title.setVisibility(View.GONE);
		}
		// CommFunc.PrintLog(5, "COntactAdapter", "type:"+person.getUsertype());
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
			viewHolder.img_tianyi.setVisibility(View.GONE);
			viewHolder.img_weibo.setVisibility(View.GONE);
			break;
		default:
			break;
		}

		final TContactInfo info = person;
		final ViewHolder holder = viewHolder;

		if (isSelect) {
			viewHolder.cb_contact.setOnCheckedChangeListener(null);
			viewHolder.cb_contact.setVisibility(View.VISIBLE);
			viewHolder.cb_contact.setTag(R.string.tag_key_contact_id,
					info.getContactId());
			viewHolder.cb_contact.setTag(R.string.tag_key_contact_object, info);
			viewHolder.cb_contact.setChecked(selectMap.keySet().contains(
					info.getContactId()));
			if (viewHolder.cb_contact.isChecked()) {
				holder.contentItem.setBackgroundColor(context.getResources()
						.getColor(R.color.contact_sel_bg));
			} else {
				holder.contentItem
						.setBackgroundResource(R.drawable.bg_yishitong_item_sel);
			}

			viewHolder.cb_contact
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							String contactId = (String) buttonView
									.getTag(R.string.tag_key_contact_id);
							TContactInfo contactInfo = (TContactInfo) buttonView
									.getTag(R.string.tag_key_contact_object);
							if (isChecked) {
								if (onContactSelect != null) {
									if (onContactSelect.onAdd(contactId)) {// 是否到添加上�?
										selectMap.put(contactId, contactInfo);
										holder.contentItem
												.setBackgroundColor(context
														.getResources()
														.getColor(
																R.color.contact_sel_bg));
									} else {
										buttonView.setChecked(false);
										holder.contentItem
												.setBackgroundResource(R.drawable.bg_yishitong_item_sel);
									}
								} else {
									selectMap.put(contactId, contactInfo);
									holder.contentItem
											.setBackgroundColor(context
													.getResources()
													.getColor(
															R.color.contact_sel_bg));
								}
							} else {
								holder.contentItem
										.setBackgroundResource(R.drawable.bg_yishitong_item_sel);
								selectMap.remove(contactId);
								if (onContactSelect != null)
									onContactSelect.onDelete(contactId);
							}
						}
					});

			viewHolder.contentItem.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (holder.cb_contact.isChecked()) {
						holder.cb_contact.setChecked(false);
					} else {
						holder.cb_contact.setChecked(true);
					}
				}
			});
		} else {
			viewHolder.cb_contact.setVisibility(View.GONE);
		}

		// if (isSelect) {
		// viewHolder.contentItem.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// if (holder.cb_contact.isChecked()) {
		// holder.cb_contact.setChecked(false);
		// }else {
		// holder.cb_contact.setChecked(true);
		// }
		// }
		// });
		// }

		// wwyue
		if (person.getStatus().equals("1")) {
			viewHolder.tv_contact_stas.setVisibility(View.VISIBLE);
		} else {
			viewHolder.tv_contact_stas.setVisibility(View.INVISIBLE);
		}
		return convertView;
	}

	private class ViewHolder {
		private TextView tv_name;
		private ImageView img_avatar;
		private ImageView img_tianyi;
		private ImageView img_weibo;
		private LinearLayout ll_title;
		private TextView tv_title;
		private RelativeLayout rl_right;
		private LinearLayout ll_icon;
		private CheckBox cb_contact;
		private RelativeLayout contentItem;
		// wwyue 显示在线状态
		private TextView tv_contact_stas;
	}

	private String getAlpha(String str) {
		if (str == null) {
			return "#";
		}

		if (str.trim().length() == 0) {
			return "#";
		}

		char c = str.trim().substring(0, 1).charAt(0);
		Pattern pattern = Pattern.compile("^[A-Za-z]+$");
		if (pattern.matcher(c + "").matches()) {
			return (c + "").toUpperCase();
		} else {
			return "#";
		}
	}

	private void listalpha() {
		alphaIndexer = new HashMap<String, Integer>();
		sections = new String[list.size()];

		for (int i = 0; i < list.size(); i++) {
			TContactInfo info = list.get(i);
			String currentStr = "";
			currentStr = getAlpha(info.getFirstChar());

			String previewStr = " ";
			if (i > 0) {
				TContactInfo info1 = list.get(i - 1);
				previewStr = getAlpha(info1.getFirstChar());
			}
			if (!previewStr.equals(currentStr)) {
				String name = currentStr;
				alphaIndexer.put(name, i);
				sections[i] = name;
			}
		}
	}

	public interface OnContactSelectListener {
		boolean onAdd(String extension);

		boolean onDelete(String extension);
	}
}
