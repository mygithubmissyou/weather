package com.example.weather.util;

import android.app.Activity;
import android.widget.Toast;

import com.example.weather.WeatherActivity;
import com.example.weather.db.City;
import com.example.weather.db.Country;
import com.example.weather.db.CountryLocal;
import com.example.weather.db.Province;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;
import java.io.File;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by jack on 2017/9/27.
 */

public class GetWeatherByLocation {
    public static Province selectedProvince;
    public static City selectedCity;
    public static List<Province> provinceList;
    public static List<City> cityList;
    public static List<Country> countryList;
    public static List<CountryLocal> countryLocalList;
    public static String weatherid;

    //发送三次请求
    public static void getProvinceAndCityAndCountry(final String province, final String city, final String country, final Activity activity) {

        //发送第一次请求
        String url = "http://guolin.tech/api/china";
        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                closeWindow(activity);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final boolean result = Utility.parseProvinceJson(response.body().string());
                if (result) {
                    provinceList = DataSupport.findAll(Province.class);
                    for (Province p : provinceList) {
                        if (p.getProvinceName().equals(province)) {
                            selectedProvince = p;
                        }
                    }
                    if(selectedProvince==null){
                        closeWindow(activity);
                        return;
                    }
                    //发送第二次请求
                    String url1 = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode();
                    HttpUtil.sendRequest(url1, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            closeWindow(activity);
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            final boolean result = Utility.parseCityJson(response.body().string(), selectedProvince.getId());
                            if (result) {
                                cityList = DataSupport.where("provinceid=?", String.valueOf(selectedProvince.getId())).find(City.class);
                                selectedCity = new City();
                                for (City c : cityList) {
                                    if (c.getCityName().equals(city)) {
                                        selectedCity = c;
                                    }
                                }
                                //第三次请求
                                if(selectedCity==null){
                                   closeWindow(activity);
                                    return;
                                }
                                String url2 = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode() + "/" + selectedCity.getCityCode();
                                HttpUtil.sendRequest(url2, new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                       closeWindow(activity);
                                    }
                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        final boolean result = Utility.parseCountryJson(response.body().string(), selectedCity.getId());
                                        if (result) {
                                            countryList = DataSupport.where("cityid=?", String.valueOf(selectedCity.getId())).find(Country.class);
                                            if (countryList.size() > 0) {
                                                for (Country c : countryList) {
                                                    if (c.getCountyName().equals(country)) {
                                                        weatherid = c.getWeatherId();
                                                        //发起网络请求天气
                                                        ((WeatherActivity) activity).requestWeatherInfo(weatherid);
                                                    }
                                                }
                                            }else{
                                                closeWindow(activity);
                                            }

                                        } else {
                                           closeWindow(activity);
                                        }
                                    }
                                });
                            } else {
                               closeWindow(activity);
                            }
                        }
                    });
                } else {
                    closeWindow(activity);
                }
            }
        });


    }
    public static void closeWindow(final Activity activity){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //关闭progressdialog
                ProgressDialogUtil.closeProgessDialog();
                Toast.makeText(activity, "请求数据失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //发送两次请求
    public static void getCityAndCountry(final String city, final String country, final Activity activity) {
        //发送两次请求
        String url = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode();
        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProgressDialogUtil.closeProgessDialog();
                        Toast.makeText(activity, "请求数据失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final boolean result = Utility.parseCityJson(response.body().string(), selectedProvince.getId());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result) {
                            cityList = DataSupport.where("provinceid=?", String.valueOf(selectedProvince.getId())).find(City.class);
                            for (City c : cityList) {
                                if (c.getCityName().equals(city)) {
                                    selectedCity = c;
                                    String url = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode() + "/" + selectedCity.getCityCode();
                                    HttpUtil.sendRequest(url, new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ProgressDialogUtil.closeProgessDialog();
                                                    Toast.makeText(activity, "请求数据失败", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            final boolean result = Utility.parseCountryJson(response.body().string(), selectedCity.getId());
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (result) {
                                                        countryList = DataSupport.where("cityid=?", String.valueOf(selectedCity.getId())).find(Country.class);
                                                        if (countryList.size() > 0) {
                                                            for (Country c : countryList) {
                                                                if (c.getCountyName().equals(country)) {
                                                                    weatherid = c.getWeatherId();
                                                                    ((WeatherActivity) activity).requestWeatherInfo(c.getWeatherId());
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        ProgressDialogUtil.closeProgessDialog();
                                                        Toast.makeText(activity, "请求数据失败", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        } else {
                            ProgressDialogUtil.closeProgessDialog();
                            Toast.makeText(activity, "请求数据失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    public static void getCountry(final String country, final Activity activity) {
        //发送一次请求
        String url = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode() + "/" + selectedCity.getCityCode();
        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProgressDialogUtil.closeProgessDialog();
                        Toast.makeText(activity, "请求数据失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final boolean result = Utility.parseCountryJson(response.body().string(), selectedCity.getId());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result) {
                            countryList = DataSupport.where("cityid=?", String.valueOf(selectedCity.getId())).find(Country.class);
                            if (countryList.size() > 0) {
                                for (Country c : countryList) {
                                    if (c.getCountyName().equals(country)) {
                                        weatherid = c.getWeatherId();
                                        ((WeatherActivity) activity).requestWeatherInfo(c.getWeatherId());
                                    }
                                }
                            }
                        } else {
                            ProgressDialogUtil.closeProgessDialog();
                            Toast.makeText(activity, "请求数据失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    public static void getWeatherIdAndRequest(final String province, final String city, final String country, final Activity activity) {
        ProgressDialogUtil.showProgressDialog(activity, "", "正在加载中...");
        File file = new File("data/data/com.example.weather/databases/" + DBUtils.dbname);
        if (file.exists()) {
            try{
                countryLocalList = DataSupport.where("countryname=?", country).find(CountryLocal.class);
            }catch (Exception e){
                ProgressDialogUtil.closeProgessDialog();
                Toast.makeText(activity,"稍后重试",Toast.LENGTH_SHORT).show();
            }

            if (countryLocalList!=null&&countryLocalList.size() > 0) {
                CountryLocal country1 = countryLocalList.get(0);
                weatherid=country1.getWeatherId();
                ((WeatherActivity) activity).requestWeatherInfo(country1.getWeatherId());
            } else {
                //发送三次请求
                getProvinceAndCityAndCountry(province, city, country, activity);
            }
        } else {
            selectedProvince = new Province();
            provinceList = DataSupport.findAll(Province.class);
            if (provinceList.size() > 0) {
                for (Province p : provinceList) {
                    if (p.getProvinceName().equals(province)) {
                        selectedProvince = p;
                        selectedCity = new City();
                        cityList = DataSupport.where("provinceid=?", String.valueOf(selectedProvince.getId())).find(City.class);
                        if (cityList.size() > 0) {
                            for (City c : cityList) {
                                if (c.getCityName().equals(city)) {
                                    selectedCity = c;
                                    countryList = DataSupport.where("cityid=?", String.valueOf(selectedCity.getId())).find(Country.class);
                                    if (countryList.size() > 0) {
                                        for (Country c1 : countryList) {
                                            if (c1.getCountyName().equals(country)) {
                                                weatherid = c1.getWeatherId();
                                                ((WeatherActivity) activity).requestWeatherInfo(c1.getWeatherId());
                                            }
                                        }
                                    } else {
                                        //发送一次请求
                                        getCountry(country, activity);
                                    }
                                }
                            }
                        } else {
                            //发送两次请求
                            getCityAndCountry(city, country, activity);
                        }
                    }
                }
            } else {
                //发送三次请求
                getProvinceAndCityAndCountry(province, city, country, activity);
            }
        }
    }


}
