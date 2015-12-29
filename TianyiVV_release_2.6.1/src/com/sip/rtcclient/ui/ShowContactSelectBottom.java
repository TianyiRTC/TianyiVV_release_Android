package com.sip.rtcclient.ui;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.adapter.ContactBottomAdapter;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.utils.ScreenUtil;

/** 底部选中人员显示 */
public class ShowContactSelectBottom extends LinearLayout implements android.view.View.OnClickListener,OnItemClickListener{
	
	private View view;
	private GridView gridView;
	private Button btn_start;//开始按钮
	private int ITEM_WIDTH_480 = 67;
	private int ITEM_WIDTH = ITEM_WIDTH_480;//底部每个item宽度
	private int ITEM_SPACINF = 3;//item间隔
	private ContactBottomAdapter adapter;
	private OnContactBottomClickListener onContactBottomClickListener;
//	private int addLimit = TConfInfo.CONF_NUMBER_LIMIT - 1;//添加上限(默认算上创建者)
	private String btnString = MyApplication.getInstance().getString(R.string.ok);//按钮上的文字

	public ShowContactSelectBottom(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
		initData();
	}

	private void initData() {
		RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.relativeLayout1);
		relativeLayout.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		int cell = relativeLayout.getMeasuredWidth();
		ITEM_WIDTH = ((ScreenUtil.getScreenWidth(MyApplication.getInstance()) - cell) - ITEM_SPACINF * 4) / 5 ;
//		if (MyApplication.getInstance().getWidth() > 500) 
//			ITEM_WIDTH = ITEM_WIDTH_720;
//		else
//			ITEM_WIDTH = ITEM_WIDTH_480;
		adapter = new ContactBottomAdapter();
		initGridView();
		gridView.setAdapter(adapter);
	}

	private void initGridView(){
		LayoutParams params = new LayoutParams(adapter.getCount() * (ITEM_WIDTH + ITEM_SPACINF) - ITEM_SPACINF
				, LayoutParams.WRAP_CONTENT);
		gridView.setLayoutParams(params);
		gridView.setColumnWidth(ITEM_WIDTH);
		gridView.setHorizontalSpacing(ITEM_SPACINF);
		gridView.setStretchMode(GridView.NO_STRETCH);
		gridView.setNumColumns(adapter.getCount());
		gridView.setOnItemClickListener(this);
	}

	private void initView() {
		view = LayoutInflater.from(MyApplication.getInstance()).inflate(R.layout.view_conf_select_bottom,null);
		gridView = (GridView) view.findViewById(R.id.view_conf_select_bottom_gv);
		btn_start = (Button) view.findViewById(R.id.view_conf_select_bottom_start_btn);
		btn_start.setText(btnString);
		btn_start.setOnClickListener(this);
		addView(view);
	}
	
	public void setOnContactBottomClickListener(
			OnContactBottomClickListener onContactBottomClickListener) {
		this.onContactBottomClickListener = onContactBottomClickListener;
	}

	/** 
	 * 添加数据
	 * @param contactId 
	 */
	public boolean addData(String contactId){
//		if (adapter.getCount() >= addLimit)
//			return false;
		if(adapter != null && adapter.addList(contactId)){
			initGridView();
			showSelectNum();
			return true;
		}
		return false;
	}

	/** 
	 * 删除数据
	 * @param contactId 
	 */
	public boolean deleteData(String contactId){
		if(adapter != null && adapter.deleteList(contactId)){
			initGridView();
			showSelectNum();
			return true;
		}
		return false;
	}
	
	/** 计算显示选中人数 */
	private void showSelectNum(){
		if (adapter == null)
			return;
		String num = adapter.getCount() == 0 ? "" : "("+adapter.getCount()+")";
		btn_start.setText(btnString+num);
	}
	
	/** 获取添加上限人数 */
//	public int getAddLimit() {
//		return addLimit;
//	}
//	
//	/** 设置上限人数 */
//	public void setAddLimit(int addLimit) {
//		if(addLimit >= 0)
//			this.addLimit = addLimit;
//	}
	
	/** 数据更新 */
	public void dataChange(){
		if (adapter != null) {
//			int limit = adapter.getCount() - addLimit;
//			int limit = adapter.getCount() + 1;
//			if (limit > 0) {//判断添加上限是否修改
//				adapter.removeLastData(limit);
//			}
			adapter.dataChange();
		}
	}
	
	/** 设置按钮文字 */
	public void setButtonText(String text){
		if (text != null) {
			btnString = text;
			btn_start.setText(btnString);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.view_conf_select_bottom_start_btn://点击
			if (onContactBottomClickListener != null) 
				onContactBottomClickListener.onConfBottomBtnClick(adapter.getList());
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (onContactBottomClickListener != null) 
			onContactBottomClickListener.onClickBottomItem(""+arg3);
	}
	
	/** 会议添加人员底部开始按钮监听 */
	public interface OnContactBottomClickListener{
		/** 点击返回选中人员数据 */
		void onConfBottomBtnClick(List<TContactInfo> list);
		/** 
		 * 点击底部选中人员
		 * @param ext 
		 */
		void onClickBottomItem(String ext);
	}
}
