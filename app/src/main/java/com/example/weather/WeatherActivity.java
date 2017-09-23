package com.example.weather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.example.weather.db.DailyForecast;
import com.example.weather.db.Weather;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private TextView txt_comfort;
    private TextView txt_carwash;
    private TextView txt_sport;
    private TextView txt_aqi;
    private TextView txt_pm25;
    private TextView txt_forcast_item_date;
    private TextView txt_forcast_item_info;
    private TextView txt_forcast_item_max;
    private TextView txt_forcast_item_min;
    private TextView txt_degree;
    private TextView txt_weather_info;
    private TextView title_city;
    private TextView title_updatetime;
    private LinearLayout layout_forecast;
    private ScrollView layout_weather;
    private ImageView img_biying;
    public SwipeRefreshLayout swipe_refresh;
    private Button btn_select;
    public DrawerLayout layout_drawer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>21){//与状态栏融为一体
            View view=getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        title_city=(TextView)findViewById(R.id.title_city);
        title_updatetime=(TextView)findViewById(R.id.title_updatetime);
        layout_forecast=(LinearLayout)findViewById(R.id.layout_forecast);
        layout_weather=(ScrollView)findViewById(R.id.layout_weather);
        txt_weather_info=(TextView)findViewById(R.id.txt_weather_info);
        txt_degree=(TextView)findViewById(R.id.txt_degree);
        txt_pm25=(TextView)findViewById(R.id.txt_pm25);
        txt_aqi=(TextView)findViewById(R.id.txt_aqi);
        txt_comfort=(TextView)findViewById(R.id.txt_comfort);
        txt_carwash=(TextView)findViewById(R.id.txt_carwash);
        txt_sport=(TextView)findViewById(R.id.txt_sport);
        img_biying=(ImageView)findViewById(R.id.img_biying);
        swipe_refresh=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        btn_select=(Button)findViewById(R.id.btn_select);
        layout_drawer=(DrawerLayout)findViewById(R.id.layout_drawer);

        //点击按钮弹出省市区选择
        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout_drawer.openDrawer(GravityCompat.START);
            }
        });
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        String biying_pic=sharedPreferences.getString("biying",null);
        if(biying_pic!=null){
            Glide.with(this).load(biying_pic).into(img_biying);
        }else{
            requestBiyingPic();
        }
        String weather_info=sharedPreferences.getString("weather",null);
        final String weatherid;
        if(weather_info!=null){
            Weather weather= Utility.parseWeatherJson(weather_info);
            weatherid=weather.basic.id;
            showWeatherInfo(weather);
        }else{
             weatherid=getIntent().getStringExtra("weather_id");
            layout_weather.setVisibility(View.INVISIBLE);
            requestWeatherInfo(weatherid);
        }
        //下拉刷新数据
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary);
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeatherInfo(weatherid);
            }
        });
    }
    //请求必应图片
    private void requestBiyingPic(){
        String url="http://guolin.tech/api/bing_pic";
        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respnsestr=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("biying",respnsestr);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(respnsestr).into(img_biying);
                    }
                });
            }
        });
    }
    //发起网络请求天气信息
    public void requestWeatherInfo(String weatherid){
        String url="https://free-api.heweather.com/v5/weather?city="+weatherid+"&key=f5968574fabe49419386819194fca95d";
//        String url="http://guolin.tech/api/weather?cityid="+weatherid+"&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipe_refresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responsetxt=response.body().string();
                final Weather weather=Utility.parseWeatherJson(responsetxt);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responsetxt);
                            editor.apply();
                            showWeatherInfo(weather);

                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipe_refresh.setRefreshing(false);
                    }
                });

            }
        });
    }
    //为控件填充内容
    private void showWeatherInfo(Weather weather){
        title_city.setText(weather.basic.city);
        title_updatetime.setText(weather.basic.update.loc.split(" ")[1]);
        txt_weather_info.setText(weather.now.cond.txt);
        txt_degree.setText(weather.now.tmp+"℃");
        if(weather.aqi!=null){
             txt_pm25.setText(weather.aqi.city.pm25);
             txt_aqi.setText(weather.aqi.city.aqi);
        }
        txt_comfort.setText("舒适度:"+weather.suggestion.comf.brf+"\n"+weather.suggestion.comf.txt);
        txt_carwash.setText("洗车指数:"+weather.suggestion.cw.brf+"\n"+weather.suggestion.cw.txt);
        txt_sport.setText("运动指数:"+weather.suggestion.sport.brf+"\n"+weather.suggestion.sport.txt);
        layout_forecast.removeAllViews();
        for(DailyForecast d:weather.daily_forecast){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,layout_forecast,false);
            txt_forcast_item_date=(TextView)view.findViewById(R.id.txt_forcast_item_date);
            txt_forcast_item_info=(TextView)view.findViewById(R.id.txt_forcast_item_info);
            txt_forcast_item_max=(TextView)view.findViewById(R.id.txt_forcast_item_max);
            txt_forcast_item_min=(TextView)view.findViewById(R.id.txt_forcast_item_min);
            txt_forcast_item_date.setText(d.date);
            txt_forcast_item_info.setText(d.wind.dir);
            txt_forcast_item_max.setText(d.tmp.max);
            txt_forcast_item_min.setText(d.tmp.min);
            layout_forecast.addView(view);
        }
        layout_weather.setVisibility(View.VISIBLE);
    }
}
