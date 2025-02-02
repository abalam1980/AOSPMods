package sh.siava.AOSPMods;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.app.Instrumentation;
import android.content.Context;

import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import sh.siava.AOSPMods.Utils.Helpers;
import sh.siava.AOSPMods.Utils.SystemUtils;
import sh.siava.AOSPMods.allApps.overScrollDisabler;
import sh.siava.AOSPMods.android.StatusbarSize;
import sh.siava.AOSPMods.android.screenOffKeys;
import sh.siava.AOSPMods.android.screenRotation;
import sh.siava.AOSPMods.launcher.TaskbarActivator;
import sh.siava.AOSPMods.systemui.AOSPSettingsLauncher;
import sh.siava.AOSPMods.systemui.BackToKill;
import sh.siava.AOSPMods.systemui.BatteryStyleManager;
import sh.siava.AOSPMods.systemui.BrightnessSlider;
import sh.siava.AOSPMods.systemui.FeatureFlagsMods;
import sh.siava.AOSPMods.systemui.FingerprintWhileDozing;
import sh.siava.AOSPMods.systemui.GestureNavbarManager;
import sh.siava.AOSPMods.systemui.KeyGuardPinScrambler;
import sh.siava.AOSPMods.systemui.KeyguardBottomArea;
import sh.siava.AOSPMods.systemui.KeyguardCustomText;
import sh.siava.AOSPMods.systemui.LockscreenAlbumArt;
import sh.siava.AOSPMods.systemui.MultiStatusbarRows;
import sh.siava.AOSPMods.systemui.NotificationExpander;
import sh.siava.AOSPMods.systemui.QSFooterTextManager;
import sh.siava.AOSPMods.systemui.QSHaptic;
import sh.siava.AOSPMods.systemui.QSHeaderManager;
import sh.siava.AOSPMods.systemui.QSQuickPullDown;
import sh.siava.AOSPMods.systemui.QSTileGrid;
import sh.siava.AOSPMods.systemui.ScreenGestures;
import sh.siava.AOSPMods.systemui.ScreenshotController;
import sh.siava.AOSPMods.systemui.StatusbarMods;
import sh.siava.AOSPMods.systemui.UDFPSManager;
import sh.siava.AOSPMods.systemui.easyUnlock;
import sh.siava.AOSPMods.telecom.CallVibrator;

@SuppressWarnings("RedundantThrows")
public class AOSPMods implements IXposedHookLoadPackage{
    public static final String SYSTEM_UI_PACKAGE = "com.android.systemui";
    public static final String SYSTEM_FRAMEWORK_PACKAGE = "android";
    public static final String TELECOM_SERVER_PACKAGE = "com.android.server.telecom";
    public static final String LAUNCHR_PACKAGE = "com.google.android.apps.nexuslauncher";

    public static boolean isSecondProcess = false;

    public static ArrayList<Class<?>> modPacks = new ArrayList<>();
    public static ArrayList<XposedModPack> runningMods = new ArrayList<>();
    public Context mContext = null;

    public AOSPMods()
    {

        //region Mod list definition
        modPacks.add(NotificationExpander.class);
        modPacks.add(BackToKill.class);
        modPacks.add(FeatureFlagsMods.class);
        modPacks.add(QSHaptic.class);
        modPacks.add(QSHeaderManager.class);
        modPacks.add(QSQuickPullDown.class);
        modPacks.add(AOSPSettingsLauncher.class);
        modPacks.add(BrightnessSlider.class);
        modPacks.add(LockscreenAlbumArt.class);
        modPacks.add(QSTileGrid.class);
        modPacks.add(ScreenGestures.class);
        modPacks.add(miscSettings.class); //13 OK except for internet tile
        modPacks.add(KeyguardCustomText.class); //13 OK except for carrier text
        modPacks.add(KeyguardBottomArea.class); //13 OK
        modPacks.add(UDFPSManager.class); //13 OK
        modPacks.add(easyUnlock.class); //13 OK
        modPacks.add(MultiStatusbarRows.class); //13 OK
        modPacks.add(StatusbarMods.class); //13 OK
        modPacks.add(BatteryStyleManager.class); //13 OK
        modPacks.add(GestureNavbarManager.class); //13 OK
        modPacks.add(QSFooterTextManager.class); //13 OK
        modPacks.add(ScreenshotController.class); //13 OK
        modPacks.add(screenOffKeys.class); //13 OK
        modPacks.add(TaskbarActivator.class); //13 OK
        modPacks.add(KeyGuardPinScrambler.class); //13 OK
        modPacks.add(overScrollDisabler.class); //13 OK
        modPacks.add(FingerprintWhileDozing.class); //13 OK
        modPacks.add(StatusbarSize.class); //13 OK
        modPacks.add(screenRotation.class); //13 OK
        modPacks.add(CallVibrator.class); //13 OK
        //endregion
    }
    
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        isSecondProcess =  lpparam.processName.contains(":");

/*        log("------------");
        Helpers.dumpClass("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam);
        log("------------");
*/
        findAndHookMethod(Instrumentation.class, "newApplication", ClassLoader.class, String.class, Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (mContext == null) setContext((Context) param.args[2]);
    
                for (Class<?> mod : modPacks)
                {
                    try {
                        XposedModPack instance = ((XposedModPack) mod.getConstructor(Context.class).newInstance(mContext));
                        if(!instance.listensTo(lpparam.packageName)) continue;
                        try {
                            instance.updatePrefs();
                            } catch(Throwable ignored){ }
                        instance.handleLoadPackage(lpparam);
                        runningMods.add(instance);
                    }
                    catch (Throwable T)
                    {
                        log("Start Error Dump");
                        T.printStackTrace();
                    }
                }
            }
        });
 
    }

    private void setContext(Context context) {
        mContext = context;
        new SystemUtils(context);
        XPrefs.loadPrefs(mContext);
    }
}