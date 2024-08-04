package com.example.appintrosliders.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.appintrosliders.R;

/**
 * this class sets an animation when users first enter the app
 * or if the clicked the icon on the action bar.
 */
public class SplashScreen extends AppCompatActivity  {
    private static int splash_screen=3100;
ImageView blue,green,tri;
Animation topAnim,bottomAnim,triangle,exit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
blue=findViewById(R.id.blueiconImgSpSc);
green=findViewById(R.id.greeniconImgSpSc);
tri=findViewById(R.id.triangleiconImgSpSc);

topAnim= AnimationUtils.loadAnimation(this,R.anim.top_animation);
bottomAnim= AnimationUtils.loadAnimation(this,R.anim.bottom_animation);
triangle=AnimationUtils.loadAnimation(this,R.anim.triangle);
exit=AnimationUtils.loadAnimation(this,R.anim.exit_anim);

        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(SplashScreen.this, MainMenu.class);
                startActivity(intent);
                finish();
            }
        },splash_screen);
blue.setAnimation(topAnim);
green.setAnimation(bottomAnim);
tri.setAnimation(triangle);
        Handler animHandler=new Handler();
        animHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                blue.setAnimation(exit);
                green.setAnimation(exit);
                tri.setAnimation(exit);
            }
        },2500);


    }



}

