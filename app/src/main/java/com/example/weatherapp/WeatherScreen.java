package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

public class WeatherScreen extends AppCompatActivity {

    private static final int PERMISSION_ID = 111;
    FusedLocationProviderClient
            mFusedLocationClient;
    TextView weatherInfoTextView;
    TextView tempTextView;
    TextView cityNameTextView;
    TextView weatherTextView;
    TextView loadingTextView;
    EditText locationEditText;
    TextView locationTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_screen);

        mFusedLocationClient
                = LocationServices
                .getFusedLocationProviderClient(this);
        weatherInfoTextView = findViewById(R.id.weatherInfoTextView);
        tempTextView = findViewById(R.id.tempTextView);
        cityNameTextView = findViewById(R.id.cityNameTextView);
        weatherTextView = findViewById(R.id.weatherTextView);
        loadingTextView = findViewById(R.id.loadingTextView);
        locationEditText = findViewById(R.id.locationEditText);
        locationTextView = findViewById(R.id.locationTextView);
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        // check if permissions are given
        if (checkPermissions()) {
            // check if location is enabled
            if (isLocationEnabled()) {
                // getting last location from FusedLocationClient object
                mFusedLocationClient.getLastLocation()
                        .addOnCompleteListener(
                                new OnCompleteListener<Location>() {

                                    @Override
                                    public void onComplete(
                                            @NonNull Task<Location> task)
                                    {
                                        Location location = task.getResult();
                                        if (location == null) {
                                            requestNewLocationData();
                                        }
                                        else {
                                            // do something with location
                                            callApiByLocation(location);
                                            String locationTextViewString = "your last known location \nlat. = "+location.getLatitude()+"\nlon = "+location.getLongitude();
                                            locationTextView.setText(locationTextViewString);
                                        }
                                    }
                                });
            }else {
                Toast.makeText(this, "Please turn on your location...", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
        else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        // Initializing LocationRequest object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();

            callApiByLocation(mLastLocation);
            String locationTextViewString = "your last known location \nlat. = "+mLastLocation.getLatitude()+"\nlon = "+mLastLocation.getLongitude();
            locationTextView.setText(locationTextViewString);
        }
    };

    private void callApiByLocation(Location location){
        DownloadData download = new DownloadData();
        download.execute("https://api.weatherapi.com/v1/current.json?key=35a23343e2284126b8e115149200312&q="+location.getLatitude()+","+location.getLongitude());
    }

    private void callApiByName(){
        DownloadData download = new DownloadData();
        locationTextView.setText("");
        download.execute("https://api.weatherapi.com/v1/current.json?key=35a23343e2284126b8e115149200312&q="+locationEditText.getText().toString().trim());
    }

    // method to check for permissions
    private boolean checkPermissions(){
        return ActivityCompat
                .checkSelfPermission(
                        this,
                        Manifest.permission
                                .ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED

                && ActivityCompat
                .checkSelfPermission(
                        this,
                        Manifest.permission
                                .ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    // method to requestfor permissions
    private void requestPermissions(){
        ActivityCompat.requestPermissions(
                this,
                new String[] {
                        Manifest.permission
                                .ACCESS_COARSE_LOCATION,
                        Manifest.permission
                                .ACCESS_FINE_LOCATION },
                PERMISSION_ID);
    }

    // method to check if location is enabled
    private boolean isLocationEnabled(){
        LocationManager
                locationManager
                = (LocationManager)getSystemService(
                Context.LOCATION_SERVICE);

        return locationManager
                .isProviderEnabled(
                        LocationManager.GPS_PROVIDER)
                || locationManager
                .isProviderEnabled(
                        LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super
                .onRequestPermissionsResult(
                        requestCode,
                        permissions,
                        grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0
                    && grantResults[0]
                    == PackageManager
                    .PERMISSION_GRANTED) {

                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }

    public void findWeather(View view){
        loading();
        callApiByName();
    }

    public void locationSearchClick(View view){
        loading();
        getLastLocation();
    }

    private void loading(){
        String loadingText = "Loading...";
        loadingTextView.setText(loadingText);
    }

    public void updateText(String city,String weatherText,String tempInC,String weatherInfo,String iconNo){
        cityNameTextView.setText(city);
        weatherTextView.setText(weatherText);
        tempTextView.setText(tempInC);
        weatherInfoTextView.setText(weatherInfo);
        loadingTextView.setText("");
    }

    public void resetText(){
        cityNameTextView.setText("");
        weatherTextView.setText("");
        tempTextView.setText("0");
        weatherInfoTextView.setText("");
        loadingTextView.setText("");
    }

    public void downloadDataComplete(String result) {
        if (result != null) {
            Log.d("Download data", "on post excute  is called");
            try {
                JSONObject jsonObject =new  JSONObject(result);
                JSONObject current = jsonObject.getJSONObject("current");
                JSONObject location = jsonObject.getJSONObject("location");
                String city = location.getString("name");
                DecimalFormat df = new DecimalFormat("#.##");

                JSONObject condition = current.getJSONObject("condition");
                String weatherText = condition.getString("text");
                int iconNo = condition.getInt("code");
                String tempInC = df.format(current.getDouble("temp_c"));
                String windKph = df.format(current.getDouble("wind_kph"));
                int windDeg= current.getInt("wind_degree");
                String pressureInMb = df.format(current.getDouble("pressure_mb"));
                int humidity = current.getInt("humidity");

                String weatherInfo = String.format("Wind speed : %s\n" +
                        "Wind degree= %d\n"+
                        "pressure= %s mb\n"+
                        "Humidity= %d",windKph,windDeg,pressureInMb,humidity);

                updateText(city, weatherText, tempInC, weatherInfo, String.valueOf(iconNo));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(
                        this, e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
                String errorMsg = "Error!!";
                resetText();
                weatherInfoTextView.setText(errorMsg);
            }
        }
    }

    public class DownloadData extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... strings) {
            return downloadJson(strings[0]);
        }

        private String downloadJson(String url){
            StringBuilder data = new StringBuilder();
            Log.d("Download data", "download json is called");
            try {
                URL urlObject = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
                int responseCode = connection.getResponseCode();

                Log.d("DownloadData", "response code is $responseCode");

                BufferedReader reader =new BufferedReader(new InputStreamReader(connection.getInputStream()));

                char[] inputBuffer = new char[500];
                int charRead = 0;
                while (charRead >= 0) {
                    charRead = reader.read(inputBuffer);
                    if (charRead > 0) {
                        data.append(inputBuffer, 0, charRead);
                    }
                }

                reader.close();
                Log.d("Download data", "data is downloaded ${data.length}");
            } catch (MalformedURLException e) {
                Log.d("DownloadData", "url is incorrect");
            } catch (IOException e) {
                Log.d("DownloadData", "io exception");
                e.printStackTrace();
            } catch (Exception e) {
                Log.d("DownloadData", "unknown error");
                e.printStackTrace();
            }
            return data.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            downloadDataComplete(s);
        }
    }
}