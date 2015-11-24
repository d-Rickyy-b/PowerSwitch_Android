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

package eu.power_switch.timer.action;

import android.content.Context;

import eu.power_switch.api.IntentReceiver;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.device.Receiver;

/**
 * TimerReceiverAction that holds a specific room/receiver/button combination to activate on execution
 * <p/>
 * Created by Markus on 24.09.2015.
 */
public class TimerReceiverAction extends TimerAction {

    private Room room;
    private Receiver receiver;
    private Button button;

    public TimerReceiverAction(long id, Room room, Receiver receiver, Button button) {
        this.id = id;
        this.room = room;
        this.receiver = receiver;
        this.button = button;
    }

    public Room getRoom() {
        return room;
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public Button getButton() {
        return button;
    }

    @Override
    public String getActionType() {
        return ACTION_TYPE_RECEIVER;
    }

    @Override
    public String toString() {
        return room.getName() + ": " + receiver.getName() + ": " + button.getName();
    }

    @Override
    public void execute(Context context) {
        IntentReceiver.parseActionIntent(context,
                IntentReceiver.createReceiverButtonIntent(room.getName(), receiver.getName(), button.getName()));
    }
}
