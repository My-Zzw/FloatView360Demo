package com.example.administrator.floatview360demo;

import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import service.MyFloatService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        //动态权限申请.不单独针对某一危险权限。弹出信任此应用页面，允许该应用所有的权限。
        if (Build.VERSION.SDK_INT >= 23) {

            if (Settings.canDrawOverlays(this)) {
                //如果赋权直接运行如下程序
                Intent intent = new Intent(this, MyFloatService.class);
                startService(intent);
            } else {
                // 跳转到相关的设置权限设置页面
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

        }
        finish();
    }
}
