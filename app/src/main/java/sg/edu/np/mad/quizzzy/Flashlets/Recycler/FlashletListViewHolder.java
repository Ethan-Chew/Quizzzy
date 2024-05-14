package sg.edu.np.mad.quizzzy.Flashlets.Recycler;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.quizzzy.Flashlets.FlashletDetail;
import sg.edu.np.mad.quizzzy.R;

public class FlashletListViewHolder extends RecyclerView.ViewHolder {

    LinearLayout container;
    TextView titleLabel;
    TextView flashcardCountLabel;
    TextView lastUpdatedLabel;
    TextView optionsMenu;

    public FlashletListViewHolder(View itemView, FlashletListRecyclerInterface flashletListRecyclerInterface) {
        super(itemView);

        // Find item from View
        container = itemView.findViewById(R.id.fLIContainer);
        titleLabel = itemView.findViewById(R.id.fLITitle);
        flashcardCountLabel = itemView.findViewById(R.id.fLICount);
        lastUpdatedLabel = itemView.findViewById(R.id.fLILUpdatedLabel);
        optionsMenu = itemView.findViewById(R.id.fLIOptionsMenu);

        // Set onClick of Flashlet List Container
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (flashletListRecyclerInterface != null) {
                    int pos = getAdapterPosition();

                    if (pos != RecyclerView.NO_POSITION) {
                        flashletListRecyclerInterface.onItemClick(pos);
                    }
                }
            }
        });
    }
}
