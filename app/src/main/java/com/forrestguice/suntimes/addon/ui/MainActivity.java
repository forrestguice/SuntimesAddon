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

package com.forrestguice.suntimes.addon.ui;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.forrestguice.suntimes.addon.AddonHelper;
import com.forrestguice.suntimes.addon.LocaleHelper;
import com.forrestguice.suntimes.addon.SuntimesInfo;

import com.forrestguice.suntimes.addon.R;

public class MainActivity extends AppCompatActivity
{
    private SuntimesInfo suntimesInfo = null;

    @Override
    protected void attachBaseContext(Context context)
    {
        suntimesInfo = SuntimesInfo.queryInfo(context.getContentResolver());    // obtain Suntimes version info
        super.attachBaseContext( (suntimesInfo != null && suntimesInfo.appLocale != null) ?    // override the locale
                LocaleHelper.loadLocale(context, suntimesInfo.appLocale) : context );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (suntimesInfo.appTheme != null) {    // override the theme
            setTheme(suntimesInfo.appTheme.equals(SuntimesInfo.THEME_LIGHT) ? R.style.AppTheme_Light : R.style.AppTheme_Dark);
        }
        setContentView(R.layout.activity_main);
        updateViews();
    }

    protected void updateViews()
    {
        checkVersion();    // check dependencies and display warnings

        TextView text_suntimesVersion = findViewById(R.id.text_suntimes_version);
        if (suntimesInfo != null && text_suntimesVersion != null)
        {
            text_suntimesVersion.setText(
                    suntimesInfo.appName + "(" + suntimesInfo.providerCode + ")" +" \n"
                    + "permission: " + suntimesInfo.hasPermission + "\n"
                    + "locale: " + suntimesInfo.appLocale + "\n"
                    + "theme: " + suntimesInfo.appTheme + "\n"
                    + "timezone: " + suntimesInfo.timezone + "\n"
                    + "location: " + suntimesInfo.location[0] + "\n" + suntimesInfo.location[1] + ", " + suntimesInfo.location[2] + " [" + suntimesInfo.location[3] +"]"
            );
            text_suntimesVersion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddonHelper.startSuntimesAlarmsActivity(MainActivity.this);
                }
            });
        }
    }

    protected void checkVersion()
    {
        if (!SuntimesInfo.checkVersion(this, suntimesInfo))
        {
            View view = getWindow().getDecorView().findViewById(android.R.id.content);
            if (!suntimesInfo.hasPermission)
                Messages.showPermissionDeniedMessage(this, view);
            else Messages.showMissingDependencyMessage(this, view);
        }
    }

}