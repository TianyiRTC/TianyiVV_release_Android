package com.sip.rtcclient.bean;

import java.io.Serializable;

public class TAccountInfo implements Serializable{
    
    private String username ="";
    private String password="";
    private String userid = "";
    private String resttoken = "";
    
    public String getUserid() {
        return userid;
    }
    public void setUserid(String userid) {
        this.userid = userid;
    }
    public String getResttoken() {
        return resttoken;
    }
    public void setResttoken(String resttoken) {
        this.resttoken = resttoken;
    }

    public String getUsername() {
        if(username.equals("")||username==null)
        {
            username = userid;
        }
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    
}
