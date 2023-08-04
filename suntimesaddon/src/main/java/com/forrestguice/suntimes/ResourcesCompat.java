package com.forrestguice.suntimes;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.forrestguice.suntimes.annotation.NonNull;
import com.forrestguice.suntimes.annotation.Nullable;

/**
 * methods copied from: core/content/res/ResourcesCompat.java
 *
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 */
public class ResourcesCompat
{
    @Nullable
    @SuppressWarnings("deprecation")
    public static Drawable getDrawable(@NonNull Resources res, int id,
                                       @Nullable Resources.Theme theme) throws Resources.NotFoundException
    {
        if (Build.VERSION.SDK_INT >= 21)
        {
            return res.getDrawable(id, theme);

        } else {
            return res.getDrawable(id);
        }
    }
}
