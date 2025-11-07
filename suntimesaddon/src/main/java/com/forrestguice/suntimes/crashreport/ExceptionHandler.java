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

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.lang.ref.WeakReference;

import com.forrestguice.suntimes.addon.ui.NotificationManagerCompat;

/**
 * 1) Add `ExceptionActivity` to your manifest.
 * 2) Extend from `ExceptionHandler` and call `setDefaultUncaughtExceptionHandler` from your app's
 * entry point. e.g.
 *     public class MyApplication extends Application ... OR ...
 *     public class MyContentProvider extends ContentProvider
 *     {
 *         public void onCreate()
 *         {
 *             super.onCreate();
 *             Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(context, Thread.getDefaultUncaughtExceptionHandler()));
 *             ...
 *         }
 *     }
 *
 *     public static class MyExceptionHandler extends ExceptionHandler
 *     {
 *         protected ExceptionNotification getNotification()
 *         {
 *             return new ExceptionNotification1()
 *             {
 *                 protected String getAppName(Context context) {
 *                     return context.getString(R.string.app_name);
 *                 }
 **                 protected String getChannelID() {
 *                     return "suntimes.notifications.misc";
 *                 }
 *                 protected int getNotificationID() {
 *                     return -1000;
 *                 }
 *             };
 *         }
 *         protected String getAppVersionInfo() {
 *             return "Suntimes " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")" + (BuildConfig.DEBUG ? " [debug] " : " ") + "[" + BuildConfig.GIT_HASH + "]";
 *         }
 *         protected String getAppSupportURL(Context context) {
 *             return context.getString(R.string.app_support_url);
 *         }
 *         protected String getAppName(Context context) {
 *             return context.getString(R.string.app_name);
 *         }
 *     }
 * @see ExceptionActivity
 * @see ExceptionNotification
 * @see ExceptionNotification1
 */
public abstract class ExceptionHandler implements Thread.UncaughtExceptionHandler
{
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final WeakReference<Context> contextRef;

    public ExceptionHandler(Context context, Thread.UncaughtExceptionHandler defaultHandler)
    {
        this.contextRef = new WeakReference<>(context);
        this.defaultHandler = defaultHandler;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e)
    {
        try {
            Log.e("CRASH", e.getClass().getSimpleName(), e);
            Context context = contextRef.get();
            if (context != null)
            {
                NotificationManagerCompat notifications = NotificationManagerCompat.from(context);
                if (notifications != null && notifications.areNotificationsEnabled()) {
                    showCrashReportNotification(context, e);
                } else {
                    launchCrashReportActivity(context, e);
                }
            }

        } finally {
            defaultHandler.uncaughtException(t, e);
        }
    }

    protected abstract ExceptionNotification getNotification();
    protected abstract String getAppName(Context context);
    protected abstract String getAppVersionInfo();
    protected abstract String getAppSupportURL(Context context);

    protected String getAndroidVersionInfo() {
        return "Android " + Build.VERSION.RELEASE +  " (" + Build.VERSION.SDK_INT + ")" + " [" + Build.MANUFACTURER + "]";
    }

    protected String createCrashReport(Throwable e) {
        return getAppVersionInfo() + "\n"
                + getAndroidVersionInfo() + "\n\n"
                + Log.getStackTraceString(e);
    }

    private void showCrashReportNotification(Context context, Throwable e)
    {
        if (context != null)
        {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (notificationManager.areNotificationsEnabled())
            {
                ExceptionNotification builder = getNotification();
                builder.setNotificationMessage(e.toString());
                Notification notification = builder.createNotification(context, createCrashReport(e), getAppSupportURL(context));
                notificationManager.notify("CrashReport", builder.getNotificationID(), notification);
            }
        }
    }

    private void launchCrashReportActivity(Context context, Throwable e)
    {
        if (context != null) {
            context.startActivity(getNotification().getCrashReportActivityIntent(context, createCrashReport(e), getAppSupportURL(context)));
        } else {
            Log.e("CrashReport", "launchCrashReportActivity: null context!");
        }
    }
}
