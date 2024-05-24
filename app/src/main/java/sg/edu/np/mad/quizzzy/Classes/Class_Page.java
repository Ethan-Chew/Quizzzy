package sg.edu.np.mad.quizzzy.Classes;

import android.app.MediaRouteButton;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import sg.edu.np.mad.quizzzy.Models.UserClass;
import sg.edu.np.mad.quizzzy.R;

public class Class_Page extends AppCompatActivity implements ClassRecyclerInterface {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<UserClass> classes = new ArrayList<UserClass>();
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.class_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.cpRecyclerView);

        // grab user id from database
        ArrayList<String> classIds = new ArrayList<>();

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

                    ClassAdapter classAdapter = new ClassAdapter(Class_Page.this, Class_Page.this, classes);
                    LinearLayoutManager classLayoutManager = new LinearLayoutManager(Class_Page.this);
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
        Intent sendToClassDetails = new Intent(Class_Page.this, Class_Details.class);
        sendToClassDetails.putExtra("classJson", classJson);
        startActivity(sendToClassDetails);
    }
}