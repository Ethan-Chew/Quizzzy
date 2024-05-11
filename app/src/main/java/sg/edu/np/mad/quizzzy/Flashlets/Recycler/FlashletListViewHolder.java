package sg.edu.np.mad.quizzzy.Flashlets.Recycler;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.quizzzy.R;

public class FlashletListViewHolder extends RecyclerView.ViewHolder {

    TextView titleLabel;
    TextView flashcardCountLabel;
    TextView lastUpdatedLabel;
    TextView optionsMenu;

    public FlashletListViewHolder(View itemView) {
        super(itemView);

        // Find item from View
        titleLabel = itemView.findViewById(R.id.fLITitle);
        flashcardCountLabel = itemView.findViewById(R.id.fLICount);
        lastUpdatedLabel = itemView.findViewById(R.id.fLILUpdatedLabel);
        optionsMenu = itemView.findViewById(R.id.fLIOptionsMenu);
    }
}
