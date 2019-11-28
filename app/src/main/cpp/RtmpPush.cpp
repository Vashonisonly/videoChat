//
// Created by letv on 19-11-25.
//

#include "RtmpPush.h"

RtmpPush::RtmpPush(const char *url,WxCallJava *wxCallJava) {
    this->url = static_cast<char *>(malloc((size_t)(512)));
    strcpy(this->url,url);
    this->wxCallJava = wxCallJava;
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
        // LOG
        LOGE("RtmpPush connect to the server fail");
        rtmpPush->wxCallJava->onConnectFail("RtmpPush connect to the server fail");
        goto end;
    }

    if(!RTMP_ConnectStream(rtmpPush->rtmp, 0)){
        // LOG
        LOGE("RtmpPush connect to the server's stream fail");
        rtmpPush->wxCallJava->onConnectFail("RtmpPush connect to the server's stream fail");
        goto end;
    }
    rtmpPush->wxCallJava->onConnected();
    rtmpPush->startPushing = true;

    LOGD("RtmpPush，连接成功，开始推流");
    rtmpPush->startTime = 0;
    while (true){
        if(!rtmpPush->startPushing){
            break;
        }
        RTMPPacket *packet = NULL;
        packet = rtmpPush->queue->getRtmpPacket();
        if(packet != nullptr){
            int ret = RTMP_SendPacket(rtmpPush->rtmp,packet,1);
            // LOGD("video send return %d",ret);
            RTMPPacket_Free(packet);
            free(packet);
            packet = NULL;
        }
    }

    end:
        RTMP_Close(rtmpPush->rtmp);
        RTMP_Free(rtmpPush->rtmp);
        rtmpPush->rtmp = nullptr;

    pthread_exit(&rtmpPush->pushThread);
}

void RtmpPush::init() {
    this->startPushing = false;
    wxCallJava->onConnecting(WX_THREAD_MAIN);
    pthread_create(&pushThread,NULL,callBackPush,this);
}

void RtmpPush::pushSPSPPS(char *sps, int sps_length, char *pps, int pps_length) {
    int bodySize = sps_length + pps_length + 16;
    RTMPPacket *packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(packet,bodySize);
    RTMPPacket_Reset(packet);

    char *body = packet->m_body;

    int i = 0;
    body[i++] = 0x17; //1byte
    body[i++] = 0x00;body[i++] = 0x00;body[i++] = 0x00;body[i++] = 0x00; // 4byte fix
    body[i++] = 0x01; // version
    body[i++] = sps[1];
    body[i++] = sps[2];
    body[i++] = sps[3];

    body[i++] = 0xFF; //sps count

    body[i++] = 0xE1; // sps count

    // sps length
    body[i++] = (sps_length >> 8) & 0xff; //
    body[i++] = sps_length & 0xff;
    // sps data
    memcpy(&body[i],sps,sps_length);
    i += sps_length;


    // pps count
    body[i++] = 0x01;
    // pps length
    body[i++] = (pps_length >> 8) & 0xff; //
    body[i++] = pps_length & 0xff;

    // sps data
    memcpy(&body[i],pps,pps_length);

    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = bodySize;
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;

    packet->m_nChannel = 0x04; //A?V
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
    packet->m_nInfoField2 = rtmp->m_stream_id;

    queue->putRtmpPacket(packet);
}

void RtmpPush::pushVideoData(char *data, int data_length, bool keyframe) {

    int bodySize = data_length + 9;
    RTMPPacket *packet = static_cast<RTMPPacket *>(malloc(sizeof(RTMPPacket)));
    RTMPPacket_Alloc(packet,bodySize);
    RTMPPacket_Reset(packet);

    char *body = packet->m_body;

    int i = 0;
    if(keyframe){
        body[i++] = 0x17;
    } else{
        body[i++] = 0x27;
    }
    body[i++] = 0x01;body[i++] = 0x00;body[i++] = 0x00;body[i++] = 0x00; // 4byte fix

    body[i++] = (data_length >> 24)&0xff;
    body[i++] = (data_length >> 16)&0xff;
    body[i++] = (data_length >> 8)&0xff;
    body[i++] = (data_length)&0xff;

    memcpy(&body[i],data,data_length);

    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = bodySize;
    packet->m_nTimeStamp = RTMP_GetTime() - startTime;
    // LOGD("RtmpPush TimeStamp is %d",(int)packet->m_nTimeStamp);
    packet->m_hasAbsTimestamp = 0;

    packet->m_nChannel = 0x04; //A?V
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = rtmp->m_stream_id;

    queue->putRtmpPacket(packet);
}


