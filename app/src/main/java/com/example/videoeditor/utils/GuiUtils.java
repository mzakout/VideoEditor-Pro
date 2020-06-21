package com.example.videoeditor.utils;

import android.widget.Toast;

import com.example.videoeditor.App;


public class GuiUtils {
    public static void showMessage(String message) {
        Toast.makeText(App.get(), message, Toast.LENGTH_LONG).show();
    }

    public static void showMessage(int messageRes) {
        String message = App.get().getResources().getString(messageRes);
        showMessage(message);
    }

}
