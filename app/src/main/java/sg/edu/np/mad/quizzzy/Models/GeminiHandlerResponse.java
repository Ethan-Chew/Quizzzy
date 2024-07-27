package sg.edu.np.mad.quizzzy.Models;

import java.util.ArrayList;

/**
 * <b>GeminiHandlerResponse</b> is used to handle the parsed response from the GeminiHandler.
 * */
public class GeminiHandlerResponse {
    private String title;
    private ArrayList<Flashcard> flashcards;

    public ArrayList<Flashcard> getFlashcards() {
        return flashcards;
    }

    public String getTitle() {
        return title;
    }

    public GeminiHandlerResponse(String title, ArrayList<Flashcard> flashcards) {
        this.title = title;
        this.flashcards = flashcards;
    }
}