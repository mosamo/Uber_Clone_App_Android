package com.example.uber_clone_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {
    
    String type = "none";
    
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        Intent intent = getIntent();
        // logout
        ParseUser.getCurrentUser().logOut();
        if (intent.getBooleanExtra("deleteHistory", false) == true) {
            ParseUser.getCurrentUser().logOut();
        }
        
        ParseCheckUser();
        
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    
    }
    
    public void startService(View view) {
    
        Button b = (Button)view;
        b.setEnabled(false);
        
        Switch userType = findViewById(R.id.userType);
        
        String userTypeText = "rider";
        
        if (userType.isChecked()) {
            userTypeText = "driver";
        }
        
        ParseUser.getCurrentUser().put("rider_driver", userTypeText);
        ParseUser.getCurrentUser().saveInBackground();
        
        if (userType.isChecked()) {
            Intent intent = new Intent(this, DriverActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, RiderActivity.class);
            startActivity(intent);
            finish();
        }
    }
    
    public void ParseCheckUser() {
    
        if (ParseUser.getCurrentUser() == null) {
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                
                    if (e == null) {
                    
                        Log.i("ParseLogIn", "Anonymous Logged in Successfully");
                    
                        try {
                            type = ParseUser.getCurrentUser().get("rider_driver").toString();
                        } catch (NullPointerException ex) {
                            // new users will not have a rider_driver
                        }
                    
                    } else {
                    
                        Log.i("ParseLogIn", "Anonymous Logged in Failed");
                    
                    }
                
                }
            });
        } else { // if there is a user
        
            // we must get the user type (driver/rider) if a user exists
        
            try {
                type = ParseUser.getCurrentUser().get("rider_driver").toString();
            } catch (Exception e) {
                ParseUser.getCurrentUser().logOut();
                ParseCheckUser();
            }
            if (type.equals("rider")) {
            
                // Redirect to Second Activity
            
                Intent intent = new Intent(getApplicationContext(), RiderActivity.class);
                startActivity(intent);
                finish();
            
            
            } else if (type.equals("driver")) {
            
                // Redirect to Driver Activity
            
                Intent intent = new Intent(getApplicationContext(), DriverActivity.class);
                startActivity(intent);
                finish();
            
            
            } else {
            
                //  if it doesn't equal anything keep him on this activity so he can choose
            
            }
        }
    }
}
