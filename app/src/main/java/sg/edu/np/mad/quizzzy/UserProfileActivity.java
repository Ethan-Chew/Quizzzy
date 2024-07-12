package sg.edu.np.mad.quizzzy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

import sg.edu.np.mad.quizzzy.Flashlets.CreateFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletDetail;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.RecyclerViewInterface;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;
import sg.edu.np.mad.quizzzy.Search.SearchActivity;

public class UserProfileActivity extends AppCompatActivity implements RecyclerViewInterface {
    // Initialisation of Firebase Cloud Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    Gson gson = new Gson();
    RecyclerView flashletRecyclerView;

    // Data Variables
    User user;
    ArrayList<Flashlet> flashlets = new ArrayList<Flashlet>();

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
                }  else if (itemId == R.id.search) {
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

        // Get User from Intent
        Intent receiveIntent = getIntent();
        user = gson.fromJson(receiveIntent.getStringExtra("userJSON"), User.class);

        // Get Currently Logged In User
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(UserProfileActivity.this);
        UserWithRecents loggedInUser = localDB.getUser();

        // Set Username Text
        TextView usernameLbl = findViewById(R.id.uPUsername);
        usernameLbl.setText(user.getUsername());

        // Retrieve Flashlets from Database
        flashletRecyclerView = findViewById(R.id.uPRecyclerView);
        if (!user.getCreatedFlashlets().isEmpty()) {
            db.collection("flashlets").whereIn("id", user.getCreatedFlashlets()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String flashletJson = gson.toJson(document.getData());
                            Flashlet flashlet = gson.fromJson(flashletJson, Flashlet.class);
                            // If the Profile's User is the same as the current user, show all flashlets. Else, only show public flashlets
                            if (!Objects.equals(loggedInUser.getUser().getId(), user.getId())) {
                                if (flashlet.getIsPublic()) {
                                    flashlets.add(flashlet);
                                }
                            } else {
                                flashlets.add(flashlet);
                            }
                        }

                        // Update Flashlet Count
                        TextView flashletCountLbl = findViewById(R.id.sURFlashletCount);
                        String flashletCount = flashlets.size() + " Flashlets";
                        flashletCountLbl.setText(flashletCount);

                        /// Display Flashlet List on Screen
                        ProfileFlashletAdapter adapter = new ProfileFlashletAdapter(flashlets, UserProfileActivity.this);
                        LinearLayoutManager userLayoutManager = new LinearLayoutManager(UserProfileActivity.this);
                        flashletRecyclerView.setLayoutManager(userLayoutManager);
                        flashletRecyclerView.setItemAnimator(new DefaultItemAnimator());
                        flashletRecyclerView.setAdapter(adapter);
                    } else {
                        Log.d("Firebase", "Flashlet get failed with ", task.getException());
                    }
                }
            });
        }
    }

    @Override
    public void onItemClick(int position) {
        Log.d("item click", String.valueOf(position));
        String flashletJson = gson.toJson(flashlets.get(position));
        Intent sendToFlashletDetail = new Intent(UserProfileActivity.this, FlashletDetail.class);
        sendToFlashletDetail.putExtra("flashletJSON", flashletJson);
    }
}