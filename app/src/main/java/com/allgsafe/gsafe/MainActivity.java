package com.allgsafe.gsafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private CardView add_contacts, Sos,  settings, map;
    private FirebaseAuth fAuth;
    private static final int RESULT_PICK_CONTACT = 1;
    private String my_id, token;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private AppUpdateManager appUpdateManager;
    private int RequestUpdate = 1;
    private Dialog dialog, logout_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.add_contact_dialog);
        dialog.setCancelable(false);

        Button ok = dialog.findViewById(R.id.ok);

        logout_dialog = new Dialog(MainActivity.this);
        logout_dialog.setContentView(R.layout.logout_dialog);
        logout_dialog.setCancelable(false);

        Button yes = logout_dialog.findViewById(R.id.yes);
        Button no = logout_dialog.findViewById(R.id.no);


        fAuth = FirebaseAuth.getInstance();
        my_id = fAuth.getCurrentUser().getUid();

        DatabaseReference stateRef = FirebaseDatabase.getInstance().getReference("NearByNotificationState");
        stateRef.child(my_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                }
                else {
                    stateRef.child(my_id).child("state").setValue("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fAuth.signOut();
                Intent intent = new Intent(MainActivity.this , LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout_dialog.dismiss();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SOSActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            token = Objects.requireNonNull(task.getResult()).getToken();
                            updateAndUploadToken(token);

                        }

                    }
                });

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.SEND_SMS,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE}, 1);

        add_contacts = findViewById(R.id.add_contacts);
        Sos = findViewById(R.id.Sos);
        settings = findViewById(R.id.settings);
        map = findViewById(R.id.map);


        appUpdateManager = AppUpdateManagerFactory.create(this);

        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new com.google.android.play.core.tasks.OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if ((result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) && result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){

                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                result, AppUpdateType.IMMEDIATE,
                                MainActivity.this,
                                RequestUpdate);

                    }
                    catch (IntentSender.SendIntentException e){
                        e.printStackTrace();
                    }
                }
            }
        });


        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AllAudioLinksActivity.class);
                startActivity(intent);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, profileActivity.class);
                startActivity(intent);
            }
        });

        add_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, RESULT_PICK_CONTACT);
            }
        });

        Sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SOSActivity.class);
                startActivity(intent);
            }
        });
    }
//
//    private void checkLocationPermission() {
//        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){
//            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
//        }
//    }


    private void updateAndUploadToken(String token) {
        DatabaseReference updateToken = FirebaseDatabase.getInstance().getReference("Tokens");
        updateToken.child(my_id).child("token").setValue(token);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==RESULT_OK){
            switch (requestCode){
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;
            }
        }
        else {
            Toast.makeText(this, "Failed To Add", Toast.LENGTH_SHORT).show();
        }

    }

    private void contactPicked(Intent data) {
        Cursor cursor = null;

        try {
            String phoneNo = null;
            String Name = null;
            Uri uri = data.getData();
            cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int Cname = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            phoneNo = cursor.getString(phoneIndex);
            Name = cursor.getString(Cname);
            String timestamp = String.valueOf(System.currentTimeMillis());


            HashMap<Object, String> haspmap = new HashMap<>();

            haspmap.put("Mobile_Number", phoneNo);
            haspmap.put("Contact_Name", Name);
            haspmap.put("uid", my_id);
            haspmap.put("cId", timestamp);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("All Contacts");
            ref.child(my_id).child(timestamp).setValue(haspmap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    dialog.show();
//                    Intent intent = new Intent(MainActivity.this, SOSActivity.class);
//
//                    startActivity(intent);
//                    Toast.makeText(MainActivity.this, "Contact add...", Toast.LENGTH_SHORT).show();
                }
            });

//            Toast.makeText(this, ""+phoneNo+ " " +Name, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();

    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.some_menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.item1:

                Intent intent1 = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent1);

                return true;
            case R.id.item2:
                Intent intent2 = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(intent2);
//                Toast.makeText(this, "Help", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.item3:
                Intent intent3 = new Intent(MainActivity.this, PrivacyActivity.class);
                startActivity(intent3);
//                Toast.makeText(this, "Privacy", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.item5:
                logout_dialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new com.google.android.play.core.tasks.OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if (result.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){

                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                result,
                                AppUpdateType.IMMEDIATE,
                                MainActivity.this,
                                RequestUpdate
                        );
                    }
                    catch (IntentSender.SendIntentException e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    private boolean islOcationRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null){
            for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)){
                if (LocationService.class.getName().equals(serviceInfo.service.getClassName())){
                    if (serviceInfo.foreground){
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1){
            if (grantResults.length > 0 && grantResults[3] == PackageManager.PERMISSION_GRANTED){
                startLocationService();
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationService() {
        if (!islOcationRunning()){
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            startService(intent);
        }

    }

//
//    private void uploadLocationInFirebase(double latitude, double longitude) {
//         DatabaseReference uploadLocationRef = FirebaseDatabase.getInstance().getReference("Users_location");
//
//        final Map data = new HashMap();
//        data.put("Longitude", longitude);
//        data.put("Latitude", latitude);
//        data.put("uid", my_id);
//
//         uploadLocationRef.child(my_id).setValue(data);
//    }
}