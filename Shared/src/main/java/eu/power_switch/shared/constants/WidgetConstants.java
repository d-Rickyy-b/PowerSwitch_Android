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

package eu.power_switch.shared.constants;

/**
 * Class holding constants related to Widgets for both Smartphone and Wearable Module
 * <p/>
 * Created by Markus on 11.11.2015.
 */
public class WidgetConstants {

    // Widgets
    public static final String WIDGET_ACTION_INTENT = "eu.power_switch.widget_action";

    public static final String KEY_APARTMENT = "Apartment";
    public static final String KEY_ROOM = "Room";
    public static final String KEY_RECEIVER = "Receiver";
    public static final String KEY_BUTTON = "Button";
    public static final String KEY_SCENE = "Scene";

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private WidgetConstants() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }
}
