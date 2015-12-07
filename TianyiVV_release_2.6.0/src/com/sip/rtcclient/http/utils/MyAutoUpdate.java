package com.sip.rtcclient.http.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import com.sip.rtcclientouter.R;
import com.sip.rtcclient.config.SysConfig;
import com.sip.rtcclient.ui.Dialog_model;
import com.sip.rtcclient.ui.Dialog_model.OnDialogClickListener;
import com.sip.rtcclient.utils.CommFunc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.webkit.URLUtil;
/**
 *
 * 1.Set apkUrl.
 *
 * 2.check().
 *
 * 3.add delFile() method in resume()\onPause().
 */
public class MyAutoUpdate {
    private Context context = null;
    public int versionCode = 0;
    public String versionName = "";
    private static final String LOGTAG = "MyAutoUpdate";
    private String currentFilePath = "";
    private String currentTempFilePath = "";
    private String fileEx = "";
    private String fileNa = "";
    private String strURL ="";
    private ProgressDialog dialog;
    private String newversion;
    private String strdesc ="";

    public MyAutoUpdate(Context context) {
        this.context = context;
        // getCurrentVersion();
    }
    public void setDownLoadInfo(String url,String newversion,String desc)
    {
        CommFunc.PrintLog(5, "MyAutoUpdate", "setDownLoadInfo:"+url+" desc:"+desc+" version:"+newversion);
        this.strURL = url;
        this.newversion = newversion;
        strdesc = "系统检测到了天翼VV发布了\r\n最新版本,请及时更新！\r\n版本号:"+newversion+"\r\n";
        CommFunc.PrintLog(5, "MyAutoUpdate", "setDownLoadInfo:"+strdesc);

        if(desc!=null && desc.equals("")==false)
        {
            strdesc +=desc;
            CommFunc.PrintLog(5, "MyAutoUpdate", "desc!=null setDownLoadInfo:"+strdesc);

        }
    }
    public boolean check() {
        if (isNetworkAvailable(this.context) == false) {
            return false;
        }
        showUpdateDialog();
        return true;
    }
    public static boolean isNetworkAvailable(Context ctx) {
        try {
            ConnectivityManager cm = (ConnectivityManager) ctx
            .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            return (info != null && info.isConnected());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    //    .setTitle("升级提示")
    //    .setIcon(R.drawable.icon)

    private Dialog_model dialog1; //
	private FileOutputStream fos;
    public void showUpdateDialog() {
        CommFunc.PrintLog(5, LOGTAG, "showUpdateDialog()");
        if (dialog1 == null) {
            dialog1 = new Dialog_model(context, R.style.FloatDialog);
        }
        dialog1.setMessageText("确定",
                "取消",
                strdesc);
        dialog1.setOnDialogClickListener(new OnDialogClickListener() {
            @Override
            public void onClickRightButton() {
                if (dialog1 != null && dialog1.isShowing())
                    dialog1.dismiss();
            }

            @Override
            public void onClickLeftButton() {

                dialog1.dismiss();
                downloadTheFile(strURL);
                showWaitDialog();  
            }
        });
        dialog1.show();
    }
    //    AlertDialog alert = null;
    //    public void showUpdateDialog() {  

    //      @SuppressWarnings("unused")   
    //       String str = "系统检测到了天翼VV发布了\r\n最新版本,请及时更新！\r\n版本号:"+newversion;

    //         alert = new AlertDialog.Builder(this.activity)  
    //        .setMessage(str)   
    //        .setPositiveButton("确定",   
    //                new DialogInterface.OnClickListener() {   
    //            public void onClick(DialogInterface dialog,   
    //                    int which) { 
    //                
    //                alert.dismiss();
    //                downloadTheFile(strURL);                  
    //                showWaitDialog();  
    //                Log.e(TAG, "AlertDialog 确定");
    //
    //            }   
    //        })   
    //        .setNegativeButton("取消",   
    //                new DialogInterface.OnClickListener() {   
    //            public void onClick(DialogInterface dialog,   
    //                    int which) {   
    //                dialog.cancel();   
    //                Log.e(TAG, "AlertDialog 取消");
    //            }   
    //        }).show();  

    //    }   
    public void showWaitDialog() {
        dialog = new ProgressDialog(context);
        dialog.setMessage("Waitting for update...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.show();
    }
    public void hideWaitDialog()
    {
        if(dialog!=null)
        {
            dialog.cancel();
            dialog.dismiss();
            dialog = null;
        }

    }
    private void downloadTheFile(final String strPath) {
        fileEx = strURL.substring(strURL.lastIndexOf(".") + 1, strURL.length())
        .toLowerCase(Locale.getDefault());
        fileNa = strURL.substring(strURL.lastIndexOf("/") + 1,
                strURL.lastIndexOf("."));
        CommFunc.PrintLog(5, LOGTAG, "downloadTheFile fileEx:"+fileEx  +"  fileNa:"+fileNa);
        try {
            if (strPath.equals(currentFilePath)) {
                doDownloadTheFile(strPath);
            }
            currentFilePath = strPath;
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        doDownloadTheFile(strPath);
                    } catch (Exception e) {
                        hideWaitDialog();
                        Log.e(LOGTAG,"doDownloadTheFile e:"+ e.getMessage(), e);
                    }
                }
            };
            new Thread(r).start();
        } catch (Exception e) {
            hideWaitDialog();
            Log.e(LOGTAG,"Exception downloadfile:"+ e.getMessage(), e);
            //e.printStackTrace();
        }
    }
    private void doDownloadTheFile(String strPath) throws Exception {
        Log.e(LOGTAG, "doDownloadTheFile :"+strPath);
        if (!URLUtil.isNetworkUrl(strPath)) {
            Log.i(LOGTAG, "getDataSource() It's a wrong URL!");
        } else {

            try {
                URL myURL = new URL(strPath);
                URLConnection conn = myURL.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                if (is == null) {
                   // throw new RuntimeException("stream is null");
                    Log.e(LOGTAG, "stream is null file not exits");
                }

                File appDir = new File(SysConfig.getInstance().getAppFolder());
                if (!appDir.exists()) {
                    CommFunc.PrintLog(5,LOGTAG, "doDownloadTheFile-----------创建文件夹");
                    appDir.mkdirs();
                }

                String saveFilePath = SysConfig.getInstance().getAppFolder() + "/"
                + this.fileNa+"."+fileEx;

                File apkFile = new File(saveFilePath);
                if (apkFile.exists()) {
                    CommFunc.PrintLog(5,LOGTAG, "doDownloadTheFile-----------文件已经存在");
                    //先删除原先下载的。
                    deleteFile(saveFilePath);
                   // return ;
                }

//                File myTempFile = File.createTempFile(fileNa, "." + fileEx);
//                currentTempFilePath = myTempFile.getAbsolutePath();
                File myTempFile = new File(saveFilePath);

                fos = new FileOutputStream(myTempFile);
                currentTempFilePath = myTempFile.getAbsolutePath();
                CommFunc.PrintLog(5, LOGTAG, "myTempFile:"+myTempFile);
                CommFunc.PrintLog(5, LOGTAG, "currentTempFilePath:"+currentTempFilePath);
                byte buf[] = new byte[128];
                do {
                    int numread = is.read(buf);
                    if (numread <= 0) {
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (true);
                Log.i(LOGTAG, "getDataSource() Download  ok...");
                hideWaitDialog();
                openFile(myTempFile);
                is.close();
            } catch (Exception ex) {
                hideWaitDialog();
                Log.e(LOGTAG, "getDataSource() error: " + ex.getMessage(), ex);
                //需要此处异常处理
                CommFunc.DisplayToast(context, "下载失败:"+ex.getMessage());
            }
        }
    }

    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.isFile() && file.exists()) {
            file.delete();
            CommFunc.PrintLog(5, LOGTAG,"删除单个文件" + fileName + "成功！");
            return true;

        } else {
            CommFunc.PrintLog(5, LOGTAG,"删除单个文件" + fileName + "失败！");
            return false;

        }

    }
    private void openFile(File f) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        String type = getMIMEType(f);
        intent.setDataAndType(Uri.fromFile(f), type);
        context.startActivity(intent);
        //   android.os.Process.killProcess(android.os.Process.myPid());// 如果不加上这句的话在apk安装
    }

    private String getMIMEType(File f) {
        String type = "";
        String fName = f.getName();
        String end = fName
        .substring(fName.lastIndexOf(".") + 1, fName.length())
        .toLowerCase(Locale.getDefault());
        if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
                || end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
            type = "audio";
        } else if (end.equals("3gp") || end.equals("mp4")) {
            type = "video";
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
                || end.equals("jpeg") || end.equals("bmp")) {
            type = "image";
        } else if (end.equals("apk")) {
            type = "application/vnd.android.package-archive";
        } else {
            type = "*";
        }
        if (end.equals("apk")) {
        } else {
            type += "/*";
        }
        return type;
    }
}