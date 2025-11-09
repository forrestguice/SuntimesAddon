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
package com.forrestguice.suntimes.addon.ui;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.os.Build;

public class NotificationManagerCompat
{
    protected androidx.core.app.NotificationManagerCompat notifications;
    protected NotificationManagerCompat(Context context) {
        notifications = androidx.core.app.NotificationManagerCompat.from(context);
    }

    public static NotificationManagerCompat from(Context context) {
        return new NotificationManagerCompat(context);
    }

    public boolean areNotificationsEnabled() {
        return (notifications != null && notifications.areNotificationsEnabled());
    }

    public static boolean areNotificationsPaused(Context context)
    {
        if (Build.VERSION.SDK_INT >= 29) {
            return NotificationManagerCompat_api29.areNotificationsPaused(context);
        } else return false;
    }

    @SuppressLint("MissingPermission")
    public void notify(String message, int notificationID, Notification notification) {
        if (notifications != null) {
            notifications.notify(message, notificationID, notification);
        }
    }
}
