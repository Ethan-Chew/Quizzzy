package sg.edu.np.mad.quizzzy.Classes;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.UUID;

import sg.edu.np.mad.quizzzy.Flashlets.CreateClassFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.CreateFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.HomeActivity;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.UsageStatistic;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserClass;
import sg.edu.np.mad.quizzzy.R;
import sg.edu.np.mad.quizzzy.Search.SearchActivity;
import sg.edu.np.mad.quizzzy.StatisticsActivity;

public class AddClass extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Gson gson = new Gson();
    int id = 0;

    private Button addMemberBtn;
    private View newMemberView;
    ArrayList<EditText> usernameInputs = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_class);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Add usage statistics to local SQLite DB
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(AddClass.this);
        User user = localDB.getUser().getUser();

        // Create new UsageStatistic class and start the update loop
        UsageStatistic usage = new UsageStatistic();
        localDB.updateStatisticsLoop(usage, 2, user.getId());

        // Handle Bottom Navigation Bar
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
        Toolbar toolbar = findViewById(R.id.acViewToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save statistics to SQLite DB before changing Activity.
                // timeType of 2 because this is a Class Activity
                localDB.updateStatistics(usage, 2, user.getId());
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                AddClass.this.getOnBackPressedDispatcher().onBackPressed();
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

        addMemberBtn = findViewById(R.id.acadd_membersbtn);
        Button createClassbtn = findViewById(R.id.accreate_class);
        LinearLayout addmem = findViewById(R.id.acaddmembers);
        Intent receivingIntent = getIntent();
        String userId = receivingIntent.getStringExtra("userId");
        addMemberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newMemberView = LayoutInflater.from(AddClass.this).inflate(R.layout.add_class_members, null, false);

                EditText memberUsernameInput = newMemberView.findViewById(R.id.acmusername);
                usernameInputs.add(memberUsernameInput);

                ImageView deleteMember = newMemberView.findViewById(R.id.acmDelete);
                deleteMember.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addmem.removeView(newMemberView);
                        usernameInputs.remove(memberUsernameInput);
                    }
                });

                addmem.addView(newMemberView);

                View spacerView = new View(AddClass.this);
                LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        20
                );
                addmem.addView(spacerView, spacerParams);
            }
        });

        // when button click
        EditText classTitle = findViewById(R.id.acNewTitle);
        createClassbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = classTitle.getText().toString();
                if (title.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please give your class a name!", Toast.LENGTH_LONG).show();
                    return;
                }
                ArrayList<String> newMemberUsernames = new ArrayList<>();
                for (EditText editText : usernameInputs) {
                    newMemberUsernames.add(editText.getText().toString());
                }

                // Check if All Members Exist in Firebase, and get their ID
                CollectionReference usersColRef = db.collection("users");
                usersColRef.whereIn("username", newMemberUsernames).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<String> newMemberIds = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String userJson = gson.toJson(document.getData());
                                User user = gson.fromJson(userJson, User.class);

                                newMemberIds.add(user.getId());
                            }

                            if (newMemberIds.size() != usernameInputs.size()) {
                                Toast.makeText(getApplicationContext(), "One or More of the Usernames entered do not exist!", Toast.LENGTH_LONG).show();
                            } else {
                                String classId = UUID.randomUUID().toString();
                                ArrayList<String> creatorId = new ArrayList<>();
                                creatorId.add(userId);
                                newMemberIds.add(userId);

                                UserClass userClass = new UserClass(classId, title, creatorId, newMemberIds, System.currentTimeMillis() / 1000L);
                                db.collection("class").document(classId).set(userClass).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getApplicationContext(), "Successfully Created Class!", Toast.LENGTH_LONG).show();

                                        // Save statistics to SQLite DB before changing Activity.
                                        // timeType of 2 because this is a Class Activity
                                        localDB.updateStatistics(usage, 2, user.getId());
                                        // Kills updateStatisticsLoop as we are switching to another activity.
                                        usage.setActivityChanged(true);

                                        startActivity(new Intent(getApplicationContext(), ClassList.class));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "Failed to create Class. Try Again!", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to check all member's existance!", Toast.LENGTH_LONG).show();
                        }
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