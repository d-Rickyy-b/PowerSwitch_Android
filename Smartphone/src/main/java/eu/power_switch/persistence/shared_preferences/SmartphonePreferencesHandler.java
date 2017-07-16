/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.persistence.shared_preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.annotation.CheckResult;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import eu.power_switch.R;
import eu.power_switch.backup.BackupHandler;
import eu.power_switch.persistence.demo_mode.DemoModePersistanceHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import lombok.Getter;
import timber.log.Timber;

/**
 * Preference handler used to store general app settings
 */
@Singleton
public class SmartphonePreferencesHandler {

    @Getter
    public enum PreferenceValue {

        // TODO: use enum instead of string constants

        ShowRoomAllOnOff(KEY_SHOW_ROOM_ALL_ON_OFF, DEFAULT_VALUE_SHOW_ROOM_ALL_ON_OFF);

        private final String key;
        private final Class  type;
        private final Object defaultValue;

        <T> PreferenceValue(String key, T defaultValue) {
            this.key = key;
            this.type = defaultValue.getClass();
            this.defaultValue = defaultValue;
        }

    }

    // default values
    public static final boolean DEFAULT_VALUE_SHOW_ROOM_ALL_ON_OFF                 = true;
    public static final boolean DEFAULT_VALUE_HIGHLIGHT_LAST_ACTIVATED_BUTTON      = false;
    public static final boolean DEFAULT_VALUE_AUTO_COLLAPSE_ROOMS                  = false;
    public static final int     DEFAULT_VALUE_THEME                                = SettingsConstants.THEME_DARK_BLUE;
    public static final boolean DEFAULT_VALUE_VIBRATE_ON_BUTTON_PRESS              = true;
    public static final int     DEFAULT_VALUE_VIBRATION_DURATION                   = SettingsConstants.DEFAULT_VIBRATION_DURATION_HAPTIC_FEEDBACK;
    public static final int     DEFAULT_VALUE_STARTUP_TAB                          = SettingsConstants.ROOMS_TAB_INDEX;
    public static final boolean DEFAULT_VALUE_STOCK_ALARM_CLOCK_ENABLED            = true;
    public static final boolean DEFAULT_VALUE_SLEEP_AS_ANDROID_ENABLED             = true;
    public static final int     DEFAULT_VALUE_KEEP_HISTORY_DURATION                = SettingsConstants.KEEP_HISTORY_FOREVER;
    public static final long    DEFAULT_VALUE_CURRENT_APARTMENT_ID                 = SettingsConstants.INVALID_APARTMENT_ID;
    public static final boolean DEFAULT_VALUE_USE_COMPACT_DRAWER                   = false;
    public static final boolean DEFAULT_VALUE_AUTO_COLLAPSE_TIMERS                 = false;
    public static final boolean DEFAULT_VALUE_USE_OPTIONS_MENU_INSTEAD_OF_FAB      = false;
    public static final String  DEFAULT_VALUE_BACKUP_PATH                          = Environment.getExternalStorageDirectory()
            .getPath() + File.separator + BackupHandler.MAIN_BACKUP_FOLDERNAME;
    public static final boolean DEFAULT_VALUE_AUTO_DISCOVER                        = true;
    public static final boolean DEFAULT_VALUE_SHOW_TOAST_IN_BACKGROUND             = true;
    public static final boolean DEFAULT_VALUE_SEND_ANONYMOUS_CRASH_DATA            = true;
    public static final int     DEFAULT_VALUE_LOG_DESTINATION                      = 0;
    public static final boolean DEFAULT_VALUE_SHOULD_ASK_SEND_ANONYMOUS_CRASH_DATA = true;
    public static final boolean DEFAULT_VALUE_SHOW_GEOFENCE_NOTIFICATIONS          = true;
    public static final boolean DEFAULT_VALUE_SHOW_TIMER_NOTIFICATIONS             = true;
    public static final boolean DEFAULT_VALUE_SHOULD_SHOW_WIZARD                   = true;
    public static final int     DEFAULT_VALUE_LAUNCHER_ICON                        = 0;

