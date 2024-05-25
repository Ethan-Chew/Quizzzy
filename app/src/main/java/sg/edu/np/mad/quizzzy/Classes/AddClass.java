package sg.edu.np.mad.quizzzy.Classes;

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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserClass;
import sg.edu.np.mad.quizzzy.R;

public class AddClass extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Gson gson = new Gson();

    UserClass newClass;
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

        addMemberBtn = findViewById(R.id.acadd_membersbtn);
        Button createClassbtn = findViewById(R.id.accreate_class);
        LinearLayout addmem = findViewById(R.id.acaddmembers);
        Intent receivingIntent = getIntent();
        String userId = receivingIntent.getStringExtra("userId");
        addMemberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newMemberView = LayoutInflater.from(AddClass.this).inflate(R.layout.add_class_members, null, false);
                final String[] memberUN = {""};

                EditText memberUsernameInput = newMemberView.findViewById(R.id.acmusername);
                usernameInputs.add(memberUsernameInput);

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
                    Toast.makeText(getApplicationContext(), "Pleass give your class a name!", Toast.LENGTH_LONG).show();
                    return;
                }
                ArrayList<String> newMemberUsernames = new ArrayList<>();
                for (EditText editText : usernameInputs) {
                    newMemberUsernames.add(editText.getText().toString());
                }

                EditText titleEntry = findViewById(R.id.acNewTitle);
                String className = titleEntry.getText().toString();
                ArrayList<String> creatorIds = new ArrayList<>(Arrays.asList(userId));

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
                                newClass = new UserClass(classId, title, creatorId, newMemberIds, System.currentTimeMillis() / 1000L);

                                db.collection("class").document(classId).set(newClass).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getApplicationContext(), "Successfully Created Class!", Toast.LENGTH_LONG).show();

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

}