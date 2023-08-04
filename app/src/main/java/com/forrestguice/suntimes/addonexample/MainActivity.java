/*
    Copyright (C) 2020-2021 Forrest Guice
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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.suntimes.actions.ActionsHelper;
import com.forrestguice.suntimes.actions.SuntimesActionsContract;
import com.forrestguice.suntimes.addon.AddonHelper;
import com.forrestguice.suntimes.addon.LocaleHelper;
import com.forrestguice.suntimes.addon.SuntimesInfo;

import com.forrestguice.suntimes.addon.ui.Messages;
import com.forrestguice.suntimes.alarm.AlarmHelper;
import com.forrestguice.suntimes.alarm.SuntimesAlarmsContract;
import com.forrestguice.suntimes.annotation.Nullable;
import com.forrestguice.suntimes.themes.SuntimesThemeContract;
import com.forrestguice.suntimes.themes.ThemeHelper;

public class MainActivity extends AppCompatActivity
{
    private SuntimesInfo suntimesInfo = null;

    @Override
    protected void attachBaseContext(Context context)
    {
        suntimesInfo = SuntimesInfo.queryInfo(context);    // obtain Suntimes version info
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_suntimes);
        }

        suntimesInfo.getOptions(this);
        initViews();
        updateViews();
    }

    protected void initViews()
    {
        initActionViews();
        initAlarmViews();
        initThemeViews();
        selectTheme(suntimesInfo.appThemeOverride);
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
                    AddonHelper.startSuntimesActivity(MainActivity.this);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case android.R.id.home:
                AddonHelper.startSuntimesActivity(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_THEME:
                onPickThemeResult(resultCode, data);
                break;

            case REQUEST_ACTION:
                onPickActionResult(resultCode, data);
                break;

            default:
                Log.w("SuntimesAddon", "unhandled result: " + requestCode);
                break;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private Spinner spin_alarms;
    private SimpleCursorAdapter alarmsAdapter;

    private void initAlarmViews()
    {
        spin_alarms = (Spinner)findViewById(R.id.spinner_alarm);
        if (spin_alarms != null)
        {
            if (suntimesInfo != null && suntimesInfo.appCode != null && suntimesInfo.appCode >= 59)    // v0.14.0  TODO: set req version code..
            {
                initAlarmsAdapter();
                spin_alarms.setAdapter(alarmsAdapter);

            } else {
                spin_alarms.setVisibility(View.GONE);
            }
        }

        TextView text_alarms = (TextView)findViewById(R.id.text_alarm);
        if (text_alarms != null)
        {
            text_alarms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAlarm();
                }
            });
        }
    }

    private void initAlarmsAdapter() {
        alarmsAdapter = createAlarmListCursorAdapter(this);
    }
    public static SimpleCursorAdapter createAlarmListCursorAdapter(Context context)
    {
        Cursor cursor = AlarmHelper.queryAlarms(context.getContentResolver());
        return new SimpleCursorAdapter(context, android.R.layout.two_line_list_item, cursor,
                new String[] { SuntimesAlarmsContract.KEY_ALARM_TYPE, SuntimesAlarmsContract.KEY_ALARM_SOLAREVENT }, new int[] { android.R.id.text1, android.R.id.text2 }, 0 );
    }

    private void showAlarm()
    {
        Cursor cursor = (Cursor)spin_alarms.getSelectedItem();
        long selected = cursor.getLong(cursor.getColumnIndex(SuntimesAlarmsContract.KEY_ROWID));
        AddonHelper.startSuntimesAlarmsActivity(MainActivity.this, selected);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private static final int REQUEST_ACTION = 200;

    private Spinner spin_actions;
    private SimpleCursorAdapter actionsAdapter;

    private void initActionViews()
    {
        spin_actions = (Spinner)findViewById(R.id.spinner_action);
        if (spin_actions != null)
        {
            if (suntimesInfo != null && suntimesInfo.appCode != null && suntimesInfo.appCode >= 59)    // v0.14.0  TODO: set req version code..
            {
                initActionsAdapter();
                spin_actions.setAdapter(actionsAdapter);

            } else {
                spin_actions.setVisibility(View.GONE);
            }
        }

        TextView text_action = (TextView)findViewById(R.id.text_action);
        if (text_action != null)
        {
            text_action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pickAction();
                }
            });
        }
    }

    private void initActionsAdapter() {
        actionsAdapter = createActionListCursorAdapter(this);
    }
    public static SimpleCursorAdapter createActionListCursorAdapter(Context context)
    {
        Cursor cursor = ActionsHelper.queryActions(context.getContentResolver());
        return new SimpleCursorAdapter(context, android.R.layout.two_line_list_item, cursor,
                new String[] { SuntimesActionsContract.COLUMN_ACTION_TITLE, SuntimesActionsContract.COLUMN_ACTION_DESC }, new int[] { android.R.id.text1, android.R.id.text2 }, 0 );
    }

    private void selectAction(@Nullable String actionID)
    {
        if (spin_actions != null && actionID != null)
        {
            Cursor cursor = (Cursor) spin_actions.getItemAtPosition(0);
            for (int i=0; i<spin_actions.getCount(); i++)
            {
                cursor.moveToPosition(i);
                String itemName = cursor.getString(cursor.getColumnIndex(SuntimesActionsContract.COLUMN_ACTION_NAME));
                if (itemName.equals(actionID)) {
                    spin_actions.setSelection(i);
                    break;
                }
            }
        }
    }

    private void pickAction()
    {
        Cursor cursor = (Cursor)spin_actions.getSelectedItem();
        String selected = cursor.getString(cursor.getColumnIndex(SuntimesActionsContract.COLUMN_ACTION_NAME));
        AddonHelper.startSuntimesActionsActivityForResult(MainActivity.this, REQUEST_ACTION, selected);
    }

    protected void onPickActionResult(int resultCode, Intent data)
    {
        Log.d("SuntimesAddon", "onPickActionResult: " + resultCode);
        if (resultCode == RESULT_OK)
        {
            if (data.getBooleanExtra("isModified", false)) {
                initActionsAdapter();
                spin_actions.setAdapter(actionsAdapter);
            }

            String actionName = data.getStringExtra("actionID");
            if (actionName != null) {
                selectAction(actionName);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private static final int REQUEST_THEME = 100;

    private Spinner spin_themes;
    private SimpleCursorAdapter themeAdapter;

    private void initThemeViews()
    {
        spin_themes = (Spinner)findViewById(R.id.spinner_theme);
        if (spin_themes != null)
        {
            if (suntimesInfo != null && suntimesInfo.appCode != null && suntimesInfo.appCode >= 59)    // v0.12.8 (60)
            {
                initThemeAdapter();
                spin_themes.setAdapter(themeAdapter);

            } else {
                spin_themes.setVisibility(View.GONE);
            }
        }

        TextView text_theme = (TextView)findViewById(R.id.text_theme);
        if (text_theme != null)
        {
            text_theme.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pickTheme();
                }
            });
        }
    }

    private void initThemeAdapter()
    {
        themeAdapter = createThemeListCursorAdapter(this);
    }
    public static SimpleCursorAdapter createThemeListCursorAdapter(Context context)
    {
        Cursor cursor = ThemeHelper.queryThemes(context.getContentResolver());
        return new SimpleCursorAdapter(context, android.R.layout.simple_list_item_1, cursor,
                new String[] { SuntimesThemeContract.THEME_DISPLAYSTRING }, new int[] { android.R.id.text1 }, 0 );
    }

    private void selectTheme(@Nullable String themeName)
    {
        if (spin_themes != null && themeName != null)
        {
            Cursor cursor = (Cursor) spin_themes.getItemAtPosition(0);
            for (int i=0; i<spin_themes.getCount(); i++)
            {
                cursor.moveToPosition(i);
                String itemName = cursor.getString(cursor.getColumnIndex(SuntimesThemeContract.THEME_NAME));
                if (itemName.equals(themeName)) {
                    spin_themes.setSelection(i);
                    break;
                }
            }
        }
    }

    private void pickTheme()
    {
        Cursor cursor = (Cursor)spin_themes.getSelectedItem();
        String selected = cursor.getString(cursor.getColumnIndex(SuntimesThemeContract.THEME_NAME));
        AddonHelper.startSuntimesThemesActivityForResult(MainActivity.this, REQUEST_THEME, selected);
    }

    protected void onPickThemeResult(int resultCode, Intent data)
    {
        Log.d("SuntimesAddon", "onPickThemeResult: " + resultCode);
        if (resultCode == RESULT_OK)
        {
            if (data.getBooleanExtra("isModified", false)) {
                initThemeAdapter();
                spin_themes.setAdapter(themeAdapter);
            }

            String themeName = data.getStringExtra(SuntimesThemeContract.THEME_NAME);
            if (themeName != null) {
                selectTheme(themeName);
            }
        }
    }
}