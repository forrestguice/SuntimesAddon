/*
    Copyright (C) 2020-2021 Forrest Guice
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
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimes.alarm.SuntimesAlarmsContract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

@SuppressWarnings("WeakerAccess")
public class AddonHelper
{
    public static final String SUNTIMES_PACKAGE = "com.forrestguice.suntimeswidget";
    public static final String ACTIVITY_MAIN = SUNTIMES_PACKAGE + ".SuntimesActivity";
    public static final String ACTIVITY_ALARMCLOCK = SUNTIMES_PACKAGE + ".alarmclock.ui.AlarmClockActivity";
    public static final String ACTIVITY_ACTIONS = SUNTIMES_PACKAGE + ".actions.ActionListActivity";
    public static final String ACTIVITY_COLOR = SUNTIMES_PACKAGE + ".settings.colors.ColorActivity";
    public static final String ACTIVITY_THEMES = SUNTIMES_PACKAGE + ".themes.WidgetThemeListActivity";
    public static final String ACTIVITY_PLACES = SUNTIMES_PACKAGE + ".getfix.PlacesActivity";
    public static final String ACTIVITY_WIDGETS = SUNTIMES_PACKAGE + ".SuntimesWidgetListActivity";
    public static final String ACTIVITY_WIDGETCONFIG = SUNTIMES_PACKAGE + ".SuntimesConfigActivity0";

    public static final String ACTIVITY_SETTINGS = SUNTIMES_PACKAGE + ".SuntimesSettingsActivity";
    public static final String FRAGMENT_SETTINGS_GENERAL = ACTIVITY_SETTINGS + "$GeneralPrefsFragment";
    public static final String FRAGMENT_SETTINGS_ALARMS = ACTIVITY_SETTINGS + "$AlarmPrefsFragment";
    public static final String FRAGMENT_SETTINGS_CALENDARS = ACTIVITY_SETTINGS + "$CalendarPrefsFragment";
    public static final String FRAGMENT_SETTINGS_LOCALE = ACTIVITY_SETTINGS + "$LocalePrefsFragment";
    public static final String FRAGMENT_SETTINGS_PLACES = ACTIVITY_SETTINGS + "$PlacesPrefsFragment";
    public static final String FRAGMENT_SETTINGS_UI = ACTIVITY_SETTINGS + "$UIPrefsFragment";


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
        startActivity(context, intentForAlarmsActivity(null, null));
    }
    public static void startSuntimesAlarmsActivity(Context context, long selectedAlarmID) {
        startActivity(context, intentForAlarmsActivity(null, selectedAlarmID));
    }
    public static Intent intentForAlarmsActivity(@Nullable String action, @Nullable Long selectedAlarmID)
    {
        Bundle extras = new Bundle();
        if (selectedAlarmID != null) {
            extras.putLong("selectedAlarm", selectedAlarmID);
        }
        Intent intent = createIntent(SUNTIMES_PACKAGE, ACTIVITY_ALARMCLOCK, action, extras, null, 0);
        if (selectedAlarmID != null) {
            intent.setData(ContentUris.withAppendedId(Uri.parse("content://" + SuntimesAlarmsContract.AUTHORITY + "/alarms"), selectedAlarmID));
        }
        return intent;
    }
    public static Intent scheduleNotification(String label, int hour, int minutes, @Nullable TimeZone timezone, @Nullable String solarEvent) {
        return scheduleAlarm("NOTIFICATION", label, hour, minutes, timezone, solarEvent);
    }
    public static Intent scheduleAlarm(String label, int hour, int minutes, @Nullable TimeZone timezone, @Nullable String solarEvent) {
        return scheduleAlarm("ALARM", label, hour, minutes, timezone, solarEvent);
    }
    public static Intent scheduleAlarm(String alarmType, String label, int hour, int minutes, @Nullable TimeZone timezone, @Nullable String solarEvent)
    {
        Calendar calendar0 = Calendar.getInstance(timezone);
        Calendar calendar1 = Calendar.getInstance(TimeZone.getDefault());
        calendar0.set(Calendar.HOUR_OF_DAY, hour);
        calendar0.set(Calendar.MINUTE, minutes);
        calendar1.setTimeInMillis(calendar0.getTimeInMillis());

        Intent intent = intentForAlarmsActivity("android.intent.action.SET_ALARM", null);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("android.intent.extra.alarm.MESSAGE", label);
        intent.putExtra("android.intent.extra.alarm.HOUR", ((timezone == null) ? hour : calendar1.get(Calendar.HOUR_OF_DAY)));
        intent.putExtra("android.intent.extra.alarm.MINUTES", ((timezone == null) ? minutes : calendar1.get(Calendar.MINUTE)));
        intent.putExtra("timezone", ((timezone == null) ? (String) null : timezone.getID()));
        intent.putExtra("solarevent", solarEvent);
        intent.putExtra("alarmtype", alarmType);
        return intent;
    }

    /**
     * Actions Activity
     */
    public static void startSuntimesActionsActivity(Context context) {
        Intent intent = intentForActionsActivity(null);
        intent.putExtra("noselect", true);
        startActivity(context, intent);
    }
    public static void startSuntimesActionsActivityForResult(Activity activity, int requestCode, String selected) {
        startActivityForResult(activity, intentForActionsActivity(selected), requestCode);
    }
    public static String resultForActionsActivity(@NonNull Intent data) {
        //boolean isModified = data.getBooleanExtra("isModified", false);    // list was modified
        return data.getStringExtra("actionID");    // selected actionID
    }
    public static boolean supportForActionsActivity(SuntimesInfo suntimesInfo) {
        return (suntimesInfo != null && suntimesInfo.appCode >= 66);    // access to Actions added v0.13.2 (66)
    }
    public static Intent intentForActionsActivity(String selected)
    {
        Bundle extras = new Bundle();
        extras.putBoolean("noselect", false);
        extras.putString("selected", selected);
        return createIntent(SUNTIMES_PACKAGE, ACTIVITY_ACTIONS, null, extras, null, 0);
    }

    /**
     * Themes Activity
     */
    public static void startSuntimesThemesActivity(Context context) {
        Intent intent = intentForThemesActivity(null);
        intent.putExtra("noselect", true);
        startActivity(context, intent);
    }
    public static void startSuntimesThemesActivityForResult(Activity activity, int requestCode, String selected) {
        startActivityForResult(activity, intentForThemesActivity(selected), requestCode);
    }
    public static String resultForThemesActivity(@NonNull Intent data) {
        //boolean isModified = data.getBooleanExtra("isModified", false);    // list was modified
        return data.getStringExtra("name");    // selected theme name
    }
    public static boolean supportForThemesActivity(SuntimesInfo suntimesInfo) {
        return (suntimesInfo != null && suntimesInfo.appCode >= 59);    // access to Themes added v0.12.8 (59)
    }
    public static Intent intentForThemesActivity(String selected)
    {
        Bundle extras = new Bundle();
        extras.putBoolean("noselect", false);
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
    public static long resultForPlacesActivity(@NonNull Intent data) {
        //boolean isModified = data.getBooleanExtra("isModified", false);    // list was modified
        return data.getLongExtra("selectedRowID", -1);
    }
    public static boolean supportForPlacesActivity(SuntimesInfo suntimesInfo) {
        return (suntimesInfo != null && suntimesInfo.appCode >= 64);    // access to Places added v0.13.0 (64)
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
    public static void startSuntimesColorActivityForResult(Activity activity, int requestCode, int selectedColor) {
        startSuntimesColorActivityForResult(activity, requestCode, selectedColor, null, false);
    }
    public static void startSuntimesColorActivityForResult(Activity activity, int requestCode, int selectedColor, @Nullable ArrayList<Integer> recentColors, boolean showAlpha) {
        startActivityForResult(activity, intentForColorActivity(selectedColor, showAlpha, recentColors), requestCode);
    }
    public static int resultForColorActivity(Intent data, int defaultColor) {
        return data.getIntExtra("color", defaultColor);
    }
    public static boolean supportForColorActivity(SuntimesInfo suntimesInfo) {
        return (suntimesInfo != null && suntimesInfo.appCode >= 64);    // access to Colors added v0.13.0 (64)
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
        startActivity(context, intentForWidgetListActivity());
    }
    public static boolean supportForWidgetListActivity(SuntimesInfo suntimesInfo) {
        return (suntimesInfo != null && suntimesInfo.appCode >= 66);    // access to WidgetList added v0.13.2 (66)
    }
    public static Intent intentForWidgetListActivity() {
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


    /**
     * Settings Activity
     */
    public static void startSuntimesSettingsActivity(Context context) {
        startActivity(context, intentForSettingsActivity(null));
    }
    public static void startSuntimesSettingsActivity(Context context, @Nullable String fragment) {
        startActivity(context, intentForSettingsActivity(fragment));
    }
    public static boolean supportForSettingsActivity(SuntimesInfo suntimesInfo) {
        return (suntimesInfo != null && suntimesInfo.appCode >= 66);    // access to SettingsActivity added v0.13.2 (66)
    }
    public static Intent intentForSettingsActivity(@Nullable String fragment)
    {
        Bundle extras = new Bundle();
        if (fragment != null)
        {
            extras.putString(PreferenceActivity.EXTRA_SHOW_FRAGMENT, fragment);
            extras.putBoolean(PreferenceActivity.EXTRA_NO_HEADERS, true);
        }
        return createIntent(SUNTIMES_PACKAGE, ACTIVITY_SETTINGS, null, extras, null, 0);
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