/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.adalberto.mapsapp.activities;

import com.example.adalberto.mapsapp.interfaces.DirectionsReceivedListener;
import com.example.adalberto.mapsapp.tasks.DirectionsTask;
import com.example.adalberto.mapsapp.utils.DurationTimeParser;
import com.example.adalberto.mapsapp.utils.PermissionUtils;
import com.example.adalberto.mapsapp.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This demo shows how GMS Location can be used to check for changes to the users location.  The
 * "My Location" button uses GMS Location to set the blue dot representing the users location.
 * Permission for {@link android.Manifest.permission#ACCESS_FINE_LOCATION} is requested at run
 * time. If the permission has not been granted, the Activity is finished with an error message.
 */
public class MyLocationDemoActivity extends AppCompatActivity
        implements
        //OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback
        {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleMap mMap;

    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_location_demo);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        //mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

            final LatLng destination = new LatLng(32.733538, -117.193201);
            final LatLng car = new LatLng(32.534368, -116.966929);
            final LatLng torreyPines = new LatLng(32.889390, -117.227211);
            final LatLng santaBarbara = new LatLng(34.413708, -119.691582);
            final LatLng zoo = new LatLng(32.733429, -117.155027);

            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, new android.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(currentLocation);
                    builder.include(destination);

                    LatLngBounds bounds = builder.build();

                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 200);
                    mMap.animateCamera(cameraUpdate);

                    mMap.addMarker(new MarkerOptions()
                            .position(destination)
                            .title("San Diego Airport"));

                    drawRoute(currentLocation, destination);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {   }

                @Override
                public void onProviderEnabled(String provider) {   }

                @Override
                public void onProviderDisabled(String provider) {   }
            });
        }
    }

    private void drawRoute(LatLng start, LatLng end){
        final DirectionsTask directionsTask = new DirectionsTask(start, end, new DirectionsReceivedListener() {
            @Override
            public void done(ArrayList<LatLng> points, String duration) {
                final PolylineOptions rectLine = new PolylineOptions().width(15).color(Color.rgb(0, 128, 255)).geodesic(true);

                for(LatLng latlng: points){
                    rectLine.add(latlng);
                }

                mMap.addPolyline(rectLine);

                if(!duration.isEmpty()){
                    findViewById(R.id.duration_layout).setVisibility(View.VISIBLE);

                    TextView durationText = (TextView)findViewById(R.id.duration_text);
                    durationText.setText(duration);

                    int minutesRoute = DurationTimeParser.getDurationInMinutes(duration);

                    if(minutesRoute > 35){
                        durationText.setTextColor(ContextCompat.getColor(MyLocationDemoActivity.this ,R.color.traffic_slow));
                    }else if(minutesRoute > 15){
                        durationText.setTextColor(ContextCompat.getColor(MyLocationDemoActivity.this, R.color.traffic_medium));
                    }else{
                        durationText.setTextColor(ContextCompat.getColor(MyLocationDemoActivity.this, R.color.traffic_fast));
                    }
                }

            }
        });

        directionsTask.execute();
    }

//    @Override
//    public boolean onMyLocationButtonClick() {
//        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
//        // Return false so that we don't consume the event and the default behavior still occurs
//        // (the camera animates to the user's current position).
//        return false;
//    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
}