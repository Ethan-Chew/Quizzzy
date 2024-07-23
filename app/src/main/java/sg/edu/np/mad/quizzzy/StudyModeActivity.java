package sg.edu.np.mad.quizzzy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Locale;

import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.Models.AppLifecycleObserver;
import sg.edu.np.mad.quizzzy.Search.SearchActivity;

public class StudyModeActivity extends AppCompatActivity {
    AppLifecycleObserver applicationState = new AppLifecycleObserver();
    private int studyDuration = 0;
    private boolean studyTimerRunning = false;
    private boolean wasStudyTimerRunning = false;

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

        Log.d("TAG", "run: " + applicationState.getAppInForeground() + applicationState.getScreenOn());

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

        TextView studyTime = findViewById(R.id.studyTime);
        Button pause = findViewById(R.id.startStopStudyTimer);

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!studyTimerRunning) {
                    studyTimerRunning = true;

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

    private void runTimer() {
        TextView studyTime = findViewById(R.id.studyTime);
        Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = studyDuration / 3600;
                int minutes = (studyDuration % 3600) / 60;
                int seconds = studyDuration % 60;

                // Formatted in Hours:Minutes:Seconds, with leading 0 if there is only 1 digit in the value
                String studyDurationFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                studyTime.setText(studyDurationFormatted);

                if (!applicationState.getAppInForeground()) {
                    Log.d("TAG", "run: " + applicationState.getAppInForeground() + applicationState.getScreenOn());
                    if (applicationState.getScreenOn()) {
                        studyTimerRunning = false;
                        Log.d("Study", "run: " + studyTimerRunning);
                    }
                }
                if (studyTimerRunning) { studyDuration++; }

                handler.postDelayed(this, 1000);
            }
        });
    }
}