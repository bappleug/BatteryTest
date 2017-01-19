package com.lexing.batterytest.batteryhelper.v18;

import android.content.Context;

import com.lexing.batterytest.batteryhelper.Wrapper;

/**
 * Created by Ray on 2017/1/16.
 */

public class PowerProfileV18 extends Wrapper {

    /**
     * No power consumption, or accounted for elsewhere.
     */
    public static final String POWER_NONE = "none";

    /**
     * Power consumption when CPU is in power collapse mode.
     */
    public static final String POWER_CPU_IDLE = "cpu.idle";

    /**
     * Power consumption when CPU is awake (when a wake lock is held).  This
     * should be 0 on devices that can go into full CPU power collapse even
     * when a wake lock is held.  Otherwise, this is the power consumption in
     * addition to POWERR_CPU_IDLE due to a wake lock being held but with no
     * CPU activity.
     */
    public static final String POWER_CPU_AWAKE = "cpu.awake";

    /**
     * Power consumption when CPU is in power collapse mode.
     */
    public static final String POWER_CPU_ACTIVE = "cpu.active";

    /**
     * Power consumption when WiFi driver is scanning for networks.
     */
    public static final String POWER_WIFI_SCAN = "wifi.scan";

    /**
     * Power consumption when WiFi driver is on.
     */
    public static final String POWER_WIFI_ON = "wifi.on";

    /**
     * Power consumption when WiFi driver is transmitting/receiving.
     */
    public static final String POWER_WIFI_ACTIVE = "wifi.active";

    /**
     * Power consumption when GPS is on.
     */
    public static final String POWER_GPS_ON = "gps.on";

    /**
     * Power consumption when Bluetooth driver is on.
     */
    public static final String POWER_BLUETOOTH_ON = "bluetooth.on";

    /**
     * Power consumption when Bluetooth driver is transmitting/receiving.
     */
    public static final String POWER_BLUETOOTH_ACTIVE = "bluetooth.active";

    /**
     * Power consumption when Bluetooth driver gets an AT command.
     */
    public static final String POWER_BLUETOOTH_AT_CMD = "bluetooth.at";

    /**
     * Power consumption when screen is on, not including the backlight power.
     */
    public static final String POWER_SCREEN_ON = "screen.on";

    /**
     * Power consumption when cell radio is on but not on a call.
     */
    public static final String POWER_RADIO_ON = "radio.on";

    /**
     * Power consumption when cell radio is hunting for a signal.
     */
    public static final String POWER_RADIO_SCANNING = "radio.scanning";

    /**
     * Power consumption when talking on the phone.
     */
    public static final String POWER_RADIO_ACTIVE = "radio.active";

    /**
     * Power consumption at full backlight brightness. If the backlight is at
     * 50% brightness, then this should be multiplied by 0.5
     */
    public static final String POWER_SCREEN_FULL = "screen.full";

    /**
     * Power consumed by the audio hardware when playing back audio content. This is in addition
     * to the CPU power, probably due to a DSP and / or amplifier.
     */
    public static final String POWER_AUDIO = "dsp.audio";

    /**
     * Power consumed by any media hardware when playing back video content. This is in addition
     * to the CPU power, probably due to a DSP.
     */
    public static final String POWER_VIDEO = "dsp.video";

    public static final String POWER_CPU_SPEEDS = "cpu.speeds";

    /**
     * Battery capacity in milliAmpHour (mAh).
     */
    public static final String POWER_BATTERY_CAPACITY = "battery.capacity";

    private PowerProfileV18(Object wrappedObj) {
        super(wrappedObj);
    }

    @Override
    protected String className() {
        return "com.android.internal.os.PowerProfile";
    }

    public static PowerProfileV18 create(Context context) {
//			mPowerProfile = new PowerProfile(context);
        try{
            Object powerProfile = Class.forName("com.android.internal.os.PowerProfile")
                    .getConstructor(Context.class)
                    .newInstance(context);
            return new PowerProfileV18(powerProfile);
        } catch (Exception e){
            handleException(e);
        }
        return null;
    }

    public double getAveragePower(String type){
        return (double) invoke("getAveragePower", type);
    }

    public double getAveragePower(String type, int level){
        return (double) invoke("getAveragePower", type, level);
    }

    public int getNumSpeedSteps(){
        return (int) invoke("getNumSpeedSteps");
    }
}
