package com.example.weather.util;

import android.text.TextUtils;

import com.example.weather.db.City;
import com.example.weather.db.Country;
import com.example.weather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jack on 2017/9/23.
 */

public class Utility {
    public static boolean parseProvinceJson(String jsonstr){
        if(!TextUtils.isEmpty(jsonstr)){
            try{
                JSONArray array=new JSONArray(jsonstr);
                for(int i=0;i<array.length();i++){
                    JSONObject object=array.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceCode(object.getInt("id"));
                    province.setProvinceName(object.getString("name"));
                    province.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean parseCityJson(String jsonstr,int provinceid){
        if(!TextUtils.isEmpty(jsonstr)){
            try{
                JSONArray array=new JSONArray(jsonstr);
                for(int i=0;i<array.length();i++){
                    JSONObject object=array.getJSONObject(i);
                    City city=new City();
                    city.setCityCode(object.getInt("id"));
                    city.setCityName(object.getString("name"));
                    city.setProvinceId(provinceid);
                    city.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean parseCountryJson(String jsonstr,int cityid){
        if(!TextUtils.isEmpty(jsonstr)){
            try{
                JSONArray array=new JSONArray(jsonstr);
                for(int i=0;i<array.length();i++){
                    JSONObject object=array.getJSONObject(i);
                    Country country=new Country();
                    country.setCityId(cityid);
                    country.setCountryName(object.getString("name"));
                    country.setWeatherId(object.getString("weather_id"));
                    country.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
}
