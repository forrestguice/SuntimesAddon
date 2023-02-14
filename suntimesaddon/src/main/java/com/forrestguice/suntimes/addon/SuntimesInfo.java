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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimes.calculator.core.CalculatorProviderContract;

import java.util.Calendar;

/**
 * SuntimesInfo
 */
public class SuntimesInfo
{
    public Integer providerCode = null;
    public String providerName = null;
    public boolean isInstalled = false;
    public boolean hasPermission = true;    // true until SecurityException encountered

    public Integer appCode = null;
    public String appName = null;
    public String appLocale = null;
    public String appTheme = null;
    public String appThemeOverride = null;
    public String appTextSize = null;

    public String timezone = null;
    public String timezoneMode = "CUSTOM_TIMEZONE";        // "SOLAR_TIME", "CURRENT_TIMEZONE", "CUSTOM_TIMEZONE";
    public String solartimeMode = "APPARENT_SOLAR_TIME";   // "APPARENT_SOLAR_TIME", "LOCAL_MEAN_TIME"

    public String[] location = null;    // [0]label, [1]latitude (dd), [2]longitude (dd), [3]altitude (meters)
    protected SuntimesOptions options = null;

    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_DAYNIGHT = "daynight";

    public SuntimesInfo(@NonNull Context context) {
    }

    protected static final String[] projection = new String[] {
            CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION_CODE, CalculatorProviderContract.COLUMN_CONFIG_APP_VERSION_CODE,
            CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION,      CalculatorProviderContract.COLUMN_CONFIG_APP_VERSION,
            CalculatorProviderContract.COLUMN_CONFIG_LOCALE,
            CalculatorProviderContract.COLUMN_CONFIG_APP_THEME, CalculatorProviderContract.COLUMN_CONFIG_APP_THEME_OVERRIDE,
            CalculatorProviderContract.COLUMN_CONFIG_TIMEZONE, CalculatorProviderContract.COLUMN_CONFIG_TIMEZONEMODE, CalculatorProviderContract.COLUMN_CONFIG_SOLARTIMEMODE,
            CalculatorProviderContract.COLUMN_CONFIG_LOCATION, CalculatorProviderContract.COLUMN_CONFIG_LATITUDE,
            CalculatorProviderContract.COLUMN_CONFIG_LONGITUDE, CalculatorProviderContract.COLUMN_CONFIG_ALTITUDE,
            CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION_CODE_V2,    // legacy support
            CalculatorProviderContract.COLUMN_CONFIG_APP_TEXT_SIZE
    };

    public void initFromCursor(@NonNull Cursor cursor)
    {
        cursor.moveToFirst();
        providerCode = (!cursor.isNull(0)) ? cursor.getInt(0) : null;
        appCode = (!cursor.isNull(1)) ? cursor.getInt(1) : null;
        providerName = (!cursor.isNull(2)) ? cursor.getString(2) : null;
        appName = (!cursor.isNull(3)) ? cursor.getString(3) : null;
        appLocale = (!cursor.isNull(4)) ? cursor.getString(4) : null;
        appTheme = (!cursor.isNull(5)) ? cursor.getString(5) : null;
        appThemeOverride = (!cursor.isNull(6)) ? cursor.getString(6) : null;
        timezone = (!cursor.isNull(7)) ? cursor.getString(7) : null;
        timezoneMode = (!cursor.isNull(8)) ? cursor.getString(8) : null;
        solartimeMode = (!cursor.isNull(9)) ? cursor.getString(9) : null;
        location = new String[4];
        location[0] = (!cursor.isNull(10)) ? cursor.getString(10) : null;
        location[1] = (!cursor.isNull(11)) ? cursor.getString(11) : null;
        location[2] = (!cursor.isNull(12)) ? cursor.getString(12) : null;
        location[3] = (!cursor.isNull(13)) ? cursor.getString(13) : null;

        if (providerCode == null) {    // is this an older provider? limited support
            providerCode = (!cursor.isNull(14)) ? cursor.getInt(14) : null;
        }
        appTextSize = (!cursor.isNull(15)) ? cursor.getString(15) : null;

        hasPermission = isInstalled = (providerCode != null);
    }

