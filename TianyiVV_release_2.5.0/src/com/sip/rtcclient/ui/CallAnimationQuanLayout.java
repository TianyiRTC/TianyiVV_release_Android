package com.sip.rtcclient.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sip.rtcclientouter.R;
/** 呼叫头像外围圈圈动画 */
public class CallAnimationQuanLayout extends RelativeLayout {

	private static final String LOGTAG = "CallAnimationQuanLayout";

	public static final int ANIMATION_TIME = 3000;  
	public static final int FIRST_SLEEP = 1500;
	private Context context;
//	private CircleView outsideCircle;//内圈
//	private CircleView insideCircle;//外圈
	private ImageView outsideCircle;
	private ImageView insideCircle;
	private ImageView avatar;//头像
	
	public static int BASE_SIZE = 134;//默认头像的高度
	public final static int SPACING = 20;//图片和圈之间的间距
	public static int OUTSIDE_RADIUS = BASE_SIZE + SPACING * 2;//外圈半径
	private int INSIDE_RADIUS = BASE_SIZE + SPACING;//内圈半径
	public final static int TOP_PADDING = 20;//顶部距离
	private boolean bAnimation = true;//是否开启动画

	public CallAnimationQuanLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
//		setAvatar();
		initView();
		startAnimation();
	}

	private void initView(){
		LayoutInflater localLayoutInflater = (LayoutInflater) context
				.getSystemService("layout_inflater");
		View view = localLayoutInflater.inflate(R.layout.view_call_animation_circle, null);
		outsideCircle = (ImageView) view.findViewById(R.id.imageView2);
		insideCircle = (ImageView) view.findViewById(R.id.imageView1);
		avatar = (ImageView) view.findViewById(R.id.imageView3);
		addView(view);
	}
	
	/**
	 * 设置头像 
	 * 粗略设置头像，编辑头像功能未添加，编辑头像功能处理完毕再更改设置头像
	 * TODO 待更改
	 * @param info
	 */
	public void setAvatar(){
		Bitmap bitmap = null;
		bitmap = getRoundedCornerBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.call_video_default_avatar));
//		if (info.getAvatarType() == PhoneBookInfo.AVATAR_TYPE_SYSTEM) {	//系统头像
//		} else {	//自定义头像
//			// do something
//		}
		avatar.setImageBitmap(bitmap);
	}

	/** 停止动画 */
	public void stopAnimation(){
		bAnimation = false;
	}

	/** 开始动画 */
	public void startAnimation(){
		bAnimation = true;
		animation(insideCircle);
		outsideCircle.setVisibility(View.INVISIBLE);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(FIRST_SLEEP);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mHandler.sendMessage(new Message());
			}
		}).start();
	}
	
	/** 动画是否开始 */
	public boolean isAnimation(){
		return bAnimation;
	}
	
	/** 动画 */
	private void animation(final View view){
		if (!bAnimation)
			return;
		//图片渐变清楚
		final AlphaAnimation toClear  = new AlphaAnimation(0.0f,0.8f);   
		//渐变时间   
		toClear.setDuration(ANIMATION_TIME);   

		//图片渐变模糊度 
		final AlphaAnimation toBlur = new AlphaAnimation(0.8f,0.0f);   
		//渐变时间   
		toBlur.setDuration(ANIMATION_TIME - 1000);   

		//展示图片渐变动画   
		view.startAnimation(toClear);   
		//渐变过程监听   
		toClear.setAnimationListener(new AnimationListener() {   
			/**  
			 * 动画开始时  
			 */  
			@Override  
			public void onAnimationStart(Animation animation) {   
	//			System.out.println("动画开始...");   
			}   
			/**  
			 * 重复动画时  
			 */  
			@Override  
			public void onAnimationRepeat(Animation animation) {   
	//			System.out.println("动画重复...");   
			}   
			/**  
			 * 动画结束时  
			 */  
			@Override  
			public void onAnimationEnd(Animation animation) {   
			//	System.out.println("动画结束...");   
				if (bAnimation)
					view.startAnimation(toBlur);  
			}   
		});   


		//展示图片渐变动画   
		//        this.findViewById(R.id.iv_animation_logo).startAnimation(bb);   
		//渐变过程监听   
		toBlur.setAnimationListener(new AnimationListener() {   
			/**  
			 * 动画开始时  
			 */  
			@Override  
			public void onAnimationStart(Animation animation) {   
		//	System.out.println("动画开始...");   
			}   
			/**  
			 * 重复动画时  
			 */  
			@Override  
			public void onAnimationRepeat(Animation animation) {   
//				System.out.println("动画重复...");   
			}   
			/**  
			 * 动画结束时  
			 */  
			@Override  
			public void onAnimationEnd(Animation animation) {   
		//		System.out.println("动画结束...");   
				if (bAnimation)
					view.startAnimation(toClear);  
			}   
		});   
	}

	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			animation(outsideCircle);
			outsideCircle.setVisibility(View.VISIBLE);
		}

	};
	
	/** 图片改为圆型 */
	public Bitmap getRoundedCornerBitmap(Bitmap bitmap) { 
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int radius = width >= height ? height : width;
		
		//截取中间部分
		int x = (width - radius)/2;
		int y = (height - radius)/2;
		
		Bitmap output = Bitmap.createBitmap(width, 
				height, Config.ARGB_8888); 
		Canvas canvas = new Canvas(output); 

		final int color = 0xff424242; 
		final Paint paint = new Paint(); 
		final Rect rect = new Rect(0, 0, radius, radius); 
		final RectF rectF = new RectF(rect); 
		final float roundPx = radius / 2; 

		paint.setAntiAlias(true); 
		canvas.drawARGB(0, 0, 0, 0); 
		paint.setColor(color); 
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint); 

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN)); 
		canvas.drawBitmap(bitmap, rect, rect, paint); 
		return output; 
		}  
}
