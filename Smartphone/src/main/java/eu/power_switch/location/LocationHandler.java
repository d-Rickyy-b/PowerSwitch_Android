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

package eu.power_switch.location;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import eu.power_switch.R;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.shared.constants.PermissionConstants;
import eu.power_switch.shared.log.Log;

/**
 * Created by Markus on 21.12.2015.
 */
public class LocationHandler {

    private static Activity activity;
    private static GoogleApiClient googleApiClient;

    private static boolean isGoogleApiConnected;

    private LocationHandler() {
    }

    public static void init(Activity activity) {
        LocationHandler.activity = activity;

        if (!checkLocationPermission()) {
            requestLocationPermission();
        }

        // Create an instance of GoogleAPIClient.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(activity)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            isGoogleApiConnected = true;
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            isGoogleApiConnected = false;
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            isGoogleApiConnected = false;
                        }
                    })
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public static void connect() {
        if (googleApiClient != null && !isGoogleApiConnected) {
            googleApiClient.connect();
        }
    }

    public static void disconnect() {
        if (googleApiClient != null && isGoogleApiConnected) {
            googleApiClient.disconnect();
        }
    }

    public static Location getLastLocation() {
        // TODO: get permission

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                .PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return null;
        }
        return LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }

    private static boolean checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+
            int hasLocationPermission = ContextCompat.checkSelfPermission(activity, Manifest
                    .permission.ACCESS_FINE_LOCATION);
            if (hasLocationPermission == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            // Pre-Marshmallow
            return true;
        }
    }

    public static void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission
                .ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.d("Displaying location permission rationale to provide additional context.");

            StatusMessageHandler.showStatusMessage(activity, R.string.missing_location_permission, android.R.string.ok, new Runnable() {
                @Override
                public void run() {
                    ActivityCompat.requestPermissions(activity, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION}, PermissionConstants.REQUEST_CODE_LOCATION_PERMISSION);
                }
            }, Snackbar.LENGTH_INDEFINITE);
        } else {
            Log.d("Displaying default location permission dialog to request permission");
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, PermissionConstants.REQUEST_CODE_LOCATION_PERMISSION);
        }
    }

}