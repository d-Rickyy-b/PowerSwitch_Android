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

package eu.power_switch.obj.receiver.device.rev;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

import eu.power_switch.obj.button.Button;
import eu.power_switch.obj.button.OnButton;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;
import eu.power_switch.obj.receiver.DipReceiver;
import eu.power_switch.obj.receiver.DipSwitch;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.shared.exception.gateway.GatewayNotSupportedException;
import eu.power_switch.shared.exception.receiver.ActionNotSupportedException;

public class Ritter extends Receiver implements DipReceiver {
    // dips are 123456 ABCD

    private static final Brand BRAND = Brand.REV;
    static String MODEL = Receiver.getModelName(Ritter.class.getCanonicalName());

    private String tx433version = "1,";
    private String sSpeedConnAir = "14";
    private String headConnAir = "TXP:0,0,10,5600,350,25,";
    private String tailConnAir = tx433version + sSpeedConnAir + ";";

    private String sSpeedITGW = "125,";
    private String headITGW = "0,0,10,11200,350,26,0,";
    private String tailITGW = tx433version + sSpeedITGW + "0";

    private LinkedList<DipSwitch> dipList;

    public Ritter(Context context, Long id, String name, LinkedList<Boolean> dips, Long roomId) {
        super(context, id, name, BRAND, MODEL, Type.DIPS, roomId);
        dipList = new LinkedList<>();

        if (dips != null && dips.size() == 10) {
            dipList.add(new DipSwitch("1", dips.get(0)));
            dipList.add(new DipSwitch("2", dips.get(1)));
            dipList.add(new DipSwitch("3", dips.get(2)));
            dipList.add(new DipSwitch("4", dips.get(3)));
            dipList.add(new DipSwitch("5", dips.get(4)));
            dipList.add(new DipSwitch("6", dips.get(5)));
            dipList.add(new DipSwitch("A", dips.get(6)));
            dipList.add(new DipSwitch("B", dips.get(7)));
            dipList.add(new DipSwitch("C", dips.get(8)));
            dipList.add(new DipSwitch("D", dips.get(9)));
        } else {
            dipList.add(new DipSwitch("1", false));
            dipList.add(new DipSwitch("2", false));
            dipList.add(new DipSwitch("3", false));
            dipList.add(new DipSwitch("4", false));
            dipList.add(new DipSwitch("5", false));
            dipList.add(new DipSwitch("6", false));
            dipList.add(new DipSwitch("A", false));
            dipList.add(new DipSwitch("B", false));
            dipList.add(new DipSwitch("C", false));
            dipList.add(new DipSwitch("D", false));
        }

        buttons.add(new OnButton(context, id));
    }

    public List<String> getDipNames() {
        List<String> d = new LinkedList<>();
        for (DipSwitch dipSwitch : dipList) {
            d.add(dipSwitch.getName());
        }
        return d;
    }

    @Override
    public String getSignal(Gateway gateway, String action) throws GatewayNotSupportedException, ActionNotSupportedException {
        boolean actionSupported = false;
        for (Button button : buttons) {
            if (button.getName().equals(action)) {
                actionSupported = true;
                break;
            }
        }
        if (!actionSupported) {
            throw new ActionNotSupportedException(action);
        }

        String seq = "";
        String lo = "1,";
        String hi = "3,";

        // segments of four
        String seqLo = lo + hi + lo + hi; // low
        @SuppressWarnings("unused")
        String seqHi = hi + lo + hi + lo; // high (never used)
        String seqFl = lo + hi + hi + lo; // floating

        String on = seqFl + seqFl;

        for (DipSwitch dip : dipList) {
            if (dip.isChecked()) {
                seq += seqLo;
            } else {
                seq += seqFl;
            }
        }

        if (gateway instanceof ConnAir) {
            String ON = headConnAir + seq + on + tailConnAir;
            return ON;
        } else if (gateway instanceof BrematicGWY433) {
            String ON = headConnAir + seq + on + tailConnAir;
            return ON;
        } else if (gateway instanceof ITGW433) {
            String ON = headITGW + seq + on + tailITGW;
            return ON;
        } else {
            throw new GatewayNotSupportedException();
        }
    }

    @Override
    public LinkedList<DipSwitch> getDips() {
        return dipList;
    }
}
