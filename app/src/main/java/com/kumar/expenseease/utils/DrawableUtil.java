package com.kumar.expenseease.utils;

import android.content.Context;
import android.net.Uri;

public class DrawableUtil {
    public static Uri getDrawableUri(Context context, int drawableId) {
        return Uri.parse("android.resource://" + context.getPackageName() + "/" + drawableId);
    }
}

