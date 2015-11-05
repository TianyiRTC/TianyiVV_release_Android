package com.sip.rtcclient.activity.person;

import rtc.sdk.common.RtcConst;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.oauth2.weibo.OAuthSharepreference;
import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.BaseActivity;
import com.sip.rtcclient.config.SysConfig;
import com.sip.rtcclient.ui.TitleViewTianyi;
import com.sip.rtcclient.ui.TitleViewTianyi.OnTianyiTitleActed;
import com.sip.rtcclient.utils.DialogUtil;
import com.sip.rtcclient.utils.ScreenUtil;

public class PersonalActivity extends BaseActivity implements OnTianyiTitleActed {

    private TitleViewTianyi titleView;

    private View popView;
    private PopupWindow popupWindow;
    boolean isPopup;
    private TextView vTel,vAppId,vAccount,weiboAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_personal);

        initView();
        initData();
    }

    /**
     * 
     */
    private void initView() {
        titleView=(TitleViewTianyi)findViewById(R.id.titleView);
        titleView.setTitle(-1, getString(R.string._setting_person));
        titleView.setOnTitleActed(this);
        vTel = (TextView)findViewById(R.id.personal_tel_number);
        vAppId = (TextView)findViewById(R.id.personal_app_id);
        vAccount = (TextView)findViewById(R.id.personal_tianyi_account);
        weiboAccount =(TextView)findViewById(R.id.personal_weibo_sina_account); 
    }
    private void initData()
    {
        vTel.setText(MyApplication.getInstance().getUserID());
        if(SysConfig.login_type==SysConfig.USERTYPE_TIANYI)
        {
            vAppId.setText(SysConfig.APP_ID);
            vAccount.setText(MyApplication.getInstance().getUserID());   
        }
        else if(SysConfig.login_type==SysConfig.USERTYPE_WEIBO)
        {
            weiboAccount.setText(OAuthSharepreference.getUname(PersonalActivity.this)); 
        }

    }

    @Override
    public void onClickLeftButton() {
        finish();
    }

    @Override
    public void onClickRightButton() {

    }

    /**
     * 选择头像获取方式
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

    /**
     * 绑定手机号界面
     * @param view
     */
    public void onTelStatus(View view) {
        Intent intent = new Intent(this, PersonalTelActivity.class);
        startActivity(intent);
    }

    /**
     * 绑定邮箱界面
     * @param view
     */
    public void onMailStatus(View view) {
        Intent intent = new Intent(this, PersonalMailActivity.class);
        startActivity(intent);
    }

    /**
     * 绑定天翼帐号界面
     * @param view
     */
    public void onTianyiStatus(View view) {
        Intent intent = new Intent(this, PersonalTianyiActivity.class);
        startActivity(intent);
    }

    /**
     * 绑定新浪微博界面
     * @param view
     */
    public void onSinaStatus(View view) {
        Intent intent = new Intent(this, PersonalSinaActivity.class);
        startActivity(intent);
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
    
}