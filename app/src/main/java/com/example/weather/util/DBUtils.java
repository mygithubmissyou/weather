package com.example.weather.util;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by jack on 2017/9/27.
 */

public class DBUtils {
    public DBUtils(String packagename,Context c){
        writeDBFile(packagename,c);
    }

    public static String dbname="weather.db";
    public static void writeDBFile(String packagename,Context c){
        File file=new File("data/data/"+packagename+"/databases/"+dbname);
        try{
            if(!file.exists())
                file.createNewFile();
            InputStream fi= c.getAssets().open(dbname);
            FileOutputStream fo=new FileOutputStream(file);
            byte[] buffer=new byte[1024];
            int len=0;
            while((len=fi.read(buffer))!=-1){
                fo.write(buffer,0,len);
            }
            fo.flush();
            fo.close();
            fi.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
