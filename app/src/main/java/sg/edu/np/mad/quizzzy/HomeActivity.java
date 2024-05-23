package sg.edu.np.mad.quizzzy;

import android.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import sg.edu.np.mad.quizzzy.Fragments.CreateFragment;
import sg.edu.np.mad.quizzzy.Fragments.FlashletsFragment;
import sg.edu.np.mad.quizzzy.Fragments.HomeFragment;
import sg.edu.np.mad.quizzzy.Fragments.StatsFragment;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.User;

public class HomeActivity extends AppCompatActivity  {

    BottomNavigationView bottomNavigationView;
    TextView idView;
    TextView usernameView;
    TextView emailView;
    HomeFragment homeFragment = new HomeFragment();
    CreateFragment createFragment = new CreateFragment();
    FlashletsFragment flashletsFragment = new FlashletsFragment();
    StatsFragment statsFragment = new StatsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashTitle), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
             v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, homeFragment).commit();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.home) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.flFragment, homeFragment)
                            .commit();
                    return true;
                } else if (itemId == R.id.create) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.flFragment, createFragment)
                            .commit();
                    return true;
                } else if (itemId == R.id.flashlets) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.flFragment, flashletsFragment)
                            .commit();
                    return true;
                } else if (itemId == R.id.stats) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.flFragment, statsFragment)
                            .commit();
                    return true;
                }
                return false;
            }
        });

    }

}