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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;

import com.forrestguice.suntimes.addon.SuntimesInfo;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

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
public class ActionsHelper
{
    /**
     * actionListCursorAdapter
     * @param context Context
     * @return CursorAdapter
     */
    public static SimpleCursorAdapter createActionListCursorAdapter(Context context)
    {
        Cursor cursor = queryActions(context.getContentResolver());
        return new SimpleCursorAdapter(context, android.R.layout.two_line_list_item, cursor,
                new String[] { SuntimesActionsContract.COLUMN_ACTION_TITLE, SuntimesActionsContract.COLUMN_ACTION_DESC }, new int[] { android.R.id.text1, android.R.id.text2 }, 0 );
    }

    /**
     * queryActions
     * @param resolver ContentResolver
     * @return Cursor
     */
    public static Cursor queryActions(@Nullable ContentResolver resolver)
    {
        if (resolver != null)
        {
            Uri uri = Uri.parse("content://" + SuntimesActionsContract.AUTHORITY + "/" + SuntimesActionsContract.QUERY_ACTIONS);
            try {
                return resolver.query(uri, SuntimesActionsContract.QUERY_ACTION_PROJECTION_MIN, null, null, null);

            } catch (SecurityException e) {
                Log.e(SuntimesInfo.class.getSimpleName(), "queryInfo: Unable to access " + SuntimesActionsContract.AUTHORITY + "! " + e);
                return null;
            }
        }
        return null;
    }

    public static Set<String> getStringSet(@Nullable String value) {
        return (value != null) ? new TreeSet<>(Arrays.asList(value.split("\\|"))) : null;
    }

    public static String stringSetToString(@Nullable Set<String> values)
    {
        if (values != null) {
            StringBuilder s = new StringBuilder();
            for (String v : values) {
                s.append(v).append("|");
            }
            return s.toString();
        } else {
            return null;
        }
    }

    /**
     * getActionInfoUri
     */
    public static String getActionInfoUri(String authority, String actionID) {
        return "content://" + authority + "/" + SuntimesActionsContract.QUERY_ACTIONS + "/" + actionID;
    }
}
