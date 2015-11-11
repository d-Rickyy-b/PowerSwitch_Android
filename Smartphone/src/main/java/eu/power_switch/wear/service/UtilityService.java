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

package eu.power_switch.wear.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.developer.PlayStoreModeDataModel;
import eu.power_switch.log.Log;
import eu.power_switch.obj.Button;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.device.Receiver;
import eu.power_switch.settings.SharedPreferencesHandler;
import eu.power_switch.shared.Constants;

/**
 * Created by Markus on 06.06.2015.
 * <p/>
 * Service to handle background Network communication with the Wearable App
 */
public class UtilityService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public UtilityService() {
        super("UtilityService");
    }

    /**
     * Create Intent to update Wear Data via background service
     *
     * @param context
     */
    public static void forceWearDataUpdate(Context context) {
        Intent intent = new Intent(context, UtilityService.class);
        intent.setAction(Constants.REQUEST_DATA_UPDATE_PATH);
        context.startService(intent);
    }

    /**
     * Transfer the required data over to the wearable
     *
     * @param rooms     List containing Rooms from Database
     * @param receivers List containing Receivers from Database
     */
    private void sendDataToWearable(List<Room> rooms, List<Receiver> receivers, List<Button> buttons, List<Scene>
            scenes) {
        Log.d("Sending new Data to Wearable...");
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        // It's OK to use blockingConnect() here as we are running in an
        // IntentService that executes work on a separate (background) thread.
        ConnectionResult connectionResult = googleApiClient.blockingConnect(Constants.GOOGLE_API_CLIENT_TIMEOUT, TimeUnit.SECONDS);

        ArrayList<DataMap> data = new ArrayList<>();

        ArrayList<DataMap> roomData = new ArrayList<>();
        ArrayList<DataMap> receiverData = new ArrayList<>();
        ArrayList<DataMap> buttonData = new ArrayList<>();
        ArrayList<DataMap> sceneData = new ArrayList<>();

        for (Room room : rooms) {
            roomData.add(convertToDataMap(room));
            data.add(convertToDataMap(room));
        }

        for (Receiver receiver : receivers) {
            receiverData.add(convertToDataMap(receiver));
            data.add(convertToDataMap(receiver));
        }

        for (Button button : buttons) {
            buttonData.add(convertToDataMap(button));
            data.add(convertToDataMap(button));
        }

        for (Scene scene : scenes) {
            sceneData.add(convertToDataMap(scene));
            data.add(convertToDataMap(scene));
        }

        if (connectionResult.isSuccess() && googleApiClient.isConnected()
                && data.size() > 0) {

            PutDataMapRequest dataMap = PutDataMapRequest.create(Constants.DATA_PATH);
            dataMap.getDataMap().putDataMapArrayList(Constants.EXTRA_DATA, data);
            PutDataRequest request = dataMap.asPutDataRequest();

            // Send the data over
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(googleApiClient, request).await();

            if (!result.getStatus().isSuccess()) {
                Log.e("", String.format("Error sending data using DataApi (error code = %d)",
                        result.getStatus().getStatusCode()));
            } else {
                Log.d("Update data sent");
            }

        } else {
            // GoogleApiClient connection error
            Log.e("Error connecting GoogleApiClient");
        }

    }

    /**
     * Puts a Room into a DataMap
     *
     * @param room Room to convert
     * @return DataMap
     */
    private DataMap convertToDataMap(Room room) {
        DataMap roomDataMap = new DataMap();

        roomDataMap.putLong(Constants.ROOM_ID_DATAMAP_KEY, room.getId());
        roomDataMap.putString(Constants.ROOM_NAME_DATAMAP_KEY, room.getName());

        return roomDataMap;
    }

    /**
     * Puts a Receiver into a DataMap
     *
     * @param receiver Receiver to convert
     * @return DataMap
     */
    private DataMap convertToDataMap(Receiver receiver) {
        DataMap receiverDataMap = new DataMap();

        receiverDataMap.putLong(Constants.RECEIVER_ID_DATAMAP_KEY, receiver.getId());
        receiverDataMap.putString(Constants.RECEIVER_NAME_DATAMAP_KEY, receiver.getName());
        receiverDataMap.putLong(Constants.RECEIVER_ROOM_ID_DATAMAP_KEY, receiver.getRoomId());
        receiverDataMap.putInt(Constants.RECEIVER_POSITION_IN_ROOM_DATAMAP_KEY, receiver.getPositionInRoom());
        receiverDataMap.putLong(Constants.RECEIVER_LAST_ACTIVATED_BUTTON_ID_DATAMAP_KEY, DatabaseHandler
                .getLastActivatedButtonId(receiver.getId()));

        return receiverDataMap;
    }

    /**
     * Puts a Button into a DataMap
     *
     * @param button Button to convert
     * @return DataMap
     */
    private DataMap convertToDataMap(Button button) {
        DataMap buttonDataMap = new DataMap();

        buttonDataMap.putLong(Constants.BUTTON_ID_DATAMAP_KEY, button.getId());
        buttonDataMap.putString(Constants.BUTTON_NAME_DATAMAP_KEY, button.getName());
        buttonDataMap.putLong(Constants.BUTTON_RECEIVER_ID_DATAMAP_KEY, button.getReceiverId());

        return buttonDataMap;
    }

    /**
     * Puts a Scene into a DataMap
     *
     * @param scene Scene to convert
     * @return DataMap
     */
    private DataMap convertToDataMap(Scene scene) {
        DataMap roomDataMap = new DataMap();

        roomDataMap.putLong(Constants.SCENE_ID_DATAMAP_KEY, scene.getId());
        roomDataMap.putString(Constants.SCENE_NAME_DATAMAP_KEY, scene.getName());

        return roomDataMap;
    }

    /**
     * Receive internal intents
     *
     * @param intent
     */
    @Override
    synchronized protected void onHandleIntent(Intent intent) {
        DatabaseHandler.init(getApplicationContext());

        // Get Room/Receiver/Scene Data from Database and send to wearable
        if (intent.getAction().equals(Constants.REQUEST_DATA_UPDATE_PATH)) {
            SharedPreferencesHandler sharedPreferencesHandler = new SharedPreferencesHandler(getApplicationContext());

            Log.d("Getting Data from Database to send to Wearable...");

            if (sharedPreferencesHandler.getPlayStoreMode()) {
                PlayStoreModeDataModel playStoreModeDataModel = new PlayStoreModeDataModel(getApplicationContext());

                List<Room> rooms = playStoreModeDataModel.getRooms();
                List<Receiver> receivers = playStoreModeDataModel.getReceivers();

                List<Button> buttons = new ArrayList<>();
                for (Receiver receiver : receivers) {
                    buttons.addAll(receiver.getButtons());
                }

                List<Scene> scenes = playStoreModeDataModel.getScenes();

                sendDataToWearable(rooms, receivers, buttons, scenes);
                return;
            }
            List<Room> rooms = DatabaseHandler.getAllRooms();
            List<Receiver> receivers = DatabaseHandler.getAllReceivers();
            List<Button> buttons = new ArrayList<>();

            for (Receiver receiver : receivers) {
                buttons.addAll(receiver.getButtons());
            }

            List<Scene> scenes = DatabaseHandler.getAllScenes();

            sendDataToWearable(rooms, receivers, buttons, scenes);
        }
    }
}
