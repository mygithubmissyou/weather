package com.example.weather;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

public class LauncherActivity extends AppCompatActivity {

    private final int START_LAUNCHER=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_launcher);
        myhandler.sendEmptyMessageDelayed(START_LAUNCHER,2000);
    }
    public Handler myhandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case START_LAUNCHER:
                    Intent intent=new Intent(LauncherActivity.this,WeatherActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                default:
                    break;
            }
        }
    };
}
