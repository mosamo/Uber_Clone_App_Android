package com.example.uber_clone_app;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

import android.app.Application;


public class ParseSetting extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
        
                // change the following to your own server
                .applicationId(getResources().getString(R.string.app_id_parse))
                .clientKey(getResources().getString(R.string.client_key_parse))
                .server(getResources().getString(R.string.server_parse))
                .build()
                
        );
    
        // ParseUser.enableAutomaticUser(); // Allows Guests (Auto Creation?)
        // AnonymousUser =/= AutomaticUser
        
        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }
}