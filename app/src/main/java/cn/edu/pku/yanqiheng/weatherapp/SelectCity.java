package cn.edu.pku.yanqiheng.weatherapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.view.View;

public class SelectCity extends Activity implements View.OnClickListener {

    private ImageView mBackBtn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);//设置布局
        mBackBtn=(ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        if(view.getId()==R.id.title_back){
            //在finish之前，传递数据
            Intent i = new Intent();
            i.putExtra("cityCode", "101160101");
            setResult(RESULT_OK, i);
            finish();
        }
    }




}
