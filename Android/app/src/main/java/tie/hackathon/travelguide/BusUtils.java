package tie.hackathon.travelguide;

import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import flipviewpager.utils.FlipSettings;
import objects.City;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utils.Constants;

/**
 * TODO: Add a class header comment!
 */

public class BusUtils {
    public interface BusDataCallback {
        void onBusData(JSONArray YTFeedItems);
    }

    public static void getBuslist(
        String source,
        String dest,
        String dates,
        final BusDataCallback callback
    ) {
        String uri = Constants.apilink + "bus-booking.php?src=" +
            source +
            "&dest=" +
            dest +
            "&date=" +
            dates;

        Log.e("CALLING : ", uri);

        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        Request request = new Request.Builder()
            .url(uri)
            .build();
        //Setup callback
        //Setup callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Request Failed", "Message : " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String res = response.body().string();
                Log.e("RESPONSE : ", "Done");
                try {
                    JSONObject YTFeed = new JSONObject(String.valueOf(res));
                    JSONArray YTFeedItems = YTFeed.getJSONArray("results");
                    Log.e("response", YTFeedItems + " ");

                    if (callback != null) {
                        callback.onBusData(YTFeedItems);
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
}
