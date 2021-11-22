/*
    Copyright (C) 2021 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.suntimes.alarm;

import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Addons can use this helper to implement a ContentProvider to supply alarm events to SuntimesAlarms.
 * Addons should declare the availability of their provider(s) in the manifest with an intent-filter
 * and with meta-data (containing a | delimited list of provider URI).
 *
 *   <activity>
 *      <meta-data android:name="AlarmInfoProvider" android:value="content://custom.alarm.provider" />
 *        <intent-filter>
 *          <action android:name="suntimes.action.ADDON_ALARM" />
 *          <category android:name="suntimes.SUNTIMES_ALARM" />
 *       </intent-filter>
 *   </activity>
 *
 *   <provider
 *      android:name="addon.packge.id.AlarmProviderExample"
 *      android:authorities="custom.alarm.provider"
 *      android:exported="true" android:permission="suntimes.permission.READ_CALCULATOR"
 *      android:syncable="false" />
 *
 * Addons can also declare the availability of AlarmPicker activities in their manifest by including an intent-filter
 * in that activity's definition. The picker is responsible for returning a valid URI into its alarm provider.
 *
 *   <activity>
 *      <meta-data android:name="SuntimesAlarmPickerTitle" android:value="Custom Alarm Picker" />
 *      <intent-filter>
 *         <action android:name="suntimes.action.PICK_ALARM" />
 *         <category android:name="suntimes.SUNTIMES_ADDON" />
 *      </intent-filter>
 *   </activity>
 *
 * Addons can select their custom alarms by including `EXTRA_ALARM_EVENT` with a valid URI when
 * starting the AlarmClockActivity (@see AddonHelper.scheduleAlarm).
 *
 * PERMISSIONS
 * The `READ_CALCULATOR` permission must be declared by the package that responds to the alarm URI.
 * All addons are expected to declare this permission in their manifest:
 *
 *   `<uses-permission android:name="suntimes.permission.READ_CALCULATOR" />` in their manifest
 */
public class AlarmHelper
{
    public static String COLUMN_ALARM_NAME = "alarm_name";            // String (alarm/event ID)
    public static String COLUMN_ALARM_TITLE = "alarm_title";          // String (display string)
    public static String COLUMN_ALARM_SUMMARY = "alarm_summary";      // String (extended display string)
    public static String COLUMN_ALARM_TIMEMILLIS = "alarm_time";      // long (timestamp millis)

    public static String EXTRA_ALARM_NOW = "alarm_now";                  // long (millis)
    public static String EXTRA_ALARM_REPEAT = "alarm_repeat";            // boolean
    public static String EXTRA_ALARM_REPEAT_DAYS = "alarm_repeat_days";  // Integer[] .. [1,2,3,4,..]
    public static String EXTRA_ALARM_OFFSET = "alarm_offset";            // long (millis)
    public static String EXTRA_ALARM_EVENT = "alarm_event";              // eventID

    public static String EXTRA_LOCATION_LABEL = "location_label";        // String
    public static String EXTRA_LOCATION_LAT = "latitude";                // double (DD)
    public static String EXTRA_LOCATION_LON = "longitude";               // double (DD)
    public static String EXTRA_LOCATION_ALT = "altitude";                // double (meters)

    public static String QUERY_ALARM_INFO = "alarmInfo";
    public static String[] QUERY_ALARM_INFO_PROJECTION = new String[] {
            COLUMN_ALARM_NAME, COLUMN_ALARM_TITLE, COLUMN_ALARM_SUMMARY
    };

    public static String QUERY_ALARM_CALC = "alarmCalc";
    public static String[] QUERY_ALARM_CALC_PROJECTION = new String[] {
            COLUMN_ALARM_NAME, COLUMN_ALARM_TIMEMILLIS
    };

    /**
     * processSelection
     * A query helper method; extracts selection columns/values to HashMap.
     * @param selection a completed selection string (@see processSelectionArgs)
     * @return a HashMap containing <KEY, VALUE> pairs
     */
    public static HashMap<String, String> processSelection(@Nullable String selection)
    {
        HashMap<String, String> retValue = new HashMap<>();
        if (selection != null)
        {
            String[] expressions = selection.split(" or | OR | and | AND ");  // just separators in this context (all interpreted the same)
            for (String expression : expressions)
            {
                String[] parts = expression.split("=");
                if (parts.length == 2) {
                    retValue.put(parts[0].trim(), parts[1].trim());
                } else Log.w("CalendarProvider", "processSelection: Too many parts! " + expression);
            }
        }
        return retValue;
    }

    /**
     * processSelectionArgs
     * A query helper method; inserts arguments into selection string.
     * @param selection a selection string (as passed to query)
     * @param selectionArgs a list of selection arguments
     * @return a completed selection string containing substituted arguments
     */
    @Nullable
    public static String processSelectionArgs(@Nullable String selection, @Nullable String[] selectionArgs)
    {
        String retValue = selection;
        if (selectionArgs != null && selection != null)
        {
            for (int i=0; i<selectionArgs.length; i++)
            {
                if (selectionArgs[i] != null)
                {
                    if (retValue.contains("?")) {
                        retValue = retValue.replaceFirst("\\?", selectionArgs[i]);

                    } else {
                        Log.w("CalendarProvider", "processSelectionArgs: Too many arguments! Given " + selectionArgs.length + " arguments, but selection contains only " + (i+1));
                        break;
                    }
                }
            }
        }
        return retValue;
    }

    /**
     * getRepeatDays
     */
    public static ArrayList<Integer> getRepeatDays(@Nullable String repeatDaysString)
    {
        ArrayList<Integer> result = new ArrayList<>();
        if (repeatDaysString != null)
        {
            repeatDaysString = repeatDaysString.replaceAll("\\[", "");
            repeatDaysString = repeatDaysString.replaceAll("]", "");
            String[] repeatDaysArray = repeatDaysString.split(",");
            for (int i=0; i<repeatDaysArray.length; i++) {
                String element = repeatDaysArray[i].trim();
                if (!element.isEmpty()) {
                    result.add(Integer.parseInt(element));
                }
            }
        }
        return result;
    }

    /**
     * getNowCalendar
     */
    public static Calendar getNowCalendar(String nowString)
    {
        long nowMillis = (nowString != null ? Long.parseLong(nowString) : System.currentTimeMillis());
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(nowMillis);
        return now;
    }

}
