package com.example.weatherjournal2.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherjournal2.R;
import com.example.weatherjournal2.WeatherRVAdapter;
import com.example.weatherjournal2.WeatherRVModal;
import com.example.weatherjournal2.databinding.FragmentHomeBinding;
import com.example.weatherjournal2.ui.Models.Post;
import com.example.weatherjournal2.ui.Models.Weather;
import com.example.weatherjournal2.ui.Models.WeatherManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ImageButton searchButton;
    private RelativeLayout homeRL;
    private LinearLayout postsLayout;
    private TextView cityNameTV, temperatureTV, conditionTV, whatsNew;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backIV, iconIV, searchIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private FusedLocationProviderClient fusedLocationClient;
    private String cityName;
    private ProgressBar loadingPB;
    Weather currentWeather = new Weather();
    Weather searchedWeather = new Weather();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        homeRL = root.findViewById(R.id.idRLHome);
        cityNameTV = root.findViewById(R.id.idTVCityName);
        conditionTV = root.findViewById(R.id.idTVCondition);
        temperatureTV = root.findViewById(R.id.idTVTemperature);
        weatherRV = root.findViewById(R.id.idRVWeather);
        cityEdt = root.findViewById(R.id.idEdtCity);
        backIV = root.findViewById(R.id.idIVBack);
        searchIV = root.findViewById(R.id.idIVSearch);
        iconIV = root.findViewById(R.id.idIVIcon);
        loadingPB = root.findViewById(R.id.idPBLoading);
        postsLayout = root.findViewById(R.id.postsLayout);
        whatsNew = root.findViewById(R.id.idTVWhatsNewIn);

        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(getContext(), weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);
        homeRL.setVisibility(View.GONE);
        loadingPB.setVisibility(View.VISIBLE);

        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
            Task<Location> locationTask = fusedLocationClient.getLastLocation();
            locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        cityName = getCityName(longitude, latitude);
                        whatsNew.setText("What's new in " + cityName + "?");
                        currentWeather.setLongitude(longitude);
                        currentWeather.setLatitude(latitude);
                        currentWeather.setCityName(cityName);
                        Log.d("Latitude", String.valueOf(latitude));
                        Log.d("Longitude", String.valueOf(longitude));
                        Log.d("city", String.valueOf(getCityName(longitude, latitude)));
                        getWeatherInfo(cityName);
                        WeatherManager.getInstance().setCurrentWeather(currentWeather);
                        fetchPosts(cityName);
                    }
                }
            });
        }

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityEdt.getText().toString();
                if (city.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a city name", Toast.LENGTH_SHORT).show();
                } else {
                    getWeatherInfo(city);
                    fetchPosts(city);
                }
            }
        });

        return root;
    }

    private String getCityName(double longitude, double latitude) {
        String cityName = "Not found";
        Geocoder gcd = new Geocoder(requireActivity().getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                Address adr = addresses.get(0);
                String city = adr.getLocality();
                if (city != null && !city.isEmpty()) {
                    cityName = city;
                } else {
                    Log.d("TAG", "CITY NOT FOUND");
                    Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d("TAG", "No addresses found");
                Toast.makeText(getContext(), "No address found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("TAG", "Geocoder error: " + e.getMessage());
            Toast.makeText(getContext(), "Geocoding error", Toast.LENGTH_SHORT).show();
        }
        return cityName;
    }

    public static String removeDiacritics(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private void getWeatherInfo(String cityName) {

        cityName = removeDiacritics(cityName);
        String url = "http://api.weatherapi.com/v1/forecast.json?key=2897fb1a2e364ceebd3155906230411&q=" + cityName + "&days=1&aqi=no&alerts=no";

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                homeRL.setVisibility(View.VISIBLE);
                loadingPB.setVisibility(View.GONE);
                weatherRVModalArrayList.clear();
                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    String name = response.getJSONObject("location").getString("name");
                    String region = response.getJSONObject("location").getString("region");
                    cityNameTV.setText(name + ", " + region);
                    currentWeather.setTemperature(temperature);
                    temperatureTV.setText(temperature + "°C");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    Picasso.get().load("http:".concat(conditionIcon)).resize(200,200).into(iconIV);
                    currentWeather.setConditionIconString(conditionIcon);
                    conditionTV.setText(condition);
                    if (isDay == 1) {
                        Picasso.get().load("https://images.unsplash.com/photo-1617150119111-09bbb85178b0?auto=format&fit=crop&q=80&w=1887&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D").into(backIV);
                    } else {
                        Picasso.get().load("https://images.unsplash.com/photo-1472552944129-b035e9ea3744?auto=format&fit=crop&q=80&w=1887&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D").into(backIV);
                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastday = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecastday.getJSONArray("hour");

                    for (int i = 0; i < hourArray.length(); i++) {
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");
                        weatherRVModalArrayList.add(new WeatherRVModal(time, temper, img, wind));
                    }
                    weatherRVAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error parsing weather data", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NoConnectionError) {
                    Toast.makeText(getContext(), "No internet connection. Please check your network.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "An error occurred. Please enter a valid city name.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    public void fetchPosts(String city){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference postsRef = db.collection("posts");
        city = removeDiacritics(city);
        String finalCity = city;
        postsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<Post> postsList = new ArrayList<>();

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    Post post = documentSnapshot.toObject(Post.class);
                    GeoPoint location = post.getLocation();
                    String postCity = getCityName(location.getLongitude(), location.getLatitude());
                    if (postCity.equals("Timișoara")){
                        postCity = "Timisoara";
                    }
                    if (postCity.equals(finalCity)){
                        postsList.add(post);
                    }
                }
                if (postsList.isEmpty()){
                    whatsNew.setText("What's new in " + finalCity + "? Nothing now.");
                    postsLayout.removeAllViews();
                    return;
                }
                whatsNew.setText("What's new in " + finalCity + "?");

                Collections.sort(postsList);

                for (Post post : postsList) {
                    View postView = LayoutInflater.from(requireContext()).inflate(R.layout.post, null);
                    ProgressBar progressBar = postView.findViewById(R.id.idProgressBar);
                    progressBar.setVisibility(View.VISIBLE);

                    ImageView userPhotoImageView = postView.findViewById(R.id.idIVUserPhoto);
                    String userPhoto = post.getUserPhoto();
                    Picasso.get()
                            .load(userPhoto)
                            .transform(new CropCircleTransformation())
                            .into(userPhotoImageView);

                    progressBar.setVisibility(View.GONE);

                    TextView usernameTextView = postView.findViewById(R.id.idTVUsername);
                    String username = post.getUsername();
                    usernameTextView.setText(username);

                    TextView descriptionTextView = postView.findViewById(R.id.idTVDescription);
                    String description = post.getPostDescription();
                    descriptionTextView.setText(description);

                    ImageView postImageView = postView.findViewById(R.id.idIVPostPhoto);
                    String postImageUri = post.getPostImageUri();
                    Picasso.get().load(postImageUri).into(postImageView);

                    TextView timeAgoTextView = postView.findViewById(R.id.idTVTimeAgo);
                    String timeAgo = calculateTimeAgo(post.getTimeStampDate());
                    timeAgoTextView.setText(timeAgo);
                    Log.d("timeAgo", timeAgo);
                    Log.d("timestamp", post.getTimeStampDate().toString());

                    postsLayout.addView(postView);
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //
            }
        });
    }

    private String calculateTimeAgo(Date timestampDate) {
        if (timestampDate != null) {
            long currentTime = System.currentTimeMillis();
            long postTime = timestampDate.getTime();
            long timeDifference = currentTime - postTime;

            long hours = timeDifference / (60 * 60 * 1000);
            long days = hours / 24;

            if (timeDifference < 60000) { // Less than 1 minute
                return "Just now";
            } else if (timeDifference < 3600000) { // Less than 1 hour
                long minutesAgo = timeDifference / 60000;
                return minutesAgo + (minutesAgo == 1 ? " minute ago" : " minutes ago");
            } else if (hours > 0 && hours < 24) {
                return hours + ("h ago");
            } else if (days > 0) {
                return days + ("d ago");
            }
        }

        return "";
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}