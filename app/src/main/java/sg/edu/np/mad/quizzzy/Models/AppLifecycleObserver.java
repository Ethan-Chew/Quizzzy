package sg.edu.np.mad.quizzzy.Models;

import static android.content.Context.POWER_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class AppLifecycleObserver implements DefaultLifecycleObserver {
    Context context;

    // Setter for context
    public void setContext(Context context) { this.context = context; }

    // This method is called when the app comes into the foreground
    @Override
    public void onStart(LifecycleOwner owner) {
        boolean screenOn = isScreenOn(context);

        Log.d("AppLifecycleObserver", "App moved to foreground");
        Log.d("AppLifecycleObserver", "Screen is on: " + screenOn);
    }

    // This method is called when the app goes into the background
    @Override
    public void onStop(LifecycleOwner owner) {
        boolean screenOn = isScreenOn(context);

        Log.d("AppLifecycleObserver", "App moved to background");
        Log.d("AppLifecycleObserver", "Screen is on: " + screenOn);
    }

    // Uses power manager to check if the screen is on, and return a boolean value
    public boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.isInteractive();
    }
}


