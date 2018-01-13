package com.biswas.locationaccuracytimebarchart.model;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.location.Location;

import android.os.Bundle;

import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.view.accessibility.AccessibilityEvent;

import com.biswas.locationaccuracytimebarchart.util.Constants;
import com.biswas.locationaccuracytimebarchart.viewmodel.TimeAccuracy;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class LocationUpdateService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private Timer timer;
    private long mLastClickTime = 0;

    // Google client to interact with Google API
    private static GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    private String strDateFormat = "HH:mm a";
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 5000; // 5 sec
    private static int FASTEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 5; // 5 meters
    private Intent i;
    private ArrayList<TimeAccuracy> timeAccuracyList;

    @Override
    public void onCreate()
    {
        super.onCreate();
        timeAccuracyList = new ArrayList<>();
        startTimer();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // Building the GoogleApi client
        buildGoogleApiClient();
        createLocationRequest();
        startLocationUpdates();
        return Service.START_STICKY;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (location != null)
        {
            int accuracy = (int) location.getAccuracy();
            long locTimeStamp = location.getTime();
            sendLocation(locTimeStamp, accuracy);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    public static Location getLastLocation()
    {
        Location mLastLocation = null;
        try
        {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            return mLastLocation;
        }
        catch (Exception e)
        {
            return mLastLocation;
        }
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient()
    {
        try
        {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
            mGoogleApiClient.connect();
        }
        catch (Exception e)
        {

        }
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest()
    {
        try
        {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        }
        catch (Exception e)
        {

        }
    }

    /**
     * Starting the location updates
     */
    protected void startLocationUpdates()
    {
        try
        {
            if (mGoogleApiClient.isConnected())
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        catch (Exception e)
        {

        }
    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates()
    {
        try
        {
            if (mGoogleApiClient.isConnected())
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        catch (Exception e)
        {

        }
    }

    /**
     * Method to display the location on UI
     */
    private void sendLocation(long locTimeStamp, int accuracy)
    {
        ArrayList<TimeAccuracy> timeAccuracyList = addLocationInfoToList(locTimeStamp, accuracy);
        if (accuracy <= Constants.ACCURACY_SCALE)
        {
            i = new Intent();
            i.putExtra("accuracy", accuracy);
            i.putExtra("timeAccuracyList", timeAccuracyList);
            i.setAction(Constants.CUSTOM_INTENT);
            sendBroadcast(i);
        }
    }

    private ArrayList<TimeAccuracy> addLocationInfoToList(long locTimeStamp, int accuracy)
    {
        Date date = new Date(locTimeStamp);
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat, Locale.ENGLISH);
        TimeAccuracy ta = new TimeAccuracy();
        ta.setTime(sdf.format(date));
        ta.setAccuracy(accuracy);
        if (timeAccuracyList == null)
            timeAccuracyList = new ArrayList<>();

        if (timeAccuracyList.size() > 6)
            timeAccuracyList.remove(0);
        timeAccuracyList.add(timeAccuracyList.size(), ta);
        return timeAccuracyList;
    }

    private void startTimer()
    {
        try
        {
            if (timer == null)
            {
                timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        sendLocationWheneverUserAtSameLocation();
                    }
                }, 10, 5 * 1000);
            }
        }
        catch (Exception e)
        {

        }
    }

    private void sendLocationWheneverUserAtSameLocation()
    {
        try
        {
            long lastClickTime = mLastClickTime;
            long now = System.currentTimeMillis();
            mLastClickTime = now;
            if (now - lastClickTime > 5000)
            {
                Location mLastLocation = getLastLocation();
                if (mLastLocation != null)
                {
                    int accuracy = (int) (mLastLocation.getAccuracy());
                    long locTimeStamp = mLastLocation.getTime();
                    sendLocation(locTimeStamp, accuracy);
                }
            }
        }
        catch (Exception e)
        {

        }
    }


    private void cancelTimer()
    {
        try
        {
            if (timer != null)
            {
                timer.cancel();
                timer.purge();
                timer = null;
            }
        }
        catch (Exception e)
        {

        }
    }

    @Override
    public boolean stopService(Intent name)
    {
        cancelTimer();
        return super.stopService(name);
    }

    @Override
    public void onDestroy()
    {
        cancelTimer();
        super.onDestroy();
    }
}