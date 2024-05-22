package sg.edu.np.mad.quizzzy.Classes;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.BreakIterator;

import sg.edu.np.mad.quizzzy.R;

public class ClassViewHolder extends RecyclerView.ViewHolder {
    TextView classname;
    TextView options;
    public ClassViewHolder(View itemView, ClassRecyclerInterface classRecyclerInterface){
        super(itemView);

        classname = itemView.findViewById(R.id.cpViewTitle);
        options = itemView.findViewById(R.id.cpOptionsMenu);

    }
}
