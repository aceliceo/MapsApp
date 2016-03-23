package com.example.adalberto.mapsapp.tasks;

import android.media.UnsupportedSchemeException;
import android.os.AsyncTask;
import android.util.Log;

import com.example.adalberto.mapsapp.Constants;
import com.example.adalberto.mapsapp.interfaces.DirectionsReceivedListener;
import com.example.adalberto.mapsapp.utils.DurationTimeParser;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class DirectionsTask extends AsyncTask <Void, Void, Void>{

    private DirectionsReceivedListener listener;
    private LatLng start;
    private LatLng end;
    private String result;

    public DirectionsTask(LatLng start, LatLng end, DirectionsReceivedListener listener){
        this.listener = listener;
        this.start = start;
        this.end = end;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        InputStream inputStream = null;

        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(Constants.DIRECTIONS_API);
            stringBuilder.append("origin=");
            stringBuilder.append(start.latitude + "," + start.longitude);
            stringBuilder.append("&destination=");
            stringBuilder.append(end.latitude + "," + end.longitude);
            stringBuilder.append("&sensor=false&units=metric&mode=driving");

            URL url = new URL(stringBuilder.toString());
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            result = readIt(inputStream, 20000);

        }catch (Exception ex){
            Log.e("DirectionsTask", ex.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        getDirectionJson();
    }

    private String readIt(InputStream inputStream, int len) throws IOException, UnsupportedSchemeException {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;

        try {

            br = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }

    public void getDirectionJson() {
        try {
            ArrayList<LatLng> listGeopoints = new ArrayList<LatLng>();
            JSONObject json = new JSONObject(result);
            JSONArray routesArray = json.getJSONArray("routes");
            JSONObject route = routesArray.getJSONObject(0);
            JSONArray legsArray = route.getJSONArray("legs");
            JSONObject leg = legsArray.getJSONObject(0);
            JSONArray stepsArray = leg.getJSONArray("steps");

            JSONObject durationObject = leg.getJSONObject("duration");
            String duration = DurationTimeParser.parseDuration(durationObject.getString("text"));

            JSONObject step;
            JSONObject startLocation;
            JSONObject endLocation;
            JSONObject polyline;
            double latitude;
            double longitude;

            for(int i=0; i < stepsArray.length(); i++){
                step = stepsArray.getJSONObject(i);

                startLocation = step.getJSONObject("start_location");
                latitude = startLocation.getDouble("lat");
                longitude = startLocation.getDouble("lng");
                listGeopoints.add(new LatLng(latitude, longitude));

                polyline = step.getJSONObject("polyline");
                ArrayList<LatLng> points = decodePoly(polyline.get("points").toString());

                for(int j=0; j < points.size(); j++){
                    latitude = points.get(j).latitude;
                    longitude = points.get(j).longitude;
                    listGeopoints.add(new LatLng(latitude, longitude));
                }

                endLocation = step.getJSONObject("end_location");
                latitude = endLocation.getDouble("lat");
                longitude = endLocation.getDouble("lng");
                listGeopoints.add(new LatLng(latitude, longitude));
            }

            listener.done(listGeopoints, duration);
        }catch (Exception ex){
            Log.e("DirectionsTask", ex.getMessage());
        }
    }

    private ArrayList<LatLng> decodePoly(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(position);
        }
        return poly;
    }
}
