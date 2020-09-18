/**
 Copyright (C) 2014-2020 Forrest Guice
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

package com.forrestguice.suntimes.addon;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.forrestguice.suntimes.calculator.core.CalculatorProviderContract;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class TimeZoneHelper
{
    public static class LocalMeanTime extends TimeZone
    {
        public static final String TIMEZONEID = "Local Mean Time";

        private int rawOffset = 0;

        public LocalMeanTime(double longitude, String name)
        {
            super();
            setID(name);
            setRawOffset(findOffset(longitude));
        }

        /**
         * @param longitude a longitude value; degrees [-180, 180]
         * @return the offset of this longitude from utc (in milliseconds)
         */
        public int findOffset( double longitude )
        {
            double offsetHrs = longitude * 24 / 360;           // offset from gmt in hrs
            //noinspection UnnecessaryLocalVariable
            int offsetMs = (int)(offsetHrs * 60 * 60 * 1000);  // hrs * 60min in a day * 60s in a min * 1000ms in a second
            //Log.d("DEBUG", "offset: " + offsetHrs + " (" + offsetMs + ")");
            return offsetMs;
        }

        @Override
        public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds)
        {
            return getRawOffset();
        }

        @Override
        public int getOffset( long date )
        {
            return getRawOffset();
        }

        @Override
        public int getRawOffset()
        {
            return rawOffset;
        }

        @Override
        public void setRawOffset(int offset)
        {
            rawOffset = offset;
        }

        @Override
        public boolean inDaylightTime(Date date)
        {
            return false;
        }

        @Override
        public boolean useDaylightTime()
        {
            return false;
        }

        @Override
        public String toString()
        {
            return "id: " + getID() + ", offset: " + getRawOffset() + ", useDaylight: " + useDaylightTime();
        }
    }

    /**
     * ApparentSolarTime : TimeZone
     */
    public static class ApparentSolarTime extends LocalMeanTime
    {
        public static final String TIMEZONEID = "Apparent Solar Time";

        public ApparentSolarTime(double longitude, String name, ContentResolver resolver)
        {
            super(longitude, name);
            this.resolver = resolver;
        }

        @Override
        public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds)
        {
            Calendar calendar = new GregorianCalendar();
            calendar.set(year, month, day);
            return getOffset(calendar.getTimeInMillis());
        }

        /**
         * @param date a given date
         * @return ms offset with "equation of time" correction applied for the given date
         */
        @Override
        public int getOffset( long date )
        {
            eotOffset = lookupEquationOfTimeOffset(resolver, date);
            return getRawOffset() + eotOffset;
        }

        /**
         * @param resolver ContentResolver
         * @param date date millis
         * @return eot in seconds
         */
        public static int lookupEquationOfTimeOffset(ContentResolver resolver, long date)
        {
            int eot;
            if (resolver != null)
            {
                Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SUNPOS);
                try {
                    Cursor cursor = resolver.query(uri, new String[]{CalculatorProviderContract.COLUMN_SUNPOS_EOT}, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        eot = (cursor.isNull(0) ? equationOfTimeOffset(date) : (int) cursor.getDouble(0) * 1000);
                        cursor.close();
                    } else {
                        eot = equationOfTimeOffset(date);
                    }
                } catch (SecurityException e) {
                    Log.e(ApparentSolarTime.class.getSimpleName(), "queryInfo: Unable to access " + CalculatorProviderContract.AUTHORITY + "! " + e);
                    eot = equationOfTimeOffset(date);
                }
            } else {
                Log.e(ApparentSolarTime.class.getSimpleName(), "queryInfo: ContentResolver is null!");
                eot = equationOfTimeOffset(date);
            }
            return eot;
        }


        /**
         * @param date a given date
         * @return equation of time correction in milliseconds
         */
        public static int equationOfTimeOffset(long date)
        {
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(date);
            return (int)(equationOfTimeOffset(calendar.get(Calendar.DAY_OF_YEAR)) * 60 * 1000);
        }

        /**
         * http://www.esrl.noaa.gov/gmd/grad/solcalc/solareqns.PDF
         * @param n day of year (n=1 is january 1)
         * @return equation of time correction in decimal minutes
         */
        public static double equationOfTimeOffset(int n)
        {
            while (n <= 0)    // n in range [1, 365]
            {
                n += 365;
            }
            while (n > 365)
            {
                n -= 365;
            }

            double d = (2 * Math.PI / 365.24) * (n - 1);   // fractional year (radians)
            return 229.18 * (0.000075
                    + 0.001868 * Math.cos(d)
                    - 0.032077 * Math.sin(d)
                    - 0.014615 * Math.cos(2*d)
                    - 0.040849 * Math.sin(2*d));   // .oO(a truly magical return statement)
        }

        @Override
        public boolean useDaylightTime()
        {
            return true;
        }
        public boolean observesDaylightTime()
        {
            return useDaylightTime();
        }

        @Override
        public boolean inDaylightTime(Date date)
        {
            return true;
        }

        @Override
        public int getDSTSavings() {
            return eotOffset;
        }
        private int eotOffset = 0;
        private ContentResolver resolver = null;
    }
}
