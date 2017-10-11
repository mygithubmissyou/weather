package com.example.weather;


import android.Manifest;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.graphics.Color;

import android.graphics.drawable.Animatable;

import android.location.LocationManager;
import android.os.Build;

import android.preference.PreferenceManager;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;

import com.example.weather.db.DailyForecast;
import com.example.weather.db.HourlyForecast;

import com.example.weather.db.Rain;

import com.example.weather.db.Weather;

import com.example.weather.util.DBUtils;
import com.example.weather.util.GetWeatherBG;
import com.example.weather.util.GetWeatherByLocation;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.ProgressDialogUtil;
import com.example.weather.util.Utility;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import layout.LineChartView;
import layout.WeatherView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private RelativeLayout line_chart_layout;
    //    private WeatherView weatherView;
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
    private Button btn_setting;
    private LocationClient locationClient;
    private String province = "北京";
    private String city = "北京";
    private String country = "北京";
    public static String weatherid = "CN101010100";
    private FrameLayout layout_main;
    private FrameLayout cloud_layout;
    private String weather_condition;
    private BDAbstractLocationListener mylistener=new myListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new DBUtils(getApplicationInfo().packageName, getApplicationContext());
        if (Build.VERSION.SDK_INT > 21) {//与状态栏融为一体
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        //百度地图初始化
        locationClient = new LocationClient(getApplicationContext());
        //动态获取权限
        getPermission();
        locationClient.registerLocationListener(mylistener);

        locationClient.start();
        setContentView(R.layout.activity_weather);

        //折线layout
        line_chart_layout = (RelativeLayout) findViewById(R.id.line_chart_layout);
        //加载天气背景图
//        weatherView=(WeatherView)findViewById(R.id.weather_view);
//        weatherView.setBackgroundResource(R.drawable.background_for_default);
//        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        layout_main = (FrameLayout) findViewById(R.id.layout_main);
        layout_main.setBackgroundResource(R.drawable.background_for_default);
        cloud_layout = (FrameLayout) findViewById(R.id.cloud_layout);

        img_biying = (ImageView) findViewById(R.id.img_biying);
        title_city = (TextView) findViewById(R.id.title_city);
        title_updatetime = (TextView) findViewById(R.id.title_updatetime);
        layout_forecast = (LinearLayout) findViewById(R.id.layout_forecast);
        layout_weather = (ScrollView) findViewById(R.id.layout_weather);
        txt_weather_info = (TextView) findViewById(R.id.txt_weather_info);
        txt_degree = (TextView) findViewById(R.id.txt_degree);
        txt_pm25 = (TextView) findViewById(R.id.txt_pm25);
        txt_aqi = (TextView) findViewById(R.id.txt_aqi);
        txt_comfort = (TextView) findViewById(R.id.txt_comfort);
        txt_carwash = (TextView) findViewById(R.id.txt_carwash);
        txt_sport = (TextView) findViewById(R.id.txt_sport);
//        img_biying=(ImageView)findViewById(R.id.img_biying);
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        btn_select = (Button) findViewById(R.id.btn_select);
        layout_drawer = (DrawerLayout) findViewById(R.id.layout_drawer);
        img_status_icon = (ImageView) findViewById(R.id.img_status_icon);
        //小时天气数据
//        line_chart=(LineChart)findViewById(R.id.line_chart);

        //点击按钮弹出省市区选择
        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout_drawer.openDrawer(GravityCompat.START);
            }
        });
        //点击菜单弹出设置
        btn_setting = (Button) findViewById(R.id.btn_setting);
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout_drawer.openDrawer(GravityCompat.END);
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //请求必应图片
//        String biying_pic=sharedPreferences.getString("biying",null);
//        if(biying_pic!=null){
//            Glide.with(this).load(biying_pic).into(img_biying);
//        }else{
//            requestBiyingPic();
//        }
        String weather_info = sharedPreferences.getString("weather", null);
        if (weather_info != null) {
            Weather weather = Utility.parseWeatherJson(weather_info);
            weatherid = weather.basic.id;
            showWeatherInfo(weather);
        } else {
            weatherid = getIntent().getStringExtra("weather_id");
            if (weatherid != null && !"".equals(weatherid)) {
                layout_weather.setVisibility(View.INVISIBLE);
                requestWeatherInfo(weatherid);
            } else {
                weatherid = "CN101010100";
                requestWeatherInfo(weatherid);
            }
            //第一次进入开启自动更新
//            AutoUpdateService.is_auto=true;
//            Intent intent=new Intent(this,AutoUpdateService.class);
//            startService(intent);
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

        btn_map = (Button) findViewById(R.id.btn_map);
        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                locationClient.requestLocation();
                GetWeatherByLocation.getWeatherIdAndRequest(province, city, country, WeatherActivity.this);
//                getWeatherIdAndRequest(province,city,country);
            }
        });
        requestWeatherInfo(weatherid);

    }
