package sg.edu.np.mad.quizzzy.Search;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.HomeActivity;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.FlashletWithUsername;
import sg.edu.np.mad.quizzzy.Models.RecyclerViewInterface;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.SQLiteRecentSearchesManager;
import sg.edu.np.mad.quizzzy.Models.SearchResult;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.R;
import sg.edu.np.mad.quizzzy.Search.Recycler.RecentSearchesAdapter;
import sg.edu.np.mad.quizzzy.StatisticsActivity;

interface OnSearchEventListener {
    void onSearchResult(SearchResult searchResult);
    void onError(Exception err);
}

public class SearchActivity extends AppCompatActivity implements RecyclerViewInterface, RecentSearchesAdapter.OnEmptyListener {

    // Search Result Items
    private TabLayout searchResultTabs;
    private ViewPager2 searchResultViewPager;
    private ScrollView recentsListContainer;
    private LinearLayout noRecentsContainer;
    private RecyclerView recentsContainer;
    private SearchAdapter searchAdapter;
    private SearchView searchView;
    private TextView clearAllRecents;
    private ImageView startOcrBtn;

    // Global Data
    private ArrayList<String> recentSearches = new ArrayList<String>();

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

        /// Hide Search Result Container
        recentsListContainer = findViewById(R.id.aSRecentsListContainer);
        searchResultTabs = findViewById(R.id.aSResultsTabBar);
        searchResultViewPager = findViewById(R.id.aSResultsViewPager);
        searchResultTabs.setVisibility(View.GONE);
        searchResultViewPager.setVisibility(View.GONE);

        /// Display list of Recents or 'No Recent Searches'
        onResume();

        // Handle Search View Searches
        searchView = findViewById(R.id.aSSearchField);
        searchResultTabs = findViewById(R.id.aSResultsTabBar);
        searchResultViewPager = findViewById(R.id.aSResultsViewPager);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!localSearchesDB.getSearchQueries().contains(query)) {
                    localSearchesDB.addSearchQueries(query);
                }
                onResume();

                retrieveSearchResults(query, localDB, new OnSearchEventListener() {
                    @Override
                    public void onSearchResult(SearchResult searchResult) {
                        searchResultTabs.setVisibility(View.VISIBLE);
                        searchResultViewPager.setVisibility(View.VISIBLE);
                        recentsListContainer.setVisibility(View.GONE);

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
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
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
        if (recentSearches.isEmpty()) {
            noRecentsContainer.setVisibility(View.VISIBLE);
            recentsContainer.setVisibility(View.GONE);
        } else {
            noRecentsContainer.setVisibility(View.GONE);
        }

        recentsContainer = findViewById(R.id.aSRecentsRecyclerView);
        RecentSearchesAdapter searchesAdapter = new RecentSearchesAdapter(SearchActivity.this, recentSearches, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(SearchActivity.this);
        recentsContainer.setLayoutManager(layoutManager);
        recentsContainer.setItemAnimator(new DefaultItemAnimator());
        recentsContainer.setAdapter(searchesAdapter);
    }

    // Handle Search Results
    protected void retrieveSearchResults(String searchQuery, SQLiteManager localDB, OnSearchEventListener callback) {
        // Get Current User
        User currentLoggedInUser = localDB.getUser().getUser();

        // Search database with SearchQuery
        CollectionReference flashletColRef = db.collection("flashlets");
        CollectionReference usersColRef = db.collection("users");

        SearchResult searchResult = new SearchResult();

        // Query the Cloud Firestore Database to search for Flashlet titles similar to the searchQuery
        flashletColRef
                .whereGreaterThanOrEqualTo("title", searchQuery)
                .whereLessThanOrEqualTo("title", searchQuery + "\uf8ff")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<String> flashletOwnerIds = new ArrayList<String>();
                            ArrayList<Flashlet> flashletList = new ArrayList<Flashlet>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String flashletJson = gson.toJson(document.getData());
                                Flashlet flashlet = gson.fromJson(flashletJson, Flashlet.class);
                                if (flashlet.getIsPublic()) {
                                    flashletList.add(flashlet);
                                    flashletOwnerIds.add(flashlet.getCreatorID());
                                }
                            }

                            // Query the Database to search for User with Usernames similar to searchQuery
                            usersColRef
                                    .whereGreaterThanOrEqualTo("username", searchQuery)
                                    .whereLessThanOrEqualTo("username", searchQuery + "\uf8ff")
                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                ArrayList<User> usersList = new ArrayList<User>();
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    String userJson = gson.toJson(document.getData());
                                                    User user = gson.fromJson(userJson, User.class);
                                                    if (!Objects.equals(user.getId(), currentLoggedInUser.getId())) {
                                                        usersList.add(user);
                                                    }
                                                }
                                                searchResult.setUsers(usersList);

                                                // If there are Flashlet Search Results, get the Username of the Owners
                                                if (!flashletList.isEmpty()) {
                                                    usersColRef.whereIn("id", flashletOwnerIds).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                ArrayList<User> users = new ArrayList<User>();
                                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                                    String userJson = gson.toJson(document.getData());
                                                                    users.add(gson.fromJson(userJson, User.class));
                                                                }
                                                                // Add User ID and Username to FlashletId
                                                                ArrayList<FlashletWithUsername> flashletWithUsernames = new ArrayList<FlashletWithUsername>();
                                                                for (int i = 0; i < flashletList.size(); i++) {
                                                                    for (int j = 0; j < flashletList.size(); j++) {
                                                                        if (Objects.equals(users.get(j).getId(), flashletList.get(i).getCreatorID())) {
                                                                            flashletWithUsernames.add(new FlashletWithUsername(flashletList.get(i), users.get(j).getUsername(), users.get(j).getId()));
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                                searchResult.setFlashlets(flashletWithUsernames);
                                                                callback.onSearchResult(searchResult);
                                                            } else {
                                                                callback.onError(task.getException());
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    callback.onSearchResult(searchResult);
                                                }
                                            } else {
                                                callback.onError(task.getException());
                                            }
                                        }
                                    });
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    // Handle Recent RecyclerView Item onClick
    @Override
    public void onItemClick(int position) {
        searchView.setQuery(recentSearches.get(position), true);
    }

    // Handle Recent List Empty
    @Override
    public void isRecentsEmpty(Boolean isEmpty) {
        if (isEmpty) {
            noRecentsContainer.setVisibility(View.VISIBLE);
            recentsContainer.setVisibility(View.GONE);
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