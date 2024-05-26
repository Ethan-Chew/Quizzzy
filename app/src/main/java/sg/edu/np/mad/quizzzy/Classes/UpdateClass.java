package sg.edu.np.mad.quizzzy.Classes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

public class UpdateClass extends AppCompatActivity {
    Gson gson = new Gson();
    UserClass userClass;
    ArrayList<EditText> newUNList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_class);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Receive Class from Intent
        Intent receiveIntent = getIntent();
        userClass = gson.fromJson(receiveIntent.getStringExtra("classJson"), UserClass.class);

        // Populate View Components with Data
        EditText titleEditField = findViewById(R.id.uCNewTitle);
        titleEditField.setHint(userClass.getClassTitle());

        LinearLayout memberList = findViewById(R.id.uCUpdateMembers);
        for (String username : userClass.getMembers()) {
            View listItem = LayoutInflater.from(UpdateClass.this).inflate(R.layout.add_class_members, null, false);
            EditText listItemEdit = listItem.findViewById(R.id.acmusername);
            listItemEdit.setHint(username);
            listItemEdit.setInputType(0); // Disable Text Editing

            ImageView deleteMember = listItem.findViewById(R.id.acmDelete);
            deleteMember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    memberList.removeView(listItem);
                }
            });

            memberList.addView(listItem);
        }
    }
}