package com.example.weather.db;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jack on 2017/9/26.
 */

public class Snow extends AbstractWeather {

    private Context mContext;
    private Drawable mDrawable;
    private Paint mPaint;
    private int bg_id;
    private int mw;
    private int mh;
    private List<SnowHolder> snowHolders;
    private SnowHolder snowHolder;
    private static Snow snow;
    public static Snow getInstance(Context context,int bg_id){
        if(snow==null){
            snow=new Snow(context,bg_id);
        }
        return snow;
    }
    private Snow(Context context,int bg_id) {
        super(context);
        this.mContext=context;
        this.bg_id=bg_id;
        mPaint=new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(3);

        mDrawable=context.getResources().getDrawable(bg_id);
        snowHolders=new ArrayList<>();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mDrawable.draw(canvas);

        for(int i=0;i<snowHolders.size();i++){
            snowHolder=snowHolders.get(i);
            mPaint.setAlpha(snowHolder.a);//设置画笔透明度
            canvas.drawCircle(snowHolder.x,snowHolder.y,snowHolder.r,mPaint);
        }
        for(int i=0;i<snowHolders.size();i++){
            snowHolder=snowHolders.get(i);
            snowHolder.y+=snowHolder.s;
            if(snowHolder.y>mh){
                snowHolder.y=-snowHolder.r;
            }
        }
    }

    @Override
    public void onSizeChanged(Context context, int w, int h) {
        this.mw=w;
        this.mh=h;
        mDrawable.setBounds(0,0,w,h);
        //构造出SnowHolder
        for(int i=0;i<60;i++){
            snowHolder=new SnowHolder(randowNum(1,w),randowNum(1,h),randowNum(dp2px(3),dp2px(5)),randowNum(dp2px(5),dp2px(8)),randowNum(20,100));
            snowHolders.add(snowHolder);
        }
    }
    class SnowHolder{
        int x;
        int y;
        int r;
        int s;//速度
        int a;//透明度
        public SnowHolder(int x,int y,int r,int s,int a){
            this.x=x;
            this.y=y;
            this.r=r;
            this.s=s;
            this.a=a;
        }
    }
}
