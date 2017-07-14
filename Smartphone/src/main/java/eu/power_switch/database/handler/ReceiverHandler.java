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

package eu.power_switch.database.handler;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import eu.power_switch.database.table.receiver.ReceiverGatewayRelationTable;
import eu.power_switch.database.table.receiver.ReceiverTable;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.AutoPairReceiver;
import eu.power_switch.obj.receiver.DipReceiver;
import eu.power_switch.obj.receiver.MasterSlaveReceiver;
import eu.power_switch.obj.receiver.Receiver;
import eu.power_switch.obj.receiver.UniversalReceiver;
import timber.log.Timber;

/**
 * Provides database methods for managing Receivers of any type
 */
class ReceiverHandler {

    private RoomHandler                roomHandler;
    private MasterSlaveReceiverHandler masterSlaveReceiverHandler;
    private DipHandler                 dipHandler;
    private UniversalButtonHandler     universalButtonHandler;
    private AutoPairHandler            autoPairHandler;
    private SceneItemHandler           sceneItemHandler;
    private ActionHandler              actionHandler;
    private ReceiverReflectionMagic    receiverReflectionMagic;
    private GatewayHandler             gatewayHandler;

    ReceiverHandler(ReceiverReflectionMagic receiverReflectionMagic) {
        this.receiverReflectionMagic = receiverReflectionMagic;

        roomHandler = new RoomHandler();
        masterSlaveReceiverHandler = new MasterSlaveReceiverHandler();
        dipHandler = new DipHandler();
        universalButtonHandler = new UniversalButtonHandler();
        autoPairHandler = new AutoPairHandler();
        sceneItemHandler = new SceneItemHandler();
        actionHandler = new ActionHandler();
        gatewayHandler = new GatewayHandler();
    }

    /**
     * Adds Receiver to Database
     *
     * @param receiver Receiver
     */
    protected void add(@NonNull SQLiteDatabase database, Receiver receiver) throws Exception {
        ContentValues values = new ContentValues();
        values.put(ReceiverTable.COLUMN_NAME, receiver.getName());
        values.put(ReceiverTable.COLUMN_ROOM_ID, receiver.getRoomId());
        values.put(ReceiverTable.COLUMN_MODEL, receiver.getModel());
        values.put(ReceiverTable.COLUMN_CLASSNAME,
                receiver.getClass()
                        .getName());
        values.put(ReceiverTable.COLUMN_TYPE,
                receiver.getType()
                        .toString());
        values.put(ReceiverTable.COLUMN_POSITION_IN_ROOM, roomHandler.get(database, receiver.getRoomId())
                        .getReceivers()
                        .size());
        values.put(ReceiverTable.COLUMN_REPETITION_AMOUNT, receiver.getRepetitionAmount());

        Long receiverId = database.insert(ReceiverTable.TABLE_NAME, null, values);

        if (receiverId > -1) {
            insertDetails(database, receiver, receiverId);
            addAssociatedGateways(database, receiverId, receiver.getAssociatedGateways());
        } else {
            throw new Exception("invalid database.insert() return value: " + receiverId);
        }
    }

    /**
     * Insert Receiver details into related database tables
     *
     * @param receiver   the new Receiver
     * @param receiverId ID of the new Receiver in database
     */
    private void insertDetails(@NonNull SQLiteDatabase database, Receiver receiver, Long receiverId) throws Exception {
        Receiver.Type type = receiver.getType();
        switch (type) {
            case MASTER_SLAVE:
                MasterSlaveReceiver receiverAsMasterSlave = (MasterSlaveReceiver) receiver;
                masterSlaveReceiverHandler.add(database, receiverId, receiverAsMasterSlave.getMaster(), receiverAsMasterSlave.getSlave());
                break;
            case DIPS:
                DipReceiver receiverAsDipReceiver = (DipReceiver) receiver;
                dipHandler.add(database, receiverId, receiverAsDipReceiver);
                break;
            case UNIVERSAL:
                UniversalReceiver receiverAsUniversalReceiver = (UniversalReceiver) receiver;
                universalButtonHandler.addUniversalButtons(database, receiverId, receiverAsUniversalReceiver.getButtons());
                break;
            case AUTOPAIR:
                AutoPairReceiver receiverAsAutoPairReceiver = (AutoPairReceiver) receiver;
                autoPairHandler.add(database, receiverId, receiverAsAutoPairReceiver.getSeed());
                break;
        }
    }

