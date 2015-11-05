package com.oauth2.weibo;

public interface WeiboConstParam {

    public static final String CONSUMER_KEY = "3615118073";                             // appkey   
    public static final String CONSUMER_SECRET = "7c868436b0af02fe7173945bed0c3157";    // secret	
    public static final String REDIRECT_URL ="https://api.weibo.com/oauth2/default.html"; 
    public static final String OAUTH2_ACCESS_TOKEN_URL = "https://open.weibo.cn/oauth2/access_token";
    public static final String GET_USER = "https://api.weibo.com/2/users/show.json";
    public static final String SCOPE = 
        "email,direct_messages_read,direct_messages_write,"
                + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
                + "follow_app_official_microblog," + "invitation_write";


}
