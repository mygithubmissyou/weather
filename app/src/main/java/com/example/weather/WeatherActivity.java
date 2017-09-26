package com.example.weather;

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
import com.example.weather.db.City;
import com.example.weather.db.Country;
import com.example.weather.db.DailyForecast;
import com.example.weather.db.HourlyForecast;
import com.example.weather.db.Province;
import com.example.weather.db.Rain;
import com.example.weather.db.Snow;
import com.example.weather.db.Weather;
import com.example.weather.service.AutoUpdateService;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.ProgressDialogUtil;
import com.example.weather.util.Utility;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import layout.WeatherView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private WeatherView weatherView;
    private LineChart line_chart;
    private TextView txt_degree_range;
    private ImageView img_status_icon;
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
        //加载天气背景图
        weatherView=(WeatherView)findViewById(R.id.weather_view);
//        weatherView.setWeatherType(new Rain(this,R.drawable.rain_sky_day));
//        weatherView.setWeatherType(new Snow(this,R.drawable.rain_sky_day));

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
//        img_biying=(ImageView)findViewById(R.id.img_biying);
        swipe_refresh=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        btn_select=(Button)findViewById(R.id.btn_select);
        layout_drawer=(DrawerLayout)findViewById(R.id.layout_drawer);
        img_status_icon=(ImageView)findViewById(R.id.img_status_icon);
        //小时天气数据
        line_chart=(LineChart)findViewById(R.id.line_chart);

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
        //请求必应图片
