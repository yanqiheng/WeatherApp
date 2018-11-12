package cn.edu.pku.yanqiheng.weatherapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.edu.pku.yanqiheng.bean.TodayWeather;
import cn.edu.pku.yanqiheng.util.NetUtil;

public class MainActivity extends Activity implements View.OnClickListener{

    private static final int UPDATE_TODAY_WEATHER = 1;

    // 在MainActivity中声明变量，绑定布局组件，在parseXML时将他们的数据也解析出来
    // 然后在initView和updateTodayWeather函数中，将数据更新到界面上
    private ImageView mUpdateBtn;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;
    private ImageView mCitySelect;
    private TextView week1,week2,week3,temperature1,temperature2,temperature3,wind1,wind2,wind3,climate1,climate2,climate3;
    private ProgressBar mUpdateProgressBar;
    String newCityCode;
    String newCityName;

    // 主线程接受后进行更新
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    // 主线程进行主界面信息的更新
                    updateTodayWeather((TodayWeather) msg.obj);
                    mUpdateBtn.setVisibility(View.VISIBLE);
                    mUpdateProgressBar.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        mUpdateBtn = (ImageView)findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);
        mCitySelect=(ImageView)findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);
        mUpdateProgressBar=(ProgressBar)findViewById(R.id.title_update_progress);

        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE)
            Toast.makeText(MainActivity.this,"网络连接正常", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(MainActivity.this,"网络挂了！", Toast.LENGTH_LONG).show();
        initView();

    }

    // 设置触发事件
    @Override
    public void onClick(View view){
        if(view.getId()==R.id.title_city_manager){
            Intent i=new Intent(this,SelectCity.class);
            if(newCityName!=null){
                i.putExtra("cityName",newCityName);
            }
            //
            //启动intent并且没有返回值：startActivity(i);
            //启动intent并且有返回值：
            startActivityForResult(i,1);
        }

        if (view.getId() == R.id.title_update_btn){
            if(newCityCode!=null){
                if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE)
                    queryWeatherCode(newCityCode);
                else
                    Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
            else {
                if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE)
                    queryWeatherCode("101010100");
                else
                    Toast.makeText(MainActivity.this,"网络挂了！",Toast.LENGTH_LONG).show();
            }
        }

    }

    // onActivityResult函数用于接收返回的intent数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK){
            if(data.getStringExtra("cityName")!=null){
                newCityCode= data.getStringExtra("cityCode");
                newCityName= data.getStringExtra("cityName");
                Log.d("myWeather", "选择的城市代码为"+newCityCode);
                if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                    queryWeatherCode(newCityCode);
                }
                else{
                    Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    // 获取网络数据
    private void queryWeatherCode(String cityCode)
    {
        mUpdateBtn.setVisibility(View.GONE);
        mUpdateProgressBar.setVisibility(View.VISIBLE);

        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeather", address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con=null;
                TodayWeather todayWeather;
                try{
                    URL url = new URL(address);
                    con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while((str=reader.readLine()) != null){
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr=response.toString();
                    Log.d("myWeather", responseStr);
                    // 在获取网络数据后，调用解析函数，返回新的对象
                    todayWeather=parseXML(responseStr);
                    // 消息机制传递给主线程
                    if(todayWeather!=null){
                        Log.d("myWeather",todayWeather.toString());
                        Message msg =new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj=todayWeather;
                        mHandler.sendMessage(msg);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(con != null){
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    // 解析函数,并把解析好的数据存入到TodayWeather对象
    private TodayWeather parseXML(String xmldata) {
        TodayWeather todayWeather = null;
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    // 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
                        }
                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("date") && dateCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate1(xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("low") && lowCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow1(xmlPullParser.getText());
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("high") && highCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh1(xmlPullParser.getText());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType1(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 1) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli1(xmlPullParser.getText());
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("date") && dateCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate2(xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("low") && lowCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow2(xmlPullParser.getText());
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("high") && highCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh2(xmlPullParser.getText());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType2(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 2) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli2(xmlPullParser.getText());
                                fengliCount++;
                            }else if (xmlPullParser.getName().equals("date") && dateCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate3(xmlPullParser.getText());
                                dateCount++;
                            }else if (xmlPullParser.getName().equals("low") && lowCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow3(xmlPullParser.getText());
                                lowCount++;
                            }else if (xmlPullParser.getName().equals("high") && highCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh3(xmlPullParser.getText());
                                highCount++;
                            }else if (xmlPullParser.getName().equals("type") && typeCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType3(xmlPullParser.getText());
                                typeCount++;
                            }else if (xmlPullParser.getName().equals("fengli") && fengliCount == 3) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli3(xmlPullParser.getText());
                                fengliCount++;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                // 进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return todayWeather;
    }

    // 初始化控件内容
    void initView(){
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);

        // future
        week1=(TextView)findViewById(R.id.weather_future1_week);
        temperature1=(TextView)findViewById(R.id.weather_future1_temperature);
        climate1=(TextView)findViewById(R.id.weather_future1_state);
        wind1=(TextView)findViewById(R.id.weather_future1_wind);

        week2=(TextView)findViewById(R.id.weather_future2_week);
        temperature2=(TextView)findViewById(R.id.weather_future2_temperature);
        climate2=(TextView)findViewById(R.id.weather_future2_state);
        wind2=(TextView)findViewById(R.id.weather_future2_wind);

        week3=(TextView)findViewById(R.id.weather_future3_week);
        temperature3=(TextView)findViewById(R.id.weather_future3_temperature);
        climate3=(TextView)findViewById(R.id.weather_future3_state);
        wind3=(TextView)findViewById(R.id.weather_future3_wind);

        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");

        week1.setText("N/A");
        temperature1.setText("N/A");
        climate1.setText("N/A");
        wind1.setText("N/A");

        week2.setText("N/A");
        temperature2.setText("N/A");
        climate2.setText("N/A");
        wind2.setText("N/A");

        week3.setText("N/A");
        temperature3.setText("N/A");
        climate3.setText("N/A");
        wind3.setText("N/A");
    }

    void updateTodayWeather(TodayWeather todayWeather){
        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+ "发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());

        week1.setText(todayWeather.getDate1());
        temperature1.setText(todayWeather.getHigh1()+"~"+todayWeather.getLow1());
        climate1.setText(todayWeather.getType1());
        wind1.setText("风力"+todayWeather.getFengli1());

        week2.setText(todayWeather.getDate2());
        temperature2.setText(todayWeather.getHigh2()+"~"+todayWeather.getLow2());
        climate2.setText(todayWeather.getType2());
        wind2.setText("风力"+todayWeather.getFengli2());

        week3.setText(todayWeather.getDate3());
        temperature3.setText(todayWeather.getHigh3()+"~"+todayWeather.getLow3());
        climate3.setText(todayWeather.getType3());
        wind3.setText("风力"+todayWeather.getFengli3());
        Toast.makeText(MainActivity.this,"当前城市更新成功！",Toast.LENGTH_SHORT).show();
    }



}
