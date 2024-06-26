package sg.edu.np.mad.quizzzy.Widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.google.gson.Gson;

import sg.edu.np.mad.quizzzy.Flashlets.FlashletDetail;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.R;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link PersonalFlashletWidgetConfigureActivity PersonalFlashletWidgetConfigureActivity}
 */
public class PersonalFlashletWidget extends AppWidgetProvider {
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Gson gson = new Gson();
        String[] flashletInfo = PersonalFlashletWidgetConfigureActivity.loadFlashletPref(context, appWidgetId);
        Flashlet userFlashlet;

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.personal_flashlet_widget, appWidgetId);
        if (flashletInfo == null) {
            views.setTextViewText(R.id.pFWTitle, "No Flashlet");
        } else {
            userFlashlet = gson.fromJson(flashletInfo[0], Flashlet.class);
            views.setTextViewText(R.id.pFWTitle, userFlashlet.getTitle());
        }

        Intent intent = new Intent(context, FlashletDetail.class);
        intent.putExtra("flashletJSON", flashletInfo[0]);
        intent.putExtra("userId", flashletInfo[1]);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.pFWContainer, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            PersonalFlashletWidgetConfigureActivity.deleteFlashletPref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}