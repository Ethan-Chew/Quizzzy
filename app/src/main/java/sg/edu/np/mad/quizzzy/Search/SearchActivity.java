package sg.edu.np.mad.quizzzy.Search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.HomeActivity;
import sg.edu.np.mad.quizzzy.Models.FlashletWithCreator;
import sg.edu.np.mad.quizzzy.Models.RecyclerViewInterface;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.SQLiteRecentSearchesManager;
import sg.edu.np.mad.quizzzy.Models.SearchResult;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.R;
import sg.edu.np.mad.quizzzy.Search.Recycler.RecentSearchesAdapter;
import sg.edu.np.mad.quizzzy.StatisticsActivity;

// Callback function to handle Searches; onSearchResult is called after the search result is returned from the Firebase Query
interface OnSearchEventListener {
    void onSearchResult(SearchResult searchResult);
    void onError(Exception err);
}

/*
 * SearchActivity is the main user-facing screen responsible for handling the Global Search for Flashlets and User
 * */
public class SearchActivity extends AppCompatActivity implements RecyclerViewInterface, RecentSearchesAdapter.OnResultChangeListener {

    // Search Result Items
    private TabLayout searchResultTabs;
    private ViewPager2 searchResultViewPager;
    private ScrollView recentsListContainer;
    private RecentSearchesAdapter searchesAdapter;
    private LinearLayout noRecentsContainer;
    private RecyclerView recentsContainer;
    private SearchAdapter searchAdapter;
    private SearchView searchView;
    private TextView clearAllRecents;
    private ImageView startOcrBtn;

    // Data Variables
    private ArrayList<String> recentSearches = new ArrayList<String>();
    private Boolean isSearchShown = false;
    Gson gson = new Gson();

