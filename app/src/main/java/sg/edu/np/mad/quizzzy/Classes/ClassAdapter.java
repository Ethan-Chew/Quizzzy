package sg.edu.np.mad.quizzzy.Classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Models.UserClass;
import sg.edu.np.mad.quizzzy.R;

public class ClassAdapter extends RecyclerView.Adapter<ClassViewHolder>{
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Gson gson = new Gson();

    private final ClassRecyclerInterface classRecyclerInterface;
    private ArrayList<UserClass> classes;
    private ClassList activity;

    public ClassAdapter(ClassRecyclerInterface classRecyclerInterface, ClassList activity, ArrayList<UserClass> classes) {
        this.classRecyclerInterface = classRecyclerInterface;
        this.classes = classes;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_list_item, parent, false);
        return new ClassViewHolder(item, classRecyclerInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        UserClass listItem = classes.get(position);

        holder.classname.setText(listItem.getClassTitle());
        String memberCountText = listItem.getMembers().size() + " Member" + (listItem.getMembers().size() == 1 ? "" : "s");
        holder.memberCount.setText(memberCountText);
    }

    @Override
    public int getItemCount() {
        return classes.size();
    }
}
