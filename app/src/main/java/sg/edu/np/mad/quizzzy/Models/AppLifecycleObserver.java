package sg.edu.np.mad.quizzzy.Models;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import android.util.Log;

public class AppLifecycleObserver implements DefaultLifecycleObserver {

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        // This method is called when the app comes into the foreground
        Log.d("AppLifecycleObserver", "App moved to foreground");
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        // This method is called when the app goes into the background
        Log.d("AppLifecycleObserver", "App moved to background");
    }
}


