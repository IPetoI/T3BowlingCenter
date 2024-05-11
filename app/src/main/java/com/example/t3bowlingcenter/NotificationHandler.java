package com.example.t3bowlingcenter;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.t3bowlingcenter.Activitys.KezdolapActivity;
import com.example.t3bowlingcenter.Activitys.ProfilActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.TimeZone;

public class NotificationHandler {
    private static final String CHANNEL_ID = "bowling_notification_channel";

    private NotificationManager mManager;
    private Context mContext;

    private FirebaseUser felhasznalo = FirebaseAuth.getInstance().getCurrentUser();


    public NotificationHandler(Context context) {
        this.mContext = context;
        this.mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        createChannel();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"Bowling Notification", NotificationManager.IMPORTANCE_DEFAULT);

        channel.enableLights(true);
        channel.setLightColor(Color.WHITE);
        channel.setDescription("Értesítés a T3 Bowling Center-től.");
        this.mManager.createNotificationChannel(channel);
    }

    public void sendNotification(String uzenet) {
        Intent intent;
        if (felhasznalo.isAnonymous()) {
            intent = new Intent(mContext, KezdolapActivity.class);
        } else {
            intent = new Intent(mContext, ProfilActivity.class);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,0,intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentTitle("T3 Bowling Center")
                .setContentText(uzenet)
                .setSmallIcon(R.drawable.palyafoglalas_icon)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(uzenet))
                .setContentIntent(pendingIntent);

        this.mManager.notify(0, builder.build());
    }
}
