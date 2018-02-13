package utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.slanglabs.slang.Slang;
import com.slanglabs.slang.action.SlangAction;
import com.slanglabs.slang.action.SlangIntent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import flipviewpager.utils.FlipSettings;
import objects.City;
import tie.hackathon.travelguide.AddNewTrip;
import tie.hackathon.travelguide.CityUtils;
import tie.hackathon.travelguide.MyTripUtils;
import tie.hackathon.travelguide.MyTrips;

/**
 * Abstraction for adding voice interface
 */

public class VoiceInterface {
    private static final String TAG = "VoiceInterface";

    private static final String INTENT_CITY_SEARCH = "navigate_to_city";
    private static final String INTENT_TRAVEL_MODE = "travel_with_mode";
    private static final String INTENT_TRAVEL_OPTIONS = "travel_options";
    private static final String INTENT_TRIPS_SHOW = "trips_show";
    private static final String INTENT_TRIP_ADD = "trip_add";

    private static final String ENTITY_CITY_NAME = "city_name";
    private static final String ENTITY_TRIP_NAME = "name";
    private static final String ENTITY_TRIP_END_CITY = "destination";
    private static final String ENTITY_TRIP_STARTDATE = "start";

    public static void init(Application appContext) {
        // Initialize slang and turn off trigger by default
        Slang
            .init(appContext)
            .appKey("ab989ba6a5b54314afb5b2afc9bd6fe4")
            .ui()
            .trigger()
            .hide();

        registerActions();
    }

    public static void setHelpMessage(String helpMessage) {
        Slang.ui().surface().setHelpMessage(helpMessage);
    }

    public static void setMainHelpMessage() {
        setHelpMessage("How can we help you?\nTry saying \n\"Show me Bangalore\"\n\"Show my recent trips\"\n\"Cancel\" (or) \"Never mind\"");
    }

    private static void registerActions() {
        Slang
            .action()
            .fallback(new SlangAction.ActionCallback() {
                @Override
                public boolean onEntityProcessing(
                    @NonNull Activity activity, SlangIntent intent) {
                    // Use this to pre-process the entities and modify them or fill in missing
                    // entities

                    return true;  // return false to stop processing the intent
                }

                @Override
                public void onIntentDetected(
                    @NonNull final Activity activity,
                    final SlangIntent intent,
                    final SlangAction.IntentProgressListener listener) {

                    switch (intent.getIntentString().toLowerCase()) {
                        case INTENT_CITY_SEARCH:
                            handleCitySearch(activity, intent, listener);
                            break;

                        case INTENT_TRAVEL_MODE:
                            handleTravelMode(activity, intent, listener);
                            break;
                        case INTENT_TRAVEL_OPTIONS:
                            handleTravelOptions(activity, intent, listener);
                            break;
                        case INTENT_TRIPS_SHOW:
                            handleTripShow(activity, intent, listener);
                            break;
                        case INTENT_TRIP_ADD:
                            handleTripAdd(activity, intent, listener);
                            break;
                    }
                }
            });
    }

    private static void handleTravelMode(
        @NonNull final Activity activity,
        final SlangIntent intent,
        final SlangAction.IntentProgressListener listener
    ) {

    }

    private static void handleTravelOptions(
        @NonNull final Activity activity,
        final SlangIntent intent,
        final SlangAction.IntentProgressListener listener
    ) {

    }

    private static void handleTripAdd(
        @NonNull final Activity activity,
        final SlangIntent intent,
        final SlangAction.IntentProgressListener listener
    ) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String strDate = intent.getEntity(ENTITY_TRIP_STARTDATE).getValue();
                    Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(strDate);
                    String cityName = intent.getEntity(ENTITY_TRIP_END_CITY).getValue();

                    MyTripUtils.addTripWithName(
                        activity,
                        "Trip to " + cityName,
                        startDate,
                        cityName,
                        new MyTripUtils.TripUtilListener() {
                            @Override
                            public void onComplete() {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e("RESPONSE : ", "Done");
                                        listener.intentCompleted(intent, "Trip added to your list", false);
                                        // Show the list of trips
                                        handleTripShow(activity, intent, listener);
                                    }
                                });
                            }
                        }
                    );
                } catch (Exception e) {
                    Log.e(TAG, "error parsing date - " + e.getLocalizedMessage());
                }
            }
        });
    }

    private static void handleTripShow(
        @NonNull final Context context,
        final SlangIntent intent,
        final SlangAction.IntentProgressListener listener
    ) {
        Intent i = new Intent((Activity) context, MyTrips.class);
        ((Activity) context).startActivity(i);
        listener.intentCompleted(intent, "Showing your past trips", true);
    }

    private static void handleCitySearch(
        @NonNull final Context context,
        final SlangIntent intent,
        final SlangAction.IntentProgressListener listener
    ) {
        final String city = intent.getEntity(ENTITY_CITY_NAME).getValue();

        Log.d(TAG, "onIntentDetected: intent detected - " + intent.getIntentString());
        CityUtils.getCityDetails(
            city,
            new CityUtils.CityDataCallback() {
                @Override
                public void onMatchedCityData(final List list, final List list1, final List<String> list2) {
                    if (list == null || list.size() == 0) {
                        listener.intentCompleted(intent, "Could not find " + city, true);
                    } else {
                        listener.intentCompleted(intent, "Switching to " + city, true);
                        CityUtils.launchCity(
                            (Activity) context,
                            list1.get(0).toString(),
                            list.get(0).toString(),
                            list2.get(0)
                        );
                    }
                }

                @Override
                public void onAllCitiesData(List<City> cities, FlipSettings settings) {
                    // not needed here
                }
            });
    }
}
