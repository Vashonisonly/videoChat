//
// Created by letv on 19-11-25.
//

#pragma once
#ifndef WXVIDEOTALK1030_ANDROIDLOG_H
#define WXVIDEOTALK1030_ANDROIDLOG_H

#include <android/log.h>

#define LOG_SHOW true

#define LOGD(FORMAT,...) __android_log_print(ANDROID_LOG_DEBUG,"vashonWang",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"vashonWang",FORMAT,##__VA_ARGS__);


#endif //WXVIDEOTALK1030_ANDROIDLOG_H
