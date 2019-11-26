//
// Created by letv on 19-11-26.
//
#include "WxCallJava.h"

WxCallJava::WxCallJava(JNIEnv *jniEnv1, JavaVM *javaVm1, jobject *jobj) {
    this->jniEnv = jniEnv1;
    this->javaVm = javaVm1;
    this->jobj = jniEnv->NewGlobalRef(*jobj);

    jclass jlz = jniEnv->GetObjectClass(this->jobj);

    jmid_connecting = jniEnv->GetMethodID(jlz,"onConnecting","()V");
    jmid_connected = jniEnv->GetMethodID(jlz,"onConnected","()V");
    jmid_connectFail = jniEnv->GetMethodID(jlz,"onConnectFail","(Ljava/lang/String;)V");
}

WxCallJava::~WxCallJava() {

}

void WxCallJava::onConnecting(int type) {
    if(type == WX_THREAD_CHILD){
        JNIEnv *jniEnv;
        if(javaVm->AttachCurrentThread(&jniEnv,0) != JNI_OK){
            return;
        }
        jniEnv->CallVoidMethod(jobj,jmid_connecting);
        javaVm->DetachCurrentThread();
    } else{
        jniEnv->CallVoidMethod(jobj,jmid_connecting);
    }
}

void WxCallJava::onConnected() {
    JNIEnv *jniEnv;
    if(javaVm->AttachCurrentThread(&jniEnv,0) != JNI_OK){
        return;
    }
    jniEnv->CallVoidMethod(jobj,jmid_connected);
    javaVm->DetachCurrentThread();
}

void WxCallJava::onConnectFail(char *msg) {
    JNIEnv *jniEnv;
    if(javaVm->AttachCurrentThread(&jniEnv,0) != JNI_OK){
        return;
    }
    jstring jmsg = jniEnv->NewStringUTF(msg);
    jniEnv->CallVoidMethod(jobj,jmid_connectFail,jmsg);
    jniEnv->DeleteLocalRef(jmsg);
    javaVm->DetachCurrentThread();
}
