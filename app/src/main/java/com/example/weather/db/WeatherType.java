package com.example.weather.db;

import android.content.Context;
import android.graphics.Canvas;

/**
 * Created by jack on 2017/9/26.
 */

public interface WeatherType {
    void draw(Canvas canvas);
    void onSizeChanged(Context context,int w ,int h);
}
