package sg.edu.np.mad.quizzzy.Flashlets;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Flashlets.Recycler.FlashletListAdapter;
import sg.edu.np.mad.quizzzy.HomeActivity;
import sg.edu.np.mad.quizzzy.MainActivity;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.GeminiHandler;
import sg.edu.np.mad.quizzzy.Models.GeminiHandlerResponse;
import sg.edu.np.mad.quizzzy.Models.GeminiResponseEventHandler;
import sg.edu.np.mad.quizzzy.Models.RecyclerViewInterface;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.UsageStatistic;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;
import sg.edu.np.mad.quizzzy.QrCodeScannerActivity;
import sg.edu.np.mad.quizzzy.R;
import sg.edu.np.mad.quizzzy.Search.SearchActivity;
import sg.edu.np.mad.quizzzy.StatisticsActivity;

public class FlashletList extends AppCompatActivity implements RecyclerViewInterface, FlashletListAdapter.FlashletCountListener {
    // Data
    ArrayList<Flashlet> userFlashlets = new ArrayList<Flashlet>();
    UserWithRecents userWithRecents;
    SQLiteManager localDB;
    UsageStatistic usage;
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
        localDB = SQLiteManager.instanceOfDatabase(FlashletList.this);
        userWithRecents = localDB.getUser();

