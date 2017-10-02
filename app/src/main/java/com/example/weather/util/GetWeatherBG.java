package com.example.weather.util;

import com.example.weather.R;
import com.example.weather.db.Rain;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jack on 2017/9/29.
 */

public class GetWeatherBG {

    public static final int TYPE_SNOW=1;
    public static final int TYPE_RAIN=2;
    public static final int TYPE_SUN=3;
    public static final int TYPE_SHOWER=4;
    public static Map<String, Integer> getBGResource(String con) {
        Map<String, Integer> map = new HashMap<>();
        Calendar calendar=Calendar.getInstance();
        int hour=calendar.get(Calendar.HOUR_OF_DAY);

        if(hour>6&&hour<18){
            switch (con) {
                case "多云":
                    map.put("bg", R.drawable.background_for_cloud);
                    map.put("animate",R.animator.cloud_animate);
                    break;
                case "沙尘暴":
                    map.put("bg", R.drawable.background_for_dust_storm);
                    map.put("animate",R.drawable.dust_storm_animate);
                    break;
                case "雾":
                    map.put("bg", R.drawable.background_for_fog);
                    map.put("animate",R.drawable.fog_animate);
                    break;
                case "霾":
                    map.put("bg", R.drawable.background_for_haze);
//                    map.put("animate",R.drawable.thunder_animate);
                    break;
                case "大雨":
                    map.put("bg", R.drawable.background_for_heavy_rain);
                    map.put("animate", TYPE_RAIN);
                    map.put("speed",2000);
                    map.put("num",35);
                    map.put("h",5);
                    break;
                case "中雨":
                    map.put("bg", R.drawable.background_for_heavy_rain);
                    map.put("animate",TYPE_RAIN);
                    map.put("speed",2500);
                    map.put("num",25);
                    map.put("h",5);
                    break;
                case "小雨":
                    map.put("bg", R.drawable.background_for_overcast);
                    map.put("animate",TYPE_RAIN);
                    map.put("speed",3000);
                    map.put("num",15);
                    map.put("h",5);
                    break;
                case "阴":
                    map.put("bg", R.drawable.background_for_cloud);
                    map.put("animate",R.animator.cloud_animate);
                    break;
                case "阵雨":map.put("bg", R.drawable.background_for_shower);
                    map.put("animate",TYPE_SHOWER);
                    map.put("speed",3000);
                    map.put("num",30);
                    map.put("h",5);
                    break;
                case "强阵雨":
                    map.put("bg", R.drawable.background_for_shower);
                    map.put("animate",TYPE_SHOWER);
                    map.put("speed",3000);
                    map.put("num",40);
                    map.put("h",8);
                    break;
                case "小雪":
                    map.put("bg", R.drawable.background_for_snow);
                    map.put("animate",TYPE_SNOW);
                    map.put("speed",3000);
                    map.put("num",15);
                case "暴雪":
                    map.put("bg", R.drawable.background_for_snow);
                    map.put("animate",TYPE_SNOW);
                    map.put("speed",1000);
                    map.put("num",30);
                case "中雪":
                    map.put("bg", R.drawable.background_for_snow);
                    map.put("animate",TYPE_SNOW);
                    map.put("speed",2500);
                    map.put("num",20);
                case "大雪":
                    map.put("bg", R.drawable.background_for_snow);
                    map.put("animate",TYPE_SNOW);
                    map.put("speed",2000);
                    map.put("num",25);
                    break;
                case "晴":
                    map.put("bg", R.drawable.background_for_default);
                    map.put("animate",TYPE_SUN);
//                    map.put("animate",R.drawable.background_for_star_night);
                    break;
                case "雷阵雨":
                    map.put("bg", R.drawable.background_for_thunderstorm);
                    map.put("animate",R.drawable.thunder_animate);
                    break;
                case "强雷阵雨":
                    map.put("bg", R.drawable.background_for_thunderstorm);
                    map.put("animate",R.drawable.thunder_animate);
                    break;
                default:
                    break;

            }
        }else{
            switch (con) {
                case "多云":
                    map.put("bg", R.drawable.background_for_cloud_night);
                    map.put("animate",R.animator.cloud_animate);
                    break;
                case "沙尘暴":
                    map.put("bg", R.drawable.background_for_dust_storm_night);
                    map.put("animate",R.drawable.dust_storm_animate);
                    break;
                case "雾":
                    map.put("bg", R.drawable.background_for_fog_night);
                    map.put("animate",R.drawable.fog_animate);
                    break;
                case "霾":
                    map.put("bg", R.drawable.background_for_haze_night);
//                    map.put("animate",R.drawable.thunder_animate);
                    break;
                case "大雨":
                    map.put("bg", R.drawable.background_for_heavy_rain_night);
                    map.put("animate",TYPE_RAIN);
                    map.put("speed",2000);
                    map.put("num",35);
                    map.put("h",5);
                    break;
                case "中雨":
                    map.put("bg", R.drawable.background_for_heavy_rain_night);
                    map.put("animate",TYPE_RAIN);
                    map.put("speed",2500);
                    map.put("num",25);
                    map.put("h",5);
                    break;
                case "小雨":
                    map.put("bg", R.drawable.background_for_overcast_night);
                    map.put("animate",TYPE_RAIN);
                    map.put("speed",3000);
                    map.put("num",15);
                    map.put("h",5);
                    break;
                case "阴":
                    map.put("bg", R.drawable.background_for_cloud_night);
                    map.put("animate",R.animator.cloud_animate);
                    break;
                case "阵雨":
                    map.put("bg", R.drawable.background_for_shower_night);
                    map.put("animate",TYPE_SHOWER);
                    map.put("speed",3000);
                    map.put("num",30);
                    map.put("h",5);
                    break;
                case "强阵雨":
                    map.put("bg", R.drawable.background_for_shower_night);
                    map.put("animate",TYPE_SHOWER);
                    map.put("speed",3000);
                    map.put("num",40);
                    map.put("h",5);
                    break;
                case "小雪":
                    map.put("bg", R.drawable.background_for_snow_night);
                    map.put("animate",TYPE_SNOW);
                    map.put("speed",3000);
                    map.put("num",15);
                    break;
                case "暴雪":
                    map.put("bg", R.drawable.background_for_snow_night);
                    map.put("animate",TYPE_SNOW);
                    map.put("speed",1000);
                    map.put("num",30);
                    break;
                case "中雪":
                    map.put("bg", R.drawable.background_for_snow_night);
                    map.put("animate",TYPE_SNOW);
                    map.put("speed",2500);
                    map.put("num",20);
                    break;
                case "大雪":
                    map.put("bg", R.drawable.background_for_snow_night);
                    map.put("animate",TYPE_SNOW);
                    map.put("speed",2000);
                    map.put("num",25);
                    break;
                case "晴":
                    map.put("bg", R.drawable.background_for_star_night);
//                    map.put("animate",R.drawable.background_for_star_night);
                    break;
                case "雷阵雨":
                case "强雷阵雨":
                    map.put("bg", R.drawable.background_for_thunderstorm_night);
                    map.put("animate",R.drawable.thunder_animate);
                    break;
                default:
                    break;

            }
        }

        return map;
    }
}
