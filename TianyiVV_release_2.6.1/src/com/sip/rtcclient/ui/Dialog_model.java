package com.sip.rtcclient.ui;




import com.sip.rtcclientouter.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import android.widget.TextView;

public class Dialog_model extends Dialog implements OnClickListener{
	private View deleteView;
	private Button submitImg;
	private Button cancelImg;
	private TextView tv_message;
	private OnDialogClickListener listener; 
	public Dialog_model(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		LayoutInflater inflater = LayoutInflater.from(context);
		deleteView=inflater.inflate(R.layout.dialog_model, null);
		tv_message=(TextView)deleteView.findViewById(R.id.tv_message);
		submitImg=(Button)deleteView.findViewById(R.id.img_submit);
		cancelImg=(Button)deleteView.findViewById(R.id.img_cancel);
		
		submitImg.setOnClickListener(this);
		cancelImg.setOnClickListener(this);
		this.setContentView(deleteView,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}
	public void setMessageText(String leftString,String rightString,String message){
		if(leftString!=null)
		submitImg.setText(leftString);
		if(rightString!=null){
			cancelImg.setText(rightString);
		}else{
			cancelImg.setVisibility(View.GONE);
		}	
		tv_message.setText(message);//"确定要删除该联系人吗"
	}
	public void setMessageText(int leftId,int rightId,int messageId){
		submitImg.setText(leftId);
		cancelImg.setText(rightId);
		tv_message.setText(messageId);//"确定要删除该联系人吗"
	}
	public void setOnDialogClickListener(OnDialogClickListener listener){
		this.listener=listener;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.img_submit:
			listener.onClickLeftButton();
			break;
		case R.id.img_cancel:
			listener.onClickRightButton();
			break;
		default:
			break;
		}
	}
	public interface OnDialogClickListener {
		public void onClickLeftButton();
		public void onClickRightButton();

	}
}