    /**
     * Updates a Receiver in Database
     *
     * @param receiver Receiver
     */
    protected void update(@NonNull SQLiteDatabase database, Receiver receiver) throws Exception {
        updateDetails(database, receiver);

        ContentValues values = new ContentValues();
        values.put(ReceiverTable.COLUMN_NAME, receiver.getName());
        values.put(ReceiverTable.COLUMN_ROOM_ID, receiver.getRoomId());
        values.put(ReceiverTable.COLUMN_MODEL, receiver.getModel());
        values.put(ReceiverTable.COLUMN_CLASSNAME,
                receiver.getClass()
                        .getName());
        values.put(ReceiverTable.COLUMN_TYPE,
                receiver.getType()
                        .toString());
        values.put(ReceiverTable.COLUMN_REPETITION_AMOUNT, receiver.getRepetitionAmount());

        database.update(ReceiverTable.TABLE_NAME, values, ReceiverTable.COLUMN_ID + "=" + receiver.getId(), null);

        // update associated Gateways
        removeAssociatedGateways(database, receiver.getId());
        addAssociatedGateways(database, receiver.getId(), receiver.getAssociatedGateways());

        sceneItemHandler.update(database, receiver.getId());
    }

    /**
     * Updates Receiver DB details
     *
     * @param receiver the edited Receiver
     */
    private void updateDetails(@NonNull SQLiteDatabase database, Receiver receiver) throws Exception {
        long receiverId = receiver.getId();

        deleteDetails(database, receiverId);
        insertDetails(database, receiver, receiverId);
    }

    /**
     * Gets Receiver from Database
     *
     * @param id ID of Receiver
     *
     * @return Receiver
     */
    @NonNull
    protected Receiver get(@NonNull SQLiteDatabase database, Long id) throws Exception {
        Receiver receiver = null;
        Cursor cursor = database.query(ReceiverTable.TABLE_NAME,
                ReceiverTable.ALL_COLUMNS,
                ReceiverTable.COLUMN_ID + "=" + id,
                null,
                null,
                null,
                null);

        if (cursor.moveToFirst()) {
            receiver = dbToReceiver(database, cursor);
        } else {
            cursor.close();
            throw new NoSuchElementException(String.valueOf(id));
        }

        cursor.close();
        return receiver;
    }

    /**
     * Gets a Receiver in a Room
     *
     * @param roomId       ID of Room
     * @param receiverName Name of Receiver
     *
     * @return Receiver
     */
    protected Receiver getByRoom(@NonNull SQLiteDatabase database, Long roomId, String receiverName) throws Exception {
        for (Receiver receiver : getByRoom(database, roomId)) {
            if (receiverName.equals(receiver.getName())) {
                return receiver;
            }
        }

        return null;
    }

    /**
     * Gets all Receivers in a Room
     *
     * @param roomId ID of Room
     *
     * @return List of Receivers
     */
    protected ArrayList<Receiver> getByRoom(@NonNull SQLiteDatabase database, Long roomId) throws Exception {
        ArrayList<Receiver> receivers = new ArrayList<>();
        Cursor cursor = database.query(ReceiverTable.TABLE_NAME,
                ReceiverTable.ALL_COLUMNS,
                ReceiverTable.COLUMN_ROOM_ID + "=" + roomId,
                null,
                null,
                null,
                ReceiverTable.COLUMN_POSITION_IN_ROOM + " ASC");
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            receivers.add(dbToReceiver(database, cursor));
            cursor.moveToNext();
        }
        cursor.close();

