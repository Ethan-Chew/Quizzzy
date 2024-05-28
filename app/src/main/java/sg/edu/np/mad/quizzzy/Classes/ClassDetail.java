package sg.edu.np.mad.quizzzy.Classes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Flashlets.CreateFlashlet;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserClass;
import sg.edu.np.mad.quizzzy.R;

public class ClassDetail extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Gson gson = new Gson();
    
    UserClass userClass;
    TextView classtitle;
    TextView memberscount;
    Button Studyfleshlets;
    LinearLayout memberscontainer;

    ArrayList<User> users = new ArrayList<User>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_class_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Handle Back Navigation Toolbar
        Toolbar toolbar = findViewById(R.id.fDViewToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClassDetail.this.getOnBackPressedDispatcher().onBackPressed();
            }
        });

        classtitle = findViewById(R.id.cdclasstitle);
        memberscount = findViewById(R.id.cdmembers);
        Studyfleshlets = findViewById(R.id.cdStudyfleshlets);
        memberscontainer = findViewById(R.id.cdmemberscontainer);

        Intent receiveintent = getIntent();
        userClass = gson.fromJson(receiveintent.getStringExtra("classJSON"), UserClass.class);
        ArrayList<String> members = userClass.getMemberId();

        classtitle.setText(userClass.getClassTitle());
        String membercount = members.size() + " Total Members" + (members.size() == 1 ? "" : "s");
        memberscount.setText(membercount);

        db.collection("class").whereIn("id", members).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String classJson = gson.toJson(document.getData());
                                users.add(gson.fromJson(classJson, User.class));
                            }

                            for (int i = 0; i < users.size(); i++) {
                                User user = users.get(i);
                                View memberView = LayoutInflater.from(ClassDetail.this).inflate(R.layout.member_list, null, false);
                                TextView memberusername = memberView.findViewById(R.id.mlusername);
                                memberusername.setText(user.getUsername());

                                memberscontainer.addView(memberView);

                                View spacerView = new View(ClassDetail.this);
                                LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        20
                                );
                                memberscontainer.addView(spacerView, spacerParams);
                            }
                        }
                    }
                });
    }

}