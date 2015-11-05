package com.sip.rtcclient.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.sip.rtcclient.MyApplication;

/**
 * 贴脸黑屏
 */
public class NearBlackScreen {
	private SensorManager mManager;
	private Sensor mSensor = null;
	private PowerManager localPowerManager = null;
	private WakeLock localWakeLock = null;
	private Context context;
	private static final String LOGTAG = "NearBlackScreen";
	private static NearBlackScreen instance;
	
	public static NearBlackScreen getInstance() {
		if (instance == null) {
			instance = new NearBlackScreen();
		}
		return instance;
	}
	
	public NearBlackScreen(){
		this.context = MyApplication.getInstance();
		initBlackScreen(); 
	}
	
	private SensorEventListener mListener = new SensorEventListener() {// 注册感应器事件

		@Override
		public void onSensorChanged(SensorEvent event) {
			float[] its = event.values;
			if (its != null
					&& event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
				// 经过测试，当手贴近距离感应器的时候its[0]返回值为0.0，当手离开时返回1.0
				if (its[0] == 0.0 || its[0] == 3.0) {// 贴近手机
					if (localWakeLock.isHeld()) {
						return;
					} else
						localWakeLock.acquire();// 申请设备电源锁
				} else {// 远离手机
					if (localWakeLock.isHeld()) {
						return;
					} else
						localWakeLock.setReferenceCounted(false);
					localWakeLock.release(); // 释放设备电源锁
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};
	
	//贴脸黑屏设置
	private void initBlackScreen() {
		// 获取系统服务POWER_SERVICE，返回一个PowerManager对象
		localPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		// 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
		localWakeLock = this.localPowerManager.newWakeLock(32, "rtc");
		// 获取系统服务SENSOR_SERVICE，返回一个SensorManager对象
		mManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		// 获取距离感应器对象
		mSensor = mManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
	}
	
	/**
	 * 开启贴脸黑屏监听
	 */
	public void start() {
		// 注册监听
		mManager.registerListener(mListener, mSensor,
				SensorManager.SENSOR_DELAY_GAME);
	}
	
	/**
	 * 关闭贴脸黑屏
	 */
	public void stop(){
		mManager.unregisterListener(mListener);
		if(localWakeLock != null && localWakeLock.isHeld())
		{
			localWakeLock.release(); // 释放设备电源锁
			lightScreen();//最后点亮屏幕
		}
	}
	
	//点亮屏幕
	private void lightScreen(){
	WakeLock wakeLock = localPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.ON_AFTER_RELEASE,"" );
	wakeLock.acquire();
	wakeLock.release();
	}
}
