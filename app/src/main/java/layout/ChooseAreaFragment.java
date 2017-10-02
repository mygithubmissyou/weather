package layout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weather.MainActivity;
import com.example.weather.R;
import com.example.weather.WeatherActivity;
import com.example.weather.db.City;
import com.example.weather.db.Country;
import com.example.weather.db.CountryLocal;
import com.example.weather.db.Province;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.ProgressDialogUtil;
import com.example.weather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by jack on 2017/9/23.
 */

public class ChooseAreaFragment extends Fragment {
    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTRY = 2;

    private TextView textView;
    private Button btn_back;
    private ListView listView;
    private List<String> datalist = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;
    private List<CountryLocal> countryLocalList;
    private int level;
    private ProgressDialog progressdialog;
    private Province selectedProvince;
    private City selectedCity;
    private String selectprovincename;
    private String selectCityname;
    String[] special_area = {"北京", "上海", "天津", "重庆", "香港", "澳门"};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_area, container, false);
        textView = (TextView) view.findViewById(R.id.txt_name);
        btn_back = (Button) view.findViewById(R.id.btn_back);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, datalist);
        listView.setAdapter(adapter);
        Log.d("ssss", "onCreateView");
//        new DBUtils(getActivity().getApplicationInfo().packageName,getActivity().getApplicationContext());
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (level == LEVEL_PROVINCE) {
                    if (provinceList.size() > 0) {
                        selectedProvince = provinceList.get(i);
                        selectprovincename = provinceList.get(i).getProvinceName();
                    } else {
                        selectprovincename = datalist.get(i);
                    }
                    queryCity();
                } else if (level == LEVEL_CITY) {
                    if (cityList.size() > 0) {
                        selectedCity = cityList.get(i);
                        selectCityname = cityList.get(i).getCityName();
                    } else {
                        selectCityname = datalist.get(i);
                    }
                    queryCountry();
                } else if (level == LEVEL_COUNTRY) {
                    String weatherid;
                    if(countryList!=null&&countryList.size() > 0){
                        weatherid = countryList.get(i).getWeatherId();
                    }else{
                        weatherid = countryLocalList.get(i).getWeatherId();
                    }
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherid);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity weatherActivity = (WeatherActivity) getActivity();
                        weatherActivity.layout_drawer.closeDrawers();
                        weatherActivity.swipe_refresh.setRefreshing(true);
                        weatherActivity.weatherid = weatherid;
                        weatherActivity.requestWeatherInfo(weatherid);
                    }
                }
            }
        });
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (level == LEVEL_COUNTRY) {
                    queryCity();
                } else if (level == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        queryProvinces();

    }

    public void queryProvinces() {

        textView.setText("中国");
        btn_back.setVisibility(View.INVISIBLE);
        Cursor cursor = DataSupport.findBySQL("select * from countrylocal group by provincename order by id");//优先查询本地数据库
        provinceList = new ArrayList<>();
        if (cursor != null&&cursor.moveToFirst()) {
            Log.d("ssss","本地省");
            datalist.clear();
//            cursor.moveToFirst();
            do{
                datalist.add(cursor.getString(cursor.getColumnIndex("provincename")));
            }while (cursor.moveToNext()) ;
        } else {
            Log.d("ssss","网络省");
            provinceList = DataSupport.findAll(Province.class);//查询本地数据库确定是否有数据
            if (provinceList.size() > 0) {
                datalist.clear();
                for (Province p : provinceList) {
                    datalist.add(p.getProvinceName());
                }
            }
        }
        if (datalist.size() > 0) {//查询数据库
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            level = LEVEL_PROVINCE;
        } else {//网络请求数据
//            Log.d("ssss","查省");
            String url = "http://guolin.tech/api/china";
            queryServer(url, "province");
        }

    }

    //查询城市数据
    private void queryCity() {

        textView.setText(selectprovincename);
        btn_back.setVisibility(View.VISIBLE);
        cityList = new ArrayList<>();
        if (selectedProvince != null) {
            //查询网络获取的数据
            Log.d("ssss","网络市");
            cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
            if (cityList.size() > 0) {
                datalist.clear();
                for (City city : cityList) {
                    datalist.add(city.getCityName());
//                    datalist.add(city.getId());
                }
                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                level = LEVEL_CITY;
            }else {
                Log.d("ssss","查市");
                String url = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode();
                queryServer(url, "city");
            }
        } else {
            //查询本地数据库获得的数据
            Cursor cursor = DataSupport.findBySQL("select distinct(cityname) from countrylocal where provincename='" + selectprovincename + "'");
            Log.d("ssss","本地市"+selectprovincename);
            if (cursor != null&&cursor.moveToFirst()) {
                datalist.clear();
                do{
                    datalist.add(cursor.getString(cursor.getColumnIndex("cityname")));
                    City city=new City();
                    city.setCityName(cursor.getString(cursor.getColumnIndex("cityname")));
                    cityList.add(city);
                }while (cursor.moveToNext());
            }
            if (datalist.size() > 0) {
                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                level = LEVEL_CITY;
            } else {
                Log.d("ssss","查市");
                String url = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode();
                queryServer(url, "city");
            }
        }

    }

    //查询县区数据
    private void queryCountry() {
//        Log.d("ssss", "queryCountry");
        textView.setText(selectCityname);
        btn_back.setVisibility(View.VISIBLE);
        //查询本地数据库
        countryLocalList=new ArrayList<>();
        countryLocalList = DataSupport.where("cityname=?", String.valueOf(selectCityname)).find(CountryLocal.class);
        if (countryLocalList.size() == 0) {
            //查询网络数据库
            countryList =new ArrayList<>();
            countryList= DataSupport.where("cityid=?", String.valueOf(selectedCity.getId())).find(Country.class);
        }

        if (countryList!=null&&countryList.size() > 0) {
            datalist.clear();
            for (Country c : countryList) {
                datalist.add(c.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            level = LEVEL_COUNTRY;
        }else if (countryLocalList !=null&& countryLocalList.size()>0) {
            datalist.clear();
            for (CountryLocal c : countryLocalList) {
                datalist.add(c.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            level = LEVEL_COUNTRY;
        }
        else {
            Log.d("ssss","查县"+ selectedProvince.getProvinceCode()+"--"+selectedCity.getCityCode());
            String url = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode() + "/" + selectedCity.getCityCode();
            queryServer(url, "country");
        }
    }

    private void queryServer(String url, final String type) {

        showProgressDialog();
        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProgressDialogUtil.closeProgessDialog();
                        Toast.makeText(getContext(), "加载失败...", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responsetxt = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.parseProvinceJson(responsetxt);
                } else if ("city".equals(type)) {
                    result = Utility.parseCityJson(responsetxt, selectedProvince.getId());
                } else if ("country".equals(type)) {
                    result = Utility.parseCountryJson(responsetxt, selectedCity.getId());
                }
                final boolean nresult = result;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        if (nresult) {
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCity();
                            } else if ("country".equals(type)) {
                                queryCountry();
                            } else {
                                Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });
    }

    private void showProgressDialog() {
        if (progressdialog == null) {
            progressdialog = new ProgressDialog(getActivity());
            progressdialog.setMessage("正在加载...");
            progressdialog.setCanceledOnTouchOutside(false);
        }
        progressdialog.show();
    }

    private void closeProgressDialog() {
        if (progressdialog != null) {
            progressdialog.dismiss();
        }
    }
}
