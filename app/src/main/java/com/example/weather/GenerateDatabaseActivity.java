package com.example.weather;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.weather.db.City;
import com.example.weather.db.Country;
import com.example.weather.db.CountryLocal;
import com.example.weather.db.Province;
import com.example.weather.util.DBUtils;
import com.example.weather.util.HttpUtil;
import com.example.weather.util.ProgressDialogUtil;
import com.example.weather.util.Utility;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class GenerateDatabaseActivity extends AppCompatActivity {

     Province pn=new Province();
     City cn=new City();
     Country cyn=new Country();
    int counter=0;
    List<Province> provinceList;
    List<City> cityList;
    List<Country> countries;
    String path= Environment.getExternalStorageDirectory().getPath();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_database);
        Button btn=(Button)findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String npath= path+"/weatherid.txt";
                try{
                    File file=new File(npath);
                    if(!file.exists())
                        Toast.makeText(view.getContext(),"no file",Toast.LENGTH_SHORT).show();
                    FileInputStream fi=new FileInputStream(new File(npath));
                    BufferedReader reader=new BufferedReader(new InputStreamReader(fi));
                    String len;
                    byte[] bytes=new byte[1024];
                    while((len=reader.readLine())!=null){
                        String[] ids=len.split(" ");
                        CountryLocal country=new CountryLocal();
                        country.setWeatherId(ids[0]);
                        country.setCountryName(ids[4]);
                        country.setCityName(ids[8]);
                        country.setProvinceName(ids[12]);
                        country.save();
                    }
                    fi.close();
                    reader.close();
                    List<CountryLocal> countryList=DataSupport.findAll(CountryLocal.class);
                    Toast.makeText(view.getContext(),"size:"+countryList.size(),Toast.LENGTH_SHORT).show();
                }catch (Exception e){

                }


            }
        });

        Button btn1=(Button)findViewById(R.id.btn1);
        Button btn2=(Button)findViewById(R.id.btn2);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               String oldpath="data/data/com.example.weather/databases/weather.db";
                String npath=path+"/weather.db";
                try{
                    File fo=new File(oldpath);
                    File fn=new File(npath);
                    if(!fn.exists()){
                        fn.createNewFile();
                    }
                    if(!fo.exists())
                        Toast.makeText(view.getContext(),"no file",Toast.LENGTH_SHORT).show();
                    FileInputStream fi=new FileInputStream(fo);

                    FileOutputStream fileOutputStream=new FileOutputStream(fn);

                    int len=0;
//                    BufferedReader reader=new BufferedReader(new InputStreamReader(fi));
//                    BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(fileOutputStream));
                    byte[] bytes=new byte[8];
                    while ((len= fi.read(bytes))!=-1){
                        fileOutputStream.write(bytes,0,len);
                    }

                    fi.close();
                    fileOutputStream.close();
                    Toast.makeText(view.getContext(),"ok",Toast.LENGTH_SHORT).show();
                }catch (Exception e){

                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file=new File("data/data/com.example.weather/databases/"+ DBUtils.dbname);
                List<Country> countryList=DataSupport.findAll(Country.class);
                Toast.makeText(view.getContext(),""+countryList.size(),Toast.LENGTH_SHORT).show();
            }
        });
        Button btn3=(Button)findViewById(R.id.btn3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListView listView=(ListView)findViewById(R.id.list_views);
                List<String> list=new ArrayList<String>();
                cityList=DataSupport.findAll(City.class);
                for(City c:cityList){
                    list.add(c.getCityName());
                }
                ArrayAdapter adapter=new ArrayAdapter<String>(view.getContext(),android.R.layout.simple_list_item_1,list);

                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
