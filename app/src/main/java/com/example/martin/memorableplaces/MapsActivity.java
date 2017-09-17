package com.example.martin.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import static android.R.attr.defaultValue;
import static android.R.attr.key;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager mLocationManager;
    LocationListener mLocationListener;
    Location lastKnownLocation;
    Double latitude;
    Double longitude;
    Intent sendAddress;
    String houseNumber;
    String streetName;
    Boolean memorablePlace = false;
    String receivedAddress;
    Double receivedLatitude;
    Double receivedLongitude;

    Geocoding task;


    private class Geocoding extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1){

                    char content = (char) data;
                    result += content;
                    data = reader.read();
                }

                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject geoData = new JSONObject(s);
                String houseNumber = geoData.getJSONArray("results").getJSONObject(0).getJSONArray("address_components").getJSONObject(0).getString("short_name");
                String streetName = geoData.getJSONArray("results").getJSONObject(0).getJSONArray("address_components").getJSONObject(1).getString("short_name");
                updateAddress(houseNumber, streetName);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public void updateAddress(String number, String street){

        mMap.clear();
        LatLng tappedPlace = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(tappedPlace).title(number + " "  + street));
        updateLocation();
        houseNumber = number;
        streetName = street;
        Toast.makeText(this, houseNumber + streetName, Toast.LENGTH_SHORT).show();

    }

    public void updateLocation(){

        LatLng place = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 15));

    }

    public void setInitialLocation(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            latitude = lastKnownLocation.getLatitude();
            longitude = lastKnownLocation.getLongitude();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            setInitialLocation();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                latitude = location.getLatitude();
                longitude = location.getLongitude();
                updateLocation();

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else{

            setInitialLocation();

        }

        receivedAddress = getIntent().getStringExtra("Address");
        receivedLatitude = getIntent().getDoubleExtra("Latitude", latitude);
        receivedLongitude = getIntent().getDoubleExtra("Longitude", longitude);

        if (!receivedAddress.equals("")){

            latitude = receivedLatitude;
            longitude = receivedLongitude;
            memorablePlace = true;

        }



        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        updateLocation();


        if(memorablePlace){

            LatLng receivedLatLng = new LatLng(receivedLatitude, receivedLongitude);
            mMap.addMarker(new MarkerOptions().position(receivedLatLng).title(receivedAddress));
            memorablePlace = false;
        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                latitude = latLng.latitude;
                longitude = latLng.longitude;
                task = new Geocoding();
                task.execute("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + Double.toString(latitude) + "," + Double.toString(longitude) + "&key=AIzaSyBY83Z9h6yKfsfEbBepf5qYK9uoRT5VsNk");
            }
        });

    }

    @Override
    public void onBackPressed() {
        sendAddress = new Intent(getApplicationContext(), MainActivity.class);
        if (houseNumber != null || streetName != null) {
            sendAddress.putExtra("Address", houseNumber + " " + streetName);
            sendAddress.putExtra("Latitude", latitude);
            sendAddress.putExtra("Longitude", longitude);
        }
        setResult(RESULT_OK, sendAddress);
        finish();
        super.onBackPressed();
    }
}
