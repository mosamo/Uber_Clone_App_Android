package com.example.uber_clone_app;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class DriverMapsActivity extends FragmentActivity implements OnMapReadyCallback {
    
    private GoogleMap mMap;
    String riderName;
    boolean mapLoaded = false;
    
    LatLng requestLocation;
    LatLng driverLocation;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    
    
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near requestLocation, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mapLoaded = true;
                waitForMapPlease();
            }
        });
    }
    
    public void waitForMapPlease() {
        Intent intent = getIntent();
    
        requestLocation = new LatLng(intent.getDoubleExtra("reqLat", 0), intent.getDoubleExtra("reqLng", 0));
        driverLocation = new LatLng(intent.getDoubleExtra("driverLat", 0), intent.getDoubleExtra("driverLng", 0));
        
        riderName = intent.getStringExtra("username");
    
        // code to display all markers on screen
        ArrayList<Marker> markers = new ArrayList<Marker>();
    
        markers.add(mMap.addMarker(new MarkerOptions().position(requestLocation).title("Ride Request here")));
        markers.get(0).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        
        markers.add(mMap.addMarker(new MarkerOptions().position(driverLocation).title("You are here")));
        
    
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
    
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
    
        LatLngBounds bounds = builder.build();
    
        int padding = 60;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
    
        mMap.moveCamera(cameraUpdate);
        // you can use mMap.animateCamera(cameraUpdate) but i dont like the transition animation
    
    }
    
    public void acceptRequest (View view) {
        final Button v = (Button)view;
        
        v.setEnabled(false);
        v.setText("Request Accepted");
        
        if (mapLoaded) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("request");
            
            query.whereEqualTo("username", riderName);
            
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() > 0) {
                            for (ParseObject object : objects) {
                                
                                object.put("driver_responsible", ParseUser.getCurrentUser().getUsername());
    
                                object.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            v.setEnabled(false);
                                            Intent directions_intent = new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("http://maps.google.com/maps?saddr="+driverLocation.latitude+","+driverLocation.longitude+"&daddr="+requestLocation.latitude+","+requestLocation.longitude));
                                            startActivity(directions_intent);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }
    }
}
