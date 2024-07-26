package sg.edu.np.mad.quizzzy.Search.Recycler;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.quizzzy.Models.RecyclerViewInterface;
import sg.edu.np.mad.quizzzy.R;

public class SearchedUsersViewHolder extends RecyclerView.ViewHolder {

    TextView userTitleLabel;
    TextView flashletCountLabel;

    public SearchedUsersViewHolder(View itemView, RecyclerViewInterface recyclerViewInterface) {
        super(itemView);

        // Find Item From View
        userTitleLabel = itemView.findViewById(R.id.sURTitle);
        flashletCountLabel = itemView.findViewById(R.id.sURFlashletCount);

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
