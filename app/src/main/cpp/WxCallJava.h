//
// Created by vashonwang on 19-11-26.
//

#ifndef WXVIDEOTALK1030_WXCALLJAVA_H
#define WXVIDEOTALK1030_WXCALLJAVA_H

#define WX_THREAD_MAIN 1
#define WX_THREAD_CHILD 2

#include <jni.h>

class WxCallJava {
public:
    JNIEnv *jniEnv = nullptr;
    JavaVM *javaVm = nullptr;

    jobject jobj;

    jmethodID jmid_connecting;
    jmethodID jmid_connected;
    jmethodID jmid_connectFail;

public:
    WxCallJava(JNIEnv *jniEnv1, JavaVM *javaVm1, jobject *jobj);
    ~WxCallJava();

    void onConnecting(int type);

    void onConnected();

    void onConnectFail(char *msg);
};


#endif //WXVIDEOTALK1030_WXCALLJAVA_H
