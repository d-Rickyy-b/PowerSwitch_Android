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

package eu.power_switch.gui.fragment.geofences;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.event.ApartmentGeofenceChangedEvent;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.google_play_services.geofence.GeofenceApiHandler;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.adapter.GeofenceRecyclerViewAdapter;
import eu.power_switch.gui.dialog.SelectApartmentForGeofenceDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.obj.Apartment;
import eu.power_switch.persistence.PersistanceHandler;
import eu.power_switch.settings.SmartphonePreferencesHandler;
import eu.power_switch.shared.ThemeHelper;
import eu.power_switch.shared.constants.PermissionConstants;
import eu.power_switch.shared.event.PermissionChangedEvent;
import eu.power_switch.shared.permission.PermissionHelper;
import timber.log.Timber;

/**
 * Fragment containing a List of all Apartment related Geofences
 */
public class ApartmentGeofencesFragment extends RecyclerViewFragment<Geofence> {

    private static final String[] NEEDED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};

    @BindView(R.id.add_fab)
    FloatingActionButton fab;

    @Inject
    GeofenceApiHandler geofenceApiHandler;

    @Inject
    PersistanceHandler persistanceHandler;

    private HashMap<Long, Apartment> geofenceIdApartmentMap = new HashMap<>();
    private ArrayList<Geofence>      geofences              = new ArrayList<>();
    private GeofenceRecyclerViewAdapter geofenceRecyclerViewAdapter;

    /**
     * Used to notify the apartment geofence page (this) that geofences have changed
     */
    public static void notifyApartmentGeofencesChanged() {
        EventBus.getDefault()
                .post(new ApartmentGeofenceChangedEvent());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        geofenceRecyclerViewAdapter = new GeofenceRecyclerViewAdapter(getActivity(),
                geofences,
                geofenceApiHandler,
                persistanceHandler,
                statusMessageHandler);
        getRecyclerView().setAdapter(geofenceRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
        getRecyclerView().setLayoutManager(layoutManager);

        final RecyclerViewFragment recyclerViewFragment = this;
        geofenceRecyclerViewAdapter.setOnItemLongClickListener(new GeofenceRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                final Geofence geofence = geofences.get(position);

                // TODO: fix this
                Timber.w("needs fix");

//                ConfigureApartmentGeofenceDialog configureApartmentGeofenceDialog = ConfigureApartmentGeofenceDialog.newInstance(
//                        geofenceIdApartmentMap.get(geofence.getId())
//                                .getId(), recyclerViewFragment);
//                configureApartmentGeofenceDialog.show(getFragmentManager(), null);
            }
        });

        fab = rootView.findViewById(R.id.add_fab);
        fab.setImageDrawable(IconicsHelper.getAddIcon(getActivity(), ContextCompat.getColor(getActivity(), android.R.color.white)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionHelper.isLocationPermissionAvailable(getContext())) {
                    PermissionHelper.showMissingPermissionDialog(getActivity(),
                            PermissionConstants.REQUEST_CODE_LOCATION_PERMISSION,
                            NEEDED_PERMISSIONS);
                    return;
                }

                try {
                    int apartmentsCount = persistanceHandler.getAllApartments()
                            .size();

                    if (apartmentsCount == 0) {
                        new AlertDialog.Builder(getContext()).setMessage(R.string.please_create_or_activate_apartment_first)
                                .setNeutralButton(android.R.string.ok, null)
                                .show();
                        return;
                    } else if (persistanceHandler.getAllApartments()
                            .size() == geofences.size()) {
                        new AlertDialog.Builder(getContext()).setMessage(R.string.all_apartments_have_geofence)
                                .setNeutralButton(android.R.string.ok, null)
                                .show();
                        return;
                    }
                } catch (Exception e) {
                    statusMessageHandler.showErrorMessage(recyclerViewFragment.getRecyclerView(), e);
                    return;
                }

                SelectApartmentForGeofenceDialog selectApartmentForGeofenceDialog = new SelectApartmentForGeofenceDialog();
                selectApartmentForGeofenceDialog.setTargetFragment(recyclerViewFragment, 0);
                selectApartmentForGeofenceDialog.show(getFragmentManager(), null);
            }
        });

        if (!PermissionHelper.isLocationPermissionAvailable(getContext())) {
            showEmpty();
            statusMessageHandler.showPermissionMissingMessage(getActivity(), getRecyclerView(), PermissionConstants.REQUEST_CODE_LOCATION_PERMISSION,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            refreshGeofences();
        }

        return rootView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onPermissionChanged(PermissionChangedEvent permissionChangedEvent) {
        int   permissionRequestCode = permissionChangedEvent.getRequestCode();
        int[] result                = permissionChangedEvent.getGrantResults();

        if (permissionRequestCode == PermissionConstants.REQUEST_CODE_LOCATION_PERMISSION) {
            if (result[0] == PackageManager.PERMISSION_GRANTED) {
                statusMessageHandler.showInfoMessage(getRecyclerView(), R.string.permission_granted, Snackbar.LENGTH_SHORT);

                notifyApartmentGeofencesChanged();
            } else {
                statusMessageHandler.showPermissionMissingMessage(getActivity(),
                        getRecyclerView(),
                        PermissionConstants.REQUEST_CODE_LOCATION_PERMISSION,
                        Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onApartmentGeofenceChanged(ApartmentGeofenceChangedEvent apartmentGeofenceChangedEvent) {
        refreshGeofences();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_apartment_geofences;
    }

    private void refreshGeofences() {
        Timber.d("refreshGeofences");
        updateListContent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        switch (menuItem.getItemId()) {
            case R.id.create_geofence:
                if (!PermissionHelper.isLocationPermissionAvailable(getContext())) {
                    PermissionHelper.showMissingPermissionDialog(getActivity(),
                            PermissionConstants.REQUEST_CODE_LOCATION_PERMISSION,
                            NEEDED_PERMISSIONS);
                    break;
                }

                try {
                    int apartmentsCount = persistanceHandler.getAllApartments()
                            .size();

                    if (apartmentsCount == 0) {
                        statusMessageHandler.showInfoMessage(getRecyclerView(),
                                R.string.please_create_or_activate_apartment_first,
                                Snackbar.LENGTH_LONG);
                        return true;
                    } else if (apartmentsCount == geofences.size()) {
                        statusMessageHandler.showInfoMessage(getRecyclerView(), R.string.all_apartments_have_geofence, Snackbar.LENGTH_LONG);
                        return true;
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }

                SelectApartmentForGeofenceDialog selectApartmentForGeofenceDialog = new SelectApartmentForGeofenceDialog();
                selectApartmentForGeofenceDialog.setTargetFragment(this, 0);
                selectApartmentForGeofenceDialog.show(getFragmentManager(), null);
            default:
                break;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.apartment_geofences_fragment_menu, menu);
        final int color = ThemeHelper.getThemeAttrColor(getActivity(), android.R.attr.textColorPrimary);
        menu.findItem(R.id.create_geofence)
                .setIcon(IconicsHelper.getAddIcon(getActivity(), color));

        if (!smartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB)) {
            menu.findItem(R.id.create_geofence)
                    .setVisible(false)
                    .setEnabled(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        geofenceApiHandler.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (smartphonePreferencesHandler.<Boolean>get(SmartphonePreferencesHandler.KEY_USE_OPTIONS_MENU_INSTEAD_OF_FAB)) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
        geofenceApiHandler.onStop();
        super.onStop();
    }

    @Override
    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return geofenceRecyclerViewAdapter;
    }

    @Override
    protected int getSpanCount() {
        return getResources().getInteger(R.integer.geofence_grid_span_count);
    }

    @Override
    public List<Geofence> loadListData() throws Exception {
        geofenceIdApartmentMap.clear();

        ArrayList<Geofence> geofences = new ArrayList<>();
        List<Apartment>     apartments;

        apartments = persistanceHandler.getAllApartments();

        for (Apartment apartment : apartments) {
            // apartment can have no associated Geofence, so we just ignore it
            if (apartment.getGeofence() != null) {
                geofences.add(apartment.getGeofence());
                geofenceIdApartmentMap.put(apartment.getGeofence()
                        .getId(), apartment);
            }
        }

        return geofences;
    }

    @Override
    protected void onListDataChanged(List<Geofence> list) {
        geofences.clear();
        geofences.addAll(list);
    }
}