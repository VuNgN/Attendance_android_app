package com.vungn.attendancedemo.util

data class Manufacturer(val brand: String, val pkg: String, val cls: String)

val manufacturers = listOf(
    Manufacturer(
        "xiaomi",
        "com.miui.securitycenter",
        "com.miui.permcenter.autostart.AutoStartManagementActivity"
    ),
    Manufacturer(
        "oppo",
        "com.coloros.safecenter",
        "com.coloros.safecenter.permission.startup.StartupAppListActivity"
    ),
    Manufacturer(
        "vivo",
        "com.vivo.permissionmanager",
        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
    ),
    Manufacturer(
        "letv", "com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"
    ),
    Manufacturer(
        "honor",
        "com.huawei.systemmanager",
        "com.huawei.systemmanager.optimize.process.ProtectActivity"
    ),
    Manufacturer("asus", "com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"),
    Manufacturer(
        "nokia",
        "com.evenwell.powersaving.g3",
        "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity"
    ),
    Manufacturer(
        "huawei",
        "com.huawei.systemmanager",
        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
    ),
    Manufacturer(
        "samsung", "com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"
    ),
    Manufacturer(
        "oneplus",
        "com.oneplus.security",
        "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
    ),
    Manufacturer(
        "htc", "com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity"
    ),
    Manufacturer(
        "lenovo", "com.lenovo.security", "com.lenovo.security.purebackground.PureBackgroundActivity"
    ),
    Manufacturer(
        "smartisan",
        "com.smartisanos.security",
        "com.smartisanos.security.SmartPermissionManagerActivity"
    ),
    Manufacturer("meizu", "com.meizu.safe", "com.meizu.safe.permission.SmartBGActivity"),
    Manufacturer(
        "360",
        "com.yulong.android.coolsafe",
        "com.yulong.android.coolsafe.ui.activity.autorun.AutoRunListActivity"
    ),
    Manufacturer("sony", "com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity"),
    Manufacturer("lg", "com.lge.purebackground", "com.lge.purebackground.PurebackgroundActivity"),
    Manufacturer("zte", "com.zte.heartyservice", "com.zte.heartyservice.autorun.AppAutoRunManager"),
    Manufacturer(
        "panasonic",
        "com.panasonic.powersavingtool",
        "com.panasonic.powersavingtool.PowerSavingToolActivity"
    ),
    Manufacturer("gionee", "com.gionee.softmanager", "com.gionee.softmanager.MainActivity"),
    Manufacturer("sugar", "com.sugar.appmanager", "com.sugar.appmanager.AutoStartManager"),
    Manufacturer(
        "smartisanos",
        "com.smartisanos.security",
        "com.smartisanos.security.SmartPermissionManagerActivity"
    ),
    Manufacturer(
        "leeco", "com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"
    ),
)
