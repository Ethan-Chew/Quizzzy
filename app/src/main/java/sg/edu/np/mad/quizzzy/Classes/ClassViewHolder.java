package sg.edu.np.mad.quizzzy.Classes;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.BreakIterator;

import sg.edu.np.mad.quizzzy.R;

public class ClassViewHolder extends RecyclerView.ViewHolder {
    TextView classname;
    TextView options;
    TextView memberCount;
    LinearLayout container;
    public ClassViewHolder(View itemView, ClassRecyclerInterface classRecyclerInterface) {
        super(itemView);

        classname = itemView.findViewById(R.id.cpViewTitle);
        options = itemView.findViewById(R.id.clOptionsMenu);
        memberCount = itemView.findViewById(R.id.cpnummem);
        container = itemView.findViewById(R.id.cLIContainer);

        // Set onClick of Class List Container
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (classRecyclerInterface != null) {
                    int pos = getAdapterPosition();

                    if (pos != RecyclerView.NO_POSITION) {
                        classRecyclerInterface.onItemClick(pos);
                    }
                }
            }
        });
    }
}
