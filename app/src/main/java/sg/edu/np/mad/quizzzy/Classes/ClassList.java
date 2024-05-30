package sg.edu.np.mad.quizzzy.Classes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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

import sg.edu.np.mad.quizzzy.Flashlets.CreateFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.MainActivity;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserClass;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;
import sg.edu.np.mad.quizzzy.R;

public class ClassList extends AppCompatActivity implements ClassRecyclerInterface {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<UserClass> classes = new ArrayList<UserClass>();
    Gson gson = new Gson();
    User user;
    UserWithRecents userWithRecents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_class_list);
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
                    // TODO: Integrate Darius's Part
                    return true;
                }
                return false;
            }
        });


        // Handle Back Navigation Toolbar
        Toolbar toolbar = findViewById(R.id.cLViewToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClassList.this.getOnBackPressedDispatcher().onBackPressed();
            }
        });

        RecyclerView recyclerView = findViewById(R.id.cLRecyclerView);

        // Get User from SQLite DB
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(ClassList.this);
        userWithRecents = localDB.getUser();
        /// If User is somehow null, return user back to login page
        if (userWithRecents == null) {
            Intent returnToLoginIntent = new Intent(ClassList.this, MainActivity.class);
            startActivity(returnToLoginIntent);
        }
        user = userWithRecents.getUser();

        // Get all the Classes the User is in from Firebase
        CollectionReference classColRef = db.collection("class");
        classColRef.whereArrayContains("memberId", user.getId()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String classJson = gson.toJson(document.getData());
                                classes.add(gson.fromJson(classJson, UserClass.class));
                            }
                            // Set Screen Data
                            TextView classesCount = findViewById(R.id.cLnumofclass);
                            String classCountText = "You have " + classes.size() + " Class" + (classes.size() == 1 ? "" : "es");
                            classesCount.setText(classCountText);

                            // If no IDs in List, show create no class popup
                            View noClassNotif = findViewById(R.id.cLnoclass);
                            if (classes.isEmpty()) {
                                noClassNotif.setVisibility(View.VISIBLE);
                                return;
                            }

                            // Send Classes to RecyclerView
                            ClassAdapter classAdapter = new ClassAdapter(ClassList.this, ClassList.this, classes, user);
                            LinearLayoutManager classLayoutManager = new LinearLayoutManager(ClassList.this);
                            recyclerView.setLayoutManager(classLayoutManager);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                            recyclerView.setAdapter(classAdapter);
                        }
                    }
                });


        // Handle Create Button Press
        TextView createText = findViewById(R.id.cLAddClass);
        createText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createClassIntent = new Intent(getApplicationContext(), AddClass.class);
                createClassIntent.putExtra("userId", user.getId());
                startActivity(createClassIntent);
            }
        });

        TextView createclass = findViewById(R.id.cLaddclass);

        createclass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createclassintent = new Intent(ClassList.this, AddClass.class);
                createclassintent.putExtra("userId", user.getId());
                startActivity(createclassintent);
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        String classJson = gson.toJson(classes.get(position));
        Intent sendToClassDetails = new Intent(ClassList.this, ClassDetail.class);
        sendToClassDetails.putExtra("classJson", classJson);
        sendToClassDetails.putExtra("userId", user.getId());
        startActivity(sendToClassDetails);
    }
}