/*
 * Copyright (C) 2010-2015 The MPDroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.namelessdev.mpdroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WifiConnectionSettings extends PreferenceActivity {

    private static final String KEY_ROOM_BASED_CATEGORY = "roomBasedCategory";

    private static final String KEY_ROOM_BASED_SCREEN = "roomBasedScreen";

    private static final String KEY_WIFI_BASED_CATEGORY = "wifibasedCategory";

    private static final String KEY_WIFI_BASED_SCREEN = "wifibasedScreen";

    private static final int MAIN = 0;

    private static final Pattern QUOTATION_DELIMITER = Pattern.compile("\"");

    private static final String TAG = "WifiConnectionSettings";

    private PreferenceScreen createPreferenceScreen(String key, String title, Intent intent) {
        PreferenceScreen result = getPreferenceManager().createPreferenceScreen(this);

        result.setPersistent(false);
        result.setKey(key);
        result.setTitle(title);
        result.setIntent(intent);

        return result;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.wificonnectionsettings);

        final SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        /** If the warning has never been shown before, show it. */
        if (!settings.getBoolean("newWarningShown", false)) {
            startActivity(new Intent(this, WarningActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, MAIN, 0, R.string.mainMenu).setIcon(android.R.drawable.ic_menu_revert);

        return result;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        boolean result = super.onOptionsItemSelected(item);

        if (item.getItemId() == MAIN) {
            final Intent intent = new Intent(this, MainMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            result = true;
        }

        return result;
    }

    /** Method is called on any click of a preference... */
    @Override
    public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen,
            final Preference preference) {
        Intent intent;
        final PreferenceCategory wifiPreferenceCategory =
                (PreferenceCategory) preferenceScreen.findPreference(KEY_WIFI_BASED_CATEGORY);
        final Collection<WifiConfiguration> wifiList = new ArrayList<>();

        final PreferenceCategory roomPreferenceCategory =
                (PreferenceCategory) preferenceScreen.findPreference(KEY_ROOM_BASED_CATEGORY);
        final Collection<String> roomList = new ArrayList<>();

        if (wifiPreferenceCategory == null) {
            Log.e(TAG, "Failed to find PreferenceCategory: " + KEY_WIFI_BASED_CATEGORY);
        } else {
            if (preference.getKey().equals(KEY_WIFI_BASED_SCREEN)) {
                /** Clear the wifi list. */
                wifiPreferenceCategory.removeAll();

                final WifiManager wifiManager =
                        (WifiManager) getSystemService(Context.WIFI_SERVICE);

                if (wifiManager == null) {
                    Log.e(TAG, "Failed to retrieve the WifiManager service.");
                } else {
                    final Collection<WifiConfiguration> networks =
                            wifiManager.getConfiguredNetworks();

                    if (networks == null) {
                        Log.e(TAG, "Failed to retrieve a list of configured networks.");
                    } else {
                        wifiList.addAll(networks);
                    }
                }

                for (final WifiConfiguration wifi : wifiList) {
                    if (wifi != null && wifi.SSID != null) {
                        // Friendly SSID-Name
                        final Matcher matcher = QUOTATION_DELIMITER.matcher(wifi.SSID);
                        final String ssid = matcher.replaceAll("");

                        // Add PreferenceScreen for each network
                        final PreferenceScreen ssidItem = createPreferenceScreen(
                                "wifiNetwork" + ssid
                                , ssid
                                , new Intent(this, ConnectionSettings.class).putExtra("SSID", ssid)
                        );

                        if (WifiConfiguration.Status.CURRENT == wifi.status) {
                            ssidItem.setSummary(R.string.connected);
                        } else {
                            ssidItem.setSummary(R.string.notInRange);
                        }

                        wifiPreferenceCategory.addPreference(ssidItem);
                    }
                }
            }

            if (preference.getKey().equals(KEY_ROOM_BASED_SCREEN)) {
                Toast.makeText(this, KEY_ROOM_BASED_CATEGORY, Toast.LENGTH_LONG).show();

                /* clear the room list. */
                roomPreferenceCategory.removeAll();

                for (final String room : MPDApplication.rooms) {
                    final PreferenceScreen roomItem = createPreferenceScreen(
                            "room" + room
                            , room
                            , new Intent(this, ConnectionSettings.class).putExtra("ROOM", room)
                    );

                    roomPreferenceCategory.addPreference(roomItem);
                }

            }

        }

        return false;
    }
}
