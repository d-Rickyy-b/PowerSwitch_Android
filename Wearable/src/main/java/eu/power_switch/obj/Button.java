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

package eu.power_switch.obj;

import java.io.Serializable;

/**
 * Represents a Button associated with a Receiver
 * <p/>
 * Created by Markus on 06.06.2015.
 */
public class Button implements Serializable {

    /**
     * ID of this Button
     */
    private long id;

    /**
     * Name of this Button
     */
    private String name;

    /**
     * ID of the Receiver this Button is associated with
     */
    private long receiverId;

    /**
     * Default constructor
     *
     * @param id
     * @param name
     * @param receiverId
     */
    public Button(long id, String name, long receiverId) {
        this.id = id;
        this.name = name;
        this.receiverId = receiverId;
    }

    /**
     * Get ID of this Button
     *
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     * Set ID of this Button
     *
     * @param id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Get Name of this Button
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Set Name of this Button
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get ID of associated Receiver
     *
     * @return
     */
    public long getReceiverId() {
        return receiverId;
    }
}
