package sg.edu.np.mad.quizzzy;

import android.content.Intent;
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
import com.google.gson.Gson;

import sg.edu.np.mad.quizzzy.Flashlets.CreateFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.Models.User;

public class UserProfileActivity extends AppCompatActivity {

    Gson gson = new Gson();

    // Data Variables
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Handle Back Navigation Toolbar
        Toolbar toolbar = findViewById(R.id.uPViewToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfileActivity.this.getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // Handle Bottom Navigation View
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
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
                }  else if (itemId == R.id.create) {
                    Intent createFlashletIntent = new Intent(getApplicationContext(), CreateFlashlet.class);
                    createFlashletIntent.putExtra("userId", "");
                    startActivity(createFlashletIntent);
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

        // Get User from Intent
        Intent receiveIntent = getIntent();
        user = gson.fromJson(receiveIntent.getStringExtra("userJSON"), User.class);

        // Set View Text
        TextView usernameLbl = findViewById(R.id.uPUsername);
        TextView flashletCountLbl = findViewById(R.id.uPFlashletCount);

        usernameLbl.setText(user.getUsername());
        String flashletCount = user.getCreatedFlashlets().size() + " Flashlets";
        flashletCountLbl.setText(flashletCount);


    }
}