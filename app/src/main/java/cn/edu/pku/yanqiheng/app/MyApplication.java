package cn.edu.pku.yanqiheng.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.yanqiheng.bean.City;
import cn.edu.pku.yanqiheng.db.CityDB;

public class MyApplication extends Application {

    private static final String TAG="MyAPP";
    private static MyApplication mApplication;
    private CityDB mCityDB;
    private List<City> mCityList;




    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"MyApplication->Oncreate");
        mApplication=this;
        // 打开数据库
        mCityDB = openCityDB();
        // 初始化城市信息列表
        initCityList();
    }

    // 创建getInstance方法
    public static MyApplication getInstance(){
        return mApplication;
    }

    // 创建打开数据库的方法，并返回一个数据库对象CityDB
    private CityDB openCityDB(){
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath()
                + File.separator + getPackageName()
                + File.separator + "databases1"
                + File.separator
                + CityDB.CITY_DB_NAME;
        File db = new File(path);
        Log.d(TAG,path);
        // 推断数据库文件是否存在,若不存在则运行导入,否则直接打开数据库
        if (!db.exists()){
            // 打开对应文件夹
            String pathfolder = "/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + getPackageName()
                    + File.separator + "databases1"
                    + File.separator;
            File dirFirstFolder = new File(pathfolder);
            // 假设没有这个文件夹,则创建
            if(!dirFirstFolder.exists()){
                dirFirstFolder.mkdirs();
                Log.i("MyApp","mkdirs");
            }
            Log.i("MyApp","db is not exists");
            try{
                InputStream is = getAssets().open("city.db");
                FileOutputStream fos = new FileOutputStream(db);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) != -1){
                    fos.write(buffer, 0, len);
                    fos.flush();
                }
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this, path);
    }

    private void initCityList(){
        mCityList = new ArrayList<City>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                /* TODO Auto-generated method stub */
                prepareCityList();
            }

        }).start();
    }

    private boolean prepareCityList() {
        mCityList = mCityDB.getAllCity();
        int i=0;
        for (City city : mCityList) {
            i++;
            String cityName = city.getCity();
            String cityCode = city.getNumber();
            Log.d(TAG,cityCode+":"+cityName);
        }
        Log.d(TAG,"i="+i);
        return true;
    }

    public List<City> getCityList() {
        return mCityList;
    }


}
