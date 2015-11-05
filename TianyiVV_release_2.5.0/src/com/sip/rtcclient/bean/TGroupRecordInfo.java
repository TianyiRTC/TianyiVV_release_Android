package com.sip.rtcclient.bean;

import java.io.Serializable;

/**
 * 群组通话记录
 * 
 */
public class TGroupRecordInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3129365802495918948L;

	public static final int CALL_TYPE_IN_FAIL = 1;
	public static final int CALL_TYPE_IN_SUCCESS = 2;
	public static final int CALL_TYPE_OUT_FAIL = 3;
	public static final int CALL_TYPE_OUT_SUCCESS = 4;

	public static final int TYPE_NMULTICHAT = 0; // 聊天室
	public static final int TYPE_MULTISPEAK = 1; // 群对讲
	public static final int TYPE_SHOW = 2; // VV秀场
	public static final int TYPE_LIVE = 3; // 现场直播

	public static final int CONF_JOINSUCCESS = 1;
	public static final int CONF_JOINFAILED = -1;
	
	public static final int CONF_CALL = 0;	//呼出
	public static final int CONF_RECEIVE = 1;	//呼入
	
	public static String _ID = "_id";
	public static String _GROUP_ID = "_groupId";
	public static String _GROUP_CALL_ID = "_group_call_id";
	public static String _CONF_TYPE = "_conftype";
	public static String _STARTTIME = "_startTime";
	public static String _ENDTIME = "_endTime";
	public static String _TIME = "_time";
	public static String _JOIN_RESULT = "_joinResult";
	public static String _DURATION = "_duration";
	public static String _START_DATE = "_start_date";
	public static String _END_DATE = "_end_date";

	private String groupId; // 群组ID
	public String callId;	//通话记录id
	private int conftype; // 四种会议类型
	private String startTime; // 通话发起时间
	private String endTime; // 通话结束时间
	private String time; // 通话时间
	private int joinResult; // 加入会议结果 失败 成功
	private int duration; // 方向 对应呼入 呼出
	private String startDate;
	private String endDate;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public int getConftype() {
		return conftype;
	}

	public void setConftype(int conftype) {
		this.conftype = conftype;
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

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getJoinResult() {
		return joinResult;
	}

	public void setJoinResult(int joinResult) {
		this.joinResult = joinResult;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
}
