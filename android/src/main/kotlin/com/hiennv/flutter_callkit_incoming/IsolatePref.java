package com.hiennv.flutter_callkit_incoming;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import io.flutter.view.FlutterCallbackInformation;

public class IsolatePref {

    private static final String SHARED_PREFS_FILE_NAME = "sapa_isolate_pref";

    private final String CALLBACK_DISPATCHER_HANDLE_KEY =
            "com.codeid.sapa.CALLBACK_DISPATCHER_HANDLE_KEY";

    private final String CALLBACK_HANDLE_KEY =
            "com.codeid.sapa.CALLBACK_HANDLE_KEY";

    public IsolatePref(Context context) {
        this.context = context;
    }

    private final Context context;

    private SharedPreferences get() {
        return context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }

    public void saveCallbackKeys(Long dispatcherCallbackHandle, Long callbackHandle) {
        get().edit().putLong(CALLBACK_DISPATCHER_HANDLE_KEY, dispatcherCallbackHandle).apply();
        get().edit().putLong(CALLBACK_HANDLE_KEY, callbackHandle).apply();
    }

    public Long getCallbackDispatcherHandle() {
        return get().getLong(CALLBACK_DISPATCHER_HANDLE_KEY, -1L);
    }

    public Long getCallbackHandle() {
        return get().getLong(CALLBACK_HANDLE_KEY, -1L);
    }

    @Nullable
    public FlutterCallbackInformation lookupDispatcherHandle() {
        return FlutterCallbackInformation.lookupCallbackInformation(getCallbackDispatcherHandle());
    }
}