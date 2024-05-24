package sg.edu.np.mad.quizzzy.Classes;

import android.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
    private Class_Page activity;

    public ClassAdapter(ClassRecyclerInterface classRecyclerInterface, Class_Page activity, ArrayList<UserClass> classes) {
        this.classRecyclerInterface = classRecyclerInterface;
        this.classes = classes;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.classes, parent, false);
        return new ClassViewHolder(item, classRecyclerInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        UserClass listItem = classes.get(position);

        holder.classname.setText(listItem.getClassTitle());
//        PopupMenu popup = new PopupMenu(activity, holder.optionsMenu);
//        popup.inflate(R.menu.class_options)
//        ArrayList<String> classids = new ArrayList<String>();
//            DocumentReference docRef = db.collection("class").document(classids.get(position));
//            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot document = task.getResult();
//                        if (document.exists()) {
//                            String flashletJson = gson.toJson(document.getData());
//                            classes.add(gson.fromJson(flashletJson, UserClass.class));
//                        }
//                    }
//                }
//            });
//        holder.classname.setText(listItem.getClassTitle());
    }

    @Override
    public int getItemCount() {
        return classes.size();
    }
}
