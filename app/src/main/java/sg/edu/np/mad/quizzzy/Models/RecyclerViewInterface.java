package sg.edu.np.mad.quizzzy.Models;

/**
* <b>RecyclerViewInterface</b> is a custom Java Interface that is used to handle Item Click Events in the RecyclerView.
 * It provides a method that can be triggered when the user interacts with a item in the RecyclerView.
* */
public interface RecyclerViewInterface {
    void onItemClick(int position);
}
