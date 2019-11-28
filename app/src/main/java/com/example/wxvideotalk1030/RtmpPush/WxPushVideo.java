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

    public void pushSPSPPS(byte[] sps, byte[] pps){
        if( sps != null && pps != null){
            pushSPSPPS(sps,sps.length,pps,pps.length);
        }
    }

    public void pushVideoData(byte[] data, boolean iskeyFrame){
        if(data != null){
            pushVideoData(data,data.length, iskeyFrame);
        }
    }

    private native void initPush(String pushUrl);

    private native void pushSPSPPS(byte[] sps, int sps_len, byte[] pps, int pps_len);

    private native void pushVideoData(byte[] data, int dataLength, boolean isKeyFrame);
}
