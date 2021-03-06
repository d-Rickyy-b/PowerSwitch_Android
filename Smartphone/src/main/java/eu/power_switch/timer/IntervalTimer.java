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

package eu.power_switch.timer;

import java.util.ArrayList;
import java.util.Calendar;

import eu.power_switch.action.Action;
import eu.power_switch.shared.log.LogHandler;

/**
 * Timer based on just a start time and an interval
 * <p/>
 * Created by Markus on 21.09.2015.
 */
public class IntervalTimer extends Timer {

    private Calendar executionTime;
    private long executionInterval;

    public IntervalTimer(long id, boolean isActive, String name, Calendar executionTime, long executionInterval,
                         ArrayList<Action> actions) {
        super(id, isActive, name, EXECUTION_TYPE_INTERVAL, new ArrayList<Action>());
        this.executionTime = executionTime;
        this.executionInterval = executionInterval;
        this.actions = actions;
    }

    @Override
    public Calendar getExecutionTime() {
        return executionTime;
    }

    @Override
    public long getExecutionInterval() {
        return executionInterval;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Timer: ");
        if (active) {
            stringBuilder.append("(enabled) ");
        } else {
            stringBuilder.append("(disabled) ");
        }
        stringBuilder.append(getName())
                .append("(").append(getId()).append(") Time: ")
                .append(getExecutionTime().getTimeInMillis())
                .append(" {\n");

        for (Action action : getActions()) {
            stringBuilder.append(LogHandler.addIndentation(action.toString())).append("\n");
        }

        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
