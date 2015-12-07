package com.sip.rtcclient.bean;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TGroupMemberInfo implements Serializable{
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
