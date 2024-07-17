package sg.edu.np.mad.quizzzy.Flashlets.Recycler;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.Flashlets.UpdateFlashlet;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.RecyclerViewInterface;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.R;

public class FlashletListAdapter extends RecyclerView.Adapter<FlashletListViewHolder> {
    Gson gson = new Gson();

    // Initialisation of Firebase Cloud Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final RecyclerViewInterface recyclerViewInterface;
    private final ArrayList<Flashlet> userFlashlets;
    private final User user;
    private FlashletList activity;
    boolean flashletOptionsOnClick = false;
    private FlashletCountListener flashletCountListener;

    public interface FlashletCountListener {
        void flashletCount(Integer count);
    }

    public FlashletListAdapter(ArrayList<Flashlet> userFlashlets, FlashletList activity, RecyclerViewInterface recyclerViewInterface, User user, FlashletCountListener flashletCountListener) {
        this.userFlashlets = userFlashlets;
        this.activity = activity;
        this.recyclerViewInterface = recyclerViewInterface;
        this.user = user;
        this.flashletCountListener = flashletCountListener;
    }

    @NonNull
    public FlashletListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flashlet_list_item, parent, false);

        return new FlashletListViewHolder(view, recyclerViewInterface);
    }

    public void onBindViewHolder(FlashletListViewHolder holder, int position) {
        Flashlet listItem = userFlashlets.get(holder.getAdapterPosition());

        // Create Drop Down Options Menu
        PopupMenu popup = new PopupMenu(activity, holder.optionsMenu);
        popup.inflate(R.menu.flashlet_list_options);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.fLOUpdate) {
                    String flashletJson = gson.toJson(listItem);
                    String userJson = gson.toJson(user);

                    Intent intent = new Intent(activity, UpdateFlashlet.class);
                    intent.putExtra("flashletJSON", flashletJson);
                    intent.putExtra("userJSON", userJson);
                    activity.startActivity(intent);
                    return true;
                } else if (itemId == R.id.fLODelete) {
                    SQLiteManager localDB = SQLiteManager.instanceOfDatabase(activity);
                    // Display Alert to confirm before deletion of Flashlet
                    String confirmationMessage = "Confirm you want to delete Flashlet: " + listItem.getTitle() + "? This process is irreversible.";
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Are you sure?")
                            .setMessage(confirmationMessage)
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // Confirmed Delete
                                db.collection("flashlets").document(listItem.getId())
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                User user = localDB.getUser().getUser();
                                                userFlashlets.remove(listItem);
                                                ArrayList<String> createdFlashletsId = user.getCreatedFlashlets();
                                                createdFlashletsId.remove(listItem.getId());
                                                // Update User's Created Flashlet List in Firebase
                                                db.collection("users").document(user.getId()).update("createdFlashlets", createdFlashletsId).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            localDB.updateCreatedFlashcards(user.getId(), createdFlashletsId);
                                                            notifyItemRemoved(holder.getAdapterPosition());
                                                            notifyItemRangeChanged(holder.getAdapterPosition(), getItemCount());

                                                            Toast.makeText(activity.getApplicationContext(), "Deleted Successfully!", Toast.LENGTH_LONG).show();
                                                            flashletCountListener.flashletCount(getItemCount());
                                                        }
                                                    }
                                                });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("Delete Flashlet", e.toString());
                                                Toast.makeText(activity.getApplicationContext(), "Failed to Delete!", Toast.LENGTH_LONG).show();
                                            }
                                        });

                            })
                            .setNegativeButton("Cancel", ((dialog, which) -> {
                                // Handle Cancel Delete
                            }))
                            .setCancelable(true);

                    builder.create().show(); // Show Alert
                    return true;
                }
                return false;
            }
        });
        holder.optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flashletOptionsOnClick = !flashletOptionsOnClick;
                if (flashletOptionsOnClick) {
                    popup.show();
                }
            }
        });

        // Set Text of Elements on UI
        holder.titleLabel.setText(listItem.getTitle());

        String flashcardCountTxt = listItem.getFlashcards().size() + " Keyword" + (listItem.getFlashcards().size() > 1 ? "s" : "");
        holder.flashcardCountLabel.setText(flashcardCountTxt);

        Date lastUpdated = new Date(listItem.getLastUpdatedUnix() * 1000L);
        String lastUpdatedStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(lastUpdated);
        holder.lastUpdatedLabel.setText(lastUpdatedStr);
    }

    public int getItemCount() { return userFlashlets.size(); }
}
