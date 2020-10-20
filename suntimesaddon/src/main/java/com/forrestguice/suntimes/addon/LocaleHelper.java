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

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Locale;

/**
 * LocaleHelper
 * A helper class for loading or resetting an activity's locale.
 *
 * To use, add the following to your activity:
 * ```
 * @Override
 * protected void attachBaseContext(Context context){
 *     super.attachBaseContext( LocaleHelper.loadLocale(context, "es") );    // e.g. load Spanish (es)
 * }
 * ```
 */
@SuppressWarnings("WeakerAccess")
public class LocaleHelper
{
    protected static String systemLocale = null;  // null until locale is overridden w/ loadLocale

    /**
     * @return the system locale (null until set by loadLocale).
     */
    public String getSystemLocale() {
         return systemLocale;
    }

    /**
     * localeLocale
     */
    public static Context loadLocale( Context context, String languageTag )
    {
        if (systemLocale == null) {
            systemLocale = Locale.getDefault().getLanguage();
        }

        Locale customLocale = localeForLanguageTag(languageTag);
        Locale.setDefault(customLocale);
        Log.i(SuntimesInfo.class.getSimpleName(), "loadLocale: " + languageTag);

        Resources resources = context.getApplicationContext().getResources();
        Configuration config = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= 17)
            config.setLocale(customLocale);
        else config.locale = customLocale;

        if (Build.VERSION.SDK_INT >= 25) {
            return new ContextWrapper(context.createConfigurationContext(config));

        } else {
            DisplayMetrics metrics = resources.getDisplayMetrics();
            //noinspection deprecation
            resources.updateConfiguration(config, metrics);
            return new ContextWrapper(context);
        }
    }

    /**
     * resetLocale
     */
    public static Context resetLocale( Context context )
    {
        if (systemLocale != null) {
            return loadLocale(context, systemLocale);
        }
        return context;
    }

    /**
     * localeForLanguageTag
     */
    public static @NonNull Locale localeForLanguageTag(@NonNull String languageTag)
    {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            locale = Locale.forLanguageTag(languageTag.replaceAll("_", "-"));

        } else {
            String[] parts = languageTag.split("[_]");
            String language = parts[0];
            String country = (parts.length >= 2) ? parts[1] : null;
            locale = (country != null) ? new Locale(language, country) : new Locale(language);
        }
        Log.d(SuntimesInfo.class.getSimpleName(), "localeForLanguageTag: tag: " + languageTag + " :: locale: " + locale.toString());
        return locale;
    }
}