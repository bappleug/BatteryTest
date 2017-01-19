package com.lexing.batterytest.batteryhelper;

import android.graphics.drawable.Drawable;

/**
 * Created by Ray on 2017/1/18.
 */
public class BatteryInfo implements Cloneable {

    private String pkgName;
    private String name;
    private Drawable icon;
    private double percentOfTotal;
    private BatterySipperWrapper.DrainTypeWrapper drainType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public double getPercentOfTotal() {
        return percentOfTotal;
    }

    public void setPercentOfTotal(double getPercentOfTotal) {
        this.percentOfTotal = getPercentOfTotal;
    }

    public BatterySipperWrapper.DrainTypeWrapper getDrainType() {
        return drainType;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public void setDrainType(BatterySipperWrapper.DrainTypeWrapper drainType) {
        this.drainType = drainType;
    }

    public BatteryInfo clone() {
        try {
            return (BatteryInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
