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

package eu.power_switch.gui.fragment.main;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import eu.power_switch.R;
import eu.power_switch.action.ActionHandler;
import eu.power_switch.event.ActiveApartmentChangedEvent;
import eu.power_switch.event.SceneChangedEvent;
import eu.power_switch.gui.adapter.SceneRecyclerViewAdapter;
import eu.power_switch.gui.dialog.configuration.ConfigureSceneDialog;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.obj.Scene;
import eu.power_switch.persistence.preferences.SmartphonePreferencesHandler;
import eu.power_switch.shared.constants.SettingsConstants;
import timber.log.Timber;

import static eu.power_switch.persistence.preferences.SmartphonePreferencesHandler.KEY_CURRENT_APARTMENT_ID;


/**
 * Fragment containing a List of all Scenes
 */
public class ScenesFragment extends RecyclerViewFragment<Scene> {

    @Inject
    ActionHandler actionHandler;

    private ArrayList<Scene> scenes = new ArrayList<>();
    private SceneRecyclerViewAdapter sceneRecyclerViewAdapter;

    /**
     * Used to notify Scene Fragment (this) that Scenes have changed
     */
    public static void notifySceneChanged() {
        Timber.d("ScenesFragment", "notifySceneChanged");
        EventBus.getDefault()
                .post(new SceneChangedEvent());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        sceneRecyclerViewAdapter = new SceneRecyclerViewAdapter(this, scenes, actionHandler, persistenceHandler,
                smartphonePreferencesHandler);
        getRecyclerView().setAdapter(sceneRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(getSpanCount(), StaggeredGridLayoutManager.VERTICAL);
        getRecyclerView().setLayoutManager(layoutManager);

        final RecyclerViewFragment recyclerViewFragment = this;
        sceneRecyclerViewAdapter.setOnItemLongClickListener(new SceneRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int position) {
                final Scene scene = scenes.get(position);

                ConfigureSceneDialog configureSceneDialog = ConfigureSceneDialog.newInstance(scene, recyclerViewFragment);
                configureSceneDialog.show(getFragmentManager(), null);
            }
        });

        IconicsDrawable icon = iconicsHelper.getFabIcon(MaterialDesignIconic.Icon.gmi_plus);
        addFAB.setImageDrawable(icon);
        addFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    long apartmentId = smartphonePreferencesHandler.getValue(KEY_CURRENT_APARTMENT_ID);
                    if (SettingsConstants.INVALID_APARTMENT_ID == apartmentId) {
                        new AlertDialog.Builder(getContext()).setMessage(R.string.please_create_or_activate_apartment_first)
                                .setNeutralButton(android.R.string.ok, null)
                                .show();
                        return;
                    }


                    ConfigureSceneDialog configureSceneDialog = ConfigureSceneDialog.newInstance(recyclerViewFragment);
                    configureSceneDialog.show(getFragmentManager(), null);
                } catch (Exception e) {
                    statusMessageHandler.showErrorMessage(getRecyclerView(), e);
                }
            }
        });

        updateUI();

        return rootView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_scenes;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onActiveApartmentChanged(ActiveApartmentChangedEvent activeApartmentChangedEvent) {
        updateUI();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onSceneChanged(SceneChangedEvent sceneChangedEvent) {
        updateUI();
    }

    private void updateUI() {
        Timber.d("ScenesFragment", "updateUI");
        updateListContent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (super.onOptionsItemSelected(menuItem)) {
            return true;
        }

        long apartmentId = smartphonePreferencesHandler.getValue(KEY_CURRENT_APARTMENT_ID);
        switch (menuItem.getItemId()) {
            case R.id.create_scene:
                if (SettingsConstants.INVALID_APARTMENT_ID == apartmentId) {
                    new AlertDialog.Builder(getContext()).setMessage(R.string.please_create_or_activate_apartment_first)
                            .setNeutralButton(android.R.string.ok, null)
                            .show();
                    return true;
                }

                ConfigureSceneDialog configureSceneDialog = ConfigureSceneDialog.newInstance(this);
                configureSceneDialog.show(getFragmentManager(), null);
            default:
                break;

        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.scene_fragment_menu, menu);
        IconicsDrawable icon = iconicsHelper.getOptionsMenuIcon(MaterialDesignIconic.Icon.gmi_plus);
        menu.findItem(R.id.create_scene)
                .setIcon(icon);

        boolean useOptionsMenuOnly = smartphonePreferencesHandler.getValue(SmartphonePreferencesHandler.USE_OPTIONS_MENU_INSTEAD_OF_FAB);
        if (!useOptionsMenuOnly) {
            menu.findItem(R.id.create_scene)
                    .setVisible(false)
                    .setEnabled(false);
        }
    }

    @Override
    public RecyclerView.Adapter getRecyclerViewAdapter() {
        return sceneRecyclerViewAdapter;
    }

    @Override
    protected int getSpanCount() {
        return getResources().getInteger(R.integer.scene_grid_span_count);
    }

    @Override
    public List<Scene> loadListData() throws Exception {
        long apartmentId = smartphonePreferencesHandler.getValue(KEY_CURRENT_APARTMENT_ID);
        return persistenceHandler.getScenes(apartmentId);
    }

    @Override
    protected void onListDataChanged(List<Scene> list) {
        scenes.clear();
        scenes.addAll(list);
    }
}