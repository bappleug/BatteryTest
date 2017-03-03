package com.lexing.batterytest.batteryhelper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.lexing.common.utils.SPUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by Ray on 2017/1/20.
 */

public class AppStatsHelper {

    long lastUpdate = 0L;   //h
    List<RunningAppInfo> appWithoutSystem = new ArrayList<>();//f
    List<RunningAppInfo> appWithSystem = new ArrayList<>();//cpuPercent

    //List<RunningAppInfo> process.c.a(boolean, boolean)
    public List<RunningAppInfo> readAppStats(Context context, boolean collectDataCost) {
        PackageManager packageManager = context.getPackageManager();

        long currentTimeMillis = System.currentTimeMillis();
        boolean showSystem = (boolean) SPUtils.getParamDefault(context, "rank_show_system", false);
        if (currentTimeMillis - lastUpdate < 60 * 60 * 1000) {
            if (showSystem && this.appWithSystem.size() > 0) {
                return this.appWithSystem;
            }
            if (!showSystem && this.appWithoutSystem.size() > 0) {
                return this.appWithoutSystem;
            }
        }
        ArrayList<RunningAppInfo> runningAppInfos = new ArrayList<>();
        try {
            List<PackageInfo> installedApps = getInstalledApps(context);
            HashMap<String, PackageInfo> installedPackageMap = new HashMap<>(installedApps.size());
            for (PackageInfo packageInfo : installedApps) {
                installedPackageMap.put(packageInfo.packageName, packageInfo);
            }
            Map<String, List<Integer>> pkgWithPids = PkgNProcUtils.readPkgInfos(context);
            for (String pkg : pkgWithPids.keySet()) {
                RunningAppInfo runningAppInfo = new RunningAppInfo();
                PackageInfo packageInfo = installedPackageMap.get(pkg);
                if (packageInfo != null) {
                    ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                    runningAppInfo.name = packageManager.getApplicationLabel(applicationInfo).toString();
                    runningAppInfo.isValid = isValidAppInfo(applicationInfo);
                    runningAppInfo.appInfo = applicationInfo;
                    runningAppInfo.pkgName = pkg;
                    runningAppInfo.pids.addAll(pkgWithPids.get(pkg));
                    runningAppInfos.add(runningAppInfo);
                }
            }
        } catch (OutOfMemoryError e) {
            System.gc();
        } catch (Exception e2) {
            e2.printStackTrace();
        }

        HashSet<Integer> pidMap = new HashSet<>();
        long cpuTotalTime = 0;
        for (RunningAppInfo runningAppInfo : runningAppInfos) {
            if (!TextUtils.equals(runningAppInfo.pkgName, "android")) {
                runningAppInfo.cpuTime = 0;
                runningAppInfo.dataSent = 0;
                runningAppInfo.dateReceived = 0;
                for (Integer pid : runningAppInfo.pids) {
                    if (!pidMap.contains(pid)) {
                        pidMap.add(pid);
                        String path = "/proc/" + String.valueOf(pid) + "/";
                        PkgNProcUtils.calcCpuTime(path, runningAppInfo);
                        if(collectDataCost){
                            PkgNProcUtils.calcDataCost(path, runningAppInfo);
                        }
                    }
                }
                cpuTotalTime = runningAppInfo.cpuTime + cpuTotalTime;
            }
        }

        List<RunningAppInfo> appStatses = new ArrayList<>();
        if (cpuTotalTime > 0) {
            for (RunningAppInfo runningAppInfo : runningAppInfos) {
                if (!((runningAppInfo.isValid && !showSystem) || runningAppInfo.cpuTime == 0 || TextUtils.equals(runningAppInfo.pkgName, context.getPackageName()))) {
                    runningAppInfo.cpuPercent = (((float) runningAppInfo.cpuTime) / ((float) cpuTotalTime)) * 100.0f;
                    if (((double) runningAppInfo.cpuPercent) > 0.1d) {
                        appStatses.add(runningAppInfo);
                    }
                }
            }
        }
        Collections.sort(appStatses, new Comparator<RunningAppInfo>() {
            public int compare(RunningAppInfo runningAppInfo, RunningAppInfo runningAppInfo2) {
                return (int) (runningAppInfo2.cpuTime - runningAppInfo.cpuTime);
            }
        });
        lastUpdate = System.currentTimeMillis();
        if (showSystem) {
            this.appWithSystem = appStatses;
        } else {
            this.appWithoutSystem = appStatses;
        }
        return appStatses;
    }

