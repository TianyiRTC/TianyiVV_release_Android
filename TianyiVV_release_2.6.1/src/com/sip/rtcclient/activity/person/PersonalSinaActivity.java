package com.sip.rtcclient.activity.person;

import android.os.Bundle;
import android.view.KeyEvent;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.BaseActivity;
import com.sip.rtcclient.ui.TitleViewTianyi;
import com.sip.rtcclient.ui.TitleViewTianyi.OnTianyiTitleActed;

/**
 * <p>desc: 
 * @data 2013-7-10
 * @time 下午10:34:59
 */

public class PersonalSinaActivity extends BaseActivity implements OnTianyiTitleActed{
	
	private TitleViewTianyi titleView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_personal_sinal);
		initView();
	}
	
	/**
	 * 
	 */
	private void initView() {
		titleView=(TitleViewTianyi)findViewById(R.id.titleView);
		titleView.setTitle(-1, getString(R.string._personal_weibo_sina));
		titleView.setOnTitleActed(this);
	}

	@Override
	public void onClickLeftButton() {
		finish();
	}

	@Override
	public void onClickRightButton() {

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}