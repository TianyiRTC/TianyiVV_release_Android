package com.sip.rtcclient.services;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
		
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {     // boot  
			Intent intent1=new Intent(Intent.ACTION_MAIN);
		     //CommFunc.DisplayToast(context, "onReceive 123");
		    System.out.println("11111111111111111111111111111111111111111");
//			Intent intent1=new Intent(context, WelcomeActivity.class);
			intent1.addCategory(Intent.CATEGORY_HOME);
			intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent1);
		}
	}
}
