package com.allgsafe.gsafe;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {

public static final String CHANNEL_ID = "SOS Activate";


@Override
public void onCreate() {
        super.onCreate();

        CreateNotificationChannel();
        }


private void CreateNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        NotificationChannel service = new NotificationChannel(CHANNEL_ID, "Notification", NotificationManager.IMPORTANCE_NONE);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(service);
        }
        }
        }