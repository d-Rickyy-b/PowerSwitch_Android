/*
 *  PowerSwitch by Max Rosin & Markus Ressel
 *  Copyright (C) 2015  Markus Ressel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.dialog.configuration;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.util.List;

import eu.power_switch.R;
import eu.power_switch.gui.dialog.configuration.holder.ApartmentConfigurationHolder;
import eu.power_switch.gui.fragment.ApartmentFragment;
import eu.power_switch.gui.fragment.configure_apartment.ConfigureApartmentDialogPage1Name;
import eu.power_switch.obj.Apartment;
import eu.power_switch.shared.constants.SettingsConstants;
import timber.log.Timber;

import static eu.power_switch.persistence.preferences.SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID;

/**
 * Dialog to configure (create/edit) an Apartment
 * <p/>
 * Created by Markus on 27.12.2015.
 */
public class ConfigureApartmentDialog extends ConfigurationDialog<ApartmentConfigurationHolder> {

    /**
     * Open this dialog without predefined data
     *
     * @return An instance of this ConfigurationDialog
     */
    public static ConfigureApartmentDialog newInstance(@NonNull Fragment targetFragment) {
        return newInstance(null, targetFragment);
    }

    /**
     * Open this dialog with predefined data
     *
     * @return An instance of this ConfigurationDialog
     */
    public static ConfigureApartmentDialog newInstance(Apartment apartment, @NonNull Fragment targetFragment) {
        Bundle args = new Bundle();

        ConfigureApartmentDialog     fragment                     = new ConfigureApartmentDialog();
        ApartmentConfigurationHolder apartmentConfigurationHolder = new ApartmentConfigurationHolder();
        if (apartment != null) {
            apartmentConfigurationHolder.setApartment(apartment);
        }
        fragment.setConfiguration(apartmentConfigurationHolder);
        fragment.setTargetFragment(targetFragment, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initializeFromExistingData(Bundle args) throws Exception {
        getConfiguration().setExistingApartments(persistenceHandler.getAllApartments());

        Apartment apartment = getConfiguration().getApartment();
        if (apartment != null) {
            getConfiguration().setName(apartment.getName());
            getConfiguration().setAssociatedGateways(apartment.getAssociatedGateways());
        }
    }

    @Override
    protected int getDialogTitle() {
        return R.string.configure_apartment;
    }

    @Override
    protected void addPageEntries(List<PageEntry<ApartmentConfigurationHolder>> pageEntries) {
        pageEntries.add(new PageEntry<>(R.string.name, ConfigureApartmentDialogPage1Name.class));
    }

    @Override
    protected void saveConfiguration() throws Exception {
        Timber.d("Saving apartment");

        long apartmentId = -1;
        if (getConfiguration().getApartment() != null) {
            apartmentId = getConfiguration().getApartment()
                    .getId();
        }
        if (apartmentId == -1) {
            boolean isActive = persistenceHandler.getAllApartmentNames()
                    .size() <= 0;
            Apartment newApartment = new Apartment((long) -1,
                    isActive,
                    getConfiguration().getName(),
                    getConfiguration().getAssociatedGateways(),
                    null);

            long newId = persistenceHandler.addApartment(newApartment);
            // set new apartment as active if it is the first and only one
            if (isActive) {
                smartphonePreferencesHandler.setValue(KEY_CURRENT_APARTMENT_ID, newId);
            }
        } else {
            Apartment apartment = persistenceHandler.getApartment(apartmentId);
            if (apartment.getGeofence() != null) {
                apartment.getGeofence()
                        .setName(getConfiguration().getName());
            }

            Apartment updatedApartment = new Apartment(apartmentId,
                    apartment.isActive(),
                    getConfiguration().getName(),
                    getConfiguration().getAssociatedGateways(),
                    apartment.getGeofence());

            persistenceHandler.updateApartment(updatedApartment);
        }

        ApartmentFragment.notifyActiveApartmentChanged(getActivity());
    }

    @Override
    protected void deleteConfiguration() throws Exception {
        Long existingApartmentId = getConfiguration().getApartment()
                .getId();
        if (smartphonePreferencesHandler.getValue(KEY_CURRENT_APARTMENT_ID)
                .equals(existingApartmentId)) {
            persistenceHandler.deleteApartment(existingApartmentId);

            // update current Apartment selection
            List<Apartment> apartments = persistenceHandler.getAllApartments();
            if (apartments.isEmpty()) {
                smartphonePreferencesHandler.setValue(KEY_CURRENT_APARTMENT_ID, SettingsConstants.INVALID_APARTMENT_ID);
            } else {
                smartphonePreferencesHandler.setValue(KEY_CURRENT_APARTMENT_ID,
                        apartments.get(0)
                                .getId());
            }
        } else {
            persistenceHandler.deleteApartment(existingApartmentId);
        }

        ApartmentFragment.notifyActiveApartmentChanged(getActivity());
    }

}
