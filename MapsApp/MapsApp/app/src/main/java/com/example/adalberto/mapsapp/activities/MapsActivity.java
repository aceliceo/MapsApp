package com.example.adalberto.mapsapp.activities;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.example.adalberto.mapsapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    /*
    tuPollitaTeAma();
    besitos();
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng myLocation = new LatLng(-34, 151);

        mMap.addMarker(new MarkerOptions().position(myLocation).title("Mi casa!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
    }
}
