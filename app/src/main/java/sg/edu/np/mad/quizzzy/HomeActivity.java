package sg.edu.np.mad.quizzzy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import sg.edu.np.mad.quizzzy.Classes.ClassList;
import sg.edu.np.mad.quizzzy.Flashlets.CreateFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletDetail;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.PushNotificationService;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.SQLiteRecentSearchesManager;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;
import sg.edu.np.mad.quizzzy.Search.SearchActivity;

/** <strong>Home Screen</strong> <br/>
 * Once Logged In, the User will be able to view their Recently Viewed Flashlets, Created Flashlets, and access the list of Classes <br/>
 * <strong>Recently Viewed Flashlets</strong> <br/>
 * Recently Viewed Flashlets are stored locally, and will be reset if the user logs out of their account.
 * If there are no Recently Viewed Flashlets, a 'No Recently Viewed' will be shown.
 * Else, a list of Recently Viewed Flashlets would be displayed. Users can scroll horizontally to view the full list.
 */

public class HomeActivity extends AppCompatActivity  {
    // Initialisation of Firebase Cloud Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SQLiteManager localDB;
    Gson gson = new Gson();

    UserWithRecents userWithRecents;
    ArrayList<Flashlet> recentlyViewedFlashlets = new ArrayList<>();
    ArrayList<Flashlet> createdFlashlets = new ArrayList<>();
    ArrayList<String> recentlyOpenedFlashletsIds = new ArrayList<>();

    // View Components
    TextView usernameView;
    LinearLayout horiRecentlyViewed;
    LinearLayout createdFlashletsContainer;

    ImageView dropdownMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.hSConstrainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Customise Bottom Navigation Bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnApplyWindowInsetsListener(null);
        bottomNavigationView.setPadding(0,0,0,0);

