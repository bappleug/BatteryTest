package com.lexing.batterytest.batteryhelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.lexing.common.utils.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ray on 2017/1/23.
 */
//process.a.d
public class PkgNProcUtils {
    private static List<Integer> uidExclusion = new ArrayList<Integer>() {
        {
            add(0);
            add(1000);
            add(1002);
            add(1001);
            add(1007);
            add(1010);
            add(1013);
            add(1016);
            add(1019);
            add(1027);
            add(2000);
        }
    };
    public static final String[] DATA_COST_KEY = { "wlan0:", "rmnet0:", "rmnet_ipa0:" };

    //Map<String, List<Integer>> process.a.d.a(Context)
    @SuppressLint({"DefaultLocale"})
    public static Map<String, List<Integer>> readPkgInfosMatchingCmdline(Context context) {
        List<ProcessStats> processStatsList = readProcStats(context);
        Map<String, List<Integer>> hashMap = new HashMap<>();
        for (ProcessStats processStats : processStatsList) {
            if (processStats != null && !uidExclusion.contains(processStats.ruid) && processStats.pkgs != null) {
                for (String pkg : processStats.pkgs) {
                    if (processStats.cmdLine.toLowerCase().contains(pkg.toLowerCase())) {
                        List<Integer> pidList = hashMap.containsKey(pkg) ? (List) hashMap.get(pkg) : new ArrayList<>();
                        pidList.add(processStats.pid);
                        hashMap.put(pkg, pidList);
                    }
                }
            }
        }
        return hashMap;
    }

    //Map<String, List<Integer>> process.a.d.c(Context)
    @SuppressLint({"DefaultLocale"})
    public static Map<String, List<Integer>> readPkgInfos(Context context) {
        List<ProcessStats> processStatses = readProcStatsWithAndroid(context);
        Map<String, List<Integer>> hashMap = new HashMap<>();
        for (ProcessStats processStats : processStatses) {
            List<Integer> pids;
            if (processStats.pkgs != null) {
                for (String pkg : processStats.pkgs) {
                    pids = hashMap.containsKey(pkg) ? (List) hashMap.get(pkg) : new ArrayList<>();
                    pids.add(processStats.pid);
                    hashMap.put(pkg, pids);
                }
            } else {
                pids = hashMap.containsKey("android") ? (List) hashMap.get("android") : new ArrayList<>();
                pids.add(processStats.pid);
                hashMap.put("android", pids);
            }
        }
        return hashMap;
    }

