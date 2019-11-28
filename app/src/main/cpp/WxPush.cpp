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

extern "C"
JNIEXPORT void JNICALL
Java_com_example_wxvideotalk1030_RtmpPush_WxPushVideo_pushSPSPPS(JNIEnv *env, jobject thiz,
                                                                 jbyteArray sps_, jint sps_len,
                                                                 jbyteArray pps_, jint pps_len) {
    // TODO: implement pushSPSPPS()
    jbyte *sps = env->GetByteArrayElements(sps_, NULL);
    jbyte *pps = env->GetByteArrayElements(pps_, NULL);

    if(rtmpPush != nullptr){
        rtmpPush->pushSPSPPS(reinterpret_cast<char *>(sps),sps_len, reinterpret_cast<char *>(pps),pps_len);
    }
    env->ReleaseByteArrayElements(sps_,sps,0);
    env->ReleaseByteArrayElements(pps_,pps,0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_wxvideotalk1030_RtmpPush_WxPushVideo_pushVideoData(JNIEnv *env, jobject thiz,
                                                                    jbyteArray data_,
                                                                    jint data_length,
                                                                    jboolean isKeyFrame) {
    // TODO: implement pushVideoData()
    jbyte *data = env->GetByteArrayElements(data_, nullptr);

    if(rtmpPush != nullptr){
        rtmpPush->pushVideoData(reinterpret_cast<char *>(data),data_length,isKeyFrame);
    }

    env->ReleaseByteArrayElements(data_,data,0);
}