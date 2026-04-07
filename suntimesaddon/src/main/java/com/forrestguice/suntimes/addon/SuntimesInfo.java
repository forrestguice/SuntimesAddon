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
import android.util.Log;

import com.forrestguice.suntimes.annotation.NonNull;
import com.forrestguice.suntimes.annotation.Nullable;
import com.forrestguice.suntimes.calculator.core.CalculatorProviderContract;

import java.util.Calendar;

import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_LENGTH_UNITS;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OBJECT_HEIGHT;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_ALTITUDE;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_FIELDS;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_TALKBACK;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_TIME_DATETIME;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_TIME_HOURS;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_TIME_IS24;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_TIME_SECONDS;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_TIME_WEEKS;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_OPTION_WARNINGS;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION_CODE;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_APP_VERSION_CODE;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_APP_VERSION;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_LOCALE;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_APP_THEME;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_APP_THEME_OVERRIDE;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_TIMEZONE;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_TIMEZONEMODE;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_SOLARTIMEMODE;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_LOCATION;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_LATITUDE;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_LONGITUDE;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_ALTITUDE;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION_CODE_V2;
import static com.forrestguice.suntimes.calculator.core.CalculatorProviderContract.COLUMN_CONFIG_APP_TEXT_SIZE;

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

    public static final String THEME_CONTRAST_LIGHT = "contrast_light";
    public static final String THEME_CONTRAST_DARK = "contrast_dark";
    public static final String THEME_CONTRAST_SYSTEM = "contrast_system";

    public static final String THEME_MONET_SYSTEM = "monet_system";
    public static final String THEME_MONET_LIGHT = "monet_light";
    public static final String THEME_MONET_DARK = "monet_dark";

    public SuntimesInfo(@NonNull Context context) {
    }

    public static String AUTHORITY = CalculatorProviderContract.AUTHORITY;
    public static void setAuthorityRoot(@NonNull String value) {
        AUTHORITY = value + "." + CalculatorProviderContract.AUTHORITY_ID;
    }

    protected static final String[] projection = new String[] {
            COLUMN_CONFIG_PROVIDER_VERSION_CODE, COLUMN_CONFIG_APP_VERSION_CODE,
            COLUMN_CONFIG_PROVIDER_VERSION,      COLUMN_CONFIG_APP_VERSION,
            COLUMN_CONFIG_LOCALE,
            COLUMN_CONFIG_APP_THEME, COLUMN_CONFIG_APP_THEME_OVERRIDE,
            COLUMN_CONFIG_TIMEZONE, COLUMN_CONFIG_TIMEZONEMODE, COLUMN_CONFIG_SOLARTIMEMODE,
            COLUMN_CONFIG_LOCATION, COLUMN_CONFIG_LATITUDE,
            COLUMN_CONFIG_LONGITUDE, COLUMN_CONFIG_ALTITUDE,
            COLUMN_CONFIG_PROVIDER_VERSION_CODE_V2,    // legacy support
            COLUMN_CONFIG_APP_TEXT_SIZE
    };

    public void initFromCursor(@NonNull Cursor cursor)
    {
        cursor.moveToFirst();

        int i_providerCode = cursor.getColumnIndex(COLUMN_CONFIG_PROVIDER_VERSION_CODE);  // 0
        providerCode = (i_providerCode >=0 && !cursor.isNull(i_providerCode)) ? cursor.getInt(i_providerCode) : null;

        int i_appCode = cursor.getColumnIndex(COLUMN_CONFIG_APP_VERSION_CODE);            // 1
        appCode = (i_appCode >= 0 && !cursor.isNull(i_appCode)) ? cursor.getInt(i_appCode) : null;

        int i_providerName = cursor.getColumnIndex(COLUMN_CONFIG_PROVIDER_VERSION);       // 2
        providerName = (i_providerName >= 0 && !cursor.isNull(i_providerName)) ? cursor.getString(i_providerName) : null;

        int i_appName = cursor.getColumnIndex(COLUMN_CONFIG_APP_VERSION);     // 3
        appName = (i_appName >= 0 && !cursor.isNull(i_appName)) ? cursor.getString(i_appName) : null;

        int i_appLocale = cursor.getColumnIndex(COLUMN_CONFIG_LOCALE);   // 4
        appLocale = (i_appLocale >= 0 && !cursor.isNull(i_appLocale)) ? cursor.getString(i_appLocale) : null;

        int i_appTheme = cursor.getColumnIndex(COLUMN_CONFIG_APP_THEME);    // 5
        appTheme = (i_appTheme >= 0 && !cursor.isNull(i_appTheme)) ? cursor.getString(i_appTheme) : null;

        int i_appThemeOverride = cursor.getColumnIndex(COLUMN_CONFIG_APP_THEME_OVERRIDE);    // 6
        appThemeOverride = (i_appThemeOverride >= 0 && !cursor.isNull(i_appThemeOverride)) ? cursor.getString(i_appThemeOverride) : null;

        int i_timezone = cursor.getColumnIndex(COLUMN_CONFIG_TIMEZONE);    // 7
        timezone = (i_timezone >= 0 && !cursor.isNull(i_timezone)) ? cursor.getString(i_timezone) : null;

        int i_timezoneMode = cursor.getColumnIndex(COLUMN_CONFIG_TIMEZONEMODE);    // 8
        timezoneMode = (i_timezoneMode >= 0 && !cursor.isNull(i_timezoneMode)) ? cursor.getString(i_timezoneMode) : null;

        int i_solartimeMode = cursor.getColumnIndex(COLUMN_CONFIG_SOLARTIMEMODE);    // 9
        solartimeMode = (i_solartimeMode >= 0 && !cursor.isNull(i_solartimeMode)) ? cursor.getString(i_solartimeMode) : null;

        int i_location = cursor.getColumnIndex(COLUMN_CONFIG_LOCATION);      // 10
        int i_latitude = cursor.getColumnIndex(COLUMN_CONFIG_LATITUDE);      // 11
        int i_longitude = cursor.getColumnIndex(COLUMN_CONFIG_LONGITUDE);    // 12
        int i_altitude = cursor.getColumnIndex(COLUMN_CONFIG_ALTITUDE);      // 13

        location = new String[4];
        location[0] = (i_location >= 0 && !cursor.isNull(i_location)) ? cursor.getString(i_location) : null;
        location[1] = (i_latitude >= 0 && !cursor.isNull(i_latitude)) ? cursor.getString(i_latitude) : null;
        location[2] = (i_longitude >= 0 && !cursor.isNull(i_longitude)) ? cursor.getString(i_longitude) : null;
        location[3] = (i_altitude >= 0 && !cursor.isNull(i_altitude)) ? cursor.getString(i_altitude) : null;

        if (providerCode == null) {    // is this an older provider? limited support
            int i_providerCode2 = cursor.getColumnIndex(COLUMN_CONFIG_PROVIDER_VERSION_CODE_V2);    // 14
            providerCode = (i_providerCode2 >= 0 && !cursor.isNull(i_providerCode2)) ? cursor.getInt(i_providerCode2) : null;
        }

        int i_appTextSize = cursor.getColumnIndex(COLUMN_CONFIG_APP_TEXT_SIZE);    // 15
        appTextSize = (i_appTextSize >= 0 && !cursor.isNull(i_appTextSize)) ? cursor.getString(i_appTextSize) : null;

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
            Uri uri = Uri.parse("content://" + AUTHORITY + "/" + CalculatorProviderContract.QUERY_CONFIG );

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
                Log.e(SuntimesInfo.class.getSimpleName(), "queryInfo: Unable to access " + AUTHORITY + "! " + e);
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
            Uri uri = Uri.parse("content://" + AUTHORITY + "/" + CalculatorProviderContract.QUERY_CONFIG );
            try {
                Cursor cursor = resolver.query(uri, new String[] { COLUMN_CONFIG_APP_THEME } , null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int i_theme = cursor.getColumnIndex(COLUMN_CONFIG_APP_THEME);
                    theme = (i_theme >= 0 && !cursor.isNull(i_theme) ? cursor.getString(i_theme) : THEME_DARK);
                    cursor.close();
                }
            } catch (SecurityException e) {
                Log.e(SuntimesInfo.class.getSimpleName(), "queryInfo: Unable to access " + AUTHORITY + "! " + e);
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
            Uri uri = Uri.parse("content://" + AUTHORITY + "/" + CalculatorProviderContract.QUERY_SUN + "/" + dateMillis );
            try {
                Cursor cursor = resolver.query(uri, new String[] { CalculatorProviderContract.COLUMN_SUN_ACTUAL_RISE, CalculatorProviderContract.COLUMN_SUN_ACTUAL_SET } , null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int i_sunrise = cursor.getColumnIndex(CalculatorProviderContract.COLUMN_SUN_ACTUAL_RISE);     // 0
                    int i_sunset = cursor.getColumnIndex(CalculatorProviderContract.COLUMN_SUN_ACTUAL_SET);       // 1
                    sun[0] = (i_sunrise >= 0 && !cursor.isNull(i_sunrise) ? cursor.getLong(i_sunrise) : null);
                    sun[1] = (i_sunset >= 0 && !cursor.isNull(i_sunset) ? cursor.getLong(i_sunset) : null);
                    cursor.close();
                }
            } catch (SecurityException e) {
                Log.e(SuntimesInfo.class.getSimpleName(), "queryInfo: Unable to access " + AUTHORITY + "! " + e);
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
        if (location != null)
        {
            retValue += "location: ";
            for (int i=0; i<location.length; i++) {
                //noinspection StringConcatenationInLoop
                retValue += location[i];
                if (i != location.length-1) {
                    retValue += ", ";
                }
            }
            retValue += "\n\n";

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

            int i_time_is24 = cursor.getColumnIndex(COLUMN_CONFIG_OPTION_TIME_IS24);    // 0
            time_is24 = (i_time_is24 >= 0 && !cursor.isNull(i_time_is24)) ? (cursor.getInt(i_time_is24) == 1) : time_is24;

            int i_time_showSeconds = cursor.getColumnIndex(COLUMN_CONFIG_OPTION_TIME_SECONDS);    // 1
            time_showSeconds = (i_time_showSeconds >=0 && !cursor.isNull(i_time_showSeconds)) ? (cursor.getInt(i_time_showSeconds) == 1) : time_showSeconds;

            int i_time_showHours = cursor.getColumnIndex(COLUMN_CONFIG_OPTION_TIME_HOURS);    // 2
            time_showHours = (i_time_showHours >= 0 && !cursor.isNull(i_time_showHours)) ? (cursor.getInt(i_time_showHours) == 1) : time_showHours;

            int i_time_showWeeks = cursor.getColumnIndex(COLUMN_CONFIG_OPTION_TIME_WEEKS);    // 3
            time_showWeeks = (i_time_showWeeks >= 0 && !cursor.isNull(i_time_showWeeks)) ? (cursor.getInt(i_time_showWeeks) == 1) : time_showWeeks;

            int i_time_showDateTime = cursor.getColumnIndex(COLUMN_CONFIG_OPTION_TIME_DATETIME);    // 4
            time_showDateTime = (i_time_showDateTime >= 0 && !cursor.isNull(i_time_showDateTime)) ? (cursor.getInt(i_time_showDateTime) == 1) : time_showDateTime;

            int i_use_altitude = cursor.getColumnIndex(COLUMN_CONFIG_OPTION_ALTITUDE);    // 5
            use_altitude = (i_use_altitude >= 0 && !cursor.isNull(i_use_altitude)) ? (cursor.getInt(i_use_altitude) == 1) : use_altitude;

            int i_show_warnings = cursor.getColumnIndex(COLUMN_CONFIG_OPTION_WARNINGS);    // 6
            show_warnings = (i_show_warnings >= 0 && !cursor.isNull(i_show_warnings)) ? (cursor.getInt(i_show_warnings) == 1) : show_warnings;

            int i_verbose_talkback = cursor.getColumnIndex(COLUMN_CONFIG_OPTION_TALKBACK);    // 7
            verbose_talkback = (i_verbose_talkback >= 0 && !cursor.isNull(i_verbose_talkback)) ? (cursor.getInt(i_verbose_talkback) == 1) : verbose_talkback;

            int i_length_units = cursor.getColumnIndex(COLUMN_CONFIG_LENGTH_UNITS);    // 8
            length_units = (i_length_units >= 0 && !cursor.isNull(i_length_units)) ? cursor.getString(i_length_units) : length_units;

            int i_object_height = cursor.getColumnIndex(COLUMN_CONFIG_OBJECT_HEIGHT);    // 9
            object_height = (i_object_height >= 0 && !cursor.isNull(i_object_height)) ? cursor.getFloat(i_object_height) : object_height;

            int i_show_fields = cursor.getColumnIndex(COLUMN_CONFIG_OPTION_FIELDS);    // 10
            show_fields = (byte)(i_show_fields >= 0 && (!cursor.isNull(i_show_fields)) ? cursor.getInt(i_show_fields) : show_fields);
        }

        public static SuntimesOptions queryInfo(@NonNull Context context)
        {
            SuntimesOptions options = new SuntimesOptions(context);
            ContentResolver resolver = context.getContentResolver();
            if (resolver != null)
            {
                Uri uri = Uri.parse("content://" + AUTHORITY + "/" + CalculatorProviderContract.QUERY_CONFIG);
                try {
                    Cursor cursor = resolver.query(uri, SuntimesOptions.projection, null, null, null);
                    if (cursor != null) {
                        options.initFromCursor(cursor);
                        cursor.close();
                    } else {
                        Log.e(SuntimesOptions.class.getSimpleName(), "queryInfo: null cursor! defaults will be used.");
                    }
                } catch (SecurityException e) {
                    Log.e(SuntimesOptions.class.getSimpleName(), "queryInfo: Unable to access " + AUTHORITY + "! " + e);
                }
            }
            return options;
        }

        public static final String[] projection = new String[] {
                COLUMN_CONFIG_OPTION_TIME_IS24, COLUMN_CONFIG_OPTION_TIME_SECONDS,
                COLUMN_CONFIG_OPTION_TIME_HOURS, COLUMN_CONFIG_OPTION_TIME_WEEKS,
                COLUMN_CONFIG_OPTION_TIME_DATETIME, COLUMN_CONFIG_OPTION_ALTITUDE,
                COLUMN_CONFIG_OPTION_WARNINGS, COLUMN_CONFIG_OPTION_TALKBACK,
                COLUMN_CONFIG_LENGTH_UNITS, COLUMN_CONFIG_OBJECT_HEIGHT,
                COLUMN_CONFIG_OPTION_FIELDS
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