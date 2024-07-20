package sg.edu.np.mad.quizzzy.Models;

import android.app.Application;

import androidx.lifecycle.ProcessLifecycleOwner;

public class QuizzzyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AppLifecycleObserver appLifecycleObserver = new AppLifecycleObserver();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(appLifecycleObserver);
    }
}
