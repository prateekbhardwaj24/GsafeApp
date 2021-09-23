package com.allgsafe.gsafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class profileActivity extends AppCompatActivity {

    private TextView userName, userEmail;
    private SwitchCompat serviceSwitch, nearNotification;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, audioRef, NameRef;
    private String my_id;
    private ScreenOnOffReceiver screenOnOffReceiver = null;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        serviceSwitch = findViewById(R.id.serviceSwitch);
        nearNotification = findViewById(R.id.nearNotification);


        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference("Users");
        my_id = mAuth.getCurrentUser().getUid();
        audioRef = FirebaseDatabase.getInstance().getReference("Audio").child(my_id);

        screenOnOffReceiver = new ScreenOnOffReceiver();

        SharedPreferences sharedPreferences = getSharedPreferences("save", MODE_PRIVATE);
        serviceSwitch.setChecked(sharedPreferences.getBoolean("value", false));

        serviceSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (serviceSwitch.isChecked()){

                    SharedPreferences.Editor editor = getSharedPreferences("save", MODE_PRIVATE).edit();
                    editor.putBoolean("value", true);
                    editor.apply();
                    serviceSwitch.setChecked(true);

                    String edit = "Your SOS Service Is Activate";
                    Intent service = new Intent(profileActivity.this, ScreenOnOffBackgroundService.class);
                    service.putExtra("inputExtra", edit);
                    service.setAction(ScreenOnOffBackgroundService.ACTION_START_FOREGROUND_SERVICE);
                    startService(service);

                    Log.d(ScreenOnOffReceiver.SCREEN_TOGGLE_TAG, "Activity onCreate");

                }
                else {
                    SharedPreferences.Editor editor = getSharedPreferences("save", MODE_PRIVATE).edit();
                    editor.putBoolean("value", false);
                    editor.apply();
                    serviceSwitch.setChecked(false);
                    Intent service = new Intent(profileActivity.this, ScreenOnOffBackgroundService.class);
                    service.setAction(ScreenOnOffBackgroundService.ACTION_STOP_FOREGROUND_SERVICE);
                    startService(service);
//                    ScreenOnOffReceiver.mHandler.removeCallbacks();
                }
            }
        });

        getUserInfo();
        getNearSwitchState();

        nearNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nearNotification.isChecked()){
                    nearNotification.setChecked(true);
                    uploadState();
                }
                else {
                    nearNotification.setChecked(false);
                    uploadoffSate();
                }
            }
        });

    }

    private void getNearSwitchState() {
        DatabaseReference stateRef = FirebaseDatabase.getInstance().getReference("NearByNotificationState");
        stateRef.child(my_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String state = snapshot.child("state").getValue().toString();

                    if (state.equals("1")){
                        nearNotification.setChecked(true);
                    }
                    else {
                        nearNotification.setChecked(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadoffSate() {
        DatabaseReference stateRef = FirebaseDatabase.getInstance().getReference("NearByNotificationState");
        stateRef.child(my_id).child("state").setValue("0");
    }

    private void uploadState() {
        DatabaseReference stateRef = FirebaseDatabase.getInstance().getReference("NearByNotificationState");
        stateRef.child(my_id).child("state").setValue("1");
    }


    private void getUserInfo() {
        UsersRef.child(my_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String Name = snapshot.child("name").getValue().toString();
                String Email = snapshot.child("email").getValue().toString();

                userName.setText(Name);
                userEmail.setText(Email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
    protected void onDestroy() {
        super.onDestroy();
        Log.d(ScreenOnOffReceiver.SCREEN_TOGGLE_TAG, "Activity onDestroy");
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}