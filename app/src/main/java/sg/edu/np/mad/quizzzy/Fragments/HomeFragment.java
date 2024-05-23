package sg.edu.np.mad.quizzzy.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;


import androidx.fragment.app.Fragment;

import sg.edu.np.mad.quizzzy.R;

public class HomeFragment extends Fragment {
    TextView usernameView;
    public HomeFragment(){
        // require a empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(container.getContext());
        UserWithRecents userWithRecents = localDB.getUser();
        User user = userWithRecents.getUser();

        usernameView = usernameView.findViewById(R.id.usernameView);
        usernameView.setText(user.getUsername());

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}