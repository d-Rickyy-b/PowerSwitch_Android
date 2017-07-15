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

package eu.power_switch.widget.provider;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Arrays;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import eu.power_switch.R;
import eu.power_switch.obj.Apartment;
import eu.power_switch.obj.Room;
import eu.power_switch.persistence.PersistanceHandler;
import eu.power_switch.widget.RoomWidget;
import eu.power_switch.widget.WidgetIntentReceiver;
import eu.power_switch.widget.activity.ConfigureRoomWidgetActivity;
import timber.log.Timber;

/**
 * This class is responsible for updating existing Room widgets
 */
public class RoomWidgetProvider extends AppWidgetProvider {

    @Inject
    PersistanceHandler persistanceHandler;

    /**
     * Forces an Update of all Room Widgets
     *
     * @param context any suitable context
     */
    public static void forceWidgetUpdate(Context context) {
        // update room widgets
        Intent intent = new Intent(context, RoomWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(context.getApplicationContext())
                .getAppWidgetIds(new ComponentName(context.getApplicationContext(), RoomWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Timber.d("Updating Room Widgets...");
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews remoteViews = new RemoteViews(context.getResources()
                    .getString(eu.power_switch.shared.R.string.PACKAGE_NAME), R.layout.widget_room);

            try {
                RoomWidget roomWidget = persistanceHandler.getRoomWidget(appWidgetId);
                try {
                    Room      room      = persistanceHandler.getRoom(roomWidget.getRoomId());
                    Apartment apartment = persistanceHandler.getApartment(room.getApartmentId());

                    // update UI
                    remoteViews.setTextViewText(R.id.textView_room_widget_name, apartment.getName() + ": " + room.getName());

                    // set button action
                    remoteViews.setOnClickPendingIntent(R.id.button_on,
                            WidgetIntentReceiver.buildRoomWidgetButtonPendingIntent(context, apartment, room, context.getString(R.string.on),
                                    ConfigureRoomWidgetActivity.ROOM_INTENT_ID_OFFSET + appWidgetId));
                    remoteViews.setOnClickPendingIntent(R.id.button_off,
                            WidgetIntentReceiver.buildRoomWidgetButtonPendingIntent(context, apartment, room, context.getString(R.string.off),
                                    ConfigureRoomWidgetActivity.ROOM_INTENT_ID_OFFSET + appWidgetId + 1));
                    remoteViews.setViewVisibility(R.id.linearlayout_room_widget, View.VISIBLE);
                } catch (NoSuchElementException e) {
                    remoteViews.setTextViewText(R.id.textView_room_widget_name, context.getString(R.string.room_not_found));
                    remoteViews.setViewVisibility(R.id.linearlayout_room_widget, View.GONE);
                }
            } catch (NoSuchElementException e) {
                remoteViews.setTextViewText(R.id.textView_room_widget_name, context.getString(R.string.missing_widget_data));
                remoteViews.setViewVisibility(R.id.linearlayout_room_widget, View.GONE);
            } catch (Exception e) {
                Timber.e(e);
                remoteViews.setTextViewText(R.id.textView_room_widget_name, context.getString(R.string.unknown_error));
                remoteViews.setViewVisibility(R.id.linearlayout_room_widget, View.GONE);
            }
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Timber.d("Deleting Room Widgets: " + Arrays.toString(appWidgetIds));
        for (int appWidgetId : appWidgetIds) {
            try {
                persistanceHandler.deleteRoomWidget(appWidgetId);
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        super.onDeleted(context, appWidgetIds);
    }
}
