package sg.edu.np.mad.quizzzy.Flashlets;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Flashlets.Recycler.FlashletListAdapter;
import sg.edu.np.mad.quizzzy.Flashlets.Recycler.FlashletListRecyclerInterface;
import sg.edu.np.mad.quizzzy.HomeActivity;
import sg.edu.np.mad.quizzzy.MainActivity;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.UsageStatistic;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;
import sg.edu.np.mad.quizzzy.R;
import sg.edu.np.mad.quizzzy.StatisticsActivity;

public class FlashletList extends AppCompatActivity implements FlashletListRecyclerInterface {
    // Data
    ArrayList<Flashlet> userFlashlets = new ArrayList<Flashlet>();
    UserWithRecents userWithRecents;
    Gson gson = new Gson();

    // Initialisation of Firebase Cloud Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flashlet_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashTitle), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get User from SQLite DB
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(FlashletList.this);
        userWithRecents = localDB.getUser();

        // Create new UsageStatistic class and start the update loop
        UsageStatistic usage = new UsageStatistic();
        localDB.updateStatisticsLoop(usage, 1, userWithRecents.getUser().getId());

        // Bottom Navigation View
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.flashlets);
        bottomNavigationView.setOnApplyWindowInsetsListener(null);
        bottomNavigationView.setPadding(0,0,0,0);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                // Save statistics to SQLite DB before changing Activity.
                // timeType of 1 because this is a Flashlet Activity
                localDB.updateStatistics(usage, 1, userWithRecents.getUser().getId());

                if (itemId == R.id.home) {
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.create) {
                    Intent createFlashletIntent = new Intent(getApplicationContext(), CreateFlashlet.class);
                    createFlashletIntent.putExtra("userId", "");
                    startActivity(createFlashletIntent);
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.flashlets) {
                    return true;
                } else if (itemId == R.id.stats) {
                    startActivity(new Intent(getApplicationContext(), StatisticsActivity.class));
                    return true;
                }
                return false;
            }
        });

        RecyclerView recyclerView = findViewById(R.id.fLRecyclerView);
        LinearLayout noFlashletNotif = findViewById(R.id.fLNoFlashlets);

        // Handle Back Navigation Toolbar
        Toolbar toolbar = findViewById(R.id.fLViewToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save statistics to SQLite DB before changing Activity.
                // timeType of 1 because this is a Flashlet Activity
                localDB.updateStatistics(usage, 1, userWithRecents.getUser().getId());

                FlashletList.this.getOnBackPressedDispatcher().onBackPressed();
            }
        });

        /// If User is somehow null, return user back to login page
        if (userWithRecents == null) {
            Intent returnToLoginIntent = new Intent(FlashletList.this, MainActivity.class);

            // Save statistics to SQLite DB before changing Activity.
            // timeType of 1 because this is a Flashlet Activity
            localDB.updateStatistics(usage, 1, userWithRecents.getUser().getId());

            startActivity(returnToLoginIntent);
        }

        // Listen to Add Flashlet Button Click
        TextView createFlashlet = findViewById(R.id.fLNewFlashlet);
        createFlashlet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlashletList.this.getOnBackPressedDispatcher().onBackPressed();
                Intent createFlashcardIntent = new Intent(FlashletList.this, CreateFlashlet.class);
                createFlashcardIntent.putExtra("userId", userWithRecents.getUser().getId());

                // Save statistics to SQLite DB before changing Activity.
                // timeType of 1 because this is a Flashlet Activity
                localDB.updateStatistics(usage, 1, userWithRecents.getUser().getId());

                startActivity(createFlashcardIntent);
            }
        });

        // Update User Interface with Updated Data
        ArrayList<String> userFlashletIDs = userWithRecents.getUser().getCreatedFlashlets();

        TextView flashletCount = findViewById(R.id.fLCounterLabel);
        String flashletCountStr = "You have " + userFlashletIDs.size() + " Total Flashlet" + (userFlashletIDs.size() == 1 ? "" : "s");
        flashletCount.setText(flashletCountStr);

        // Check if the User has any Flashlets; If not, ask them to create one
        if (userFlashletIDs.isEmpty()) {
            Button nFNCreateFlashlet = findViewById(R.id.fLNoFlashletsCreate);
            nFNCreateFlashlet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent createFlashletIntent = new Intent(FlashletList.this, CreateFlashlet.class);
                    createFlashletIntent.putExtra("userId", userWithRecents.getUser().getId());

                    // Save statistics to SQLite DB before changing Activity.
                    // timeType of 1 because this is a Flashlet Activity
                    localDB.updateStatistics(usage, 1, userWithRecents.getUser().getId());

                    startActivity(createFlashletIntent);
                }
            });
            recyclerView.setVisibility(View.GONE);
            noFlashletNotif.setVisibility(View.VISIBLE);
            findViewById(R.id.fLProgressBar).setVisibility(View.GONE);
            return;
        }

        // Get User's Flashlets from Firebase
        CollectionReference flashletColRef = db.collection("flashlets");
        flashletColRef.whereIn("id", userFlashletIDs).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String flashletJson = gson.toJson(document.getData());
                        userFlashlets.add(gson.fromJson(flashletJson, Flashlet.class));
                    }

                    /// Display Flashlet List on Screen
                    noFlashletNotif.setVisibility(View.GONE);
                    FlashletListAdapter userAdapter = new FlashletListAdapter(userFlashlets, FlashletList.this, FlashletList.this, userWithRecents.getUser());
                    LinearLayoutManager userLayoutManager = new LinearLayoutManager(FlashletList.this);
                    recyclerView.setLayoutManager(userLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(userAdapter);
                    findViewById(R.id.fLProgressBar).setVisibility(View.GONE);
                } else {
                    Log.d("Firebase", "Flashlet get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        String flashletJson = gson.toJson(userFlashlets.get(position));
        Intent sendToFlashletDetail = new Intent(FlashletList.this, FlashletDetail.class);
        sendToFlashletDetail.putExtra("flashletJSON", flashletJson);
        startActivity(sendToFlashletDetail);
    }
}