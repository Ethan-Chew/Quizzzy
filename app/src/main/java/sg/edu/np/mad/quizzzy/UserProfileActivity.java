package sg.edu.np.mad.quizzzy;

import static sg.edu.np.mad.quizzzy.Classes.TOTPUtil.verifyTOTP;

import android.net.Uri;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import sg.edu.np.mad.quizzzy.Classes.QRCodeUtil;
import sg.edu.np.mad.quizzzy.Classes.TOTPUtil;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletDetail;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.RecyclerViewInterface;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;
import sg.edu.np.mad.quizzzy.Search.SearchActivity;

public class UserProfileActivity extends AppCompatActivity implements RecyclerViewInterface {
    public static class secret {
        public static String secret = TOTPUtil.generateSecretKey();
    }

    // Initialisation of Firebase Cloud Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    Gson gson = new Gson();
    RecyclerView flashletRecyclerView;

    // Data Variables
    User user;
    ArrayList<Flashlet> flashlets = new ArrayList<Flashlet>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Handle Back Navigation Toolbar
        Toolbar toolbar = findViewById(R.id.uPViewToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfileActivity.this.getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // Handle Bottom Navigation View
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnApplyWindowInsetsListener(null);
        bottomNavigationView.setPadding(0,0,0,0);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.home) {
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                }  else if (itemId == R.id.search) {
                    startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.flashlets) {
                    startActivity(new Intent(getApplicationContext(), FlashletList.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.stats) {
                    startActivity(new Intent(getApplicationContext(), StatisticsActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                }
                return false;
            }
        });

        // Get User from Intent
        Intent receiveIntent = getIntent();
        user = gson.fromJson(receiveIntent.getStringExtra("userJSON"), User.class);

        // Get Currently Logged In User
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(UserProfileActivity.this);
        UserWithRecents loggedInUser = localDB.getUser();

        // Set Username Text
        TextView usernameLbl = findViewById(R.id.uPUsername);
        usernameLbl.setText(user.getUsername());

        // Retrieve Flashlets from Database
        flashletRecyclerView = findViewById(R.id.uPRecyclerView);
        if (!user.getCreatedFlashlets().isEmpty()) {
            db.collection("flashlets").whereIn("id", user.getCreatedFlashlets()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String flashletJson = gson.toJson(document.getData());
                            Flashlet flashlet = gson.fromJson(flashletJson, Flashlet.class);
                            // If the Profile's User is the same as the current user, show all flashlets. Else, only show public flashlets
                            if (!Objects.equals(loggedInUser.getUser().getId(), user.getId())) {
                                if (flashlet.getIsPublic()) {
                                    flashlets.add(flashlet);
                                }
                            } else {
                                flashlets.add(flashlet);
                            }
                        }

                        // Update Flashlet Count
                        TextView flashletCountLbl = findViewById(R.id.sURFlashletCount);
                        String flashletCount = flashlets.size() + " Flashlets";
                        flashletCountLbl.setText(flashletCount);

                        /// Display Flashlet List on Screen
                        ProfileFlashletAdapter adapter = new ProfileFlashletAdapter(flashlets, UserProfileActivity.this);
                        LinearLayoutManager userLayoutManager = new LinearLayoutManager(UserProfileActivity.this);
                        flashletRecyclerView.setLayoutManager(userLayoutManager);
                        flashletRecyclerView.setItemAnimator(new DefaultItemAnimator());
                        flashletRecyclerView.setAdapter(adapter);
                    } else {
                        Log.d("Firebase", "Flashlet get failed with ", task.getException());
                    }
                }
            });
        }

        // Handle OTP
        String secret = UserProfileActivity.secret.secret;
        Button register2FA = findViewById(R.id.register2FA);
        Button unregister2FA = findViewById(R.id.unregister2FA);
        FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        DocumentReference docRef = firebase.collection("users").document(currentUser.getUid());
        //attempt to get the 2faSecret from firebase
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    //if the user has a 2faSecret, set the button to show that they are registered
                    if (document.getData().get("2faSecret") != null) {
                        register2FA.setText("2FA Registered");
                        register2FA.setEnabled(false);
                    } else {
                        unregister2FA.setEnabled(false);
                    }

                } else if (!task.getException().toString().isEmpty()) {
                    Toast.makeText(UserProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserProfileActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        unregister2FA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set user's 2faSecret to null in firebase
                Map<String, Object> firebaseSecret = new HashMap<>();
                firebaseSecret.put("2faSecret", null);

                firebase.collection("users").document(currentUser.getUid()).update(firebaseSecret);
                Toast.makeText(UserProfileActivity.this, "Successfully unregistered from 2FA verification", Toast.LENGTH_SHORT).show();
                register2FA.setText("Register for 2FA");
                register2FA.setEnabled(true);
                unregister2FA.setEnabled(false);
            }
        });


        register2FA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.register2fa_popup, null);
                PopupWindow popupWindow = new PopupWindow(popupView, ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT, true);
                popupWindow.setElevation(5.0f);
                ImageView qrCodeImageView = popupView.findViewById(R.id.qrCodeImageView);
                Button closeButton = popupView.findViewById(R.id.close_button);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });
                // Set a dim background behind the popup
                popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                popupWindow.setOutsideTouchable(true);

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

                EditText pin1 = popupView.findViewById(R.id.pin1);
                EditText pin2 = popupView.findViewById(R.id.pin2);
                EditText pin3 = popupView.findViewById(R.id.pin3);
                EditText pin4 = popupView.findViewById(R.id.pin4);
                EditText pin5 = popupView.findViewById(R.id.pin5);
                EditText pin6 = popupView.findViewById(R.id.pin6);

                pin1.addTextChangedListener(new TOTPWatcher(pin1, pin2, popupView, popupWindow));
                pin2.addTextChangedListener(new TOTPWatcher(pin2, pin3, popupView, popupWindow));
                pin3.addTextChangedListener(new TOTPWatcher(pin3, pin4, popupView, popupWindow));
                pin4.addTextChangedListener(new TOTPWatcher(pin4, pin5, popupView, popupWindow));
                pin5.addTextChangedListener(new TOTPWatcher(pin5, pin6, popupView, popupWindow));
                pin6.addTextChangedListener(new TOTPWatcher(pin6, null, popupView, popupWindow));


                String issuer = "Quizzzy";
                String account = currentUser.getEmail();
                String totpUri = TOTPUtil.getTOTPURI(secret, issuer, account);

                qrCodeImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PackageManager manager = UserProfileActivity.this.getPackageManager();
                        Intent i = manager.getLaunchIntentForPackage("com.google.android.apps.authenticator2");
                        if (i == null) {
                            try {
                                //Open google authenticator if user has it installed
                                UserProfileActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(totpUri)));
                            } catch (android.content.ActivityNotFoundException e) {
                                //Open google play store if user does not have Google Authenticator installed
                                UserProfileActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2")));
                            }
                            return;
                        }
                        i.addCategory(Intent.CATEGORY_LAUNCHER);
                        UserProfileActivity.this.startActivity(i);
                    }
                });

                try {
                    Bitmap qrCodeBitmap = QRCodeUtil.generateQRCode(totpUri);
                    qrCodeImageView.setImageBitmap(qrCodeBitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    //Class makes it so that the cursor moves to the next EditText when the user types in a character
    private class TOTPWatcher implements TextWatcher {

        private View currentView;
        private View nextView;
        private View popupView;
        private PopupWindow popupWindow;

        public TOTPWatcher(View currentView, View nextView, View popupView, PopupWindow popupWindow) {
            this.currentView = currentView;
            this.nextView = nextView;
            this.popupView = popupView;
            this.popupWindow = popupWindow;
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
                // if all fields are filled, verify the TOTP
                String secret = UserProfileActivity.secret.secret;
                EditText pin1 = popupView.findViewById(R.id.pin1);
                EditText pin2 = popupView.findViewById(R.id.pin2);
                EditText pin3 = popupView.findViewById(R.id.pin3);
                EditText pin4 = popupView.findViewById(R.id.pin4);
                EditText pin5 = popupView.findViewById(R.id.pin5);
                EditText pin6 = popupView.findViewById(R.id.pin6);

                //concatenate all the pin fields
                String totp = pin1.getText().toString() +
                        pin2.getText().toString() +
                        pin3.getText().toString() +
                        pin4.getText().toString() +
                        pin5.getText().toString() +
                        pin6.getText().toString();
                boolean isValid = verifyTOTP(secret, totp);
                if (isValid) {

                    //if the TOTP is valid, update the user's 2faSecret in firebase
                    Button register2FA = findViewById(R.id.register2FA);
                    Button unregister2FA = findViewById(R.id.unregister2FA);
                    FirebaseAuth mAuth;
                    mAuth = FirebaseAuth.getInstance();
                    FirebaseFirestore firebase = FirebaseFirestore.getInstance();
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    Map<String, Object> firebaseSecret = new HashMap<>();
                    firebaseSecret.put("2faSecret", secret);

                    firebase.collection("users").document(currentUser.getUid()).update(firebaseSecret);
                    Toast.makeText(UserProfileActivity.this, "Successfully registered for 2FA verification", Toast.LENGTH_SHORT).show();
                    register2FA.setText("2FA Registered");
                    register2FA.setEnabled(false);
                    unregister2FA.setEnabled(true);

                    popupWindow.dismiss();
                } else {
                    Toast.makeText(UserProfileActivity.this, "Invalid TOTP", Toast.LENGTH_SHORT).show();
                }
            }
        }

        private boolean areAllPinFieldsFilled() {
            EditText pin1 = popupView.findViewById(R.id.pin1);
            EditText pin2 = popupView.findViewById(R.id.pin2);
            EditText pin3 = popupView.findViewById(R.id.pin3);
            EditText pin4 = popupView.findViewById(R.id.pin4);
            EditText pin5 = popupView.findViewById(R.id.pin5);
            EditText pin6 = popupView.findViewById(R.id.pin6);

            return !pin1.getText().toString().isEmpty() &&
                    !pin2.getText().toString().isEmpty() &&
                    !pin3.getText().toString().isEmpty() &&
                    !pin4.getText().toString().isEmpty() &&
                    !pin5.getText().toString().isEmpty() &&
                    !pin6.getText().toString().isEmpty();
        }
    }

    @Override
    public void onItemClick(int position) {
        String flashletJson = gson.toJson(flashlets.get(position));
        Intent sendToFlashletDetail = new Intent(UserProfileActivity.this, FlashletDetail.class);
        sendToFlashletDetail.putExtra("flashletJSON", flashletJson);
        startActivity(sendToFlashletDetail);
    }
}