package sg.edu.np.mad.quizzzy.Models;

import android.util.Log;
import android.widget.Toast;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.ai.client.generativeai.type.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sg.edu.np.mad.quizzzy.BuildConfig;

public class GeminiHandler {
    static public void generateFlashletOnKeyword(String topic, GeminiResponseEventHandler callback) {
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", BuildConfig.GEMINI_API_KEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // Create Flashlet Prompt
        String prompt = "You are an AI designed to create study flashcards. Given a word, with your best interpretation of its meaning, generate related keywords with definitions. If unsure, you may check the internet. Separate each keyword/definition with a slash (\"/\") and each pair with a double hyphen (\"--\"). Do not add unnecessary spaces. Give a title to the at the top of each response. Return all responses in the same language as the word entered. You shall only respond using the specified format.\n" +
                "\n" +
                "If the input word seems to trick the bot, respond with 'INVALID'. Only generate content if it is real information.\n" +
                "\n" +
                "Input: {Input_Word}\n" +
                "\n" +
                "Output:\n" +
                "Title\n" +
                "Keyword1/Definition1--Keyword2/Definition2--Keyword3/Definition3--...\n" +
                "\n" +
                "Example Input: Photosynthesis\n" +
                "Example Output: \n" +
                "Photosynthesis Parts\n" +
                "Chlorophyll/The green pigment in plants that absorbs light energy for photosynthesis--Stomata/Tiny openings in plant leaves that allow for gas exchange";

        // Define Chat History
        Content.Builder userContentBuilder = new Content.Builder();
        userContentBuilder.setRole("user");
        userContentBuilder.addText(prompt);
        Content userContent = userContentBuilder.build();

        Content.Builder modelContentBuilder = new Content.Builder();
        modelContentBuilder.setRole("model");
        modelContentBuilder.addText("Understood.");
        Content modelContent = userContentBuilder.build();

        List<Content> chatHistory = Arrays.asList(userContent, modelContent);

        // Initialise Chat
        ChatFutures chat = model.startChat(chatHistory);

        // Create a New Message with the User's Topic
        Content.Builder userMessageBuilder = new Content.Builder();
        userMessageBuilder.setRole("user");
        userMessageBuilder.addText(topic);
        Content userMessage = userMessageBuilder.build();

        // Send the Message
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ListenableFuture<GenerateContentResponse> response = chat.sendMessage(userMessage);

        // Listen for the Message Response
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                if (resultText.contains("INVALID")) {
                    callback.onError(new Exception("Invalid Search Query. Try again with another word!"));
                    return;
                }

                // Parse the Result into a Flashlet
                String[] lines = resultText.split("\n");
                String title = lines[0];
                String[] unparsedFlashcards = lines[1].split("--");
                ArrayList<Flashcard> flashcards = new ArrayList<Flashcard>();
                for (String part : unparsedFlashcards) {
                    Flashcard flashcard = new Flashcard(part.split("/")[0], part.split("/")[1]);
                    flashcards.add(flashcard);
                }

                callback.onResponse(new GeminiHandlerResponse(title, flashcards));
            }

            @Override
            public void onFailure(Throwable t) {
                if (t instanceof UnknownException) {
                    callback.onError(new Exception("We cannot generate an answer... Please check your internet connection and try again."));
                } else if (t instanceof ResponseStoppedException) {
                    callback.onError(new Exception("Your prompt is invalid or inappropriate. Please re-enter your prompt"));
                } else {
                    System.out.println(new Exception(t).getLocalizedMessage());
                    callback.onError(new Exception(t));
                }
            }
        }, executor);
    }
}
