package com.example.weather.db;

import java.util.List;

/**
 * Created by jack on 2017/9/23.
 */

public class Weather {
    public String status;
    public List<Alarms> alarms;
    public AQI aqi;
    public Basic basic;
    public List<DailyForecast> daily_forecast;
    public List<HourlyForecast> hourly_forecast;
    public Now now;
    public Suggestion suggestion;
}
