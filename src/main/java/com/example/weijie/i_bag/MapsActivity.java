package com.example.weijie.i_bag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = "mapsActivity";
    private List<Marker> markerList = new ArrayList<>();
    private ArrayList<LatLng> latlng_list = new ArrayList<>();
    private ArrayList<Long> time_list = new ArrayList<>();
    private ArrayList<String> time_diff = new ArrayList<>();
    private MarkerOptions options = new MarkerOptions();
    private LatLngBounds.Builder builder = new LatLngBounds.Builder();
    private int map_type, poi_number;
    private LatLng coordinate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        FloatingActionButton fab_mapSetting = findViewById(R.id.floatingActionButtonSetting);
        FloatingActionButton fab_mapRefresh = findViewById(R.id.floatingActionButtonRefresh);


        fab_mapSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapPreferences_intent = new Intent(getApplicationContext(), MapPreferencesActivity.class);
                startActivity(mapPreferences_intent);
            }
        });

        fab_mapRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(getIntent());
            }
        });

        mapTypeInit();
        poiNumberInit();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        String myUrl = "http://iotcourier.com/WeiJie/android_query.php";
        HttpGetRequest getRequest = new HttpGetRequest();
        getRequest.execute(myUrl);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng KL = new LatLng(3.139003,101.686855);
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(KL,12));
        mMap.setMapType(map_type);
    }

    @Override
    public void onBackPressed() {
        SavePreferencesBoolean("BT_STATE",true);
        Intent intentMapMenuActivity = new Intent(getApplicationContext(),MainMenuActivity.class);
        startActivity(intentMapMenuActivity);
    }

    public class HttpGetRequest extends AsyncTask<String, Void, String> {
        public static final String REQUEST_METHOD = "GET";
        public static final int READ_TIMEOUT = 15000;
        public static final int CONNECTION_TIMEOUT = 15000;

        @Override
        protected String doInBackground(String... params){
            String stringUrl = params[0];
            String result;
            String inputLine;

            try {
                //Create a URL object holding our url
                URL myUrl = new URL(stringUrl);
                //Create a connection
                HttpURLConnection connection =(HttpURLConnection) myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                //Connect to our url
                connection.connect();
                //Create a new InputStreamReader
                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                //Create a new buffered reader and String Builder
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                //Check if the line we are reading is not null
                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }
                //Close our InputStream and Buffered reader
                reader.close();
                streamReader.close();
                //Set our result equal to our stringBuilder
                result = stringBuilder.toString();
            }
            catch(IOException e){
                e.printStackTrace();
                result = null;
                Log.d(TAG, "Failed to get response from the server...\nResult = " + result);
            }
            return result;
        }
        protected void onPostExecute(String result){
            Log.d(TAG, "HTTP GET result: " + result);
            String jsonStr = result;
            int i;
            double lat, lng;
            long time;

            if (jsonStr != null) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonStr);
                    for(i = 0; i< jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        lat = jsonObject.getDouble("latitude");
                        lng = jsonObject.getDouble("longitude");
                        time = jsonObject.getLong("time");
                        coordinate = new LatLng(lat,lng);
                        latlng_list.add(coordinate);
                        time_list.add(time);
                    }
                    Log.d("Time","time from server: " + time_list);
                    for(long server_time : time_list){
                        time_diff.add(elapsedTime(server_time));
                    }
                    Log.d("Time","Time difference : " + time_diff);

                    markerList.clear();

                    i =0;

                    for (LatLng latestPoint : latlng_list) {
                        options.position(latestPoint);
                        options.title(time_diff.get(i) + " ago");
                        options.snippet("Lat: " + latlng_list.get(i).latitude + ", Lng : " + latlng_list.get(i).longitude);
                        if(i == 0){
                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        }else{
                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        }

                        Marker marker = mMap.addMarker(options);
                        builder.include(marker.getPosition());

                        if( i >= poi_number - 1){
                            break;
                        }
                        i++;
                    }

                    if(i == 0){
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng_list.get(0),12));
                    }else{
                        LatLngBounds bounds = builder.build();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,200));
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Could not connect to the server...",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    private int ReloadPreferences(String key){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MY_SHARED_PREF",MODE_PRIVATE);
        int RETRIVED_DATA = sharedPreferences.getInt(key,0);
        return RETRIVED_DATA;
    }

    private void SavePreferencesInteger(String key, int value){
        SharedPreferences sharedPreferences = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private void SavePreferencesBoolean(String key, boolean value){
        SharedPreferences sharedPreferences = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private void mapTypeInit(){
        map_type = ReloadPreferences("RADIO_BUTTON_INDEX");
        if(map_type == 0){
            map_type = 1;
        }
        Intent get_map_type = getIntent();
        map_type = get_map_type.getIntExtra("mapType",map_type);
        SavePreferencesInteger("RADIO_BUTTON_INDEX",map_type);
    }

    private void poiNumberInit(){
        poi_number = ReloadPreferences("poi_number");
        if(poi_number == 0){
            poi_number = 1;
        }
        Intent get_poi_number = getIntent();
        poi_number = get_poi_number.getIntExtra("poiNumber",poi_number);
        SavePreferencesInteger("poi_number",poi_number);
    }

    private String elapsedTime(long input_time){
        String time = null;
        long time_current = System.currentTimeMillis();
        time_current = time_current/1000;
        long diff = time_current - input_time;

        Log.d("Time","Time difference = " + diff);
        if(diff > 86400){
            long day = diff / 86400;
            Log.d("Time","Day = " + day);
            if(day <= 1){
                return (String.valueOf(day) + " day");
            }else{
                return (String.valueOf(day) + " days");
            }
        }else {
            Log.d("Time", "Current_time = " + time_current);
            Log.d("Time", "Input_time = " + input_time);
            Log.d("Time", "Time_diff = " + diff);

            long millis = diff;
            long second = diff % 60;
            long minute = (diff / 60) % 60;
            long hour = (diff / (60 * 60)) % 24;

            if (hour == 0 && minute != 0) {
                if (minute < 10 && second < 10) {
                    time = String.format("%01d minute %01d second", minute, second);
                } else if (minute < 10 && second > 10) {
                    time = String.format("%01d minute %02d second", minute, second);
                } else if (minute > 10 && second < 10) {
                    time = String.format("%02d minute %01d second", minute, second);
                } else {
                    time = String.format("%02d minute %02d second", minute, second);
                }
            } else if (hour == 0 && minute == 0) {
                if (second < 10) {
                    time = String.format("%01d second", second);
                } else {
                    time = String.format("%02d second", second);
                }
            } else {
                if (hour < 10) {
                    if (minute < 10 && second < 10) {
                        time = String.format("%01d hour %01d minute %01d second", hour, minute, second);
                    } else if (minute < 10 && second > 10) {
                        time = String.format("%01d hour %01d minute %02d second", hour, minute, second);
                    } else if (minute > 10 && second < 10) {
                        time = String.format("%01d hour %02d minute %01d second", hour, minute, second);
                    } else {
                        time = String.format("%01d hour %02d minute %02d second", hour, minute, second);
                    }
                } else if (hour > 10) {
                    if (minute < 10 && second < 10) {
                        time = String.format("%02d hour %01d minute %01d second", hour, minute, second);
                    } else if (minute < 10 && second > 10) {
                        time = String.format("%02d hour %01d minute %02d second", hour, minute, second);
                    } else if (minute > 10 && second < 10) {
                        time = String.format("%02d hour %02d minute %01d second", hour, minute, second);
                    } else {
                        time = String.format("%02d hour %02d minute %02d second", hour, minute, second);
                    }
                }
            }
            Log.d("Time", "Time = " + time);
        }
        return time;
    }
}