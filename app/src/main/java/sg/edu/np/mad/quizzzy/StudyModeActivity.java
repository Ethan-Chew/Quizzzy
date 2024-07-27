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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
    private String userId;
    private int studyDuration;
    private boolean studyTimerRunning;
    private boolean wasStudyTimerRunning;
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
        bottomNavigationView.setSelectedItemId(R.id.stats);
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
                    return true;
                }
                return false;
            }
        });

        Button pause = findViewById(R.id.startStopStudyTimer);

        // Manager for gyroscope tracking
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!studyTimerRunning) {
                    studyTimerRunning = true;

                    // Prevents creating more timer instances
                    if (!wasStudyTimerRunning) {
                        runTimer();
                        wasStudyTimerRunning = true;
                    }
                } else {
                    studyTimerRunning = false;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        firebaseDB = FirebaseDatabase.getInstance("https://quizzzy-21bea-default-rtdb.asia-southeast1.firebasedatabase.app/");
        firebaseReference = firebaseDB.getReference("studyDuration");
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(StudyModeActivity.this);
        userId = localDB.getUser().getUser().getId();
        TextView studyTime = findViewById(R.id.studyTime);

        firebaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();

                    if (dataSnapshot.hasChild(userId)) {
                        studyDuration = Integer.parseInt(String.valueOf(dataSnapshot.child(userId).child("studyDuration").getValue()));

                        if (!dataSnapshot.child(userId).child("currentDate").getValue().toString().equals(SimpleDateFormat.getDateInstance().format(new Date()).toString())) {
                            StudyDurationHelper helper = new StudyDurationHelper(userId, "0");
                            firebaseReference.child(userId).setValue(helper);
                            studyDuration = 0;
                        }
                    } else {
                        studyDuration = 0;
                    }
                    studyTime.setText(formatStudyTime(studyDuration));

                    Log.d("Read Firebase", "StudyDuration: " + studyDuration);
                } else {
                    studyDuration = 0;

                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float z = event.values[2]; // Acceleration in the Z-axis

            // If gyroscope is face down
            if (z < -9.0) {
                Log.d("Gyroscope", "Screen is face down");
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
        TextView studyTime = findViewById(R.id.studyTime);
        Handler handler = new Handler();
        StudyDurationHelper helper = new StudyDurationHelper(userId, Integer.toString(studyDuration));
        Log.d("date", "runTimer: " + helper.getCurrentDate());;

        handler.post(new Runnable() {
            @Override
            public void run() {
                String studyDurationFormatted = formatStudyTime(studyDuration);
                studyTime.setText(studyDurationFormatted);

                // Check if app is in the background while the screen is on and pauses the timer
                if (!AppLifecycleObserver.getAppInForeground() && AppLifecycleObserver.getScreenOn()) {
                    studyTimerRunning = false;
                }
                if (studyTimerRunning) {
                    studyDuration++;
                    helper.setStudyDuration(Integer.toString(studyDuration));
                    firebaseReference.child(userId).setValue(helper);
                }
                handler.postDelayed(this, 1000);
            }
        });
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