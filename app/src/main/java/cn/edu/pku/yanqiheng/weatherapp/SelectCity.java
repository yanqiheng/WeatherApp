package cn.edu.pku.yanqiheng.weatherapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.yanqiheng.app.MyApplication;
import cn.edu.pku.yanqiheng.bean.City;

public class SelectCity extends Activity implements View.OnClickListener {

    private ImageView mBackBtn;
    private ListView mlistView;
    private List<String> data_city;
    private List<String> data_code;
    private List<City> cityList;
    private StringBuilder cityNowCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置新的选择城市的布局
        setContentView(R.layout.select_city);
        // 获取cityList
        MyApplication myApplication= (MyApplication) getApplication();
        cityList=new ArrayList<>();
        cityList= myApplication.getCityList();
        mlistView = (ListView)findViewById(R.id.list_view);
        data_city=new ArrayList<>();
        data_code=new ArrayList<>();
        // data_city赋值
        for(City temp: cityList){
                data_city.add(temp.getCity());
                data_code.add(temp.getNumber());
        }

        // 设置适配器
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,data_city);
        mlistView.setAdapter(adapter);

        mlistView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Toast.makeText(SelectCity.this,"对应城市的code码:"+data_code.get(i),Toast.LENGTH_SHORT).show();
                        cityNowCode=new StringBuilder();
                        cityNowCode.append(data_code.get(i));
                        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("cityCode",data_code.get(i));
                        editor.commit();
                    }
                });

        mBackBtn=(ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View view){
        if(view.getId()==R.id.title_back){
            // 在finish之前，传递数据
            Intent i = new Intent();
            i.putExtra("cityCode", (Serializable) cityNowCode);
            setResult(RESULT_OK, i);
            finish();
        }
    }




}