        // Create new UsageStatistic class and start the update loop
        usage = new UsageStatistic();
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
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                if (itemId == R.id.home) {
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.search) {
                    startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.flashlets) {
                    return true;
                } else if (itemId == R.id.stats) {
                    startActivity(new Intent(getApplicationContext(), StatisticsActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                }
                return false;
            }
        });

        // Handle Back Navigation Toolbar
        Toolbar toolbar = findViewById(R.id.fLViewToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save statistics to SQLite DB before changing Activity.
                // timeType of 1 because this is a Flashlet Activity
                localDB.updateStatistics(usage, 1, userWithRecents.getUser().getId());
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                FlashletList.this.getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // Handle Back Button Click
        // Enabled is true so that the code within handleOnBackPressed will be executed
        // This also disables the back button press from going to the previous screen
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Save statistics to SQLite DB before changing Activity.
                // timeType of 1 because this is a Flashlet Activity
                localDB.updateStatistics(usage, 1, userWithRecents.getUser().getId());
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                // Enable the back button to be able to be used to go to the previous screen
                setEnabled(false);
                // Call the default back press behavior again to return to previous screen
                getOnBackPressedDispatcher().onBackPressed();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        /// If User is somehow null, return user back to login page
        if (userWithRecents == null) {
            Intent returnToLoginIntent = new Intent(FlashletList.this, MainActivity.class);

            // Save statistics to SQLite DB before changing Activity.
            // timeType of 1 because this is a Flashlet Activity
            localDB.updateStatistics(usage, 1, userWithRecents.getUser().getId());
            // Kills updateStatisticsLoop as we are switching to another activity.
            usage.setActivityChanged(true);

            startActivity(returnToLoginIntent);
        }

        // Listen to Add Flashlet Button Click
        TextView createFlashlet = findViewById(R.id.fLNewFlashlet);
        createFlashlet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(FlashletList.this, v);
                popupMenu.inflate(R.menu.create_flashlet_options);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();

                        // Save statistics to SQLite DB before changing Activity.
                        // timeType of 1 because this is a Flashlet Activity
                        localDB.updateStatistics(usage, 1, userWithRecents.getUser().getId());
                        // Kills updateStatisticsLoop as we are switching to another activity.
                        usage.setActivityChanged(true);

                        if (itemId == R.id.cFOCreate) {
                            startActivity(new Intent(FlashletList.this, CreateFlashlet.class));
                        } else if (itemId == R.id.cFOAutogenerate) {
                            handleBottomDialogView();
                        } else if (itemId == R.id.cFOJoinFlashlet) {
                            startActivity(new Intent(FlashletList.this, QrCodeScannerActivity.class));
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        RecyclerView recyclerView = findViewById(R.id.fLRecyclerView);
        LinearLayout noFlashletNotif = findViewById(R.id.fLNoFlashlets);

        /// Update User Interface with Updated Data
        ArrayList<String> userFlashletIDs = new ArrayList<>();
        userFlashletIDs.addAll(userWithRecents.getUser().getCreatedFlashlets());

        TextView flashletCount = findViewById(R.id.fLCounterLabel);
        String flashletCountStr = "You have " + userFlashletIDs.size() + " Total Flashlet" + (userFlashletIDs.size() == 1 ? "" : "s");
        flashletCount.setText(flashletCountStr);

        // Check if the User has any Flashlets; If not, ask them to create one
        if (userFlashletIDs.isEmpty()) {
            Button nFNCreateFlashlet = findViewById(R.id.fLNoFlashletsCreate);
            nFNCreateFlashlet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(FlashletList.this, v);
                    popupMenu.inflate(R.menu.create_flashlet_options);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int itemId = item.getItemId();

                            // Save statistics to SQLite DB before changing Activity.
                            // timeType of 1 because this is a Flashlet Activity
                            localDB.updateStatistics(usage, 1, userWithRecents.getUser().getId());
                            // Kills updateStatisticsLoop as we are switching to another activity.
                            usage.setActivityChanged(true);

                            if (itemId == R.id.cFOCreate) {
                                startActivity(new Intent(FlashletList.this, CreateFlashlet.class));
                            } else if (itemId == R.id.cFOAutogenerate) {
                                handleBottomDialogView();
                            } else if (itemId == R.id.cFOJoinFlashlet) {
                                startActivity(new Intent(FlashletList.this, QrCodeScannerActivity.class));
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
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
                    FlashletListAdapter userAdapter = new FlashletListAdapter(userFlashlets, FlashletList.this, FlashletList.this, userWithRecents.getUser(), FlashletList.this);
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

    // To re-initialize the DB update loop when returning to the screen
    @Override
    protected void onRestart() {
        super.onRestart();
        // Recreate the activity so that the DB update loop will be called again and be able to be terminated
        recreate();
    }

    // Handle onClick of the Flashlet List Recycler View
    @Override
    public void onItemClick(int position) {
        String flashletJson = gson.toJson(userFlashlets.get(position));
        Intent sendToFlashletDetail = new Intent(FlashletList.this, FlashletDetail.class);
        sendToFlashletDetail.putExtra("flashletJSON", flashletJson);
        sendToFlashletDetail.putExtra("userId", localDB.getUser().getUser().getId());

        // Save statistics to SQLite DB before changing Activity.
        // timeType of 1 because this is a Flashlet Activity
        localDB.updateStatistics(usage, 1, userWithRecents.getUser().getId());
        // Kills updateStatisticsLoop as we are switching to another activity.
        usage.setActivityChanged(true);

        startActivity(sendToFlashletDetail);
    }

    // Update the Flashlet Count everytime a Flashlet is deleted
    @Override
    public void flashletCount(Integer count) {
        TextView flashletCount = findViewById(R.id.fLCounterLabel);
        String flashletCountStr = "You have " + count + " Total Flashlet" + (count == 1 ? "" : "s");
        flashletCount.setText(flashletCountStr);
    }

    // Create the BottomDialogView to get the user's Search Term to be Autogenerated into a Flashlet
    private void handleBottomDialogView() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(FlashletList.this);
        View dialogView = LayoutInflater.from(FlashletList.this).inflate(R.layout.autogenerate_flashlet_bottom_sheet, null);
        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();

        // Handle Search Button Click
        TextInputEditText editText = dialogView.findViewById(R.id.aFEditText);
        Button generateBtn = dialogView.findViewById(R.id.aFGenerateBtn);
        generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Disable the Button and show loading
                generateBtn.setText("Loading...");
                generateBtn.setEnabled(false);

                // Send the Flashlet to the Gemini AI Handler and await for a response/error
                GeminiHandler.generateFlashletOnKeyword(editText.getText().toString(), new GeminiResponseEventHandler() {
                    @Override
                    public void onResponse(GeminiHandlerResponse handlerResponse) {
                        Looper.prepare();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Send Intent to CreateFlashlet
                                Intent sendToCreateFlashlet = new Intent(FlashletList.this, CreateFlashlet.class);
                                sendToCreateFlashlet.putExtra("autofilledFlashletJSON", gson.toJson(handlerResponse));
                                startActivity(sendToCreateFlashlet);

                                // Reset the Button
                                generateBtn.setText("Generate Flashlet");
                                generateBtn.setEnabled(true);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception err) {
                        Looper.prepare();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Display an Error to the User
                                Toast.makeText(FlashletList.this, err.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                // Enable the Button
                                generateBtn.setText("Generate Flashlet");
                                generateBtn.setEnabled(true);
                            }
                        });
                    }
                });
            }
        });
    }
}