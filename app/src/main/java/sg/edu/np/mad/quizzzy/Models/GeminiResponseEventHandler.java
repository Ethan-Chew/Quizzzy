package sg.edu.np.mad.quizzzy.Models;

public interface GeminiResponseEventHandler {
    void onResponse(GeminiHandlerResponse handlerResponse);

    void onError(Exception err);
}
