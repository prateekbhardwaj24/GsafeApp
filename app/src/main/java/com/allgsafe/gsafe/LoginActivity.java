package com.allgsafe.gsafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText user_mobile, user_password;
    Button loginBtn;
    private TextView createTv, forgotPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        user_mobile = findViewById(R.id.user_mobile);
        user_password = findViewById(R.id.user_password);
        loginBtn = findViewById(R.id.loginBtn);
        createTv = findViewById(R.id.createTv);
        forgotPassword = findViewById(R.id.forgotPassword);
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCanceledOnTouchOutside(false);



        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Email = user_mobile.getText().toString();
                String Password = user_password.getText().toString();

                if (TextUtils.isEmpty(Email) && TextUtils.isEmpty(Password))
                {
                    Toast.makeText(LoginActivity.this, "Please Fill All Credentials ", Toast.LENGTH_SHORT).show();

                }

                else if (TextUtils.isEmpty(Email) || TextUtils.isEmpty(Password))
                {

                    if (TextUtils.isEmpty(Email))
                    {
                        Toast.makeText(LoginActivity.this, "Please enter email.. ", Toast.LENGTH_SHORT).show();
                    }
                    if (TextUtils.isEmpty(Password))
                    {
                        Toast.makeText(LoginActivity.this, "Please enter password.. ", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                    progressDialog.show();
                    progressDialog.setContentView(R.layout.progress_dialog);

                    mAuth.signInWithEmailAndPassword(Email , Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful())
                            {
                                Intent intent = new Intent(LoginActivity.this , MainActivity.class);
                                startActivity(intent);
                                finish();
                                Toast.makeText(LoginActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();

                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error ! "+message, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }

                        }
                    });
                }
            }
        });

        createTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, signupActivity.class);
                startActivity(intent);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText resetmail = new EditText(v.getContext());
                AlertDialog.Builder passwordresetdialog = new AlertDialog.Builder(v.getContext());
                passwordresetdialog.setTitle("Reset Password");
                passwordresetdialog.setMessage("Enter your email to received reset link");
                passwordresetdialog.setView(resetmail);

                passwordresetdialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // extract the mail and reset link
                        String mail = resetmail.getText().toString();
                        mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "Reset link send to your mail", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Error reset is not send" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordresetdialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // close dialog
                    }
                });
                passwordresetdialog.create().show();
            }
        });

    }

    @Override
    protected void onStart() {

        if (mAuth.getCurrentUser()!= null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

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