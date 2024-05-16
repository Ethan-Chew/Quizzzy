package sg.edu.np.mad.quizzzy.Flashlets;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Flashlets.Recycler.FlashletListAdapter;
import sg.edu.np.mad.quizzzy.Flashlets.Recycler.FlashletListRecyclerInterface;
import sg.edu.np.mad.quizzzy.Models.Flashcard;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.R;

public class FlashletList extends AppCompatActivity implements FlashletListRecyclerInterface {
    // Data
    ArrayList<Flashlet> userFlashlets = new ArrayList<Flashlet>();
    User user;
    Gson gson = new Gson();

    // Initialisation of Firebase Cloud Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flashlet_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Temp Data
        ArrayList<Flashcard> tempFlashcards = new ArrayList<Flashcard>();
        tempFlashcards.add(new Flashcard("Keyword 1", "Defintion 1"));
        tempFlashcards.add(new Flashcard("Keyword 2", "Defintion 2"));
        tempFlashcards.add(new Flashcard("Keyword 3", "Defintion 3"));
        userFlashlets.add(new Flashlet("0", "Test Flashlet", null, new ArrayList<String>(), null, tempFlashcards, 1714883105));

        // Get User ID using Intent
        Intent receivingIntent = getIntent();
        user = gson.fromJson(receivingIntent.getStringExtra("userJSON"), User.class);

        // Get Data from Firebase
        /// Get User Info
        DocumentReference userDocRef = db.collection("users").document("IdhWjBsjccPm6mecWk1q");
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    String userJson = gson.toJson(document.getData());
                    user = gson.fromJson(userJson, User.class);

                    /// Get Flashlets related to the User
                    CollectionReference flashletColRef = db.collection("flashlets");
                    flashletColRef.whereArrayContains("creatorId", user.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String flashletJson = gson.toJson(document.getData());
                                    userFlashlets.add(gson.fromJson(flashletJson, Flashlet.class));
                                }

                                // Update User Interface with Updated Data
                                updateFlashletList();
                                findViewById(R.id.fLProgressBar).setVisibility(View.GONE);
                            } else {
                                Log.d("Firebase", "Flashlet get failed with ", task.getException());
                            }
                        }
                    });
                } else {
                    Log.d("Firebase", "User get failed with ", task.getException());
                }
            }
        });
    }

    void updateFlashletList() {
        // Update Flashlet Count
        TextView flashletCount = findViewById(R.id.fLCounterLabel);
        String flashletCountStr = "You have " + userFlashlets.size() + " Total Flashlet" + (userFlashlets.size() == 1 ? "" : "s");
        flashletCount.setText(flashletCountStr);

        // If user has no Flashlets, display a message to ask them to create one
        RecyclerView recyclerView = findViewById(R.id.fLRecyclerView);
        LinearLayout noFlashletNotif = findViewById(R.id.fLNoFlashlets);
        if (userFlashlets.isEmpty()) {
            Button nFNCreateFlashlet = findViewById(R.id.fLNoFlashletsCreate);
            nFNCreateFlashlet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent createFlashletIntent = new Intent(FlashletList.this, CreateFlashlet.class);
                    startActivity(createFlashletIntent);
                }
            });
            recyclerView.setVisibility(View.GONE);
            noFlashletNotif.setVisibility(View.VISIBLE);
        }

        /// Display Flashlet List on Screen
        if (!userFlashlets.isEmpty()) {
            noFlashletNotif.setVisibility(View.GONE);
            FlashletListAdapter userAdapter = new FlashletListAdapter(userFlashlets, this, this, user);
            LinearLayoutManager userLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(userLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(userAdapter);
        }

    }

    @Override
    public void onItemClick(int position) {
        String flashletJson = gson.toJson(userFlashlets.get(position));
        Intent sendToFlashletDetail = new Intent(FlashletList.this, FlashletDetail.class);
        sendToFlashletDetail.putExtra("flashletJSON", flashletJson);
        startActivity(sendToFlashletDetail);
    }
}