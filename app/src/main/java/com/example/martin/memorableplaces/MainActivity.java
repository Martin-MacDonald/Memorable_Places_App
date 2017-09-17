package com.example.martin.memorableplaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import static android.R.attr.checkable;
import static android.R.attr.data;
import static android.R.attr.defaultValue;

public class MainActivity extends AppCompatActivity {

    ListView placesListView;
    Intent changeToMap;
    ArrayList<String> placesListItems = new ArrayList<>();
    ArrayList<Double> latitudeList = new ArrayList<>();
    ArrayList<Double> longitudeList = new ArrayList<>();
    SharedPreferences sharedPreferences;



    public void createAdapter(){

        ArrayAdapter<String> placesList = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, placesListItems);
        placesListView.setAdapter(placesList);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = this.getSharedPreferences("com.example.martin.memorableplaces", Context.MODE_PRIVATE);

        placesListView = (ListView) findViewById(R.id.placesListView);
        changeToMap = new Intent(getApplicationContext(), MapsActivity.class);

        try {
            placesListItems = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Address", ObjectSerializer.serialize(new ArrayList<String>())));
            latitudeList = (ArrayList<Double>) ObjectSerializer.deserialize(sharedPreferences.getString("Lat", ObjectSerializer.serialize(new ArrayList<Double>())));
            longitudeList = (ArrayList<Double>) ObjectSerializer.deserialize(sharedPreferences.getString("Long", ObjectSerializer.serialize(new ArrayList<Double>())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (placesListItems.isEmpty()) {
            placesListItems.add("Find a place...");
            latitudeList.add(0.00);
            longitudeList.add(0.00);
        }

        createAdapter();

        placesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    changeToMap.putExtra("Address", "");
                    startActivityForResult(changeToMap, 1);
                } else {
                    changeToMap.putExtra("Address", placesListItems.get(i));
                    changeToMap.putExtra("Latitude", latitudeList.get(i));
                    changeToMap.putExtra("Longitude", longitudeList.get(i));
                    startActivityForResult(changeToMap, 1);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                String receivedAddress = data.getStringExtra("Address");
                Double receivedLatitude = data.getDoubleExtra("Latitude", defaultValue);
                Double receivedLongitude = data.getDoubleExtra("Longitude", defaultValue);
                if (receivedAddress != null) {
                    placesListItems.add(receivedAddress);
                    latitudeList.add(receivedLatitude);
                    longitudeList.add(receivedLongitude);
                    createAdapter();
                    Toast.makeText(this, Double.toString(latitudeList.get(1)) + " " + Double.toString(longitudeList.get(1)) , Toast.LENGTH_SHORT).show();
                }
            }
        }

        try {
            sharedPreferences.edit().putString("Address", ObjectSerializer.serialize(placesListItems)).apply();
            sharedPreferences.edit().putString("Lat", ObjectSerializer.serialize(latitudeList)).apply();
            sharedPreferences.edit().putString("Long", ObjectSerializer.serialize(longitudeList)).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
