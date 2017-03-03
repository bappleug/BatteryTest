package com.lexing.batterytest.batteryhelper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.lexing.batterytest.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Ray on 2017/1/23.
 */

//process.b
class SystemPkgFilter {
    Context context;    //a
    DBHelper dbHelper;  //b
    HashSet<String> unknownProcess;  //c
    HashSet<String> systemIgnoreProcess;  //d

    public SystemPkgFilter(Context context) {
        this.context = context;
    }

    //List<ResolveInfo> process.b.b()
    private List<ResolveInfo> queryLaunchActs() {
        Intent localIntent = new Intent("android.intent.action.MAIN");
        localIntent.addCategory("android.intent.category.HOME");
        localIntent.addCategory("android.intent.category.DEFAULT");
        return context.getPackageManager().queryIntentActivities(localIntent,
                PackageManager.GET_INTENT_FILTERS | PackageManager.GET_SIGNATURES | PackageManager.MATCH_DEFAULT_ONLY);
    }

    //List<InputMethodInfo> process.b.c()
    private List<InputMethodInfo> queryInputMethods() {
        return ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).getEnabledInputMethodList();
    }

    //void process.b.a()
    public void init() {
        dbHelper = DBHelper.getInstance(context);
//            if (dbHelper.isEmpty()) {
        ArrayList<String> pkgs = new ArrayList<>();
        List<ResolveInfo> launchActs = queryLaunchActs();
        if (launchActs != null) {
            for (ResolveInfo resolveInfo : launchActs) {
                pkgs.add(resolveInfo.activityInfo.packageName);
            }
        }
        for (InputMethodInfo packageName : queryInputMethods()) {
            pkgs.add(packageName.getPackageName());
        }
//                dbHelper.insert(pkgs);
        unknownProcess = new HashSet<>(pkgs);
//            } else {
//                unknownProcess = dbHelper.query();
//            }
        systemIgnoreProcess = new HashSet<>();
        Collections.addAll(systemIgnoreProcess, context.getResources().getStringArray(R.array.default_ignore_process));
    }

    //boolean process.b.a(String, boolean)
    public boolean isSystemProcess(String pkgName, boolean isValidAppInfo) {
        if (systemIgnoreProcess.contains(pkgName) || unknownProcess.contains(pkgName)) {
            return true;
        } else if (isValidAppInfo) {
            return (pkgName.contains("dialer")
                    || pkgName.contains("phone")
                    || pkgName.contains("contacts")
                    || pkgName.contains("android"));
        }
        return false;
    }
}
