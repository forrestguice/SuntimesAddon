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

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.forrestguice.suntimes.addon.ui.NotificationCompat;
import com.forrestguice.suntimes.annotation.Nullable;

/**
 * This class defines a notification to be displayed if the app crashes with an Exception.
 * @see ExceptionHandler
 * @see ExceptionNotification1
 */
public abstract class ExceptionNotification
{
    protected abstract String getChannelID();
    protected abstract String getChannelTitle(Context context);
    protected abstract String getChannelDesc(Context context);

    @TargetApi(24)
    protected int getChannelImportance() {
        return NotificationManager.IMPORTANCE_HIGH;
    }

    protected abstract int getNotificationID();
    protected abstract String getNotificationTitle(Context context);
    protected abstract String getNotificationActionText(Context context);
    protected abstract int getNotificationIconResID();

    private String message = "";
    protected String getNotificationMessage(Context context) {
        return message;
    }
    public void setNotificationMessage(String value) {
        message = value;
    }

    protected int getNotificationVisibility() {
        return NotificationCompat.VISIBILITY_PUBLIC;
    }
    protected int getNotificationPriority() {
        return NotificationCompat.PRIORITY_HIGH;
    }
    protected String getNotificationCategory() {
        return NotificationCompat.CATEGORY_ERROR;
    }

    protected abstract Class<? extends ExceptionActivity> getCrashReportActivityClass();

    @Nullable
    protected Intent getCrashReportActivityIntent(Context context, String report, String supportURL)
    {
        if (context != null)
        {
            Intent intent = new Intent(context, getCrashReportActivityClass());
            intent.setAction(Intent.ACTION_MAIN);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(ExceptionActivity.EXTRA_REPORT, report);
            intent.putExtra(ExceptionActivity.EXTRA_SUPPORT_URL, supportURL);
            return intent;

        } else {
            Log.e("CRASH", "launchCrashReportActivity: null context!");
            return null;
        }
    }

    public Notification createNotification(Context context, String report, String supportURL)
    {
        NotificationCompat.Builder builder = createNotificationBuilder(context);
        builder.setDefaults(Notification.DEFAULT_LIGHTS);
        builder.setVisibility(getNotificationVisibility());
        builder.setPriority(getNotificationPriority());
        builder.setCategory(getNotificationCategory());
        builder.setSmallIcon(getNotificationIconResID());
        builder.setAutoCancel(true);

        String title = getNotificationTitle(context);
        String message = getNotificationMessage(context);
        if (title != null) {
            builder.setContentTitle(title);
        }
        builder.setContentText(message);

        PendingIntent intent = PendingIntent.getActivity(context, message.hashCode(), getCrashReportActivityIntent(context, report, supportURL), PendingIntent.FLAG_UPDATE_CURRENT);
        String actionText = getNotificationActionText(context);
        if (actionText != null) {
            builder.addAction(getNotificationIconResID(), getNotificationActionText(context), intent);
        } else {
            builder.setContentIntent(intent);
        }
        return builder.build();
    }

    protected NotificationCompat.Builder createNotificationBuilder(Context context)
    {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            builder = new NotificationCompat.Builder(context, createNotificationChannel(context));
        } else {
            //noinspection deprecation
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    protected String createNotificationChannel(Context context)
    {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
        {
            NotificationChannel channel = new NotificationChannel(getChannelID(), getChannelTitle(context), getChannelImportance());
            channel.setDescription(getChannelDesc(context));
            notificationManager.createNotificationChannel(channel);
            return getChannelID();
        }
        return "";
    }
}
