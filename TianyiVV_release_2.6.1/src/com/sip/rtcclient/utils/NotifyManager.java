package com.sip.rtcclient.utils;

import java.util.HashSet;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;
import com.sip.rtcclient.activity.CatchActivity;
import com.sip.rtcclient.activity.MainActivity;

public class NotifyManager {
	private final int NEW_FAX_NOTIFY_ID=1001;
	private final int SHT_STATUS_NOTIFY_ID=1002;
	private final int MISS_CALL_NOTIFY_ID=1003;
	private final int SUBMITTING_NOTIFY_ID=1004;
	private final int SUBMIT_FAILURE_NOTIFY_ID=1005;
	private Context context;
	private NotificationManager notificationManager; 
	private int missCallNumber;
	private boolean isCalling;
	private String LOGTAG = "NotifyManager";
	private HashSet<String> faxSet;//新传真保存	
	private HashSet<String> faxSubmittingSet;//正在提交传真集合
	private HashSet<String> faxSubmitFailureSet;//提交失败传真集合
	/**
	 * 点击未接电话通知栏
	 */
	public static final int CLICK_MISS_CALL_ID=2;
	/**
	 * 点击新传真通知栏
	 */
	public static final int CLICK_FAX_ID=3;
	/**
	 * 点击正在提交通知栏
	 */
	public static final int CLICK_SUBMITTING_ID=4;
	/**
	 * 点击提交失败通知栏
	 */
	public static final int CLICK_SUBMIT_FAILURE_ID=5;
	public static NotifyManager getInstance() {
		return MyApplication.getInstance().getNotifyManager();
	}

