package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHandler {
    private static final String CHANNEL_ID="recipe_notification_channel";
    private final int NOTIFICATION_ID=0;
    private NotificationManager mManager;
    private Context mContext;

    public NotificationHandler(Context context) {
        this.mContext = context;
        this.mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel();

    }
    private void createChannel() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"Recipe notification", NotificationManager.IMPORTANCE_DEFAULT);

        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.RED);
        channel.setDescription("Notification from Recipe application");
        this.mManager.createNotificationChannel(channel);
    }
    public void send(String message) {
        Intent intent = new Intent(mContext,RecipeListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext,CHANNEL_ID).setContentTitle("Recipe Application").setContentText(message)
                .setSmallIcon(R.drawable.baseline_grid_view_24).setContentIntent(pendingIntent);
        this.mManager.notify(NOTIFICATION_ID,builder.build());
    }

    public void cancel() {
        this.mManager.cancel(NOTIFICATION_ID);
    }

}