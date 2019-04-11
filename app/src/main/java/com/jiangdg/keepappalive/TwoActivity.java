package com.jiangdg.keepappalive;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * 测试界面切换 定时器是否被杀死
 */
public class TwoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two);
    }
}
