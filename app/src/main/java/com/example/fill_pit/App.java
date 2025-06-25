package com.example.fill_pit;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class App extends Application {

    /**
     * Initializes the Cloudinary MediaManager with the necessary configuration.
     * Replace the placeholders with your actual Cloudinary credentials.
     */

    @Override
    public void onCreate() {
        super.onCreate();
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", ""); // Replace with your Cloudinary cloud name
        config.put("api_key", "");  // Replace with your Cloudinary API key
        config.put("api_secret", "");  // Replace with your Cloudinary API secret
        MediaManager.init(this, config);
    }
}