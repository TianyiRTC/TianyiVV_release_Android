package com.sip.rtcclient.activity.person;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.BaseActivity;
import com.sip.rtcclient.ui.TitleViewTianyi;
import com.sip.rtcclient.ui.TitleViewTianyi.OnTianyiTitleActed;
import com.sip.rtcclient.utils.DialogUtil;


public class PersonTelBindActivity extends BaseActivity implements OnTianyiTitleActed {

	private TitleViewTianyi titleView;
	private EditText et_tel_number;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_personal_tel_bind);

		initView();
	}

	/**
	 * 
	 */
	private void initView() {
		titleView=(TitleViewTianyi)findViewById(R.id.titleView);
		titleView.setTitle(R.drawable.btn_right, getString(R.string._personal_setting_tel_bind));
		titleView.setOnTitleActed(this);
		
		et_tel_number = (EditText) findViewById(R.id.personal_tel_bind_et);
	}
	
	@Override
	public void onClickLeftButton() {
		finish();
	}

	@Override
	public void onClickRightButton() {
		DialogUtil.showShortToast(getApplicationContext(), "绑定电话号码");
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}




