package org.biantan.bilifm3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.thl.filechooser.FileChooser;
import com.thl.filechooser.FileInfo;

import java.io.File;


public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private String save_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        toolbar.setNavigationIcon(R.drawable.back);//设置导航栏图标
        toolbar.setTitle(R.string.action_settings);//设置主标题
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        init();//初始化

    }

    public void init() {
        TextView save_path_id = (TextView) findViewById(R.id.save_path_text);
        SharedPreferences pref = getSharedPreferences("settings_data", MODE_PRIVATE );
        save_path = pref.getString("save_path", ""); //获取保存路径的数据
        save_path_id.setText(save_path);//设置保存路径的数据
        LinearLayout save_path_button = (LinearLayout) findViewById(R.id.save_path_button);
        save_path_button.setOnClickListener(this);
    }

    /**
     * 按钮事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_path_button:
                FileChooser fileChooser = new FileChooser(this, new FileChooser.FileChoosenListener() {
                    @Override
                    public void onFileChoosen(String filePath) {
                        ((TextView) findViewById(R.id.save_path_text)).setText(filePath);
                        SharedPreferences pref = getSharedPreferences("settings_data", MODE_PRIVATE );
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putString("save_path",filePath);
                        edit.commit();
                    }
                });
                fileChooser.setCurrentPath(save_path);
                fileChooser.setTitle("设置保存路径");
                fileChooser.setDoneText("确定");
                fileChooser.setBackIconRes(R.drawable.back);
                fileChooser.setThemeColor(R.color.save_path);
                fileChooser.setChooseType(FileInfo.FILE_TYPE_FOLDER);
                fileChooser.showFile(false); //不显示文件
                fileChooser.open();
                break;
        }
    }
}