        // Handle Tabbing for Bottom Nav Bar
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.home) {
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

        // Check if User is Connected to the Network; if not, tell them it is required
        ConnectivityManager cm = (ConnectivityManager)getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isNetworkConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isNetworkConnected) {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder
                    .setTitle("Not connected to Internet!")
                    .setMessage("For Quizzzy to function normally, your device has to be connected to the internet! Most features will not work as intended without internet.")
                    .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            
                        }
                    });
            builder.create();
        }

        // Get User from SQLite
        localDB = SQLiteManager.instanceOfDatabase(HomeActivity.this);
        SQLiteRecentSearchesManager recentSearchesDB = SQLiteRecentSearchesManager.instanceOfDatabase(HomeActivity.this);
        userWithRecents = localDB.getUser();
        /// If User is somehow null, return user back to login page
        if (userWithRecents == null) {
            Intent returnToLoginIntent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(returnToLoginIntent);
        }

        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(HomeActivity.this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS},101);
            }
        }

        // Subscribe to Firebase FCM
        PushNotificationService pushNotificationService = new PushNotificationService();
        pushNotificationService.subscribeToUserIDTopic(userWithRecents.getUser().getId());

        // Set Home Screen Data
        usernameView = findViewById(R.id.hPUsernameText);
        horiRecentlyViewed = findViewById(R.id.hPHoriRecentlyViewed);
        createdFlashletsContainer = findViewById(R.id.hPCFContainer);
        dropdownMenu = findViewById(R.id.dropdownMenu);

        //dropdown menu to logout
        dropdownMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(HomeActivity.this, v);
                popup.inflate(R.menu.home_dropdown_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.logout) {
                            localDB.dropUser(userWithRecents.getUser().getId());
                            recentSearchesDB.dropAllSearchQuery();
                            FirebaseAuth.getInstance().signOut();
                            // Unsubscribe from Firebase FCM
                            PushNotificationService pushNotificationService = new PushNotificationService();
                            pushNotificationService.unsubscribeFromUserIDTopic(userWithRecents.getUser().getId());
                            startActivity(new Intent(HomeActivity.this, MainActivity.class));
                        } else if (itemId == R.id.profile) {
                            Intent profileIntent = new Intent(HomeActivity.this, UserProfileActivity.class);
                            profileIntent.putExtra("userJSON", gson.toJson(userWithRecents.getUser()));
                            startActivity(profileIntent);
                        }
                        return true;
                    }
                }); 
                popup.show();
            }
        });

        // Set the User's Username on the Header
        usernameView.setText(userWithRecents.getUser().getUsername());
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView showClassList = findViewById(R.id.hSClassList);
        showClassList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showclassintent = new Intent(getApplicationContext(), ClassList.class);
                startActivity(showclassintent);
            }
        });

        TextView openStudyMode = findViewById(R.id.hSStudyMode);
        openStudyMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openStudyModeIntent = new Intent(getApplicationContext(), StudyModeActivity.class);
                startActivity(openStudyModeIntent);
            }
        });

        // Reset Data
        horiRecentlyViewed.removeAllViews(); // Clear the Recently Viewed List
        createdFlashletsContainer.removeAllViews(); // Clear the List of Created Flashlets
        recentlyViewedFlashlets.clear();
        createdFlashlets.clear();

        // If there are no Recently Viewed, display text
        recentlyOpenedFlashletsIds = userWithRecents.getRecentlyOpenedFlashlets();
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

                                if (recentlyViewedFlashlets.isEmpty()) {
                                    recentlyOpenedFlashletsIds.clear();
                                    localDB.updateRecentlyViewed(userWithRecents.getUser().getId(), recentlyOpenedFlashletsIds);
                                } else {
                                    // Retrieve Usernames for the Flashlet Owners
                                    CollectionReference userColRef = db.collection("users");
                                    userColRef
                                            .whereIn("id", recentlyViewedFlashlets.stream().map(flashlet -> flashlet.getCreatorID().get(0)).collect(Collectors.toList()))
                                            .get()
                                            .addOnCompleteListener(userTask -> {
                                                if (userTask.isSuccessful()) {
                                                    Map<String, String> userMap = new HashMap<>();

                                                    for (QueryDocumentSnapshot document : userTask.getResult()) {
                                                        String userJson = gson.toJson(document.getData());
                                                        User user = gson.fromJson(userJson, User.class);
                                                        userMap.put(user.getId(), user.getUsername());
                                                    }

                                                    // Display Recently Viewed Flashlet and Display on Screen
                                                    for (int i = 0; i < recentlyViewedFlashlets.size(); i++) {
                                                        View recentlyViewedView = LayoutInflater.from(HomeActivity.this).inflate(R.layout.flashlet_recently_viewed, null, false);
                                                        Flashlet flashlet = recentlyViewedFlashlets.get(i);
                                                        TextView nRVTitle = recentlyViewedView.findViewById(R.id.fRVTitle);
                                                        nRVTitle.setText(flashlet.getTitle());
                                                        TextView nRVDesc = recentlyViewedView.findViewById(R.id.fRVDescription);
                                                        nRVDesc.setText(userMap.get(flashlet.getCreatorID().get(0)));

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
                                                    Toast.makeText(HomeActivity.this, "Failed to get Usernames for Recently Viewed", Toast.LENGTH_SHORT).show();
                                                }
                                            });


                                }
                            } else {
                                Log.e("Firebase", "Error getting Recently Viewed Flashlets");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(HomeActivity.this, "Failed to get Usernames for Recently Viewed", Toast.LENGTH_SHORT).show();
                    });
        }

        /// Get User's Created Flashlets
        ProgressBar loader = findViewById(R.id.hSSpinner);
        if (!userWithRecents.getUser().getCreatedFlashlets().isEmpty()) {
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
                                            showFlashletDetail.putExtra("userId", userWithRecents.getUser().getId());
                                            startActivity(showFlashletDetail);
                                        }
                                    });

                                    // Set Text
                                    fVTitle.setText(flashlet.getTitle());
                                    String pillText = flashlet.getFlashcards().size() + " Keyword" + (flashlet.getFlashcards().size() == 0 ? "" : "s");
                                    fVPill.setText(pillText);
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
                                    String formattedLastUpdate = "Last Updated: " + sdf.format(flashlet.getLastUpdatedUnix() * 1000L);
                                    fVDesc.setText(formattedLastUpdate);
                                    createdFlashletsContainer.addView(flashletView);

                                    // Add Spacer View
                                    View spacerView = new View(HomeActivity.this);
                                    LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                                            20,
                                            LinearLayout.LayoutParams.MATCH_PARENT
                                    );
                                    createdFlashletsContainer.addView(spacerView, spacerParams);
                                }

                                loader.setVisibility(View.GONE);
                            } else {
                                Log.e("Firebase", "Error getting User Created Flashlets");
                            }
                        }
                    });
        } else {
            // Show No Flashlets Component
            LinearLayout noFlashletsComponent = findViewById(R.id.hSNoFlashlets);
            noFlashletsComponent.setVisibility(View.VISIBLE);

            /// Use Intent to Navigate from Create Flashlet btnn
            Button noFlashletCreate = findViewById(R.id.hSNoFlashletsCreate);
            noFlashletCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sendToCreate = new Intent(getApplicationContext(), CreateFlashlet.class);
                    sendToCreate.putExtra("userId", userWithRecents.getUser().getId());
                    startActivity(sendToCreate);
                }
            });

            /// Re-Constrain Class Title
            ConstraintLayout constraintLayout = findViewById(R.id.hSContainerConstraint);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.clear(R.id.hPClassesTitle, ConstraintSet.TOP);
            constraintSet.connect(R.id.hPClassesTitle, ConstraintSet.TOP, R.id.hSNoFlashlets, ConstraintSet.BOTTOM, 20);
            constraintSet.applyTo(constraintLayout);

            // Remove Loader
            loader.setVisibility(View.GONE);
        }
    }
}