//        String biying_pic=sharedPreferences.getString("biying",null);
//        if(biying_pic!=null){
//            Glide.with(this).load(biying_pic).into(img_biying);
//        }else{
//            requestBiyingPic();
//        }
        String weather_info=sharedPreferences.getString("weather",null);

        if(weather_info!=null){
            Weather weather= Utility.parseWeatherJson(weather_info);
            weatherid=weather.basic.id;
            showWeatherInfo(weather);
        }else{
             weatherid=getIntent().getStringExtra("weather_id");
            layout_weather.setVisibility(View.INVISIBLE);
            requestWeatherInfo(weatherid);
            //第一次进入开启自动更新
            AutoUpdateService.is_auto=true;
            Intent intent=new Intent(this,AutoUpdateService.class);
            startService(intent);
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

                getWeatherIdAndRequest(province,city,country);
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

    @Override
    protected void onPause() {
        super.onPause();
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
                        ProgressDialogUtil.closeProgessDialog();
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
                        ProgressDialogUtil.closeProgessDialog();
                    }
                });

            }
        });
    }
    //为控件填充内容
    private void showWeatherInfo(Weather weather){
        if(weather!=null&&"ok".equals(weather.status)){
            //改变背景
            changeWeatherBg(weather.now.cond.txt);
            title_city.setText(weather.basic.city);
            title_updatetime.setText(weather.basic.update.loc.split(" ")[1]);
            txt_weather_info.setText(weather.now.cond.txt);
            txt_degree.setText(weather.now.tmp+"℃");
            img_status_icon.setImageResource(getResources().getIdentifier("h"+weather.now.cond.code,"drawable","com.example.weather"));
        //初始化小时数据
            ArrayList<String> array_y=new ArrayList<>();
            ArrayList<String> array_x=new ArrayList<>();
            SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Calendar calendar=Calendar.getInstance();
            for(HourlyForecast h:weather.hourly_forecast){
                array_y.add(h.tmp);
                try {
                    array_x.add(format.parse(h.date).getHours()+":00\n"+h.cond.txt);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            initHourForecast(array_x,array_y);
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
            String todaydate= new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
            if(todaydate.equals(d.date)){
                txt_degree_range=(TextView)findViewById(R.id.txt_degree_range);
                txt_degree_range.setText(d.tmp.min+"℃~"+d.tmp.max+"℃");
            }else{
                txt_forcast_item_date=(TextView)view.findViewById(R.id.txt_forcast_item_date);
                txt_forcast_item_info=(TextView)view.findViewById(R.id.txt_forcast_item_info);
                txt_forcast_item_max=(TextView)view.findViewById(R.id.txt_forcast_item_max);
                txt_forcast_item_min=(TextView)view.findViewById(R.id.txt_forcast_item_min);
                txt_forcast_item_date.setText(d.date);
                txt_forcast_item_info.setText(d.wind.dir);
                txt_forcast_item_max.setText(d.tmp.max+"℃");
                txt_forcast_item_min.setText(d.tmp.min+"℃");
                layout_forecast.addView(view);
            }
        }
        layout_weather.setVisibility(View.VISIBLE);
        }else {
            Toast.makeText(this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
        }
    }
    //根据天气设置背景
    private void changeWeatherBg(String txt) {
        if(txt!=null&&!"".equals(txt)){
            Calendar calendar=Calendar.getInstance();
            int hour=calendar.get(Calendar.HOUR_OF_DAY);
            if(hour>6&&hour<18){
                if(txt.contains("小雨")){
                    weatherView.setBackgroundResource(0);
                    weatherView.setWeatherType(Rain.getInstance(this,R.drawable.background_for_light_rain));
                }else if(txt.contains("雪")){
                    weatherView.setBackgroundResource(0);
                    weatherView.setWeatherType(Snow.getInstance(this,R.drawable.background_for_snow));
                }else if(txt.contains("阴")){
                    weatherView.setBackgroundResource(R.drawable.background_for_overcast);
                }else if(txt.contains("大雨")){
                    weatherView.setBackgroundResource(0);
                    weatherView.setWeatherType(Rain.getInstance(this,R.drawable.background_for_heavy_rain));
                }else if(txt.contains("雾")){
                    weatherView.setBackgroundResource(R.drawable.background_for_fog);
                }else if(txt.contains("雷")){
                    weatherView.setBackgroundResource(R.drawable.background_for_thunderstorm);
                }else if(txt.contains("沙")){
                    weatherView.setBackgroundResource(R.drawable.background_for_dust_storm);
                }else if(txt.contains("阵雨")){
//                    weatherView.setBackgroundResource(R.drawable.background_for_shower);
                    weatherView.setBackgroundResource(0);
                    weatherView.setWeatherType(Rain.getInstance(this,R.drawable.background_for_shower));
                }else if(txt.contains("晴")){
                    weatherView.setBackgroundResource(R.drawable.background_for_sun);
                }else if(txt.contains("多云")){
                    weatherView.setBackgroundResource(R.drawable.background_for_cloud);
                }else{
                    weatherView.setBackgroundResource(R.drawable.background_for_default);
                }
            }else{
                if(txt.contains("小雨")){
                    weatherView.setBackgroundResource(0);
                    weatherView.setWeatherType(Rain.getInstance(this,R.drawable.background_for_light_rain_night));
                }else if(txt.contains("雪")){
                    weatherView.setBackgroundResource(0);
                    weatherView.setWeatherType(Snow.getInstance(this,R.drawable.background_for_snow_night));
                }else if(txt.contains("阴")){
                    weatherView.setBackgroundResource(R.drawable.background_for_overcast_night);
                }else if(txt.contains("大雨")){
                    weatherView.setBackgroundResource(0);
                    weatherView.setWeatherType(Rain.getInstance(this,R.drawable.background_for_heavy_rain_night));
                }else if(txt.contains("雾")){
                    weatherView.setBackgroundResource(R.drawable.background_for_fog_night);
                }else if(txt.contains("雷")){
                    weatherView.setBackgroundResource(R.drawable.background_for_thunderstorm_night);
                }else if(txt.contains("沙")){
                    weatherView.setBackgroundResource(R.drawable.background_for_dust_storm_night);
                }else if(txt.contains("阵雨")){
                    weatherView.setBackgroundResource(0);
                    weatherView.setWeatherType(Rain.getInstance(this,R.drawable.background_for_shower_night));
                }else if(txt.contains("晴")){
                    weatherView.setBackgroundResource(R.drawable.background_for_star_night);
                }else if(txt.contains("多云")){
                    weatherView.setBackgroundResource(R.drawable.background_for_cloud_night);
                }else{
                    weatherView.setBackgroundResource(R.drawable.background_for_default);
                }
            }
        }
    }

    //初始化小时折线图
    private void initHourForecast(ArrayList<String> array_x,ArrayList<String> array_y) {
        line_chart.clear();
        line_chart.setTouchEnabled(false);
        line_chart.setDragEnabled(true);
        line_chart.setDrawGridBackground(false);
        line_chart.setScaleYEnabled(false);
        line_chart.setScaleXEnabled(false);
        line_chart.setExtraLeftOffset(55f);
        line_chart.setExtraRightOffset(55f);
        line_chart.setDescription(null);//右下角文本值

        YAxis yaxis_left=line_chart.getAxisLeft();
        yaxis_left.setLabelCount(array_x.size(),true);//显示Y轴有几个值
        yaxis_left.setEnabled(false);

        YAxis yaxis_right=line_chart.getAxisRight();
        yaxis_right.setEnabled(false);


        XAxis xaxis=line_chart.getXAxis();
        xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xaxis.setLabelCount(array_x.size(),true);//显示X轴有几个值
        xaxis.setTextColor(Color.WHITE);
        xaxis.setTextSize(12f);
        xaxis.setDrawAxisLine(true);
        xaxis.setGridColor(Color.WHITE);
        xaxis.setAxisLineColor(Color.WHITE);
        xaxis.setDrawLabels(true);
        xaxis.setDrawGridLines(false);

        final Map<Integer,String> xmap=new HashMap<>();
        for(int i=0;i<array_x.size();i++){
            xmap.put(i,array_x.get(i));
        }
        xaxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xmap.get((int)value);
            }
        });
        Legend l=line_chart.getLegend();
        l.setTextSize(12f);
        l.setTextColor(Color.WHITE);
        l.setForm(Legend.LegendForm.LINE);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);

        List<Entry> entrydata=new ArrayList<>();
        for(int i=0;i<array_y.size();i++){
            entrydata.add(new Entry(i,Integer.valueOf(array_y.get(i))));
        }
        LineDataSet dataset1=new LineDataSet(entrydata,"");
        dataset1.setLineWidth(3.5f);
        dataset1.setColor(Color.WHITE);
        dataset1.setDrawCircles(true);
        dataset1.setCircleColor(Color.BLUE);
        dataset1.setCircleRadius(4f);