    //static List<pkgs> process.a.d.d(Context)
    private static List<ProcessStats> readProcStatsWithAndroid(Context context) {
        List<ProcessStats> processStatses = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        for (String pidStr : getProcFiles()) {
            try {
                String path = "/proc/" + pidStr + "/";
                int pid = Integer.parseInt(pidStr);
                ProcessStats processStats = new ProcessStats();
                processStats.pid = pid;
                processStats.ruid = readRUID(path);
                processStats.cmdLine = readCmdline(path);
                if (processStats.ruid != 1000 && processStats.ruid != 1001) {
                    processStats.pkgs = packageManager.getPackagesForUid(processStats.ruid);
                } else if (processStats.cmdLine.contains(".")) {
                    processStats.pkgs = new String[]{processStats.cmdLine};
                } else {
                    processStats.pkgs = new String[]{"android"};
                }
                processStatses.add(processStats);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return processStatses;
    }

    //static List<c> process.a.d.b(Context)
    public static List<ProcessStats> readProcStats(Context context) {
        return readProcStatsFromFiles(context, getProcFiles());
    }

    //static List<pkgs> process.a.d.a()
    public static List<ProcessStats> readProcStatsFromFiles(Context context, List<String> procFiles) {
        List<ProcessStats> processStatsList = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        for (String pidStr : procFiles) {
            try {
                StringBuilder rootSb = new StringBuilder();
                rootSb.append("/proc/").append(pidStr).append("/");
                int pid = Integer.parseInt(pidStr);
                ProcessStats ProcessStats = new ProcessStats();
                ProcessStats.pid = pid;
                ProcessStats.ruid = readRUID(rootSb.toString());
                ProcessStats.cmdLine = readCmdline(rootSb.toString());
                ProcessStats.pkgs = pm.getPackagesForUid(ProcessStats.ruid);
                processStatsList.add(ProcessStats);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return processStatsList;
    }

    //static List<String> process.a.d.a()
    public static List<String> getProcFiles() {
        File file = new File("/proc/");
        List<String> procList;
        if (file.isDirectory()) {
            String[] files = file.list(new FilenameFilter() {
                Pattern pattern = Pattern.compile("^[0-9]+$");

                @Override
                public boolean accept(File dir, String name) {
                    return pattern.matcher(name).matches();
                }
            });
            procList = Arrays.asList(files);
        } else {
            procList = new ArrayList<>();
        }
        return procList;
    }

    //static int process.a.d.b(String)
    public static int readRUID(String root) {
        Pattern patten = Pattern.compile("Uid:([\\s\\d]+)?(([\\w])+)?:");
        String fileStr;
        try {
            fileStr = FileUtils.readFile(root + "status");
        } catch (Exception e){
            return -1;
        }
        if (!TextUtils.isEmpty(fileStr)) {
            String[] lines = fileStr.split(System.getProperty("line.separator"));
            if(lines.length == 1){
                Matcher matcher = patten.matcher(lines[0]);
                if(matcher.find()){
                    String[] uids = matcher.group(1).trim().split("\\s+");
                    if (uids.length > 0) {
                        try {
                            return Integer.parseInt(uids[0]);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            return -1;
                        }
                    }
                }
            } else {
                for (String line : lines) {
                    if (line.contains("Uid:")) {
                        String[] split = line.trim().split("\\s+");
                        if (split.length >= 2) {
                            try {
                                return Integer.parseInt(split[1]);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                return -1;
                            }
                        }
                    }
                }
            }
        }
        return -1;
    }

    //static String process.a.d.a(String)
    public static String readCmdline(String root) {
        String fileStr;
        try{
            fileStr = FileUtils.readFile(root + "cmdline");
        }catch (Exception e) {
            return "";
        }
        if (!TextUtils.isEmpty(fileStr)) {
            return fileStr.trim().split("\u0000")[0];
        }
        return "";
    }

    //static void process.a.d.a(String, RunningAppInfo, boolean)
    public static void calcCpuTime(String rootPath, RunningAppInfo runningAppInfo) {
        String statFileStr = FileUtils.readFile(rootPath + "stat");
        if (!TextUtils.isEmpty(statFileStr)) {
            String[] split = statFileStr.split(" ");
            if (split.length > 14) {
                runningAppInfo.cpuTime = (Long.parseLong(split[14]) + Long.parseLong(split[13])) + runningAppInfo.cpuTime;
            }
        }
    }

    public static void calcDataCost(String rootPath, RunningAppInfo runningAppInfo){
        String netFileStr = FileUtils.readFile(rootPath + "net/dev");
        if (!TextUtils.isEmpty(netFileStr)) {
            for (String key : DATA_COST_KEY) {
                String valuesStr = "";
                int indexOf = netFileStr.indexOf(key);
                if (indexOf > 0) {
                    int length = key.length() + indexOf;
                    indexOf = netFileStr.indexOf(":", length);
                    if (indexOf > 0) {
                        valuesStr = netFileStr.substring(length, indexOf);
                    } else if (length < netFileStr.length()) {
                        valuesStr = netFileStr.substring(length);
                    }
                    if (!TextUtils.isEmpty(valuesStr)) {
                        calcDataRecNSent(valuesStr, runningAppInfo);
                    }
                }
            }
        }
    }

    //static void a(String, RunningAppInfo)
    public static void calcDataRecNSent(String valuesStr, RunningAppInfo runningAppInfo) {
        int i = 0;
        for (String value : valuesStr.split(" ")) {
            if (!TextUtils.isEmpty(value)) {
                if (i == 0) {
                    runningAppInfo.dateReceived = Long.parseLong(value) + runningAppInfo.dateReceived;
                } else if (i == 8) {
                    runningAppInfo.dataSent += Long.parseLong(value);
                    return;
                }
                i++;
            }
        }
    }

    //not working
//    @Deprecated
//    public static Pair<Long, Long> calcAppNetData(int uid){
//        return new Pair<>(TrafficStats.getUidRxBytes(uid), TrafficStats.getUidTxBytes(uid));
//    }

    //class process.a.c
    static class ProcessStats {
        public int pid = -1;        //a
        public String cmdLine = ""; //b
        public String[] pkgs = null;//c
        public int ruid = -1;       //d
        public int e = -1;          //e
        public String f = "";       //f
    }
}
