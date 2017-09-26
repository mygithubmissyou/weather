package layout;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.weather.service.AutoUpdateService;
import com.example.weather.R;


public class SettingFragment extends Fragment {

    private CheckBox cb_autoupdate;
    public RadioGroup radio_updatetime;
    private RadioButton radio_four;
    private RadioButton radio_six;
    private RadioButton radio_eight;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_setting,container,false);
        cb_autoupdate=(CheckBox)view.findViewById(R.id.cb_autoupdate);
        radio_updatetime=(RadioGroup)view.findViewById(R.id.radio_updatetime);
        radio_four=(RadioButton)view.findViewById(R.id.radio_four);
        radio_six=(RadioButton)view.findViewById(R.id.radio_six);
        radio_eight=(RadioButton)view.findViewById(R.id.radio_eight);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Intent intent=new Intent(getContext(), AutoUpdateService.class);
        cb_autoupdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    AutoUpdateService.is_auto=true;
                    getContext().startService(intent);
                    Toast.makeText(getContext(),"自动更新开始",Toast.LENGTH_SHORT).show();

                }else{
                    AutoUpdateService.is_auto=false;
                    getContext().stopService(intent);
                    Toast.makeText(getContext(),"停止自动更新",Toast.LENGTH_SHORT).show();
                }
            }
        });
        radio_updatetime.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {

                    if(radioGroup.getCheckedRadioButtonId()==radio_four.getId()){
                        AutoUpdateService.updatetime=4;
                        Toast.makeText(getContext(),"设置成功"+4+"小时后更新",Toast.LENGTH_SHORT).show();
                    }else if(radioGroup.getCheckedRadioButtonId()==radio_six.getId()){
                        AutoUpdateService.updatetime=6;
                        Toast.makeText(getContext(),"设置成功"+6+"小时后更新",Toast.LENGTH_SHORT).show();
                    }else if(radioGroup.getCheckedRadioButtonId()==radio_eight.getId()){
                        AutoUpdateService.updatetime=8;
                        Toast.makeText(getContext(),"设置成功"+8+"小时后更新",Toast.LENGTH_SHORT).show();
                    }
            }
        });
    }
}
