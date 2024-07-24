package sg.edu.np.mad.quizzzy.Classes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import sg.edu.np.mad.quizzzy.Flashlets.CreateFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.Flashlets.CreateFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.HomeActivity;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.UsageStatistic;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserClass;
import sg.edu.np.mad.quizzzy.R;
import sg.edu.np.mad.quizzzy.StatisticsActivity;

public class UpdateClass extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Gson gson = new Gson();
    UserClass userClass;
    ArrayList<EditText> newUNList = new ArrayList<>();
    ArrayList<User> users = new ArrayList<>();
    Set<String> existingUsernames = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_class);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Add usage statistics to local SQLite DB
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(UpdateClass.this);
        User user = localDB.getUser().getUser();

        // Create new UsageStatistic class and start the update loop
        UsageStatistic usage = new UsageStatistic();
        localDB.updateStatisticsLoop(usage, 2, user.getId());

        // Handle Back Navigation Toolbar
        Toolbar toolbar = findViewById(R.id.uCViewToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save statistics to SQLite DB before changing Activity.
                // timeType of 2 because this is a Class Activity
                localDB.updateStatistics(usage, 2, user.getId());
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                UpdateClass.this.getOnBackPressedDispatcher().onBackPressed();
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

        // Handle Bottom Navigation Bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
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
                    startActivity(new Intent(getApplicationContext(), StatisticsActivity.class));
                    return true;
                }
                return false;
            }
        });

        // Receive Class from Intent
        Intent receiveIntent = getIntent();
        userClass = gson.fromJson(receiveIntent.getStringExtra("classJson"), UserClass.class);

        // Populate View Components with Data
        EditText titleEditField = findViewById(R.id.uCNewTitle);
        titleEditField.setText(userClass.getClassTitle());

        LinearLayout memberList = findViewById(R.id.uCUpdateMembers);
        db.collection("users").whereIn("id", userClass.getMemberId()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String userJson = gson.toJson(document.getData());
                                User user = gson.fromJson(userJson, User.class);
                                users.add(user);
                            }

                            for (User user : users) {
                                addUsernameItem(user.getUsername(), memberList, userClass.getCreatorId().contains(user.getId()));
                                existingUsernames.add(user.getUsername());
                            }
                        }
                    }
                });

        // Add a new TextField onAdd Clicked
        Button addMemberBtn = findViewById(R.id.uCAddMember);
        addMemberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUsernameItem("", memberList, false);
            }
        });

        // Wait for onClick
        Button updateClassBtn = findViewById(R.id.uCUpdateClass);
        updateClassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Usernames from EditText
                ArrayList<String> usernames = new ArrayList<>();
                for (EditText textField : newUNList) {
                    usernames.add(textField.getText().toString());
                }

                // Check if there are duplicated usernames
                Set<String> nonDuplicatedUsernames = new HashSet<>(usernames);
                if (usernames.size() != nonDuplicatedUsernames.size()) {
                    Toast.makeText(UpdateClass.this, "There is a duplicated member!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if Usernames Exist
                Set<String> usernameIds = new HashSet<>();
                db.collection("users").whereIn("username",usernames).get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                String userJson = gson.toJson(document.getData());
                                                usernameIds.add(gson.fromJson(userJson, User.class).getId());
                                            }

                                            if (usernameIds.size() != usernames.size()) {
                                                Toast.makeText(UpdateClass.this, "One or More Usernames do not exist!", Toast.LENGTH_LONG).show();
                                                return;
                                            } else {
                                                userClass.setMemberId(new ArrayList<>(usernameIds)); // Updated
                                                // Update Class
                                                db.collection("class").document(userClass.getId()).set(userClass)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                Toast.makeText(UpdateClass.this, "Successfully Updated Class!", Toast.LENGTH_LONG).show();
                                                                // Once Delete is Successful, send user back to ClassList
                                                                startActivity(new Intent(UpdateClass.this, ClassList.class));
                                                            }
                                                        });
                                            }
                                        }
                                    }
                                });
            }
        });
    }

    private void addUsernameItem(String username, LinearLayout memberList, boolean isCreator) {
        // Prevent adding duplicate usernames
        if (!username.isEmpty() && existingUsernames.contains(username)) {
            Toast.makeText(this, "Username already exists!", Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View memberItem = inflater.inflate(R.layout.add_class_members, null);

        EditText usernameField = memberItem.findViewById(R.id.acmusername);
        usernameField.setText(username);

        // Add Remove Functionality
        ImageView removeIcon = memberItem.findViewById(R.id.acmDelete);
        removeIcon.setVisibility(isCreator ? View.GONE : View.VISIBLE); // Updated
        removeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                memberList.removeView(memberItem);
                newUNList.remove(usernameField);
                existingUsernames.remove(usernameField.getText().toString()); // Updated
            }
        });

        memberList.addView(memberItem);
        newUNList.add(usernameField);
        existingUsernames.add(username);
    }

    // To re-initialize the DB update loop when returning to the screen
    @Override
    protected void onRestart() {
        super.onRestart();
        // Recreate the activity so that the DB update loop will be called again and be able to be terminated
        recreate();
    }
}