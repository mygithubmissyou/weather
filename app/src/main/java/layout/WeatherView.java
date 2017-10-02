package layout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.weather.R;
import com.example.weather.db.WeatherType;

/**
 * Created by jack on 2017/9/26.
 */

public class WeatherView extends SurfaceView implements SurfaceHolder.Callback {
    LoopThread thread;
    boolean isRunning = true;
    static WeatherType weathertype;
    static Context mContext;
    SurfaceHolder holder;
    int w;
    int h;

    public static WeatherView getInstance(Context context, WeatherType type) {
        mContext = context;
        weathertype = type;
        WeatherView wv = new WeatherView(context);
        return wv;
    }

    public void setWeatherType(WeatherType type) {
        this.weathertype = type;
    }

    public WeatherView(Context context) {
        this(context, null);
    }

    public WeatherView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        setZOrderOnTop(true);
        setZOrderMediaOverlay(true);
        holder = getHolder();
        holder.addCallback(this);
//        holder.setFormat(PixelFormat.TRANSPARENT);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            isRunning = false;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        thread = new LoopThread();
        isRunning = true;
        thread.start();
    }


    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        isRunning = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
        if (weathertype != null) {
            weathertype.onSizeChanged(mContext, w, h);
        }
    }

    class LoopThread extends Thread {


        @Override
        public void run() {
            while (isRunning) {
                synchronized (this) {

                    if (weathertype != null && w != 0 && h != 0) {
                        Canvas c = holder.lockCanvas();
                        if (c != null) {

                            try {
                                weathertype.draw(c);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                holder.unlockCanvasAndPost(c);
                            }
                        }
                    }
                }
            }
        }
        //画圈测试
//        float radius=10f;
//        public void doDraw(Canvas c){
//            Paint paint=new Paint();
//            paint.setColor(Color.YELLOW);
//            paint.setStyle(Paint.Style.STROKE);
//
//            //这个很重要，清屏操作，清楚掉上次绘制的残留图像
//            c.drawColor(Color.BLACK);
//
//            c.translate(200, 200);
//            c.drawCircle(0,0, radius++, paint);
//
//            if(radius > 1000){
//                radius = 10f;
//            }
//
//        }
    }
}
