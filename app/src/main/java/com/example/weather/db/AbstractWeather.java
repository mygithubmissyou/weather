package com.example.weather.db;

import android.content.Context;
import android.graphics.Canvas;

import java.util.Random;

/**
 * Created by jack on 2017/9/26.
 */

public abstract class AbstractWeather implements WeatherType {
    Context mContext;
    public AbstractWeather(Context context){
        this.mContext=context;
    }
    @Override
    public abstract void draw(Canvas canvas);

    @Override
    public abstract void onSizeChanged(Context context, int w, int h);
    //随机生成数字
    public int randowNum(int min,int max){
        if(max<min){
            return 1;
        }
        return min+new Random().nextInt(max-min);
    }

    public int dp2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
