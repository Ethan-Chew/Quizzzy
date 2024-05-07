package sg.edu.np.mad.quizzzy.Models;

public class Flashcard {
    String keyword;
    String definition;

    // Setters
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    // Getters
    public String getKeyword() { return this.keyword; }
    public String getDefinition() { return this.definition; }

    // Constructor
    public Flashcard(String keyword, String definition) {
        this.keyword = keyword;
        this.definition = definition;
    }
}
