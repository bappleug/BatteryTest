package com.lexing.batterytest.batteryhelper;

/**
 * Created by Ray on 2017/1/18.
 */

public interface IBatteryStatsConst {

    /**
     * A constant indicating a partial wake lock timer.
     */
    int WAKE_TYPE_PARTIAL = 0;

    /**
     * A constant indicating a full wake lock timer.
     */
    int WAKE_TYPE_FULL = 1;

    /**
     * A constant indicating a window wake lock timer.
     */
    int WAKE_TYPE_WINDOW = 2;

    /**
     * A constant indicating a sensor timer.
     */
    int SENSOR = 3;

    /**
     * A constant indicating a a wifi running timer
     */
    int WIFI_RUNNING = 4;

    /**
     * A constant indicating a full wifi lock timer
     */
    int FULL_WIFI_LOCK = 5;

    /**
     * A constant indicating a wifi scan
     */
    int WIFI_SCAN = 6;

    /**
     * A constant indicating a wifi multicast timer
     */
    int WIFI_MULTICAST_ENABLED = 7;

    /**
     * A constant indicating a video turn on timer
     */
    int VIDEO_TURNED_ON = 8;

    /**
     * A constant indicating a vibrator on timer
     */
    int VIBRATOR_ON = 9;

    /**
     * A constant indicating a foreground activity timer
     */
    int FOREGROUND_ACTIVITY = 10;

    /**
     * A constant indicating a wifi batched scan is active
     */
    int WIFI_BATCHED_SCAN = 11;

    /**
     * A constant indicating a process state timer
     */
    int PROCESS_STATE = 12;

    /**
     * A constant indicating a sync timer
     */
    int SYNC = 13;

    /**
     * A constant indicating a job timer
     */
    int JOB = 14;

    /**
     * A constant indicating an audio turn on timer
     */
    int AUDIO_TURNED_ON = 15;

    /**
     * Include all of the data in the stats, including previously saved data.
     */
    int STATS_SINCE_CHARGED = 0;

    /**
     * Include only the current run in the stats.
     */
    int STATS_CURRENT = 1;

    /**
     * Include only the run since the last time the device was unplugged in the stats.
     */
    int STATS_SINCE_UNPLUGGED = 2;

    // NOTE: Update this list if you add/change any stats above.
    // These characters are supposed to represent "total", "last", "current",
    // and "unplugged". They were shortened for efficiency sake.
    String[] STAT_NAMES = { "l", "c", "u" };

    int NUM_SCREEN_BRIGHTNESS_BINS = 5;

    int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    int SIGNAL_STRENGTH_POOR = 1;
    int SIGNAL_STRENGTH_MODERATE = 2;
    int SIGNAL_STRENGTH_GOOD = 3;
    int SIGNAL_STRENGTH_GREAT = 4;
    int NUM_SIGNAL_STRENGTH_BINS = 5;
    String[] SIGNAL_STRENGTH_NAMES = {
            "none", "poor", "moderate", "good", "great"
    };
}
