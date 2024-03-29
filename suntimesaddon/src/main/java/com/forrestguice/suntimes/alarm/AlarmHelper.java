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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
//import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;

import com.forrestguice.suntimes.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * @see AlarmEventContract
 *
 * Addons can use this helper to implement a ContentProvider to supply alarm events to SuntimesAlarms.
 * Addons should declare the availability of their provider(s) in the manifest with an intent-filter
 * and with meta-data (containing a | delimited list of provider URI).
 *
 *   <activity>
 *      <meta-data android:name="EventInfoProvider" android:value="content://custom.alarm.event.provider" />
 *        <intent-filter>
 *          <action android:name="suntimes.action.ADDON_EVENT" />
 *          <category android:name="suntimes.SUNTIMES_ALARM" />
 *       </intent-filter>
 *   </activity>
 *
 *   <provider
 *      android:name="addon.package.id.AlarmEventProviderExample"
 *      android:authorities="custom.alarm.event.provider"
 *      android:exported="true" android:permission="suntimes.permission.READ_CALCULATOR"
 *      android:syncable="false" />
 * ------------------------------------------------------------------------------------------------
 * Addons can also declare the availability of EventPicker activities in their manifest by including an intent-filter
 * in that activity's definition. The picker is responsible for returning a valid URI into its event provider; the
 * currently selected uri is provided by the `alarm_event` extra.
 *
 *   <activity>
 *      <meta-data android:name="SuntimesEventPickerTitle" android:value="Custom Event Picker" />
 *      <intent-filter>
 *         <action android:name="suntimes.action.PICK_EVENT" />
 *         <category android:name="suntimes.SUNTIMES_ADDON" />
 *      </intent-filter>
 *   </activity>
 *
 * Addons can select their custom alarms by including the `solarevent` extra with a valid URI when
 * starting the AlarmClockActivity (@see AddonHelper.scheduleAlarm).
 * ------------------------------------------------------------------------------------------------
 * Addons can declare the available of DismissChallenge activities in their manifest by including an intent-filter
 * in that activity's definition.
 *
 *   <activity>
 *      <meta-data android:name="SuntimesDismissChallengeTitle" android:value="Custom Challenge" />
 *      <meta-data android:name="SuntimesDismissChallengeID" android:value="9000" />
 *      <intent-filter>
 *         <action android:name="suntimes.action.DISMISS_CHALLENGE" />
 *         <category android:name="suntimes.SUNTIMES_ADDON" />
 *      </intent-filter>
 *   </activity>
 *
 * The `SuntimesDismissChallengeID` should be assigned a unique value unlikely to collide with other
 * addons; ID's 0-99 are reserved by SuntimesAlarms.
 *
 * When the activity is started the Intent data will contain the alarm URI; i.e. `long rowID = ContentUris.parseId(data)`
 * The Activity should return RESULT_OK if the challenge was passed (and the alarm should be dismissed).
 * The Activity may trigger SNOOZE by setting the action to `suntimeswidget.alarm.snooze` (with RESULT_CANCELED).
 * ------------------------------------------------------------------------------------------------
 * PERMISSIONS
 * The `READ_CALCULATOR` permission must be declared by the package that responds to the alarm URI.
 * All addons are expected to declare this permission in their manifest:
 *
 *   `<uses-permission android:name="suntimes.permission.READ_CALCULATOR" />` in their manifest
 */
public class AlarmHelper
{
    public static final String CATEGORY_SUNTIMES_ALARMS = "suntimes.SUNTIMES_ALARM";
    public static final String ACTION_ADDON_EVENT = "suntimes.action.ADDON_EVENT";
    public static final String ACTION_PICK_EVENT = "suntimes.action.PICK_EVENT";

    /**
     * Alarm Dismiss Challenge
     */
    public static final String ACTION_DISMISS_CHALLENGE = "suntimes.action.DISMISS_CHALLENGE";
    public static final String ACTION_DISMISS_CHALLENGE_CONFIG = "suntimes.action.DISMISS_CHALLENGE_CONFIG";
    public static final String META_DISMISS_CHALLENGE_TITLE = "SuntimesDismissChallengeTitle";
    public static final String META_DISMISS_CHALLENGE_ID = "SuntimesDismissChallengeID";

