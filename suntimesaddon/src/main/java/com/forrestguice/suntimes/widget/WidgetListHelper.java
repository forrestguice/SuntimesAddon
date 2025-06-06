/*
    Copyright (C) 2020 Forrest Guice
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

package com.forrestguice.suntimes.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.forrestguice.suntimes.ContextCompat;

import java.io.ByteArrayOutputStream;

/**
 * Addons can use this helper to implement a ContentProvider that supplies info about running widgets.
 * The main app's WidgetListActivity queries available providers to display a list of all widgets.
 *
 * Addon providers should respond to QUERY_WIDGET (i.e. content://<providerURI>/widgets) with a cursor
 * containing one or more rows of widget info (e.g. COLUMN_WIDGET_APPWIDGETID, COLUMN_WIDGET_CLASS, etc).
 *
 * Addons should declare the availability of their provider in the manifest using an intent-filter
 * and meta-data ("WidgetInfoProvider" containing the provider's URI).
 *
 *    <meta-data android:name="WidgetInfoProvider" android:value="content://provider.uri.goes.here" />
 *    <intent-filter>
 *        <action android:name="suntimes.action.LIST_WIDGETS" />
 *        <category android:name="suntimes.SUNTIMES_ADDON" />
  *    </intent-filter>
 *
 * To permit the main app to trigger reconfiguration, widget configuration activities should be exported (and protected
 * with suntimes permission).
 * e.g. <activity ... android:exported="true" android:permission="suntimes.permission.READ_CALCULATOR" />
 */
@SuppressWarnings("WeakerAccess")
public class WidgetListHelper
{
    public static final String ACTION_LIST_WIDGETS = "suntimes.action.LIST_WIDGETS";

    public static final String COLUMN_WIDGET_PACKAGENAME = "packagename";    // String
    public static final String COLUMN_WIDGET_APPWIDGETID = "appwidgetid";    // int
    public static final String COLUMN_WIDGET_CLASS = "widgetclass";          // String (fully qualified class name)
    public static final String COLUMN_WIDGET_CONFIGCLASS = "configclass";    // String (fully qualified class name)
    public static final String COLUMN_WIDGET_LABEL = "label";                // String
    public static final String COLUMN_WIDGET_SUMMARY = "summary";            // String
    public static final String COLUMN_WIDGET_ICON = "icon";                  // BloB (png byte[])

    public static final String QUERY_WIDGET = "widgets";
    public static final String[] QUERY_WIDGET_PROJECTION = new String[] {
            COLUMN_WIDGET_APPWIDGETID, COLUMN_WIDGET_CLASS, COLUMN_WIDGET_CONFIGCLASS, COLUMN_WIDGET_PACKAGENAME,
            COLUMN_WIDGET_LABEL, COLUMN_WIDGET_SUMMARY, COLUMN_WIDGET_ICON
    };

    /**
     * Creates a Cursor for return by ContentProvider (a suitable response to QUERY_WIDGET).
     * @param context Context
     * @param widgetClass array of Class (widget types)
     * @param summary array of summary strings (for each widgetClass)
     * @param iconResID array of iconResID (for each widgetClass)
     * @return a Cursor containing rows for each running widget (of the supplied widget type)
     */
    public static MatrixCursor createWidgetListCursor(Context context, Class<?>[] widgetClass, String[] summary, int[] iconResID) {
        return createWidgetListCursor(context, QUERY_WIDGET_PROJECTION, widgetClass, summary, iconResID);
    }
    public static MatrixCursor createWidgetListCursor(Context context, String[] columns, Class<?>[] widgetClass, String[] summary, int[] iconResID)
    {
        MatrixCursor cursor = new MatrixCursor(columns);
        if (context != null)
        {
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            for (int i=0; i<widgetClass.length; i++)
            {
                int[] widgetIDs = widgetManager.getAppWidgetIds(new ComponentName(context, widgetClass[i]));
                for (int appWidgetID : widgetIDs)
                {
                    Drawable icon = ContextCompat.getDrawable(context, iconResID[i]);
                    cursor.addRow(createWidgetListRow(context, widgetManager, columns, appWidgetID, widgetClass[i].getName(), summary[i], icon));
                }
            }
        }
        return cursor;
    }

    public static Object[] createWidgetListRow(Context context, AppWidgetManager widgetManager, String[] columns, int appWidgetID, String widgetClass, String summary, Drawable icon)
    {
        AppWidgetProviderInfo info = widgetManager.getAppWidgetInfo(appWidgetID);
        Object[] row = new Object[columns.length];
        for (int i=0; i<columns.length; i++)
        {
            switch (columns[i])
            {
                case COLUMN_WIDGET_PACKAGENAME:
                    row[i] = context.getPackageName();
                    break;

                case COLUMN_WIDGET_APPWIDGETID:
                    row[i] = appWidgetID;
                    break;

                case COLUMN_WIDGET_CLASS:
                    row[i] = widgetClass;
                    break;

                case COLUMN_WIDGET_CONFIGCLASS:
                    row[i] = (info.configure != null ? info.configure.getClassName() : null);
                    break;

                case COLUMN_WIDGET_LABEL:
                    row[i] = info.label;
                    break;

                case COLUMN_WIDGET_SUMMARY:
                    row[i] = summary;
                    break;

                case COLUMN_WIDGET_ICON:
                    row[i] = drawableToBitmapArray(icon);
                    break;

                default:
                    row[i] = null;
                    break;
            }
        }
        return row;
    }

    /**
     * @param drawable drawable
     * @return drawable as Blob (byte[])
     */
    public static byte[] drawableToBitmapArray(Drawable drawable) {
        Bitmap bitmap = drawableToBitmap(drawable);
        byte[] retValue = bitmapToBitmapArray(bitmap);
        bitmap.recycle();
        return retValue;
    }

    /**
     * @param drawable drawable
     * @return drawable as Bitmap (remember to call .recycle() when finished with it)
     */
    public static Bitmap drawableToBitmap(Drawable drawable)
    {
        if (drawable != null)
        {
            int w = drawable.getIntrinsicWidth();
            int h = drawable.getIntrinsicHeight();
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, w, h);
            drawable.draw(canvas);
            return bitmap;
        } else return null;
    }

    /**
     * @param bitmap Bitmap
     * @return bitmap as Blob (byte[])
     */
    public static byte[] bitmapToBitmapArray(Bitmap bitmap)
    {
        if (bitmap != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return out.toByteArray();
        } else return null;
    }
}
