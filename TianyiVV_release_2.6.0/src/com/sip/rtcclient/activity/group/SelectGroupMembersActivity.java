package com.sip.rtcclient.activity.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.BaseActivity;
import com.sip.rtcclient.adapter.GroupMembersAdapter;
import com.sip.rtcclient.adapter.GroupMembersAdapter.OnClickConfAddPerson;
import com.sip.rtcclient.bean.TGroupInfo;
import com.sip.rtcclient.db.SQLiteManager;
import com.sip.rtcclient.ui.TitleViewTianyi;
import com.sip.rtcclient.ui.TitleViewTianyi.OnTianyiTitleActed;
import com.sip.rtcclient.utils.CommFunc;

/**
 * 创建群组，已选择的联系人Activity
 * @author ThinkPad
 *
 */
public class SelectGroupMembersActivity extends BaseActivity implements OnItemClickListener, OnTianyiTitleActed, OnClickConfAddPerson {

    private String LOGTAG = "SelectGroupMembersActivity";

    private TitleViewTianyi titleView;
    private RelativeLayout okLayout,selGrpvType;
    private TextView membersCount;
    private GridView gridView;

    private GroupMembersAdapter adapter;

    private TGroupInfo groupInfo;

    public static final int SELECT_MENBER = 1;// 选择群组人员
    public static final int ADD_MENBER = 2;// 添加群组人员
    private Spinner sp_conftype;
    private ArrayAdapter<?> adapter_conftype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_sel_members);
        getExtras();
        initView();
        initData();
    }

    /**
     * 获取Extras
     */
    private void getExtras() {
        Bundle bundle = getIntent().getExtras();
        groupInfo = (TGroupInfo) bundle.getSerializable("groupInfo");
    }

    /**
     * 初始化View
     */
    private void initView() {
        titleView=(TitleViewTianyi)findViewById(R.id.titleView);
        gridView = (GridView) findViewById(R.id.group_create_sel_menber_gv);
        okLayout = (RelativeLayout) findViewById(R.id.group_create_sel_members_ok_layout);
        membersCount = (TextView) findViewById(R.id.group_create_sel_members_count_txt);
        selGrpvType = (RelativeLayout) findViewById(R.id.Rl_group_create_selgrpvtype);
        initSpinnerConfType();
    }
    private int conftype;
    private void initSpinnerConfType()
    {
        sp_conftype = (Spinner)findViewById(R.id.spinner_conftype);
        //将可选内容与ArrayAdapter连接起来
        adapter_conftype = ArrayAdapter.createFromResource(this, R.array.conftype, android.R.layout.simple_spinner_item);
        //设置下拉列表的风格 
        adapter_conftype.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //设置下拉列表的风格 
        //将adapter2 添加到spinner中
        sp_conftype.setAdapter(adapter_conftype);
        //添加事件Spinner事件监听  
        sp_conftype.setOnItemSelectedListener(new SpinnerXMLSelectedListener_vformat());
        conftype = 0;
        //设置默认值
        selGrpvType.setVisibility(View.GONE);
    }
    //使用XML形式操作
    class SpinnerXMLSelectedListener_vformat implements OnItemSelectedListener{
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
            conftype = arg2;
            CommFunc.PrintLog(5, LOGTAG,"选择会议类型:"+adapter_conftype.getItem(arg2)+"  confselect:"+arg2);
        }
        public void onNothingSelected(AdapterView<?> arg0) {

        }
    }
    /**
     * 初始化数据
     */
    private void initData() {
        titleView.setTitle(-1, getString(R.string._group_create_name));
        titleView.setOnTitleActed(this);

        adapter = new GroupMembersAdapter();
        Intent intent = getIntent();
        if (intent != null) {// 初始化选中联系人\
            String[] els = intent.getStringArrayExtra("els");
            if (els != null)
                for (int i = 0, len = els.length; i < len; i++) {
                    if (!els[i].equals(MyApplication.getInstance().getUserID()))
                        adapter.add(SQLiteManager.getInstance().getContactInfoById(els[i]));
                }
        }
        adapter.setOnClickConfAddPerson(this);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
    }

    /**
     * 更新已经选择的联系人GridView
     */
    private void updateView() {
        if (adapter.getCount() > 1) {
            //selGrpvType.setVisibility(View.VISIBLE);
            okLayout.setVisibility(View.VISIBLE);
            membersCount.setText("(" + (adapter.getCount()-1) + ")");
        }else {
            okLayout.setVisibility(View.GONE);
            //selGrpvType.setVisibility(View.GONE);
            membersCount.setText("");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case SELECT_MENBER:
            case ADD_MENBER:
                String el = data.getStringExtra("el");
                String[] els = el.split(",");
                for (int i = 0; i < els.length; i++) {
                    adapter.add(SQLiteManager.getInstance().getContactInfoById(els[i]));
                }
                adapter.notifyDataSetChanged();
                updateView();
                break;
            default:
                break;
        }
    }

    /**
     * 选择、添加联系人
     * @param view
     */
    public void onAddMembers(View view) {
        CommFunc.PrintLog(5, LOGTAG, "onAddMembers");
        Intent intent = new Intent(this, SelectContactActivity.class);
		if (adapter != null && adapter.getList().length > 0) {
			intent.putExtra("el", adapter.getList());// 目前人员总数
			startActivityForResult(intent, ADD_MENBER);
		}else {
			startActivityForResult(intent, SELECT_MENBER);
		}
    }


    /**
     * 创建按钮点击事件
     * @param view
     */
    public void onCreateGroup(View view) {
        groupInfo.setGroupId(String.valueOf(System.currentTimeMillis()));
        groupInfo.setGroupName(groupInfo.getGroupName());
        groupInfo.setGroupMembers(adapter.getContactIds());
        groupInfo.setGroupPhoto(null);
        groupInfo.setGroupCreator((MyApplication.getInstance().getUserID().equals(""))?"xxx":MyApplication.getInstance().getUserID());
        groupInfo.setGroupCreateTime(String.valueOf(System.currentTimeMillis()));
        groupInfo.setGroupType(TGroupInfo.GROUP_TYPE_CREATE);
        groupInfo.setGroupShieldTag(TGroupInfo.GROUP_SHIELD_NO);
        CommFunc.PrintLog(5, LOGTAG, "groupName: " + groupInfo.getGroupName() + " groupMembers: " + groupInfo.getGroupMembers());
        CommFunc.PrintLog(5, LOGTAG, "groupName: " + groupInfo.getGroupName() + " groupMembers: " + adapter.getContactIds());
        
        
        SQLiteManager.getInstance().saveGroupInfo(groupInfo, true);

//        final String remote[] = CommFunc.SplictStr(adapter.getContactIds(), ";");
//        //TODO http操作放在Runnable中执行
//        HBaseApp.post2WorkRunnable(new Runnable() {
//
//            @Override
//            public void run() 
//            {
//
//                String params = HttpManager.getInstance().createGroupVoiceCallJson(remote,
//                        MyApplication.getInstance().getUserID(),conftype,groupInfo.getGroupName()).toString();
//                MyApplication.getInstance().CreateConf(params); 
//            }
//        });
        // 创建群组处先实现本地创建 ，本地创建完后 从群组详情发起 类型群组的创建 。
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        adapter.remove(arg2);
        adapter.notifyDataSetChanged();
        updateView();
    }

    @Override
    public void onAddPerson() {
        onAddMembers(null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
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
