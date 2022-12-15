/*
    Copyright (C) 2022 Forrest Guice
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

package com.forrestguice.suntimes.addonexample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.forrestguice.suntimes.addon.AddonHelper;
import com.forrestguice.suntimes.addon.LocaleHelper;
import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.addon.ui.Messages;
import com.forrestguice.suntimes.alarm.AlarmHelper;

/**
 * An example "AlarmPicker" activity that may be started by SuntimesAlarms when choosing an alarm event.
 */
public class AlarmPickerActivity extends AppCompatActivity
{
    String AUTHORITY = "suntimes.addonexample.provider";

    private SuntimesInfo suntimesInfo = null;

    protected String getSelectedAlarmID() {
        return "NOON";
    }
    protected String getSelectedAlarmTitle() {
        return "Addon Event (Noon)";
    }

    protected void onDone()
    {
        Intent result = AlarmHelper.getEventData(AUTHORITY, getSelectedAlarmID(), getSelectedAlarmTitle(), "An addon alarm event example." );
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    protected void onCanceled() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    protected void attachBaseContext(Context context)
    {
        suntimesInfo = SuntimesInfo.queryInfo(context);    // obtain Suntimes version info
        super.attachBaseContext( (suntimesInfo != null && suntimesInfo.appLocale != null) ?    // override the locale
                LocaleHelper.loadLocale(context, suntimesInfo.appLocale) : context );
    }

    @Override
    protected void onCreate(Bundle savedState)
    {
        setResult(RESULT_CANCELED);
        super.onCreate(savedState);
        if (suntimesInfo.appTheme != null) {    // override the theme
            setTheme(suntimesInfo.appTheme.equals(SuntimesInfo.THEME_LIGHT) ? R.style.AppTheme_Light : R.style.AppTheme_Dark);
        }
        setContentView(R.layout.activity_pick_alarm);

        /*Intent intent = getIntent();
        if (intent != null) {
            Uri data = intent.getData();
        }
        if (savedState != null) {
        }*/

        suntimesInfo.getOptions(this);
        initViews();
        updateViews();
    }

    @Override
    public void onSaveInstanceState( Bundle outState ) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        String appTheme = SuntimesInfo.queryAppTheme(getContentResolver());
        if (appTheme != null && !appTheme.equals(suntimesInfo.appTheme)) {
            recreate();
        }
    }

    protected void initViews()
    {
        Button okButton = (Button) findViewById(R.id.ok_button);
        if (okButton != null)
        {
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDone();
                }
            });
        }

        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        if (cancelButton != null)
        {
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCanceled();
                }
            });
        }
    }

    protected void updateViews()
    {
        checkVersion();    // check dependencies and display warnings

        TextView text_eventName = (TextView) findViewById(R.id.text_eventName);
        if (text_eventName != null) {
            text_eventName.setText(getSelectedAlarmTitle());
        }
    }

    protected void checkVersion()
    {
        if (!SuntimesInfo.checkVersion(this, suntimesInfo))
        {
            View view = getWindow().getDecorView().findViewById(android.R.id.content);
            if (!suntimesInfo.hasPermission && suntimesInfo.isInstalled)
                Messages.showPermissionDeniedMessage(this, view);
            else Messages.showMissingDependencyMessage(this, view);
        }
    }

}