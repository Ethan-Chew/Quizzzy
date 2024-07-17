package sg.edu.np.mad.quizzzy.Models;

import sg.edu.np.mad.quizzzy.Models.Flashlet;

public interface GeminiResponseEventHandler {
    void onResponse(GeminiHandlerResponse handlerResponse);

    void onError(Exception err);
}
