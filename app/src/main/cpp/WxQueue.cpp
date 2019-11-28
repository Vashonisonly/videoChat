//
// Created by letv on 19-11-25.
//

#include "WxQueue.h"

WxQueue::WxQueue() {
    pthread_mutex_init(&mutexPacket, NULL);
    pthread_cond_init(&condPacket, NULL);
}

WxQueue::~WxQueue() {
    cleanQueue();
    pthread_mutex_destroy(&mutexPacket);
    pthread_cond_destroy(&condPacket);
}

int WxQueue::putRtmpPacket(RTMPPacket *packet) {
    pthread_mutex_lock(&mutexPacket);
    queuePacket.push(packet);
    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);

    return 0;
}

RTMPPacket *WxQueue::getRtmpPacket() {
    pthread_mutex_lock(&mutexPacket);
    RTMPPacket *p = NULL;
    if(!queuePacket.empty()){
        p = queuePacket.front();
        queuePacket.pop();
    } else{
        pthread_cond_wait(&condPacket,&mutexPacket);
    }
    pthread_mutex_unlock(&mutexPacket);

    return p;
}

void WxQueue::cleanQueue() {
    pthread_mutex_lock(&mutexPacket);
    while (!queuePacket.empty()){
        RTMPPacket *p = queuePacket.front();
        queuePacket.pop();
        RTMPPacket_Free(p);
        p = NULL;
    }
    pthread_mutex_unlock(&mutexPacket);
}

void WxQueue::notifyQueue() {
    pthread_mutex_lock(&mutexPacket);
    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);
}
