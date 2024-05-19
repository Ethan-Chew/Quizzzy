package sg.edu.np.mad.quizzzy.Flashlets;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import sg.edu.np.mad.quizzzy.MainActivity;
import sg.edu.np.mad.quizzzy.Models.Flashcard;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashTitle), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get User from SQLite DB
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(FlashletList.this);
        user = localDB.getUser();
        /// If User is somehow null, return user back to login page
        if (user == null) {
            Intent returnToLoginIntent = new Intent(FlashletList.this, MainActivity.class);
            startActivity(returnToLoginIntent);
        }

        // Get User's Flashlets from Firebase
        CollectionReference flashletColRef = db.collection("flashlets");
        flashletColRef.whereIn("id", user.getCreatedFlashlets()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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