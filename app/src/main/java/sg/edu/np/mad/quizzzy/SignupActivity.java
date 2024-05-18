package sg.edu.np.mad.quizzzy;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;

import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.User;

public class SignupActivity extends AppCompatActivity {

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

        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();

        View signupBtn = findViewById(R.id.signupBtnSignupAct);
        EditText emailView = findViewById(R.id.emailFieldSignupAct);
        EditText passwordView = findViewById(R.id.passwordFieldSignupAct);
        EditText usernameView = findViewById(R.id.usernameFieldSignupAct);
        EditText confirmPassword = findViewById(R.id.confirmPassword);
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(SignupActivity.this);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = emailView.getText().toString();
                String password = passwordView.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Username or Password is blank.", Toast.LENGTH_SHORT).show();
                } else if (!confirmPassword.getText().toString().equals(password)) {
                    Toast.makeText(SignupActivity.this, "Passwords does not match.", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(username, password)
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        // Create user in Firebase
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        User userInfo = new User(user.getUid(), usernameView.getText().toString(), user.getEmail());
                                        firebase.collection("users").document(user.getUid()).set(userInfo);
                                        // Save user into SQLite Local DB
                                        localDB.addUser(userInfo);
                                        // Send user to Home Screen
                                        Intent homeScreenIntent = new Intent(SignupActivity.this, HomeActivity.class);
                                        startActivity(homeScreenIntent);
                                    } else if (!task.getException().toString().isEmpty()) {
                                        Toast.makeText(SignupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(SignupActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

    }
}