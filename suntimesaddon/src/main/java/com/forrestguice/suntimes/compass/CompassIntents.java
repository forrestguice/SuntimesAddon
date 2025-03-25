package com.forrestguice.suntimes.compass;

import android.content.Intent;

/**
 * These are proposed Intent definitions for interactions between different compass apps.
 *
 * Example: An app that shows directional information (such as a heading or bearing) may want
 * to delegate those values to some other app (i.e. a dedicated compass).
 *
 * The app creates an implicit Intent with `ACTION_SET_BEARING`, provides a degree value
 * with `EXTRA_DEGREES`, and then calls `startActivity` to launch a supporting app.
 * ```
 *     double degrees = 243.5;
 *     Intent intent = new Intent("compass.intent.action.SET_BEARING");
 *     intent.putExtra("compass.intent.extra.DEGREES", degrees);
 *     intent.putExtra("compass.intent.extra.TRUE_NORTH", true);
 *
 *     try {
 *         startActivity(intent);
 *     } catch (ActivityNotFoundException e) {
 *         Toast.makeText(context, "compass app not found!", Toast.LENGTH_SHORT).show();
 *     }
 * ```
 *
 * Compass apps that wish to support this should define an `exported` activity with a
 * matching `intent-filter`:
 * ```
 *     <activity
 *         ...
 *         android:exported="true">
 *
 *         <intent-filter>
 *             <action android:name="compass.intent.action.SET_BEARING" />
 *             <category android:name="android.intent.category.DEFAULT" />
 *         </intent-filter>
 *     </activity>
 * ```
 *
 * Supporting compass apps can then handle those actions from `onCreate`:
 * ```
 *     public static final String ACTION_SET_BEARING = "compass.intent.action.SET_BEARING";
 *     public static final String EXTRA_DEGREES = "compass.intent.extra.DEGREES";
 *
 *     public void onCreate(Bundle savedState)
 *     {
 *         ...
 *         Intent intent = getIntent();
 *         String action = intent.getAction();
 *         if (ACTION_SET_BEARING.equals(action))
 *         {
 *             double degrees = intent.getDoubleExtra(EXTRA_DEGREES, 0);
 *             boolean isTrueNorth = intent.getBooleanExtra(EXTRA_TRUE_NORTH, false);
 *             ...
 *         }
 *     }
 * ```
 */

@SuppressWarnings("unused")
public class CompassIntents
{
    /**
     * Set or view an absolute bearing; used with `startActivity`, `EXTRA_DEGREES` and `EXTRA_TRUE_NORTH`.
     */
    public static final String ACTION_SET_BEARING = "compass.intent.action.SET_BEARING";

    /**
     * Pick an absolute bearing; used with `startActivityForResult` and `EXTRA_TRUE_NORTH`.
     * The user selected value will be returned using `EXTRA_DEGREES`.
     */
    public static final String ACTION_PICK_BEARING = "compass.intent.action.PICK_BEARING";

    /**
     * Used as a double field in compass intents to indicate the bearing in degrees.
     */
    public static final String EXTRA_DEGREES = "compass.intent.extra.DEGREES";

    /**
     * Used as a boolean field in compass intents to indicate if the bearing is relative to
     * true north. The default is false (bearing is relative to magnetic north).
     */
    public static final String EXTRA_TRUE_NORTH = "compass.intent.extra.TRUE_NORTH";

    /**
     * Creates an intent that sets the compass's absolute bearing.
     *
     * @param degrees a degree value
     * @param trueNorth true degree value is relative to true north; false degrees are relative to magnetic north
     * @return an Intent that can be started with `startActivity`
     */
    public static Intent compassBearingIntent(double degrees, boolean trueNorth)
    {
        Intent intent = new Intent(ACTION_SET_BEARING);
        intent.putExtra(EXTRA_TRUE_NORTH, trueNorth);
        intent.putExtra(EXTRA_DEGREES, degrees);
        return intent;
    }
    public static Intent compassBearingIntent(double degrees) {
        return compassBearingIntent(degrees, false);
    }
}
