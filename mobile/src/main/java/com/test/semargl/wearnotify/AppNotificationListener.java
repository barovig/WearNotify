package com.test.semargl.wearnotify;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

/**
 * Created by semargl on 06/09/16.
 */
public class AppNotificationListener extends NotificationListenerService implements
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener{


    //** Attributes
    public static ArrayList<String> packageList = new ArrayList<>();
    GoogleApiClient googleClient;
    //**

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        Log.v("WEARNOTIFY", "in notification posted");
        final String packageName = sbn.getPackageName();
        boolean hasName = false;
        for(String name : packageList) {
            Log.v("WEARNOTIFY", "Name in package list: "+name);
            if (packageName.contains(name.toLowerCase())){
                hasName = true;
                break;
            }
        }
        if( !TextUtils.isEmpty(packageName) && hasName)
        {
            // **LOG**
            Log.v("WEARNOTIFY", "String to send: "+packageName);

            DataMap notifyWearable = new DataMap();
            notifyWearable.putString("title", sbn.getNotification().extras.getString(Notification.EXTRA_TITLE));
            notifyWearable.putString("body", sbn.getNotification().extras.getString(Notification.EXTRA_TEXT));
            notifyWearable.putString("package", packageName);
            // Send to data layer
            new SendToDataLayerThread("/wearable_data", notifyWearable).start();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v("WEARNOTIFY", "Connected to wear");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v("WEARNOTIFY", "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v("WEARNOTIFY", "Connection failed");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        /// **DELETE ME AFTER UI IS WRITTEN
        packageList.add("skype");
        /// **END
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    class SendToDataLayerThread extends Thread {
        String path;
        DataMap dataMap;

        // Constructor for sending data objects to the data layer
        SendToDataLayerThread(String p, DataMap data) {
            path = p;
            dataMap = data;
        }

        public void run() {
            Log.v("WEARNOTIFY", "Sending data to wearable");
            // Connecto to DAL
            googleClient.connect();
            // Construct a DataRequest and send over the data layer
            PutDataMapRequest putDMR = PutDataMapRequest.create(path);
            putDMR.getDataMap().putAll(dataMap);
            PutDataRequest request = putDMR.asPutDataRequest();
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(googleClient, request).await();
            if (result.getStatus().isSuccess()) {
                Log.v("WEARNOTIFY", "DataMap: " + dataMap + " sent successfully to data layer ");
            }
            else {
                // Log an error
                Log.v("WEARNOTIFY", "ERROR: failed to send DataMap to data layer");
            }

            // Disconnect DAL
            if (null != googleClient && googleClient.isConnected()) {
                googleClient.disconnect();
            }
        }
    }
}
