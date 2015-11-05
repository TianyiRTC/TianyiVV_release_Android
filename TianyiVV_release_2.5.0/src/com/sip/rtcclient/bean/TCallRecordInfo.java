package com.sip.rtcclient.bean;

import java.io.Serializable;


public class TCallRecordInfo implements Serializable{

	public static final String _ID = "_id";
	public static final String _CALL_RECORD_ID = "_call_record_id";
	public static final String _DATE = "_date";
	public static final String _START_TIME = "_start_time";
	public static final String _END_TIME = "_end_time";
	public static final String _TOTAL_TIME = "_total_time";
	public static final String _FROM_USER = "_from_user";
	public static final String _TO_USER = "_to_user";
	public static final String _TYPE = "_type";
	public static final String _RESULT = "_result";
	public static final String _DIRECTION = "_direction";

//	public static final int CALL_TYPE_AUDIO = 0; // 音频
//	public static final int CALL_TYPE_VIDEO = 1; // 视频

	public static final int CALL_RESULT_FAIL = 0; // 失败
	public static final int CALL_RESULT_SUCCESS = 1; // 成功

	public static final int CALL_DIRECTION_IN = 0; // 呼入
	public static final int CALL_DIRECTION_OUT = 1; // 呼出

	private int id;
	private String callRecordId;
	private String date; // 日期
	private String startTime; // 开始时间
	private String endTime; // 结束时间
	private String totalTime; // 通话时长
	private String fromUser; // 主叫
	private String toUser; // 被叫
	private int type; // 通话类型
	private int result; // 呼叫结果
	private int direction; // 呼入与呼出

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCallRecordId() {
		return callRecordId;
	}

	public void setCallRecordId(String callRecordId) {
		this.callRecordId = callRecordId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(String totalTime) {
		this.totalTime = totalTime;
	}

	public String getFromUser() {
		return fromUser;
	}

	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}

	public String getToUser() {
		return toUser;
	}

	public void setToUser(String toUser) {
		this.toUser = toUser;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

}
