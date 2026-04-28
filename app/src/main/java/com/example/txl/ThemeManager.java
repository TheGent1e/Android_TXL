package com.example.txl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class ThemeManager {
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "theme_mode";
    private static final String THEME_DARK = "dark";
    private static final String THEME_LIGHT = "light";

    public static void applyTheme(Context context) {
        if (isDarkTheme(context)) {
            context.setTheme(R.style.Base_Theme_Txl);
        } else {
            context.setTheme(R.style.Base_Theme_Txl_Light);
        }
    }

    public static void setDarkTheme(Context context, boolean isDark) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_THEME, isDark ? THEME_DARK : THEME_LIGHT).apply();
    }

    public static boolean isDarkTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return THEME_DARK.equals(prefs.getString(KEY_THEME, THEME_DARK));
    }

    public static void toggleTheme(Activity activity) {
        boolean wasDark = isDarkTheme(activity);
        setDarkTheme(activity, !wasDark);
        activity.recreate();
    }
}
