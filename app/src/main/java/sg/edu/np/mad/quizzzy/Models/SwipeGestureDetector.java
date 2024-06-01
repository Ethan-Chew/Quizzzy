package sg.edu.np.mad.quizzzy.Models;

import android.view.GestureDetector;
import android.view.MotionEvent;

// Handles Swipe Animations for Flashcards
public abstract class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float diffX = e2.getX() - e1.getX();
        float diffY = e2.getY() - e1.getY();
        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    return onSwipeRight();
                } else {
                    return onSwipeLeft();
                }
            }
        }
        return false;
    }

    public abstract boolean onSwipeRight();

    public abstract boolean onSwipeLeft();
}
