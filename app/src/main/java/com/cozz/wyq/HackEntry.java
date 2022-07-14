package com.cozz.wyq;

import com.cozz.wyq.hack.HackMain;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HackEntry implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private final static String modulePackageName = HackEntry.class.getPackage().getName();
    private XSharedPreferences sharedPreferences;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        String appPkgName = "com.yunjian.wyq";
        if (appPkgName.equals(loadPackageParam.packageName) && appPkgName.equals(loadPackageParam.processName)) {
            XposedBridge.log("We are already in package: `" + appPkgName + "`, process name: `" + loadPackageParam.processName + "`!");
            HackMain.hackEntry(loadPackageParam, loadPackageParam.processName);
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        this.sharedPreferences = new XSharedPreferences(modulePackageName, "default");
//        XposedBridge.log(modulePackageName + " initZygote");
    }
}