package sg.edu.np.mad.quizzzy.Search.Recycler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Models.FlashletWithUsername;
import sg.edu.np.mad.quizzzy.Models.RecyclerViewInterface;
import sg.edu.np.mad.quizzzy.R;

public class SearchedFlashletsAdapter extends RecyclerView.Adapter<SearchedFlashletsViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;
    private ArrayList<FlashletWithUsername> flashlets;

    public SearchedFlashletsAdapter(RecyclerViewInterface recyclerViewInterface) {
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public SearchedFlashletsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.searched_flashlets_recycler, parent, false);

        return new SearchedFlashletsViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchedFlashletsViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}