package com.sip.rtcclient.tools;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

import com.sip.rtcclient.MyApplication;

public class LocalActManager {
    
    private String LOGTAG = "LocalActManager";
    private List<Activity> mList = new ArrayList<Activity>();
    
    public static LocalActManager getInstance() {
        return MyApplication.getInstance().getLocalActManager();
    }
    public void addActivity(Activity activity) {
        mList.add(activity);
    }

    public void clearActivity(Activity act) {
        try {
            for (int i = 0; i < mList.size(); i++) {
                Activity activity = mList.get(i);
                if (activity != null && activity != act) {
                    activity.finish();
                }
            }
        } catch (Exception e) {
   //         CommFunc.PrintLog(5, LOGTAG, "clearActivity:" + e.getMessage());
        }
    }

    public void removeActivity(Activity act) {
        try {
            for (int i = 0; i < mList.size(); i++) {
                Activity activity = mList.get(i);
                if (activity != null && activity == act) {
                    mList.remove(i);
                }
            }
        } catch (Exception e) {
          }
    }
    
    /**
     * finish������mList�е�Activity
     */
    public void finishActivity(){
    	for(Activity activity : mList) {  
            activity.finish();  
        }  
        mList.clear();
    }

}
