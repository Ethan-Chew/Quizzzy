package sg.edu.np.mad.quizzzy;

import static android.content.ContentValues.TAG;
import static sg.edu.np.mad.quizzzy.Classes.TOTPUtil.verifyTOTP;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import java.util.List;

import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;

public class LoginActivity extends AppCompatActivity {

    Gson gson = new Gson();
    FirebaseAuth mAuth;
    FirebaseFirestore firebase;
    String flashletId;

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
        mAuth = FirebaseAuth.getInstance();
        firebase = FirebaseFirestore.getInstance();
        flashletId = getIntent().getStringExtra("FLASHLET_ID");

        // Get Respective Text Items on screen
        View loginBtn = findViewById(R.id.loginBtnLoginAct);
        EditText usernameView = findViewById(R.id.usernameField);
        EditText passwordView = findViewById(R.id.passwordField);
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();

        // Handle Back Navigation
        Toolbar toolbar = findViewById(R.id.loginToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.this.getOnBackPressedDispatcher().onBackPressed();
            }
        });

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
                                                    DocumentSnapshot document = task.getResult();

                                                        if (document.getData().get("2faSecret") != null) {
                                                            String secret = document.getData().get("2faSecret").toString();
                                                            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                                                            View popupView = inflater.inflate(R.layout.login_2fa_popup, null);
                                                            PopupWindow popupWindow = getPopupWindow(popupView);

                                                            // Show the popup window at the center of the layout
                                                            popupWindow.showAtLocation(v, android.view.Gravity.CENTER, 0, 0);

                                                            // Dim the background
                                                            View container = popupWindow.getContentView().getRootView();
                                                            if (container != null) {
                                                                WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                                                                WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
                                                                p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                                                                p.dimAmount = 0.5f;
                                                                if (wm != null) {
                                                                    wm.updateViewLayout(container, p);
                                                                }
                                                            }

                                                            EditText pin1 = popupView.findViewById(R.id.loginPin1);
                                                            EditText pin2 = popupView.findViewById(R.id.loginPin2);
                                                            EditText pin3 = popupView.findViewById(R.id.loginPin3);
                                                            EditText pin4 = popupView.findViewById(R.id.loginPin4);
                                                            EditText pin5 = popupView.findViewById(R.id.loginPin5);
                                                            EditText pin6 = popupView.findViewById(R.id.loginPin6);

                                                            pin1.addTextChangedListener(new LoginActivity.TOTPWatcher(pin1, pin2, popupView, secret));
                                                            pin2.addTextChangedListener(new LoginActivity.TOTPWatcher(pin2, pin3, popupView, secret));
                                                            pin3.addTextChangedListener(new LoginActivity.TOTPWatcher(pin3, pin4, popupView, secret));
                                                            pin4.addTextChangedListener(new LoginActivity.TOTPWatcher(pin4, pin5, popupView, secret));
                                                            pin5.addTextChangedListener(new LoginActivity.TOTPWatcher(pin5, pin6, popupView, secret));
                                                            pin6.addTextChangedListener(new LoginActivity.TOTPWatcher(pin6, null, popupView, secret));

                                                        } else {
                                                            // Save the User to our SQLite (Local) Database
                                                            SQLiteManager localDB = SQLiteManager.instanceOfDatabase(LoginActivity.this);
                                                            String userJson = gson.toJson(document.getData());
                                                            User user = gson.fromJson(userJson, User.class);
                                                            localDB.addUser(new UserWithRecents(user));
                                                            // Send User to Home Screen
                                                            Intent homeScreenIntent = new Intent(LoginActivity.this, HomeActivity.class);
                                                            startActivity(homeScreenIntent);
                                                        }

                                                } else {
                                                    Log.d(TAG, "Get user failed with ", task.getException());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.d(TAG, "Login failed.", task.getException());
                                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<String> createdFlashlets = (List<String>) document.get("createdFlashlets");
                    if (createdFlashlets != null && createdFlashlets.contains(flashletId)) {
                        Log.d("LoginActivity", "Flashlet already exists in user createdFlashlets.");
                        navigateToHome(flashletId);
                        Toast.makeText(LoginActivity.this, "You already have this flashlet.", Toast.LENGTH_SHORT).show();
                    } else {
                        updateUserAndFlashlet(userRef, flashletId);
                        Toast.makeText(LoginActivity.this, "Flashlet added successfully.", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Log.e("LoginActivity", "Error checking user document", task.getException());
            }
        });
    }

    private void updateUserAndFlashlet(DocumentReference userRef, String flashletId) {
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        DocumentReference flashletRef = firebase.collection("flashlets").document(flashletId);

        userRef.update("createdFlashlets", FieldValue.arrayUnion(flashletId))
                .addOnSuccessListener(aVoid -> Log.d("LoginActivity", "User flashlets updated successfully"))
                .addOnFailureListener(e -> Log.e("LoginActivity", "Failed to update user createdFlashlets", e));

        flashletRef.update("creatorID", FieldValue.arrayUnion(userRef.getId()))
                .addOnSuccessListener(aVoid -> navigateToHome(flashletId))
                .addOnFailureListener(e -> Log.e("LoginActivity", "Failed to update flashlet creatorID", e));
    }

    private void navigateToHome(String flashletId) {
        Intent resultIntent = new Intent(LoginActivity.this, HomeActivity.class);
        resultIntent.putExtra("FLASHLET_ID", flashletId);
        startActivity(resultIntent);
        finish();
    }

    @NonNull
    private static PopupWindow getPopupWindow(View popupView) {
        PopupWindow popupWindow = new PopupWindow(popupView, ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setElevation(5.0f);

        // Set a dim background behind the popup
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        return popupWindow;
    }

    private class TOTPWatcher implements TextWatcher {

        private View currentView;
        private View nextView;
        private View popupView;
        private String secret;

        public TOTPWatcher(View currentView, View nextView, View popupView, String secret) {
            this.currentView = currentView;
            this.nextView = nextView;
            this.popupView = popupView;
            this.secret = secret;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus(); // Move focus to next EditText
            } else if (s.length() == 0 && currentView != null) {
                currentView.requestFocus(); // Stay on the current EditText
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (areAllPinFieldsFilled()) {
                EditText pin1 = popupView.findViewById(R.id.loginPin1);
                EditText pin2 = popupView.findViewById(R.id.loginPin2);
                EditText pin3 = popupView.findViewById(R.id.loginPin3);
                EditText pin4 = popupView.findViewById(R.id.loginPin4);
                EditText pin5 = popupView.findViewById(R.id.loginPin5);
                EditText pin6 = popupView.findViewById(R.id.loginPin6);
                String totp = pin1.getText().toString() +
                        pin2.getText().toString() +
                        pin3.getText().toString() +
                        pin4.getText().toString() +
                        pin5.getText().toString() +
                        pin6.getText().toString();
                boolean isValid = verifyTOTP(secret, totp);
                if (isValid) {
                    FirebaseAuth mAuth;
                    mAuth = FirebaseAuth.getInstance();
                    FirebaseFirestore firebase = FirebaseFirestore.getInstance();
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    DocumentReference docRef = firebase.collection("users").document(currentUser.getUid());
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                // Save the User to our SQLite (Local) Database
                                SQLiteManager localDB = SQLiteManager.instanceOfDatabase(LoginActivity.this);
                                String userJson = gson.toJson(document.getData());
                                User user = gson.fromJson(userJson, User.class);
                                localDB.addUser(new UserWithRecents(user));
                                if (flashletId != null) {
                                    handleFlashletAddition(flashletId, currentUser.getUid());
                                    finish();
                                }
                                else {
                                    // Send User to Home Screen
                                    Intent homeScreenIntent = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(homeScreenIntent);
                                }
                            }
                        }
                    });
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid TOTP", Toast.LENGTH_SHORT).show();
                }
            }
        }

        private boolean areAllPinFieldsFilled() {
            EditText loginPin1 = popupView.findViewById(R.id.loginPin1);
            EditText loginPin2 = popupView.findViewById(R.id.loginPin2);
            EditText loginPin3 = popupView.findViewById(R.id.loginPin3);
            EditText loginPin4 = popupView.findViewById(R.id.loginPin4);
            EditText loginPin5 = popupView.findViewById(R.id.loginPin5);
            EditText loginPin6 = popupView.findViewById(R.id.loginPin6);

            return  !loginPin1.getText().toString().isEmpty() &&
                    !loginPin2.getText().toString().isEmpty() &&
                    !loginPin3.getText().toString().isEmpty() &&
                    !loginPin4.getText().toString().isEmpty() &&
                    !loginPin5.getText().toString().isEmpty() &&
                    !loginPin6.getText().toString().isEmpty();
        }
    }
}