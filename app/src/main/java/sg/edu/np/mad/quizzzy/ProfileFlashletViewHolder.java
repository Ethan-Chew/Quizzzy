package sg.edu.np.mad.quizzzy;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.quizzzy.Models.RecyclerViewInterface;

public class ProfileFlashletViewHolder extends RecyclerView.ViewHolder {

    TextView titleLabel;
    TextView flashcardCountLabel;

    public ProfileFlashletViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
        super(itemView);

        // Find Item from View
        titleLabel = itemView.findViewById(R.id.fLITitle);
        flashcardCountLabel = itemView.findViewById(R.id.fLICount);
        TextView lastUpdatedLabel = itemView.findViewById(R.id.fLILUpdatedLabel);
        TextView optionsMenu = itemView.findViewById(R.id.fLIOptionsMenu);

        // Hide Unnecessary Items
        lastUpdatedLabel.setVisibility(View.GONE);
        optionsMenu.setVisibility(View.GONE);

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
