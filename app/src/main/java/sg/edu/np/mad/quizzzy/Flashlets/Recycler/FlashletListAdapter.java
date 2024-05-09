package sg.edu.np.mad.quizzzy.Flashlets.Recycler;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.R;

public class FlashletListAdapter extends RecyclerView.Adapter<FlashletListViewHolder> {
    private final ArrayList<Flashlet> userFlashlets;
    private FlashletList activity;

    public FlashletListAdapter(ArrayList<Flashlet> userFlashlets, FlashletList activity) {
        this.userFlashlets = userFlashlets;
        this.activity = activity;
    }

    @NonNull
    public FlashletListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flashlet_list_item, parent, false);

        return new FlashletListViewHolder(view);
    }

    public void onBindViewHolder(FlashletListViewHolder holder, int position) {
        Flashlet listItem = userFlashlets.get(position);

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
