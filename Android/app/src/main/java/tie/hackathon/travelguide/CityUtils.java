package tie.hackathon.travelguide;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import flipviewpager.utils.FlipSettings;
import objects.City;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tie.hackathon.travelguide.destinations.description.FinalCityInfo;
import utils.Constants;

/**
 * TODO: Add a class header comment!
 */

public class CityUtils {
    public interface CityDataCallback {
        void onMatchedCityData(List list, List list1, List<String> list2);

        void onAllCitiesData(List<City> cities, FlipSettings settings);
    }

    public static void getCityDetails(
        final String cityName,
        final CityDataCallback listener
    ) {
        // to fetch city names
        String uri = cityName == null ?
            Constants.slang_apilink + "all-cities.php" :
            Constants.slang_apilink + "city/autocomplete.php?search=" + cityName.trim();
        Log.e("executing", uri + " ");

        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        final Request request = new Request.Builder()
            .url(uri)
            .build();
        //Setup callback
        client
            .newCall(request)
            .enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("Request Failed", "Message : " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    try {
                        if (cityName != null) {
                            matchedCityResponse(response, listener);
                        } else {
                            allCityResponse(response, listener);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("erro", e.getMessage() + " ");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
    }

    public static void launchCity(Activity activity, String cityId, String cityName, String cityImage) {
        Intent i = new Intent(activity, FinalCityInfo.class);
        i.putExtra("id_", cityId);
        i.putExtra("name_", cityName);
        i.putExtra("image_", cityImage);
        activity.startActivity(i);
    }

    private static void matchedCityResponse(Response response, CityDataCallback listener) throws JSONException, IOException{
        JSONArray arr;
        final ArrayList list, list1;
        final ArrayList<String> list2;

        arr = new JSONArray(response.body().string());
        Log.e("erro", arr + " ");

        list = new ArrayList<>();
        list1 = new ArrayList<>();
        list2 = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            try {
                list.add(arr.getJSONObject(i).getString("name"));
                list1.add(arr.getJSONObject(i).getString("id"));
                list2.add(arr.getJSONObject(i).optString("image", "http://i.ndtvimg.com/i/2015-12/delhi-pollution-traffic-cars-afp_650x400_71451565121.jpg"));
                Log.e("adding", "aff");

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("error ", " " + e.getMessage());
            }
        }

        if (listener != null) {
            listener.onMatchedCityData(list, list1, list2);
        }
    }

    private static void allCityResponse(Response response, CityDataCallback listener) throws JSONException, IOException {
        String responseStr = response.body().string();
        JSONArray ar = new JSONArray();

        if (responseStr != null && responseStr.length() > 0) {
            JSONObject ob = new JSONObject(responseStr);
            ar = ob.getJSONArray("cities");
        }

        FlipSettings settings = new FlipSettings.Builder().defaultPage().build();
        List<City> cities = new ArrayList<>();
        for (int i = 0; i < ar.length(); i++) {

            double color = Math.random();
            int c = (int) (color * 100) % 8;

            int colo;
            switch (c) {
                case 0:
                    colo = R.color.sienna;
                    break;
                case 1:
                    colo = R.color.saffron;
                    break;
                case 2:
                    colo = R.color.green;
                    break;
                case 3:
                    colo = R.color.pink;
                    break;
                case 4:
                    colo = R.color.orange;
                    break;
                case 5:
                    colo = R.color.saffron;
                    break;
                case 6:
                    colo = R.color.purple;
                    break;
                case 7:
                    colo = R.color.blue;
                    break;
                default:
                    colo = R.color.blue;
                    break;
            }

            cities.add(new City(
                ar.getJSONObject(i).getString("id"),
                ar.getJSONObject(i).optString("image", "yolo"),
                ar.getJSONObject(i).getString("name"),
                colo,
                ar.getJSONObject(i).getString("lat"),
                ar.getJSONObject(i).getString("lng"),
                "Know More", "View on Map", "Fun Facts", "View Website"));
        }

        if (listener != null) {
            listener.onAllCitiesData(cities, settings);
        }
    }
}
