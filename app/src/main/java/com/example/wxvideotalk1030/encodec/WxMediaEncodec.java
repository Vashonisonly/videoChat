package com.example.wxvideotalk1030.encodec;

import android.content.Context;

public class WxMediaEncodec extends WxBaseMediaEncoder {
    private WxEncodecRender wxEncodecRender;

    public WxMediaEncodec(Context context, int textureId){
        super(context);
        wxEncodecRender = new WxEncodecRender(context, textureId);
        setWxGlRender(wxEncodecRender);
        setmRenderMode(WxBaseMediaEncoder.RENDERMODE_CONTINUOUSLY);
    }
}
