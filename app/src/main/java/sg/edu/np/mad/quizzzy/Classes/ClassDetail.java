package sg.edu.np.mad.quizzzy.Classes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;

import sg.edu.np.mad.quizzzy.Flashlets.CreateClassFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.CreateFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletDetail;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.HomeActivity;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserClass;
import sg.edu.np.mad.quizzzy.R;
import sg.edu.np.mad.quizzzy.StatisticsActivity;

public class ClassDetail extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Gson gson = new Gson();
    
    UserClass userClass;
    TextView classtitle;
    TextView memberscount;
    Button createFlashlet;
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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnApplyWindowInsetsListener(null);
        bottomNavigationView.setPadding(0,0,0,0);

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
                    startActivity(new Intent(getApplicationContext(), StatisticsActivity.class));
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
                ClassDetail.this.getOnBackPressedDispatcher().onBackPressed();
            }
        });

        classtitle = findViewById(R.id.cdclasstitle);
        memberscount = findViewById(R.id.cdmembers);
        createFlashlet = findViewById(R.id.createFlashlet);
        memberscontainer = findViewById(R.id.cdmemberscontainer);
        createdFlashletsContainer = findViewById(R.id.hPCFContainer);
        editButton = findViewById(R.id.cDEditOption);

        // Receive data from Intent
        Intent receiveintent = getIntent();
        userClass = gson.fromJson(receiveintent.getStringExtra("classJson"), UserClass.class);
        ArrayList<String> members = userClass.getMemberId();
        String classId = userClass.getId();
        String userId = receiveintent.getStringExtra("userId");

        // Handle onClick of Edit Class Button
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent updateClassIntent = new Intent(ClassDetail.this, UpdateClass.class);
                updateClassIntent.putExtra("classJson", gson.toJson(userClass));
                startActivity(updateClassIntent);
            }
        });

        // Handle onClick of Create Flashlet Button
        createFlashlet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createFlashletIntent = new Intent(getApplicationContext(), CreateClassFlashlet.class);
                createFlashletIntent.putExtra("classId", classId);
                createFlashletIntent.putExtra("userId",userId);
                startActivity(createFlashletIntent);
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
                                                                                                   startActivity(showFlashletDetail);
                                                                                               }
                                                                                           });

                                                                                           // Set Text
                                                                                           fVTitle.setText(String.valueOf(flashlet.get("title")).replace("\"", ""));
                                                                                           String pillText = flashlet.get("flashcards").getAsJsonArray().size() + " Keyword" + (flashlet.get("flashcards").getAsJsonArray().size() == 0 ? "" : "s");
                                                                                           fVPill.setText(pillText);

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

}