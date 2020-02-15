package com.midhun.myweather;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

public class Splash_screen extends AppCompatActivity {

    ImageView iv_imagview;
    TextView tv_version;

    private final int SPLASH_DISPLAY_LENGTH = 3000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        iv_imagview = (ImageView) findViewById(R.id.iv_imagview);

        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(iv_imagview);
        Glide.with(getApplicationContext()).load(R.raw.sunmoon).into(imageViewTarget);

        tv_version = (TextView) findViewById(R.id.tv_version);

        PackageManager manager = getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version_pack = info.versionName;

       // tv_version.setText("V " + version_pack);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(getApplicationContext(), WeatherActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }


}


