package com.sip.rtcclient.bean;

import java.io.Serializable;

/**
 * <p>
 * desc:
 * <p>
 * Copyright: Copyright(c) 2013
 * </p>
 * 
 */

public class TGroupInfo implements Serializable {

	/**
	 * 
	 */
    private String LOGTAG = "TGroupInfo";
	private static final long serialVersionUID = -2911718154674969218L;

	public static String _ID = "_id";
	public static String _GROUP_ID = "_group_id";
	public static String _GROUP_NAME = "_group_name";
	public static String _GROUP_MEMBERS = "_group_members";
	public static String _GROUP_CREATE_TIME = "_group_create_time";
	public static String _GROUP_PHOTO = "_group_photo";
	public static String _GROUP_CREATOR = "_group_creator";
	public static String _GROUP_TYPE = "_group_type";
	public static String _GROUP_SHIELD = "_group_shield";
	
	public static final int GROUP_TYPE_CREATE = 0; //自己创建的群组
	public static final int GROUP_TYPE_JOIN = 1; //加入别人的群组
	
	public static final int GROUP_SHIELD_NO = 0; //未被屏蔽
	public static final int GROUP_SHIELD_YES = 1; //被屏蔽
	
	private int id; // ID
	private String groupId; // 群组ID
	private String groupName; // 群组名称
	private String groupMembers; //群组成员
	private String groupCreateTime;// 群组创建时间
	private String groupPhoto; // 群组头像
	private String groupCreator; // 群组创建者
	private int groupType;	//群组类型  0自己创建 1加入别人创建
	private int groupShieldTag; // 群组是否被屏蔽 0未被屏蔽 1被屏蔽

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupMembers() {
		return groupMembers;
	}

	public void setGroupMembers(String groupMembers) {
		this.groupMembers = groupMembers;
	}

	public String getGroupCreateTime() {
		return groupCreateTime;
	}

	public void setGroupCreateTime(String groupCreateTime) {
		this.groupCreateTime = groupCreateTime;
	}

	public String getGroupPhoto() {
		return groupPhoto;
	}

	public void setGroupPhoto(String groupPhoto) {
		this.groupPhoto = groupPhoto;
	}

	public String getGroupCreator() {
		return groupCreator;
	}

	public void setGroupCreator(String groupCreator) {
		this.groupCreator = groupCreator;
	}

	public int getGroupType() {
		return groupType;
	}

	public void setGroupType(int groupType) {
		this.groupType = groupType;
	}

	public int getGroupShieldTag() {
		return groupShieldTag;
	}

	public void setGroupShieldTag(int groupShieldTag) {
		this.groupShieldTag = groupShieldTag;
	}
//	public String[] getArr()
//	{
//	    String str = getGroupMembers();
//	    CommFunc.PrintLog(1, LOGTAG, "getGroupMembers:"+str);
//	    String[] els = str.split(";");
//	    return els;
//	}

}
