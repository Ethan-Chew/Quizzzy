package sg.edu.np.mad.quizzzy.Classes;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserClass;
import sg.edu.np.mad.quizzzy.R;

public class ClassAdapter extends RecyclerView.Adapter<ClassViewHolder>{
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Gson gson = new Gson();

    private final ClassRecyclerInterface classRecyclerInterface;
    private ArrayList<UserClass> classes;
    private ClassList activity;
    private User user;

    public ClassAdapter(ClassRecyclerInterface classRecyclerInterface, ClassList activity, ArrayList<UserClass> classes, User user) {
        this.classRecyclerInterface = classRecyclerInterface;
        this.classes = classes;
        this.activity = activity;
        this.user = user;
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

        // Check if User is the Creator. If yes, allow for Update and Delete Operations
        if (!listItem.getMembers().contains(user.getId())) {
            holder.options.setVisibility(View.GONE);
            return;
        }

        // Configure Popup
        PopupMenu popup = new PopupMenu(activity, holder.options);
        popup.inflate(R.menu.classes_list_options);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.cLOUpdate) {
                    Intent sendToUpdate = new Intent(activity, UpdateClass.class);
                    sendToUpdate.putExtra("classJson", gson.toJson(listItem));
                    activity.startActivity(sendToUpdate);
                } else if (itemId == R.id.cLODelete) {
                    // Display Alert to confirm before deletion of Flashlet
                    String confirmationMessage = "Confirm you want to delete Class: " + listItem.getClassTitle() + "? This process is irreversible.";
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Are you sure?")
                            .setMessage(confirmationMessage)
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // Confirmed Delete
                                Intent sendToUpdate = new Intent(activity, UpdateClass.class);
                                sendToUpdate.putExtra("classJson", gson.toJson(listItem));
                                activity.startActivity(sendToUpdate);

                            })
                            .setNegativeButton("Cancel", ((dialog, which) -> {
                                // Handle Cancel Delete
                            }))
                            .setCancelable(true);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return classes.size();
    }
}
