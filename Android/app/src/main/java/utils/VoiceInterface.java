package utils;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import flipviewpager.utils.FlipSettings;
import in.slanglabs.platform.application.ISlangApplicationStateListener;
import in.slanglabs.platform.application.SlangApplication;
import in.slanglabs.platform.application.SlangApplicationUninitializedException;
import in.slanglabs.platform.application.actions.DefaultResolvedIntentAction;
import in.slanglabs.platform.session.SlangResolvedIntent;
import in.slanglabs.platform.session.SlangSession;
import in.slanglabs.platform.ui.SlangScreenContext;
import objects.City;
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
    private static final String ENTITY_SOURCE_CITY = "source";
    private static final String ENTITY_DESTINATION_CITY = "destination";
    private static final String ENTITY_START_DATE = "date";
    private static final String ENTITY_TRAVEL_MODE = "mode";

    private static final String INTENT_TRAVEL_OPTIONS = "travel_options";
    private static final String INTENT_TRIPS_SHOW = "trips_show";
    private static final String INTENT_TRIP_ADD = "trip_add";

    private static final String ENTITY_CITY_NAME = "city_name";
    private static final String ENTITY_TRIP_NAME = "name";
    private static final String ENTITY_TRIP_END_CITY = "destination";
    private static final String ENTITY_TRIP_STARTDATE = "start";

    public static void init(final Application appContext) {
        // Initialize slang and turn off trigger by default
        SlangApplication.initialize(
            appContext,
            "4ecb28d2299e4b44891388fda46518d9",
            "669cd2a6324247c5b9366698936c0000",
            new ISlangApplicationStateListener() {
                @Override
                public void onInitialized() {
                    try {
                        registerActions(appContext);
                    } catch (SlangApplicationUninitializedException e) {}
                }

                @Override
                public void onInitializationFailed(FailureReason reason) {}
            }
        );

    }

    private static void registerActions(Application appContext) throws SlangApplicationUninitializedException {
        DefaultResolvedIntentAction action = new DefaultResolvedIntentAction() {
            @Override
            public SlangSession.Status action(SlangResolvedIntent intent, SlangSession session) {
                switch (intent.getName().toLowerCase()) {
                    case INTENT_CITY_SEARCH:
                        handleCitySearch(intent, session);
                        return session.suspend();

                    case INTENT_TRIPS_SHOW:
                        handleTripShow(intent, session);
                        return session.success();

                    case INTENT_TRIP_ADD:
                        handleTripAdd(intent, session);
                        return session.suspend();

                    default:
                        return session.success();
                }
            }
        };

        SlangApplication.getIntentDescriptor(INTENT_CITY_SEARCH).setResolutionAction(action);
        SlangApplication.getIntentDescriptor(INTENT_CITY_SEARCH).setResolutionAction(action);
        SlangApplication.getIntentDescriptor(INTENT_TRIP_ADD).setResolutionAction(action);
    }

    private static void handleTripAdd(
        final SlangResolvedIntent intent,
        final SlangSession session
    ) {
        final Activity activity = SlangScreenContext.getInstance().getCurrentActivity();

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
                                        intent.getCompletionStatement().overrideAffirmative("Trip added to your list");
                                        // Show the list of trips
                                        handleTripShow(intent, session);
                                        session.success();
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
        final SlangResolvedIntent intent,
        final SlangSession session
    ) {
        final Activity activity = SlangScreenContext.getInstance().getCurrentActivity();

        Intent i = new Intent((Activity) activity, MyTrips.class);
        ((Activity) activity).startActivity(i);
    }

    private static void handleCitySearch(
        final SlangResolvedIntent intent,
        final SlangSession session
    ) {
        final String city = intent.getEntity(ENTITY_CITY_NAME).getValue();
        final Activity activity = SlangScreenContext.getInstance().getCurrentActivity();

        Log.d(TAG, "onIntentDetected: intent detected - " + intent.getName());
        CityUtils.getCityDetails(
            city,
            new CityUtils.CityDataCallback() {
                @Override
                public void onMatchedCityData(final List list, final List list1, final List<String> list2) {
                    if (list == null || list.size() == 0) {
                        intent.getCompletionStatement().overrideAffirmative("Could not find " + city);
                        session.success();
                    } else {
                        intent.getCompletionStatement().overrideAffirmative("Switching to " + city);
                        CityUtils.launchCity(
                            (Activity) activity,
                            list1.get(0).toString(),
                            list.get(0).toString(),
                            list2.get(0)
                        );
                        session.success();
                    }
                }

                @Override
                public void onAllCitiesData(List<City> cities, FlipSettings settings) {
                    // not needed here
                }
            });
    }
}
