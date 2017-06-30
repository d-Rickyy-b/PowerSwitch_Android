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

package eu.power_switch.gui.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.View;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.adapter.ConfigurationDialogTabAdapter;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage1Location;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage2EnterActions;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage3ExitActions;
import eu.power_switch.gui.fragment.configure_geofence.ConfigureGeofenceDialogPage4Summary;
import eu.power_switch.gui.fragment.geofences.ApartmentGeofencesFragment;
import eu.power_switch.obj.Apartment;

/**
 * Dialog to create or modify a Geofence related to an Apartment
 * <p/>
 * Created by Markus on 28.06.2015.
 */
public class ConfigureApartmentGeofenceDialog extends ConfigureGeofenceDialog {

    /**
     * ID of existing Geofence to Edit
     */
    public static final String APARTMENT_ID_KEY = "ApartmentId";

    private long apartmentId = -1;

    public static ConfigureApartmentGeofenceDialog newInstance(long apartmentId) {
        Bundle args = new Bundle();
        args.putLong(APARTMENT_ID_KEY, apartmentId);

        ConfigureApartmentGeofenceDialog fragment = new ConfigureApartmentGeofenceDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected boolean initializeFromExistingData(Bundle arguments) {
        if (arguments != null && arguments.containsKey(APARTMENT_ID_KEY)) {
            apartmentId = arguments.getLong(APARTMENT_ID_KEY);

            try {
                Apartment apartment = DatabaseHandler.getApartment(apartmentId);
                if (apartment.getGeofence() == null) {
                    // Create the adapter that will return a fragment
                    // for each of the two primary sections of the app.
                    setTabAdapter(new CustomTabAdapter(this, getChildFragmentManager(),
                            getTargetFragment(), apartmentId));
                    imageButtonDelete.setVisibility(View.GONE);
                    return false;
                } else {
                    // init dialog using existing geofence
                    geofenceId = apartment.getGeofence().getId();
                    setTabAdapter(new CustomTabAdapter(this, getChildFragmentManager(),
                            getTargetFragment(), apartmentId, geofenceId));
                    imageButtonDelete.setVisibility(View.VISIBLE);
                    return true;
                }
            } catch (Exception e) {
                StatusMessageHandler.showErrorMessage(getContext(), e);
            }
        }

        return false;
    }

    @Override
    protected void deleteExistingConfigurationFromDatabase() {
        new AlertDialog.Builder(getActivity()).setTitle(R.string.are_you_sure).
                setMessage(R.string.geofence_will_be_gone_forever)
                .setPositiveButton
                        (android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    DatabaseHandler.deleteGeofence(geofenceId);
                                    geofenceApiHandler.removeGeofence(geofenceId);

                                    // same for timers
                                    ApartmentGeofencesFragment.sendApartmentGeofencesChangedBroadcast(getActivity());

                                    StatusMessageHandler.showInfoMessage(getTargetFragment(),
                                            R.string.geofence_deleted, Snackbar.LENGTH_LONG);
                                } catch (Exception e) {
                                    StatusMessageHandler.showErrorMessage(getActivity(), e);
                                }

                                // close dialog
                                getDialog().dismiss();
                            }
                        }).setNeutralButton(android.R.string.cancel, null).show();
    }

    protected static class CustomTabAdapter extends ConfigurationDialogTabAdapter {

        private ConfigurationDialogTabbed parentDialog;
        private long apartmentId;
        private long geofenceId;
        private ConfigurationDialogTabbedSummaryFragment summaryFragment;
        private Fragment targetFragment;

        public CustomTabAdapter(ConfigurationDialogTabbed parentDialog, FragmentManager fm, Fragment targetFragment, long apartmentId) {
            super(fm);
            this.parentDialog = parentDialog;
            this.apartmentId = apartmentId;
            this.geofenceId = -1;
            this.targetFragment = targetFragment;
        }

        public CustomTabAdapter(ConfigurationDialogTabbed parentDialog, FragmentManager fm, Fragment targetFragment, long apartmentId,
                                long geofenceId) {
            super(fm);
            this.parentDialog = parentDialog;
            this.apartmentId = apartmentId;
            this.geofenceId = geofenceId;
            this.targetFragment = targetFragment;
        }

        public ConfigurationDialogTabbedSummaryFragment getSummaryFragment() {
            return summaryFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return parentDialog.getString(R.string.location);
                case 1:
                    return parentDialog.getString(R.string.enter);
                case 2:
                    return parentDialog.getString(R.string.exit);
                case 3:
                    return parentDialog.getString(R.string.summary);
            }

            return "" + (position + 1);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;

            switch (i) {
                case 0:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureGeofenceDialogPage1Location.class, parentDialog);
                    break;
                case 1:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureGeofenceDialogPage2EnterActions.class, parentDialog);
                    break;
                case 2:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureGeofenceDialogPage3ExitActions.class, parentDialog);
                    break;
                case 3:
                    fragment = ConfigurationDialogPage.newInstance(ConfigureGeofenceDialogPage4Summary.class, parentDialog);
                    fragment.setTargetFragment(targetFragment, 0);

                    summaryFragment = (ConfigurationDialogTabbedSummaryFragment) fragment;
                    break;
                default:
                    break;
            }

            if (fragment != null && apartmentId != -1) {
                Bundle bundle = new Bundle();
                bundle.putLong(APARTMENT_ID_KEY, apartmentId);

                if (geofenceId != -1) {
                    bundle.putLong(GEOFENCE_ID_KEY, geofenceId);
                }

                fragment.setArguments(bundle);
            }

            return fragment;
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 4;
        }
    }

}