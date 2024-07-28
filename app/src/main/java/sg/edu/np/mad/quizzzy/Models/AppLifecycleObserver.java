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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

import sg.edu.np.mad.quizzzy.R;

public class AppLifecycleObserver implements DefaultLifecycleObserver {
    Context context;
    static boolean appInForeground;
    static boolean screenOn;

    private FirebaseDatabase firebaseDB = FirebaseDatabase.getInstance("https://quizzzy-21bea-default-rtdb.asia-southeast1.firebasedatabase.app/");
    private DatabaseReference firebaseReference = firebaseDB.getReference("studyDuration");
    static String userId;

    // Setters
    public void setContext(Context context) { this.context = context; }
    public static void setAppInForeground(boolean isAppInForeground) { appInForeground = isAppInForeground; }
    public static void setScreenOn(boolean isScreenOn) { screenOn = isScreenOn; }
    public static void setUserId(String id) { userId = id; }

    // Getters
    public static boolean getAppInForeground() { return appInForeground; }
    public static boolean getScreenOn() { return screenOn; }

    // This method is called when the app comes into the foreground
    @Override
    public void onStart(LifecycleOwner owner) {
        appInForeground = true;
    }

    // This method is called when the app goes into the background
    @Override
    public void onStop(LifecycleOwner owner) {
        appInForeground = false;

        // If user exits the app while timer was running, reset "pauseTime", after checking for UserId
        // Since user may be using other app features, such as Flashcards,
        // We will store the elapsed time to the Firebase RTDB as "studyDuration", before resetting
        if (isScreenOn(context) && userId != null) {
            // Get data from database
            firebaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();

                        int studyDuration = Integer.parseInt(String.valueOf(dataSnapshot.child(userId).child("studyDuration").getValue()));
                        long pauseTime = (long) dataSnapshot.child(userId).child("pauseTime").getValue();

                        // Check if "pauseTime" exists, which means that the activity had been paused unexpectedly
                        // Either going to a different activity, or turning the screen off
                        // Gets the elapsed time between the pause and reopening this Activity and adds to studyDuration
                        if (pauseTime != 0) {
                            int elapsedTime = (int) Math.floorDiv(System.currentTimeMillis() - pauseTime, 1000);
                            studyDuration += elapsedTime;

                            firebaseReference.child(userId).child("studyDuration").setValue(studyDuration);
                            firebaseReference.child(userId).child("pauseTime").setValue(0);
                        }

                    }
                }
            });
        }
    }

    // Uses power manager to check if the screen is on, and return a boolean value
    public boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        screenOn = pm.isInteractive();
        return screenOn;
    }
}