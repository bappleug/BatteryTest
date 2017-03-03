package com.lexing.batterytest.batteryhelper;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Migrate from system PowerUsageSummary under android KITKAT, can't be used in higher versions.
 */
public class BatteryStatsHelper {

    private double mMinPercentOfTotal = 0;

    public BatteryStatsHelper() {
    }

    /**
     * 设置最小百分比，小于该值的程序将被过滤掉
     *
     * @param minPercentOfTotal
     */
    public void setMinPercentOfTotal(double minPercentOfTotal) {
        this.mMinPercentOfTotal = minPercentOfTotal;
    }

    public List<BatterySipper> getBatteryStats(Context context) {
        return getAppListCpuTime(context);
    }

    private List<BatterySipper> getAppListCpuTime(Context context) {
        final List<BatterySipper> list = new ArrayList<>();
        long totalTime = 0;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        HashMap<String, BatterySipper> templist = new HashMap<>();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || (runningApps != null && runningApps.size() >= 3)) {
            for (RunningAppProcessInfo info : runningApps) {
                final long time = getAppProcessTime(info.pid);
                String[] pkgNames = info.pkgList;
                if (pkgNames == null) {
                    if (templist.containsKey(info.processName)) {
                        BatterySipper sipper = templist.get(info.processName);
                        sipper.setValue(sipper.getValue() + time);
                    } else {
                        templist.put(info.processName, new BatterySipper(info.processName, time));
                    }
                    totalTime += time;
                } else {
                    for (String pkgName : pkgNames) {
                        if (templist.containsKey(pkgName)) {
                            BatterySipper sipper = templist.get(pkgName);
                            sipper.setValue(sipper.getValue() + time);
                        } else {
                            templist.put(pkgName, new BatterySipper(pkgName, time));
                        }
                        totalTime += time;
                    }
                }
            }
        } else {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            long endTime = System.currentTimeMillis();
            long beginTime = endTime - 1000 * 60;

            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime);

            if (stats != null) {
                for (UsageStats usageStats : stats) {
                    BatterySipper sipper = new BatterySipper(usageStats.getPackageName(), usageStats.getTotalTimeInForeground());
                    totalTime += usageStats.getTotalTimeInForeground();
                    list.add(sipper);
                }
            }
        }

        if (totalTime == 0) totalTime = 1;

        list.addAll(templist.values());
        for (int i = list.size() - 1; i >= 0; i--) {
            BatterySipper sipper = list.get(i);
            double percentOfTotal = sipper.getValue() * 100 / totalTime;
            if (percentOfTotal < mMinPercentOfTotal) {
                list.remove(i);
            } else {
                sipper.setPercent(percentOfTotal);
            }
        }

        Collections.sort(list, new Comparator<BatterySipper>() {
            @Override
            public int compare(BatterySipper object1, BatterySipper object2) {
                double d1 = object1.getPercentOfTotal();
                double d2 = object2.getPercentOfTotal();
                if (d1 - d2 < 0) {
                    return 1;
                } else if (d1 - d2 > 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        return list;
    }

    private long getAppProcessTime(int pid) {
        FileInputStream in = null;
        String ret = null;
        try {
            in = new FileInputStream("/proc/" + pid + "/stat");
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            ret = os.toString();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (ret == null) {
            return 0;
        }

        String[] s = ret.split(" ");
        if (s.length < 17) {
            return 0;
        }

        final long utime = string2Long(s[13]);
        final long stime = string2Long(s[14]);
        final long cutime = string2Long(s[15]);
        final long cstime = string2Long(s[16]);

        return utime + stime + cutime + cstime;
    }

    private long string2Long(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            //do nothing
        }
        return 0;
    }
}
