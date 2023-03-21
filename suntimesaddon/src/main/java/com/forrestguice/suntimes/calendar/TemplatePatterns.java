/*
    Copyright (C) 2023 Forrest Guice
    This file is part of SuntimesCalendars.

    SuntimesCalendars is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesCalendars is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesCalendars.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.forrestguice.suntimes.calendar;

import android.content.ContentValues;
import android.support.annotation.Nullable;

/**
 * TemplatePatterns
 */
public enum TemplatePatterns
{
    pattern_cal("%cal"),
    pattern_summary("%summary"),
    pattern_color("%color"),
    pattern_loc("%loc"),
    pattern_lat("%lat"),
    pattern_lon("%lon"),
    pattern_lel("%lel"),
    pattern_event("%M"),
    pattern_dist("%dist"),
    pattern_illum("%i"),
    pattern_percent("%%");

    private final String pattern;

    private TemplatePatterns(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public String toString() {
        return pattern;
    }

    public static ContentValues createContentValues(@Nullable ContentValues values, String calendarTitle)
    {
        if (values == null) {
            values = new ContentValues();
        }
        values.put(pattern_cal.getPattern(), calendarTitle);
        return values;
    }

    public static ContentValues createContentValues(@Nullable ContentValues values, String[] location)
    {
        if (values == null) {
            values = new ContentValues();
        }
        if (location != null && location.length > 0) {
            values.put(pattern_loc.getPattern(), location[0]);
            if (location.length > 1) {
                values.put(pattern_lat.getPattern(), location[1]);
                if (location.length > 2) {
                    values.put(pattern_lon.getPattern(), location[2]);
                    if (location.length > 3) {
                        values.put(pattern_lel.getPattern(), location[3]);
                    }
                }
            }
        }
        return values;
    }

    public static String replaceSubstitutions(@Nullable String pattern, ContentValues values)
    {
        String displayString = pattern;
        if (pattern != null)
        {
            //noinspection ForLoopReplaceableByForEach
            TemplatePatterns[] patterns = TemplatePatterns.values();
            for (int i=0; i<patterns.length; i++)
            {
                String p = patterns[i].getPattern();
                String v = values.getAsString(p);
                displayString = displayString.replaceAll(p, ((v != null) ? v : ""));
            }
        }
        return displayString;
    }

}
