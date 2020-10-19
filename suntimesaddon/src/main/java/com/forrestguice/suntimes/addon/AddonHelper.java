/*
    Copyright (C) 2020 Forrest Guice
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
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

public class AddonHelper
{
    public static final String SUNTIMES_PACKAGE = "com.forrestguice.suntimeswidget";
    public static final String ACTIVITY_MAIN = SUNTIMES_PACKAGE + ".SuntimesActivity";
    public static final String ACTIVITY_ALARMCLOCK = SUNTIMES_PACKAGE + ".alarmclock.ui.AlarmClockActivity";
    public static final String ACTIVITY_COLOR = SUNTIMES_PACKAGE + ".settings.colors.ColorActivity";
    public static final String ACTIVITY_THEMES = SUNTIMES_PACKAGE + ".themes.WidgetThemeListActivity";
    public static final String ACTIVITY_PLACES = SUNTIMES_PACKAGE + ".getfix.PlacesActivity";
    public static final String ACTIVITY_WIDGETS = SUNTIMES_PACKAGE + ".SuntimesWidgetListActivity";
    public static final String ACTIVITY_WIDGETCONFIG = SUNTIMES_PACKAGE + ".SuntimesConfigActivity0";

    /**
     * Main Activity
     */
    public static void startSuntimesActivity(Context context) {
        startActivity(context, intentForMainActivity());
    }
    public static Intent intentForMainActivity() {
        return createIntent(SUNTIMES_PACKAGE, ACTIVITY_MAIN, null, null, null, 0);
    }

    /**
     * Alarms Activity
     */
    public static void startSuntimesAlarmsActivity(Context context) {
        startActivity(context, intentForAlarmsActivity());
    }
    public static Intent intentForAlarmsActivity() {
        return createIntent(SUNTIMES_PACKAGE, ACTIVITY_ALARMCLOCK, null, null, null, 0);
    }

    /**
     * Themes Activity
     */
    public static void startSuntimesThemesActivity(Context context) {
        startActivity(context, intentForThemesActivity(null));
    }
    public static void startSuntimesThemesActivityForResult(Activity activity, int requestCode, String selected) {
        startActivityForResult(activity, intentForThemesActivity(selected), requestCode);
    }
    public static Intent intentForThemesActivity(String selected)
    {
        Bundle extras = new Bundle();
        extras.putString("selected", selected);
        return createIntent(SUNTIMES_PACKAGE, ACTIVITY_THEMES, null, extras, null, 0);
    }

    /**
     * Places Activity
     */
    public static void startSuntimesPlacesActivity(Context context) {
        startActivity(context, intentForPlacesActivity(-1, false));
    }
    public static void startSuntimesPlacesActivityForResult(Activity activity, int requestCode, long selected) {
        startActivityForResult(activity, intentForPlacesActivity(selected, true), requestCode);
    }
    public static Intent intentForPlacesActivity(long selected, boolean allowPick)
    {
        Bundle extras = new Bundle();
        extras.putLong("selectedRowID", selected);
        extras.putBoolean("allowPick", allowPick);
        return createIntent(SUNTIMES_PACKAGE, ACTIVITY_PLACES, null, extras, null, 0);
    }

    /**
     * Color Activity
     */
    public static final void startSuntimesColorActivityForResult(Activity activity, int requestCode, int selectedColor) {
        startSuntimesColorActivityForResult(activity, requestCode, selectedColor, null, false);
    }
    public static final void startSuntimesColorActivityForResult(Activity activity, int requestCode, int selectedColor, @Nullable ArrayList<Integer> recentColors, boolean showAlpha) {
        startActivityForResult(activity, intentForColorActivity(selectedColor, showAlpha, recentColors), requestCode);
    }
    public static Intent intentForColorActivity(int selectedColor, boolean showAlpha, @Nullable ArrayList<Integer> recentColors)
    {
        Uri data = Uri.parse("color://" + String.format("#%08X", selectedColor));
        Bundle extras = new Bundle();
        extras.putInt("color", selectedColor);
        extras.putBoolean("showAlpha", showAlpha);
        if (recentColors != null) {
            extras.putIntegerArrayList("recentColors", recentColors);
        }
        return createIntent(SUNTIMES_PACKAGE, ACTIVITY_COLOR, Intent.ACTION_PICK, extras, data, 0);
    }

    /**
     * WidgetList Activity
     */
    public static void startSuntimesWidgetListActivity(Context context) {
        startActivity(context, intentForPlacesActivity(-1, false));
    }
    public static void startSuntimesWidgetListActivityForResult(Activity activity, int requestCode, long selected) {
        startActivityForResult(activity, intentForPlacesActivity(selected, true), requestCode);
    }
    public static Intent intentForWidgetListActivity(long selected, boolean allowPick) {
        return createIntent(SUNTIMES_PACKAGE, ACTIVITY_WIDGETS, null, null, null, 0);
    }

    /**
     * WidgetConfig Activity
     */
    public static void reconfigureWidget(Context context, int appWidgetID) {
        reconfigureWidget(context, appWidgetID, ACTIVITY_WIDGETCONFIG);
    }
    public static void reconfigureWidget(Context context, int appWidgetID, String widgetConfigClassName) {
        startActivity(context, intentForWidgetConfigActivity(appWidgetID, widgetConfigClassName));
    }
    public static Intent intentForWidgetConfigActivity(int appWidgetID, String widgetConfigClassName)
    {
        Bundle extras = new Bundle();
        extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetID);
        extras.putBoolean("ONTAP_LAUNCH_CONFIG", true);
        return createIntent(SUNTIMES_PACKAGE, widgetConfigClassName, null, extras, null, Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void startActivity(Context context, Intent intent)
    {
        try {
            intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (SecurityException | ActivityNotFoundException e) {
            Log.e("AddonHelper", "unable to startActivity: " + e);
        }
    }

    public static void startActivityForResult(Activity activity, Intent intent, int requestCode)
    {
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (SecurityException | ActivityNotFoundException e) {
            Log.e("AddonHelper", "unable to startActivity: " + e);
        }
    }

    public static Intent createIntent(String packageName, String className, @Nullable String action, @Nullable Bundle extras, @Nullable Uri data, int flags)
    {
        Intent intent = new Intent();
        intent.setClassName(packageName, className);
        intent.setFlags(flags);
        if (action != null) {
            intent.setAction(action);
        }
        if (extras != null) {
            intent.putExtras(extras);
        }
        if (data != null) {
            intent.setData(data);
        }
        return intent;
    }

}