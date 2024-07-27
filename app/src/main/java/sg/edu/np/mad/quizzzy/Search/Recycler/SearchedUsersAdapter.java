package sg.edu.np.mad.quizzzy.Search.Recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Models.RecyclerViewInterface;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.R;

public class SearchedUsersAdapter extends RecyclerView.Adapter<SearchedUsersViewHolder> {
    // Initialisation of Firebase Cloud Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final RecyclerViewInterface recyclerViewInterface;
    private ArrayList<User> users;

    public SearchedUsersAdapter(RecyclerViewInterface recyclerViewInterface, ArrayList<User> users) {
        this.recyclerViewInterface = recyclerViewInterface;
        this.users = users;
    }

    @NonNull
    @Override
    public SearchedUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.searched_users_recycler, parent, false);

        return new SearchedUsersViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchedUsersViewHolder holder, int position) {
        User userListItem = users.get(position);

        // Get the number of public flashlets for the user
        Query query = db.collection("flashlets").whereArrayContains("creatorID", userListItem.getId()).whereEqualTo("isPublic", true);
        AggregateQuery publicFlashletCount = query.count();
        publicFlashletCount.get(AggregateSource.SERVER).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                AggregateQuerySnapshot snapshot = task.getResult();
                // Set Text of Elements
                holder.userTitleLabel.setText(userListItem.getUsername());
                String flashletCountText = snapshot.getCount() + " Flashlets";
                holder.flashletCountLabel.setText(flashletCountText);
            }
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
