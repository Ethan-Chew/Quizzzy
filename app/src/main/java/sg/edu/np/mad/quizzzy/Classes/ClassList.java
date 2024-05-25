package sg.edu.np.mad.quizzzy.Classes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserClass;
import sg.edu.np.mad.quizzzy.R;

public class ClassList extends AppCompatActivity implements ClassRecyclerInterface {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<UserClass> classes = new ArrayList<UserClass>();
    Gson gson = new Gson();
    User user;

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

        RecyclerView recyclerView = findViewById(R.id.cLRecyclerView);

        // grab user id from database
        ArrayList<String> classIds = new ArrayList<>();
        classIds.add("a61ad1fb-c8b5-46f8-879e-7055e1eda495");

        // Set Screen Data
        TextView classesCount = findViewById(R.id.cLnumofclass);
        String classCountText = "You have " + classIds.size() + " Class" + (classIds.size() == 1 ? "" : "es");
        classesCount.setText(classCountText);

        // Handle Create Button Press
        TextView createText = findViewById(R.id.cLAddClass);
        createText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createClassIntent = new Intent(getApplicationContext(), AddClass.class);
                createClassIntent.putExtra("userId", ""); // TODO: Implement this
                startActivity(createClassIntent);
            }
        });

        // If no IDs in List, show create alert
        View noClassNotif = findViewById(R.id.cLnoclass);
        if (classIds.size() == 0) {
            noClassNotif.setVisibility(View.VISIBLE);
            return;
        }

        // Retrieve from Firebase
        CollectionReference classColRef = db.collection("class");
        classColRef.whereIn("id", classIds).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String classJson = gson.toJson(document.getData());
                        classes.add(gson.fromJson(classJson, UserClass.class));
                    }

                    ClassAdapter classAdapter = new ClassAdapter(ClassList.this, ClassList.this, classes);
                    LinearLayoutManager classLayoutManager = new LinearLayoutManager(ClassList.this);
                    recyclerView.setLayoutManager(classLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(classAdapter);
                } else {
                    Log.d("Firebase", "Class get failed with ", task.getException());
                }
            }
        });
    }

    @Override public void onItemClick(int position) {
        String classJson = gson.toJson(classes.get(position));
        Intent sendToClassDetails = new Intent(ClassList.this, ClassDetail.class);
        sendToClassDetails.putExtra("classJson", classJson);
        startActivity(sendToClassDetails);
    }
}