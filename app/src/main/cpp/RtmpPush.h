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

public:
    RtmpPush(const char *url, WxCallJava *wxCallJava1);
    ~RtmpPush();

    void init();
};


#endif //WXVIDEOTALK1030_RTMPPUSH_H
