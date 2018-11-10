package cn.edu.pku.yanqiheng.weatherapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.pku.yanqiheng.app.MyApplication;
import cn.edu.pku.yanqiheng.bean.City;
import cn.edu.pku.yanqiheng.util.PinYin;

public class SelectCity extends Activity implements View.OnClickListener {

    private ImageView mBackBtn;
    private ListView mlistView;
    private List<String> data_city;
    private List<City> cityList;
    private EditText mEditText;
    private TextView mTextView;

    // 城市名到编码
    private Map<String,String> nameToCode = new HashMap<>();
    // 城市名到拼音
    private Map<String,String> nameToPinyin = new HashMap<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置新的选择城市的布局
        setContentView(R.layout.select_city);

        initView();

    }


    @Override
    public void onClick(View view){
        if(view.getId()==R.id.title_back)
            // 在finish之前，传递数据
//            Intent i = new Intent();
//            i.putExtra("cityCode", (Serializable) cityNowCode);
//            setResult(RESULT_OK, i);
            finish();
    }


    // 初始化函数
    @SuppressLint("SetTextI18n")
    protected void initView(){
        // 更新上方的当前城市
        String nowCityName=this.getIntent().getStringExtra("cityName");
        if(nowCityName!=null){
            mTextView=(TextView)findViewById(R.id.title_name);
            mTextView.setText("当前城市: "+nowCityName);
        }
        // 设置返回按钮的监听
        mBackBtn=(ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        // 获取cityList
        MyApplication myApplication= (MyApplication) getApplication();
        cityList=new ArrayList<>();
        cityList= myApplication.getCityList();

        String strNamePinyin;
        data_city=new ArrayList<>();
        for(City temp: cityList){
            data_city.add(temp.getCity());
            strNamePinyin= PinYin.converterToSpell(temp.getCity());
            nameToCode.put(temp.getCity(),temp.getNumber()); // 城市名到城市编码
            nameToPinyin.put(temp.getCity(),strNamePinyin); // 城市名到拼音
        }

        // 给ListView配置适配器，并且设置监听事件
        mlistView = (ListView)findViewById(R.id.list_view);
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,data_city);
        mlistView.setAdapter(adapter);
        mlistView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        // 点击的第i个位置的城市名
                        String cityName=data_city.get(i);
                        // 点击的城市的城市码
                        String cityCode=nameToCode.get(cityName);
                        Toast.makeText(SelectCity.this,"已经选择好城市"+cityName,Toast.LENGTH_SHORT).show();

                        // 返回给MainActivity的意图
                        Intent returnIntent=new Intent();
                        returnIntent.putExtra("cityName",cityName);
                        returnIntent.putExtra("cityCode",cityCode);
                        // 把intent返回
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                });

        UseEditText();

    }


    //对editText控件进行控制
    public void UseEditText(){
        // 新建一个TextWatcher对象并且重写其内的方法
        TextWatcher mTextWatcher = new TextWatcher(){
            private CharSequence temp;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                temp = charSequence;
                Log.d("myWeather","beforeTextChanged:"+temp) ;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3){
                // 搜索栏不为空时
                if (!TextUtils.isEmpty(charSequence)){
                    if(data_city!=null)
                        data_city.clear();
                    // 遍历所有城市名
                    for (String str : nameToPinyin.keySet()){
                        if (str.contains(charSequence)||nameToPinyin.get(str).contains(charSequence)) {
                            data_city.add(str);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    Log.d("myWeather","onTextChanged:"+charSequence) ;
                }
            }

            @Override
            public void afterTextChanged(Editable editable){
                Log.d("myWeather","afterTextChanged:") ;
            }
        };
        // 编辑控件
        mEditText = (EditText)findViewById(R.id.edittext);
        mEditText.addTextChangedListener(mTextWatcher);

    }



}
