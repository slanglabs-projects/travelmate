package tie.hackathon.travelguide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.slanglabs.slang.Slang;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import flipviewpager.adapter.BaseFlipAdapter;
import flipviewpager.utils.FlipSettings;
import objects.City;
import tie.hackathon.travelguide.destinations.description.FinalCityInfo;
import tie.hackathon.travelguide.destinations.funfacts.FunFacts;
import views.FontTextView;

public class CityFragment extends Fragment {

    @BindView(R.id.cityname)    AutoCompleteTextView    cityname;
    @BindView(R.id.pb)          ProgressBar             pb;
    @BindView(R.id.music_list)  ListView                lv;

    List<String> id     = new ArrayList<>();

    private String      nameyet;
    private Activity    activity;
    private Typeface    tex;
    private Handler     mHandler;

    public CityFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_citylist, container, false);

        ButterKnife.bind(this,v);

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);

        mHandler = new Handler(Looper.getMainLooper());
        tex = Typeface.createFromAsset(activity.getAssets(), "fonts/texgyreadventor-bold.otf");
        cityname.setThreshold(1);

        getCity();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Slang.ui().trigger().show();
    }

    @OnTextChanged(R.id.cityname) void onTextChanged(){
        nameyet = cityname.getText().toString();
        if (!nameyet.contains(" ")) {
            Log.e("name", nameyet + " ");
            tripAutoComplete(nameyet.trim());
        }
    }

    private void tripAutoComplete(String cityName) {
        final Activity activity = this.getActivity();

        CityUtils.getCityDetails(cityName, new CityUtils.CityDataCallback() {
            @Override
            public void onMatchedCityData(final List list, final List list1, final List<String> list2) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                            (activity.getApplicationContext(), R.layout.spinner_layout, list);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        cityname.setThreshold(1);
                        cityname.setAdapter(dataAdapter);
                        cityname.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                Log.e("jkjb", "uihgiug" + arg2);
                                CityUtils.launchCity(
                                    activity,
                                    list1.get(arg2).toString(),
                                    list.get(arg2).toString(),
                                    list2.get(arg2)
                                );
                            }
                        });
                    }
                });
            }

            @Override
            public void onAllCitiesData(List<City> cities, FlipSettings settings) {
                // not needed here
            }
        });
    }

    private void getCity() {
        CityUtils.getCityDetails(null, new CityUtils.CityDataCallback() {
            @Override
            public void onMatchedCityData(final List list, final List list1, final List<String> list2) {
                // not needed here
            }

            @Override
            public void onAllCitiesData(final List<City> cities, final FlipSettings settings) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        pb.setVisibility(View.GONE);
                        lv.setAdapter(new CityAdapter(activity, cities, settings));
                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id1) {
                                City f = (City) lv.getAdapter().getItem(position);
                                Toast.makeText(activity, f.getNickname(), Toast.LENGTH_SHORT).show();
                                CityUtils.launchCity(
                                    activity,
                                    f.getId(),
                                    f.getNickname(),
                                    f.getAvatar()
                                );
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        this.activity = (Activity) activity;
    }

    class CityAdapter extends BaseFlipAdapter<City> {

        private final int PAGES = 3;
        private final int[] IDS_INTEREST = {R.id.interest_1, R.id.interest_2, R.id.interest_3, R.id.interest_4};

        CityAdapter(Context context, List<City> items, FlipSettings settings) {
            super(context, items, settings);
        }

        @Override
        public View getPage(int position, View convertView, ViewGroup parent, final City friend1, final City friend2) {
            final CitiesHolder holder;

            if (convertView == null) {
                holder = new CitiesHolder();
                convertView = activity.getLayoutInflater().inflate(R.layout.friends_merge_page, parent, false);
                holder.leftAvatar = (ImageView) convertView.findViewById(R.id.first);
                holder.rightAvatar = (ImageView) convertView.findViewById(R.id.second);
                holder.left = (TextView) convertView.findViewById(R.id.name1);
                holder.right = (TextView) convertView.findViewById(R.id.name2);
                holder.infoPage = activity.getLayoutInflater().inflate(R.layout.friends_info, parent, false);
                holder.nickName = (TextView) holder.infoPage.findViewById(R.id.nickname);
                holder.fv1 = (FontTextView) holder.infoPage.findViewById(R.id.interest_1);
                holder.fv2 = (FontTextView) holder.infoPage.findViewById(R.id.interest_2);
                holder.fv3 = (FontTextView) holder.infoPage.findViewById(R.id.interest_3);
                holder.fv4 = (FontTextView) holder.infoPage.findViewById(R.id.interest_4);

                for (int id : IDS_INTEREST)
                    holder.interests.add((TextView) holder.infoPage.findViewById(id));

                convertView.setTag(holder);
            } else {
                holder = (CitiesHolder) convertView.getTag();
            }

            switch (position) {
                case 1:
                    Picasso.with(getActivity()).load(friend1.getAvatar()).placeholder(R.drawable.delhi).into(holder.leftAvatar);
                    holder.left.setTypeface(tex);
                    holder.left.setText(friend1.getNickname());

                    if (friend2 != null) {
                        holder.right.setText(friend2.getNickname());
                        holder.right.setTypeface(tex);
                        Picasso.with(getActivity()).load(friend2.getAvatar()).placeholder(R.drawable.delhi).into(holder.rightAvatar);
                    }
                    break;
                default:
                    fillHolder(holder, position == 0 ? friend1 : friend2);
                    holder.infoPage.setTag(holder);
                    return holder.infoPage;
            }
            return convertView;
        }

        @Override
        public int getPagesCount() {
            return PAGES;
        }

        private void fillHolder(CitiesHolder holder, final City friend) {
            if (friend == null)
                return;
            Iterator<TextView> iViews = holder.interests.iterator();
            Iterator<String> iInterests = friend.getInterests().iterator();
            while (iViews.hasNext() && iInterests.hasNext())
                iViews.next().setText(iInterests.next());
            holder.infoPage.setBackgroundColor(getResources().getColor(friend.getBackground()));
            holder.nickName.setText(friend.getNickname());

            holder.nickName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("fsgb", "clikc");
                }
            });

            holder.fv1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(activity, FinalCityInfo.class);
                    i.putExtra("id_", friend.getId());
                    i.putExtra("name_", friend.getNickname());
                    i.putExtra("image_", friend.getAvatar());
                    startActivity(i);
                }
            });

            holder.fv3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(activity, FunFacts.class);
                    i.putExtra("id_", friend.getId());
                    i.putExtra("name_", friend.getNickname());
                    activity.startActivity(i);
                }
            });

            holder.fv2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/?ie=UTF8&hq=&ll=" +
                            friend.getLa() +
                            "," +
                            friend.getLo() +
                            "&z=13"));
                    activity.startActivity(browserIntent);
                }
            });

            holder.fv4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                    activity.startActivity(browserIntent);
                }
            });
        }

        class CitiesHolder {
            ImageView leftAvatar;
            ImageView rightAvatar;
            View infoPage;
            TextView fv1, fv2, fv3, fv4;
            TextView left, right;
            final List<TextView> interests = new ArrayList<>();
            TextView nickName;
        }
    }
}
