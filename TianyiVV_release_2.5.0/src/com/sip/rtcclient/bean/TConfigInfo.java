package com.sip.rtcclient.bean;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TConfigInfo implements Serializable{

    public static boolean bFirstLogin = false;
    public static String deviceName;
    public static String token;
    public static String APIVersion="1.0.0";
    
    public static String sipServerAddr;
    public static String sipServerPort;
    public static String siprealm;
}
