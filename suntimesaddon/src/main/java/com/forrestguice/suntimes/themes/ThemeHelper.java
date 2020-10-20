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

package com.forrestguice.suntimes.themes;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;

import com.forrestguice.suntimes.addon.SuntimesInfo;

@SuppressWarnings("WeakerAccess")
public class ThemeHelper
{
    /**
     * themeListCursorAdapter
     * @param context Context
     * @return CursorAdapter
     */
    public static SimpleCursorAdapter createThemeListCursorAdapter(Context context)
    {
        Cursor cursor = queryThemes(context.getContentResolver());
        return new SimpleCursorAdapter(context, android.R.layout.simple_list_item_1, cursor,
                new String[] { SuntimesThemeContract.THEME_DISPLAYSTRING }, new int[] { android.R.id.text1 }, 0 );
    }

    /**
     * queryThemes
     * @param resolver ContentResolver
     * @return Cursor
     */
    public static Cursor queryThemes(@Nullable ContentResolver resolver)
    {
        if (resolver != null)
        {
            Uri uri = Uri.parse("content://" + SuntimesThemeContract.AUTHORITY + "/" + SuntimesThemeContract.QUERY_THEMES);
            try {
                return resolver.query(uri, SuntimesThemeContract.QUERY_THEMES_PROJECTION, null, null, null);

            } catch (SecurityException e) {
                Log.e(SuntimesInfo.class.getSimpleName(), "queryInfo: Unable to access " + SuntimesThemeContract.AUTHORITY + "! " + e);
                return null;
            }
        }
        return null;
    }

    /**
     * queryTheme
     * @param resolver ContentResolver
     * @param themeName theme name (ID)
     * @return a Cursor containing theme values (or null if theme not found)
     */
    public static Cursor queryTheme(@Nullable ContentResolver resolver, @NonNull String themeName)
    {
        if (resolver != null)
        {
            Uri uri = Uri.parse("content://" + SuntimesThemeContract.AUTHORITY + "/" + SuntimesThemeContract.QUERY_THEME + "/" + themeName);
            try {
                return resolver.query(uri, SuntimesThemeContract.QUERY_THEME_PROJECTION, null, null, null);

            } catch (SecurityException e) {
                Log.e("ThemeHelper", "queryInfo: Unable to access " + SuntimesThemeContract.AUTHORITY + "! " + e);
                return null;
            }
        }
        return null;
    }

    /**
     * LoadTheme
     * @param context Context
     * @param themeName theme name (ID)
     * @return ContentValues containing theme values (or null if theme not found)
     */
    @Nullable
    public static ContentValues loadTheme(Context context, @NonNull String themeName)
    {
        ContentValues values = null;
        Cursor cursor = queryTheme(context.getContentResolver(), themeName);
        if (cursor != null)
        {
            cursor.moveToFirst();
            DatabaseUtils.cursorRowToContentValues(cursor, values = new ContentValues());
            cursor.close();
        }
        return values;
    }
}
