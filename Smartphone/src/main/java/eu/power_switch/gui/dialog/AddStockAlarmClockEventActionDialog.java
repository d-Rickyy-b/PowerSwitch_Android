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

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.gui.fragment.RecyclerViewFragment;
import eu.power_switch.shared.constants.AlarmClockConstants;
import eu.power_switch.shared.constants.LocalBroadcastConstants;

/**
 * Dialog to select an action configuration for a stock alarm clock event
 * <p/>
 * Created by Markus on 27.03.2016.
 */
public class AddStockAlarmClockEventActionDialog extends AddActionDialog {

    public static final String EVENT_ID_KEY = "eventId";

    AlarmClockConstants.Event currentEventType = AlarmClockConstants.Event.ALARM_TRIGGERED;

    public static AddStockAlarmClockEventActionDialog newInstance(int eventId) {
        Bundle args = new Bundle();
        args.putInt(EVENT_ID_KEY, eventId);

        AddStockAlarmClockEventActionDialog fragment = new AddStockAlarmClockEventActionDialog();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Used to notify the setup page that some info has changed
     *
     * @param context any suitable context
     */
    public static void sendAlarmEventActionAddedBroadcast(Context context) {
        Intent intent = new Intent(LocalBroadcastConstants.INTENT_ALARM_EVENT_ACTION_ADDED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null && args.containsKey(EVENT_ID_KEY)) {
            int eventId = args.getInt(EVENT_ID_KEY);
            currentEventType = AlarmClockConstants.Event.getById(eventId);
        }

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    protected void addCurrentSelection() {
        try {
            ArrayList<Action> actions = new ArrayList<>(DatabaseHandler.getAlarmActions(currentEventType));
            actions.add(getCurrentSelection());
            DatabaseHandler.setAlarmActions(currentEventType, actions);
            StatusMessageHandler.showInfoMessage(((RecyclerViewFragment) getTargetFragment()).getRecyclerView(), R.string
                    .action_saved, Snackbar.LENGTH_LONG);
        } catch (Exception e) {
            StatusMessageHandler.showErrorMessage(getActivity(), e);
        }
    }

    @Override
    protected void sendDataChangedBroadcast(Context context) {
        sendAlarmEventActionAddedBroadcast(context);
    }
}