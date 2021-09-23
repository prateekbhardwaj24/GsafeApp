package com.allgsafe.gsafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AllAudioLinksActivity extends AppCompatActivity {

    private RecyclerView linkdRecy;
    RecyclerView.LayoutManager manager;
    FirebaseRecyclerAdapter<LinkModel, Link_viewHolder> adapter;
    private String my_id;
    DatabaseReference linkRef, audioRef;
    private FirebaseAuth fAuth;
    private Button deactivate;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_audio_links);
        setTitle("Record Audio");
        linkdRecy = findViewById(R.id.linkdRecy);
        deactivate = findViewById(R.id.deactivate);

        dialog = new Dialog(AllAudioLinksActivity.this);
        dialog.setContentView(R.layout.deactivate_dialog);
        dialog.setCancelable(false);

        Button ok = dialog.findViewById(R.id.ok);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        fAuth = FirebaseAuth.getInstance();
        my_id = fAuth.getCurrentUser().getUid();
        linkRef = FirebaseDatabase.getInstance().getReference().child("Audio").child(my_id);
        audioRef = FirebaseDatabase.getInstance().getReference("Audio").child(my_id);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        manager = new LinearLayoutManager(this);
        linkdRecy.setLayoutManager(manager);

        FirebaseRecyclerOptions<LinkModel> options = new FirebaseRecyclerOptions.Builder<LinkModel>()
                .setQuery(linkRef , LinkModel.class).build();

        adapter = new FirebaseRecyclerAdapter<LinkModel, Link_viewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull Link_viewHolder holder, int position, @NonNull LinkModel model) {
                String RecordTime = model.getUploadTime();
                String Audio = model.getUrl();
                String Uid = model.getUid();

                String showPostTime = holder.getFormateDate(getApplicationContext() , RecordTime);


                holder.audioLinks.setText(Audio);
                holder.time.setText(showPostTime);

                holder.audioLinks.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager cm = (ClipboardManager)getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        cm.setText(Audio);
                        Toast.makeText(AllAudioLinksActivity.this, "Copied To Clipboard", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @NonNull
            @Override
            public Link_viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.links_layout, parent, false);
                return new Link_viewHolder(view);

            }
        };
        adapter.notifyDataSetChanged();
        adapter.startListening();
        linkdRecy.setAdapter(adapter);

        deactivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeactivateLinks();
            }
        });
    }

    private void DeactivateLinks() {
       audioRef.orderByChild("uid").equalTo(my_id).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               for (DataSnapshot ds: snapshot.getChildren()){

                   String url = ""+ds.child("url").getValue().toString();

                   StorageReference linkUrl = FirebaseStorage.getInstance().getReferenceFromUrl(url);

                   linkUrl.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void aVoid) {
                           Query query = FirebaseDatabase.getInstance().getReference("Audio").child(my_id).orderByChild("uid").equalTo(my_id);
                           query.addListenerForSingleValueEvent(new ValueEventListener() {
                               @Override
                               public void onDataChange(@NonNull DataSnapshot snapshot) {
                                   for (DataSnapshot ds: snapshot.getChildren()){
                                       ds.getRef().removeValue();
                                   }
                                   dialog.show();
                               }

                               @Override
                               public void onCancelled(@NonNull DatabaseError error) {

                               }
                           });
                       }
                   });

               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}