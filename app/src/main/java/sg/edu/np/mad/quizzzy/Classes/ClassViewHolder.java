package sg.edu.np.mad.quizzzy.Classes;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.quizzzy.R;

public class ClassViewHolder extends RecyclerView.ViewHolder {
    TextView classname;
    TextView description;
    Button addmember;
    Button  addfl;
    Button edit;
    Button delete;
    public ClassViewHolder(View itemView){
        super(itemView);

        classname = itemView.findViewById(R.id.cpViewTitle);
        description = itemView.findViewById(R.id.cpCounterLabel);
        addmember = itemView.findViewById(R.id.cpadd_members);
        addfl = itemView.findViewById(R.id.cpadd_fl);
        edit = itemView.findViewById(R.id.cpadd_edit);
        delete = itemView.findViewById(R.id.cpadd_delete);
    }
}
