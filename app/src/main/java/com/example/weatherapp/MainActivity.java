package com.example.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity{

    Animation top,bottom;
    ImageView logo;
    TextView name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        top = AnimationUtils.loadAnimation(this,R.anim.topanimation);
        bottom = AnimationUtils.loadAnimation(this,R.anim.bottomanimation);

        logo = findViewById(R.id.logoImageView);
        name = findViewById(R.id.nameTextView);

        logo.setAnimation(top);
        name.setAnimation(bottom);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this,WeatherScreen.class);
                startActivity(intent);
                finish();
            }
        },4000);
    }


}