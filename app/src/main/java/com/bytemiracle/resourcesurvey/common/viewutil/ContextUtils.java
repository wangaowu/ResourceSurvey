package com.bytemiracle.resourcesurvey.common.viewutil;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

/**
 * 类功能：TODO:
 *
 * @author gwwang
 * @date 2021/6/19 15:58
 */
public class ContextUtils {

    //copy from  com.google.android.material.internal.ContextUtils
    public static Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