//百度定位监听器
    class myListener extends BDAbstractLocationListener{
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        Log.d("ssss","sss"+bdLocation.getLocType());
        province = bdLocation.getProvince();
        province = province.substring(0, province.length() - 1);
        city = bdLocation.getCity();
        city = city.substring(0, city.length() - 1);
        country = bdLocation.getDistrict();
        country = country.substring(0, country.length() - 1);
        GetWeatherByLocation.getWeatherIdAndRequest(province, city, country, WeatherActivity.this);
//                getWeatherIdAndRequest(province,city,country);
    }
}
    //请求权限
    private void getPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //检查gps是否开启
            LocationManager locationmanager= (LocationManager) getSystemService(LOCATION_SERVICE);
            if(!locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                Toast.makeText(this,"请打开GRS定位",Toast.LENGTH_SHORT).show();
            List<String> permissionlist = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionlist.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                permissionlist.add(Manifest.permission.READ_PHONE_STATE);
//            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionlist.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (!permissionlist.isEmpty()) {
                String[] permissionArray = permissionlist.toArray(new String[permissionlist.size()]);
                ActivityCompat.requestPermissions(this, permissionArray, 1);
            }
        }
        initLocationOption();
    }
    //权限请求结果

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length != 0 && requestCode == 1) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "你必须给予定位权限", Toast.LENGTH_SHORT).show();
            }
            initLocationOption();

        } else {
            Toast.makeText(this, "请给予权限", Toast.LENGTH_SHORT).show();
        }
    }

    //初始化百度地图参数
    private void initLocationOption() {
        LocationClientOption clientOption = new LocationClientOption();
        clientOption.setOpenGps(true);
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        initAnimate();
    }

    List<TranslateAnimation> tlist = new ArrayList<>();

    //初始化动画
    public void initAnimate() {
        if (weather_condition != null && !"".equals(weather_condition)) {
            Map<String, Integer> map = GetWeatherBG.getBGResource(weather_condition);

            if (map.get("bg") != null) {
                for (TranslateAnimation t : tlist) {
                    t.cancel();
                }
                cloud_layout.removeAllViews();
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                cloud_layout.setLayoutParams(params);
                layout_main.setBackgroundResource(map.get("bg"));
            }
            if (map.get("animate") != null) {
                Random random = new Random();
                WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

                int width = wm.getDefaultDisplay().getWidth();
                int height = wm.getDefaultDisplay().getHeight();
                if (map.get("animate").equals(R.animator.cloud_animate)) {
                    ImageView img_cloud = new ImageView(this);
                    ImageView img_cloud1 = new ImageView(this);
                    ImageView img_cloud2 = new ImageView(this);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    img_cloud.setLayoutParams(params);
                    img_cloud1.setLayoutParams(params);
                    img_cloud2.setLayoutParams(params);
                    img_cloud.setBackgroundResource(R.drawable.anim_for_cloudy_02);
                    img_cloud1.setBackgroundResource(R.drawable.anim_for_cloudy_04);
                    img_cloud2.setBackgroundResource(R.drawable.anim_for_cloudy_05);
                    cloud_layout.addView(img_cloud);
                    cloud_layout.addView(img_cloud1);
                    cloud_layout.addView(img_cloud2);
                    tlist.add(cloudFly(img_cloud1, -400, 700, 900, -100, 18000));
                    tlist.add(cloudFly(img_cloud, -600, 900, 200, -100, 19000));
                    tlist.add(cloudFly(img_cloud2, -300, 300, 400, -100, 17000));
                } else if (map.get("animate").equals(GetWeatherBG.TYPE_SNOW)) {
                    int speed = map.get("speed");
                    int num = map.get("num");

                    for (int i = 0; i < 20; i++) {
                        ImageView imageView = new ImageView(this);
                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(random.nextInt(20) + num, random.nextInt(20) + num);
                        imageView.setLayoutParams(lp);
                        imageView.setBackgroundResource(R.drawable.anim_snow_big1);
                        tlist.add(cloudFly(imageView, i * width / 20 - 40, i * width / 20 - 40, -80, height, random.nextInt(speed) + 1000));
                        cloud_layout.addView(imageView);
                    }
                } else if (map.get("animate").equals(GetWeatherBG.TYPE_RAIN)) {
//                    int speed = map.get("speed");
//                    int num = map.get("num");
//                    int h = map.get("h");
//                    for (int i = 0; i < num; i++) {
//                        ImageView imageView = new ImageView(this);
//                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(random.nextInt(2) + h, random.nextInt(50) + h);
//                        imageView.setLayoutParams(lp);
//                        imageView.setBackgroundResource(R.drawable.anim_snow_big1);
//                        tlist.add(cloudFly(imageView, i * width / 20 - 50, i * width / 20 - 50, -80, height, random.nextInt(speed) + 1000));
//                        cloud_layout.addView(imageView);
//                    }
                    WeatherView weatherView = WeatherView.getInstance(this, Rain.getInstance(this, R.drawable.background_for_light_rain));
                    cloud_layout.addView(weatherView);
                } else if (map.get("animate").equals(GetWeatherBG.TYPE_SHOWER)) {
//                    int speed = map.get("speed");
//                    int num = map.get("num");
//                    int h = map.get("h");
//
//                    for (int i = 0; i < num; i++) {
//                        ImageView imageView = new ImageView(this);
//                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(random.nextInt(2) + h, random.nextInt(50) + h);
//                        imageView.setLayoutParams(lp);
//                        imageView.setBackgroundResource(R.drawable.anim_snow_big1);
//                        tlist.add(cloudFly(imageView, i * width / 20 + 50, i * width / 20 + 50, -80, height, random.nextInt(speed) + 1000));
//                        cloud_layout.addView(imageView);
//                    }
                    WeatherView weatherView = WeatherView.getInstance(this, Rain.getInstance(this, R.drawable.background_for_light_rain));
                    cloud_layout.addView(weatherView);
                } else if (map.get("animate").equals(GetWeatherBG.TYPE_SUN)) {
                    img_biying.setImageResource(R.drawable.sunshine);
                    Animation ra = AnimationUtils.loadAnimation(this, R.anim.sun_animate);
//                    ra.setRepeatCount(Animation.INFINITE);
                    img_biying.setAnimation(ra);
                    ImageView imageView = new ImageView(this);
                    imageView.setBackgroundResource(R.drawable.fine_day_cloud1);
                    Animation imganimate = AnimationUtils.loadAnimation(this, R.anim.cloud_animate1);
                    imageView.setAnimation(imganimate);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(217, 120);
                    imageView.setLayoutParams(params);
                    cloud_layout.addView(imageView);
                    cloud_layout.setX(0);
                    cloud_layout.setY(500);
                    ra.start();
                    imganimate.start();
                } else {
                    img_biying.setBackgroundResource(map.get("animate"));
                    Animatable animatable = (Animatable) img_biying.getBackground();
                    if (animatable.isRunning())
                        animatable.stop();
                    animatable.start();
                }
            }
        }
    }

    //云彩动画调用
    private TranslateAnimation cloudFly(View view, int fx, int tx, int fy, int ty, int delay) {
        TranslateAnimation ta = new TranslateAnimation(fx, tx, fy, ty);
        ta.setRepeatMode(Animation.RESTART);
        ta.setRepeatCount(Animation.INFINITE);
        ta.setDuration(delay);
        ta.setInterpolator(new LinearInterpolator());
        view.setAnimation(ta);
        return ta;
    }

    //请求必应图片
    private void requestBiyingPic() {
        String url = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respnsestr = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("biying", respnsestr);
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
    public void requestWeatherInfo(String weatherid) {
        this.weatherid = weatherid;
        String url = "https://free-api.heweather.com/v5/weather?city=" + weatherid + "&key=f5968574fabe49419386819194fca95d";
//        String url="http://guolin.tech/api/weather?cityid="+weatherid+"&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        ProgressDialogUtil.closeProgessDialog();
                        swipe_refresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responsetxt = response.body().string();
                final Weather weather = Utility.parseWeatherJson(responsetxt);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responsetxt);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipe_refresh.setRefreshing(false);

                    }
                });

            }
        });
    }

    //为控件填充内容
    private void showWeatherInfo(Weather weather) {
        if (weather != null && "ok".equals(weather.status)) {
//            //改变背景
            weather_condition = weather.now.cond.txt;
            this.initAnimate();
            title_city.setText(weather.basic.city);
            title_updatetime.setText(weather.basic.update.loc.split(" ")[1]);
            txt_weather_info.setText(weather.now.cond.txt);
            txt_degree.setText(weather.now.tmp + "℃");
            img_status_icon.setImageResource(getResources().getIdentifier("h" + weather.now.cond.code, "drawable", "com.example.weather"));
//        //初始化小时数据
            ArrayList<Integer> array_y = new ArrayList<>();
            ArrayList<String> array_x = new ArrayList<>();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Calendar calendar = Calendar.getInstance();
            for (HourlyForecast h : weather.hourly_forecast) {
                array_y.add(Integer.parseInt(h.tmp));
                try {
                    array_x.add(format.parse(h.date).getHours() + ":00|" + h.cond.txt);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (array_x.size() > 1 && array_y.size() > 1) {
//                initHourForecast(array_x, array_y);
                line_chart_layout.removeAllViews();
                LineChartView lineChartView = new LineChartView(this);
                LineChartView.setData(array_y, array_x);
                lineChartView.invalidate();
                line_chart_layout.addView(lineChartView);
            }

            if (weather.aqi != null) {
                txt_pm25.setText(weather.aqi.city.pm25);
                txt_aqi.setText(weather.aqi.city.aqi);
            }
            txt_comfort.setText("舒适度:" + weather.suggestion.comf.brf + "\n" + weather.suggestion.comf.txt);
            txt_carwash.setText("洗车指数:" + weather.suggestion.cw.brf + "\n" + weather.suggestion.cw.txt);
            txt_sport.setText("运动指数:" + weather.suggestion.sport.brf + "\n" + weather.suggestion.sport.txt);
            layout_forecast.removeAllViews();
            for (DailyForecast d : weather.daily_forecast) {
                View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, layout_forecast, false);
                String todaydate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
                if (todaydate.equals(d.date)) {
                    txt_degree_range = (TextView) findViewById(R.id.txt_degree_range);
                    txt_degree_range.setText(d.tmp.min + "℃~" + d.tmp.max + "℃");
                } else {
                    txt_forcast_item_date = (TextView) view.findViewById(R.id.txt_forcast_item_date);
                    txt_forcast_item_info = (TextView) view.findViewById(R.id.txt_forcast_item_info);
                    txt_forcast_item_max = (TextView) view.findViewById(R.id.txt_forcast_item_max);
                    txt_forcast_item_min = (TextView) view.findViewById(R.id.txt_forcast_item_min);
                    txt_forcast_item_date.setText(d.date);
                    txt_forcast_item_info.setText(d.wind.dir);
                    txt_forcast_item_max.setText(d.tmp.max + "℃");
                    txt_forcast_item_min.setText(d.tmp.min + "℃");
                    layout_forecast.addView(view);
                }
            }
            layout_weather.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "获取天气信息失败showWeatherInfo", Toast.LENGTH_SHORT).show();
        }
        ProgressDialogUtil.closeProgessDialog();
    }

    //初始化小时折线图
    private void initHourForecast(ArrayList<String> array_x, ArrayList<String> array_y) {

        try {
            line_chart.clear();
            Description desc = new Description();
            desc.setText("无数据,稍后刷新");
            line_chart.setDescription(desc);
            line_chart.setTouchEnabled(false);
            line_chart.setDragEnabled(true);
            line_chart.setDrawGridBackground(false);
            line_chart.setScaleYEnabled(false);
            line_chart.setScaleXEnabled(false);
            line_chart.setExtraLeftOffset(55f);
            line_chart.setExtraRightOffset(55f);
            line_chart.setDescription(null);//右下角文本值

            YAxis yaxis_left = line_chart.getAxisLeft();
            yaxis_left.setLabelCount(array_x.size(), true);//显示Y轴有几个值
            yaxis_left.setEnabled(false);

            YAxis yaxis_right = line_chart.getAxisRight();
            yaxis_right.setEnabled(false);

            XAxis xaxis = line_chart.getXAxis();
            xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xaxis.setLabelCount(array_x.size(), true);//显示X轴有几个值
            xaxis.setTextColor(Color.WHITE);
            xaxis.setTextSize(12f);
            xaxis.setDrawAxisLine(true);
            xaxis.setGridColor(Color.WHITE);
            xaxis.setAxisLineColor(Color.WHITE);
            xaxis.setDrawLabels(true);
            xaxis.setDrawGridLines(false);

            final Map<Integer, String> xmap = new HashMap<>();
            for (int i = 0; i < array_x.size(); i++) {
                xmap.put(i, array_x.get(i));
            }
            xaxis.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return xmap.get((int) value);
                }
            });
            Legend l = line_chart.getLegend();
            l.setTextSize(12f);
            l.setTextColor(Color.WHITE);
            l.setForm(Legend.LegendForm.LINE);
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);

            List<Entry> entrydata = new ArrayList<>();
            for (int i = 0; i < array_y.size(); i++) {
                entrydata.add(new Entry(i, Integer.valueOf(array_y.get(i))));
            }
            LineDataSet dataset1 = new LineDataSet(entrydata, "");
            dataset1.setLineWidth(3.5f);
            dataset1.setColor(Color.WHITE);
            dataset1.setDrawCircles(true);
            dataset1.setCircleColor(Color.BLUE);
            dataset1.setCircleRadius(4f);
//        dataset1.setHighLightColor(Color.rgb(244, 117, 117));
            dataset1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataset1.setLabel("");
            LineData linedata = new LineData(dataset1);
            linedata.setValueFormatter(new IValueFormatter() {
                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    return (int) value + "°";
                }
            });
            linedata.setValueTextColor(Color.WHITE);
            linedata.setValueTextSize(15f);

            line_chart.setData(linedata);
            line_chart.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "获取数据失败", Toast.LENGTH_SHORT).show();
        }
    }

}
