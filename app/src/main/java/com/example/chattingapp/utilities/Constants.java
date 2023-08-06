package com.example.chattingapp.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTIONS_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME ="chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSingnedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";

    public static final String KEY_FCM_TOKEN = "fcmtoken";

    public static final String KEY_USER = "user";

    public static final String KEY_COLLECTION_CHAT = "chat";

    public static final String KEY_RECEIVER_ID = "senderId";

    public static final String KEY_SENDER_ID = "receiverId";

    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME= "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";

    public static final String REMOTE_MSG_AUTHORIZATION = "AUTHORIZATION";

    public static final String REMOTE_MESSAGE_CONTENT_TYPE = "CONTENT-TYPE";

    public static  final String REMOTE_MSG_data = "data";
    public static final String REMOTE_MSG_Registrations_IDS = "registration_ids";

    public static HashMap<String,String> remoteMsdHeaders = null;

    public  static  HashMap<String,String> getRemoteMessage(){
        if(remoteMsdHeaders == null){
            remoteMsdHeaders = new HashMap<>();
            remoteMsdHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAA9GlE8DY:APA91bH8WOLg2CFytGqmg16OY0fqq-M4EWeCYWqRBCy5L_fBKytDgmCOqNeeUB8w10FQ2JKDZ_quP28ByYtzBBuYhVUTiY7a9MEFzDSjrxZSEPaZySjzyWMY9G9c8-EaFZ1ck2es_kFF"
            );
            remoteMsdHeaders.put(
                    REMOTE_MESSAGE_CONTENT_TYPE,
                    "applications/json"
            );
        }
        return remoteMsdHeaders;
    }

}
