package io.openinstall.openinstall_flutter_v2;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.fm.openinstall.Configuration;
import com.fm.openinstall.OpenInstall;
import com.fm.openinstall.listener.AppInstallAdapter;
import com.fm.openinstall.listener.AppWakeUpAdapter;
import com.fm.openinstall.model.AppData;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * OpeninstallFlutterV2Plugin
 */
public class OpeninstallFlutterV2Plugin implements FlutterPlugin, MethodCallHandler, ActivityAware {

    private MethodChannel channel;
    private ActivityPluginBinding activityPluginBinding;
    private volatile boolean initialized = false;
    private Intent intentHolder = null;
    private static final String TAG = "OpeninstallV2Plugin";
    private Configuration configuration = null;

    private final AppWakeUpAdapter wakeUpAdapter = new AppWakeUpAdapter() {
        @Override
        public void onWakeUp(AppData appData) {
            channel.invokeMethod("onWakeupNotification", data2Map(appData));
            intentHolder = null;
        }
    };

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "openinstall_flutter_v2");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        Log.d(TAG, "call method " + call.method);
        if (call.method.equals("getInstall")) {
            Integer seconds = call.argument("seconds");
            OpenInstall.getInstall(new AppInstallAdapter() {
                @Override
                public void onInstall(AppData appData) {
                    channel.invokeMethod("onInstallNotification", data2Map(appData));
                }
            }, seconds == null ? 0 : seconds);
            result.success("getInstall success, wait callback");
        } else if (call.method.equals("reportRegister")) {
            OpenInstall.reportRegister();
            result.success("reportRegister success");
        } else if (call.method.equals("reportEffectPoint")) {
            String pointId = call.argument("pointId");
            Integer pointValue = call.argument("pointValue");
            OpenInstall.reportEffectPoint(pointId, pointValue == null ? 0 : pointValue);
            result.success("reportEffectPoint success");
        } else if (call.method.equals("init")) {
            init();
            result.success("init success");
        } else if (call.method.equals("initWithPermission")) {
            Activity activity = activityPluginBinding.getActivity();
            if (activity != null) {
                initWithPermission(activity);
                result.success("initWithPermission success, wait request permission");
            } else {
                Log.d(TAG, "Activity is null, can't call initWithPermission");
                init();
                result.success("init success");
            }
        } else if (call.method.equals("config")) {
            String oaid = call.argument("oaid");
            String gaid = call.argument("gaid");
            Boolean adEnabled = call.argument("adEnabled");
            config(adEnabled == null ? false : adEnabled, oaid, gaid);
            result.success("config success");
        } else {
            result.notImplemented();
        }
    }

    private void config(boolean adEnabled, String oaid, String gaid) {
        Configuration.Builder builder = new Configuration.Builder();
        builder.adEnabled(adEnabled);
        builder.oaid(oaid);
        builder.gaid(gaid);
        Log.d(TAG, String.format("config adEnabled=%b, oaid=%s, gaid=%s",
                adEnabled, oaid == null ? "NULL" : oaid, gaid == null ? "NULL" : gaid));
        configuration = builder.build();
    }

    private void init() {
        Activity activity = activityPluginBinding.getActivity();
        if (activity != null) {
            OpenInstall.init(activity, configuration);
            initialized = true;
            if (intentHolder == null) {
                OpenInstall.getWakeUp(activity.getIntent(), wakeUpAdapter);
            } else {
                OpenInstall.getWakeUp(intentHolder, wakeUpAdapter);
            }
        } else {
            Log.d(TAG, "Activity is null, can not init OpenInstall");
        }
    }

    private void initWithPermission(final Activity activity) {
        if (activity == null) {
            return;
        }
        activityPluginBinding.addRequestPermissionsResultListener(new PluginRegistry.RequestPermissionsResultListener() {
            @Override
            public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
                OpenInstall.onRequestPermissionsResult(requestCode, permissions, grantResults);
                return false;
            }
        });
        OpenInstall.initWithPermission(activity, configuration, new Runnable() {
            @Override
            public void run() {
                initialized = true;
                if (intentHolder == null) {
                    OpenInstall.getWakeUp(activity.getIntent(), wakeUpAdapter);
                } else {
                    OpenInstall.getWakeUp(intentHolder, wakeUpAdapter);
                }
            }
        });

    }

    private Map<String, String> data2Map(AppData data) {
        Map<String, String> result = new HashMap<>();
        result.put("channelCode", data.getChannel());
        result.put("bindData", data.getData());
        return result;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        this.activityPluginBinding = binding;
        this.activityPluginBinding.addOnNewIntentListener(new PluginRegistry.NewIntentListener() {
            @Override
            public boolean onNewIntent(Intent intent) {
                if (initialized) {
                    OpenInstall.getWakeUp(intent, wakeUpAdapter);
                } else {
                    intentHolder = intent;
                }
                return false;
            }
        });
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }

}
