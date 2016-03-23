package com.example.adalberto.mapsapp.interfaces;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public interface DirectionsReceivedListener {
    void done(ArrayList<LatLng> points, String duration);
}
