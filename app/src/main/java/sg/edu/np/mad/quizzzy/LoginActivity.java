package sg.edu.np.mad.quizzzy;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;

public class LoginActivity extends AppCompatActivity {

    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashTitle), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configure Firebase Authenticator
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        // Get Respective Text Items on screen
        View loginBtn = findViewById(R.id.loginBtnLoginAct);
        EditText usernameView = findViewById(R.id.usernameField);
        EditText passwordView = findViewById(R.id.passwordField);
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();

        // Detect when the user has pressed the Login Button
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = usernameView.getText().toString();
                String password = passwordView.getText().toString();

                // Ensures that the user's email and password field is not empty
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Username or Password is blank.", Toast.LENGTH_SHORT).show();
                } else {
                    // Send an Authentication request to Firebase Authentication
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser currentUser = mAuth.getCurrentUser();
                                        DocumentReference docRef = firebase.collection("users").document(currentUser.getUid());
                                        // Get the User's Data from Firebase under the 'Users' collection
                                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    // Save the User to our SQLite (Local) Database
                                                    DocumentSnapshot document = task.getResult();
                                                    SQLiteManager localDB = SQLiteManager.instanceOfDatabase(LoginActivity.this);
                                                    String userJson = gson.toJson(document.getData());
                                                    User user = gson.fromJson(userJson, User.class);
                                                    localDB.addUser(new UserWithRecents(user));
                                                    // Send User to Home Screen
                                                    Intent homeScreenIntent = new Intent(LoginActivity.this, HomeActivity.class);
                                                    startActivity(homeScreenIntent);
                                                } else {
                                                    Log.d(TAG, "Get user failed with ", task.getException());
                                                }
                                            }
                                        });
                                    } else if (!task.getException().toString().isEmpty()) {
                                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

    }
}