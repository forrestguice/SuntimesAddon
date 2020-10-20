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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_ACCENTCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_ACTIONCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_ASTROCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_BACKGROUND;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_BACKGROUND_COLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_CIVILCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_DAYCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_DISPLAYSTRING;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_FALLCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_ISDEFAULT;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_MAP_BACKGROUNDCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_MAP_FOREGROUNDCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_MAP_HIGHLIGHTCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_MAP_SHADOWCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_MOONFULLCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_MOONFULL_STROKE_WIDTH;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_MOONNEWCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_MOONNEW_STROKE_WIDTH;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_MOONRISECOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_MOONSETCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_MOONWANINGCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_MOONWAXINGCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_NAME;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_NAUTICALCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_NIGHTCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_NOONCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_NOONICON_FILL_COLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_NOONICON_STROKE_COLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_NOONICON_STROKE_WIDTH;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_PADDING;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_PADDING_BOTTOM;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_PADDING_LEFT;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_PADDING_RIGHT;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_PADDING_TOP;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_RISEICON_FILL_COLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_RISEICON_STROKE_COLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_RISEICON_STROKE_WIDTH;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_SETICON_FILL_COLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_SETICON_STROKE_COLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_SETICON_STROKE_WIDTH;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_SPRINGCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_SUMMERCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_SUNRISECOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_SUNSETCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_TEXTCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_TEXTSIZE;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_TIMEBOLD;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_TIMECOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_TIMESIZE;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_TIMESUFFIXCOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_TIMESUFFIXSIZE;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_TITLEBOLD;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_TITLECOLOR;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_TITLESIZE;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_VERSION;
import static com.forrestguice.suntimes.themes.SuntimesThemeContract.THEME_WINTERCOLOR;

public class ThemeHelper
{
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
        ContentResolver resolver = context.getContentResolver();
        if (resolver != null)
        {
            Uri uri = Uri.parse("content://" + SuntimesThemeContract.AUTHORITY + "/" + SuntimesThemeContract.QUERY_THEME + "/" + themeName);
            try {
                Cursor cursor = resolver.query(uri, SuntimesThemeContract.QUERY_THEME_PROJECTION, null, null, null);
                if (cursor != null)
                {
                    values = new ContentValues();
                    //DatabaseUtils.cursorRowToContentValues(cursor, values);    // does this call preserve type?
                    for (String key : SuntimesThemeContract.QUERY_THEME_PROJECTION) {
                        loadIntoValues(cursor, key, values);
                    }
                    cursor.close();
                }
            } catch (SecurityException e) {
                Log.e("ThemeHelper", "Unable to access " + SuntimesThemeContract.AUTHORITY + "! " + e);
            }
        }
        return values;
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    public static void loadIntoValues(@NonNull Cursor cursor, @NonNull String key, @NonNull ContentValues values)
    {
        switch (key) {
            // String
            case THEME_NAME: case THEME_DISPLAYSTRING:
                values.put(key, cursor.getString(cursor.getColumnIndex(key)));
                break;

            // int (color
            case THEME_VERSION:
            case THEME_BACKGROUND: case THEME_BACKGROUND_COLOR:
            case THEME_TEXTCOLOR: case THEME_TITLECOLOR: case THEME_TIMECOLOR: case THEME_TIMESUFFIXCOLOR: case THEME_ACTIONCOLOR: case THEME_ACCENTCOLOR:
            case THEME_SUNRISECOLOR: case THEME_NOONCOLOR: case THEME_SUNSETCOLOR:
            case THEME_MOONRISECOLOR: case THEME_MOONSETCOLOR: case THEME_MOONWANINGCOLOR: case THEME_MOONWAXINGCOLOR: case THEME_MOONNEWCOLOR: case THEME_MOONFULLCOLOR:
            case THEME_NOONICON_FILL_COLOR: case THEME_NOONICON_STROKE_COLOR:
            case THEME_RISEICON_FILL_COLOR: case THEME_RISEICON_STROKE_COLOR:
            case THEME_SETICON_FILL_COLOR: case THEME_SETICON_STROKE_COLOR:
            case THEME_DAYCOLOR: case THEME_CIVILCOLOR: case THEME_NAUTICALCOLOR: case THEME_ASTROCOLOR: case THEME_NIGHTCOLOR:
            case THEME_SPRINGCOLOR: case THEME_SUMMERCOLOR: case THEME_FALLCOLOR: case THEME_WINTERCOLOR:
            case THEME_MAP_BACKGROUNDCOLOR: case THEME_MAP_FOREGROUNDCOLOR: case THEME_MAP_SHADOWCOLOR: case THEME_MAP_HIGHLIGHTCOLOR:
                values.put(key, cursor.getInt(cursor.getColumnIndex(key)));
                break;

            // int (pixels)
            case THEME_PADDING: case THEME_PADDING_LEFT: case THEME_PADDING_TOP: case THEME_PADDING_RIGHT: case THEME_PADDING_BOTTOM:
                values.put(key, cursor.getInt(cursor.getColumnIndex(key)));
                break;

            // float
            case THEME_MOONFULL_STROKE_WIDTH: case THEME_MOONNEW_STROKE_WIDTH: case THEME_SETICON_STROKE_WIDTH: case THEME_NOONICON_STROKE_WIDTH: case THEME_RISEICON_STROKE_WIDTH:
                values.put(key, cursor.getFloat(cursor.getColumnIndex(key)));
                break;

            // float (sp)
            case THEME_TITLESIZE: case THEME_TEXTSIZE: case THEME_TIMESIZE: case THEME_TIMESUFFIXSIZE:
                values.put(key, cursor.getFloat(cursor.getColumnIndex(key)));
                break;

            // int (boolean
            case THEME_TITLEBOLD: case THEME_TIMEBOLD: case THEME_ISDEFAULT:
                values.put(key, cursor.getInt(cursor.getColumnIndex(key)) != 0);
                break;
        }
    }

}
