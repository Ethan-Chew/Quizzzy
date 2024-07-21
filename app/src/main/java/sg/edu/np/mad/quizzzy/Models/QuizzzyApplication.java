package sg.edu.np.mad.quizzzy.Models;

import android.app.Application;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import androidx.lifecycle.ProcessLifecycleOwner;

public class QuizzzyApplication extends Application {

    private PowerManager pm;
    @Override
    public void onCreate() {
        super.onCreate();

        AppLifecycleObserver appLifecycleObserver = new AppLifecycleObserver();
        AppLifecycleObserver.context = getApplicationContext();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(appLifecycleObserver);
    }
}
