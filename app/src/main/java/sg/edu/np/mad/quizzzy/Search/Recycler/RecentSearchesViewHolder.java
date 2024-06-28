package sg.edu.np.mad.quizzzy.Search.Recycler;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.quizzzy.Models.RecyclerViewInterface;
import sg.edu.np.mad.quizzzy.R;

public class RecentSearchesViewHolder extends RecyclerView.ViewHolder {
    LinearLayout container;
    TextView titleLabel;
    ImageView removeSearch;

    public RecentSearchesViewHolder(View itemView, RecyclerViewInterface recyclerViewInterface) {
        super(itemView);

        // Find Item from View
        container = itemView.findViewById(R.id.rSRContainer);
        titleLabel = itemView.findViewById(R.id.rSRText);
        removeSearch = itemView.findViewById(R.id.rSRRemove);

        // Set onClick Function of Item View
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recyclerViewInterface != null) {
                    int pos = getAdapterPosition();

                    if (pos != RecyclerView.NO_POSITION) {
                        recyclerViewInterface.onItemClick(pos);
                    }
                }
            }
        });
    }
}
