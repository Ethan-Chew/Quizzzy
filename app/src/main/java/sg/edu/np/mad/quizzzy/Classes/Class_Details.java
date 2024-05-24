package sg.edu.np.mad.quizzzy.Classes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Models.UserClass;
import sg.edu.np.mad.quizzzy.R;

public class Class_Details extends AppCompatActivity {
    Gson gson = new Gson();
    
    UserClass userClass;
    TextView classtitle;
    TextView memberscount;
    Button Studyfleshlets;
    LinearLayout memberscontainer;
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

        classtitle = findViewById(R.id.cdclasstitle);
        memberscount = findViewById(R.id.cdmembers);
        Studyfleshlets = findViewById(R.id.cdStudyfleshlets);
        memberscontainer = findViewById(R.id.cdmemberscontainer);

        Intent receiveintent = getIntent();
        userClass = gson.fromJson(receiveintent.getStringExtra("classJSON"), UserClass.class);
        ArrayList<String> members = userClass.getMembers();
        
        classtitle.setText(UserClass.getClassTitle());
        String membercount = members.size() + " Total Members" + (members.size() == 1 ? "" : "s");
        memberscount.setText(membercount);
    }

}