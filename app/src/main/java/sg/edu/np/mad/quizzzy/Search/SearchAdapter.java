package sg.edu.np.mad.quizzzy.Search;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.jetbrains.annotations.NotNull;

public class SearchAdapter extends FragmentStateAdapter {

    public SearchAdapter(@NonNull FragmentManager fragmentManager, @NotNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new SearchUsersFragment();
        }
        return new SearchFlashletFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}