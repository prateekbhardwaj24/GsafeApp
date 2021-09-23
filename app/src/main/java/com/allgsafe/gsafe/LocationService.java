package com.allgsafe.gsafe;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LocationService extends Service {

    private String my_id;
    private FirebaseAuth fAuth;

    private LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null){
                double latitude1 = locationResult.getLastLocation().getLatitude();
                double longitude1 = locationResult.getLastLocation().getLongitude();
                uploadLocationInFirebase(latitude1, longitude1);
                Log.d("location_Update", latitude1+ ", "+ longitude1);
            }
        }
    };

    private void uploadLocationInFirebase(double latitude1, double longitude1) {
        fAuth = FirebaseAuth.getInstance();
        my_id = fAuth.getCurrentUser().getUid();

        DatabaseReference retreiveState = FirebaseDatabase.getInstance().getReference("NearByNotificationState");
        retreiveState.child(my_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String state = snapshot.child("state").getValue().toString();

                DatabaseReference uploadLocationRef = FirebaseDatabase.getInstance().getReference("Users_location");

                final Map data = new HashMap();
                data.put("Longitude", longitude1);
                data.put("Latitude", latitude1);
                data.put("uid", my_id);
                data.put("state", state);

                uploadLocationRef.child(my_id).setValue(data);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implememted");
    }
    @SuppressLint("MissingPermission")
    private void startLocationService(){
        String channelId = "location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setContentText("Running");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentTitle("Location Service");
        builder.setAutoCancel(false);
        builder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null){
                NotificationChannel notificationChannel = new NotificationChannel(channelId, "location service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("this is");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        startForeground(Constants.LOCATION_SERVICE_ID, builder.build());

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationService();
        return super.onStartCommand(intent, flags, startId);
    }
}
