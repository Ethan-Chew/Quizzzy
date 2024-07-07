package sg.edu.np.mad.quizzzy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Flashlets.Recycler.FlashletListViewHolder;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.RecyclerViewInterface;

public class ProfileFlashletAdapter extends RecyclerView.Adapter<ProfileFlashletViewHolder> {
    Gson gson = new Gson();

    // Initialisation of Firebase Cloud Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final RecyclerViewInterface recyclerViewInterface;
    private final ArrayList<Flashlet> flashlets;

    public ProfileFlashletAdapter(ArrayList<Flashlet> flashlets, RecyclerViewInterface recyclerViewInterface) {
        this.recyclerViewInterface = recyclerViewInterface;
        this.flashlets = flashlets;
    }

    @NonNull
    @Override
    public ProfileFlashletViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flashlet_list_item, parent, false);

        return new ProfileFlashletViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileFlashletViewHolder holder, int position) {
        Flashlet listItem = flashlets.get(holder.getAdapterPosition());

        // Set Text of UI Elements
        holder.titleLabel.setText(listItem.getTitle());

        String flashcardCountTxt = listItem.getFlashcards().size() + " Keyword" + (listItem.getFlashcards().size() == 1 ? "" : "s");
        holder.flashcardCountLabel.setText(flashcardCountTxt);
    }

    @Override
    public int getItemCount() {
        return flashlets.size();
    }
}