    //public List<RunningAppInfo> process.c.b()
    List<RunningAppInfo> getAppCpuUsg(Context context) {
        PackageManager packageManager = context.getPackageManager();
        SystemPkgFilter filter = new SystemPkgFilter(context);
        filter.init();
        List<RunningAppInfo> runningAppInfos = new ArrayList<>();

        HashMap<String, RunningAppInfo> appMap = new HashMap<>();

        List<PackageInfo> apps = getInstalledApps(context);
        Map<String, List<Integer>> pkgWithPids = PkgNProcUtils.readPkgInfosMatchingCmdline(context);
        for (PackageInfo packageInfo : apps) {
            String pkgName = packageInfo.packageName;
            if (!pkgWithPids.containsKey(pkgName)) {
                Log.d("ProcessManager", "没有运行的进程 - [" + pkgName + "]过滤");
            } else if (pkgStopped(context, pkgName)) {
                Log.d("ProcessManager", "已经停止运行的进程 - [" + pkgName + "]过滤");
            } else {
                ApplicationInfo appInfo = packageInfo.applicationInfo;
                boolean isValidAppInfo = isValidAppInfo(appInfo);
                if (filter.isSystemProcess(pkgName, isValidAppInfo)) {
                    Log.d("ProcessManager", "系统关键进程/需忽略进程 - [" + pkgName + "]过滤");
                } else {
                    RunningAppInfo runningAppInfo3 = appMap.get(pkgName);
                    if (runningAppInfo3 != null) {
                        runningAppInfo3.pids.clear();
                        runningAppInfo3.pids.addAll(pkgWithPids.get(pkgName));
                    } else {
                        RunningAppInfo runningAppInfo4 = new RunningAppInfo();
                        runningAppInfo4.name = packageManager.getApplicationLabel(appInfo).toString();
                        runningAppInfo4.pids.addAll(pkgWithPids.get(pkgName));
                        runningAppInfo4.pkgName = pkgName;
                        runningAppInfo4.isValid = isValidAppInfo;
                        runningAppInfo4.appInfo = appInfo;
                        runningAppInfo4.launchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName) != null;
                        runningAppInfos.add(runningAppInfo4);
                        appMap.put(pkgName, runningAppInfo4);
                        Log.d("ProcessManager", "添加到正在运行程序列表 - [" + runningAppInfo4.name + "|" + pkgName + "]");
                    }
                }
            }
        }
        return runningAppInfos;
    }

    //List<PackageInfo> c.a.a(Context)
    public static List<PackageInfo> getInstalledApps(Context context) {
        List<PackageInfo> list = null;
        try {
            list = context.getPackageManager().getInstalledPackages(0);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return list == null ? new ArrayList<PackageInfo>() : list;
    }

    //boolean c.a.a(Context, String)
    @TargetApi(12)
    public static boolean pkgStopped(Context context, String pkgName) {
        try {
            return (context.getPackageManager().getApplicationInfo(pkgName, 0).flags & 0x200000) != 0;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    //boolean c.a.a(ApplicationInfo)
    public static boolean isValidAppInfo(ApplicationInfo appInfo) {
        if (appInfo != null) {
            if ((appInfo.flags & PackageManager.GET_ACTIVITIES) == 0 && (appInfo.flags & PackageManager.GET_META_DATA) == 0) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

}
