/**
    Copyright (C) 2025 Forrest Guice
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
package com.forrestguice.suntimes.crashreport;

import android.content.Context;

import com.forrestguice.suntimes.addon.R;

/**
 * An `ExceptionNotification` with default notification/channel values.
 * Note: add `ExceptionActivity` to your manifest.
 * Note: declare and request the follow 'dangerous' permission when targeting api33+;
 *       <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
 * @see ExceptionNotification
 * @see ExceptionActivity
 */
public abstract class ExceptionNotification1 extends ExceptionNotification
{
    protected abstract String getAppName(Context context);

    @Override
    protected String getChannelTitle(Context context) {
        return context.getString(R.string.crash_channel_title);
    }

    @Override
    protected String getChannelDesc(Context context) {
        return context.getString(R.string.crash_channel_desc);
    }

    @Override
    protected String getNotificationTitle(Context context) {
        return context.getString(R.string.crash_dialog_message, getAppName(context));
    }

    @Override
    protected String getNotificationActionText(Context context) {
        return context.getString(R.string.crash_dialog_view);
    }

    @Override
    protected int getNotificationIconResID() {
        return R.drawable.ic_status_error;
    }

    @Override
    protected Class<? extends ExceptionActivity> getCrashReportActivityClass() {
        return ExceptionActivity.class;    // make sure this class is defined in the manifest
    }
}
