package com.example.weather.db;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.example.weather.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by jack on 2017/9/26.
 */

public class Rain extends AbstractWeather {
    private Drawable drawable;
    private Paint paint;
    private List<RainHolder> rainHolders;
    private Context mContext;
    RainHolder mHolder;
    private int mWidth;
    private int mHeight;
    private int bg_pic_id;

    private static Rain rain;

    public static Rain getInstance(Context context, int bg_pic_id) {
        if (rain == null) {
            rain = new Rain(context, bg_pic_id);
        }
        return rain;
    }

    private Rain(Context context, int bg_pic_id) {
        super(context);
        this.mContext = context;
        this.bg_pic_id = bg_pic_id;
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(3);
        drawable = mContext.getResources().getDrawable(bg_pic_id);
        rainHolders = new ArrayList<>();
    }

    @Override
    public void draw(Canvas canvas) {
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        drawable.draw(canvas);

        for (int i = 0; i < rainHolders.size(); i++) {

            mHolder = rainHolders.get(i);
            paint.setAlpha(mHolder.a);

            canvas.drawLine(mHolder.x, mHolder.y, mHolder.x, mHolder.y + mHolder.l, paint);
        }
        for (int i = 0; i < rainHolders.size(); i++) {
            mHolder = rainHolders.get(i);
            mHolder.y += mHolder.s;
            if (mHolder.y > mHeight) {
                mHolder.y = -mHolder.l;
            }
        }
    }

    @Override
    public void onSizeChanged(Context context, int w, int h) {
        this.mWidth = w;
        this.mHeight = h;
        genetate(mContext, w, h);
    }

    private void genetate(Context context, int w, int h) {

        drawable.setBounds(0, 0, w, h);
        for (int i = 0; i < 60; i++) {
            RainHolder rainHolder = new RainHolder(randowNum(1, w), randowNum(1, h), randowNum(dp2px(9), dp2px(15)),
                    randowNum(dp2px(10), dp2px(15)), randowNum(20, 100));
            rainHolders.add(rainHolder);
        }
    }

    private class RainHolder {
        int x;//坐标x
        int y;//坐标y
        int l;//长度
        int s;//速度
        int a;//透明度

        public RainHolder(int x, int y, int l, int s, int a) {
            this.x = x;
            this.y = y;
            this.l = l;
            this.s = s;
            this.a = a;
        }
    }
}
