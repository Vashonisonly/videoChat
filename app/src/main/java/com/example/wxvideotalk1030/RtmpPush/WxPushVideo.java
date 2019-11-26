package com.example.wxvideotalk1030.RtmpPush;

import android.text.TextUtils;

public class WxPushVideo {

    private WxConnectListener wxConnectListener;

    static {
        System.loadLibrary("WxPush");
    }

    public void setWxConnectListener(WxConnectListener wxConnectListener){
        this.wxConnectListener = wxConnectListener;
    }

    private void onConnecting(){
        if(wxConnectListener != null){
            wxConnectListener.onConnecting();
        }
    }

    private void onConnected(){
        if(wxConnectListener != null){
            wxConnectListener.onConnected();
        }
    }

    private void onConnectFail(String msg){
        if(wxConnectListener != null){
            wxConnectListener.onConnectFail(msg);
        }
    }

    public void initLivePush(String url){
        if(!TextUtils.isEmpty(url)){
            initPush(url);
        }
    }

    private native void initPush(String pushUrl);
}
