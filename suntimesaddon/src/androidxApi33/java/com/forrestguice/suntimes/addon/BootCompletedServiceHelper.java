// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2025 Forrest Guice
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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;

public class BootCompletedServiceHelper
{
    public static void registerReceiver(Service service, BroadcastReceiver receiver, IntentFilter intentFilter)
    {
        if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.registerReceiver(service, receiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
        } else {
            if (Build.VERSION.SDK_INT >= 26) {
                service.registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                service.registerReceiver(receiver, intentFilter);
            }
        }
    }

    public static void onStartCommand(Service service, Intent intent, int flags, int startId, int notificationID, NotificationCompat.Builder notification)
    {
        if (Build.VERSION.SDK_INT >= 29) {
            service.startForeground(notification, notification.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE);   // we are obligated to startForeground within 5s
        }
    }

    public static void setSilent(NotificationCompat.Builder notification) {
        notification.setSilent(true);
    }
}
