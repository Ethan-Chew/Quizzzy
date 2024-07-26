package sg.edu.np.mad.quizzzy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;

public class SignupActivity extends AppCompatActivity {

    Gson gson = new Gson();
    FirebaseAuth mAuth;
    FirebaseFirestore firebase;
    SQLiteManager localDB;
    String flashletId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashTitle), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        firebase = FirebaseFirestore.getInstance();
        localDB = SQLiteManager.instanceOfDatabase(SignupActivity.this);
        flashletId = getIntent().getStringExtra("FLASHLET_ID");

        View signupBtn = findViewById(R.id.signupBtnSignupAct);
        EditText emailView = findViewById(R.id.emailFieldSignupAct);
        EditText passwordView = findViewById(R.id.passwordFieldSignupAct);
        EditText usernameView = findViewById(R.id.usernameFieldSignupAct);
        EditText confirmPassword = findViewById(R.id.confirmPassword);
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(SignupActivity.this);

        //Handle Back Navigation
        Toolbar toolbar = findViewById(R.id.signUpToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignupActivity.this.getOnBackPressedDispatcher().onBackPressed();
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailView.getText().toString();
                String password = passwordView.getText().toString();
                String username = usernameView.getText().toString();

                if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Email, Username, or Password is blank.", Toast.LENGTH_SHORT).show();
                } else if (!confirmPassword.getText().toString().equals(password)) {
                    Toast.makeText(SignupActivity.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        // Create user in Firebase
                                        FirebaseUser currentUser = mAuth.getCurrentUser();
                                        if (currentUser != null) {
                                            User userInfo = new User(currentUser.getUid(), username, username.toLowerCase() ,email, new ArrayList<>());
                                            firebase.collection("users").document(currentUser.getUid()).set(userInfo)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                DocumentReference docRef = firebase.collection("users").document(currentUser.getUid());
                                                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            DocumentSnapshot document = task.getResult();
                                                                            if (document != null) {
                                                                                String userJson = gson.toJson(document.getData());
                                                                                User user = gson.fromJson(userJson, User.class);
                                                                                localDB.addUser(new UserWithRecents(user));

                                                                                if (flashletId != null) {
                                                                                    handleFlashletAddition(flashletId, currentUser.getUid());
                                                                                    finish();
                                                                                } else {
                                                                                    Intent homeScreenIntent = new Intent(SignupActivity.this, HomeActivity.class);
                                                                                    startActivity(homeScreenIntent);
                                                                                    finish(); // End SignupActivity
                                                                                }
                                                                            } else {
                                                                                Toast.makeText(SignupActivity.this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        } else {
                                                                            Toast.makeText(SignupActivity.this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                            } else {
                                                                Toast.makeText(SignupActivity.this, "Failed to create user in Firestore.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(SignupActivity.this, "User is null after signup.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(SignupActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    private void handleFlashletAddition(String flashletId, String userId) {
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        DocumentReference userRef = firebase.collection("users").document(userId);
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(SignupActivity.this);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<String> createdFlashlets = (List<String>) document.get("createdFlashlets");
                    if (createdFlashlets != null && createdFlashlets.contains(flashletId)) {
                        Log.d("SignupActivity", "Flashlet already exists in user createdFlashlets.");
                        Toast.makeText(SignupActivity.this, "You already have this flashlet.", Toast.LENGTH_SHORT).show();
                        navigateToHome(flashletId);
                    } else {
                        updateUserAndFlashlet(userRef, flashletId);
                        ArrayList<String> createdFlashletIds = localDB.getUser().getUser().getCreatedFlashlets();
                        createdFlashletIds.add(flashletId);
                        localDB.updateCreatedFlashcards(userId, createdFlashletIds);
                        Toast.makeText(SignupActivity.this, "Flashlet added successfully.", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Log.e("SignupActivity", "Error checking user document", task.getException());
            }
        });
    }

    private void updateUserAndFlashlet(DocumentReference userRef, String flashletId) {
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        DocumentReference flashletRef = firebase.collection("flashlets").document(flashletId);

        userRef.update("createdFlashlets", FieldValue.arrayUnion(flashletId))
                .addOnSuccessListener(aVoid -> {
                    Log.d("SignupActivity", "User flashlets updated successfully");
                })
                .addOnFailureListener(e -> Log.e("SignupActivity", "Failed to update user createdFlashlets", e));

        flashletRef.update("creatorID", FieldValue.arrayUnion(userRef.getId()))
                .addOnSuccessListener(aVoid -> navigateToHome(flashletId))
                .addOnFailureListener(e -> Log.e("SignupActivity", "Failed to update flashlet creatorID", e));
    }


    private void navigateToHome(String flashletId) {
        Intent resultIntent = new Intent(SignupActivity.this, HomeActivity.class);
        resultIntent.putExtra("FLASHLET_ID", flashletId);
        startActivity(resultIntent);
        finish();
    }


}