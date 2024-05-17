package sg.edu.np.mad.quizzzy.Classes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.UUID;

import sg.edu.np.mad.quizzzy.R;

public class Add_Class extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        addMemberBtn = findViewById(R.id.cpadd_membersbtn);
        LinearLayout addmem = findViewById(R.id.addmembers);
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
        ///Class class = new Class(UUID.randomUUID().toString(), '', '');

    }

}