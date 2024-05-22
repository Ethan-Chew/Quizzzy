package sg.edu.np.mad.quizzzy.Classes;

import android.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.R;

public class ClassAdapter extends RecyclerView.Adapter<ClassViewHolder>{
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final ClassRecyclerInterface classRecyclerInterface;
    private final ArrayList<Class> classes;

    public ClassAdapter(ClassRecyclerInterface classRecyclerInterface, ArrayList<Class> userClass, ArrayList<Class> classes) {
        this.classRecyclerInterface = classRecyclerInterface;
        this.classes = classes;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.classes, parent, false);
        return new ClassViewHolder(item, classRecyclerInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        Class listItem = classes.get(position);
        //holder.classname.setText(listItem.getClassTitle());
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
