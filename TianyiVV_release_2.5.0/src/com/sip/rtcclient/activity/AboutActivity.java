package com.sip.rtcclient.activity;

import rtc.sdk.common.RtcConst;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.ui.TitleViewTianyi;
import com.sip.rtcclient.ui.TitleViewTianyi.OnTianyiTitleActed;

/**
 * <p>desc: 
 * @time 下午08:27:54
 */

public class AboutActivity extends BaseActivity implements OnTianyiTitleActed{

	private TitleViewTianyi titleView;
	
	private TextView sdkVersion;
	private TextView vvVersion;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_about);

        initView();
        initData();
    }
	
	/**
     * 
     */
    private void initView() {
        titleView=(TitleViewTianyi)findViewById(R.id.titleView);
        titleView.setTitle(-1, getString(R.string._setting_about));
        titleView.setOnTitleActed(this);
        
        sdkVersion = (TextView) findViewById(R.id.about_txt_sdk_version);
        vvVersion = (TextView) findViewById(R.id.about_txt_vv_version);
    }
    
    /**
     * TODO设置版本号的数值
     */
    private void initData(){
    	sdkVersion.setText(RtcConst.sdkVersion);
    	vvVersion.setText(MyApplication.getInstance().getAppVersionName());
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
