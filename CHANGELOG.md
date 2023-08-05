~

### v0.4.0 (2023-08-04)
* adds support for "text size" setting and high contrast themes.
* adds AppThemeInfo; add-ons may extend this class to more easily support Suntimes themes.
* adds tooltip support for older Android versions.
* fixes bug where toasts are unreadable on api33+.
* updates CalculatorProviderContract (v0.5.0).
* updates AddonHelper; legacy settings fragment identifiers (changed in Suntimes v0.15.2).
* updates AlarmHelper; support for add-on dismiss challenge.
* updates CalendarHelper; support for templates (adds contract and patterns).
* updates build; minSdkVersion 11->14, targetSdkVersion 25->28, support libs updated to 28.0.0.
* other changes in preparation for future migration to AndroidX libraries; annoations, ContextCompat, ResourcesCompat.

### v0.3.0 (2022-02-04)
* adds AlarmHelper; add-ons may define custom alarm events.
* adds ActionsHelper; add-ons can access user-defined actions.
* adds SuntimesUtils helper (copied from Suntimes).
* updates TimeZoneHelper; sidereal time.
* adds strings for events, cardinal directions, timezones, formatting, ...
* adds resources for displaying events, 'chip', and 'chip tray' ui.

### v0.2.0 (2020-12-31)
* adds CalendarHelper; convenience methods for interacting with SuntimesCalendars public interfaces.
* adds ThemeHelper; convenience methods for retrieving Suntimes themes.
* adds TimeZoneHelper; convenience methods for matching Suntimes configuration (support for solar time).
* adds WidgetListHelper; convenience methods for listing and reconfiguring add-on widgets.
* extends AddonHelper; additional convenience methods for starting Suntimes activities; fixes bug in startWidgetListActivity;
* updates to colors, icons, strings, and other resources.

### v0.1.0 (2020-03-16) [First Release]
* a copy of the Suntimes CalculatorProviderContract (v0.4.0).
* Suntimes app resources (common drawables, colors, and styles).
* convenience methods for verifying the Suntimes dependency (and standard messages to be displayed if missing).
* convenience methods for initializing an app's theme and locale (to match Suntimes).
* convenience methods for retrieving the current Suntimes calculator configuration and user interface options.
* convenience methods for starting public Suntimes activities (or re-configuring widgets).