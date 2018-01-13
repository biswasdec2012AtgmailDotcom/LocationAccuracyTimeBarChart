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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LocationUpdateService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{
    private Timer timer;
    private long mLastClickTime = 0;

    // Google client to interact with Google API
    private static GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    static int UPDATE_INTERVAL =5000; // 5 sec
    static int FASTEST_INTERVAL = 5000; // 5 sec
    static int DISPLACEMENT = 5; // 5 meters
    Intent i;
    @Override
    public void onCreate()
    {
        super.onCreate();
        startTimer();
    }
    @Override
    public IBinder onBind(Intent intent) {
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
        try
        {
            if (location != null)
            {
                double lat = location.getLatitude();
                double longi = location.getLongitude();
                int accuracy = (int) (location.getAccuracy());
                long locTimeStamp = location.getTime();
                sendLocation(locTimeStamp, lat, longi, accuracy);
            }
        } catch (Exception e)
        {

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
        {
            mGoogleApiClient.connect();
        }
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
        } catch (Exception e)
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
        } catch (Exception e)
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
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
        } catch (Exception e)
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
        } catch (Exception e)
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
        } catch (Exception e)
        {

        }
    }

    /**
     * Method to display the location on UI
     */
    private void sendLocation(long locTimeStamp, double lat, double longi, double accuracy)
    {
        i = new Intent();
        i.putExtra("accuracy",accuracy);
        i.setAction(Constants.CUSTOM_INTENT);
        sendBroadcast(i);
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
                        if (!isRunning(getApplicationContext()))
                        {

                        }
                    }
                }, 10, 5 * 1000);
            }
        } catch (Exception e)
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
                    double lat = mLastLocation.getLatitude();
                    double longi = mLastLocation.getLongitude();
                    int accuracy = (int) (mLastLocation.getAccuracy());
                    long locTimeStamp = mLastLocation.getTime();
                    sendLocation(locTimeStamp, lat, longi, accuracy);
                }
            }
        } catch (Exception e)
        {

        }
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
        } catch (Exception e)
        {
            return false;
        }
        return false;
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
        } catch (Exception e)
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