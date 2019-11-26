package com.example.wxvideotalk1030.RtmpPush;

public interface WxConnectListener {

    void onConnecting();

    void onConnected();

    void onConnectFail(String msg);
}
