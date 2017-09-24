package com.example.weather;

import android.app.ProgressDialog;
import android.content.Intent;
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

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.example.weather.db.City;
import com.example.weather.db.Country;
import com.example.weather.db.DailyForecast;
import com.example.weather.db.Province;
import com.example.weather.db.Weather;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.ProgressDialogUtil;
import com.example.weather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

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
    private Button btn_map;
    private  Button btn_setting;
    private LocationClient locationClient;
    private String province;
    private String city;
    private String country;

    private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;

    private Province selectedProvince;
    private City selectedCity;
    public String weatherid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>21){//与状态栏融为一体
            View view=getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        //百度地图初始化
        locationClient=new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                province=bdLocation.getProvince();
                province=province.substring(0,province.length()-1);
                city=bdLocation.getCity();
                city=city.substring(0,city.length()-1);
                country=bdLocation.getDistrict();
                country=country.substring(0,country.length()-1);
            }
        });
        initLocationOption();
        locationClient.start();
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
        //点击菜单弹出设置
        btn_setting=(Button)findViewById(R.id.btn_setting);
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout_drawer.openDrawer(GravityCompat.END);
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
        //百度定位更新天气

        btn_map=(Button)findViewById(R.id.btn_map);
        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(WeatherActivity.this,csdm,Toast.LENGTH_SHORT).show();
//                ProgressDialogUtil.showProgressDialog(WeatherActivity.this,"","正在加载");
                swipe_refresh.setRefreshing(true);
                weatherid=getWeatherId(province,city,country);
                requestWeatherInfo(weatherid);
//                ProgressDialogUtil.closeProgessDialog();
            }
        });

    }
    //初始化百度地图参数
    private void initLocationOption(){
        LocationClientOption clientOption=new LocationClientOption();
        clientOption.setTimeOut(10000);
        clientOption.setIsNeedAddress(true);
        clientOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        locationClient.setLocOption(clientOption);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.stop();
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
        if(weather!=null&&"ok".equals(weather.status)){
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
            Intent intent=new Intent(this,AutoUpdateService.class);
            startService(intent);
        }else {
            Toast.makeText(this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
        }
    }
    //查询weatherid
    public String getWeatherId(final String province,final String city,final String country){
//        showProgressDialog();

        selectedProvince=new Province();
        provinceList= DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            for(Province p:provinceList){
                if(p.getProvinceName().equals(province)){
                    selectedProvince=p;
                    break;
                }
            }
        }else{
            String url="http://guolin.tech/api/china";
            HttpUtil.sendRequest(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    boolean result=Utility.parseProvinceJson(response.body().string());
                    if(result){
                        provinceList=DataSupport.findAll(Province.class);
                        for(Province p:provinceList){
                            if(p.getProvinceName().equals(province)){
                                selectedProvince=p;
                                break;
                            }
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"请求数据失败",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        selectedCity=new City();
        cityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            for(City c:cityList){
                if(c.getCityName().equals(city)){
                    selectedCity=c;
                    break;
                }
            }
        }else{
            String url="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode();
            HttpUtil.sendRequest(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    boolean result=Utility.parseCityJson(response.body().string(),selectedProvince.getId());
                    if(result){
                        cityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
                        for(City c:cityList){
                            if(c.getCityName().equals(city)){
                                selectedCity=c;
                                break;
                            }
                        }
                    }else {
                        Toast.makeText(getApplicationContext(),"请求数据失败",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        countryList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(Country.class);
        if(countryList.size()>0){
            for(Country c:countryList){
                if(c.getCountryName().equals(country)){
                    return c.getWeatherId();
                }
            }
        }else{
            String url="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode()+"/"+selectedCity.getCityCode();
            HttpUtil.sendRequest(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
//                    closeProgressDialog();
                    ProgressDialogUtil.closeProgessDialog();
                    Toast.makeText(getApplicationContext(),"加载失败...",Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    boolean result=Utility.parseCountryJson(response.body().string(),selectedCity.getId());
                    if(result){
                        countryList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(Country.class);
                        if(countryList.size()>0){
                            for(Country c:countryList){
                                if(c.getCountryName().equals(country)){
                                    weatherid= c.getWeatherId();
                                    break;
                                }
                            }
                        }
                    }
                }
            });
        }
//        closeProgressDialog();

        return weatherid;
    }
}
