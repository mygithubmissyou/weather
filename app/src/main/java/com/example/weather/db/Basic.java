package com.example.weather.db;

/**
 * Created by jack on 2017/9/23.
 */

public class Basic {
    public String city;
    public String id;
    public String cnty;
    public String lat;
    public String lon;
    public String prov;
    public Update update;
    public class Update{
        public String loc;
        public String utc;
    }
}
