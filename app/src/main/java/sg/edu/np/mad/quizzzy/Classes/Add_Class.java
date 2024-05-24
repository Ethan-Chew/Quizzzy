package sg.edu.np.mad.quizzzy.Classes;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.UUID;

import sg.edu.np.mad.quizzzy.Models.UserClass;
import sg.edu.np.mad.quizzzy.R;

public class Add_Class extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    UserClass newClass;

    private Button addMemberBtn;
    private View newMemberView;
    private ArrayList<String> newMemberList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.add_class);
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
                // this will run when your button is clicked
                /// create the view
                newMemberView = LayoutInflater.from(Add_Class.this).inflate(R.layout.add_class_members, null, false);

                EditText member = newMemberView.findViewById(R.id.acmusername);
                newMemberList.add(member.getText().toString());

                addmem.addView(newMemberView);

                View spacerView = new View(Add_Class.this);
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

                String classId = UUID.randomUUID().toString();
                ArrayList<String> creatorId = new ArrayList<>();
                creatorId.add(userId);
                ArrayList<String> memberId = new ArrayList<>();
                memberId.add(userId);
                newClass = new UserClass(classId, title, creatorId, memberId, System.currentTimeMillis() / 1000L);

                createClassbtn.setEnabled(false);
                createClassbtn.setText("Loading...");

                db.collection("class")
                        .document(classId)
                        .set(newClass)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                createClassbtn.setText("Create Class");
                                Toast.makeText(getApplicationContext(), "Class successfully created!", Toast.LENGTH_LONG).show();

                                Intent classPageIntent = new Intent (Add_Class.this, Class_Page.class);
                                startActivity(classPageIntent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                createClassbtn.setText("Create Class");
                                createClassbtn.setEnabled(true);

                                Log.e("Class Creation", e.toString());
                                Toast.makeText(getApplicationContext(), "Failed to create Class!", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

    }

}