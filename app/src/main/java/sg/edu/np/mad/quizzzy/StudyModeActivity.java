package sg.edu.np.mad.quizzzy;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import sg.edu.np.mad.quizzzy.Flashlets.CreateFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.Models.AppLifecycleObserver;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.StudyDurationHelper;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Search.SearchActivity;

public class StudyModeActivity extends AppCompatActivity implements SensorEventListener {
    FirebaseDatabase firebaseDB;
    DatabaseReference firebaseReference;
    TextView studyTime;
    private Handler handler;
    private Runnable runnable;
    private String userId;
    private int studyDuration;
    private boolean studyTimerRunning = false;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_study_mode);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configure Back Button
        Toolbar toolbar = findViewById(R.id.studyToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Configure Bottom Navigation Bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnApplyWindowInsetsListener(null);
        bottomNavigationView.setPadding(0,0,0,0);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.home) {
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.search) {
                    startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.flashlets) {
                    startActivity(new Intent(getApplicationContext(), FlashletList.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.stats) {
                    startActivity(new Intent(getApplicationContext(), StatisticsActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                }
                return false;
            }
        });


        ImageView startStopImage = findViewById(R.id.startStopStudyTimerImage);

        // Manager for gyroscope tracking
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Setup databases
        firebaseDB = FirebaseDatabase.getInstance("https://quizzzy-21bea-default-rtdb.asia-southeast1.firebasedatabase.app/");
        firebaseReference = firebaseDB.getReference("studyDuration");
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(StudyModeActivity.this);
        userId = localDB.getUser().getUser().getId();

        // Setup views
        studyTime = findViewById(R.id.studyTime);
        ImageView startStopImage = findViewById(R.id.startStopStudyTimerImage);
        Button startStopTimer = findViewById(R.id.startStopStudyTimer);
        TextView encouragementsText = findViewById(R.id.encouragementText);

        // Generate a motivational quote
        String[] encouragements = new String[] {
                "Don’t watch the clock; do what it does. Keep studying",
                "There is no elevator to success. You have to take the stairs.",
                "It always seems impossible until it’s done"
        };
        encouragementsText.setText(encouragements[new Random().nextInt(encouragements.length)]);

        startStopTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!studyTimerRunning) {
                    studyTimerRunning = true;
                    startStopImage.setImageResource(R.drawable.pause);
                    runTimer();
                } else {
                    stopTimer();
                    startStopImage.setImageResource(R.drawable.play);
                }
            }
        });

        // Get data from database
        firebaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();

                    // Check if data has already been logged
                    if (dataSnapshot.hasChild(userId)) {
                        studyDuration = Integer.parseInt(String.valueOf(dataSnapshot.child(userId).child("studyDuration").getValue()));
                        long pauseTime = (long) dataSnapshot.child(userId).child("pauseTime").getValue();

                        // Check if "pauseTime" exists, which means that the activity had been paused unexpectedly
                        // Either going to a different activity, or turning the screen off
                        // Gets the elapsed time between the pause and reopening this Activity and adds to studyDuration
                        if (pauseTime != 0) {
                            int elapsedTime = (int) Math.floorDiv(System.currentTimeMillis() - pauseTime, 1000);
                            studyDuration += elapsedTime;
                            studyTimerRunning = true;
                            runTimer();
                        }

                        // Change image of button
                        if (studyTimerRunning) {
                            startStopImage.setImageResource(R.drawable.pause);
                        } else {
                            startStopImage.setImageResource(R.drawable.play);
                        }

                        // Check if the data has not been logged in the past day, and reset it if true
                        if (!dataSnapshot.child(userId).child("currentDate").getValue().toString().equals(SimpleDateFormat.getDateInstance().format(new Date()).toString())) {
                            StudyDurationHelper helper = new StudyDurationHelper(userId, "0");
                            firebaseReference.child(userId).setValue(helper);
                            studyDuration = 0;
                        }
                    } else {
                        studyDuration = 0;
                    }
                    studyTime.setText(formatStudyTime(studyDuration));
                } else {
                    studyDuration = 0;
                }
            }
        });

        // Starts listening using accelerometer (gyroscope) when activity is resumed
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister sensor when activity is paused to save battery (Since it is only needed in this activity)
        sensorManager.unregisterListener(this);

        StudyDurationHelper helper = new StudyDurationHelper(userId, Integer.toString(studyDuration));

        // Check if app is in the background while the screen is on (User exits app)
        // Then checks if the study timer is still running (User did not pause)
        if (studyTimerRunning) {
            helper.setPauseTime(System.currentTimeMillis());

            // Passes userId to AppLifeCycleObserver
            // If the app goes into the background, pauseTimer would be reset in FireBase
            // This is because onPause is called before the Application detects that the app goes into the background
            AppLifecycleObserver.setUserId(userId);
        }
        firebaseReference.child(userId).setValue(helper);
        stopTimer();
    }

    // Gyroscope tracker
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Acceleration in the Z-axis
            float z = event.values[2];

            // If screen is face down, dim the screen
            // When the screen is no longer face down (picked up), reset brightness
            if (z < -9.0) {
                dimScreen();
            } else {
                restoreScreenBrightness();
            }
        }
    }

    // Required method for SensorEventListener
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // Method to dim the screen
    private void dimScreen() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = 0; // Value range from 0 (dark) to 1 (bright)
        getWindow().setAttributes(params);
    }

    // Method to restore screen brightness
    private void restoreScreenBrightness() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = -1; // Use default brightness
        getWindow().setAttributes(params);
    }

    private void runTimer() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                // Run the timer, increment the study duration var and update the DB
                if (studyTimerRunning) {
                    updateTimer();

                    // Delays post by 1 second, to update both the TextView and DB per second
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(runnable);
    }

    // Stops the timer and kills the runnable
    private void stopTimer() {
        if (studyTimerRunning) {
            studyTimerRunning = false;
            handler.removeCallbacks(runnable);
        }
    }

    // Updates the timer in app and in the Firebase RTDB
    private void updateTimer() {
        StudyDurationHelper helper = new StudyDurationHelper(userId, Integer.toString(studyDuration));
        String studyDurationFormatted = formatStudyTime(studyDuration);

        studyTime.setText(studyDurationFormatted);

        // Writes to Firebase db
        if (studyTimerRunning) {
            studyDuration++;
            helper.setStudyDuration(Integer.toString(studyDuration));
            firebaseReference.child(userId).setValue(helper);
        }
    }

    // Formats time to Hours:Minutes:Seconds, with leading 0 if there is only 1 digit in the value
    private String formatStudyTime(int studyDuration) {
        return String.format(
                Locale.getDefault(), "%02d:%02d:%02d",
                studyDuration / 3600,
                (studyDuration % 3600) / 60,
                studyDuration % 60
        );
    }
}