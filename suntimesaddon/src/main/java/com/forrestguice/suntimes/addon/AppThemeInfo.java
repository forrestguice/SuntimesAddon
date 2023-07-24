/*
    Copyright (C) 2023 Forrest Guice
    This file is part of Suntimes.

    Suntimes is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Suntimes is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Suntimes.  If not, see <http://www.gnu.org/licenses/>.
*/ 

package com.forrestguice.suntimes.addon;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

/**
 * AppThemeInfo
 */
public abstract class AppThemeInfo
{
    public abstract int getStyleId(Context context, TextSize textSize);
    public abstract String getThemeName();

    /**
     * @return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_NO;
     */
    public abstract int getDefaultNightMode();

    public String getExtendedThemeName(TextSize textSize) {
        return getExtendedThemeName(getThemeName(), textSize.name());
    }
    public String getExtendedThemeName(String textSize) {
        return getExtendedThemeName(getThemeName(), textSize);
    }

    public String getDisplayString(Context context) {
        return getThemeName();
    }
    public String toString() {
        return getThemeName();
    }

    /**
     * getExtendedThemeName
     */
    public static String getExtendedThemeName(String themeName, String textSize) {
        return themeName + "_" + textSize;
    }

    /**
     * getTextSize
     */
    public static TextSize getTextSize(String extendedThemeName) {
        String[] parts = extendedThemeName.split("_");
        return TextSize.valueOf((parts.length > 0 ? parts[parts.length-1] : TextSize.NORMAL.name()), TextSize.NORMAL);
    }

    /**
     * setTheme
     */
    public static int setTheme(Activity activity, String appTheme)
    {
        Log.d("DEBUG", "setTheme: " + appTheme);
        int themeResID = themePrefToStyleId(activity, appTheme);
        activity.setTheme(themeResID);
        AppCompatDelegate.setDefaultNightMode(info.loadThemeInfo(appTheme).getDefaultNightMode());
        return themeResID;
    }

    public static int setTheme(Activity activity, SuntimesInfo info)
    {
        if (info != null)
        {
            if (info.appThemeOverride != null) {
                return setTheme(activity, themeNameFromInfo(info));

            } else if (info.appTheme != null) {
                return setTheme(activity, info.appTheme);
            }
        }
        return 0;
    }

    /**
     * themeNameFromInfo
     */
    public static String themeNameFromInfo(SuntimesInfo info)
    {
        return ((info.appTextSize != null) ? getExtendedThemeName(info.appThemeOverride, info.appTextSize)
                : getExtendedThemeName(info.appThemeOverride, TextSize.NORMAL.name()));
    }

    /**
     * themePrefToStyleId
     */
    public static int themePrefToStyleId(Context context, String themeName)
    {
        if (themeName != null) {
            AppThemeInfo themeInfo = info.loadThemeInfo(themeName);
            TextSize textSize = AppThemeInfo.getTextSize(themeName);
            Log.d("DEBUG", "themePrefToStyleId: textSize: " + textSize);
            return themeInfo.getStyleId(context, textSize);
        } else return info.getDefaultThemeID();
    }

    /**
     * setFactory
     */
    public static void setFactory(AppThemeInfoFactory value) {
        info = value;
    }
    public static AppThemeInfoFactory info = new AppThemeInfoFactory()
    {
        @Override
        public AppThemeInfo loadThemeInfo(@Nullable String extendedThemeName)
        {
            return new AppThemeInfo()
            {
                @Override
                public int getStyleId(Context context, TextSize textSize) {
                    return R.style.SuntimesAddonTheme_Dark;
                }

                @Override
                public String getThemeName() {
                    return SuntimesInfo.THEME_DARK;
                }

                @Override
                public int getDefaultNightMode() {
                    return AppCompatDelegate.MODE_NIGHT_NO;
                }
            };
        }
        @Override
        public int getDefaultThemeID() {
            return R.style.SuntimesAddonTheme_Dark;
        }
    };

    /**
     * systemInNightMode
     */
    public static boolean systemInNightMode(Context context)
    {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        if (uiModeManager != null) {
            return (uiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES);
        } else return false;
    }

    /**
     * Text sizes
     */
    public enum TextSize
    {
        SMALL("Small"),
        NORMAL("Normal"),
        LARGE("Large"),
        XLARGE("Extra Large");

        private TextSize( String displayString ) {
            this.displayString = displayString;
        }

        public String getDisplayString() {
            return displayString;
        }
        public void setDisplayString( String displayString ) {
            this.displayString = displayString;
        }
        private String displayString;

        public static void initDisplayStrings( Context context )
        {
            SMALL.setDisplayString(context.getString(R.string.textSize_small));
            NORMAL.setDisplayString(context.getString(R.string.textSize_normal));
            LARGE.setDisplayString(context.getString(R.string.textSize_large));
            XLARGE.setDisplayString(context.getString(R.string.textSize_xlarge));
        }

        public static TextSize valueOf(String value, TextSize defaultValue)
        {
            try {
                return TextSize.valueOf(value);
            } catch (IllegalArgumentException e) {
                return defaultValue;
            }
        }
    }

    /**
     * AppThemeInfoFactory
     */
    public static abstract class AppThemeInfoFactory
    {
        public abstract AppThemeInfo loadThemeInfo(@Nullable String extendedThemeName);
        public abstract int getDefaultThemeID();
    }

}
