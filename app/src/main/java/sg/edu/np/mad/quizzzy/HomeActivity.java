package sg.edu.np.mad.quizzzy;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
                    Intent createFlashletIntent = new Intent(HomeActivity.this, CreateFlashlet.class);
                    createFlashletIntent.putExtra("userId", "");
                    startActivity(createFlashletIntent);
                    return true;
                } else if (itemId == R.id.flashlets) {
                    startActivity(new Intent(HomeActivity.this, FlashletList.class));
                    return true;
                } else if (itemId == R.id.stats) {
//                    getSupportFragmentManager()
//                            .beginTransaction()
//                            .replace(R.id.flFragment, statsFragment)
//                            .commit();
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

        usernameView.setText(userWithRecents.getUser().getUsername());

        // If there are no Recently Viewed, display text
        ArrayList<String> recentlyOpenedFlashletsIds = userWithRecents.getRecentlyOpenedFlashlets();
        Log.d("1", recentlyOpenedFlashletsIds.toString());
        if (recentlyOpenedFlashletsIds.isEmpty()) {
            View noRecentlyViewed = LayoutInflater.from(HomeActivity.this).inflate(R.layout.flashlet_recently_viewed, null, false);
            TextView nRVTitle = noRecentlyViewed.findViewById(R.id.fRVTitle);
            nRVTitle.setText("No Recently Viewed");
            TextView nRVDesc = noRecentlyViewed.findViewById(R.id.fRVDescription);
            nRVDesc.setText("");
            horiRecentlyViewed.addView(noRecentlyViewed);
        } else {
            Log.d("2", "please work");
            // Get Data from Firebase
            CollectionReference flashletColRef = db.collection("flashlets");
            /// Get RecentlyViewedFlashlets
            flashletColRef.whereIn("id", recentlyOpenedFlashletsIds).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Log.d("3", "please work");
                            if (task.isSuccessful()) {
                                Log.d("4", "please work");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String flashletJson = gson.toJson(document.getData());
                                    Log.d("5", flashletJson);
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
                                }
                            } else {
                                Log.e("Firebase", "Error getting Recently Viewed Flashlets");
                            }
                        }
                    });
        }

        /// Get User's Created Flashlets
//        flashletColRef.whereIn("id", userWithRecents.getUser().getCreatedFlashlets()).get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                String flashletJson = gson.toJson(document.getData());
//                                createdFlashlets.add(gson.fromJson(flashletJson, Flashlet.class));
//                            }
//
//                            // Display Created Flashets on the Screen
//
//                        } else {
//                            Log.e("Firebase", "Error getting User Created Flashlets");
//                        }
//                    }
//                });
    }
}