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

package com.forrestguice.suntimes.actions;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;

/**
 * Addons can use this helper to implement a ContentProvider to supply custom actions Suntimes.
 * Addons should declare the availability of their provider(s) in the manifest with an intent-filter
 * and with meta-data (containing a | delimited list of provider URI).
 *
 *   <activity>
 *      <meta-data android:name="ActionInfoProvider" android:value="content://custom.action.provider" />
 *        <intent-filter>
 *          <action android:name="suntimes.action.ADDON_ACTION" />
 *          <category android:name="suntimes.SUNTIMES_ACTION" />
 *       </intent-filter>
 *   </activity>
 *
 *   <provider
 *      android:name="addon.package.id.ActionProviderExample"
 *      android:authorities="custom.action.provider"
 *      android:exported="true" android:permission="suntimes.permission.READ_CALCULATOR"
 *      android:syncable="false" />
 *
 * PERMISSIONS
 * The `READ_CALCULATOR` permission must be declared by the package that responds to the alarm URI.
 * All addons are expected to declare this permission in their manifest:
 *
 *   `<uses-permission android:name="suntimes.permission.READ_CALCULATOR" />` in their manifest
 */
public class ActionHelper
{
    public static final String COLUMN_ACTION_NAME = "name";                 // String (action ID)
    public static final String COLUMN_ACTION_TITLE = "title";               // String (display string)
    public static final String COLUMN_ACTION_DESC = "desc";                 // String (extended display string)
    public static final String COLUMN_ACTION_COLOR = "color";

    public static final String COLUMN_ACTION_CLASS = "launch";
    public static final String COLUMN_ACTION_TYPE = "type";
    public static final String COLUMN_ACTION_ACTION = "action";
    public static final String COLUMN_ACTION_DATA = "data";
    public static final String COLUMN_ACTION_MIMETYPE = "datatype";
    public static final String COLUMN_ACTION_EXTRAS = "extras";

    public static final String TYPE_ACTIVITY = "ACTIVITY";
    public static final String TYPE_BROADCAST = "BROADCAST";
    public static final String TYPE_SERVICE = "SERVICE";

    public static final String QUERY_ACTION_INFO = "actionInfo";
    public static final String[] QUERY_ACTION_INFO_PROJECTION_MIN = new String[] {
            COLUMN_ACTION_NAME, COLUMN_ACTION_TITLE, COLUMN_ACTION_DESC, COLUMN_ACTION_COLOR
    };
    public static final String[] QUERY_ACTION_INFO_PROJECTION_FULL = new String[] {
            COLUMN_ACTION_NAME, COLUMN_ACTION_TITLE, COLUMN_ACTION_DESC, COLUMN_ACTION_COLOR,
            COLUMN_ACTION_CLASS, COLUMN_ACTION_TYPE, COLUMN_ACTION_ACTION, COLUMN_ACTION_DATA, COLUMN_ACTION_MIMETYPE, COLUMN_ACTION_EXTRAS
    };

    /**
     * getActionInfoUri
     */
    public static String getActionInfoUri(String authority, String actionID) {
        return "content://" + authority + "/" + QUERY_ACTION_INFO + "/" + actionID;
    }

}
