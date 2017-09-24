package com.example.weather.util;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by jack on 2017/9/24.
 */

public class ProgressDialogUtil {
    private static ProgressDialog progressDialog;
    public static void showProgressDialog(Context context,String title,String content){
        progressDialog=new ProgressDialog(context);
        progressDialog.setCanceledOnTouchOutside(false);
        if(!"".equals(title))
            progressDialog.setTitle(title);
        if(!"".equals(content))
            progressDialog.setMessage(content);
         progressDialog.show();
    }
    public static void closeProgessDialog(){
        if(progressDialog!=null)
            progressDialog.dismiss();
    }
}
