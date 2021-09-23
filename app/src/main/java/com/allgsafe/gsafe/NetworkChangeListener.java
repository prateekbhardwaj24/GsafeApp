package com.allgsafe.gsafe;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.allgsafe.gsafe.Utility.Common;

public class NetworkChangeListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Common.isConnectedToInternet(context)){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View layout_view = LayoutInflater.from(context).inflate(R.layout.network_check_dialog, null);
            builder.setView(layout_view);

            Button retryBtn = layout_view.findViewById(R.id.retryBtn);

            AlertDialog dialog = builder.create();
            dialog.show();;
            dialog.setCancelable(false);

            dialog.getWindow().setGravity(Gravity.CENTER);

            retryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    onReceive(context, intent);
                }
            });
        }
    }
}
