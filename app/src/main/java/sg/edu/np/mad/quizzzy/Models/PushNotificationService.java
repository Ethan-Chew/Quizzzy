package sg.edu.np.mad.quizzzy.Models;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import sg.edu.np.mad.quizzzy.R;

/**
 * <b>PushNotificationService</b> is the manager which handles the receiving and sending of Firebase Cloud Messaging (FCM) messages.<br>
 * This service allows for the feature where Flashlet Owners would get a notification when their Flashlet has been cloned.
 * */
public class PushNotificationService extends FirebaseMessagingService {
    /**
     * Creates a Notification when a message is received using FCM
     * When a message is sent to the User ID of the current Logged In User, this function is called
     * */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        String title = message.getData().get("title");
        String body = message.getData().get("body");
        final String CHANNEL_ID = "HEADS_UP_NOTIFICATION";
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Head Up Notification",
                NotificationManager.IMPORTANCE_HIGH
        );
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(this).notify(1, notification.build());
        super.onMessageReceived(message);
    }

    /**
     * Subscribes the current device to a topic (the user's User ID) in FCM
     * Allows the device to receive notifications intended for the given user ID.
     *
     * @param userId The user ID to which the device will subscribe in FCM.
     */
    public void subscribeToUserIDTopic(String userId) {
        FirebaseMessaging.getInstance().subscribeToTopic(userId)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Firebase FCM Subscribed", "Successfully subscribed to Firebase FCM with UserID");
                        } else {
                            Log.d("Firebase FCM Subscribe Fail", "Failed to subscribed to Firebase FCM with UserID");
                        }
                    }
                });
    }

    public void unsubscribeFromUserIDTopic(String userId) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(userId);
    }

    /**
     * Sends a push notification to a specified user indicating that their Flashlet has been cloned.
     * This is achieved by making an HTTP POST request to our custom endpoint.
     *
     * @param receiverUserId The user ID of the original Flashlet Owner
     * @param flashletName The name of the Flashlet that was cloned.
     */
    public void sendFlashletCloneMessage(String receiverUserId, String flashletName) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                final OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(120, TimeUnit.SECONDS) // Connection timeout
                        .writeTimeout(120, TimeUnit.SECONDS)   // Write timeout
                        .build();

                String jsonData = "{\"topic\":\"" + receiverUserId + "\",\"message\":\"" + "Someone cloned your Flashlet: " + flashletName + "\"}";
                RequestBody requestBody = RequestBody.create(jsonData, MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url("https://quizzzyfcmbackend.onrender.com/firebase/notification")
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
