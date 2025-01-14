package com.quangtruong.appbanlinhkien;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        configCloudinary();
    }

    private void configCloudinary() {
        Map config = new HashMap();
        config.put("cloud_name", "desqhqdlt");
        config.put("api_key", "889824668862124");
        config.put("api_secret", "KA3Xsvaql5Sph1sQs30kRc5v1KM");
        MediaManager.init(this, config);
    }
}