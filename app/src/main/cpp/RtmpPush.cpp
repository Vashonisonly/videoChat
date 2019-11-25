//
// Created by letv on 19-11-25.
//

#include "RtmpPush.h"

RtmpPush::RtmpPush(const char *url) {
    this->url = static_cast<char *>(malloc((size_t)(512)));
    strcpy(this->url,url);
    this->queue = new WxQueue();

}

RtmpPush::~RtmpPush() {
    queue->notifyQueue();
    queue->cleanQueue();
    free(url);
}

void *callBackPush(void *data){
    RtmpPush *rtmpPush = static_cast<RtmpPush *>(data);

    rtmpPush->rtmp = RTMP_Alloc();
    RTMP_Init(rtmpPush->rtmp);
    rtmpPush->rtmp->Link.timeout = 10;
    rtmpPush->rtmp->Link.lFlags |= RTMP_LF_LIVE;

    RTMP_SetupURL(rtmpPush->rtmp,rtmpPush->url);

    RTMP_EnableWrite(rtmpPush->rtmp);

    LOGD("url is: %s",rtmpPush->url);
    if(!RTMP_Connect(rtmpPush->rtmp, NULL)){
        // Log
        LOGE("RtmpPush connect to the server fail");
        goto end;
    }

    if(!RTMP_ConnectStream(rtmpPush->rtmp, 0)){
        //LOG
        LOGE("RtmpPush connect to the server's stream fail");
        goto end;
    }

    LOGD("RtmpPush","连接成功，开始推流");
//    while (true){
//
//    }

    end:
        RTMP_Close(rtmpPush->rtmp);
        RTMP_Free(rtmpPush->rtmp);
        rtmpPush->rtmp = nullptr;

    pthread_exit(&rtmpPush->pushThread);
}

void RtmpPush::init() {
    pthread_create(&pushThread,NULL,callBackPush,this);
}
