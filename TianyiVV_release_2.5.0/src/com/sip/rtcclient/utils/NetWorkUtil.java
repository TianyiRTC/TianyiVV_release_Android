package com.sip.rtcclient.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;


import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.sip.rtcclient.MyApplication;
import com.sip.rtcclientouter.R;


public class NetWorkUtil {

    private static String LOGTAG="NetWorkUtil";
    /** 没有网络 */
    public static final int NETWORKTYPE_INVALID = 0;
    /** 2G网络 */
    public static final int NETWORKTYPE_2G = 1;
    /** 3G和3G以上网络，或统称为快速网络 */
    public static final int NETWORKTYPE_3G = 2;// 非联通3G
    /** wifi网络 */
    public static final int NETWORKTYPE_WIFI = 3;
    /** 3G环境为中国联通 */
    public static final int NETWORKTYPE_3G_UNICOM = 4; // 联通3G
    /** wap网络 */
    public static final int NETWORKTYPE_WAP = 5;

    /**
     * 判断是否是FastMobileNetWork，将3G或者3G以上的网络称为快速网络
     * 
     * @param context
     * @return
     */
    private static boolean isFastMobileNetwork(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context
        .getSystemService(Context.TELEPHONY_SERVICE);
        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return false; // ~ 14-64 kbps
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return true; // ~ 400-1000 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return true; // ~ 600-1400 kbps
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return false; // ~ 100 kbps
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return true; // ~ 2-14 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return true; // ~ 700-1700 kbps
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return true; // ~ 1-23 Mbps
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return true; // ~ 400-7000 kbps
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return true; // ~ 1-2 Mbps
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return true; // ~ 5 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return true; // ~ 10-20 Mbps
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return false; // ~25 kbps
            case TelephonyManager.NETWORK_TYPE_LTE:
                return true; // ~ 10+ Mbps
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return false;
            default:
                return false;
        }
    }

    /**
     * 获取网络类型
     * 
     * @param context
     * @return
     */
    public static int getNetWorkType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = manager.getActiveNetworkInfo();
        if (netInfo == null) {
            // 无网络连接提示
            DialogUtil.showShortToast(context, MyApplication.getInstance()
                    .getString(R.string.info_no_net));
            return NETWORKTYPE_INVALID;
        }
        if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return NETWORKTYPE_WIFI;
        } else if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
