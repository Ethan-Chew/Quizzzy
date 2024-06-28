package sg.edu.np.mad.quizzzy.Search.Recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Models.RecyclerViewInterface;
import sg.edu.np.mad.quizzzy.R;

public class RecentSearchesAdapter extends RecyclerView.Adapter<RecentSearchesViewHolder> {
    private final RecyclerViewInterface recyclerViewInterface;

    private ArrayList<String> recentSearches;

    public RecentSearchesAdapter(RecyclerViewInterface recyclerViewInterface, ArrayList<String> recentSearches) {
        this.recyclerViewInterface = recyclerViewInterface;
        this.recentSearches = recentSearches;
    }

    @NonNull
    @Override
    public RecentSearchesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_searches_recycler_adapter, parent, false);

        return new RecentSearchesViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentSearchesViewHolder holder, int position) {
        String listItem = recentSearches.get(position);

        // Set Text of Elements on the UI
        holder.titleLabel.setText(listItem);
    }

    @Override
    public int getItemCount() {
        return recentSearches.size();
    }
}
