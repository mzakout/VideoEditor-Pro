package com.example.videoeditor.helpers;

import android.Manifest;

import com.example.videoeditor.App;
import com.example.videoeditor.utils.GuiUtils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;


public class PermissionHelper {

    private static final String TAG = PermissionHelper.class.getSimpleName();

    public static void checkPermissions(MultiplePermissionsListener listener) {
        Dexter.withContext(App.get())
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.ACCESS_NETWORK_STATE)
                .withListener(listener)
                .withErrorListener(error -> GuiUtils.showMessage(error.toString()))
                .check();


    }

}
