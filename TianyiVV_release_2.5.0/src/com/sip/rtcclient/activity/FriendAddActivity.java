package com.sip.rtcclient.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.config.SysConfig;
import com.sip.rtcclient.db.SQLiteManager;
import com.sip.rtcclient.ui.TitleViewTianyi;
import com.sip.rtcclient.ui.TitleViewTianyi.OnTianyiTitleActed;
import com.sip.rtcclient.utils.CommFunc;
import com.sip.rtcclient.utils.PinYinManager;


public class FriendAddActivity extends BaseActivity implements
OnTianyiTitleActed,OnCheckedChangeListener {

    public static boolean changed = false;
    private TitleViewTianyi titleView;
    private CheckBox cb_autoAddPhoneBook; //add_phonebook_cb

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_add);

        initView();
        cb_autoAddPhoneBook.setChecked(false);
    }

    /**
     * 
     */
    private void initView() {
        titleView=(TitleViewTianyi)findViewById(R.id.titleView);
        titleView.setTitle(-1, getString(R.string._setting_add_friend));
        titleView.setOnTitleActed(this);
        cb_autoAddPhoneBook = (CheckBox)findViewById(R.id.add_phonebook_cb);
        cb_autoAddPhoneBook.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClickLeftButton() {
        finish();
    }

    @Override
    public void onClickRightButton() {

    }
    /**
     * 添加通讯录好友
     * @param view
     */
    public void onAddPhoneFriend(View view)
    {
        CommFunc.PrintLog(5, LOGTAG, "onAddPhoneFriend");
        OnAutoAddFriends();
        CommFunc.PrintLog(5,LOGTAG, "通讯录已同步!");
        CommFunc.DisplayToast(FriendAddActivity.this, "通讯录已同步!");
  
    }
    /**
     * 添加通讯录好友
     * @param view
     */
//    public void onAddPhoneBookFriend(View view) {
//        CommFunc.PrintLog(5, LOGTAG, "onAddPhoneBookFriend");
//        CommFunc.DisplayToast(getApplicationContext(), "添加通讯录好友暂时未实现");
//        //		DialogUtil.showShortToast(getApplicationContext(), "添加通讯录好友");
//        //		Intent intent=new Intent(this,AddAddressActivity.class);
//        //		startActivity(intent);
//    }

    /**
     * 添加新浪微博好友
     * @param view
     */
    //更改为获取账号好友
    public void onAddAccFriend(View view) {
        CommFunc.PrintLog(5, LOGTAG, "onAddAccFriend");
        MainActivity.bShowToast = true;
        //MainActivity.getInstance().getUserList();
        //CommFunc.DisplayToast(getApplicationContext(), "添加账号好友暂时未实现");
        //		DialogUtil.showShortToast(getApplicationContext(), "添加新浪微博好友");
        //		Intent intent=new Intent(this,AddWeiboActivity.class);
        //		startActivity(intent);
        
        
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private Map<Integer, TContactInfo> contactIdMap = null;
    /**
     * 获取系统通讯录
     * 
     * @return
     */
    private class MyAsyncQueryHandler extends AsyncQueryHandler {

        public MyAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        /**
         * 查询结束的回调函数
         */
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (cursor != null && cursor.getCount() > 0) {

                List<TContactInfo> list = new ArrayList<TContactInfo>();
                contactIdMap = new HashMap<Integer, TContactInfo>();
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToPosition(i);
                    String name = cursor.getString(1);
                    String number = cursor.getString(2);
                    String sortKey = cursor.getString(3);
                    int contactId = cursor.getInt(4);
                    Long photoId = cursor.getLong(5);
                    String lookUpKey = cursor.getString(6);

                    if (contactIdMap.containsKey(contactId) == false) {

                        if(number!=null)
                        {   
                            
                            number = number.replace(" ", "");
                           
                        }
                        TContactInfo cb = new TContactInfo();
                        cb.setName(name);
                        if (number.startsWith("+86")) {// 去除多余的中国地区号码标志，对这个程序没有影响。
                            cb.setPhoneNum(number.substring(3));
                            cb.setContactId(number.substring(3));
                            
                        } else {
                            cb.setPhoneNum(number);
                            cb.setContactId(number);
                        }
                        cb.setFirstChar(sortKey); 
                        cb.setPhotoId(photoId);
                        cb.setLookUpKey(lookUpKey);
                        cb.setUsertype(SysConfig.USERTYPE_SYSTEM);
                        list.add(cb);

                        contactIdMap.put(contactId, cb);

                    }
                }
                // TODO 测试用，需要修改
                SQLiteManager.getInstance().deleteAllContactInfo(true);
                
                TContactInfo cb = new TContactInfo();
                // 如果新浪微博登录取用户名称
                String username = MyApplication.getInstance().getUserID();
                if (SysConfig.login_type == SysConfig.USERTYPE_WEIBO) {
                    cb.setUsertype(SysConfig.USERTYPE_WEIBO);
                } else
                    cb.setUsertype(SysConfig.USERTYPE_TIANYI);
                String[] pinyin = PinYinManager.toPinYin(username);
                cb.setName(username);
                cb.setPhoneNum(MyApplication.getInstance().getUserID());
                cb.setFirstChar(pinyin[0]);
                cb.setLookUpKey(pinyin[1]);
                cb.setContactId(MyApplication.getInstance().getUserID());
                cb.setPhotoId(null);
                SQLiteManager.getInstance().saveContactInfo(cb, true);

                SQLiteManager.getInstance().saveContactInfoList(list, true);
                changed = true;
            }
            if(cursor!=null)
            {
                cursor.close();
                cursor = null;
            }
        }

    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // TODO Auto-generated method stub
        switch (buttonView.getId()) 
        {
            case R.id.add_phonebook_cb:
                if(isChecked)
                {
                }

                break;
        }
    }
    //自动添加通讯录好友
    public void OnAutoAddFriends()
    {
        CommFunc.PrintLog(5, LOGTAG, "OnAutoAddFriends");
        AsyncQueryHandler asyncQuery = new MyAsyncQueryHandler(getContentResolver());
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
        String[] projection = {
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.DATA1,
                "sort_key",
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
        }; // 查询的列
        asyncQuery.startQuery(0, null, uri, projection, null, null,
        "sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询
    }

}

