package sg.edu.np.mad.quizzzy;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import sg.edu.np.mad.quizzzy.Classes.ClassDetail;
import sg.edu.np.mad.quizzzy.Models.StudyDurationHelper;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserClass;

public class ClassStudyAdapter extends RecyclerView.Adapter<ClassStudyViewHolder> {
    FirebaseDatabase firebaseDB = FirebaseDatabase.getInstance("https://quizzzy-21bea-default-rtdb.asia-southeast1.firebasedatabase.app/");
    DatabaseReference firebaseReference = firebaseDB.getReference("studyDuration");
    Gson gson = new Gson();
    ArrayList<String> usernames;
    ArrayList<String> userIds;
    ArrayList<Integer> studyDurations;

    public ClassStudyAdapter(ClassStudyActivity activity, ArrayList<String> usernames, ArrayList<String> userIds) {
        this.usernames = usernames;
        this.userIds = userIds;
    }

    public ClassStudyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_study_item, parent, false);
        ClassStudyViewHolder holder = new ClassStudyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ClassStudyViewHolder holder, int position) {
        String userId = userIds.get(position);

        holder.username.setText(usernames.get(position));

        firebaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(userId)) {
                    int studyDuration = Integer.parseInt(snapshot.child(userId).child("studyDuration").getValue(String.class));
                    Log.d("database is gay", "onDataChange: " + usernames + " " + userIds);
                    holder.studyDuration.setText(String.format(
                            Locale.getDefault(), "%02d:%02d:%02d",
                            studyDuration / 3600,
                            (studyDuration % 3600) / 60,
                            studyDuration % 60
                    ));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("Firebase", "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public int getItemCount() {
        return usernames.size();
    }
}