    public static String KEY_AUTO_DISCOVER;
    public static String KEY_BACKUP_PATH;
    public static String KEY_STARTUP_DEFAULT_TAB;
    public static String KEY_SHOW_ROOM_ALL_ON_OFF;
    public static String KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON;
    public static String KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB;
    public static String KEY_AUTO_COLLAPSE_ROOMS;
    public static String KEY_AUTO_COLLAPSE_TIMERS;
    public static String KEY_THEME;
    public static String KEY_USE_COMPACT_DRAWER;
    public static String KEY_VIBRATE_ON_BUTTON_PRESS;
    public static String KEY_VIBRATION_DURATION;
    public static String KEY_CURRENT_APARTMENT_ID;
    public static String KEY_KEEP_HISTORY_DURATION;
    public static String KEY_SLEEP_AS_ANDROID_ENABLED;
    public static String KEY_STOCK_ALARM_CLOCK_ENABLED;
    public static String KEY_SHOW_TOAST_IN_BACKGROUND;
    public static String KEY_SEND_ANONYMOUS_CRASH_DATA;
    public static String KEY_SHOULD_ASK_SEND_ANONYMOUS_CRASH_DATA;
    public static String KEY_LOG_DESTINATION;
    public static String KEY_SHOW_GEOFENCE_NOTIFICATIONS;
    public static String KEY_SHOW_TIMER_NOTIFICATIONS;
    public static String KEY_SHOULD_SHOW_WIZARD;
    public static String KEY_LAUNCHER_ICON;


    // setting keys
    private SharedPreferences sharedPreferences;
    private Map<String, ?>    cachedValues;

    // default values for each settings key
    private Map<String, Object> defaultValueMap;

    private Context context;