    /**
     * Alarm Extras
     */
    public static final String EXTRA_RESULT = "result";
    public static final String EXTRA_SELECTED_ALARM = "selectedAlarm";
    public static final String EXTRA_NOTIFICATION_ID = "notificationID";

    /**
     * Alarm Actions
     */
    public static final String ACTION_SILENT = "suntimeswidget.alarm.silent";
    public static final String ACTION_DISMISS = "suntimeswidget.alarm.dismiss";
    public static final String ACTION_SNOOZE = "suntimeswidget.alarm.snooze";
    public static final String ACTION_SCHEDULE = "suntimeswidget.alarm.schedule";
    public static final String ACTION_RESCHEDULE = "suntimeswidget.alarm.reschedule";
    public static final String ACTION_RESCHEDULE1 = ACTION_RESCHEDULE + "1";
    public static final String ACTION_DISABLE = "suntimeswidget.alarm.disable";
    public static final String ACTION_DELETE = "suntimeswidget.alarm.delete";
    public static final String ACTION_UPDATE_UI = "suntimeswidget.alarm.ui.update";

    /**
     * Alarm URI
     */
    public static final String ALARM_AUTHORITY = "com.forrestguice.suntimeswidget.alarmclock";
    public static final Uri ALARM_URI = Uri.parse("content://" + ALARM_AUTHORITY + "/alarms");

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

    /**
     * getEventUri
     */
    public static String getEventInfoUri(String authority, String alarmID) {
        return "content://" + authority + "/" + AlarmEventContract.QUERY_EVENT_INFO + "/" + alarmID;
    }

    public static String getEventCalcUri(String authority, String alarmID) {
        return "content://" + authority + "/" + AlarmEventContract.QUERY_EVENT_CALC + "/" + alarmID;
    }

    public static Intent getEventData(String authority, String eventName, String eventTitle, String eventSummary)
    {
        Intent data = new Intent();
        data.putExtra(AlarmEventContract.COLUMN_CONFIG_PROVIDER, authority);
        data.putExtra(AlarmEventContract.COLUMN_EVENT_NAME, eventName);
        data.putExtra(AlarmEventContract.COLUMN_EVENT_TITLE, eventTitle);
        data.putExtra(AlarmEventContract.COLUMN_EVENT_SUMMARY, eventSummary);
        data.setData(Uri.parse(AlarmHelper.getEventInfoUri(authority, eventName)));
        return data;
    }

    /**
     * getAlarmUri
     * @param rowID alarmID
     * @return Uri pointing to alarm
     */
    public static Uri getAlarmUri(long rowID) {
        return ContentUris.withAppendedId(ALARM_URI, rowID);
    }

    /**
     * alarmListCursorAdapter
     * @param context Context
     * @return CursorAdapter
     */
    /*public static SimpleCursorAdapter createAlarmListCursorAdapter(Context context)
    {
        Cursor cursor = queryAlarms(context.getContentResolver());
        return new SimpleCursorAdapter(context, android.R.layout.two_line_list_item, cursor,
                new String[] { SuntimesAlarmsContract.KEY_ALARM_TYPE, SuntimesAlarmsContract.KEY_ALARM_SOLAREVENT }, new int[] { android.R.id.text1, android.R.id.text2 }, 0 );
    }*/

    /**
     * queryAlarms
     * @param resolver ContentResolver
     * @return Cursor
     */
    public static Cursor queryAlarms(@Nullable ContentResolver resolver)
    {
        if (resolver != null)
        {
            Uri uri = Uri.parse("content://" + SuntimesAlarmsContract.AUTHORITY + "/" + SuntimesAlarmsContract.QUERY_ALARMS);
            try {
                return resolver.query(uri, SuntimesAlarmsContract.QUERY_ALARMS_PROJECTION_MIN, null, null, null);
            } catch (SecurityException e) {
                Log.e("AlarmHelper", "queryAlarms: Unable to access " + SuntimesAlarmsContract.AUTHORITY + "! " + e);
                return null;
            }
        }
        return null;
    }

}
