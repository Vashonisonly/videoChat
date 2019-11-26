#include <jni.h>
#include <string>

#include "RtmpPush.h"
#include "WxCallJava.h"

RtmpPush *rtmpPush =NULL;
WxCallJava *wxCallJava = nullptr;
JavaVM *javaVm = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_wxvideotalk1030_RtmpPush_WxPushVideo_initPush(JNIEnv *env, jobject thiz,
                                                               jstring push_url) {
    const char *pushUrl = env->GetStringUTFChars(push_url,0);

    wxCallJava = new WxCallJava(env,javaVm,&thiz);
    rtmpPush = new RtmpPush(pushUrl,wxCallJava);
    rtmpPush->init();

    env->ReleaseStringUTFChars(push_url,pushUrl);
}

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved){
    javaVm =vm;
    JNIEnv *env;
    if(vm->GetEnv((void**)&env,JNI_VERSION_1_4) != JNI_OK){
        if(LOG_SHOW){
            LOGE("GetEnv failed!");
        }
        return -1;
    }

    return JNI_VERSION_1_4;
}

extern "C"
JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved){
    javaVm = nullptr;
}

