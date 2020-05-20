package com.example.uber_clone_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DriverActivity extends AppCompatActivity {
    
    // Locations cannot be sent in requests
    // so we will send doubles
    ArrayList<Double> rideLatitudes = new ArrayList<Double>();
    ArrayList<Double> rideLongitudes = new ArrayList<Double>();
    ArrayList<String> riderNames = new ArrayList<String>();
    
    Button refreshButton;
    LocationManager locationManager;
    LocationListener locationListener;
    ParseGeoPoint driverOrigin;
    Location LSK;
    Toast toast;
    LinearLayout linne;
    ArrayList<String> riderLocationText = new ArrayList<String>();
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED) {
                    
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastKnownLocation != null) {
                        LSK = lastKnownLocation;
                        Log.i("Fetch", "2");
                    }
                }
            }
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
    
        refreshButton = (Button)findViewById(R.id.refreshButton);
        refreshButton.setEnabled(false);
        
        
        toast = Toast.makeText(this, "Fetching Ride Requests..", Toast.LENGTH_LONG);
        toast.show();
        
        locationPermissionFunction();
        linne = (LinearLayout)findViewById(R.id.linne);
    
        getAllRiderRequests(LSK);
    }
    
    public void locationPermissionFunction() {
        
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // TODO : VERY IMPORTANT
                    //   Do NOT call it here as this happens EVERY MilliSecond???
                    //   ONLY USE THIS WHEN YOU NEED LIVE
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
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                
                    if (lastKnownLocation != null) {
                        LSK = lastKnownLocation;
                        Log.i("Fetch", "1");
                    }
                }
            }
    }
    
    public void logOutFromDriver(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("deleteHistory", true);
        startActivity(intent);
        Button b = (Button)view;
        b.setEnabled(false);
        finish();
    }
    
    public  void getAllRiderRequests(Location location) {
    
    
        
        if (location != null) {
    
            driverOrigin = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
            ParseQuery<ParseObject> query = ParseQuery.getQuery("request");
            
            query.whereNear("location", driverOrigin);
            query.setLimit(10);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    Log.i("AWS", "Call 1");
                    if (e == null) {
                        
                        riderLocationText.clear();
                        rideLatitudes.clear();
                        rideLongitudes.clear();
                        riderNames.clear();
                        
                        if (objects.size() > 0) {
                            for (ParseObject object : objects) {
                                
                                ParseGeoPoint acquiredLocation = (ParseGeoPoint)object.get("location");
                                
                                Double distanceInKilometers = driverOrigin.distanceInKilometersTo(acquiredLocation);
                                Double distanceOneDecimal = (double) Math.round(distanceInKilometers * 10) / 10;
                                
                                // how to get decimals in java:
                                
                                // Multiply : 13.29 * 10 = 132.9
                                // Round    : 132.9 => 133
                                // Divide   : 13.3
                                
                                rideLatitudes.add(acquiredLocation.getLatitude());
                                rideLongitudes.add(acquiredLocation.getLongitude());
                                riderLocationText.add(distanceOneDecimal.toString() + " Km");
                                riderNames.add(object.getString("username"));
                            }
                            toast.cancel();
                            renderRequests();
                        }
                    }
                }
            });
            
            saveDriverLocation();
            
        }
    }
    
    public void saveDriverLocation() {
        ParseUser.getCurrentUser().put("location", new ParseGeoPoint(LSK.getLatitude(), LSK.getLongitude()));
        ParseUser.getCurrentUser().saveInBackground();
    }
    
    public void renderRequests() {
    
        Log.i("basb", Arrays.toString(riderLocationText.toArray()));
        // Setting up parameters of linne to be inherited by textView
        LinearLayout.LayoutParams layparamLinear = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        
        // this is for each individual item, not linne herself
        layparamLinear.setMargins(30, 10,30, 10);
        
        if (riderLocationText.size() > 0) {
            for (int i = 0; i < 10; i++) {
                TextView requestTextView = new TextView(this);
                requestTextView.setLayoutParams(layparamLinear);
                
                if (i < riderLocationText.size()) {
                    requestTextView.setText(riderLocationText.get(i));
                    initializeTextView(requestTextView, i);
                }
            }
        }
    }
    
    public void initializeTextView(TextView view, int i) {
        view.setMaxLines(1);
        view.setTextColor(Color.WHITE);
        view.setBackgroundColor(Color.rgb(59, 59, 59));
        view.setTextSize(2, 20);
        view.setPadding(20,10, 10, 10);
        view.setEllipsize(TextUtils.TruncateAt.END);
    
        
        try {
            final double lat = rideLatitudes.get(i);
            final double lng = rideLongitudes.get(i);
            final String riderName = riderNames.get(i);
            
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), DriverMapsActivity.class);
                    
                    intent.putExtra("reqLat", lat);
                    intent.putExtra("reqLng", lng);
                    
                    intent.putExtra("driverLat", LSK.getLatitude());
                    intent.putExtra("driverLng", LSK.getLongitude());
                    
                    intent.putExtra("username", riderName);
                    startActivity(intent);
                    
                    // im not gonna finish this because i want to back of the map
                    // finish();
                }
            });
        } catch (Exception e) {
            Log.i("No Values", "Error Occured");
        }
        linne.addView(view);
    }
}









// unused
// DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        final int screenHeight = displayMetrics.heightPixels;
