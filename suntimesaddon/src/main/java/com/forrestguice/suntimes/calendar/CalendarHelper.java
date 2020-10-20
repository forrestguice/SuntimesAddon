/*
    Copyright (C) 2020 Forrest Guice
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

package com.forrestguice.suntimes.calendar;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.MatrixCursor;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

@SuppressWarnings("WeakerAccess")
@TargetApi(14)
public class CalendarHelper
{
    public static final String COLUMN_CALENDAR_NAME = "calendar_name";             // String (calendar ID)
    public static final String COLUMN_CALENDAR_TITLE = "calendar_title";           // String (display string)
    public static final String COLUMN_CALENDAR_SUMMARY = "calendar_summary";       // String (display string)
    public static final String COLUMN_CALENDAR_COLOR = "calendar_color";           // int (color)

    public static final String QUERY_CALENDAR_INFO = "calendarInfo";
    public static final String[] QUERY_CALENDAR_INFO_PROJECTION = new String[] {
            COLUMN_CALENDAR_NAME, COLUMN_CALENDAR_TITLE, COLUMN_CALENDAR_SUMMARY, COLUMN_CALENDAR_COLOR
    };

    public static final String QUERY_CALENDAR_CONTENT = "calendarContent";
    public static final String[] QUERY_CALENDAR_CONTENT_PROJECTION = new String[] {
            CalendarContract.Events.TITLE, CalendarContract.Events.DESCRIPTION, CalendarContract.Events.EVENT_TIMEZONE,
            CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.AVAILABILITY, CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS, CalendarContract.Events.GUESTS_CAN_SEE_GUESTS, CalendarContract.Events.GUESTS_CAN_MODIFY
    };

    /**
     * Creates a ContentValues containing CalendarContract values describing some event.
     * @param title event title
     * @param description event description
     * @param location even location (label)
     * @param time either the start time or both start and end times as Calendar objects (also used to obtain event TimeZone)
     * @return a ContentValues containing the CalendarContract values describing the event
     */
    @TargetApi(14)
    public static ContentValues createEventValues(String title, String description, @Nullable String location, Calendar... time)
    {
        ContentValues v = new ContentValues();
        v.put(CalendarContract.Events.TITLE, title);
        v.put(CalendarContract.Events.DESCRIPTION, description);

        if (time.length > 0)
        {
            v.put(CalendarContract.Events.EVENT_TIMEZONE, time[0].getTimeZone().getID());
            if (time.length >= 2)
            {
                v.put(CalendarContract.Events.DTSTART, time[0].getTimeInMillis());
                v.put(CalendarContract.Events.DTEND, time[1].getTimeInMillis());
            } else {
                v.put(CalendarContract.Events.DTSTART, time[0].getTimeInMillis());
                v.put(CalendarContract.Events.DTEND, time[0].getTimeInMillis());
            }
        } else {
            Log.w(CalendarHelper.class.getSimpleName(), "createEventContentValues: missing time arg (empty array); creating event without start or end time.");
        }

        if (location != null) {
            v.put(CalendarContract.Events.EVENT_LOCATION, location);
        }

        v.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
        v.put(CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS, "0");
        v.put(CalendarContract.Events.GUESTS_CAN_SEE_GUESTS, "0");
        v.put(CalendarContract.Events.GUESTS_CAN_MODIFY, "0");
        return v;
    }

    /**
     * Convenience method for putting CalendarContract values into a MatrixCursor.
     * @param cursor the MatrixCursor to add values to
     * @param values a list of events; ContentValues containing CalendarContract values
     * @return a reference to the cursor passed to this method
     */
    @TargetApi(14)
    public static MatrixCursor addEventValuesToCursor(MatrixCursor cursor, ArrayList<ContentValues> values)
    {
        if (cursor == null || values == null) {
            Log.w(CalendarHelper.class.getSimpleName(), "addEventValues: null cursor of values! skipping");
            return cursor;
        }

        for (int i=0; i<values.size(); i++)
        {
            ContentValues v = values.get(i);
            cursor.addRow(new Object[] {
                    v.get(CalendarContract.Events.TITLE), v.get(CalendarContract.Events.DESCRIPTION), v.get(CalendarContract.Events.EVENT_TIMEZONE),
                    v.get(CalendarContract.Events.DTSTART), v.get(CalendarContract.Events.DTEND), v.get(CalendarContract.Events.EVENT_LOCATION),
                    v.get(CalendarContract.Events.AVAILABILITY), v.get(CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS), v.get(CalendarContract.Events.GUESTS_CAN_SEE_GUESTS), v.get(CalendarContract.Events.GUESTS_CAN_MODIFY)
            });
        }
        return cursor;
    }

}