//            String proxyHost = android.net.Proxy.getDefaultHost();
            String proxyHost =System.getProperty("http.proxyHost");
            if (proxyHost != null && !proxyHost.equals(""))
                return NetWorkUtil.NETWORKTYPE_WAP;

            if (isFastMobileNetwork(context) == false)
                return NetWorkUtil.NETWORKTYPE_2G;
            int type = ((TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE))
                    .getNetworkType();
            if (type == TelephonyManager.NETWORK_TYPE_HSPA
                    || type == TelephonyManager.NETWORK_TYPE_UMTS)
                return NetWorkUtil.NETWORKTYPE_3G_UNICOM;
            else
                return NetWorkUtil.NETWORKTYPE_3G;
        }

        return NETWORKTYPE_INVALID;
    }

    /**
     * 获取Ip 地址
     * 
     * @return
     */
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            CommFunc.PrintLog(1,LOGTAG,MyApplication.getInstance().getString(
                    R.string.exception_ipaddress));
        }
        return null;
    }

    /**
     * 判断WIFI是否处于连接状态
     * 
     * @param context
     * @return
     */
    public static boolean IsWIFIConnect(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetInfo = connectivityManager
            .getActiveNetworkInfo();
            if (activeNetInfo != null) {
                NetworkInfo Info = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (Info != null) {
                    String strType = Info.getExtraInfo();
                    if (strType != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断cmap是否处于连接状态
     * 
     * @param context
     * @return
     */
    public static boolean IsWapNetConnect(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetInfo = connectivityManager
            .getActiveNetworkInfo();
            if (activeNetInfo != null) {
                NetworkInfo mobNetInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (mobNetInfo != null) {
                    String strType = mobNetInfo.getExtraInfo();
                    if (strType != null && strType.equals("cmwap") == true) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断网络是否可用
     * 
     * @param context
     * @return
     */
//    public static boolean isNetworkAvailable(Context context) {
//        ConnectivityManager connectivity = (ConnectivityManager) context
//        .getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (connectivity == null) {
//            return false;
//        } else {
//            NetworkInfo[] info = connectivity.getAllNetworkInfo();
//            if (info != null) {
//                for (int i = 0; i < info.length; i++) {
//                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
    public static boolean isNetConnect(Context context) {

        ConnectivityManager manager = (ConnectivityManager) MyApplication
        .getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        if (networkinfo == null || !networkinfo.isAvailable()) {
            return false;
        }
        if (networkinfo != null && networkinfo.isConnected()) {
            // 判断当前网络是否已经连接
            if (networkinfo.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }else{
                return false;
            }
        }
        return false;
    }

    /***   
     * 判断Network具体类型（联通移动wap，电信wap，其他net）   
     *      
     * */   

    public static final    String CTWAP = "ctwap";    
    public static final    String CMWAP = "cmwap";    
    public static final    String WAP_3G = "3gwap";    
    public static final    String UNIWAP = "uniwap";    
    public static final    int TYPE_NET_WORK_DISABLED = 0;// 网络不可用    
    public static final    int TYPE_CM_CU_WAP = 4;// 移动联通wap10.0.0.172    
    public static final    int TYPE_CT_WAP = 5;// 电信wap 10.0.0.200    
    public static final    int TYPE_OTHER_NET = 6;// 电信,移动,联通,wifi 等net网络  
    public static Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn"); 

    public static int checkNetworkType(Context mContext) {    
        try {    
            final ConnectivityManager connectivityManager = (ConnectivityManager) mContext    
            .getSystemService(Context.CONNECTIVITY_SERVICE);    
            final NetworkInfo mobNetInfoActivity = connectivityManager    
            .getActiveNetworkInfo();    
            if (mobNetInfoActivity == null || !mobNetInfoActivity.isAvailable()) {    
                // 注意一：    
                // NetworkInfo 为空或者不可以用的时候正常情况应该是当前没有可用网络，    
                // 但是有些电信机器，仍可以正常联网，    
                // 所以当成net网络处理依然尝试连接网络。    
                // （然后在socket中捕捉异常，进行二次判断与用户提示）。    

                Log.i("", "=====================>无网络");    
                return TYPE_OTHER_NET;    
            } else {    

                // NetworkInfo不为null开始判断是网络类型    

                int netType = mobNetInfoActivity.getType();    
                if (netType == ConnectivityManager.TYPE_WIFI) {    
                    // wifi net处理    
                    Log.i("", "=====================>wifi网络");    
                    return TYPE_OTHER_NET;    
                } else if (netType == ConnectivityManager.TYPE_MOBILE) {    


                    // 注意二：    
                    // 判断是否电信wap:    
                    //不要通过getExtraInfo获取接入点名称来判断类型，    
                    // 因为通过目前电信多种机型测试发现接入点名称大都为#777或者null，    
                    // 电信机器wap接入点中要比移动联通wap接入点多设置一个用户名和密码,    
                    // 所以可以通过这个进行判断！    

                     Cursor c = mContext.getContentResolver().query(    
                            PREFERRED_APN_URI, null, null, null, null);    
                    if (c != null) {    
                        c.moveToFirst();    
                        final String user = c.getString(c    
                                .getColumnIndex("user"));    
                        if (!TextUtils.isEmpty(user)) {    
                            Log.i("",    
                                    "=====================>代理："    
                                    + c.getString(c    
                                            .getColumnIndex("proxy")));    
                            if (user.startsWith(CTWAP)) {    
                                Log.i("", "=====================>电信wap网络");    
                                return TYPE_CT_WAP;    
                            }    
                        }    
                    }
                    if(c!=null)
                    {
                        c.close();
                        c = null;
                    }


                    // 注意三：    
                    // 判断是移动联通wap:    
                    // 其实还有一种方法通过getString(c.getColumnIndex("proxy")获取代理ip    
                    //来判断接入点，10.0.0.172就是移动联通wap，10.0.0.200就是电信wap，但在    
                    //实际开发中并不是所有机器都能获取到接入点代理信息，例如魅族M9 （2.2）等...    
                    // 所以采用getExtraInfo获取接入点名字进行判断    

                    String netMode = mobNetInfoActivity.getExtraInfo();    
                    Log.i("", "netMode ================== " + netMode);    
                    if (netMode != null) {    
                        // 通过apn名称判断是否是联通和移动wap    
                        netMode=netMode.toLowerCase(Locale.getDefault());    
                        if (netMode.equals(CMWAP) || netMode.equals(WAP_3G)    
                                || netMode.equals(UNIWAP)) {    
                            Log.i("", "=====================>移动联通wap网络");    
                            return TYPE_CM_CU_WAP;    
                        }    

                    }    

                }    
            }    
        } catch (Exception ex) {    
            ex.printStackTrace();    
            return TYPE_OTHER_NET;    
        }    

        return TYPE_OTHER_NET;    

    }    
}
