package com.sip.rtcclient.activity.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.BaseActivity;
import com.sip.rtcclient.bean.TGroupInfo;
import com.sip.rtcclient.ui.TitleViewTianyi;
import com.sip.rtcclient.ui.TitleViewTianyi.OnTianyiTitleActed;
import com.sip.rtcclient.utils.DialogUtil;
import com.sip.rtcclient.utils.ScreenUtil;

/**
 * 创建群组Activity
 * @author ThinkPad
 *
 */
public class GroupCreateActivity extends BaseActivity implements OnTianyiTitleActed {
	
	private String LOGTAG = "GroupCreateActivity";
	
	private TitleViewTianyi titleView;
	private EditText groupName;				//群组名称
	
	private boolean isPopup;				//标识选择头像上传方式Window是否显示
	private PopupWindow popupWindow;		//头像上传方式Window

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_create);
		initView();
		initData();
	}

	/**
	 * 初始化View
	 */
	private void initView() {
		titleView=(TitleViewTianyi)findViewById(R.id.titleView);
        groupName = (EditText) findViewById(R.id.group_create_name);
	}
	
	/**
	 * 初始化内容
	 */
	private void initData() {
		titleView.setTitle(-1, getString(R.string._group_create_title));
        titleView.setOnTitleActed(this);
	}
	
	/**
	 * 创建按钮点击事件
	 * @param view
	 */
	public void onNext(View view) {
		TGroupInfo groupInfo = new TGroupInfo();
		groupInfo.setGroupName((groupName.getText().toString().trim().equals(""))?getString(R.string._group_create_name):groupName.getText().toString());
		Intent intent = new Intent(this, SelectGroupMembersActivity.class);
		intent.putExtra("groupInfo", groupInfo);
		startActivity(intent);
		finish();
	}
	
	/**
	 * 头像点击事件
	 * @param view
	 */
	public void onAvatar(View view) {
		if (isPopup) {
            return;
        }
        isPopup = true;
        View viewer = getLayoutInflater().inflate(R.layout.view_photo_upload, null);
        Button btn_camer = (Button) viewer.findViewById(R.id.btn_camer);
        Button btn_local = (Button) viewer.findViewById(R.id.btn_local);
        Button btn_cancel = (Button) viewer.findViewById(R.id.btn_cancel);
        popupWindow = new PopupWindow(viewer);
        popupWindow.setAnimationStyle(R.style.animation);
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        popupWindow.update(0, 0, ScreenUtil.getScreenWidth(this), ScreenUtil.getScreenHeight(this));
        btn_camer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isPopup = false;
                popupWindow.dismiss();
                DialogUtil.showShortToast(getApplicationContext(), "btn_camer");
            }
        });
        btn_local.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isPopup = false;
                popupWindow.dismiss();
                DialogUtil.showShortToast(getApplicationContext(), "btn_local");
            }
        });
        btn_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isPopup = false;
                popupWindow.dismiss();
            }
        });
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isPopup) {
				isPopup = false;
				popupWindow.dismiss();
			}else {
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClickLeftButton() {
		finish();
	}

	@Override
	public void onClickRightButton() {
		
	}
	
}
