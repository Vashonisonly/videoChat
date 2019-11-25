package com.example.wxvideotalk1030.RtmpPush;

import android.text.TextUtils;

public class WxPushVideo {

    static {
        System.loadLibrary("WxPush");
    }

    public void initLivePush(String url){
        if(!TextUtils.isEmpty(url)){
            initPush(url);
        }
    }

    private native void initPush(String pushUrl);
}
