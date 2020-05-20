package com.example.uber_clone_app;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import static com.parse.ParseRole.getQuery;

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback {
    
    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Button callUberButton;
    Button logout;
    TextView du;
    boolean activeRequest = false;
    
    Location locationofUser;
    Handler handler = new Handler();
    
    boolean locationOfDriverAcquired = false;
    
    // how to get decimals in java:
    
    // Multiply : 13.29 * 10 = 132.9
    // Round    : 132.9 => 133
    // Divide   : 13.3
    
    
    // TODO:
    //  querying multiple times per frame is VERY WRONG
    //  NORMALYY YOU SHOULDNT DO THIS, THIS IS JUST A LEARNING DEMO
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED) {
    
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
    
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    locationofUser = lastKnownLocation;
                    checkForDriverLocationUpdate();
                }
            }
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        
        callUberButton = (Button)findViewById(R.id.uberButton);
        logout = (Button)findViewById(R.id.logOutRider);
        du = (TextView)findViewById(R.id.driverUpdateText);
        
        // request active or no?
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("request");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        activeRequest = true;
                        callUberButton.setText("Cancel Uber Request");
                    }
                }
            }
        });
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
        
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationofUser = location;
                checkForDriverLocationUpdate();
            }
    
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
        
            }
    
            @Override
            public void onProviderEnabled(String provider) {
        
            }
    
            @Override
            public void onProviderDisabled(String provider) {
        
            }
        };
        if (Build.VERSION.SDK_INT < 23) {
            
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            }
            
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0,locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                
                if (lastKnownLocation != null) {
                    locationofUser = lastKnownLocation;
                    checkForDriverLocationUpdate();
                }
            }
        }
     }
     
    public void checkForDriverLocationUpdate() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("request");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.whereExists("driver_responsible");
        query.findInBackground(new FindCallback<ParseObject>() {
            
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
    
                    
                        ParseQuery<ParseUser> query2 = ParseUser.getQuery();
                        query2.whereEqualTo("username", objects.get(0).getString("driver_responsible"));
                        query2.findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> objects, ParseException e) {
                                if (e == null && objects.size() > 0 && locationofUser != null) {
                                        callUberButton.setVisibility(View.INVISIBLE);
                                        logout.setVisibility(View.INVISIBLE);
                                        ParseGeoPoint driverGeo = objects.get(0).getParseGeoPoint("location");
                                        ParseGeoPoint riderGeo = new ParseGeoPoint(locationofUser.getLatitude(), locationofUser.getLongitude());
                                        Double distanceInKilometers = riderGeo.distanceInKilometersTo(driverGeo);
                                        Double distanceOneDecimal = (double) Math.round(distanceInKilometers * 10) / 10;
    
                                        du.setText("Your driver is: " +  Double.toString(distanceOneDecimal) + " Kilometers Away");
                                        
                                        LatLng driverLL = new LatLng(driverGeo.getLatitude(), driverGeo.getLongitude());
                                        LatLng riderLL = new LatLng(riderGeo.getLatitude(), riderGeo.getLongitude());
    
                                        ArrayList<Marker> markers = new ArrayList<Marker>();
                                        
                                        mMap.clear();
                                        markers.add(mMap.addMarker(new MarkerOptions().position(riderLL).title("You are here")));
                                        markers.add(mMap.addMarker(new MarkerOptions().position(driverLL).title("Driver is here")));
                                        markers.get(1).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
    
                                     LatLngBounds.Builder builder = new LatLngBounds.Builder();
    
                                     for (Marker marker : markers) {
                                         builder.include(marker.getPosition());
                                     }
    
                                     LatLngBounds bounds = builder.build();
    
                                     int padding = 150;
                                     CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
    
                                     mMap.moveCamera(cameraUpdate);
                                     locationOfDriverAcquired = true;
                                     
                                     if (distanceInKilometers < 0.01) {
                                         du.setText("Your Driver has Arrived!");
                                         handler.postDelayed(new Runnable() {
                                             @Override
                                             public void run() {
                                                 ParseQuery<ParseObject> query = ParseQuery.getQuery("request");
                                                 query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                                                 query.findInBackground(new FindCallback<ParseObject>() {
                                                     @Override
                                                     public void done(List<ParseObject> objects, ParseException e) {
                                                         if (e == null) {
                                                             for (ParseObject o : objects) {
                                                                 o.deleteInBackground();
                                                             }
                                                         }
                                                     }
                                                 });
                                                 ParseUser.getCurrentUser().logOut();
                                                 finish();
                                             }
                                         }, 5000);
                                     }
                                    
                                }
                            }
                        });
                        
                        
                }
            }
        });
    }
    public void callUber(View view) {
        
        callUberButton.setEnabled(false);
        logout.setEnabled(false);
    
        // is there even a neeed to double check permissions again?
        // perhaps user could ignore it the first time then try to click the button
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED) {
            
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
            
            if (activeRequest == false) {
                if (lastKnownLocation != null) {
        
                    // TODO:
                    //   SAVING USER NAME
                    ParseObject uberRequest = new ParseObject("request");
                    uberRequest.put("username", ParseUser.getCurrentUser().getUsername());
                    
                    locationofUser = lastKnownLocation;
        
                    // TODO:
                    //   SAVING LOCATION (GEOPOINT)
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    uberRequest.put("location", parseGeoPoint);
        
                    uberRequest.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                callUberButton.setText("Cancel Uber Request");
                                callUberButton.setEnabled(true);
                                logout.setEnabled(true);
                                activeRequest = true;
                            }
                        }
                    });
        
                } else {
        
                    Toast.makeText(this, "Your Location can't be found", Toast.LENGTH_SHORT).show();
        
                }
                
            } else {
                // ACTIVE REQUEST EXISTS
                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("request");
                query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                             if (objects.size() > 0) {
                                 for (ParseObject object : objects) {
                                     
                                     object.deleteInBackground();
    
                                     callUberButton.setText("Request Uber Driver");
                                     callUberButton.setEnabled(true);
                                     logout.setEnabled(true);
                                     activeRequest = false;
                                 }
                             }
                        }
                    }
                });
            }
        }
    }
    
    public void LogMeOut(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("deleteHistory", true);
        startActivity(intent);
        callUberButton.setEnabled(false);
        logout.setEnabled(false);
        finish();
    }
}
