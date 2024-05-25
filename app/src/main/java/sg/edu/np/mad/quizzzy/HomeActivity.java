package sg.edu.np.mad.quizzzy;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Flashlets.CreateFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletDetail;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;

public class HomeActivity extends AppCompatActivity  {
    // Initialisation of Firebase Cloud Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Gson gson = new Gson();

    UserWithRecents userWithRecents;
    ArrayList<Flashlet> recentlyViewedFlashlets = new ArrayList<>();
    ArrayList<Flashlet> createdFlashlets = new ArrayList<>();

    // View Components
    TextView usernameView;
    LinearLayout horiRecentlyViewed;
    LinearLayout createdFlashletsContainer;

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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.home);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.home) {
                    return true;
                } else if (itemId == R.id.create) {
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
                    // TODO: Integrate Darius's Part
                    return true;
                }
                return false;
            }
        });

        // Get User from SQLite
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(HomeActivity.this);
        userWithRecents = localDB.getUser();
        /// If User is somehow null, return user back to login page
        if (userWithRecents == null) {
            Intent returnToLoginIntent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(returnToLoginIntent);
        }

        // Set Home Screen Data
        usernameView = findViewById(R.id.hPUsernameText);
        horiRecentlyViewed = findViewById(R.id.hPHoriRecentlyViewed);
        createdFlashletsContainer = findViewById(R.id.hPCFContainer);

        usernameView.setText(userWithRecents.getUser().getUsername());

        // If there are no Recently Viewed, display text
        ArrayList<String> recentlyOpenedFlashletsIds = userWithRecents.getRecentlyOpenedFlashlets();
        CollectionReference flashletColRef = db.collection("flashlets");
        if (recentlyOpenedFlashletsIds.isEmpty()) {
            View noRecentlyViewed = LayoutInflater.from(HomeActivity.this).inflate(R.layout.flashlet_recently_viewed, null, false);
            TextView nRVTitle = noRecentlyViewed.findViewById(R.id.fRVTitle);
            nRVTitle.setText("No Recently Viewed");
            TextView nRVDesc = noRecentlyViewed.findViewById(R.id.fRVDescription);
            nRVDesc.setText("");
            horiRecentlyViewed.addView(noRecentlyViewed);
        } else {
            // Get Data from Firebase
            /// Get RecentlyViewedFlashlets
            flashletColRef.whereIn("id", recentlyOpenedFlashletsIds).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String flashletJson = gson.toJson(document.getData());
                                    recentlyViewedFlashlets.add(gson.fromJson(flashletJson, Flashlet.class));
                                }

                                // Display Recently Viewed Flashlet and Display on Screen
                                for (int i = 0; i < recentlyViewedFlashlets.size(); i++) {
                                    View recentlyViewedView = LayoutInflater.from(HomeActivity.this).inflate(R.layout.flashlet_recently_viewed, null, false);
                                    Flashlet flashlet = recentlyViewedFlashlets.get(i);
                                    TextView nRVTitle = recentlyViewedView.findViewById(R.id.fRVTitle);
                                    nRVTitle.setText(flashlet.getTitle());
                                    TextView nRVDesc = recentlyViewedView.findViewById(R.id.fRVDescription);

                                    recentlyViewedView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent sendToRecentlyViewed = new Intent(HomeActivity.this, FlashletDetail.class);
                                            sendToRecentlyViewed.putExtra("flashletJSON", gson.toJson(flashlet));
                                            startActivity(sendToRecentlyViewed);
                                        }
                                    });
                                    horiRecentlyViewed.addView(recentlyViewedView);

                                    // Add Spacer View
                                    View spacerView = new View(HomeActivity.this);
                                    LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                                            30,
                                            LinearLayout.LayoutParams.MATCH_PARENT
                                    );
                                    horiRecentlyViewed.addView(spacerView, spacerParams);
                                }
                            } else {
                                Log.e("Firebase", "Error getting Recently Viewed Flashlets");
                            }
                        }
                    });
        }

        /// Get User's Created Flashlets
        flashletColRef.whereIn("id", userWithRecents.getUser().getCreatedFlashlets()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String flashletJson = gson.toJson(document.getData());
                                createdFlashlets.add(gson.fromJson(flashletJson, Flashlet.class));
                            }

                            // Display Created Flashets on the Screen
                            for (int i = 0; i < createdFlashlets.size(); i++) {
                                View flashletView = LayoutInflater.from(HomeActivity.this).inflate(R.layout.homescreen_class_flashlet_container, null, false);
                                Flashlet flashlet = createdFlashlets.get(i);
                                TextView fVTitle = flashletView.findViewById(R.id.hSCTitle);
                                TextView fVPill = flashletView.findViewById(R.id.hSCPill);
                                TextView fVDesc = flashletView.findViewById(R.id.hSCDesc);

                                // Bring user to Flashlet on Click
                                flashletView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent showFlashletDetail = new Intent(HomeActivity.this, FlashletDetail.class);
                                        showFlashletDetail.putExtra("flashletJSON", gson.toJson(flashlet));
                                        startActivity(showFlashletDetail);
                                    }
                                });

                                // Set Text
                                fVTitle.setText(flashlet.getTitle());
                                String pillText = flashlet.getFlashcards().size() + " Keyword" + (flashlet.getFlashcards().size() == 0 ? "" : "s");
                                fVPill.setText(pillText);

                                createdFlashletsContainer.addView(flashletView);

                                // Add Spacer View
                                View spacerView = new View(HomeActivity.this);
                                LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                                        20,
                                        LinearLayout.LayoutParams.MATCH_PARENT
                                );
                                createdFlashletsContainer.addView(spacerView, spacerParams);
                            }

                            ProgressBar loader = findViewById(R.id.hSSpinner);
                            loader.setVisibility(View.GONE);
                        } else {
                            Log.e("Firebase", "Error getting User Created Flashlets");
                        }
                    }
                });
    }
}