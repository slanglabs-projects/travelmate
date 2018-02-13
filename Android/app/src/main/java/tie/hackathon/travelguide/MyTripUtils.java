package tie.hackathon.travelguide;

import android.app.Activity;
import android.app.Dialog;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import flipviewpager.utils.FlipSettings;
import objects.City;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utils.Constants;

import static utils.Constants.userid;

/**
 * TODO: Add a class header comment!
 */

public class MyTripUtils {
    public interface TripUtilListener {
        void onComplete();
    }

    /**
     * Calls API to add  new trip
     */
    public static void addTripWithName(
        final Activity activity,
        final String tripname,
        final Date startdate,
        String cityname,
        final TripUtilListener listener
    ) {
        // First convert city name into city id
        CityUtils.getCityDetails(cityname, new CityUtils.CityDataCallback() {
            @Override
            public void onMatchedCityData(final List list, final List list1, final List<String> list2) {
                String cityid = list1.get(0).toString();

                addTripWithId(activity, tripname, startdate, cityid, listener);
            }

            @Override
            public void onAllCitiesData(List<City> cities, FlipSettings settings) {
                // not needed here
            }
        });
    }

    public static void addTripWithId(
        final Activity activity,
        final String tripname,
        final Date startdate,
        String cityid,
        final TripUtilListener listener
    ) {
        String userid = PreferenceManager.getDefaultSharedPreferences(activity).getString(Constants.USER_ID, "1");

        String uri = Constants.apilink + "trip/add-trip.php?user=" + userid +
            "&title=" + tripname +
            "&start_time=" + startdate +
            "&city=" + cityid;

        Log.e("CALLING : ", uri);

        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        Request request = new Request.Builder()
            .url(uri)
            .build();
        //Setup callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Request Failed", "Message : " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                listener.onComplete();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("RESPONSE : ", "Done");
                        Toast.makeText(activity, "Trip added", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}