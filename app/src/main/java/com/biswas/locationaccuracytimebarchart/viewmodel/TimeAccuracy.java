package com.biswas.locationaccuracytimebarchart.viewmodel;

import java.io.Serializable;

/**
 * Created by BISHWAJEET BISWAS on 13-01-2018.
 */

public class TimeAccuracy implements Serializable
{
    private String time;
    private int accuracy;

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public int getAccuracy()
    {
        return accuracy;
    }

    public void setAccuracy(int accuracy)
    {
        this.accuracy = accuracy;
    }
}
