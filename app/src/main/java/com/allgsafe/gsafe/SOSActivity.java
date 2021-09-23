package com.allgsafe.gsafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SOSActivity extends AppCompatActivity {

    RecyclerView contactRecy;
    RecyclerView.LayoutManager manager;
    FirebaseRecyclerAdapter<contactModel, Contact_viewHolder> adapter;
    private String my_id;
    DatabaseReference contactRef;
    private FirebaseAuth fAuth;
    private ProgressDialog progressDialog;
    private ProgressBar progress;
    private TextView noContacts;
    private Dialog dialog;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_s_o_s);
        setTitle("SOS Contacts");
        contactRecy = findViewById(R.id.contactRecy);
        noContacts= findViewById(R.id.noContacts);
        progress = findViewById(R.id.progress);

        dialog = new Dialog(SOSActivity.this);
        dialog.setContentView(R.layout.delete_sos_dialog);
        dialog.setCancelable(false);

        Button ok = dialog.findViewById(R.id.ok);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
//        progress.setVisibility(View.VISIBLE);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        fAuth = FirebaseAuth.getInstance();
        my_id = fAuth.getCurrentUser().getUid();
        contactRef = FirebaseDatabase.getInstance().getReference().child("All Contacts").child(my_id);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        manager = new LinearLayoutManager(this);
        contactRecy.setLayoutManager(manager);


        FirebaseRecyclerOptions<contactModel> options = new FirebaseRecyclerOptions.Builder<contactModel>()
                .setQuery(contactRef , contactModel.class).build();
        adapter = new FirebaseRecyclerAdapter<contactModel, Contact_viewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull Contact_viewHolder holder, int position, @NonNull contactModel model) {

                String mobileNo = model.getMobile_Number();
                String contactName = model.getContact_Name();
                String Cid = model.getcId();


                holder.phoneNumber.setText(mobileNo);
                holder.Name.setText(contactName);

                holder.removeContact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteContact(Cid);
                    }
                });

            }

            @NonNull
            @Override
            public Contact_viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
                return new Contact_viewHolder(view);
            }
        };

        adapter.notifyDataSetChanged();
        adapter.startListening();
        contactRecy.setAdapter(adapter);
    }

    private void deleteContact(String cid) {

        Query query = FirebaseDatabase.getInstance().getReference("All Contacts").child(my_id).orderByChild("cId").equalTo(cid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                dialog.show();
//                Toast.makeText(SOSActivity.this, "Remove successfully....", Toast.LENGTH_SHORT).show();
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}