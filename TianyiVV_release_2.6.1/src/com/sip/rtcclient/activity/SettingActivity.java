package com.sip.rtcclient.activity;

import java.util.HashMap;
import java.util.Map;

import rtc.sdk.common.RtcConst;
import rtc.uploadLog.MailSenderInfo;
import rtc.uploadLog.SimpleMailSender;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Handler;
import android.util.Log;
import android.os.Message;
import android.widget.Toast;
import android.app.AlertDialog;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.person.PersonalActivity;
import com.sip.rtcclient.config.MsgKey;
import com.sip.rtcclient.ui.TitleViewSimple;
import com.sip.rtcclient.ui.TitleViewSimple.OnSimpleTitleActed;
import com.sip.rtcclient.utils.CommFunc;

public class SettingActivity extends BaseActivity implements OnSimpleTitleActed {

    private TitleViewSimple titleView;
    private int openfileDialogId = 0;
    private String sdkLogPath;
    private String mailContent;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
    }

    /**
     * 
     */
    private void initView() {
        titleView=(TitleViewSimple)findViewById(R.id.titleView);
        titleView.setTitle(-1, -1, getString(R.string.title_setting));
        titleView.setOnTitleActed(this);
    }

    @Override
    public void onClickLeftButton() {

    }

    @Override
    public void onClickRightButton() {

    }

    /**
     * 跳转个人资料界面
     * @param view
     */
    public void onPerson(View view) {
        CommFunc.PrintLog(5, LOGTAG, "onPerson");
        Intent intent = new Intent(this, PersonalActivity.class);
        startActivity(intent);
    }

    /**
     * 跳转添加好友界面
     * @param view
     */
    public void onAddFriend(View view) {
        CommFunc.PrintLog(5, LOGTAG, "onAddFriend");
        Intent intent = new Intent(this, FriendAddActivity.class);
        startActivity(intent);
    }

    /**
     * 跳转通话设置界面
     * @param view
     */
    public void onCallSetting(View view) {
        CommFunc.PrintLog(5, LOGTAG, "onAddFriend");
        Intent intent = new Intent(this, CallSettingActivity.class);
        startActivity(intent);
    }

    //升级时显示进度框
    protected ProgressDialog loadingDialog;
    protected void loadingDialog(String showtext) {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(this);
            loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            if (showtext == null) {
                loadingDialog.setMessage(getString(R.string.processing));
            } else {
                loadingDialog.setMessage(showtext);
            }
            loadingDialog.setIndeterminate(false);
            loadingDialog.setCancelable(true);
            loadingDialog.setCanceledOnTouchOutside(false);
        }
        loadingDialog.show();
    }

    /**
     * 销毁加载Dialog
     */
    protected void dismissLoadDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    public void onSettingAbout(View view)
    {
        CommFunc.PrintLog(5, LOGTAG, "onSettingAbout");
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
    public void onCheckUpdate(View view) {
        MainActivity.getInstance().bHandUpdate = true;
        MainActivity.getInstance().CheckUpdate();
    }
    
    public void onSwitchSrtp(View view) {
        CommFunc.PrintLog(5, LOGTAG, "onSwitchSrtp");
        TextView txt = (TextView) findViewById(R.id.text_switch_srtp);

        if(MyApplication.getInstance().srtp_mode == 1) {
            MyApplication.getInstance().srtp_mode = 2;
            MyApplication.getInstance().disposeSipRegister();
            CommFunc.DisplayToast(this, getString(R.string._setting_switch_srtp)+"成功！");
            txt.setText(R.string._setting_switch_rtp);
        }
        else {
            MyApplication.getInstance().srtp_mode = 1;
            MyApplication.getInstance().disposeSipRegister();
            CommFunc.DisplayToast(this, getString(R.string._setting_switch_rtp)+"成功！");
            txt.setText(R.string._setting_switch_srtp);
        }
    }

    public void upload(View view){
    	CommFunc.PrintLog(5, LOGTAG, "onUploadLog");
    	showDialog(openfileDialogId);
    }
    
    protected Dialog onCreateDialog(int id){
    	if(id==openfileDialogId){
    		Map<String, Integer> images = new HashMap<String, Integer>();
    		// 设置各文件类型的图标
    		images.put(OpenFileDialog.sRoot,R.drawable.filedialog_root);
    		images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);	//返回上一层的图标
			images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);	//文件夹图标
			images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
			Dialog dialog=OpenFileDialog.createDialog(id, this, "选择要上传的日志", new CallbackBundle(){
				@Override
				public void callback(Bundle bundle) {
					String filepath = bundle.getString("path");
					sdkLogPath=filepath;
					uploadContent();
				}
			}, ".log;", images);
			return dialog;
    	}
    	return null;
    }    
    
    private void uploadContent(){
    	LayoutInflater factory=LayoutInflater.from(this);
    	final View textEntryView=factory.inflate(R.layout.activity_setting_uploadissue, null);
    	AlertDialog.Builder dlg=new AlertDialog.Builder(this);
    	dlg.setTitle("上传原因：");
    	dlg.setView(textEntryView);
    	dlg.setPositiveButton("上传日志", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// TODO Auto-generated method stub
				EditText content=(EditText) textEntryView.findViewById(R.id.ed_uploadissue);
				mailContent=content.getText().toString();
				sendEmail();
			}
		});
    	dlg.setNegativeButton("取消上传", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
		});
    	dlg.create();
    	dlg.show();
    }
    
    private void sendEmail(){
    	pd= ProgressDialog.show(this, "上传日志", "日志上传中...");
    	
    	if (sdkLogPath=="")
    		sdkLogPath=RtcConst.sdkLogFolder;
		
    	new Thread(){
    		public void run(){
    			try{
    				String[] toAddr={"yangxin@ctbri.com.cn","dingpeng@ctbri.com.cn","zhangzch@ctbri.com.cn","shilh@ctbri.com.cn","weilai@ctbri.com.cn","shenyun_bjy@ctbri.com.cn"};
    				MailSenderInfo mailInfo=new MailSenderInfo();
    				mailInfo.setToAddress(toAddr);
    				mailInfo.setSubject("sdk日志报告");
    				mailInfo.setContent(mailContent);
    				mailInfo.setAttachFileName(sdkLogPath);
    				SimpleMailSender mailsender=new SimpleMailSender();
          	        mailsender.sendHtmlMail(mailInfo);

			        Boolean success=mailsender.getSendStatus();
          	        if (success)
          	        	handler.obtainMessage(1).sendToTarget();
          	        else
          	        	handler.obtainMessage(0).sendToTarget();
    			}
    			catch(Exception e){
    				Log.e("SendMail", e.getMessage(), e);
    				return;
    			}   			
    		}
    	}.start();
    }
    
  //定义Handler对象
    private Handler handler =new Handler(){
    	@Override
    	//当有消息发送出来的时候就执行Handler的这个方法
    	public void handleMessage(Message msg){
    		pd.dismiss();
    		switch (msg.what){
    		case 1:
    			Toast.makeText(getApplication(), getApplication().getString(R.string._setting_upload)+"成功", Toast.LENGTH_LONG).show();
    			break;
    		case 0:
    			Toast.makeText(getApplication(), getApplication().getString(R.string._setting_upload)+"失败，请检查网络连接", Toast.LENGTH_LONG).show();
    			break;
    		}
    		super.handleMessage(msg);
    	}
    };
    
    public void onExit(View view)
    {
        //程序退出
        CommFunc.PrintLog(5, LOGTAG, "onExit");
        Intent intent = new Intent(MsgKey.key_msg_appexit);
        MyApplication.getInstance().sendBroadcast(intent);
    }
}