        return receivers;
    }

    /**
     * Gets all Receivers in Database
     *
     * @return List of Receivers
     */
    protected List<Receiver> getAll(@NonNull SQLiteDatabase database) throws Exception {
        List<Receiver> receivers = new ArrayList<>();
        Cursor         cursor    = database.query(ReceiverTable.TABLE_NAME, ReceiverTable.ALL_COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            receivers.add(dbToReceiver(database, cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return receivers;
    }

    /**
     * Deletes Receiver from Database
     *
     * @param id ID of Receiver
     */
    protected void delete(@NonNull SQLiteDatabase database, Long id) throws Exception {
        Timber.d("Delete Receiver: " + id);
        // CAREFUL ABOUT DELETE ORDER, SOME THINGS DEPEND ON EXISTING DATA
        // delete depending things first!

        // delete sceneItems where receiver was used
        sceneItemHandler.deleteByReceiverId(database, id);
        // delete actions where receiver was used
        actionHandler.deleteByReceiverId(database, id);

        deleteDetails(database, id);
        removeAssociatedGateways(database, id);
        database.delete(ReceiverTable.TABLE_NAME, ReceiverTable.COLUMN_ID + "=" + id, null);
    }

    /**
     * Deletes Receiver DB details
     *
     * @param id ID of Receiver
     */
    private void deleteDetails(@NonNull SQLiteDatabase database, Long id) throws Exception {
        switch (getType(database, id)) {
            case DIPS:
                dipHandler.delete(database, id);
                break;
            case MASTER_SLAVE:
                masterSlaveReceiverHandler.delete(database, id);
                break;
            case UNIVERSAL:
                universalButtonHandler.deleteUniversalButtons(database, id);
                break;
            case AUTOPAIR:
                autoPairHandler.delete(database, id);
                break;
        }
    }

    /**
     * Gets Type of a Receiver
     *
     * @param id ID of Receiver
     *
     * @return Type of Receiver
     */
    protected Receiver.Type getType(@NonNull SQLiteDatabase database, Long id) throws Exception {
        Receiver.Type type;
        String[]      columns = {ReceiverTable.COLUMN_ID, ReceiverTable.COLUMN_TYPE};
        Cursor        cursor  = database.query(ReceiverTable.TABLE_NAME, columns, ReceiverTable.COLUMN_ID + "=" + id, null, null, null, null);
        if (cursor.moveToFirst()) {
            type = Receiver.Type.getEnum(cursor.getString(1));
        } else {
            cursor.close();
            throw new NoSuchElementException(String.valueOf(id));
        }
        cursor.close();
        return type;
    }

    /**
     * Sets ID of last activated Button of a Receiver
     *
     * @param receiverId ID of Receiver
     * @param buttonId   ID of Button
     */
    protected void setLastActivatedButtonId(@NonNull SQLiteDatabase database, Long receiverId, Long buttonId) throws Exception {
        ContentValues values = new ContentValues();
        values.put(ReceiverTable.COLUMN_LAST_ACTIVATED_BUTTON_ID, buttonId);

        database.update(ReceiverTable.TABLE_NAME, values, ReceiverTable.COLUMN_ID + "=" + receiverId, null);
    }

    /**
     * Sets position in a Room of a Receiver
     *
     * @param receiverId     ID of Receiver
     * @param positionInRoom Position in Room
     */
    protected void setPositionInRoom(@NonNull SQLiteDatabase database, Long receiverId, Long positionInRoom) throws Exception {
        ContentValues values = new ContentValues();
        values.put(ReceiverTable.COLUMN_POSITION_IN_ROOM, positionInRoom);

        database.update(ReceiverTable.TABLE_NAME, values, ReceiverTable.COLUMN_ID + "=" + receiverId, null);
    }

    /**
     * Add relation info about associated Gateways to Database
     *
     * @param receiverId         ID of Room
     * @param associatedGateways List of Gateways
     */
    private void addAssociatedGateways(@NonNull SQLiteDatabase database, Long receiverId, List<Gateway> associatedGateways) throws Exception {
        // add current
        for (Gateway gateway : associatedGateways) {
            ContentValues gatewayRelationValues = new ContentValues();
            gatewayRelationValues.put(ReceiverGatewayRelationTable.COLUMN_RECEIVER_ID, receiverId);
            gatewayRelationValues.put(ReceiverGatewayRelationTable.COLUMN_GATEWAY_ID, gateway.getId());
            database.insert(ReceiverGatewayRelationTable.TABLE_NAME, null, gatewayRelationValues);
        }
    }

    /**
     * Remove all current associated Gateways
     *
     * @param receiverId ID of Room
     */
    private void removeAssociatedGateways(@NonNull SQLiteDatabase database, Long receiverId) throws Exception {
        // delete old associated gateways
        database.delete(ReceiverGatewayRelationTable.TABLE_NAME, ReceiverGatewayRelationTable.COLUMN_RECEIVER_ID + "==" + receiverId, null);
    }

    /**
     * Get Gateways that are associated with a Receiver
     *
     * @param receiverId ID of Receiver
     *
     * @return List of Gateways
     */
    @NonNull
    protected List<Gateway> getAssociatedGateways(@NonNull SQLiteDatabase database, long receiverId) throws Exception {
        List<Gateway> associatedGateways = new ArrayList<>();

        Cursor cursor = database.query(ReceiverGatewayRelationTable.TABLE_NAME,
                ReceiverGatewayRelationTable.ALL_COLUMNS,
                ReceiverGatewayRelationTable.COLUMN_RECEIVER_ID + "==" + receiverId,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Long    gatewayId = cursor.getLong(1);
            Gateway gateway   = gatewayHandler.get(database, gatewayId);
            associatedGateways.add(gateway);
            cursor.moveToNext();
        }

        cursor.close();
        return associatedGateways;
    }

    /**
     * Creates a Receiver Object out of Database information
     *
     * @param c cursor pointing to a Receiver database entry
     *
     * @return Receiver
     */
    private Receiver dbToReceiver(@NonNull SQLiteDatabase database, Cursor c) throws Exception {
        return receiverReflectionMagic.fromDatabase(database, c);
    }
}
