#include <jni.h>
#include <string>

#include "RtmpPush.h"

RtmpPush *rtmpPush =NULL;


extern "C"
JNIEXPORT void JNICALL
Java_com_example_wxvideotalk1030_RtmpPush_WxPushVideo_initPush(JNIEnv *env, jobject thiz,
                                                               jstring push_url) {
    const char *pushUrl = env->GetStringUTFChars(push_url,0);

    rtmpPush = new RtmpPush(pushUrl);
    rtmpPush->init();

    env->ReleaseStringUTFChars(push_url,pushUrl);
}