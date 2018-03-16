package tie.hackathon.travelguide;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import utils.Constants;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Displays a list of available buses
 */
public class BusList extends AppCompatActivity implements OnDateSetListener, TimePickerDialog.OnTimeSetListener,View.OnClickListener {

    private static final String DATEPICKER_TAG = "datepicker";

    @BindView(R.id.pb)          ProgressBar pb;
    @BindView(R.id.music_list)  ListView    lv;
    @BindView(R.id.seldate)     TextView    selectdate;
    @BindView(R.id.city)        TextView    city;

    private final String DEFAULT_DATE = "28-February-2018";
    private String source;
    private String dest;
    private String dates = DEFAULT_DATE;

    private Handler             mHandler;
    private SharedPreferences   sharedPreferences;
    private DatePickerDialog    datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mHandler = new Handler(Looper.getMainLooper());

        ButterKnife.bind(this);

        getArgs();

        selectdate.setText(dates);
        city.setText(source + " to " + dest);

        getBuslist();
        final Calendar calendar = Calendar.getInstance();
        datePickerDialog = DatePickerDialog.newInstance(this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                isVibrate());

        setTitle("Buses");

        city.setOnClickListener(this);
        selectdate.setOnClickListener(this);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private boolean isVibrate() {
        return false;
    }

    private boolean isCloseOnSingleTapDay() {
        return false;
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        // Set date in format 17-October-2016
        dates = day + "-";

        String monthString;
        switch (month + 1) {
            case 1:
                monthString = "January";
                break;
            case 2:
                monthString = "February";
                break;
            case 3:
                monthString = "March";
                break;
            case 4:
                monthString = "April";
                break;
            case 5:
                monthString = "May";
                break;
            case 6:
                monthString = "June";
                break;
            case 7:
                monthString = "July";
                break;
            case 8:
                monthString = "August";
                break;
            case 9:
                monthString = "September";
                break;
            case 10:
                monthString = "October";
                break;
            case 11:
                monthString = "November";
                break;
            case 12:
                monthString = "December";
                break;
            default:
                monthString = "Invalid month";
                break;
        }

        dates = dates + monthString;
        dates = dates + "-" + year;

        selectdate.setText(dates);
        getBuslist(); //Update bus list
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
    }

    /**
     * Calls API to get bus list
     */
    private void getBuslist() {
        pb.setVisibility(View.VISIBLE);
        BusUtils.getBuslist(source, dest, dates, new BusUtils.BusDataCallback() {
            @Override
            public void onBusData(final JSONArray YTFeedItems) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        pb.setVisibility(View.GONE);
                        lv.setAdapter(new Bus_adapter(BusList.this, YTFeedItems));
                    }
                });
            }
        });
    }


    private void getArgs() {
        Intent intent = getIntent();

        source = intent.getStringExtra(Constants.SOURCE_CITY);
        dest = intent.getStringExtra(Constants.DESTINATION_CITY);
        dates = intent.getStringExtra(Constants.TRAVEL_DATE);

        sharedPreferences   = PreferenceManager.getDefaultSharedPreferences(this);

        if (source == null) {
            source = sharedPreferences.getString(Constants.SOURCE_CITY, "bangalore");
        }

        if (dest == null) {
            dest = sharedPreferences.getString(Constants.DESTINATION_CITY, "chennai");
        }

        if (dates == null) {
            dates = DEFAULT_DATE;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getArgs();
        city.setText(source + " to " + dest);
        getBuslist(); // Update Bus list
    }


    // Sets adapter for bus list
    public class Bus_adapter extends BaseAdapter {

        final Context context;
        final JSONArray FeedItems;
        private LayoutInflater inflater = null;

        Bus_adapter(Context context, JSONArray FeedItems) {
            this.context = context;
            this.FeedItems = FeedItems;

            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return FeedItems.length();
        }

        @Override
        public Object getItem(int position) {
            try {
                return FeedItems.getJSONObject(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View vi = convertView;
            if (vi == null)
                vi = inflater.inflate(R.layout.bus_listitem, null);

            TextView Title = (TextView) vi.findViewById(R.id.bus_name);
            TextView Description = (TextView) vi.findViewById(R.id.bustype);
            TextView add = (TextView) vi.findViewById(R.id.add);
            Button contact = (Button) vi.findViewById(R.id.call);
            Button url = (Button) vi.findViewById(R.id.book);
            TextView fair = (TextView) vi.findViewById(R.id.fair);


            try {
                Title.setText(FeedItems.getJSONObject(position).getString("name"));
                Description.setText(FeedItems.getJSONObject(position).getString("type"));
                add.setText(FeedItems.getJSONObject(position).getString("dep_add"));

                contact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        try {
                            intent.setData(Uri.parse("tel:" + FeedItems.getJSONObject(position).getString("contact")));
                            context.startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                url.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent browserIntent = null;
                        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://redbus.in"));

                        context.startActivity(browserIntent);
                    }
                });

                fair.setText(FeedItems.getJSONObject(position).getString("fair") + " Rs");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("ERROR : ", e.getMessage() + " ");
            }
            return vi;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.city :
                Intent i = new Intent(BusList.this, SelectCity.class);
                startActivity(i);
                break;
            case R.id.seldate :
                datePickerDialog.setVibrate(isVibrate());
                datePickerDialog.setYearRange(1985, 2028);
                datePickerDialog.setCloseOnSingleTapDay(isCloseOnSingleTapDay());
                datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
                break;
        }
    }
}
