package sg.edu.np.mad.quizzzy.Search;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import sg.edu.np.mad.quizzzy.Models.SearchResult;

public class SearchAdapter extends FragmentStateAdapter {

    final private SearchResult searchResult;
    Gson gson = new Gson();

    public SearchAdapter(@NonNull FragmentManager fragmentManager, @NotNull Lifecycle lifecycle, SearchResult searchResult) {
        super(fragmentManager, lifecycle);
        this.searchResult = searchResult;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        SearchFlashletFragment flashletFragment = new SearchFlashletFragment();
        Bundle args1 = new Bundle();
        args1.putString("searchedFlashlets", gson.toJson(searchResult.getFlashlets()));
        flashletFragment.setArguments(args1);

        SearchUsersFragment usersFragment = new SearchUsersFragment();
        Bundle args2 = new Bundle();
        args2.putString("searchedUsers", gson.toJson(searchResult.getUsers()));
        usersFragment.setArguments(args2);

        if (position == 1) {
            return usersFragment;
        }
        return flashletFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}