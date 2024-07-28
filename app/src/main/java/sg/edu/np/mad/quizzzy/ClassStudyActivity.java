package sg.edu.np.mad.quizzzy;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.Models.UserClass;
import sg.edu.np.mad.quizzzy.Search.SearchActivity;

public class ClassStudyActivity extends AppCompatActivity {
    UserClass userClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_class_study);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get intent data
        Intent receivingIntent = getIntent();
        String classTitle = receivingIntent.getStringExtra("classTitle");
        ArrayList<String> classMembers = receivingIntent.getStringArrayListExtra("classMembers");
        ArrayList<String> userIds = receivingIntent.getStringArrayListExtra("classMemberId");

        // Configure Back Button
        Toolbar toolbar = findViewById(R.id.classStudyToolbar);
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

        TextView classTitleView = findViewById(R.id.ClassTitle);
        Button studyMode = findViewById(R.id.StudyModeButton);

        classTitleView.setText(classTitle + " -\nToday's Study Time");
        studyMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openStudyModeIntent = new Intent(getApplicationContext(), StudyModeActivity.class);
                startActivity(openStudyModeIntent);
            }
        });

        // Get screen width (dp)
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidthDp = (int) (displayMetrics.widthPixels / displayMetrics.density);

        Log.d("TAG", "onCreate: "+screenWidthDp);

        // Create RecyclerView using GridLayout so that icons will be in a grid
        // Divided by 150 because that is the size of the icon, so that we make use of differently sized screens
        RecyclerView recyclerView = findViewById(R.id.ClassStudyRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, Math.floorDiv(screenWidthDp, 150)));
        ClassStudyAdapter adapter = new ClassStudyAdapter(this, classMembers, userIds);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }
}