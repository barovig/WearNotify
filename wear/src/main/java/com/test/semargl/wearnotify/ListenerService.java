package com.test.semargl.wearnotify;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;


import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by semargl on 06/09/16.
 */
public class ListenerService extends WearableListenerService {

    private static final String WEARABLE_DATA_PATH = "/wearable_data";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        DataMap dataMap;
        for (DataEvent event : dataEvents) {

            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                //String path = event.getDataItem().getUri().getPath();
                //if (path.equals(WEARABLE_DATA_PATH)) {}
                dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                String text = dataMap.getString("package");
                Log.v("WEARNOTIFY", "package name received on watch: " + text);
                sendLocalNotification(dataMap);
            }
        }
    }

    private void sendLocalNotification(DataMap dataMap) {
        int notificationId = 001;

        try {
            String packageName = dataMap.getString("package");
            // Create a pending intent that starts this wearable app
            Intent appIntent = getPackageManager().getLaunchIntentForPackage(packageName);
            if(appIntent == null)
                Log.v("WEARNOTIFY", "intent is null");

            // Add extra data for app startup or initialization, if available
            PendingIntent runAppIntent = PendingIntent.getActivity(this, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notify = new NotificationCompat.Builder(this)
                    .setContentTitle(dataMap.getString("title"))
                    .setContentText(dataMap.getString("body"))
                    .setSmallIcon(R.drawable.skype)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentIntent(runAppIntent)
                    .build();

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(notificationId, notify);
        }
        catch(Exception ex){
            Log.v("WEARNOTIFY", "Exception: "+ex.toString());
        }
    }
}
