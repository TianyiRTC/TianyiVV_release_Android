package com.sip.rtcclient;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

/**
 * 
 * ����: WriteLog</br> ����com.cndatacom.util </br> ����: ������־logcat</br> �����汾�ţ�</br>
 * ������Ա�� huangzy</br> ����ʱ�䣺 2014-3-11
 */
public class WriteLog {

  /**
   * ��ʶ
   */
  private static final String TAG = "Log";
  /**
   * // LogWrite
   */
  private String LOG_PATH_SDCARD_DIR; // log file path in sdcard
  /**
   * �ļ���
   */
  private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");// log
                                                                             // //
                                                                             // name
  /**
   * ���
   */
  private Process process;

  /**
   * 
   */
  private static WriteLog mLogDemo = null;

  /**
   * 
   * ����: ��ʼ��</br> ������Ա��huangzy</br> ����ʱ�䣺2014-3-11</br>
   */
  private WriteLog() {
    init();
  }

  /**
   * 
   * ������: getInstance</br> ����: ��ȡ����</br> ������Ա��huangzy</br> ����ʱ�䣺2014-3-11</br>
   * 
   * @return
   */
  public static WriteLog getInstance() {
    if (mLogDemo == null) {
      mLogDemo = new WriteLog();
    }
    return mLogDemo;
  }

  /**
   * 
   * ������: startLog</br> ����: ��ʼ</br> ������Ա��huangzy</br> ����ʱ�䣺2014-3-11</br>
   */
  public void startLog() {
    createLog();

  }

  /**
   * 
   * ������: stopLog</br> ����: ֹͣ</br> ������Ա��huangzy</br> ����ʱ�䣺2014-3-11</br>
   */
  public void stopLog() {
    if (process != null) {
      process.destroy();
    }

  }

  

  /**
   * 
   * ������: init</br> ����: ��ʼ��</br> ������Ա��huangzy</br> ����ʱ�䣺2014-3-11</br>
   */
  private void init() {
    LOG_PATH_SDCARD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/VV/log";
    createLogDir();
    Log.i(TAG, "Log onCreate");
    CleanLog cleanLog = new CleanLog();
    cleanLog.execute(LOG_PATH_SDCARD_DIR);
  }

  /**
   * write the log
   */
  public void createLog() {
    // TODOWriteLog
    List<String> commandList = new ArrayList<String>();
    commandList.add("logcat");
    commandList.add("-f");
    commandList.add(getLogPath());
    commandList.add("-v");
    commandList.add("time");
    try {
      process = Runtime.getRuntime().exec(commandList.toArray(new String[commandList.size()]));
    } catch (Exception e) {
      Log.e(TAG, e.getMessage(), e);
    }
  }

  /**
   * the path of the log file
   * 
   * @return
   */
  public String getLogPath() {
    createLogDir();
    String logFileName = sdf.format(new Date()) + ".log";// name
    Log.d(TAG, "Log stored in SDcard, the path is:" + LOG_PATH_SDCARD_DIR + File.separator + logFileName);
    return LOG_PATH_SDCARD_DIR + File.separator + logFileName;

  }

  /**
   * make the dir
   */
  private void createLogDir() {
    File file;
    boolean mkOk;

    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      file = new File(LOG_PATH_SDCARD_DIR);
      if (!file.isDirectory()) {
        mkOk = file.mkdirs();
        if (!mkOk) {
          return;
        }
      }
    }
  }
  
  
  /**
   * 
   * ����: CleanLog</br> 
   * ����com.cndatacom.util </br> 
   * ����: ɾ�����</br>
   * �����汾�ţ�</br>
   * ������Ա�� huangzy</br>
   * ����ʱ�䣺 2014-3-17
   */
  static class CleanLog extends AsyncTask<String,String,String>{

    /**
     * ��־�����¼�
     */
    private final static int LOGDAY = 1;
    
    
    @Override
    protected String doInBackground(String... params) {
      cleanLog(params[0]);
      return null;
    }
    
    /**
     * 
     * ������: cleanLog</br> ����: �����־��¼</br> ������Ա��huangzy</br> ����ʱ�䣺2014-3-17</br>
     */
    public void cleanLog(String filepath) {
      List<String> datas = new ArrayList<String>();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // ����ʱ���ʽ
      for (int i = 0; i < LOGDAY; i++) {
        Date dNow = new Date(); // ��ǰʱ��
        Calendar calendar = Calendar.getInstance(); // �õ�����
        calendar.setTime(dNow);// �ѵ�ǰʱ�丳������
        Date dBefore = new Date();
        calendar.add(Calendar.DAY_OF_MONTH, -(i)); // ����Ϊǰһ��
        dBefore = calendar.getTime(); // �õ�ǰһ���ʱ��
        String defaultStartDate = sdf.format(dBefore); // ��ʽ��ǰһ��
        datas.add(defaultStartDate);
      }
      
      for (int j = 0; j < datas.size(); j++) {
        System.out.println(datas.get(j));
      }
      
      try {
        File file = new File(filepath);
        if (file.isDirectory()) {
          String[] filelist = file.list();
          for (int i = 0; i < filelist.length; i++) {
            File readfile = new File(filepath + "/" + filelist[i]);
            if (!readfile.isDirectory()) {
              System.out.println("logName= " + readfile.getName());
              boolean canDelete = true;
              for (int j = 0; j < datas.size(); j++) {
                if (readfile.getName().startsWith(datas.get(j))) {
                  canDelete = false;
                }
              }
              if(canDelete){
//                System.out.println("��Ҫɾ��");
                if(readfile.delete()){
                  System.out.println();
                }
              }else{
//                System.out.println("����Ҫɾ��");
              }
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }
  
}