	public void init()
	{    
		context = MyApplication.getInstance();
		notificationManager = (NotificationManager) context
		.getSystemService(Context.NOTIFICATION_SERVICE);

	}
	public void cancelNotify(){
		missCallNumber = 0;
		if(faxSet!=null){
			faxSet.clear();
			faxSet=null;
		}
		if(faxSubmitFailureSet!=null){
			faxSubmitFailureSet.clear();
			faxSubmitFailureSet=null;
		}
		if(faxSubmittingSet!=null){
			faxSubmittingSet.clear();
			faxSubmittingSet=null;
		}
		if (notificationManager != null) {
			notificationManager.cancel(SHT_STATUS_NOTIFY_ID);
			notificationManager.cancel(NEW_FAX_NOTIFY_ID);
			notificationManager.cancel(MISS_CALL_NOTIFY_ID);
			notificationManager.cancel(SUBMIT_FAILURE_NOTIFY_ID);
			notificationManager.cancel(SUBMITTING_NOTIFY_ID);
		}
	}
	public void showNotification(boolean isOnLine) {
		Notification notification=null;
		if(isOnLine==true){
			notification = new Notification(R.drawable.notify_online_icon, "",
					System.currentTimeMillis());
		} else {
			notification = new Notification(R.drawable.notify_offline_icon, "",
					System.currentTimeMillis());
		}

		RemoteViews rv = new RemoteViews(context.getPackageName(),
				R.layout.notify_view);
		Intent intent = new Intent(context, CatchActivity.class);

		PendingIntent pi = PendingIntent.getActivity(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		notification.contentView = rv;

		notification.contentView.setImageViewResource(R.id.imageView1,
				R.drawable.notify_online_icon);
		notification.contentView.setTextViewText(R.id.textView1, context.getString(R.string.info_online));
		
		notification.contentIntent = pi;
		notificationManager.notify(SHT_STATUS_NOTIFY_ID, notification);
	}
	public void setCalling(boolean isCalling) {
		this.isCalling = isCalling;
	}
	public boolean getCalling()
	{
		return isCalling;
	}

	@SuppressWarnings("deprecation")
	public void callingMissCallNotifycation() {
		isCalling = true;
		if (missCallNumber != 0) {
			Notification notification = null;
			notification = new Notification(R.drawable.notify_message_icon, "",
					System.currentTimeMillis());
			RemoteViews rv = new RemoteViews(context.getPackageName(),
					R.layout.notify_view);
			notification.contentView = rv;
			notification.contentView.setImageViewResource(R.id.imageView1,
					R.drawable.notify_message_icon);
			notification.contentView.setTextViewText(R.id.textView1, context.getString(R.string.info_phone_not_answer_one)
					+ missCallNumber + context.getString(R.string.info_phone_not_answer_two));
			Intent intent;
			PendingIntent pi;
			if (isCalling == true) {
				intent = new Intent(MainActivity.BROADCAST_MISS_CALL);
				pi = PendingIntent.getBroadcast(context, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
			} else {
				intent = new Intent(context, MainActivity.class);
				intent.putExtra("click", CLICK_MISS_CALL_ID);
				pi = PendingIntent.getActivity(context, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
			}
			notification.contentIntent = pi;
			notification.contentView.setTextViewText(R.id.textView2,
					context.getString(R.string.info_on_the_phone));
			notificationManager.notify(MISS_CALL_NOTIFY_ID, notification);
		}
	}

	@SuppressWarnings("deprecation")
	public void cancelCallingMissCallNotifycation() {
		isCalling = false;
		if (missCallNumber != 0) {
			Notification notification = null;
			notification = new Notification(R.drawable.notify_message_icon, "",
					System.currentTimeMillis());
			RemoteViews rv = new RemoteViews(context.getPackageName(),
					R.layout.notify_view);
			Intent intent;
			PendingIntent pi;

			intent = new Intent(context, MainActivity.class);
			intent.putExtra("click", CLICK_MISS_CALL_ID);
			pi = PendingIntent.getActivity(context, 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);

			// notification.flags = Notification.FLAG_ONGOING_EVENT;
			notification.contentView = rv;
			notification.contentView.setImageViewResource(R.id.imageView1,
					R.drawable.notify_message_icon);
			notification.contentView.setTextViewText(R.id.textView1, context.getString(R.string.info_phone_not_answer_one)
					+ missCallNumber + context.getString(R.string.info_phone_not_answer_two));
			notification.contentView.setTextViewText(R.id.textView2, context
					.getString(R.string.app_name));
			notification.contentIntent = pi;

			notificationManager.notify(MISS_CALL_NOTIFY_ID, notification);
		}
	}

	@SuppressWarnings("deprecation")
	public void showMissCallNotification() {
		Notification notification = null;
		notification = new Notification(R.drawable.notify_message_icon,
				context.getString(R.string.info_phone_not_answer), System.currentTimeMillis());
		missCallNumber++;
		RemoteViews rv = new RemoteViews(context.getPackageName(),
				R.layout.notify_view);

		// notification.flags = Notification.FLAG_ONGOING_EVENT;
		notification.contentView = rv;
		notification.contentView.setImageViewResource(R.id.imageView1,
				R.drawable.notify_message_icon);
		notification.contentView.setTextViewText(R.id.textView1, context.getString(R.string.info_phone_not_answer_one)
				+ missCallNumber +  context.getString(R.string.info_phone_not_answer_two));
		Intent intent;
		PendingIntent pi;
		if (isCalling == true) {
			intent = new Intent(MainActivity.BROADCAST_MISS_CALL);
			pi = PendingIntent.getBroadcast(context, 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
		} else {
			intent = new Intent(context, MainActivity.class);
			intent.putExtra("click", CLICK_MISS_CALL_ID);
			pi = PendingIntent.getActivity(context, 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
		}

		notification.contentIntent = pi;
		if (isCalling == false) {
			notification.contentView.setTextViewText(R.id.textView2, context
					.getString(R.string.app_name));
			

		} else {
			notification.contentView.setTextViewText(R.id.textView2,
			context.getString(R.string.info_on_the_phone));
			
		}

		notificationManager.notify(MISS_CALL_NOTIFY_ID, notification);
	}

	public void cancelMissCallNotifucation() {
		missCallNumber = 0;
		notificationManager.cancel(MISS_CALL_NOTIFY_ID);
	}

}
