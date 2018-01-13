package com.biswas.locationaccuracytimebarchart.model;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.biswas.locationaccuracytimebarchart.MainActivity;
import com.biswas.locationaccuracytimebarchart.R;

public class NotificationBroadcastManager extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        double accuracy = intent.getExtras().getDouble("accuracy",0);
        displayNotification(context, accuracy);
    }

    public void displayNotification(Context context, double accuracy)
    {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Create Notification using NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context)
                // Set Icon
                .setSmallIcon(R.mipmap.ic_launcher_round)
                // Set Ticker Message
                .setTicker(""+accuracy)
                // Set Title
                .setContentTitle(context.getString(R.string.accuracy))
                // Set Text
                .setContentText("Accuracy=" + accuracy + " meter")
                // Add an Action Button below Notification
                .addAction(R.mipmap.ic_launcher, "Go to App", pIntent)
                // Set PendingIntent into Notification
                .setContentIntent(pIntent)
                // Dismiss Notification
                .setAutoCancel(true)
      .setSound(uri);
        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        notificationmanager.notify(0, builder.build());

    }

    // Check for network availability
    private boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

}