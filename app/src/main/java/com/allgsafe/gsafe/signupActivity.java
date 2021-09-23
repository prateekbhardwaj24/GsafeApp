package com.allgsafe.gsafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class signupActivity extends AppCompatActivity {

    EditText user_name, user_email, user_password;
    Button create_accountBtn;
    private TextView loginTv;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private String CurrentUserId;
    private ProgressDialog progressDialog;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        user_name = findViewById(R.id.user_name);
        user_email = findViewById(R.id.user_mobile);
        user_password = findViewById(R.id.user_password);
        loginTv = findViewById(R.id.loginTv);

        progressDialog = new ProgressDialog(signupActivity.this);
        dialog = new Dialog(signupActivity.this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setCancelable(false);

        Button ok = dialog.findViewById(R.id.ok);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(signupActivity.this, LoginActivity.class);
                startActivity(intent);
                dialog.dismiss();
                finish();
            }
        });

        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCanceledOnTouchOutside(false);

        create_accountBtn = findViewById(R.id.create_accountBtn);
        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference();


        create_accountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uName = user_name.getText().toString().trim();
                String uPassword = user_password.getText().toString().trim();
                String uEmail = user_email.getText().toString();

                if (TextUtils.isEmpty(uName) && TextUtils.isEmpty(uPassword) && TextUtils.isEmpty(uEmail))
                {
                    Toast.makeText(signupActivity.this, "Please Fill All Credentials ", Toast.LENGTH_SHORT).show();

                }
                else if (TextUtils.isEmpty(uName) || TextUtils.isEmpty(uPassword) || TextUtils.isEmpty(uEmail)){

                    if (TextUtils.isEmpty(uName))
                    {
                        Toast.makeText(signupActivity.this, "Please enter username.. ", Toast.LENGTH_SHORT).show();
                    }
                    if (TextUtils.isEmpty(uEmail))
                    {
                        Toast.makeText(signupActivity.this, "Please enter valid Mobile Number.. ", Toast.LENGTH_SHORT).show();
                    }
                    if (TextUtils.isEmpty(uPassword))
                    {
                        Toast.makeText(signupActivity.this, "Please enter password.. ", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                    progressDialog.show();
                    progressDialog.setContentView(R.layout.progress_dialog);
                    HashMap<Object, String> haspmap = new HashMap<>();
                    haspmap.put("email", uEmail);
                    haspmap.put("name", uName);

                    mAuth.createUserWithEmailAndPassword(uEmail , uPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {

                                mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){
                                            CurrentUserId = mAuth.getCurrentUser().getUid();
                                            UsersRef.child("Users").child(CurrentUserId).setValue(haspmap);
//                                            Toast.makeText(signupActivity.this, "Account created Successfully! Please Verify Your Email", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                            openDialog();
                                        }
                                        else {
                                            Toast.makeText(signupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });


                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(signupActivity.this, "Error ! " + message, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();

                            }
                        }
                    });

                }
            }
        });

        loginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(signupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    private void openDialog() {
        dialog.show();
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
}