    @Inject
    public SmartphonePreferencesHandler(Context context) {
        this.context = context;

        sharedPreferences = context.getSharedPreferences(SettingsConstants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        forceRefresh();

        initializePublicKeys(context);
        initializeDefaultValueMap();

        for (String key : cachedValues.keySet()) {
            Timber.d(key + ": " + get(key));
        }
    }

    private void initializePublicKeys(Context context) {
        KEY_AUTO_DISCOVER = context.getString(R.string.key_autodiscover);
        KEY_BACKUP_PATH = context.getString(R.string.key_backupPath);
        KEY_STARTUP_DEFAULT_TAB = context.getString(R.string.key_startupDefaultTab);
        KEY_SHOW_ROOM_ALL_ON_OFF = context.getString(R.string.key_showRoomAllOnOff);
        KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON = context.getString(R.string.key_highlightLastActivatedButton);
        KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB = context.getString(R.string.key_useOptionsMenuInsteadOfFab);
        KEY_AUTO_COLLAPSE_ROOMS = context.getString(R.string.key_autoCollapseRooms);
        KEY_AUTO_COLLAPSE_TIMERS = context.getString(R.string.key_autoCollapseTimers);
        KEY_THEME = context.getString(R.string.key_theme);
        KEY_USE_COMPACT_DRAWER = context.getString(R.string.key_useCompactDrawer);
        KEY_VIBRATE_ON_BUTTON_PRESS = context.getString(R.string.key_vibrateOnButtonPress);
        KEY_VIBRATION_DURATION = context.getString(R.string.key_vibrationDuration);
        KEY_CURRENT_APARTMENT_ID = "currentApartmentId";
        KEY_KEEP_HISTORY_DURATION = context.getString(R.string.key_keepHistoryDuration);
        KEY_SLEEP_AS_ANDROID_ENABLED = "SLEEP_AS_ANDROID_ENABLED_KEY";
        KEY_STOCK_ALARM_CLOCK_ENABLED = "STOCK_ALARM_CLOCK_ENABLED_KEY";
        KEY_SHOW_TOAST_IN_BACKGROUND = context.getString(R.string.key_showBackgroundActionToast);
        KEY_SEND_ANONYMOUS_CRASH_DATA = context.getString(R.string.key_sendAnonymousCrashData);
        KEY_SHOULD_ASK_SEND_ANONYMOUS_CRASH_DATA = "shouldAskSendAnonymousCrashData";
        KEY_LOG_DESTINATION = context.getString(R.string.key_logDestination);
        KEY_SHOW_GEOFENCE_NOTIFICATIONS = context.getString(R.string.key_showGeofenceNotifications);
        KEY_SHOW_TIMER_NOTIFICATIONS = context.getString(R.string.key_showTimerNotifications);
        KEY_SHOULD_SHOW_WIZARD = context.getString(R.string.key_shouldShowWizard);
        KEY_LAUNCHER_ICON = context.getString(R.string.key_launcher_icon);
    }

    private void initializeDefaultValueMap() {
        defaultValueMap = new HashMap<>();
        defaultValueMap.put(KEY_SHOW_ROOM_ALL_ON_OFF, DEFAULT_VALUE_SHOW_ROOM_ALL_ON_OFF);
        defaultValueMap.put(KEY_HIGHLIGHT_LAST_ACTIVATED_BUTTON, DEFAULT_VALUE_HIGHLIGHT_LAST_ACTIVATED_BUTTON);
        defaultValueMap.put(KEY_AUTO_COLLAPSE_ROOMS, DEFAULT_VALUE_AUTO_COLLAPSE_ROOMS);
        defaultValueMap.put(KEY_THEME, DEFAULT_VALUE_THEME);
        defaultValueMap.put(KEY_VIBRATE_ON_BUTTON_PRESS, DEFAULT_VALUE_VIBRATE_ON_BUTTON_PRESS);
        defaultValueMap.put(KEY_VIBRATION_DURATION, DEFAULT_VALUE_VIBRATION_DURATION);
        defaultValueMap.put(KEY_STARTUP_DEFAULT_TAB, DEFAULT_VALUE_STARTUP_TAB);

        defaultValueMap.put(KEY_STOCK_ALARM_CLOCK_ENABLED, DEFAULT_VALUE_STOCK_ALARM_CLOCK_ENABLED);
        defaultValueMap.put(KEY_SLEEP_AS_ANDROID_ENABLED, DEFAULT_VALUE_SLEEP_AS_ANDROID_ENABLED);
        defaultValueMap.put(KEY_KEEP_HISTORY_DURATION, DEFAULT_VALUE_KEEP_HISTORY_DURATION);
        defaultValueMap.put(KEY_CURRENT_APARTMENT_ID, DEFAULT_VALUE_CURRENT_APARTMENT_ID);
        defaultValueMap.put(KEY_USE_COMPACT_DRAWER, DEFAULT_VALUE_USE_COMPACT_DRAWER);
        defaultValueMap.put(KEY_AUTO_COLLAPSE_TIMERS, DEFAULT_VALUE_AUTO_COLLAPSE_TIMERS);
        defaultValueMap.put(KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB, DEFAULT_VALUE_USE_OPTIONS_MENU_INSTEAD_OF_FAB);
        defaultValueMap.put(KEY_BACKUP_PATH, DEFAULT_VALUE_BACKUP_PATH);
        defaultValueMap.put(KEY_AUTO_DISCOVER, DEFAULT_VALUE_AUTO_DISCOVER);
        defaultValueMap.put(KEY_SHOW_TOAST_IN_BACKGROUND, DEFAULT_VALUE_SHOW_TOAST_IN_BACKGROUND);
        defaultValueMap.put(KEY_SEND_ANONYMOUS_CRASH_DATA, DEFAULT_VALUE_SEND_ANONYMOUS_CRASH_DATA);
        defaultValueMap.put(KEY_SHOULD_ASK_SEND_ANONYMOUS_CRASH_DATA, DEFAULT_VALUE_SHOULD_ASK_SEND_ANONYMOUS_CRASH_DATA);
        defaultValueMap.put(KEY_LOG_DESTINATION, DEFAULT_VALUE_LOG_DESTINATION);
        defaultValueMap.put(KEY_SHOW_GEOFENCE_NOTIFICATIONS, DEFAULT_VALUE_SHOW_GEOFENCE_NOTIFICATIONS);
        defaultValueMap.put(KEY_SHOW_TIMER_NOTIFICATIONS, DEFAULT_VALUE_SHOW_TIMER_NOTIFICATIONS);
        defaultValueMap.put(KEY_SHOULD_SHOW_WIZARD, DEFAULT_VALUE_SHOULD_SHOW_WIZARD);
        defaultValueMap.put(KEY_LAUNCHER_ICON, DEFAULT_VALUE_LAUNCHER_ICON);

        for (String key : defaultValueMap.keySet()) {
            // initialize missing default values
            get(key);
        }
    }

    /**
     * Forces an update of the cached values
     */
    public void forceRefresh() {
        cachedValues = sharedPreferences.getAll();
    }

    public String getPublicKeyString() {
        return SettingsConstants.KDH_SDSA + SettingsConstants.JKD_COAP + SettingsConstants.DJA_IOVJ + SettingsConstants.VOK_ZWEQ;
    }

    /**
     * Get a settings value by key
     * <p>
     * Note: Be sure to assign the return value of this method to variable with your expected return type.
     *
     * @param settingsKey Key of setting
     * @param <T>         expected type of return value
     *
     * @return settings value
     */
    @SuppressWarnings("unchecked")
    @CheckResult
    public <T extends Comparable<? super T>> T get(String settingsKey) throws ClassCastException {
        Timber.v("retrieving current value for key \"" + settingsKey + "\"");

        Object value = cachedValues.get(settingsKey);

        if (value == null) {
            if (!defaultValueMap.containsKey(settingsKey)) {
                Timber.w("Setting \"" + settingsKey + "\" has no associated default value");
            } else {
                // set default value
                set(settingsKey, getDefaultValue(settingsKey));
                forceRefresh();
            }
            return (T) getDefaultValue(settingsKey);
        } else {
            // special treatment for this key, to make playstore mode possible
            if (KEY_CURRENT_APARTMENT_ID.equals(settingsKey)) {
                if (!DeveloperPreferencesHandler.getPlayStoreMode()) {
                    return (T) value;
                } else {
                    DemoModePersistanceHandler demoModePersistanceHandler = new DemoModePersistanceHandler(context);
                    try {
                        return (T) demoModePersistanceHandler.getAllApartments()
                                .get(0)
                                .getId();
                    } catch (Exception e) {
                        throw new RuntimeException("Error fetching apartment id for demo mode");
                    }
                }
            }

            return (T) value;
        }
    }

    @Nullable
    private Object getDefaultValue(String settingsKey) {
        return defaultValueMap.get(settingsKey);
    }

    /**
     * Set a settings value by key
     *
     * @param settingsKey Key of setting
     * @param newValue    new value
     */
    public void set(String settingsKey, Object newValue) {
        Timber.d("setting new value \"" + newValue + "\" for key \"" + settingsKey + "\"");

        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (newValue instanceof Boolean) {
            editor.putBoolean(settingsKey, (Boolean) newValue);
        } else if (newValue instanceof String) {
            editor.putString(settingsKey, (String) newValue);
        } else if (newValue instanceof Integer) {
            editor.putInt(settingsKey, (Integer) newValue);
        } else if (newValue instanceof Float) {
            editor.putFloat(settingsKey, (Float) newValue);
        } else if (newValue instanceof Long) {
            editor.putLong(settingsKey, (Long) newValue);
        } else {
            throw new IllegalArgumentException("Cant save objects of type " + newValue.getClass());
        }

        editor.apply();

        forceRefresh();
    }

}