package sg.edu.np.mad.quizzzy.Search;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Models.RecyclerViewInterface;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.R;
import sg.edu.np.mad.quizzzy.Search.Recycler.SearchedUsersAdapter;
import sg.edu.np.mad.quizzzy.UserProfileActivity;

/*
 * The SearchUsersFragment file takes in the searched Users from the SearchAdapter,
 * and populates it into the RecyclerView
 * */
public class SearchUsersFragment extends Fragment implements RecyclerViewInterface {
    RecyclerView usersRecyclerView;
    LinearLayout noRelatedSearchesContainer;
    Gson gson = new Gson();
    ArrayList<User> users = new ArrayList<User>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get Data from Fragment Bundle
        Bundle args = getArguments();
        Type ArrayOfUserType = new TypeToken<ArrayList<User>>(){}.getType();
        String flashletJson = args.getString("searchedUsers");
        users = gson.fromJson(flashletJson, ArrayOfUserType);

        noRelatedSearchesContainer = getView().findViewById(R.id.fSUNoResultsInfo);
        if (users.isEmpty()) {
            noRelatedSearchesContainer.setVisibility(View.VISIBLE);
        }

        usersRecyclerView = getView().findViewById(R.id.fSURecyclerView);
        SearchedUsersAdapter searchedUsersAdapter = new SearchedUsersAdapter(SearchUsersFragment.this, users);
        LinearLayoutManager usersLayoutManager = new LinearLayoutManager(getActivity());
        usersRecyclerView.setLayoutManager(usersLayoutManager);
        usersRecyclerView.setItemAnimator(new DefaultItemAnimator());
        usersRecyclerView.setAdapter(searchedUsersAdapter);
    }

    // When the User taps on any User in the RecyclerView, the app would bring them to the related UserProfile page
    @Override
    public void onItemClick(int position) {
        Intent sendToUserIntent = new Intent(getActivity(), UserProfileActivity.class);
        sendToUserIntent.putExtra("userJSON", gson.toJson(users.get(position)));
        startActivity(sendToUserIntent);
    }
}