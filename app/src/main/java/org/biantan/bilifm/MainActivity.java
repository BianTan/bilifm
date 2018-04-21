package org.biantan.bilifm;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private int REQUEST_CODE_SCAN = 111;
    private int cwxx = 1;
    private ProgressDialog dialog;
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36";
    private String urlx = "https://www.bilibili.com/video/av";
    private String SearchName;
    private String cover;
    private String title;
    private String up_img;
    private String user;
    private String sign;
    private String info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();  //初始化

        final EditText input = (EditText) findViewById(R.id.Search_AV);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //当actionId == XX_SEND 或者 XX_DONE时都触发
                //或者event.getKeyCode == ENTER 且 event.getAction == ACTION_DOWN时也触发
                //注意，这是一定要判断event != null。因为在某些输入法上会返回null。
                if (actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction())) {
                    SearchName = input.getText().toString();
                    CoverSearch();
                }
                return false;
            }
        });

    }

    /**
     * 按钮事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan:
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SCAN);
                break;
            case R.id.search_button:
                EditText input = (EditText) findViewById(R.id.Search_AV);
                SearchName = input.getText().toString();
                CoverSearch();
                break;
            default:
                break;
        }
    }

    /**
     * 初始化
     */
    public void init() {
        //注册按钮
        FloatingActionButton scan = (FloatingActionButton) findViewById(R.id.scan);
        ImageButton search_button = (ImageButton) findViewById(R.id.search_button);
        scan.setOnClickListener(this);
        search_button.setOnClickListener(this);

        //设置图片控件参数
        ImageView cover = (ImageView) findViewById(R.id.cover);  //绑定封面图片控件id
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;  //获取屏幕width
        cover.setMinimumHeight((20 * (screenWidth - 24) / 32) - 28);  //设置封面大小

        //接收
        Intent intent=getIntent();
        String action=intent.getAction();
        String type=intent.getType();
        if(action.equals(Intent.ACTION_SEND)&&type.equals("text/plain")){
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if(sharedText != null){
                int a = sharedText.indexOf("www.bilibili.com/video/av");
                int b = sharedText.indexOf("?share_medium=android");
                if (a > 0 && b > 0) {
                    EditText input = (EditText) findViewById(R.id.Search_AV);
                    SearchName = sharedText.substring(a + 25, b);
                    input.setText(SearchName);
                    CoverSearch();
                } else {
                    Toast.makeText(MainActivity.this, "不是有效的链接哦Orz", Toast.LENGTH_SHORT);
                }
            }
        }

        //控件长按
        cover.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final String[] items = {"百度识图", "分享封面","保存到本地","系统浏览器打开","本地哔哩哔哩打开视频"};
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("选择操作")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (items[which] == "百度识图") {

                                }
                            }
                        });
                dialog.show();
                return false;
            }
        });

    }

    /**
     * 线程
     */
    Runnable GetCover = new Runnable() {
        @Override
        public void run() {
            if (SearchName != null && !SearchName.equals("")){
                String url = urlx + SearchName;
                Connection conn = Jsoup.connect(url);
                conn.header("User-Agent", userAgent);  // 修改http包中的header,伪装成浏览器进行抓取
                Document doc = null;
                try {
                    doc = conn.get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //判断是否是错误的AV号
                String cwxxString = doc.getElementsByClass("error-text").text();
                if( cwxxString.equals("啊叻？视频不见了？") ) {
                    dialog.dismiss();
                    cwxx = 1;
                } else {
                    cover = doc.getElementsByAttributeValue("itemprop","thumbnailUrl").first().attr("content");
                    title = doc.getElementsByClass("video-info-m").first().getElementsByTag("h1").attr("title");
                    Element up_info = doc.getElementsByClass("up-info-m").first();
                    String img = up_info.getElementsByClass("u-face").first().getElementsByTag("img").first().attr("src");
                    int intimg = img.indexOf("@");
                    if ( intimg >= 0 ) {
                        up_img = "https:" + img.substring(0, intimg);
                    } else {
                        up_img = "https:" + img;
                    }
                    user = up_info.getElementsByClass("user").get(0).getElementsByTag("a").first().text();
                    sign = up_info.getElementsByClass("sign").get(0).text();
                    info = doc.getElementsByClass("video-desc-m").first().getElementsByClass("info").text();
                    cwxx = 2;
                    dialog.dismiss();
                }
            } else {
                cwxx = 0;
                dialog.dismiss();
            }
            Message message = new Message();
            handler.sendMessage(message); // 将 Message 对象发送出去
        }
    };

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if ( cwxx == 0 ) {
                Toast.makeText(MainActivity.this, "输入的 AV 号不能为空哦 _(:3...", Toast.LENGTH_SHORT).show();
            } else if( cwxx == 1 ) {
                Toast.makeText(MainActivity.this, "输入的 AV 号有误哦 _(:3...", Toast.LENGTH_SHORT).show();
            } else if ( cwxx == 2 ) {
                TextView upnamecr = (TextView) findViewById(R.id.upname);
                TextView signcr = (TextView) findViewById(R.id.sign);
                TextView Titlecr = (TextView) findViewById(R.id.Title);
                TextView infocr = (TextView) findViewById(R.id.info);
                ImageView covercr = (ImageView) findViewById(R.id.cover);
                ImageView up_imgcr = (ImageView) findViewById(R.id.up_img);
                upnamecr.setText(user);
                signcr.setText(sign);
                Titlecr.setText(title);
                infocr.setText(info);
                Glide.with(MainActivity.this)
                        .load(cover)
                        .error(R.drawable.akari)//图片加载失败后，显示的图片
                        .fitCenter()//等比拉伸
                        .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                        .into(covercr);
                Glide.with(MainActivity.this)
                        .load(up_img)
                        .error(R.drawable.akari)//图片加载失败后，显示的图片
                        .fitCenter()//等比拉伸
                        .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                        .into(up_imgcr);
                //显示控件
                LinearLayout main = (LinearLayout) findViewById(R.id.info_owo);
                main.setVisibility(View.VISIBLE);
            }

        }
    };

    /**
     * Search
     */
    public void CoverSearch() {
        //关闭软键盘
        closeKeyboard();
        //显示获取提示窗口
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("正在获取请稍后...");
        dialog.setCancelable(false);
        dialog.show();
        if (isConnectIsNomarl()) { //判断网络是否连接
            Log.d("SearchName",SearchName);
            //隐藏控件
            LinearLayout main = (LinearLayout) findViewById(R.id.info_owo);
            main.setVisibility(View.GONE);
            new Thread(GetCover).start();  // 启动子线程获取数据
        } else {
            dialog.dismiss();
        }
    }

    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNomarl() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String intentName = info.getTypeName();
            Log.d("Intent", "当前网络名称：" + intentName);
            return true;
        } else {
            Log.d("Intent", "没有可用网络");
            return false;
        }
    }

    /**
     * 二维码
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                Log.d("扫描结果为：", content);
                int a = content.indexOf("www.bilibili.com/video/av");
                int b = content.indexOf("/?bsource=pc_web");
                if (a > 0 && b > 0) {
                    EditText input = (EditText) findViewById(R.id.Search_AV);
                    SearchName = content.substring(a + 25, b);
                    input.setText(SearchName);
                    CoverSearch();
                } else {
                    Toast.makeText(MainActivity.this, "扫...扫错了吧！TAT", Toast.LENGTH_SHORT);
                }
            }
        }
    }

    /**
     * 关闭软键盘
     */
    private void closeKeyboard() {
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
