package com.sip.rtcclient.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sip.rtcclientouter.R;

public class TitleViewTianyi extends LinearLayout implements OnClickListener {

	ImageView btn_right;
	ImageView btn_left;
	TextView tv_title;
	TextView tv_right;
	OnTianyiTitleActed ontitleacted;

	public TitleViewTianyi(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater localLayoutInflater = (LayoutInflater) context
				.getSystemService("layout_inflater");
		View title = localLayoutInflater.inflate(R.layout.view_title_tianyi,
				null);
		addView(title);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
		title.setLayoutParams(params);
		InitView(title);
	}
	
	public void setTitle(String right, String title) {
		if (right != null) {
			tv_right.setText(right);
		} else {
			tv_right.setVisibility(View.GONE);
		}
		if (title != null && !title.equals(""))
			tv_title.setText(title);
		else
			tv_title.setVisibility(View.GONE);
	}

	public void setTitle(int rightid, String title) {
		if (rightid > 0) {
			btn_right.setVisibility(View.VISIBLE);
			btn_right.setBackgroundResource(rightid);

		} else
			btn_right.setVisibility(View.GONE);
		if (title != null && !title.equals(""))
			tv_title.setText(title);
		else
			tv_title.setVisibility(View.GONE);

	}

	private void InitView(View view) {
		btn_left = (ImageView) view.findViewById(R.id.ImgBtn_left);
		btn_right = (ImageView) view.findViewById(R.id.ImgBtn_right);
		tv_title = (TextView) view.findViewById(R.id.tv_title);
		tv_right = (TextView) view.findViewById(R.id.tv_right);

		tv_title.setOnClickListener(this);
		tv_right.setOnClickListener(this);
		btn_left.setOnClickListener(this);
		btn_right.setOnClickListener(this);
	}

	public void setTitle(String title) {

		if (title == null || title.equals("")) {
			tv_title.setVisibility(View.GONE);
		}
		tv_title.setVisibility(View.VISIBLE);
		tv_title.setText(title);
	}

	public void setOnTitleActed(OnTianyiTitleActed onTitleActed) {
		this.ontitleacted = onTitleActed;
	}

	public View getRightBtnView() {
		return btn_right;
	}

	public View getLeftBtnView() {
		return btn_left;
	}

	public View getTextView() {
		return tv_title;
	}

	public interface OnTianyiTitleActed {
		public void onClickLeftButton();

		public void onClickRightButton();
	}

	@Override
	public void onClick(View v) {
		if (ontitleacted == null)
			return;
		switch (v.getId()) {
		case R.id.tv_title:
			ontitleacted.onClickLeftButton();
			break;
		case R.id.tv_right:
			ontitleacted.onClickRightButton();
			break;
		case R.id.ImgBtn_left:
			ontitleacted.onClickLeftButton();
			break;
		case R.id.ImgBtn_right:
			ontitleacted.onClickRightButton();
			break;
		default:
			break;
		}
	}

}