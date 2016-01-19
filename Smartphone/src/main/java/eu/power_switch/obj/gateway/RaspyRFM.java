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

package eu.power_switch.obj.gateway;

import eu.power_switch.network.NetworkPackage;

/**
 * ConnAir represents a ConnAir Gateway from Simple-Solutions
 */
public class RaspyRFM extends Gateway {

    /**
     * Model constant
     */
    public static final String MODEL = "RaspyRFM";

    public RaspyRFM(Long id, boolean active, String name, String firmware, String address, int port) {
        super(id, active, name, MODEL, firmware, address, port);
        capabilities.add(Capability.SEND);
    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public String getModelAsString() {
        return MODEL;
    }

    @Override
    public NetworkPackage.CommunicationType getCommunicationType() {
        return NetworkPackage.CommunicationType.UDP;
    }
}