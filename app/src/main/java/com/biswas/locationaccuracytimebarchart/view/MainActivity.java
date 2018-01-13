package com.biswas.locationaccuracytimebarchart.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.biswas.locationaccuracytimebarchart.R;
import com.biswas.locationaccuracytimebarchart.model.LocationUpdateService;
import com.biswas.locationaccuracytimebarchart.model.NotificationBroadcastManager;
import com.biswas.locationaccuracytimebarchart.viewmodel.TimeAccuracy;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    public static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 2;
    private static BarChart barChart;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Dismiss Notification
        notificationmanager.cancel(0);
        if (checkPermission())
            startService();
        barChart = findViewById(R.id.barchart);
        barChart.animateY(4000);
        barChart.getAxisLeft().setAxisMinValue(0);
        barChart.getAxisRight().setAxisMinValue(0);
        drawGraph(NotificationBroadcastManager.timeAccuracyList);
        setXYaxisLabels();
    }

    public static void drawGraph(ArrayList<TimeAccuracy> timeAccuracyList)
    {
//        Toast.makeText(context, "size" + timeAccuracyList.size(), Toast.LENGTH_LONG).show();
        ArrayList<String> timeList = new ArrayList<>();
        ArrayList<BarEntry> accuracyList = new ArrayList<>();
        for (int i = 0; i < timeAccuracyList.size(); i++)
        {
            timeList.add(timeAccuracyList.get(i).getTime());
            accuracyList.add(new BarEntry(timeAccuracyList.get(i).getAccuracy(), i));
        }

        // creating dataset for Bar Group1
        IBarDataSet barDataSet = new BarDataSet(accuracyList, "Accuracy");

        ArrayList<IBarDataSet> bardataset = new ArrayList<>();  // combined all dataset into an arraylist
        bardataset.add(barDataSet);

        BarData data = new BarData(timeList, bardataset);
        barChart.clear();
        barChart.setData(data); // set the data and list of labels into chart
        barChart.setDescription("Location Accuracy and Time");  // set the description
    }

    private void setXYaxisLabels()
    {
        RelativeLayout rl_barchart = findViewById(R.id.rl_barchart);
        TextView xAxisName = new TextView(this);
        xAxisName.setText("Time");
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        params.setMargins(500, 0, 0, 40);

        VerticalTextView yAxisName = new VerticalTextView(this, null);
        yAxisName.setText("Accuracy");
        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params2.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        params2.setMargins(0, 700, 0, 40);

        rl_barchart.addView(xAxisName, params);
        rl_barchart.addView(yAxisName, params2);
    }

    private void startService()
    {
        startService(new Intent(this, LocationUpdateService.class));
    }

    public boolean checkPermission()
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) this, android.Manifest.permission.ACCESS_FINE_LOCATION))
                {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("Location permission is necessary to get current location!!!");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                    {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which)
                        {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else
                {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);
                }
                return false;
            } else
            {
                return true;
            }
        } else
        {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    startService();
                } else
                {
                    checkPermission();
                }
                break;
        }
    }
}
