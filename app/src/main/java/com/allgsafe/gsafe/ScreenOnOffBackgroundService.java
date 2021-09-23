package com.allgsafe.gsafe;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ScreenOnOffBackgroundService extends Service {


    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";

    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    private ScreenOnOffReceiver screenOnOffReceiver = null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(ACTION_START_FOREGROUND_SERVICE)){

            String EditName = intent.getStringExtra("inputExtra");
            Intent intent1 = new Intent(this, profileActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, 0);
            Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
                    .setOngoing(true)
                    .setContentTitle("SOS Activate")
                    .setContentText(EditName)
                    .setSmallIcon(R.drawable.ic_baseline_security_24)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);

            // Create an IntentFilter instance.
            IntentFilter intentFilter = new IntentFilter();

            // Add network connectivity change action.
//        intentFilter.addAction("android.intent.action.SCREEN_ON");
//        intentFilter.addAction("android.intent.action.SCREEN_OFF");

//        intentFilter.addAction("android.intent.action.MEDIA_BUTTON");
            intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");

            // Set broadcast receiver priority.
            intentFilter.setPriority(100);

            // Create a network change broadcast receiver.
            screenOnOffReceiver = new ScreenOnOffReceiver();

            // Register the broadcast receiver with the intent filter object.
            registerReceiver(screenOnOffReceiver, intentFilter);

            Log.d(ScreenOnOffReceiver.SCREEN_TOGGLE_TAG, "Service onCreate: screenOnOffReceiver is registered.");
        }
        else if (intent.getAction().equals( ACTION_STOP_FOREGROUND_SERVICE)){
            stopForeground(true);
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
//
        unregisterReceiver(screenOnOffReceiver);

//        ScreenOnOffReceiver.mediaRecorder.stop();
//        ScreenOnOffReceiver.mediaRecorder.release();
        ScreenOnOffReceiver.mHandler.removeCallbacks(ScreenOnOffReceiver.mFunctionRepeat);
        ScreenOnOffReceiver.mHandler.removeCallbacks(ScreenOnOffReceiver.AudioRepeat);
        Log.d(ScreenOnOffReceiver.SCREEN_TOGGLE_TAG, "Service onDestroy: screenOnOffReceiver is unregistered.");
        super.onDestroy();

        // Unregister screenOnOffReceiver when destroy.

    }

}
