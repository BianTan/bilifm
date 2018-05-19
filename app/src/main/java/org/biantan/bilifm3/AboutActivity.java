package org.biantan.bilifm3;

import android.content.Intent;
import android.didikee.donate.AlipayDonate;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar3);
        toolbar.setNavigationIcon(R.drawable.back);//设置导航栏图标
        toolbar.setTitle(R.string.action_about);//设置主标题
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        LinearLayout blog_button = (LinearLayout) findViewById(R.id.blog_button);
        blog_button.setOnClickListener(this);
        LinearLayout ds_button = (LinearLayout) findViewById(R.id.ds_button);
        ds_button.setOnClickListener(this);
        LinearLayout git_button = (LinearLayout) findViewById(R.id.git_button);
        git_button.setOnClickListener(this);
    }

    /**
     * 按钮事件
     */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        switch (v.getId()) {
            case R.id.blog_button:
                intent.setData(Uri.parse("https://biantan.org"));
                startActivity(intent);
                break;
            case R.id.git_button:
                intent.setData(Uri.parse("https://github.com/BianTan/bilifm"));
                startActivity(intent);
                break;
            case R.id.ds_button:
                donateAlipay("FKX04117IL7APH3OJJ8J6E");
                break;
            default:
                break;
        }
    }

    private void donateAlipay(String payCode) {
        Toast.makeText(this, "感谢支持OwO", Toast.LENGTH_SHORT).show();
        boolean hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(this);
        if (hasInstalledAlipayClient) {
            AlipayDonate.startAlipayClient(this, payCode);
        }
    }
}