//        dataset1.setHighLightColor(Color.rgb(244, 117, 117));
        dataset1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataset1.setLabel("");
        LineData linedata=new LineData(dataset1);
        linedata.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return (int)value+"°";
            }
        });
        linedata.setValueTextColor(Color.WHITE);
        linedata.setValueTextSize(15f);

        line_chart.setData(linedata);
        line_chart.notifyDataSetChanged();
    }

    //查询weatherid
    public void getWeatherIdAndRequest(final String province,final String city,final String country){
        ProgressDialogUtil.showProgressDialog(WeatherActivity.this,"","正在加载...");
        selectedProvince=new Province();
        provinceList= DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            for(Province p:provinceList){
                if(p.getProvinceName().equals(province)){
                    selectedProvince=p;
                    selectedCity=new City();
                    cityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
                    if(cityList.size()>0){
                        for(City c:cityList){
                            if(c.getCityName().equals(city)){
                                selectedCity=c;
                                countryList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(Country.class);
                                if(countryList.size()>0){
                                    for(Country c1:countryList){
                                        if(c1.getCountryName().equals(country)){
                                            weatherid=c1.getWeatherId();
                                            requestWeatherInfo(c1.getWeatherId());
                                        }
                                    }
                                }else{
                                    //发送一次请求
                                    String url = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode() + "/" + selectedCity.getCityCode();
                                    HttpUtil.sendRequest(url, new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            ProgressDialogUtil.closeProgessDialog();
                                        }
                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            boolean result=Utility.parseCountryJson(response.body().string(),selectedCity.getId());
                                            if(result){
                                                countryList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(Country.class);
                                                if(countryList.size()>0){
                                                    for(Country c:countryList){
                                                        if(c.getCountryName().equals(country)){
                                                            weatherid=c.getWeatherId();
                                                            requestWeatherInfo(c.getWeatherId());
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }else{
                        //发送两次请求
                        String url="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode();
                        HttpUtil.sendRequest(url, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                ProgressDialogUtil.closeProgessDialog();
                            }
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                boolean result=Utility.parseCityJson(response.body().string(),selectedProvince.getId());
                                if(result) {
                                    cityList = DataSupport.where("provinceid=?", String.valueOf(selectedProvince.getId())).find(City.class);
                                    for (City c : cityList) {
                                        if (c.getCityName().equals(city)) {
                                            selectedCity = c;
                                            String url = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode() + "/" + selectedCity.getCityCode();
                                            HttpUtil.sendRequest(url, new Callback() {
                                                @Override
                                                public void onFailure(Call call, IOException e) {
                                                    ProgressDialogUtil.closeProgessDialog();
                                                }
                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    boolean result=Utility.parseCountryJson(response.body().string(),selectedCity.getId());
                                                    if(result){
                                                        countryList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(Country.class);
                                                        if(countryList.size()>0){
                                                            for(Country c:countryList){
                                                                if(c.getCountryName().equals(country)){
                                                                    weatherid=c.getWeatherId();
                                                                    requestWeatherInfo(c.getWeatherId());
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }else{
            //发送三次请求
            String url="http://guolin.tech/api/china";
            HttpUtil.sendRequest(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    ProgressDialogUtil.closeProgessDialog();
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    boolean result=Utility.parseProvinceJson(response.body().string());
                    if(result){
                        provinceList=DataSupport.findAll(Province.class);
                        for(Province p:provinceList){
                            if(p.getProvinceName().equals(province)){
                                selectedProvince=p;
                                selectedCity=new City();
                                String url="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode();
                                HttpUtil.sendRequest(url, new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        ProgressDialogUtil.closeProgessDialog();
                                    }
                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        boolean result=Utility.parseCityJson(response.body().string(),selectedProvince.getId());
                                        if(result){
                                            cityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
                                            for(City c:cityList){
                                                if(c.getCityName().equals(city)){
                                                    selectedCity=c;
                                                    String url="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode()+"/"+selectedCity.getCityCode();
                                                    HttpUtil.sendRequest(url, new Callback() {
                                                        @Override
                                                        public void onFailure(Call call, IOException e) {
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
                                                                            weatherid=c.getWeatherId();
                                                                            requestWeatherInfo(c.getWeatherId());
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }else {
                                            Toast.makeText(getApplicationContext(),"请求数据失败",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"请求数据失败",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
