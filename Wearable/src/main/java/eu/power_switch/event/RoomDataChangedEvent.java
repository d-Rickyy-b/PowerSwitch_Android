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

package eu.power_switch.event;

import java.util.List;

import eu.power_switch.obj.Room;
import eu.power_switch.shared.event.EventBusEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Created by Markus on 01.08.2017.
 */
@EqualsAndHashCode(callSuper = true)
@Value
public class RoomDataChangedEvent extends EventBusEvent {

    List<Room> rooms;

}