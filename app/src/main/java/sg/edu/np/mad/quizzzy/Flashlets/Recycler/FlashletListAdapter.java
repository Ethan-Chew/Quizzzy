package sg.edu.np.mad.quizzzy.Flashlets.Recycler;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import sg.edu.np.mad.quizzzy.Flashlets.CreateFlashlet;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletDetail;
import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.Flashlets.UpdateFlashlet;
import sg.edu.np.mad.quizzzy.MainActivity;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.R;

public class FlashletListAdapter extends RecyclerView.Adapter<FlashletListViewHolder> {
    Gson gson = new Gson();
    private final FlashletListRecyclerInterface flashletListRecyclerInterface;
    private final ArrayList<Flashlet> userFlashlets;
    private FlashletList activity;
    boolean flashletOptionsOnClick = false;

    public FlashletListAdapter(ArrayList<Flashlet> userFlashlets, FlashletList activity, FlashletListRecyclerInterface flashletListRecyclerInterface) {
        this.userFlashlets = userFlashlets;
        this.activity = activity;
        this.flashletListRecyclerInterface = flashletListRecyclerInterface;
    }

    @NonNull
    public FlashletListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flashlet_list_item, parent, false);

        return new FlashletListViewHolder(view, flashletListRecyclerInterface);
    }

    public void onBindViewHolder(FlashletListViewHolder holder, int position) {
        Flashlet listItem = userFlashlets.get(position);

        // Create Drop Down Options Menu
        PopupMenu popup = new PopupMenu(activity, holder.optionsMenu);
        popup.inflate(R.menu.flashlet_list_options);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.fLOUpdate) {
                    String flashletJson = gson.toJson(listItem);
                    Intent intent = new Intent(activity, UpdateFlashlet.class);
                    intent.putExtra("flashletJSON", flashletJson);
                    activity.startActivity(intent);
                    return true;
                } else if (itemId == R.id.fLODelete) {
                    // TODO: Handle Delete
                    // Display Alert to confirm before deletion of Flashlet
                    String confirmationMessage = "Confirm you want to delete Flashlet: " + listItem.getTitle() + "? This process is irreversible.";
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Are you sure?")
                            .setMessage(confirmationMessage)
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // Confirmed Delete
                                boolean deleteItem = listItem.deleteFlashlet();

                                if (deleteItem) {
                                    Toast.makeText(activity.getApplicationContext(), "Deleted Successfully!", Toast.LENGTH_LONG).show();
                                }
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

        String flashcardCountTxt = userFlashlets.size() + " Keyword" + (userFlashlets.size() > 1 ? "s" : "");
        holder.flashcardCountLabel.setText(flashcardCountTxt);

        Date lastUpdated = new Date(listItem.getLastUpdatedUnix() * 1000L);
        String lastUpdatedStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(lastUpdated);
        holder.lastUpdatedLabel.setText(lastUpdatedStr);
    }

    public int getItemCount() { return userFlashlets.size(); }
}
