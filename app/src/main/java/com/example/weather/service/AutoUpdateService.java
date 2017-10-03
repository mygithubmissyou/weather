package com.example.weather.service;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.example.weather.db.Weather;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by jack on 2017/9/24.
 */

public class AutoUpdateService extends Service{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
public static int updatetime=8;
    public static boolean is_auto=false;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
//        updatePic();
        Intent intent1=new Intent(this,AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this,0,intent1,0);
        AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);
        int hour=updatetime*60*60*1000;
        long trigrttime= SystemClock.elapsedRealtime()+hour;
//        manager.cancel(pi);
        if(is_auto){
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,trigrttime,pi);
            Toast.makeText(this,updatetime+"小时后更新",Toast.LENGTH_SHORT).show();
        }else {
            manager.cancel(pi);
        }
//        Notification notification=new Notification.Builder(getApplicationContext())
//                .setContentText("天气更新通知")
//                .setDefaults(NotificationCompat.DEFAULT_ALL)
//                .setAutoCancel(true)
//                .build();

//        return super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }
//更新必应图片
    private void updatePic() {
        String url="http://guolin.tech/api/bing_pic";
        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String rstr=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("biying",rstr);
                editor.apply();
            }
        });
    }
//更新天气信息
    private void updateWeather() {
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherstr=sp.getString("weather",null);
        if(weatherstr!=null){
            Weather weather= Utility.parseWeatherJson(weatherstr);
            String weatherid=weather.basic.id;
            String url="https://free-api.heweather.com/v5/weather?city="+weatherid+"&key=f5968574fabe49419386819194fca95d";
            HttpUtil.sendRequest(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String wstr=response.body().string();
                    Weather weather=Utility.parseWeatherJson(wstr);
                    if(wstr!=null&&"ok".equals(weather.status)){
                        SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",wstr);
                        editor.apply();
                    }
                }
            });
        }
    }
}
