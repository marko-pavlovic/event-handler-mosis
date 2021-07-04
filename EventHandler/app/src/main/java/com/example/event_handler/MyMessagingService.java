package com.example.event_handler;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import static androidx.core.app.NotificationCompat.PRIORITY_DEFAULT;

public class MyMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyMessagingService";
    private static final String CHANNEL_ID = "event_handler";
    private static final String CHANNEL_NAME = "Event handler";
    private static final String CHANNEL_DESC = "Event handler Notifications";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel channel=new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(CHANNEL_DESC);
        NotificationManager manager=getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        displayNotification(getApplicationContext(),remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody(),remoteMessage.getData());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

    }

    private static void displayNotification(Context context, String title, String body, Map data) {
        PendingIntent pendingIntent = null;
        if(data.containsKey("event")) {
            Intent intent=new Intent(context, EventActivity.class);
            intent.putExtra("key",data.get("event").toString());
            pendingIntent=PendingIntent.getActivity(context,100,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        }
        else if(data.containsKey("friend")){
            Intent intent=new Intent(context, ProfileActivity.class);
            intent.putExtra("type","loggedIn");
            pendingIntent=PendingIntent.getActivity(context,100,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        }
        NotificationCompat.Builder builder=new NotificationCompat.Builder(context,CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.app_logo)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat=NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1,builder.build());
    }
}
