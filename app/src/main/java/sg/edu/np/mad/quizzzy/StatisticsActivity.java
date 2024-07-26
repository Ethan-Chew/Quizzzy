package sg.edu.np.mad.quizzzy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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

import sg.edu.np.mad.quizzzy.Flashlets.CreateClassFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.CreateFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Search.SearchActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.components.XAxis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configure Back Button
        Toolbar toolbar = findViewById(R.id.statsToolbar);
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

        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(StatisticsActivity.this);
        // Get data from DB and call function to calculate all statistics
        HashMap<String, Integer> statistics = localDB.calculateStatistics();

        // Get all statistics from statistics HashMap
        int todayFlashcardUsage = statistics.get("todayFlashcardUsage");
        int todayFlashletUsage = statistics.get("todayFlashletUsage");
        int todayClassUsage = statistics.get("todayClassUsage");

        int totalFlashcardUsage = statistics.get("totalFlashcardUsage");
        int averageFlashcardUsage = statistics.get("averageFlashcardUsage");


        int totalFlashletUsage = statistics.get("totalFlashletUsage");
        int averageFlashletUsage = statistics.get("averageFlashletUsage");

        int totalClassUsage = statistics.get("totalClassUsage");
        int averageClassUsage = statistics.get("averageClassUsage");

        int flashcardsViewedToday = statistics.get("flashcardsViewedToday");
        int flashcardsViewedTotal = statistics.get("flashcardsViewedTotal");
        int flashcardsViewedAverage = statistics.get("flashcardsViewedAverage");

        // Update text details into relevant TextViews
        TextView todayTotal = findViewById(R.id.statsTodayTotal);
        todayTotal.setText(String.valueOf(todayFlashcardUsage + todayFlashletUsage + todayClassUsage));

        TextView todayFlashcardView = findViewById(R.id.statsFlashcardToday);
        TextView flashcardViewTotal = findViewById(R.id.statsFlashcardWeekTotal);
        TextView flashcardViewAverage = findViewById(R.id.statsFlashcardWeekAverage);

        todayFlashcardView.setText(String.valueOf(flashcardsViewedToday));
        flashcardViewTotal.setText(String.valueOf(flashcardsViewedTotal));
        flashcardViewAverage.setText(String.valueOf(flashcardsViewedAverage));

        TextView todayFlashcard = findViewById(R.id.statsTodayFlashcard);
        TextView todayFlashlet = findViewById(R.id.statsTodayFlashlet);
        TextView todayClass = findViewById(R.id.statsTodayClass);

        todayFlashcard.setText(String.valueOf(todayFlashcardUsage));
        todayFlashlet.setText(String.valueOf(todayFlashletUsage));
        todayClass.setText(String.valueOf(todayClassUsage));

        TextView weekTotal = findViewById(R.id.statsWeekTotal);
        TextView weekAverage = findViewById(R.id.statsWeekAverage);

        weekTotal.setText(String.valueOf(Math.floorDiv(totalFlashcardUsage + totalFlashletUsage + totalClassUsage, 60)));
        weekAverage.setText(String.valueOf(averageFlashcardUsage + averageFlashletUsage + averageClassUsage));

        TextView weekFlashcardTotal = findViewById(R.id.statsWeekTotalFlashcard);
        TextView weekFlashletTotal = findViewById(R.id.statsWeekTotalFlashlet);
        TextView weekClassTotal = findViewById(R.id.statsWeekTotalClass);

        weekFlashcardTotal.setText(String.valueOf(Math.floorDiv(totalFlashcardUsage, 60)));
        weekFlashletTotal.setText(String.valueOf(Math.floorDiv(totalFlashletUsage, 60)));
        weekClassTotal.setText(String.valueOf(Math.floorDiv(totalClassUsage, 60)));

        TextView weekFlashcardAvg = findViewById(R.id.statsWeekAvgFlashcard);
        TextView weekFlashletAvg = findViewById(R.id.statsWeekAvgFlashlet);
        TextView weekClassAvg = findViewById(R.id.statsWeekAvgClass);

        weekFlashcardAvg.setText(String.valueOf(averageFlashcardUsage));
        weekFlashletAvg.setText(String.valueOf(averageFlashletUsage));
        weekClassAvg.setText(String.valueOf(averageClassUsage));

        //BarChart
        BarChart barChart = findViewById(R.id.barChart);

        //Adding data for barchart into an ArrayList
        ArrayList<BarEntry> entries = new ArrayList<>();

        final List<String> daysOfWeek = new ArrayList<>();
        daysOfWeek.add("Sun");
        daysOfWeek.add("Mon");
        daysOfWeek.add("Tue");
        daysOfWeek.add("Wed");
        daysOfWeek.add("Thu");
        daysOfWeek.add("Fri");
        daysOfWeek.add("Sat");

        //Change null data to zero
        for (int i = 0; i < daysOfWeek.size(); i++) {
            String day = daysOfWeek.get(i);
            statistics.putIfAbsent(day, 0);
            Integer value = statistics.get(day);
            entries.add(new BarEntry(i, value)); // Replace null with 0
        }

        BarDataSet barDataSet = new BarDataSet(entries, "Usage");
        barDataSet.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        barDataSet.setValueTextColor(Color.BLACK);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.getDescription().setText("Weekly Flashlet Usage");

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new StatisticsActivity.DayAxisValueFormatter(daysOfWeek));
        xAxis.setGranularity(1f); //interval
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-45f);

        barChart.getAxisLeft().setGranularity(1f); //interval
        barChart.getAxisRight().setEnabled(false); //remove right y-axis

        barChart.setFitBars(true);
        barChart.invalidate(); //refresh the chart
    }

    private static class DayAxisValueFormatter extends ValueFormatter{
        private final List<String> daysOfWeek;

        public DayAxisValueFormatter(List<String> daysOfWeek) {
            this.daysOfWeek = daysOfWeek;
        }

        @Override
        public String getFormattedValue(float value) {
            return daysOfWeek.get((int) value % daysOfWeek.size());
        }
    }
}