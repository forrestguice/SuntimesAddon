/*
    Copyright (C) 2020 Forrest Guice
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

package com.forrestguice.suntimes.addon.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import com.forrestguice.suntimes.ContextCompat;
import com.forrestguice.suntimes.addon.R;
import com.forrestguice.suntimes.annotation.Nullable;

import java.lang.reflect.Method;

import androidx.appcompat.view.menu.MenuBuilder;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class MessagesBase
{
    @SuppressLint("ResourceType")
    public static Integer[] getSnackbarColors(Context context, @Nullable Integer[] colorOverrides)
    {
        Integer[] colors = new Integer[] {null, null, null};
        int[] colorAttrs = { R.attr.snackbar_textColor, R.attr.snackbar_accentColor, R.attr.snackbar_backgroundColor };
        TypedArray a = context.obtainStyledAttributes(colorAttrs);
        colors[0] = ContextCompat.getColor(context, a.getResourceId(0, android.R.color.primary_text_dark));
        colors[1] = ContextCompat.getColor(context, a.getResourceId(1, R.color.colorAccent_dark));
        colors[2] = ContextCompat.getColor(context, a.getResourceId(2, R.color.card_dark));
        a.recycle();

        if (colorOverrides != null && colorOverrides.length == colors.length) {
            for (int i=0; i<colors.length; i++) {
                if (colorOverrides[i] != null) {
                    colors[i] = colorOverrides[i];
                }
            }
        }
        return colors;
    }

    /**
     * from http://stackoverflow.com/questions/18374183/how-to-show-icons-in-overflow-menu-in-actionbar
     */
    @SuppressLint("RestrictedApi")
    public static void forceActionBarIcons(Menu menu)
    {
        if (menu != null)
        {
            //if (menu.getClass().getSimpleName().equals("MenuBuilder"))
            if (menu instanceof MenuBuilder)
            {
                try {
                    ((MenuBuilder) menu).setOptionalIconsVisible(true);

                    //Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    //m.setAccessible(true);
                    //m.invoke(menu, true);

                } catch (Exception e) {
                    Log.e("forceActionBarIcons", "failed to set show overflow icons", e);
                }
            }
        }
    }

    /**
     * @param view the View to trigger the accessibility event
     * @param msg text that will be read aloud (if accessibility enabled)
     */
    public static void announceForAccessibility(View view, String msg)
    {
        if (view != null && msg != null)
        {
            if (Build.VERSION.SDK_INT >= 16)
            {
                view.announceForAccessibility(msg);

            } else {
                Context context = view.getContext();
                if (context != null)
                {
                    AccessibilityManager accesibility = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
                    if (accesibility != null && accesibility.isEnabled())
                    {
                        AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_FOCUSED);
                        event.getText().add(msg);
                        event.setEnabled(view.isEnabled());
                        event.setClassName(view.getClass().getName());
                        event.setPackageName(context.getPackageName());

                        ViewParent parent = view.getParent();
                        if (Build.VERSION.SDK_INT >= 14 && parent != null)
                        {
                            parent.requestSendAccessibilityEvent(view, event);

                        } else {
                            accesibility.sendAccessibilityEvent(event);
                        }
                    }
                }
            }
        }
    }
}

