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
import android.os.Bundle;
import android.util.Log;

public class AddonHelper
{
    public static final String SUNTIMES_PACKAGE = "com.forrestguice.suntimeswidget";

    public static final void startSuntimesActivity(Context context) {
        startActivity(context, SUNTIMES_PACKAGE, SUNTIMES_PACKAGE + ".SuntimesActivity", null);
    }

    public static final void startSuntimesAlarmsActivity(Context context) {
        startActivity(context, SUNTIMES_PACKAGE, SUNTIMES_PACKAGE + ".alarmclock.ui.AlarmClockActivity", null);
    }

    public static final void startSuntimesThemesActivity(Context context) {
        startActivity(context, SUNTIMES_PACKAGE, SUNTIMES_PACKAGE + ".themes.WidgetThemeListActivity", null);
    }

    public static final void startSuntimesThemesActivityForResult(Activity activity, int requestCode, String selected) {

        Bundle extras = new Bundle();
        extras.putString("selected", selected);
        startActivityForResult(activity, SUNTIMES_PACKAGE, SUNTIMES_PACKAGE + ".themes.WidgetThemeListActivity", extras, 0, requestCode);
    }

    public static final void reconfigureWidget(Context context, int appWidgetID) {
        reconfigureWidget(context, appWidgetID, SUNTIMES_PACKAGE + ".SuntimesConfigActivity0");
    }
    
    public static final void reconfigureWidget(Context context, int appWidgetID, String widgetClassName)
    {
        Bundle extras = new Bundle();
        extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetID);
        extras.putBoolean("ONTAP_LAUNCH_CONFIG", true);
        startActivity(context, SUNTIMES_PACKAGE, widgetClassName, extras);
    }

    public static final void startActivity(Context context, String packageName, String className, Bundle extras)
    {
        Intent intent = new Intent();
        intent.setClassName(packageName, className);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (extras != null) {
            intent.putExtras(extras);
        }

        try {
            context.startActivity(intent);

        } catch (SecurityException | ActivityNotFoundException e) {
            Log.e("AddonHelper", "unable to startActivity: " + e);
        }
    }

    public static final void startActivityForResult(Activity activity, String packageName, String className, Bundle extras, int flags, int requestCode)
    {
        Intent intent = new Intent();
        intent.setClassName(packageName, className);
        intent.setFlags(flags);
        if (extras != null) {
            intent.putExtras(extras);
        }

        try {
            activity.startActivityForResult(intent, requestCode);

        } catch (SecurityException | ActivityNotFoundException e) {
            Log.e("AddonHelper", "unable to startActivity: " + e);
        }
    }

}