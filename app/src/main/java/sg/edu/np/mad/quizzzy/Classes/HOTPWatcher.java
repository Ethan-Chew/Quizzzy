package sg.edu.np.mad.quizzzy.Classes;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

public class HOTPWatcher implements TextWatcher {

    private View currentView;
    private View nextView;

    public HOTPWatcher(View currentView, View nextView) {
        this.currentView = currentView;
        this.nextView = nextView;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() == 1 && nextView != null) {
            nextView.requestFocus(); // Move focus to next EditText
        } else if (s.length() == 0 && currentView != null) {
            currentView.requestFocus(); // Stay on the current EditText
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
