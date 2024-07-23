package sg.edu.np.mad.quizzzy.Models;

import static android.content.Context.POWER_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import android.app.Application;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class AppLifecycleObserver implements DefaultLifecycleObserver {
    Context context;
    boolean appInForeground;
    boolean screenOn;

    // Setters
    public void setAppInForeground(boolean appInForeground) { this.appInForeground = appInForeground; }
    public void setScreenOn(boolean screenOn) { this.screenOn = screenOn; }

    // Getters
    public boolean getAppInForeground() { return this.appInForeground; }
    public boolean getScreenOn() { return this.screenOn; }

    // Setter for context
    public void setContext(Context context) { this.context = context; }

    // This method is called when the app comes into the foreground
    @Override
    public void onStart(LifecycleOwner owner) {
        appInForeground = true;

        Log.d("AppLifecycleObserver", "App moved to foreground");
        Log.d("AppLifecycleObserver", "App in foreground: " + appInForeground);
        Log.d("AppLifecycleObserver", "Screen is on: " + isScreenOn(context));
    }

    // This method is called when the app goes into the background
    @Override
    public void onStop(LifecycleOwner owner) {
        appInForeground = false;

        Log.d("AppLifecycleObserver", "App moved to background");
        Log.d("AppLifecycleObserver", "App in foreground: " + appInForeground);
        Log.d("AppLifecycleObserver", "Screen is on: " + isScreenOn(context));
    }

    public AppLifecycleObserver() {
        appInForeground = true;
        screenOn = true;
    }

    // Uses power manager to check if the screen is on, and return a boolean value
    public boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        screenOn = pm.isInteractive();
        return screenOn;
    }
}


