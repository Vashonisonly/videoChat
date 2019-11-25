//
// Created by letv on 19-11-25.
//

#ifndef WXVIDEOTALK1030_WXQUEUE_H
#define WXVIDEOTALK1030_WXQUEUE_H

#include "queue"
#include "pthread.h"
#include "AndroidLog.h"

extern "C"{
#include "librtmp/rtmp.h"
};

class WxQueue {
public:
    std::queue<RTMPPacket*> queuePacket;
    pthread_mutex_t mutexPacket;
    pthread_cond_t condPacket;

public:
    WxQueue();
    ~WxQueue();

    int putRtmpPacket(RTMPPacket *packet);

    RTMPPacket* getRtmpPacket();

    void cleanQueue();

    void notifyQueue();
};


#endif //WXVIDEOTALK1030_WXQUEUE_H
