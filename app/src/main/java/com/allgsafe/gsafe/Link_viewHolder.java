package com.allgsafe.gsafe;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;

public class Link_viewHolder extends RecyclerView.ViewHolder {

    TextView audioLinks, time;
    public Link_viewHolder(@NonNull View itemView) {
        super(itemView);

        audioLinks = itemView.findViewById(R.id.audioLinks);
        time  = itemView.findViewById(R.id.time);

    }

    public String getFormateDate(Context applicationContext, String recordTime) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(Long.parseLong(recordTime));

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "EEEE, MMMM d, h:mm aa";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE) ) {
            return "Today " + DateFormat.format(timeFormatString, smsTime);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1  ){
            return "Yesterday " + DateFormat.format(timeFormatString, smsTime);
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return DateFormat.format(dateTimeFormatString, smsTime).toString();
        } else {
            return DateFormat.format("MMMM dd yyyy, h:mm aa", smsTime).toString();
        }
    }
}
