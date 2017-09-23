package com.example.weather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weather.db.City;
import com.example.weather.db.Country;
import com.example.weather.db.Province;
import com.example.weather.util.HttpUtil;
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
    private static final int LEVEL_PROVINCE=0;
    private static final int LEVEL_CITY=1;
    private static final int LEVEL_COUNTRY=2;
       private TextView textView;
       private Button btn_back;
       private ListView listView;
       private List<String> datalist=new ArrayList<>();
       private ArrayAdapter<String> adapter;
       private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;
    private int level;
    private ProgressDialog progressdialog;
    private Province selectedProvince;
    private City selectedCity;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_choose_area,container,false);
        textView=(TextView)view.findViewById(R.id.txt_name);
        btn_back=(Button)view.findViewById(R.id.btn_back);
        listView=(ListView)view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,datalist);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(level==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(i);
                    queryCity();
                }else if(level==LEVEL_CITY){
                    selectedCity=cityList.get(i);
                    queryCountry();
                }else if(level==LEVEL_COUNTRY){

                }
            }
        });
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(level==LEVEL_COUNTRY){
                    queryCity();
                }else if(level==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }
    private void queryProvinces(){
        textView.setText("中国");
        btn_back.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if(provinceList.size()>0){//查询数据库
            datalist.clear();
            for(Province p:provinceList){
                datalist.add(p.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            level=LEVEL_PROVINCE;
        }else{//网络请求数据
            String url="http://guolin.tech/api/china";
            queryServer(url,"province");
        }

    }
    //查询城市数据
    private void queryCity(){
        textView.setText(selectedProvince.getProvinceName());
        btn_back.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            datalist.clear();
            for(City c:cityList){
                datalist.add(c.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            level=LEVEL_CITY;
        }else{
            String url="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode();
            queryServer(url,"city");
        }
    }
    //查询县区数据
    private void queryCountry(){
        textView.setText(selectedCity.getCityName());
        btn_back.setVisibility(View.VISIBLE);
        countryList=DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(Country.class);
        if(countryList.size()>0){
            datalist.clear();
            for(Country c:countryList){
                datalist.add(c.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            level=LEVEL_COUNTRY;
        }else{
            String url="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode()+"/"+selectedCity.getCityCode();
            queryServer(url,"country");
        }
    }
    private void queryServer(String url, final String type) {
        showProgressDialog();
        HttpUtil.sendRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                closeProgressDialog();
                Toast.makeText(getContext(),"加载失败...",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responsetxt=response.body().string();
                boolean result=false;
                if("province".equals(type)){
                    result= Utility.parseProvinceJson(responsetxt);
                }else if("city".equals(type)){
                    result=Utility.parseCityJson(responsetxt,selectedProvince.getId());
                }else if("country".equals(type)){
                    result=Utility.parseCountryJson(responsetxt,selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCity();
                            }else if("country".equals(type)){
                                queryCountry();
                            }
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog() {
        if(progressdialog==null){
            progressdialog=new ProgressDialog(getActivity());
            progressdialog.setMessage("正在加载...");
            progressdialog.setCanceledOnTouchOutside(false);
        }
        progressdialog.show();
    }
    private void closeProgressDialog(){
        if(progressdialog!=null){
            progressdialog.dismiss();
        }
    }
}
