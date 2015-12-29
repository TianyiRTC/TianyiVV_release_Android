package com.sip.rtcclient.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import jni.http.HttpManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sip.rtcclient.HBaseApp;
import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.group.MemberAddToGroupActivity;
import com.sip.rtcclient.adapter.ContactAdapter;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.config.MsgKey;
import com.sip.rtcclient.config.SysConfig;
import com.sip.rtcclient.db.SQLiteManager;
import com.sip.rtcclient.ui.MyLetterListView;
import com.sip.rtcclient.ui.MyLetterListView.OnTouchingLetterChangedListener;
import com.sip.rtcclient.ui.TitleViewSimple;
import com.sip.rtcclient.ui.TitleViewSimple.OnSimpleTitleActed;
import com.sip.rtcclient.utils.CommFunc;

/**
 * 联系人Activity
 * 
 * @author Administrator
 * 
 */
public class ContactActivity extends BaseActivity implements Observer,
OnItemClickListener, OnSimpleTitleActed {
    private TitleViewSimple titleView;
    private ListView listView;
    private ContactAdapter adapter;
    private MyLetterListView letterListView;
    private TextView overlay;
    private OverlayThread overlayThread;
    private WindowManager windowManager;

    private Map<Integer, TContactInfo> contactIdMap = null;
    private AsyncQueryHandler asyncQuery;
    private List<TContactInfo> list;
    private static final int CONTEXT_MENU_LOOK = 1;
    private static final int CONTEXT_MENU_ADD = 2;
    private static final int CONTEXT_MENU_DELETE = 3;
    private static final int CONTEXT_MENU_SHIELD = 4;
    private static final int CONTEXT_MENU_DELETES = 5;
    private String LOGTAG = "ContactActivity";

    // wwyue
    private int requestTimes = 0;// 请求次数
    private ProgressBar proBar;
    private boolean hasRequestStatus = false;
    private boolean isFirstResult = true;
    private boolean isContactLoad = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommFunc.PrintLog(5, LOGTAG, "onCreate");
        setContentView(R.layout.activity_contact);
        SQLiteManager.getInstance().addObserver(this);
        initView();
        overlayThread = new OverlayThread();
        initOverlay();
        initData();
        registerForContextMenu(listView);
    }

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if(FriendAddActivity.changed) {
			CommFunc.PrintLog(5, LOGTAG, "contact changed");
			FriendAddActivity.changed = false;
			HBaseApp.post2WorkRunnable(new Runnable() {

				@Override
				public void run() {
					list = SQLiteManager.getInstance().getAllContactInfo();

					Message msg = new Message();
					msg.what = MSG_UPDATEQUERY;
					handler.dispatchMessage(msg);
				}
			});
		}
		super.onResume();
		CommFunc.PrintLog(5, LOGTAG, "onResume");
		QueryStatus();
	}

	private void QueryStatus() {
		if (!hasRequestStatus && isContactLoad) {
			queryConStatus();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
        super.onDestroy();
        CommFunc.PrintLog(5, LOGTAG, "onDestroy");
        SQLiteManager.getInstance().deleteObserver(this);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        CommFunc.PrintLog(5, LOGTAG, "onStart");
    }

    private int MSG_UPDATELIST = 100;
    private int MSG_UPDATEQUERY = 101;
    private void initData() {

        CommFunc.PrintLog(5, LOGTAG, "initData()");
        HBaseApp.post2WorkRunnable(new Runnable(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                list = SQLiteManager.getInstance().getAllContactInfo();
                CommFunc.PrintLog(5, LOGTAG, "initData():"+list.size());  
                Message msg = new Message();
                msg.what = MSG_UPDATELIST;
                handler.dispatchMessage(msg);
            }

        });


        //        list = SQLiteManager.getInstance().getAllContactInfo();
        //        CommFunc.PrintLog(5, LOGTAG, "initData() size:"+list.size());
        //        if (list.size() > 0) {
        //            setAdapter(list);
        //           // QueryStatus();
        //        }else {
        //            CommFunc.PrintLog(5, LOGTAG, "initData()MyAsyncQueryHandler");
        //            asyncQuery = new MyAsyncQueryHandler(getContentResolver());
        //            getContact();
        //        }
    }

    private void initView() {
        // TODO Auto-generated method stub
        // wwyue
        proBar = (ProgressBar) findViewById(R.id.bar_contact_title);

        titleView = (TitleViewSimple) findViewById(R.id.titleView);
        letterListView = (MyLetterListView) findViewById(R.id.activity_phone_book_letter_view);
        titleView.setTitle(-1, R.drawable.btn_contact_add,
                getString(R.string.title_contact));
        // titleView.setTitle(-1, R.drawable.btn_contact_add,
        // getString(R.string.title_contact));
        titleView.setOnTitleActed(this);
        listView = (ListView) findViewById(R.id.listview);
        letterListView.setVisibility(View.VISIBLE);
        letterListView
        .setOnTouchingLetterChangedListener(new LetterListViewListener());
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onClickLeftButton() {

    }

    @Override
    public void update(Observable observable, Object data) {
        // CommFunc.PrintLog(5, LOGTAG, "update: +data:"+data.toString());
        // TODO Auto-generated method stub
		if (data != null && adapter != null) {
			// wwyue 拨打电话后重新请求
			if (list.size() == SQLiteManager.getInstance().getAllContactInfo()
					.size()) {
				return;
			}
			list = SQLiteManager.getInstance().getAllContactInfo();
			if (list.size() > 0) {
				setAdapter(list);
				adapter.notifyDataSetChanged();
				// wwyue
				if (!hasRequestStatus) {
					queryConStatus();
					proBar.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	@Override
	public void onClickRightButton() {
		Intent intent = new Intent(this, FriendAddActivity.class);
        startActivity(intent);
    }

    /**
     * 获取系统通讯录
     * 
     * @return
     */
    public void getContact() {
        //  ArrayList<TContactInfo> list = new ArrayList<TContactInfo>();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
        String[] projection = { ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.DATA1, "sort_key",
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY }; // 查询的列
        asyncQuery.startQuery(0, null, uri, projection, null, null,
        "sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询

    }

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

                contactIdMap = new HashMap<Integer, TContactInfo>();

                list = new ArrayList<TContactInfo>();
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

                        if (number != null) {
                            // CommFunc.PrintLog(5, LOGTAG,
                            // "number have space before:"+number );
                            number = number.replace(" ", "");
                            // CommFunc.PrintLog(5, LOGTAG,
                            // "number have space after:"+number );
                        }

                        TContactInfo cb = new TContactInfo();
                        cb.setName(name);
                        if (number != null && number.startsWith("+86")) {// 去除多余的中国地区号码标志，对这个程序没有影响。
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
                CommFunc.PrintLog(5, LOGTAG, "saveContactInfoList size:" + list.size());
                // TODO 测试用，需要修改
                // SQLiteManager.getInstance().deleteAllContactInfo(true);
                SQLiteManager.getInstance().saveContactInfoList(list, true);
                HBaseApp.post2WorkRunnable(new Runnable() {

                    @Override
                    public void run() {
                        list = SQLiteManager.getInstance().getAllContactInfo();
                        CommFunc.PrintLog(5, LOGTAG, "getAllContactInfo size:" + list.size());
                        Message msg = new Message();
                        msg.what = MSG_UPDATEQUERY;
                        handler.dispatchMessage(msg);
                    }
                });
            }

        }
    }

    private class OverlayThread implements Runnable {

        @Override
        public void run() {
            overlay.setVisibility(View.GONE);
            }

    }

    private void initOverlay() {
        LayoutInflater inflater = LayoutInflater.from(this);
        overlay = (TextView) inflater.inflate(
                R.layout.dialog_phone_book_overlay, null);
        overlay.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        windowManager = (WindowManager) this
        .getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(overlay, lp);
    }

    private class LetterListViewListener implements
    OnTouchingLetterChangedListener {

        @Override
        public void onTouchingLetterChanged(final String s) {
            if (adapter.getAlphaInder().get(s) != null) {
                int position = adapter.getAlphaInder().get(s);
                listView.setSelection(position);
                overlay.setText(adapter.getSections()[position]);
                overlay.setVisibility(View.VISIBLE);
                handler.removeCallbacks(overlayThread);

                handler.postDelayed(overlayThread, 1500);
            }
        }

    }

    private void setAdapter(List<TContactInfo> list) {
        adapter = new ContactAdapter(ContactActivity.this, list, false);
        listView.setAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        // TODO Auto-generated method stub
        TContactInfo info;
        AdapterView.AdapterContextMenuInfo info1 = null;
        info1 = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (adapter != null && adapter.getCount() != 0) {
            info = adapter.getItem(info1.position);
            if (info.getName() != null && !info.getName().equals("")) {
                menu.setHeaderTitle(info.getName());
            } else {
                menu.setHeaderTitle(info.getPhoneNum());
            }

            menu.add(1, CONTEXT_MENU_LOOK, 0, R.string.menu_look_info);
            // menu.add(1,CONTEXT_MENU_ADD,0,R.string.menu_add_group);
            // menu.add(1,CONTEXT_MENU_DELETE,0,R.string.menu_delete);
            // menu.add(1,CONTEXT_MENU_SHIELD,0,R.string.menu_shield);
            // menu.add(1,CONTEXT_MENU_DELETES,0,R.string.menu_deletes);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    /**
     * 跳转到通话记录界面
     * 
     * @param number
     * @param info
     */
    private void toCallDetailActivity(TContactInfo info) {
        Intent intent = new Intent(this, ContactDetailActivity.class);
        intent.putExtra(MsgKey.intent_key_object, info);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        TContactInfo info = list.get(arg2);
        toCallDetailActivity(info);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        Intent intent;
        TContactInfo info = null;
        AdapterView.AdapterContextMenuInfo menuInfo = null;
        menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (adapter != null && adapter.getCount() != 0) {
            info = adapter.getItem(menuInfo.position);
        }
        if (info == null) {
            CommFunc.PrintLog(5, LOGTAG,
                    "onContextItemSelected ContactInfo is null");
        }
        switch (item.getItemId()) {
            case CONTEXT_MENU_LOOK:
                toCallDetailActivity(info);
                break;
            case CONTEXT_MENU_ADD: // 添加到群组
                intent = new Intent(this, MemberAddToGroupActivity.class);
                intent.putExtra("number", info.getPhoneNum());
                startActivity(intent);
                break;
            case CONTEXT_MENU_DELETE:// 删除
                showDeleteContact(info.getPhoneNum());
                break;
            case CONTEXT_MENU_SHIELD:// 屏蔽
                break;
            case CONTEXT_MENU_DELETES:// 批量删除
                intent = new Intent(this, EditContactActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void showDeleteContact(final String number) {
        // TODO Auto-generated method stub
        new AlertDialog.Builder(this)
        .setMessage(getString(R.string.dialog_delete_contact_message))
        .setPositiveButton(R.string.info_btn_ok,
                new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog,
                    int which) {
                // TODO Auto-generated method stub
                // 删除联系人

            }
        })
        .setNegativeButton(R.string.info_btn_cancel,
                new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog,
                    int which) {
                // TODO Auto-generated method stub

            }
        }).create().show();
    }

	// wwyue
	public void queryConStatus() {
		if(proBar != null) {
			proBar.setVisibility(View.INVISIBLE);
			return;
		}
		if (isFirstResult) {
			isFirstResult = false;
			proBar.setVisibility(View.VISIBLE);
		}
		HBaseApp.post2WorkRunnable(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				hasRequestStatus = true;
				int sum = list.size();
				int x = (int) sum / 100;
				int y = sum % 100;
				for (int i = 0; i <= x; i++) {
					String params = "";
					for (int j = (0 + i) * 100; j < 100 * (i + 1); j++) {
						if (i == x && j == 100 * i + y) {
							break;
						}
						params += ",10-" + list.get(j).getPhoneNum();
					}
					if(params.length() > 0)
						params = params.substring(1, params.length());
					else
						break;
					String statusParams = HttpManager.getInstance()
							.CreateQueryUserStatus(params, 0).toString();
					MyApplication.getInstance().QueryStatus(
							ContactActivity.this, statusParams);
				}

			}

		});
	}

    // wwyue
    public void freshConStatus(int status, String params) {
        if (status == 0 && hasRequestStatus) {
            requestTimes++;
            int times = 1 + (int) list.size() / 100;
            try {
                JSONObject object = new JSONObject(params);
                JSONArray array = object.getJSONArray("userStatusList");
                for (int i = 0; i < times; i++) {
                    if (((JSONObject) array.get(0)).getString("appAccountId")
                            .contains(list.get((0 + i) * 100).getPhoneNum())) {
                        for (int j = 0; j < array.length(); j++) {
                            list.get((i * 100) + j).setStatus(
                                    ((JSONObject) array.get(j))
                                    .getString("status"));
                        }
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                updateConStatus();
            } finally {
                if (requestTimes == times && hasRequestStatus) {
                    updateConStatus();
                }
            }
        } else if (hasRequestStatus) {
            updateConStatus();
        }
    }

    // wwyue
    public void updateConStatus() {
        Collections.sort(list, comparator);
        adapter.notifyDataSetChanged();
        hasRequestStatus = false;
        requestTimes = 0;
        proBar.setVisibility(View.INVISIBLE);
    }

    // wwyue
    Comparator<TContactInfo> comparator = new Comparator<TContactInfo>() {

        @Override
        public int compare(TContactInfo lhs, TContactInfo rhs) {
            if (lhs.getStatus().compareTo("1") < 0 && rhs.getStatus().compareTo("1") < 0) {
                return 0;
            }else if (rhs.getStatus().compareTo(lhs.getStatus()) > 0) {
                return 1;
            } else if (rhs.getStatus().compareTo(lhs.getStatus()) < 0) {
                return -1;
            } else {
                return 0;
            }
        }
	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == MSG_UPDATELIST) {
				if (list.size() > 1) {// 自己的数据提前入库
					HBaseApp.post2WorkRunnable(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							setAdapter(list);
						}
					});
					// wwyue
					isContactLoad = true;
				} else {
					CommFunc.PrintLog(5, LOGTAG,
							"initData()MyAsyncQueryHandler");
					asyncQuery = new MyAsyncQueryHandler(getContentResolver());
					getContact();
				}
				CommFunc.PrintLog(5, LOGTAG,
						"onQueryComplete getAllContactInfo:" + list.size());
			} else if (msg.what == MSG_UPDATEQUERY) {
				HBaseApp.post2UIRunnable(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (list.size() > 1) {
							setAdapter(list);
							// wwyue
							isContactLoad = true;
							queryConStatus();
						}
					}

				});
			}
		}

	};
}
