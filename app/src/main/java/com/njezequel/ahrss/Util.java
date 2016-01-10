package com.njezequel.ahrss;

import android.content.Context;

/**
 * Utility class
 */
public class Util {
    /**
     * Convert a size in dp to pixel
     *
     * @param context activity context
     * @param px size in pixel
     * @return size in dp
     */
    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }
}
