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
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;
import sg.edu.np.mad.quizzzy.R;

public class FlashletList extends AppCompatActivity implements FlashletListRecyclerInterface {
    // Data
    ArrayList<Flashlet> userFlashlets = new ArrayList<Flashlet>();
    UserWithRecents userWithRecents;
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

        RecyclerView recyclerView = findViewById(R.id.fLRecyclerView);
        LinearLayout noFlashletNotif = findViewById(R.id.fLNoFlashlets);

        // Get User from SQLite DB
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(FlashletList.this);
        userWithRecents = localDB.getUser();
        /// If User is somehow null, return user back to login page
        if (userWithRecents == null) {
            Intent returnToLoginIntent = new Intent(FlashletList.this, MainActivity.class);
            startActivity(returnToLoginIntent);
        }

        // Update User Interface with Updated Data
        ArrayList<String> userFlashletIDs = userWithRecents.getUser().getCreatedFlashlets();

        TextView flashletCount = findViewById(R.id.fLCounterLabel);
        String flashletCountStr = "You have " + userFlashletIDs.size() + " Total Flashlet" + (userFlashletIDs.size() == 1 ? "" : "s");
        flashletCount.setText(flashletCountStr);

        // Check if the User has any Flashlets; If not, ask them to create one
        if (userFlashletIDs.isEmpty()) {
            Button nFNCreateFlashlet = findViewById(R.id.fLNoFlashletsCreate);
            nFNCreateFlashlet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent createFlashletIntent = new Intent(FlashletList.this, CreateFlashlet.class);
                    createFlashletIntent.putExtra("userId", userWithRecents.getUser().getId());
                    startActivity(createFlashletIntent);
                }
            });
            recyclerView.setVisibility(View.GONE);
            noFlashletNotif.setVisibility(View.VISIBLE);
            findViewById(R.id.fLProgressBar).setVisibility(View.GONE);
            return;
        }

        // Get User's Flashlets from Firebase
        CollectionReference flashletColRef = db.collection("flashlets");
        flashletColRef.whereIn("id", userFlashletIDs).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String flashletJson = gson.toJson(document.getData());
                        userFlashlets.add(gson.fromJson(flashletJson, Flashlet.class));
                    }

                    /// Display Flashlet List on Screen
                    noFlashletNotif.setVisibility(View.GONE);
                    FlashletListAdapter userAdapter = new FlashletListAdapter(userFlashlets, FlashletList.this, FlashletList.this, userWithRecents.getUser());
                    LinearLayoutManager userLayoutManager = new LinearLayoutManager(FlashletList.this);
                    recyclerView.setLayoutManager(userLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(userAdapter);
                    findViewById(R.id.fLProgressBar).setVisibility(View.GONE);
                } else {
                    Log.d("Firebase", "Flashlet get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        String flashletJson = gson.toJson(userFlashlets.get(position));
        Intent sendToFlashletDetail = new Intent(FlashletList.this, FlashletDetail.class);
        sendToFlashletDetail.putExtra("flashletJSON", flashletJson);
        startActivity(sendToFlashletDetail);
    }
}