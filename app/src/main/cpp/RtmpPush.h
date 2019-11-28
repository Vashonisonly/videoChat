//
// Created by letv on 19-11-25.
//

#ifndef WXVIDEOTALK1030_RTMPPUSH_H
#define WXVIDEOTALK1030_RTMPPUSH_H

#include <malloc.h>
#include <string>
#include "WxQueue.h"
#include "pthread.h"
#include "WxCallJava.h"

extern "C"{
#include "librtmp/rtmp.h"
};


class RtmpPush {
public:
    RTMP *rtmp = nullptr;
    char *url = nullptr;
    WxQueue *queue = nullptr;
    pthread_t pushThread;
    WxCallJava *wxCallJava = nullptr;
    bool startPushing = false;
    int startTime = 0;

public:
    RtmpPush(const char *url, WxCallJava *wxCallJava1);
    ~RtmpPush();

    void init();
    void pushSPSPPS(char *sps, int sps_length, char *pps, int pps_length);
    void pushVideoData(char* data, int data_length, bool keyframe);
};


#endif //WXVIDEOTALK1030_RTMPPUSH_H
