package com.demomaster.weimusic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.demomaster.weimusic.R;

import cn.demomaster.huan.quickdeveloplibrary.base.activity.QDActivity;
import cn.demomaster.huan.quickdeveloplibrary.view.colorpicker.ColorPicker;

public class SelectThemeColorActivity extends QDActivity {
    TextView tv_color;
    ColorPicker cp_color;
    Button btn_ok;
    int mColor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_theme_color);
        cp_color = findViewById(R.id.cp_color);
        tv_color = findViewById(R.id.tv_color);
        cp_color.setOnSelectChangeListener(new ColorPicker.OnSelectChangeListener() {
            @Override
            public void onSelectChange(int color) {
                mColor = color;
                tv_color.setText("" + color);
            }
        });
        Intent intent = getIntent();
        if(intent!=null){
            Bundle bundle = intent.getExtras();
            mColor = bundle.getInt("themeColor");
            cp_color.setColor(mColor);
        }
        tv_color.setText("" + cp_color.getColor());
        btn_ok = findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                Bundle bundle = new Bundle();
                bundle.putInt("themeColor",mColor);
                intent.putExtras(bundle);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }

}