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

/**
 * SuntimesInfo
 */
public final class SuntimesInfo
{
    public Integer providerCode = null;
    public String providerName = null;
    public boolean isInstalled = false;
    public boolean hasPermission = false;

    public Integer appCode = null;
    public String appName = null;
    public String appLocale = null;
    public String appTheme = null;

    public String timezone = null;
    public String[] location = null;    // [0]label, [1]latitude (dd), [2]longitude (dd), [3]altitude (meters)

    public static final String[] projection = new String[] { CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION_CODE, CalculatorProviderContract.COLUMN_CONFIG_APP_VERSION_CODE,
                                                             CalculatorProviderContract.COLUMN_CONFIG_PROVIDER_VERSION,      CalculatorProviderContract.COLUMN_CONFIG_APP_VERSION,
                                                             CalculatorProviderContract.COLUMN_CONFIG_LOCALE,                CalculatorProviderContract.COLUMN_CONFIG_APP_THEME,
                                                             CalculatorProviderContract.COLUMN_CONFIG_TIMEZONE,
                                                             CalculatorProviderContract.COLUMN_CONFIG_LOCATION, CalculatorProviderContract.COLUMN_CONFIG_LATITUDE,
                                                             CalculatorProviderContract.COLUMN_CONFIG_LONGITUDE, CalculatorProviderContract.COLUMN_CONFIG_ALTITUDE };
    public static final String THEME_LIGHT = "light", THEME_DARK = "dark";

    public void initFromCursor(@NonNull Cursor cursor)
    {
        cursor.moveToFirst();
        providerCode = (!cursor.isNull(0)) ? cursor.getInt(0) : null;
        appCode = (!cursor.isNull(1)) ? cursor.getInt(1) : null;
        providerName = (!cursor.isNull(2)) ? cursor.getString(2) : null;
        appName = (!cursor.isNull(3)) ? cursor.getString(3) : null;
        appLocale = (!cursor.isNull(4)) ? cursor.getString(4) : null;
        appTheme = (!cursor.isNull(5)) ? cursor.getString(5) : null;
        timezone = (!cursor.isNull(6)) ? cursor.getString(6) : null;
        location = new String[4];
        location[0] = (!cursor.isNull(7)) ? cursor.getString(7) : null;
        location[1] = (!cursor.isNull(8)) ? cursor.getString(8) : null;
        location[2] = (!cursor.isNull(9)) ? cursor.getString(9) : null;
        location[3] = (!cursor.isNull(9)) ? cursor.getString(10) : null;
        hasPermission = isInstalled = (providerCode != null);
    }

    /**
     * querySuntimesInfo
     * @param resolver ContentResolver
     * @return a SuntimesInfo obj with version info, or null if resolver is null
     */
    public static SuntimesInfo queryInfo(@Nullable ContentResolver resolver)
    {
        SuntimesInfo info = null;
        if (resolver != null)
        {
            info = new SuntimesInfo();
            Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_CONFIG );

            try {
                Cursor cursor = resolver.query(uri, SuntimesInfo.projection, null, null, null);
                info.isInstalled = true;
                info.hasPermission = true;

                if (cursor != null)
                {
                    info.initFromCursor(cursor);
                    cursor.close();

                } else {               // null cursor but no SecurityException.. Suntimes isn't installed at all
                    info.isInstalled = false;
                }

            } catch (SecurityException e) {
                Log.e(SuntimesInfo.class.getSimpleName(), "queryInfo: Unable to access " + CalculatorProviderContract.AUTHORITY + "! " + e);
                info.hasPermission = false;
            }
        }
        return info;
    }

    /**
     * checkVersion
     * @return true Suntimes installed >= min_provider_version, or false < min_provider_version (or Suntimes missing)
     */
    public static boolean checkVersion(@NonNull Context context, SuntimesInfo info) {
        return (info != null && info.isInstalled && info.providerCode >= minProviderVersion(context));
    }

    /**
     * @return the minimum provider version required by this addon
     */
    public static int minProviderVersion(@NonNull Context context) {
        return context.getResources().getInteger(R.integer.min_provider_version);
    }
}