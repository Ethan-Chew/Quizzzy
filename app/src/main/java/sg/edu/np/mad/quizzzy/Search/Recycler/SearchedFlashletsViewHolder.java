package sg.edu.np.mad.quizzzy.Search.Recycler;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.quizzzy.Models.RecyclerViewInterface;
import sg.edu.np.mad.quizzzy.R;

public class SearchedFlashletsViewHolder extends RecyclerView.ViewHolder {

    TextView flashletTitleLabel;
    TextView flashcardCountLabel;
    TextView ownerUsernameLabel;

    public SearchedFlashletsViewHolder(View itemView, RecyclerViewInterface recyclerViewInterface) {
        super(itemView);

        // Find Item from View
        flashletTitleLabel = itemView.findViewById(R.id.sFRTitle);
        flashcardCountLabel = itemView.findViewById(R.id.sFRFlashcardCount);
        ownerUsernameLabel = itemView.findViewById(R.id.sFRUsername);

        // Set onClick of Flashlet List Container
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
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
