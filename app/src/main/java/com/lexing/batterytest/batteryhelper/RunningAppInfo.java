package com.lexing.batterytest.batteryhelper;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

/**
 * Created by Ray on 2017/1/23.
 */

public class RunningAppInfo {
    public String name;           //a
    public String pkgName;        //b
    public boolean isValid;       //c
    public ArrayList<Integer> pids = new ArrayList<>();    //d
    public ApplicationInfo appInfo;   //e
    public boolean launchIntent;      //f
    public float cpuPercent;             //g
    public long cpuTime;         //h
    public long dataSent;              //i
    public long dateReceived;              //j

    public Drawable icon;
}
