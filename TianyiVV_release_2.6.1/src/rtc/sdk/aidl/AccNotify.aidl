package rtc.sdk.aidl;

import rtc.sdk.aidl.SdkCall;

// 对帐户需要实现的接口
interface AccNotify {
	// 注册结果通知
	void onRegister(int nStatus, String desp, int expire);
	// 来电通知
	void onNewCall(SdkCall call);
	// 语音群组
	void onRspGroupVoice(int action, String parameters);
	void onGroupVoice(SdkCall call);
	void onReqGroupVoice(int action, String parameters);
	// 微直播
//	void onRspMicroLive(int action, String parameters);
//	void onMicroLive(SdkCall call);
//	void onReqMicroLive(int action, String parameters);
}
