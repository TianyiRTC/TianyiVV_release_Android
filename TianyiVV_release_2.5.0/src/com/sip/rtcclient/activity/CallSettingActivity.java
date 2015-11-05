package com.sip.rtcclient.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.config.MsgKey;
import com.sip.rtcclient.ui.TitleViewTianyi;
import com.sip.rtcclient.ui.TitleViewTianyi.OnTianyiTitleActed;
import com.sip.rtcclient.utils.CommFunc;

/**
 * 通话设置Activity
 * @author Administrator
 *
 */
public class CallSettingActivity extends BaseActivity implements OnTianyiTitleActed {

    private String LOGTAG = "CallSettingActivity";



    private TitleViewTianyi titleView;
    private Spinner audioSpinner; //音频编解码
    private Spinner videoSpinner; //视频编解码
    private Spinner videoformatSpinner;


    private ArrayAdapter<?> audioAdapter;
    private ArrayAdapter<?> videoAdapter;
    private ArrayAdapter<?> videoformatadapter; //0qicf 1cif

    private CheckBox call_opengl_cb; //call_nack_cb,
    private CheckBox call_refuse_stranger_group_cb,call_refuse_stranger_cb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_call);

        initSpinner();
        initView();
        initData();

    }

    /**
     * 初始化数据
     */
    private void initSpinner() {
        audioAdapter = ArrayAdapter.createFromResource(this, R.array.audiocodecs, android.R.layout.simple_spinner_item);
        audioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        videoAdapter = ArrayAdapter.createFromResource(this, R.array.videocodecs, android.R.layout.simple_spinner_item);
        videoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        videoformatadapter = ArrayAdapter.createFromResource(this, R.array.videoformat, android.R.layout.simple_spinner_item);
        videoformatadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    }
    private void initData()
    {
        int ogl = MyApplication.getInstance().getIntSharedXml(MsgKey.KEY_VOGL, 1);
        if(ogl == 1)
            call_opengl_cb.setChecked(true);
        else
            call_opengl_cb.setChecked(false);

        int formate = MyApplication.getInstance().getIntSharedXml(MsgKey.KEY_VFORMAT, 0);
        videoformatSpinner.setSelection(formate);

        int audiocode =  MyApplication.getInstance().getIntSharedXml(MsgKey.KEY_ACODEC, MsgKey.ACODEC_OPUS);
        CommFunc.PrintLog(5, LOGTAG, "initData audiocode:"+audiocode);
        audioSpinner.setSelection(audiocode);

        int videocode =  MyApplication.getInstance().getIntSharedXml(MsgKey.KEY_VCODEC, MsgKey.VCODEC_VP8);
        CommFunc.PrintLog(5, LOGTAG, "initData videocode:"+videocode);
        videoSpinner.setSelection(videocode);
    }
    /**
     * 初始化View
     */
    private void initView() {
        titleView=(TitleViewTianyi)findViewById(R.id.titleView);
        titleView.setTitle(-1, getString(R.string._setting_call));
        titleView.setOnTitleActed(this);

        call_opengl_cb = (CheckBox)findViewById(R.id.call_opengl_cb);

        audioSpinner = (Spinner) findViewById(R.id.call_spinner_audio);
        videoSpinner = (Spinner) findViewById(R.id.call_spinner_video);
        videoformatSpinner = (Spinner) findViewById(R.id.call_spinner_videoformate);

        audioSpinner.setAdapter(audioAdapter);
        videoSpinner.setAdapter(videoAdapter);
        videoformatSpinner.setAdapter(videoformatadapter);

        audioSpinner.setOnItemSelectedListener(audioItemSelectedListener);
        videoSpinner.setOnItemSelectedListener(videoItemSelectedListener);
        videoformatSpinner.setOnItemSelectedListener(videoFormateItemSelectedListener);
    }

    private void SaveConfig()
    {
        if(call_opengl_cb.isChecked())
            MyApplication.getInstance().saveSharePrefValue(MsgKey.KEY_VOGL, "1");
        else
            MyApplication.getInstance().saveSharePrefValue(MsgKey.KEY_VOGL, "0");
        MyApplication.getInstance().initVideoAttr();
        MyApplication.getInstance().initAudioCodec();
        MyApplication.getInstance().initVideoCodec();
    }
    
    @Override
    public void onClickLeftButton() {

        //将变更配置进行保存编解码
        SaveConfig();
        finish();
    }

    @Override
    public void onClickRightButton() {

    }

    /**
     * 音频编码ItemSelectedListener
     */
    OnItemSelectedListener audioItemSelectedListener = new OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
         //   CommFunc.PrintLog(5, LOGTAG, "AudioCodec arg0: " + arg0 + " arg1: " + arg1 + " arg3: " + arg3);
            MyApplication.getInstance().saveSharePrefValue(MsgKey.KEY_ACODEC, ""+arg3);       
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {

        }

    };

    /**
     * 视频编码ItemSelectedListener
     */
    OnItemSelectedListener videoItemSelectedListener = new OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            CommFunc.PrintLog(5, LOGTAG, "VideoCodec:arg0: " + arg0 + " arg1: " + arg1 + " arg3: " + arg3);
            MyApplication.getInstance().saveSharePrefValue(MsgKey.KEY_VCODEC, ""+arg3);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {

        }

    };

    //采样率
    OnItemSelectedListener videoFormateItemSelectedListener = new OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            CommFunc.PrintLog(5, LOGTAG, "videoFormate arg0: " + arg0 + " arg1: " + arg1 + " arg3: " + arg3);
            int formate = 0;
            switch ((int)arg3) {
                case 1:
                {
                    formate = MsgKey.VIDEO_FL;
                    break;
                }
                case 2:
                {
                    formate = MsgKey.VIDEO_HD;
                    break;
                }
                default:
                {
                    formate = MsgKey.VIDEO_SD;
                    break;
                }
            }
            MyApplication.getInstance().saveSharePrefValue(MsgKey.KEY_VFORMAT, String.valueOf(formate));
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {

        }

    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            SaveConfig();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}