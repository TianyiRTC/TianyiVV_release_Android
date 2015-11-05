package com.sip.rtcclient.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO 需要更改
 * 
 * @author ThinkPad
 * 
 */
public class TContactInfo implements Serializable {

	public static String _ID = "_id";
	public static String _CONTACT_ID = "_contact_id";
	public static String _CONTACT_NAME = "_contact_name";
	public static String _CONTACT_NUMBER = "_contact_number";
	public static String _CONTACT_SORT_KEY = "_contact_sort_key";
	public static String _CONTACT_PHOTO_ID = "_contact_photo_id";
	public static String _CONTACT_LOOK_UP_KEY = "_contact_look_up_key";
	public static String _CONTACT_USERTYPE = "_contact_usertype";

	private String contactId;
	private String name;// 名字
	private String phoneNum;
	private String firstChar;// 首字母
	private Long photoId;
	private String lookUpKey;
	private int selected = 0;
	private String formattedNumber;
	private String pinyin;
	private int usertype; // 用户类型，为了在通讯录中进行区分 系统通信录100、天翼账号0、新浪微博账号 1

	// wwyue
	private String status = "0";// 用户在线状态 0 ：不在线，1 ：在线

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		// if(contactId.startsWith("+86"))
		// contactId = contactId.replace("+86", "");

		this.contactId = contactId;
	}

	public Long getPhotoId() {
		return photoId;
	}

	public void setPhotoId(Long photoId) {
		this.photoId = photoId;
	}

	public String getLookUpKey() {
		return lookUpKey;
	}

	public void setLookUpKey(String lookUpKey) {
		this.lookUpKey = lookUpKey;
	}

	public int getUsertype() {
		return usertype;
	}

	public void setUsertype(int usertype) {
		this.usertype = usertype;
	}

	public int getSelected() {
		return selected;
	}

	public void setSelected(int selected) {
		this.selected = selected;
	}

	public String getFormattedNumber() {
		return formattedNumber;
	}

	public void setFormattedNumber(String formattedNumber) {
		this.formattedNumber = formattedNumber;
	}

	public String getFirstChar() {
		return firstChar;
	}

	public void setFirstChar(String firstChar) {
		this.firstChar = firstChar;
	}

	// public int getType() {
	// return type;
	// }
	// public void setType(int type) {
	// this.type = type;
	// }
	private List<String> phone;// 号码列表
	private String email;// 邮箱
	private String address;// 地址
	private String id;
	private int type;// 用户类型
	private int phoneType;// 天翼帐号状态，邀请或添加

	// public static final int USER_TIANYI=1;
	// public static final int USER_WEIBO=2;
	// public static final int USER_ALL=3;

	public static final int TYPE_APPLY = 1;
	public static final int TYPE_ADD = 2;

	public int getPhoneType() {
		return phoneType;
	}

	public void setPhoneType(int phoneType) {
		this.phoneType = phoneType;
	}

	public static int getTypeAdd() {
		return TYPE_ADD;
	}

	public TContactInfo() {
		phone = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void addPhone(String phone) {
		this.phone.add(phone);
	}

	public String getID() {
		return id;
	}

	public void setID(String _id) {
		this.id = _id;
	}
}
