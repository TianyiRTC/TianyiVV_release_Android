package com.sip.rtcclient.activity.person;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.BaseActivity;
import com.sip.rtcclient.ui.TitleViewTianyi;
import com.sip.rtcclient.ui.TitleViewTianyi.OnTianyiTitleActed;
import com.sip.rtcclient.utils.DialogUtil;

/**
 * <p>desc: 电话号码
 * @data 2013-7-10
 * @time 下午10:33:30
 */

public class PersonalTelActivity extends BaseActivity implements OnTianyiTitleActed{
	
	private TitleViewTianyi titleView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_personal_tel);

		initView();
	}

	/**
	 * 
	 */
	private void initView() {
		titleView=(TitleViewTianyi)findViewById(R.id.titleView);
		titleView.setTitle(-1, getString(R.string._personal_setting_tel));
		titleView.setOnTitleActed(this);
	}

	@Override
	public void onClickLeftButton() {
		finish();
	}

	@Override
	public void onClickRightButton() {
		
	}
	
	/**
	 * 修改电话号码
	 * @param view
	 */
	public void onChangeTel(View view) {
		Intent intent = new Intent(this, PersonTelChangeActivity.class);
		startActivity(intent);
	}
	
	/**
	 * 绑定电话号码
	 * @param view
	 */
	public void onBindTel(View view) {
		Intent intent = new Intent(this, PersonTelBindActivity.class);
		startActivity(intent);
	}
	
	/**
	 * 删除电话号码
	 * @param view
	 */
	public void onDelTel(View view) {
		DialogUtil.showShortToast(getApplicationContext(), "弹出删除电话号码提示框");
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}




