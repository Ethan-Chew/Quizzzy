package sg.edu.np.mad.quizzzy;

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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Classes.ClassDetail;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserClass;

public class ClassStudyAdapter extends RecyclerView.Adapter<ClassStudyViewHolder> {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Gson gson = new Gson();
    ArrayList<User> users;
    ArrayList<String> classMembers;
    ArrayList<String> usernames;

    public ClassStudyAdapter(ClassStudyActivity activity, ArrayList<String> usernames) {
        this.usernames = usernames;
    }

    public ClassStudyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_study_item, parent, false);
        ClassStudyViewHolder holder = new ClassStudyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ClassStudyViewHolder holder, int position) {
        // String userId = classMembers.get(position);
        holder.username.setText(usernames.get(position));
        holder.studyDuration.setText("00:00:00");
    }

    @Override
    public int getItemCount() {
        return usernames.size();
    }
}
