package sg.edu.np.mad.quizzzy.Classes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import sg.edu.np.mad.quizzzy.ClassStudyActivity;
import sg.edu.np.mad.quizzzy.ClassStudyAdapter;
import sg.edu.np.mad.quizzzy.Flashlets.CreateClassFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.CreateFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletDetail;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.HomeActivity;
import sg.edu.np.mad.quizzzy.Models.GeminiHandler;
import sg.edu.np.mad.quizzzy.Models.GeminiHandlerResponse;
import sg.edu.np.mad.quizzzy.Models.GeminiResponseEventHandler;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.UsageStatistic;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserClass;
import sg.edu.np.mad.quizzzy.QrCodeScannerActivity;
import sg.edu.np.mad.quizzzy.R;
import sg.edu.np.mad.quizzzy.Search.SearchActivity;
import sg.edu.np.mad.quizzzy.StatisticsActivity;

public class ClassDetail extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Gson gson = new Gson();
    
    UserClass userClass;
    TextView classtitle;
    TextView memberscount;
    Button createFlashlet;
    Button studyDetails;
    ImageView editButton;
    LinearLayout memberscontainer;
    LinearLayout createdFlashletsContainer;
    ArrayList<String> createdFlashlets = new ArrayList<String>();
    ArrayList<User> users = new ArrayList<User>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_class_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Add usage statistics to local SQLite DB
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(ClassDetail.this);
        User user = localDB.getUser().getUser();

        // Create new UsageStatistic class and start the update loop
        UsageStatistic usage = new UsageStatistic();
        localDB.updateStatisticsLoop(usage, 2, user.getId());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnApplyWindowInsetsListener(null);
        bottomNavigationView.setPadding(0,0,0,0);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                // Save statistics to SQLite DB before changing Activity.
                // timeType of 2 because this is a Class Activity
                localDB.updateStatistics(usage, 2, user.getId());
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                if (itemId == R.id.home) {
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
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

        // Handle Back Navigation Toolbar
        Toolbar toolbar = findViewById(R.id.fDViewToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save statistics to SQLite DB before changing Activity.
                // timeType of 2 because this is a Class Activity
                localDB.updateStatistics(usage, 2, user.getId());
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                ClassDetail.this.getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // Handle Back Button Click
        // Enabled is true so that the code within handleOnBackPressed will be executed
        // This also disables the back button press from going to the previous screen
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Save statistics to SQLite DB before changing Activity.
                // timeType of 2 because this is a Class Activity
                localDB.updateStatistics(usage, 2, user.getId());
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                // Enable the back button to be able to be used to go to the previous screen
                setEnabled(false);
                // Call the default back press behavior again to return to previous screen
                getOnBackPressedDispatcher().onBackPressed();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        classtitle = findViewById(R.id.cdclasstitle);
        memberscount = findViewById(R.id.cdmembers);
        createFlashlet = findViewById(R.id.createFlashlet);
        studyDetails = findViewById(R.id.classStudyButton);
        memberscontainer = findViewById(R.id.cdmemberscontainer);
        createdFlashletsContainer = findViewById(R.id.hPCFContainer);
        editButton = findViewById(R.id.cDEditOption);

        // Receive data from Intent
        Intent receiveintent = getIntent();
        userClass = gson.fromJson(receiveintent.getStringExtra("classJson"), UserClass.class);
        ArrayList<String> members = userClass.getMemberId();
        ArrayList<String> memberIds = new ArrayList<>();
        ArrayList<String> memberUsernames = new ArrayList<>();
        String classId = userClass.getId();
        String userId = receiveintent.getStringExtra("userId");

        // Handle onClick of Edit Class Button
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent updateClassIntent = new Intent(ClassDetail.this, UpdateClass.class);
                updateClassIntent.putExtra("classJson", gson.toJson(userClass));

                // Save statistics to SQLite DB before changing Activity.
                // timeType of 2 because this is a Class Activity
                localDB.updateStatistics(usage, 2, user.getId());
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                startActivity(updateClassIntent);
            }
        });

        // Handle onClick of Create Flashlet Button
        createFlashlet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(ClassDetail.this, v);
                popupMenu.inflate(R.menu.create_class_flashlets_options);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();


                        if (itemId == R.id.cFOCreate) {
                            Intent createFlashletIntent = new Intent(getApplicationContext(), CreateClassFlashlet.class);
                            createFlashletIntent.putExtra("classId", classId);
                            createFlashletIntent.putExtra("userId",userId);
                            createFlashletIntent.putExtra("classJson", gson.toJson(userClass));

                            // Save statistics to SQLite DB before changing Activity.
                            // timeType of 2 because this is a Class Activity
                            localDB.updateStatistics(usage, 2, user.getId());
                            // Kills updateStatisticsLoop as we are switching to another activity.
                            usage.setActivityChanged(true);

                            startActivity(createFlashletIntent);
                        } else if (itemId == R.id.cFOAutogenerate) {
                            handleBottomDialogView(classId, userId);
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        // Handle onClick to go to the class study statistics page
        studyDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewClassStudyDetails = new Intent(getApplicationContext(), ClassStudyActivity.class);
                viewClassStudyDetails.putExtra("classTitle", userClass.getClassTitle());
                viewClassStudyDetails.putExtra("classMemberId", memberIds);
                viewClassStudyDetails.putExtra("classMembers", memberUsernames);

                // Save statistics to SQLite DB before changing Activity.
                // timeType of 2 because this is a Class Activity
                localDB.updateStatistics(usage, 2, user.getId());
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                startActivity(viewClassStudyDetails);
            }
        });

        classtitle.setText(userClass.getClassTitle());
        String membercount = members.size() + " Total Member" + (members.size() == 1 ? "" : "s");
        memberscount.setText(membercount);

        db.collection("users").whereIn("id", members).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String classJson = gson.toJson(document.getData());
                                users.add(gson.fromJson(classJson, User.class));
                            }
                            for (int i = 0; i < users.size(); i++) {
                                User user = users.get(i);
                                memberIds.add(user.getId());
                                memberUsernames.add(user.getUsername());

                                View memberView = LayoutInflater.from(ClassDetail.this).inflate(R.layout.member_list, null, false);
                                TextView memberusername = memberView.findViewById(R.id.mlusername);
                                memberusername.setText(user.getUsername());

                                memberscontainer.addView(memberView);

                                View spacerView = new View(ClassDetail.this);
                                LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        20
                                );
                                memberscontainer.addView(spacerView, spacerParams);
                            }
                        }
                    }
                });

        CollectionReference classes = db.collection("class");
        classes.whereIn("id", Collections.singletonList(classId)).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                               if (task.isSuccessful()) {
                                                   for (QueryDocumentSnapshot document : task.getResult()) {
                                                       String flashletJson = gson.toJson(document.getData());
                                                       JsonObject jsonObject = gson.fromJson(flashletJson, JsonObject.class);
                                                       JsonArray jArray = jsonObject.getAsJsonArray("createdFlashlets");
                                                       if (jArray != null) {
                                                           for (int i = 0; i < jArray.size(); i++) {
                                                               createdFlashlets.add(String.valueOf(jArray.get(i)).replace("\"", ""));
                                                           }
                                                           CollectionReference docRef = db.collection("flashlets");
                                                           try{
                                                               docRef.whereIn("id", createdFlashlets).get()



                                                                       .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                           @Override
                                                                           public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                               if (task.isSuccessful()) {
                                                                                   for (QueryDocumentSnapshot document : task.getResult()) {
                                                                                       String flashletJson = gson.toJson(document.getData());
                                                                                       JsonObject jArray = gson.fromJson( flashletJson, JsonObject.class);
                                                                                       ArrayList<JsonObject> createdFlashlets = new ArrayList<JsonObject>();
                                                                                       createdFlashlets.add(jArray);

                                                                                       // Display Created Flashets on the Screen
                                                                                       for (int i = 0; i < createdFlashlets.size(); i++) {
                                                                                           View flashletView = LayoutInflater.from(ClassDetail.this).inflate(R.layout.homescreen_class_flashlet_container, null, false);
                                                                                           JsonObject flashlet = createdFlashlets.get(i);
                                                                                           TextView fVTitle = flashletView.findViewById(R.id.hSCTitle);
                                                                                           TextView fVPill = flashletView.findViewById(R.id.hSCPill);
                                                                                           TextView fVDesc = flashletView.findViewById(R.id.hSCDesc);

                                                                                           // Bring user to Flashlet on Click
                                                                                           flashletView.setOnClickListener(new View.OnClickListener() {
                                                                                               @Override
                                                                                               public void onClick(View v) {
                                                                                                   Intent showFlashletDetail = new Intent(ClassDetail.this, FlashletDetail.class);
                                                                                                   showFlashletDetail.putExtra("flashletJSON", gson.toJson(flashlet));
                                                                                                   showFlashletDetail.putExtra("userId", userId);

                                                                                                   // Save statistics to SQLite DB before changing Activity.
                                                                                                   // timeType of 2 because this is a Class Activity
                                                                                                   localDB.updateStatistics(usage, 2, user.getId());
                                                                                                   // Kills updateStatisticsLoop as we are switching to another activity.
                                                                                                   usage.setActivityChanged(true);
                                                                                                   startActivity(showFlashletDetail);
                                                                                               }
                                                                                           });

                                                                                           // Set Text
                                                                                           fVTitle.setText(String.valueOf(flashlet.get("title")).replace("\"", ""));
                                                                                           String pillText = flashlet.get("flashcards").getAsJsonArray().size() + " Keyword" + (flashlet.get("flashcards").getAsJsonArray().size() == 0 ? "" : "s");
                                                                                           fVPill.setText(pillText);
                                                                                           SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
                                                                                           String formattedLastUpdate = "Last Updated: " + sdf.format(flashlet.get("lastUpdatedUnix").getAsInt() * 1000L);
                                                                                           fVDesc.setText(formattedLastUpdate);

                                                                                           createdFlashletsContainer.addView(flashletView);

                                                                                           // Add Spacer View
                                                                                           View spacerView = new View(ClassDetail.this);
                                                                                           LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                                                                                                   20,
                                                                                                   LinearLayout.LayoutParams.MATCH_PARENT
                                                                                           );
                                                                                           createdFlashletsContainer.addView(spacerView, spacerParams);
                                                                                       }
                                                                                   }
                                                                               } else {
                                                                                   Log.e("Firebase", "Error getting User Created Flashlets");
                                                                               }
                                                                           }
                                                                       });
                                                           }catch (Exception e) {
                                                               Log.e("DEBUG", String.valueOf(e));
                                                           }
                                                       }
                                                   }
                                               }
                                           }
                });
    }

    // Create the BottomDialogView to get the user's Search Term to be Autogenerated into a Flashlet
    private void handleBottomDialogView(String classId, String userId) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ClassDetail.this);
        View dialogView = LayoutInflater.from(ClassDetail.this).inflate(R.layout.autogenerate_flashlet_bottom_sheet, null);
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
                                Intent sendToCreateFlashlet = new Intent(ClassDetail.this, CreateClassFlashlet.class);
                                sendToCreateFlashlet.putExtra("autofilledFlashletJSON", gson.toJson(handlerResponse));
                                sendToCreateFlashlet.putExtra("classId", classId);
                                sendToCreateFlashlet.putExtra("userId",userId);
                                sendToCreateFlashlet.putExtra("classJson", gson.toJson(userClass));
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
                                Toast.makeText(ClassDetail.this, err.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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

    // To re-initialize the DB update loop when returning to the screen
    @Override
    protected void onRestart() {
        super.onRestart();
        // Recreate the activity so that the DB update loop will be called again and be able to be terminated
        recreate();
    }
}