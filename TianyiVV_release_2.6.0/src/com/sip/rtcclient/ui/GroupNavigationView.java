package com.sip.rtcclient.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.bean.TContactInfo;
import com.sip.rtcclient.utils.CommFunc;
import com.sip.rtcclient.utils.ScreenUtil;

public class GroupNavigationView extends LinearLayout implements
		OnItemClickListener, OnClickListener, OnTouchListener {

	private String LOGTAG = "NavigationView";
	Context context;
	GridView gridView;
	RelativeLayout layout;
	NavigationAdapter adapter;
	HorizontalScrollView scrollView;
	HandlerThread handlerThread;
	Handler listenHandler;
	Handler mHandler;
	private ImageView left;
	private ImageView right;
	int numCoum;
	NavigationListener navigationListener;
    ImageView img_delete;

	private float listWidth;
	private TypedArray type;

	public void setOnNavigationListener(NavigationListener navigationListener) {
		this.navigationListener = navigationListener;
	}

	public GroupNavigationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initHandler();
		init(context);
		setupViews();
		initViews(attrs);
		setListener();
	}

	/**
	 * 
	 */
	private void initHandler() {
		mHandler = new Handler();
		handlerThread = new HandlerThread("listen scroll end");
		handlerThread.start();
		listenHandler = new Handler(handlerThread.getLooper());
	}

	/**
	 * 
	 * @param context
	 */
	private void init(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		layout = (RelativeLayout) inflater.inflate(
				R.layout.view_group_navigation_main, null);
		LayoutParams layoutParam = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		addView(layout, layoutParam);
	}

	/**
	 * 
	 */
	private void setupViews() {
		left = (ImageView) layout.findViewById(R.id.navigation_left);
		right = (ImageView) layout.findViewById(R.id.navigation_right);
		gridView = (GridView) layout.findViewById(R.id.navigation_list);
		scrollView = (HorizontalScrollView) layout
				.findViewById(R.id.navigation_scrollview);
	}

	/**
	 * 初始化View
	 * 
	 * @param attrs
	 */

	private void initViews(AttributeSet attrs) {
		type = context
				.obtainStyledAttributes(attrs, R.styleable.NavigationView);
		float scrollViewWidth = type.getDimension(
				R.styleable.NavigationView_list_scale,
				LayoutParams.WRAP_CONTENT);

		scrollView.getLayoutParams().width = (int) scrollViewWidth;
	}

	/**
	 * 添加项目
	 * 
	 * @param id
	 * @param requestCode
	 */
	public void addNavigationCell(TContactInfo info) {
		// if (adapter.imgList.size() > numCoum) {
		// CommFunc.PrintLog(1,LOGTAG,context.getString(R.string.error_navigation_item_size));
		// } else {

		adapter.imgList.add(info);
		CommFunc.PrintLog(5, LOGTAG, "info:"+info.getName());
		// }
	}

	/**
	 * 
	 */
	private void setListener() {
		//left.setOnClickListener(this);
		//right.setOnClickListener(this);
		scrollView.setOnTouchListener(this);
	}

	/**
	 * 获取选中ID
	 * 
	 * @return
	 */
	public int getCheckedId() {
		if (adapter == null)
			return -1;
		return adapter.getCurCheckedPosition();
	}

	/**
	 * 刷新GridView
	 * 
	 * @param numCoum
	 */
	public void refreshNavigationView(boolean showDeleteImg) {
		try {
			gridView.setNumColumns(getNumCoum());

			gridView.getLayoutParams();
			gridView.getLayoutParams().width = (int) getListWidth();

			adapter = new NavigationAdapter(context, getNumCoum(), showDeleteImg);
			gridView.setAdapter(adapter);
			gridView.setOnItemClickListener(this);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void cleanList() {
		if (adapter != null) {
			adapter.imgList.clear();
		}
	}

	public void notityChange() {
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * 更新左右伸缩按钮状态
	 */
	private void updateScalingButton() {
		if (visibleScalingBtn()) {
			listenHandler.post(new Runnable() {
				int lastScrollX;

				@Override
				public void run() {
					while (true) {
						lastScrollX = scrollView.getScrollX();
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (scrollView.getScrollX() == lastScrollX) {
							endScroll();
							return;
						}
					}
				}

			});
		}
	}

	/**
	 * 更新左右伸缩按钮状态
	 */
	private void endScroll() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				int x = scrollView.getScrollX();
				if (x <= 5) {
					setLeftNoSelector();
				} else {
					setLeftSelector();
				}
				if (x >= gridView.getWidth() - scrollView.getWidth()-5) {
					setRightNoSelector();
				} else {
					setRightSelector();
				}
			}
		});

	}

	/**
	 * 是否显示左右伸缩按钮
	 */
	public boolean visibleScalingBtn() {
		if (listWidth <= ScreenUtil.getScreenWidth(context)) {
			setLeftNoSelector();
			setRightNoSelector();
			return false;
		} else {
			setLeftSelector();
			setRightSelector();
			return true;
		}
	}

	private void setLeftSelector() {
		//left.setBackgroundResource(R.drawable.view_navigationt_left_selector);
		left.setVisibility(View.VISIBLE);
	}

	private void setRightSelector() {
		//right.setBackgroundResource(R.drawable.view_navigationt_right_selector);
		right.setVisibility(View.VISIBLE);
	}

	private void setLeftNoSelector() {
		//left.setBackgroundColor(Color.BLACK);
		left.setVisibility(View.INVISIBLE);
	}

	private void setRightNoSelector() {
		//right.setBackgroundColor(Color.BLACK);
		right.setVisibility(View.INVISIBLE);
	}

	private class NavigationAdapter extends BaseAdapter {
		private Context context;
		private ArrayList<TContactInfo> imgList = new ArrayList<TContactInfo>();
		public int curCheckedPosition = 0;
		public boolean showDeleteImg;

		public NavigationAdapter(Context c, int size, boolean showDeleteImg)
				throws IllegalArgumentException, IllegalAccessException {
			context = c;
			this.showDeleteImg = showDeleteImg;
		}

		public int getCurCheckedPosition() {
			return curCheckedPosition;
		}

		/**
		 * 重新设置gridView的Adapter然后在notifiy TODO
		 * 
		 * @param curCheckedPosition
		 */
		public void setCurCheckedPosition(int curCheckedPosition) {
			this.curCheckedPosition = curCheckedPosition;
			gridView.setAdapter(adapter);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return imgList.size();
		}

		@Override
		public Object getItem(int position) {
			return imgList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = ((Activity) context).getLayoutInflater().inflate(
						R.layout.view_group_navigation_item, null);
			}
			TContactInfo info = imgList.get(position);
			ImageView img_avatar = (ImageView) convertView
					.findViewById(R.id.img_avatar);
			img_avatar.setTag(position);
			img_avatar.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    int position = (Integer) v.getTag();
                    if(position!=-1&&navigationListener!=null)
                    navigationListener.onClick(position, ""); 
                }
            });			
			
			 img_delete = (ImageView) convertView
					.findViewById(R.id.img_delete);
			TextView tv_name = (TextView) convertView
					.findViewById(R.id.tv_name);
			img_avatar
					.setBackgroundResource(R.drawable.call_video_default_avatar);
			//if(info!=null)
			tv_name.setText(info.getName());
			if (showDeleteImg) {
				img_delete.setVisibility(View.VISIBLE);
			}else {
				img_delete.setVisibility(View.GONE);
			}
			return convertView;
		}
	}

	public interface NavigationListener {
		void onClick(int requestCode, String tag);
	}
	public void SetImageDelete(int viewtype)
	{  
	    if(img_delete!=null)
	    img_delete.setVisibility(viewtype);
	}

	// class OnCheckBoxCheckedChangeListener implements OnCheckedChangeListener
	// {
	// int position;
	//
	// OnCheckBoxCheckedChangeListener(int position) {
	// this.position = position;
	// }
	//
	// @Override
	// public void onCheckedChanged(CompoundButton buttonView,
	// boolean isChecked) {
	// if (position == 0) {
	// Intent intent = new Intent(CallRecordActivity.BROADCAST_DISMISS_DIALPAD);
	// MyApplication.getInstance().sendBroadcast(intent);
	// }
	// if (buttonView.isChecked()) {
	// if (GroupNavigationView1.this.navigationListener != null
	// && adapter.getCurCheckedPosition() != position) {
	// navigationListener.onClick(Integer.parseInt(adapter.imgList
	// .get(position)[1].toString()), adapter.imgList
	// .get(position)[2].toString());
	// }
	// adapter.setCurCheckedPosition(position);
	// } else {
	// if (position == adapter.getCurCheckedPosition()) {
	// adapter.setCurCheckedPosition(position);
	// }
	// }
	// }
	// }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		CheckBox cb = (CheckBox) view.findViewById(R.id.item_checkbox);
		if (cb != null) {
			cb.setChecked(!cb.isChecked());
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			updateScalingButton();
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.navigation_left:
			scrollView.arrowScroll(View.FOCUS_LEFT);
			updateScalingButton();
			break;
		case R.id.navigation_right:
			scrollView.arrowScroll(View.FOCUS_RIGHT);
			updateScalingButton();
			break;
		default:
			break;
		}
	}

	public void setNumCoum(int numCoum) {
		this.numCoum = numCoum;
	}

	public int getNumCoum() {
		return numCoum;
	}

	public GridView getGridView() {
		return gridView;
	}

	public void setGridView(GridView gridView) {
		this.gridView = gridView;
	}

	public HorizontalScrollView getScrollView() {
		return scrollView;
	}

	public void setScrollView(HorizontalScrollView scrollView) {
		this.scrollView = scrollView;
	}

	public float getListWidth() {
		return listWidth;
	}

	public void setListWidth(float listWidth) {
		this.listWidth = listWidth;
	}

}
