package com.example.videoeditor;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.videoeditor.helpers.PermissionHelper;
import com.example.videoeditor.utils.FileUtils;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.util.List;

public class App extends Application implements Thread.UncaughtExceptionHandler {
    private static final String TAG = App.class.getSimpleName();
    private static App sInstance;

    public static App get() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        PermissionHelper.checkPermissions(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                FileUtils.createApplicationFolder();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

            }
        });

    }


    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        throwable.printStackTrace(); // not all Android versions will print the stack trace automatically
        Toast.makeText(this, "Error Happend", Toast.LENGTH_LONG).show();
    }
}
