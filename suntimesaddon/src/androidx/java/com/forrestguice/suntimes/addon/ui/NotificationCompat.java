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

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;

import androidx.annotation.NonNull;

public class NotificationCompat
{
    public static final int VISIBILITY_PUBLIC = androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC;
    public static final int PRIORITY_HIGH = androidx.core.app.NotificationCompat.PRIORITY_HIGH;
    public static final String CATEGORY_ERROR = androidx.core.app.NotificationCompat.CATEGORY_ERROR;

    public static class Builder extends androidx.core.app.NotificationCompat.Builder
    {
        @TargetApi(19)
        public Builder(@NonNull Context context, @NonNull Notification notification) {
            super(context, notification);
        }

        public Builder(@NonNull Context context, @NonNull String channelId) {
            super(context, channelId);
        }

        public Builder(@NonNull Context context) {
            super(context);
        }
    }
}
