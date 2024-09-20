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

import sg.edu.np.mad.quizzzy.Flashlets.FlashletDetail;
import sg.edu.np.mad.quizzzy.Models.FlashletWithCreator;
import sg.edu.np.mad.quizzzy.Models.RecyclerViewInterface;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;
import sg.edu.np.mad.quizzzy.R;
import sg.edu.np.mad.quizzzy.Search.Recycler.SearchedFlashletsAdapter;

/*
* The SearchFlashletFragment file takes in the searched Flashlets from the SearchAdapter,
* and populates it into the RecyclerView
* */
public class SearchFlashletFragment extends Fragment implements RecyclerViewInterface {

    RecyclerView flashletRecyclerView;
    LinearLayout noRelatedSearchesContainer;
    Gson gson = new Gson();
    ArrayList<FlashletWithCreator> flashlets = new ArrayList<FlashletWithCreator>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_flashlet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get Data from Fragment Bundle
        Bundle args = getArguments();
        Type ArrayOfFlashletsType = new TypeToken<ArrayList<FlashletWithCreator>>(){}.getType();
        String flashletJson = args.getString("searchedFlashlets");
        flashlets = gson.fromJson(flashletJson, ArrayOfFlashletsType);

        noRelatedSearchesContainer = getView().findViewById(R.id.fSFNoResultsInfo);
        if (flashlets.isEmpty()) {
            noRelatedSearchesContainer.setVisibility(View.VISIBLE);
        }

        flashletRecyclerView = getView().findViewById(R.id.fSFRecyclerView);
        SearchedFlashletsAdapter searchedFlashletsAdapter = new SearchedFlashletsAdapter(SearchFlashletFragment.this, flashlets);
        LinearLayoutManager flashetLayoutManager = new LinearLayoutManager(getActivity());
        flashletRecyclerView.setLayoutManager(flashetLayoutManager);
        flashletRecyclerView.setItemAnimator(new DefaultItemAnimator());
        flashletRecyclerView.setAdapter(searchedFlashletsAdapter);
    }

    // When the User taps on any Flashlet in the RecyclerView, the app would bring them to the related FlashletDetail page
    @Override
    public void onItemClick(int position) {
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(getActivity());
        UserWithRecents currentUser = localDB.getUser();

        FlashletWithCreator flashlet = flashlets.get(position);
        Intent sendToFlashletDetail = new Intent(getActivity(), FlashletDetail.class);sendToFlashletDetail.putExtra("flashletJSON", gson.toJson(flashlet));
        sendToFlashletDetail.putExtra("userId", currentUser.getUser().getId());

        startActivity(sendToFlashletDetail);
    }
}