    /**
     * querySuntimesInfo
     * @param context Context
     * @return a SuntimesInfo obj with version info, or null if resolver is null
     */
    public static SuntimesInfo queryInfo(@NonNull Context context)
    {
        SuntimesInfo info = null;
        ContentResolver resolver = context.getContentResolver();
        if (resolver != null)
        {
            info = new SuntimesInfo(context);
            Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_CONFIG );

            try {
                Cursor cursor = resolver.query(uri, projection, null, null, null);
                info.isInstalled = true;
                info.hasPermission = true;

                if (cursor != null)
                {
                    info.initFromCursor(cursor);
                    cursor.close();

                    if (info.appTheme.equals(THEME_DAYNIGHT)) {
                        info.appTheme = queryDayNightTheme(resolver);
                    }

                } else {               // null cursor but no SecurityException.. Suntimes isn't installed at all
                    info.isInstalled = false;
                }

            } catch (SecurityException e) {
                Log.e(SuntimesInfo.class.getSimpleName(), "queryInfo: Unable to access " + CalculatorProviderContract.AUTHORITY + "! " + e);
                info.hasPermission = false;
                info.isInstalled = true;
            }
        }
        return info;
    }

    @Nullable
    public static String queryAppTheme(@Nullable ContentResolver resolver)
    {
        String theme = null;
        if (resolver != null) {
            Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_CONFIG );
            try {
                Cursor cursor = resolver.query(uri, new String[] { CalculatorProviderContract.COLUMN_CONFIG_APP_THEME } , null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    theme = (cursor.isNull(0) ? THEME_DARK : cursor.getString(0));
                    cursor.close();
                }
            } catch (SecurityException e) {
                Log.e(SuntimesInfo.class.getSimpleName(), "queryInfo: Unable to access " + CalculatorProviderContract.AUTHORITY + "! " + e);
            }
        }
        if (theme != null && theme.equals(THEME_DAYNIGHT)) {
            theme = queryDayNightTheme(resolver);
        }
        return theme;
    }

    public static String queryDayNightTheme(@Nullable ContentResolver resolver)
    {
        long now = Calendar.getInstance().getTimeInMillis();
        Long[] sun = querySunriseSunset(resolver, null);
        if (sun[0] != null && sun[1] != null &&
                sun[0] < now && now < sun[1]) {
            return THEME_LIGHT;
        } else {
            return THEME_DARK;
        }
    }

    /**
     * @param resolver ContentResolver
     * @param date (optiona) date of sunrise/sunset (defaults to now)
     * @return [sunriseMillis, sunsetMillis] .. may contain nulls if event does not occur
     */
    public static Long[] querySunriseSunset(@Nullable ContentResolver resolver, @Nullable Calendar date)
    {
        Long[] sun = new Long[2];
        if (resolver != null)
        {
            long dateMillis = (date != null ? date.getTimeInMillis() : Calendar.getInstance().getTimeInMillis());
            Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SUN + "/" + dateMillis );
            try {
                Cursor cursor = resolver.query(uri, new String[] { CalculatorProviderContract.COLUMN_SUN_ACTUAL_RISE, CalculatorProviderContract.COLUMN_SUN_ACTUAL_SET } , null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    sun[0] = (cursor.isNull(0) ? null : cursor.getLong(0));
                    sun[1] = (cursor.isNull(1) ? null : cursor.getLong(1));
                    cursor.close();
                }
            } catch (SecurityException e) {
                Log.e(SuntimesInfo.class.getSimpleName(), "queryInfo: Unable to access " + CalculatorProviderContract.AUTHORITY + "! " + e);
            }
        }
        return sun;
    }

    /**
     * checkVersion
     * @return true Suntimes installed >= min_provider_version, or false < min_provider_version (or Suntimes missing)
     */
    public static boolean checkVersion(@NonNull Context context, SuntimesInfo info) {
        return (info != null && info.isInstalled && info.providerCode != null && info.providerCode >= minProviderVersion(context));
    }

    /**
     * @return the minimum provider version required by this addon
     */
    public static int minProviderVersion(@NonNull Context context) {
        return context.getResources().getInteger(R.integer.min_provider_version);
    }

    /**
     * @return additional Suntimes config info (may be null if not supported by provider)
     */
    public SuntimesOptions getOptions(@NonNull Context context) {
        if (options == null) {
            options = SuntimesOptions.queryInfo(context);
        }
        return options;
    }

    public String toString()
    {
        String retValue = getClass().getSimpleName() + " [" +
                appName + "(" + providerCode + ")" +" \n"
                + "permission: " + hasPermission + "\n"
                + "locale: " + appLocale + "\n"
                + "text size: " + appTextSize + "\n"
                + "theme: " + appTheme + " (" + appThemeOverride + ")\n"
                + "timezone: " + timezone + "\n";
        if (location != null) {
            retValue += "location: " + location[0] + "\n" + location[1] + ", " + location[2] + " [" + location[3] +"]" + "\n\n";
        } else {
            retValue += "location: null\n\n";
        }
        retValue += (options != null ? options.toString() : "null options");
        return retValue;
    }

    /**
     * SuntimesOptions
     */
    public static class SuntimesOptions
    {
        public boolean time_is24;
        public boolean time_showSeconds;
        public boolean time_showHours;
        public boolean time_showWeeks;
        public boolean time_showDateTime;
        public boolean use_altitude;
        public boolean show_warnings;
        public boolean verbose_talkback;
        public String length_units;
        public float object_height;
        public byte show_fields;

        public static final String UNITS_METRIC = "METRIC";
        public static final String UNITS_IMPERIAL = "IMPERIAL";

        public static final int FIELD_ACTUAL = 0;  // bit positions
        public static final int FIELD_CIVIL = 1;
        public static final int FIELD_NAUTICAL = 2;
        public static final int FIELD_ASTRO = 3;
        public static final int FIELD_NOON = 4;
        public static final int FIELD_GOLD = 5;
        public static final int FIELD_BLUE = 6;
        public static final int NUM_FIELDS = 7;

        public SuntimesOptions(Context context) {
            initFromContext(context);
        }

        public void initFromContext(@NonNull Context context)
        {
            time_is24 = Boolean.parseBoolean(context.getString(R.string.def_time_is24));
            time_showSeconds = Boolean.parseBoolean(context.getString(R.string.def_time_showseconds));
            time_showHours = Boolean.parseBoolean(context.getString(R.string.def_time_showhours));
            time_showWeeks = Boolean.parseBoolean(context.getString(R.string.def_time_showweeks));
            time_showDateTime = Boolean.parseBoolean(context.getString(R.string.def_time_showtimedate));
            use_altitude = Boolean.parseBoolean(context.getString(R.string.def_use_altitude));
            show_warnings = Boolean.parseBoolean(context.getString(R.string.def_show_warnings));
            verbose_talkback = Boolean.parseBoolean(context.getString(R.string.def_verbose_talkback));
            length_units = context.getString(R.string.def_length_units);
            object_height = Float.parseFloat(context.getString(R.string.def_object_height));
            show_fields = (byte)Integer.parseInt(context.getString(R.string.def_show_fields), 2);
        }

        public void initFromCursor(@NonNull Cursor cursor)
        {
            cursor.moveToFirst();
            time_is24 = (!cursor.isNull(0)) ? (cursor.getInt(0) == 1) : time_is24;
            time_showSeconds = (!cursor.isNull(1)) ? (cursor.getInt(1) == 1) : time_showSeconds;
            time_showHours = (!cursor.isNull(2)) ? (cursor.getInt(2) == 1) : time_showHours;
            time_showWeeks = (!cursor.isNull(3)) ? (cursor.getInt(3) == 1) : time_showWeeks;
            time_showDateTime = (!cursor.isNull(4)) ? (cursor.getInt(4) == 1) : time_showDateTime;
            use_altitude = (!cursor.isNull(5)) ? (cursor.getInt(5) == 1) : use_altitude;
            show_warnings = (!cursor.isNull(6)) ? (cursor.getInt(6) == 1) : show_warnings;
            verbose_talkback = (!cursor.isNull(7)) ? (cursor.getInt(7) == 1) : verbose_talkback;
            length_units = (!cursor.isNull(8)) ? cursor.getString(8) : length_units;
            object_height = (!cursor.isNull(9)) ? cursor.getFloat(9) : object_height;
            show_fields = (byte)((!cursor.isNull(10)) ? cursor.getInt(10) : show_fields);
        }

        public static SuntimesOptions queryInfo(@NonNull Context context)
        {
            SuntimesOptions options = new SuntimesOptions(context);
            ContentResolver resolver = context.getContentResolver();
            if (resolver != null)
            {
                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_CONFIG);
                try {
                    Cursor cursor = resolver.query(uri, SuntimesOptions.projection, null, null, null);
                    if (cursor != null) {
                        options.initFromCursor(cursor);
                        cursor.close();
                    } else {
                        Log.e(SuntimesOptions.class.getSimpleName(), "queryInfo: null cursor! defaults will be used.");
                    }
                } catch (SecurityException e) {
                    Log.e(SuntimesOptions.class.getSimpleName(), "queryInfo: Unable to access " + CalculatorProviderContract.AUTHORITY + "! " + e);
                }
            }
            return options;
        }

        public static final String[] projection = new String[] {
                CalculatorProviderContract.COLUMN_CONFIG_OPTION_TIME_IS24, CalculatorProviderContract.COLUMN_CONFIG_OPTION_TIME_SECONDS,
                CalculatorProviderContract.COLUMN_CONFIG_OPTION_TIME_HOURS, CalculatorProviderContract.COLUMN_CONFIG_OPTION_TIME_WEEKS,
                CalculatorProviderContract.COLUMN_CONFIG_OPTION_TIME_DATETIME, CalculatorProviderContract.COLUMN_CONFIG_OPTION_ALTITUDE,
                CalculatorProviderContract.COLUMN_CONFIG_OPTION_WARNINGS, CalculatorProviderContract.COLUMN_CONFIG_OPTION_TALKBACK,
                CalculatorProviderContract.COLUMN_CONFIG_LENGTH_UNITS, CalculatorProviderContract.COLUMN_CONFIG_OBJECT_HEIGHT,
                CalculatorProviderContract.COLUMN_CONFIG_OPTION_FIELDS
        };

        /**
         * @param field FIELD_ACTUAL, FIELD_CIVIL, FIELD_NAUTICAL, ...
         * @return true show field
         */
        public boolean showField( int field ) {
            return (((show_fields >> field) & 1) == 1);
        }

        public String toString()
        {
            return getClass().getSimpleName() + "\n" +
                    "time_is24: " + time_is24 + "\n" +
                    "time_showSeconds: " + time_showSeconds + "\n" +
                    "time_showHours: " + time_showHours + "\n" +
                    "time_showWeeks: " + time_showWeeks + "\n" +
                    "time_showDateTime: " + time_showDateTime + "\n" +
                    "use_altitude: " + use_altitude + "\n" +
                    "show_warnings: " + show_warnings + "\n" +
                    "verbose_talkback: " + verbose_talkback + "\n" +
                    "length units: " + length_units + "\n" +
                    "object height: " + object_height + "\n" +
                    "fields: " + show_fields + ": " + "\n" +
                    "\tActual: " + showField(FIELD_ACTUAL) + "\n" +
                    "\tCivil: " + showField(FIELD_CIVIL) + "\n" +
                    "\tNautical: " + showField(FIELD_NAUTICAL) + "\n" +
                    "\tAstro: " + showField(FIELD_ASTRO) + "\n" +
                    "\tNoon: " + showField(FIELD_NOON) + "\n" +
                    "\tGold: " + showField(FIELD_GOLD) + "\n" +
                    "\tBlue: " + showField(FIELD_BLUE);
        }
    }

}