package com.allgsafe.gsafe;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.allgsafe.gsafe.sendPushNotification.FcmNotificationsSender;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.StringTokenizer;

public class ScreenOnOffReceiver extends BroadcastReceiver {

    public static final String SCREEN_TOGGLE_TAG = "SCREEN_TOGGLE_TAG";
    static FusedLocationProviderClient fusedLocationProviderClient;
    static DatabaseReference contactRef, NameRef;
    static String my_id;
    private static FirebaseAuth fAuth;
    private static StorageTask storageTask;
    public static MediaRecorder  mediaRecorder = new MediaRecorder();
    private static File file;
    private static StorageReference mStorage;
    private int count = 0;
    private static Context mContext;
    public static double latitude;
    public static double longitude;
    public static double latitude2;
    public static double longitude2;


    public static Handler mHandler = new Handler();


    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;

        count++;

            if (count == 4){

                Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = new long[]{0, 400, 200, 400};
                // Only perform this pattern one time (-1 means "do not repeat")
                v.vibrate(pattern, -1);
                getName();
                Log.d(SCREEN_TOGGLE_TAG, "Volume Key is Pressed."+ count);
                mFunctionRepeat.run();
                AudioRepeat.run();
                count = 0;
            }

    }

    private void getName() {
        fAuth = FirebaseAuth.getInstance();
        my_id = fAuth.getCurrentUser().getUid();
        NameRef = FirebaseDatabase.getInstance().getReference("Users").child(my_id);
        NameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String Name = snapshot.child("name").getValue().toString();

                contactRef = FirebaseDatabase.getInstance().getReference().child("All Contacts").child(my_id);

                contactRef.orderByChild("uid").equalTo(my_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){

                            String mobileNumber = ""+ds.child("Mobile_Number").getValue().toString();

                            StringTokenizer st=new StringTokenizer(mobileNumber,",");
                            while (st.hasMoreElements()){
                                String tempMobileNumber = (String)st.nextElement();
                                if (tempMobileNumber.length()>0){

                                    String messageWithname = Name + " "+ "is in trouble/emergency, Please follow these links as soon as possible.";
                                    SmsManager smsManager = SmsManager.getDefault();
                                    StringBuffer smsBody = new StringBuffer();
                                    smsBody.append(Uri.parse(messageWithname));
                                    SmsManager.getDefault().sendTextMessage(tempMobileNumber, null, smsBody.toString(), null, null);
                                    Log.d(SCREEN_TOGGLE_TAG, "Location Sent Successfully.");

                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static Runnable mFunctionRepeat = new Runnable() {
        @Override
        public void run() {
            sendLocation(mContext);
            Log.d(SCREEN_TOGGLE_TAG, "Code Repeat");
            mHandler.postDelayed(this, 60000);
        }
    };

    public static Runnable AudioRepeat = new Runnable() {
        @Override
        public void run() {

            sendAudio(mContext);

            mHandler.postDelayed(this, 120000);
        }
    };

    private static void sendAudio(Context context) {

        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);

            long timestamp = System.currentTimeMillis();
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            file = new File(path, "/"+timestamp+".mp3");


            mediaRecorder.setOutputFile(file);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            mediaRecorder.setMaxDuration(20000);
            mediaRecorder.prepare();
            mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
                        uploadAudio(file);
                        mediaRecorder.stop();

                        Log.d(SCREEN_TOGGLE_TAG, "Recording stops. Limit reached");
                    }
                }
            });
            mediaRecorder.start();
            Log.d(SCREEN_TOGGLE_TAG, "Recording start");
        }

        catch (Exception e){
            e.printStackTrace();
            Log.d(SCREEN_TOGGLE_TAG, "error"+e);
        }
    }

    private static void uploadAudio(File audio) {
        mStorage = FirebaseStorage.getInstance().getReference();
        fAuth = FirebaseAuth.getInstance();
        my_id = fAuth.getCurrentUser().getUid();
        contactRef = FirebaseDatabase.getInstance().getReference().child("All Contacts").child(my_id);
        final String timestamp = String.valueOf(System.currentTimeMillis());

        Uri uri = Uri.fromFile(audio);
        StorageReference filepath = mStorage.child("Audio").child(uri.getLastPathSegment());
        StorageMetadata storageMetadata = new StorageMetadata.Builder()
                .setContentType("audio/mpeg")
                .build();

        storageTask = filepath.putFile(uri, storageMetadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uritask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uritask.isSuccessful());

                String downloaduri = uritask.getResult().toString();

                        HashMap<Object, String> haspmap = new HashMap<>();

                        haspmap.put("url", downloaduri);
                        haspmap.put("uid", my_id);
                        haspmap.put("UploadTime", timestamp);

                        DatabaseReference audioRef = FirebaseDatabase.getInstance().getReference("Audio");

                        audioRef.child(my_id).child(timestamp).setValue(haspmap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                contactRef.orderByChild("uid").equalTo(my_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds: snapshot.getChildren()){

                                            String mobileNumber = ""+ds.child("Mobile_Number").getValue().toString();

                                            StringTokenizer st=new StringTokenizer(mobileNumber,",");
                                            while (st.hasMoreElements()){
                                                String tempMobileNumber = (String)st.nextElement();
                                                if (tempMobileNumber.length()>0){

                                                    SmsManager smsManager = SmsManager.getDefault();
                                                    StringBuffer smsBody = new StringBuffer();
                                                    smsBody.append(Uri.parse(downloaduri));
                                                    SmsManager.getDefault().sendTextMessage(tempMobileNumber, null, smsBody.toString(), null, null);
                                                    Log.d(SCREEN_TOGGLE_TAG, "Recording sent");

                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        });

            }
        });
    }

    private static void sendLocation(Context context) {

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation(mContext);

        } else {
//            ActivityCompat.requestPermissions(SendCurrentActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 44);
        }
    }

    @SuppressLint("MissingPermission")
    private static void getCurrentLocation(Context context) {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);

        LocationManager locationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){

            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {

                    LocationRequest locationRequest = new LocationRequest()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10000)
                            .setFastestInterval(1000)
                            .setNumUpdates(1);

                    LocationCallback locationCallback = new LocationCallback(){
                        @Override
                        public void onLocationResult(LocationResult locationResult) {

                            Location location1 = locationResult.getLastLocation();

                            latitude = location1.getLatitude();
                             longitude = location1.getLongitude();

                            sendMessage(latitude, longitude);
                            sendNotification();
                        }
                    };

                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

                }
            });
        }else {
            mContext.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        }
    }

    private static void sendNotification() {
        DatabaseReference NotifiRef = FirebaseDatabase.getInstance().getReference("Users_location");
        NotifiRef.orderByChild("state").equalTo("1").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds2 : snapshot.getChildren()){

                    latitude2 = Double.parseDouble(""+ds2.child("Latitude").getValue().toString());
                    longitude2 = Double.parseDouble(""+ds2.child("Longitude").getValue().toString());
                    String uid = ""+ds2.child("uid").getValue().toString();
                    float[] results = new float[1];
                    Location.distanceBetween(latitude, longitude, latitude2, longitude2, results);
                    float distance = results[0];
                    Log.d("Distance", ""+String.valueOf(distance));

                    if (distance < 1000){

                        if (uid.equals(my_id)){

                        }else {
                            sendNotificationToNear(uid, latitude2, longitude2);
                            Log.d("user_location", ""+latitude+""+longitude);
                        }

                    }

//                    if (uid.equals(my_id)){
//
//                    }else {
//
//
//
//                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private static void sendNotificationToNear(String uid, double latitude2, double longitude2) {
        DatabaseReference getToken = FirebaseDatabase.getInstance().getReference("Tokens");
        getToken.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userToken = snapshot.child("token").getValue().toString();
                String message = "Tab To go to direction";

                FcmNotificationsSender notificationsSender = new FcmNotificationsSender(userToken, "Emergency", message, mContext);
                notificationsSender.SendNotifications();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private static void sendMessage(double latitude, double longitude) {

        fAuth = FirebaseAuth.getInstance();
        my_id = fAuth.getCurrentUser().getUid();
        contactRef = FirebaseDatabase.getInstance().getReference().child("All Contacts").child(my_id);

        contactRef.orderByChild("uid").equalTo(my_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){

                    String mobileNumber = ""+ds.child("Mobile_Number").getValue().toString();

                    StringTokenizer st=new StringTokenizer(mobileNumber,",");
                    while (st.hasMoreElements()){
                        String tempMobileNumber = (String)st.nextElement();
                        if (tempMobileNumber.length()>0){

                            String message ="http://maps.google.com/?q="+latitude+","+longitude;
                            SmsManager smsManager = SmsManager.getDefault();
                            StringBuffer smsBody = new StringBuffer();
                            smsBody.append(Uri.parse(message));
                            SmsManager.getDefault().sendTextMessage(tempMobileNumber, null, smsBody.toString(), null, null);
                            Log.d(SCREEN_TOGGLE_TAG, "Location Sent Successfully.");

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
