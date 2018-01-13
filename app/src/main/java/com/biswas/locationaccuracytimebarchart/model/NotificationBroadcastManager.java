package com.biswas.locationaccuracytimebarchart.model;

import android.app.ActivityManager;
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

import com.biswas.locationaccuracytimebarchart.util.Constants;
import com.biswas.locationaccuracytimebarchart.view.MainActivity;
import com.biswas.locationaccuracytimebarchart.R;
import com.biswas.locationaccuracytimebarchart.viewmodel.TimeAccuracy;
import com.google.android.gms.plus.model.people.Person;

import java.util.ArrayList;
import java.util.List;

public class NotificationBroadcastManager extends BroadcastReceiver
{
    public static ArrayList<TimeAccuracy> timeAccuracyList=new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent)
    {
        int accuracy = intent.getExtras().getInt("accuracy", 0);
        if (accuracy <= Constants.ACCURACY_SCALE)
            displayNotification(context, accuracy);

        timeAccuracyList = (ArrayList<TimeAccuracy>) intent
                .getSerializableExtra("timeAccuracyList");
        if (isRunning(context))
            MainActivity.drawGraph(timeAccuracyList);
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
                .setTicker("" + accuracy)
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

    private boolean isRunning(Context ctx)
    {
        try
        {
            List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(Integer.MAX_VALUE);
            for (ActivityManager.RunningTaskInfo task : tasks)
            {
                if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
                    return true;
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return false;
    }

}