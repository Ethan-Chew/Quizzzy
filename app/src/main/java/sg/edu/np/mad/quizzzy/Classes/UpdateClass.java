package sg.edu.np.mad.quizzzy.Classes;

import android.content.Intent;
import android.os.Bundle;
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

import sg.edu.np.mad.quizzzy.Flashlets.CreateFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
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
                                View listItem = LayoutInflater.from(UpdateClass.this).inflate(R.layout.add_class_members, null, false);
                                EditText listItemEdit = listItem.findViewById(R.id.acmusername);
                                listItemEdit.setText(user.getUsername());
                                listItemEdit.setInputType(0); // Disable Text Editing
                                newUNList.add(listItemEdit);

                                ImageView deleteMember = listItem.findViewById(R.id.acmDelete);
                                deleteMember.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        memberList.removeView(listItem);
                                        newUNList.remove(listItemEdit);
                                    }
                                });

                                memberList.addView(listItem);
                            }
                        }
                    }
                });

        // Add a new TextField onAdd Clicked
        Button addMemberBtn = findViewById(R.id.uCAddMember);
        addMemberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View newMemberView = LayoutInflater.from(UpdateClass.this).inflate(R.layout.add_class_members, null, false);

                EditText memberUsernameInput = newMemberView.findViewById(R.id.acmusername);
                newUNList.add(memberUsernameInput);

                ImageView deleteMember = newMemberView.findViewById(R.id.acmDelete);
                deleteMember.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        memberList.removeView(newMemberView);
                        newUNList.remove(memberUsernameInput);
                    }
                });

                memberList.addView(newMemberView);

                View spacerView = new View(UpdateClass.this);
                LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        20
                );
                memberList.addView(spacerView, spacerParams);
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

                // Check if Usernames Exist
                ArrayList<String> usernameIds = new ArrayList<>();
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
                                                userClass.setMemberId(usernameIds);
                                                // Update Class
                                                db.collection("class").document(userClass.getId()).set(userClass)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                Toast.makeText(UpdateClass.this, "Successfully Updated Class!", Toast.LENGTH_LONG).show();

                                                                // Save statistics to SQLite DB before changing Activity.
                                                                // timeType of 2 because this is a Class Activity
                                                                localDB.updateStatistics(usage, 2, user.getId());
                                                                // Kills updateStatisticsLoop as we are switching to another activity.
                                                                usage.setActivityChanged(true);

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
}