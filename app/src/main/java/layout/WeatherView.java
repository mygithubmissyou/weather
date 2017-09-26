package layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.weather.db.WeatherType;

/**
 * Created by jack on 2017/9/26.
 */

public class WeatherView extends SurfaceView implements SurfaceHolder.Callback{
    LoopThread thread;
    WeatherType weathertype;
    Context mContext;
    SurfaceHolder holder;
    int w;
    int h;
    public void setWeatherType(WeatherType type){
        this.weathertype=type;
    }
    public WeatherView(Context context) {
        this(context,null);
    }
    public WeatherView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public WeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext=context;
        holder=getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        thread=new LoopThread();
        thread.isRunning=true;
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        thread.isRunning=false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w=w;
        this.h=h;
        if(weathertype!=null){
            weathertype.onSizeChanged(mContext,w,h);
        }
    }

    class LoopThread extends Thread{

        boolean isRunning=false;

        @Override
        public void run() {
            Canvas c;
            while(isRunning){
                if(weathertype!=null&&w!=0&&h!=0){
                    c=holder.lockCanvas();
                    weathertype.draw(c);
                        if(isRunning){
                            holder.unlockCanvasAndPost(c);
                        }else{
                            break;
                        }
                        SystemClock.sleep(5);
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
