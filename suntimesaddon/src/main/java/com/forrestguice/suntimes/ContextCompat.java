package com.forrestguice.suntimes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;

import com.forrestguice.suntimes.annotation.NonNull;
import com.forrestguice.suntimes.annotation.Nullable;

/**
 * methods copied from: core/core/src/main/java/androidx/core/content/ContextCompat.java
 *
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 */

@SuppressLint("UseCompatLoadingForDrawables")
public class ContextCompat
{
    @SuppressWarnings("deprecation")
    public static int getColor(@NonNull Context context, int id)
    {
        if (Build.VERSION.SDK_INT >= 23) {
            return context.getColor(id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable
    public static Drawable getDrawable(@NonNull Context context, int id)
    {
        if (Build.VERSION.SDK_INT >= 21)
        {
            return context.getDrawable(id);

        } else if (Build.VERSION.SDK_INT >= 16) {
            return context.getResources().getDrawable(id);

        } else {
            // Prior to JELLY_BEAN, Resources.getDrawable() would not correctly
            // retrieve the final configuration density when the resource ID
            // is a reference another Drawable resource. As a workaround, try
            // to resolve the drawable reference manually.
            final int resolvedId;
            synchronized (sLock) {
                if (sTempValue == null) {
                    sTempValue = new TypedValue();
                }
                context.getResources().getValue(id, sTempValue, true);
                resolvedId = sTempValue.resourceId;
            }
            return context.getResources().getDrawable(resolvedId);
        }
    }

    private static TypedValue sTempValue;
    private static final Object sLock = new Object();
}
