package com.example.wxvideotalk1030.Utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class DisPlayerUtil {
    public static int getScreenWidth(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }
    public static int getScreenHeigth(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }
}
