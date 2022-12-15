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

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.forrestguice.suntimes.addon.AddonHelper;
import com.forrestguice.suntimes.addon.LocaleHelper;
import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.addon.ui.Messages;
import com.forrestguice.suntimes.alarm.AlarmHelper;

/**
 * An example "DismissChallenge" activity that is displayed by SuntimesAlarms before an alarm may be
 * dismissed. The activity needs to return `RESULT_OK` when the challenge is passed (and the alarm
 * should be dismissed).
 */
public class DismissChallengeActivity extends AppCompatActivity
{
    private SuntimesInfo suntimesInfo = null;

    private long alarmID = -1;
    public void setAlarmID(long alarmID) {
        this.alarmID = alarmID;
    }

    /**
     * onChallengePassed
     * Pass RESULT_OK back to the calling activity.
     */
    protected void onChallengePassed() {
        setResult(RESULT_OK, new Intent().setData(AlarmHelper.getAlarmUri(alarmID)));
        finish();
    }

    /**
     * onSnooze
     * Pass ACTION_SNOOZE back to the calling activity with RESULT_CANCELED.
     */
    protected void onSnooze() {
        setResult(RESULT_CANCELED, new Intent().setAction(AlarmHelper.ACTION_SNOOZE).setData(AlarmHelper.getAlarmUri(alarmID)));
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
        setContentView(R.layout.activity_dismiss_alarm);

        Intent intent = getIntent();
        if (intent != null)
        {
            Uri data = intent.getData();
            if (data != null) {
                setAlarmID(ContentUris.parseId(data));
            } else {
                setAlarmID(intent.getLongExtra(AlarmHelper.EXTRA_SELECTED_ALARM, alarmID));
            }
        }
        if (savedState != null) {
            alarmID = savedState.getLong(AlarmHelper.EXTRA_SELECTED_ALARM, -1);
        }

        suntimesInfo.getOptions(this);
        initViews();
        updateViews();
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        outState.putLong(AlarmHelper.EXTRA_SELECTED_ALARM, alarmID);
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
        Button dismissButton = (Button) findViewById(R.id.dismiss_button);
        if (dismissButton != null) {
            dismissButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onChallengePassed();
                }
            });
        }

        Button snoozeButton = (Button) findViewById(R.id.snooze_button);
        if (snoozeButton != null) {
            snoozeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSnooze();
                }
            });
        }
    }

    protected void updateViews()
    {
        checkVersion();    // check dependencies and display warnings

        TextView text_suntimesVersion = (TextView)findViewById(R.id.text_suntimes_version);
        if (suntimesInfo != null && text_suntimesVersion != null)
        {
            text_suntimesVersion.setText(suntimesInfo.toString());
            text_suntimesVersion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddonHelper.startSuntimesActivity(DismissChallengeActivity.this);
                }
            });
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