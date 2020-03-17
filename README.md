# Suntimes Addon
An Android Library that assists the development of [Suntimes](https://github.com/forrestguice/SuntimesWidget/) add-ons.  

This library is used by the [Solunar Periods](https://github.com/forrestguice/SolunarPeriods) add-on.

### To use:
* Create a new Android project; `File -> New Project`
* Add Jitpack to the root `build.gradle`.
```
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
``` 

* Add the `SuntimesAddon` library to dependencies.
```
dependencies {
    ...
    implementation 'com.github.forrestguice:SuntimesAddon:v0.1.0'
}
``` 

* (optional) Override `values/suntimes.xml` to define your project's Suntimes version requirements. The default looks like:
``` 
<resources>
    <integer name="min_provider_version">1</integer>
    <string name="min_suntimes_version">Suntimes v0.10.3</string>
    <string name="min_suntimes_url">https://forrestguice.github.io/SuntimesWidget/</string>
</resources>
``` 

* Override `attachBaseContext` to obtain Suntimes info when your activity first starts. Optionally override the locale to match the one used by Suntimes.
```
private SuntimesInfo suntimesInfo = null;

    @Override
    protected void attachBaseContext(Context context)
    {
        suntimesInfo = SuntimesInfo.queryInfo(context.getContentResolver());    // obtain Suntimes version info
        super.attachBaseContext( (suntimesInfo != null && suntimesInfo.appLocale != null) ?    // override the locale
                LocaleHelper.loadLocale(context, suntimesInfo.appLocale) : context );
    }
```

* In `onCreate` check Suntimes version info and display a message if its missing. Optionally override the theme to match the one used by Suntimes. 
```
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        if (suntimesInfo.appTheme != null) {    // override the theme
            setTheme(suntimesInfo.appTheme.equals(SuntimesInfo.THEME_LIGHT) ? com.forrestguice.suntimes.addon.R.style.AppTheme_Light : com.forrestguice.suntimes.addon.R.style.AppTheme_Dark);
        }
        setContentView(R.layout.activity_main);      
        ...
        
        if (!SuntimesInfo.checkVersion(this, suntimesInfo))
        {
            View view = getWindow().getDecorView().findViewById(android.R.id.content);
            if (!suntimesInfo.hasPermission)
                Messages.showPermissionDeniedMessage(this, view);
            else Messages.showMissingDependencyMessage(this, view);
        }       
    }    
```
* Use the Suntimes ContentProvider to query information.
```
    String[] projection = new String[] { CalculatorProviderContract.COLUMN_MOON_FULL };
    Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOONPHASE + "/" + startDate.getTimeInMillis() + "-" + endDate.getTimeInMillis());

    ContentResolver resolver = context.getContentResolver();
    Cursor cursor = resolver.query(uri, projection, null, null, null);
    if (cursor != null
    {
        cursor.moveToFirst();                     // Expect at least one row, but a cursor will contain multiple rows if a timestamp-range is used.
        while (!cursor.isAfterLast())
        {
            Long fullMoonTimeMillis = cursor.getLong(cursor.getColumnIndex(CalculatorProviderContract.COLUMN_MOON_FULL));       
            if (fullMoonTimeMillis != null) {             // might be null if column is not part of or is missing from the projection
                Calendar fullMoonCalendar = Calendar.getInstance();
                fullMoonCalendar.setTimeInMillis(fullMoonTimeMillis);
                // ... do something with fullMoonCalendar
            }
            cursor.moveToNext();
        }
        cursor.close();  // don't forget to close the cursor when finished
    }
```

### Legal Stuff

Copyright © 2020 Forrest Guice

Source code is available under GPLv3 (https://github.com/forrestguice/SuntimesAddon).

> This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

**Using this library means your project must also be licensed GPLv3.** 

This library is intended to assist add-on development but its use is not required. You should call the Suntimes public interfaces directly if your project is licensed differently.      