    // Initialisation of Firebase Cloud Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.aSConstrainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bottom Navigation View
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.search);
        bottomNavigationView.setOnApplyWindowInsetsListener(null);
        bottomNavigationView.setPadding(0,0,0,0);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.home) {
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.search) {
                    return true;
                } else if (itemId == R.id.flashlets) {
                    startActivity(new Intent(getApplicationContext(), FlashletList.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.stats) {
                    startActivity(new Intent(getApplicationContext(), StatisticsActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                }
                return false;
            }
        });

        // Initialise SQLite Database
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(SearchActivity.this);
        SQLiteRecentSearchesManager localSearchesDB = SQLiteRecentSearchesManager.instanceOfDatabase(SearchActivity.this);

        // Hide Search Result Container
        recentsListContainer = findViewById(R.id.aSRecentsListContainer);
        searchResultTabs = findViewById(R.id.aSResultsTabBar);
        searchResultViewPager = findViewById(R.id.aSResultsViewPager);
        recentsContainer = findViewById(R.id.aSRecentsRecyclerView);

        // Bind the RecyclerView
        searchesAdapter = new RecentSearchesAdapter(SearchActivity.this, recentSearches, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(SearchActivity.this);
        recentsContainer.setLayoutManager(layoutManager);
        recentsContainer.setItemAnimator(new DefaultItemAnimator());
        recentsContainer.setAdapter(searchesAdapter);

        // Display list of Recent Searches or container showing No Recent Searches
        onResume();

        // Handle Search View Searches
        searchView = findViewById(R.id.aSSearchField);
        searchResultTabs = findViewById(R.id.aSResultsTabBar);
        searchResultViewPager = findViewById(R.id.aSResultsViewPager);
        /// Listen to 'Focus' on the SearchView (when it is tapped)
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                /*
                * If the Search Results Container (Tab and ViewPager) is Visible (isSearchShown == true) to the user,
                * when the SearchView is focused, display the recent searches. Else, display the Search Results.
                * If the Search Results Container is Not Visible, do nothing (display the recent searches at all times).
                * */

                if (isSearchShown) {
                    if (hasFocus) {
                        searchResultTabs.setVisibility(View.GONE);
                        searchResultViewPager.setVisibility(View.GONE);
                        recentsListContainer.setVisibility(View.VISIBLE);
                    } else {
                        searchResultTabs.setVisibility(View.VISIBLE);
                        searchResultViewPager.setVisibility(View.VISIBLE);
                        recentsListContainer.setVisibility(View.GONE);
                    }
                }
            }
        });
        /// Listen to Text Changes in the Search View
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Add the Search Query to the local SQLite database and update the Recent Searches list
                if (!localSearchesDB.getSearchQueries().contains(query)) {
                    localSearchesDB.addSearchQueries(query, System.currentTimeMillis() / 1000L);
                } else {
                    localSearchesDB.updateSearchQueryTimestamp(query, System.currentTimeMillis() / 1000L);
                }
                onResume();

                // Callback Function to retrieve the Search Results
                retrieveSearchResults(query, localDB, new OnSearchEventListener() {
                    @Override
                    public void onSearchResult(SearchResult searchResult) {
                        searchResultTabs.setVisibility(View.VISIBLE);
                        searchResultViewPager.setVisibility(View.VISIBLE);
                        recentsListContainer.setVisibility(View.GONE);
                        isSearchShown = true;

                        FragmentManager fragmentManager = getSupportFragmentManager();
                        searchAdapter = new SearchAdapter(fragmentManager, getLifecycle(), searchResult);
                        searchResultViewPager.setAdapter(searchAdapter);
                    }

                    @Override
                    public void onError(Exception err) {
                        Toast.makeText(getApplicationContext(), "Error Searching :(, try again later.", Toast.LENGTH_LONG).show();
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Handle Clear All Recently Searched
        clearAllRecents = findViewById(R.id.aSClearRecentsTxt);
        clearAllRecents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localSearchesDB.dropAllSearchQuery();
                onResume();
            }
        });

        // Handle Search Result Pages
        searchResultTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                searchResultViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        searchResultViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                searchResultTabs.selectTab(searchResultTabs.getTabAt(position));
            }
        });

        // Handle OCR Button onClick
        startOcrBtn = findViewById(R.id.aSOCRBtn);
        startOcrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendToOCRActivity = new Intent(SearchActivity.this, OCRActivity.class);
                ocrActivityResultLauncher.launch(sendToOCRActivity);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Update Recent RecyclerView with Items
        SQLiteRecentSearchesManager localSearchesDB = SQLiteRecentSearchesManager.instanceOfDatabase(SearchActivity.this);
        recentSearches = localSearchesDB.getSearchQueries();

        noRecentsContainer = findViewById(R.id.aSNoRecentsList);
        recentsContainer = findViewById(R.id.aSRecentsRecyclerView);

        searchesAdapter.updateAdapterData(recentSearches);

        if (recentSearches.isEmpty()) {
            noRecentsContainer.setVisibility(View.VISIBLE);
            recentsContainer.setVisibility(View.GONE);
        } else {
            recentsContainer.setVisibility(View.VISIBLE);
            noRecentsContainer.setVisibility(View.GONE);
        }
    }

    // Handle Search Results
    protected void retrieveSearchResults(String searchQuery, SQLiteManager localDB, OnSearchEventListener callback) {
        // Get Current User
        User currentLoggedInUser = localDB.getUser().getUser();

        // Search database with SearchQuery
        CollectionReference flashletColRef = db.collection("flashlets");
        CollectionReference usersColRef = db.collection("users");

        SearchResult searchResult = new SearchResult();

        // Query the Cloud Firestore Database to search for Flashlet titles and User usernames similar to the searchQuery
        // NOTE: searchQuery is casted to lower case to ensure for searches to be non case-sensitive
        flashletColRef
                .whereGreaterThanOrEqualTo("insensitiveTitle", searchQuery.toLowerCase())
                .whereLessThanOrEqualTo("insensitiveTitle", searchQuery.toLowerCase() + "\uf8ff")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Convert the Flashlets from JSON to a Flashlet Object, and if it's public, add it to the list
                        ArrayList<Flashlet> flashletList = new ArrayList<Flashlet>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String flashletJson = gson.toJson(document.getData());
                            Flashlet flashlet = gson.fromJson(flashletJson, Flashlet.class);
                            if (flashlet.getIsPublic()) {
                                flashletList.add(flashlet);
                            }
                        }

                        // Retrieve Users with Usernames similar to the searchQuery
                        usersColRef
                                .whereGreaterThanOrEqualTo("insensitiveUsername", searchQuery.toLowerCase())
                                .whereLessThanOrEqualTo("insensitiveUsername", searchQuery.toLowerCase() + "\uf8ff")
                                .get().addOnCompleteListener(userTask -> {
                                    if (userTask.isSuccessful()) {
                                        // Convert the Users from JSON to a User Object, only if the User is not the currently logged in user
                                        ArrayList<User> usersList = new ArrayList<User>();
                                        for (QueryDocumentSnapshot document : userTask.getResult()) {
                                            String userJson = gson.toJson(document.getData());
                                            User user = gson.fromJson(userJson, User.class);
                                            if (!Objects.equals(user.getId(), currentLoggedInUser.getId())) {
                                                usersList.add(user);
                                            }
                                        }
                                        searchResult.setUsers(usersList);

                                        // If there were any Flashlets found, get the Username of the Owner
                                        if (!flashletList.isEmpty()) {
                                            usersColRef
                                                    .whereIn("id", flashletList.stream().map(flashlet -> flashlet.getCreatorID().get(0)).collect(Collectors.toList()))
                                                    .get().addOnCompleteListener(ownerTask -> {
                                                        if (ownerTask.isSuccessful()) {
                                                            Map<String, User> userMap = new HashMap<>();
                                                            for (QueryDocumentSnapshot document : ownerTask.getResult()) {
                                                                String userJson = gson.toJson(document.getData());
                                                                User user = gson.fromJson(userJson, User.class);
                                                                userMap.put(user.getId(), user);
                                                            }

                                                            ArrayList<FlashletWithCreator> flashletsWithCreators = new ArrayList<>();
                                                            for (Flashlet flashlet : flashletList) {
                                                                User owner = userMap.get(flashlet.getCreatorID().get(0));

                                                                if (owner != null) {
                                                                    flashletsWithCreators.add(new FlashletWithCreator(flashlet, owner));
                                                                }
                                                            }

                                                            searchResult.setFlashlets(flashletsWithCreators);
                                                            callback.onSearchResult(searchResult);
                                                        } else {
                                                            callback.onError(ownerTask.getException());
                                                        }
                                                    });
                                        }

                                        callback.onSearchResult(searchResult);
                                    } else {
                                        callback.onError(userTask.getException());
                                    }
                                });
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    // When a item on the Recent Searches RecyclerView is clicked, search it using the SearchView
    @Override
    public void onItemClick(int position) {
        searchView.setQuery(recentSearches.get(position), true);
    }

    @Override
    public void handleChange(Boolean isEmpty) {
        if (isEmpty) {
            noRecentsContainer.setVisibility(View.VISIBLE);
            recentsContainer.setVisibility(View.GONE);
        } else {
            onResume();
        }
    }

    // Handle OCR Result from OCRActivity
    ActivityResultLauncher<Intent> ocrActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        String ocrResult = data.getStringExtra("result");
                        searchView.setQuery(ocrResult, true);
                    }
                }
            }
    );
}