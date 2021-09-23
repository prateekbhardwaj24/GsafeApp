package com.allgsafe.gsafe;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Contact_viewHolder extends RecyclerView.ViewHolder {

    TextView Name, phoneNumber;
    Button removeContact;

    public Contact_viewHolder(@NonNull View itemView) {
        super(itemView);

        Name = itemView.findViewById(R.id.Name);
        phoneNumber = itemView.findViewById(R.id.phoneNumber);
        removeContact = itemView.findViewById(R.id.removeContact);
    }

}
