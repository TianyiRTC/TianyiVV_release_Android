/*
 * Copyright (C) 2010-2013 The SINA WEIBO Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oauth2.weibo;

import org.json.JSONObject;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboParameters;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.AbsOpenAPI;

public class GetUserAPI extends AbsOpenAPI {
	private final static String TAG = GetUserAPI.class.getName();

	/**
	 * 构造函数。
	 * 
	 * @param oauth2AccessToken Token 实例
	 */
	public GetUserAPI(Oauth2AccessToken oauth2AccessToken) {
		super(oauth2AccessToken);
	}

	/**
	 * 向好友发送邀请。支持登录用户向自己的微博互粉好友发送私信邀请、礼物。
	 * 
	 * @param uid      被邀请人的 Uid，需要为当前用户互粉好友
	 * @param jsonData 邀请数据。以 {@link JSONObject} 数据填充
	 * @param listener 邀请接口对应的回调
	 */
	public void GetUser(String uid, String token,String customer_key, RequestListener listener) 
	{
		WeiboParameters params = new WeiboParameters();
		params.add("source",     customer_key);
		params.add("access_token", token);
		params.add("uid",    uid);
		request(WeiboConstParam.GET_USER, params, "GET", listener);
	}
}
