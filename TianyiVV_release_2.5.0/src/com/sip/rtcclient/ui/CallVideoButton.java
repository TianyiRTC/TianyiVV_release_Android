package com.sip.rtcclient.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sip.rtcclientouter.R;

public class CallVideoButton extends RelativeLayout implements OnClickListener{
	
	private CheckBox checkBox;
	private TextView textView;
	private RelativeLayout relativeLayout;
	private Context context;
	private OnCallViedoCheckBoxListener onCallViedoCheckBoxListener;
	private boolean clickable = true;
	
	public CallVideoButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.context = context;
		initView();
	}
	
	private void initView(){
		LayoutInflater localLayoutInflater = (LayoutInflater) context
				.getSystemService("layout_inflater");
		View view = localLayoutInflater
				.inflate(R.layout.view_call_video_button, null);
		checkBox = (CheckBox) view.findViewById(R.id.calling_video_button_cb);
		textView = (TextView) view.findViewById(R.id.textView1);
		relativeLayout = (RelativeLayout) view.findViewById(R.id.relativeLayout1);
		checkBox.setClickable(false);
		addView(view);
		relativeLayout.setOnClickListener(this);
	}
	
	public void setOnCallViedoCheckBoxListener(OnCallViedoCheckBoxListener onCallViedoCheckBoxListener){
		this.onCallViedoCheckBoxListener = onCallViedoCheckBoxListener;
	}
	
	/**
	 * 设置checkbox图片和文字
	 * @param checkBoxBg
	 * @param text
	 */
	public void setData(int checkBoxBg,String text){
		if (checkBoxBg <= 0) 
			checkBox.setVisibility(View.GONE);
		else{
			checkBox.setVisibility(View.VISIBLE);
			checkBox.setBackgroundResource(checkBoxBg);
		}
		if (text == null) 
			textView.setVisibility(View.GONE);
		else{
			textView.setVisibility(View.VISIBLE);
			textView.setText(text);
		}
		setTextColor();
	}
	
	/** 设置是否能点击 */
	public void setClickable(boolean clickable,int checkBoxBg,String text){
		this.clickable = clickable;
		if (checkBoxBg <= 0) 
			checkBox.setVisibility(View.GONE);
		else{
			checkBox.setVisibility(View.VISIBLE);
			checkBox.setBackgroundResource(checkBoxBg);
		}
		if (text == null) 
			textView.setVisibility(View.GONE);
		else{
			textView.setVisibility(View.VISIBLE);
			textView.setText(text);
		}
		setTextColor();
	}
	
	/** 设置是否能点击 */
	public void setClickable(boolean clickable){
		this.clickable = clickable;
		setTextColor();
	}
	
	private void setTextColor(){
		if (clickable) 
			textView.setTextColor(0xffffffff);
		else{
			textView.setTextColor(0xff6685a4);
			relativeLayout.setBackgroundResource(R.drawable.call_video_button_unclick);
		}
		relativeLayout.setClickable(clickable);
	}
	
	@Override
	public void onClick(View v) {
		if(!clickable)
			return;
		if (checkBox.isChecked()) {
			relativeLayout.setBackgroundResource(R.drawable.call_video_button_s);
		}else {
			relativeLayout.setBackgroundResource(R.drawable.call_video_button_checked);
		}
		checkBox.setChecked(!checkBox.isChecked());
		if (onCallViedoCheckBoxListener != null) 
			onCallViedoCheckBoxListener.onCallVideoCheckBox(this, checkBox.isChecked());
	}
	
	/** 自定义checkbox监听*/
	public interface OnCallViedoCheckBoxListener{
		void onCallVideoCheckBox(View view,boolean check);
	}
}
	