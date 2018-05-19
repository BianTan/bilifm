package org.biantan.bilifm3;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
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
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private int REQUEST_CODE_SCAN = 111;
    private int onDownload = 2;
    private int onPermissions = 1;
    private int cwxx = 3;
    private int md_xx = 3;
    private int live_xx = 3;
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
    private String md_score;
    private String md_NOP;
    private String save_path;
    private String save_id_url;
    private int md_score2;
    private int Search_option_id = 1;

    //首先还是先声明这个Spinner控件
    private Spinner spinner;
    //定义一个String类型的List数组作为数据源
    private List<String> dataList;
    //定义一个ArrayAdapter适配器作为spinner的数据适配器
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //初始化
        init();
        final EditText input = (EditText) findViewById(R.id.Search_AV);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            // 输入法回车响应事件
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
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

    @Override
    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();
        if(action.equals(Intent.ACTION_SEND)&&type.equals("text/plain")){
            handlerText(intent);
        }
    }

    /**
     * 按钮事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan:
                initPermission();
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
     * 权限相关
     */
    private void initPermission(){
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SCAN);
        } else {
            EasyPermissions.requestPermissions(this, "扫描二维码需要摄像头权限哦_(:3", 0, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    //成功
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    //失败
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        if (onPermissions == 1) {
            String[] perms = {Manifest.permission.CAMERA};
            EasyPermissions.requestPermissions(this, "扫描二维码需要摄像头权限啦！麻烦授权一下~", 0, perms);
            onPermissions = 2;
        }

    }

    /**
     * 接收分享的文本
     */
    public void fx(){
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if(action.equals(Intent.ACTION_SEND) && type.equals("text/plain")){
            handlerText(intent);
        }
    }
    private void handlerText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        int a = sharedText.indexOf("www.bilibili.com/video/av");
        int b = sharedText.indexOf("?share_medium=android");
        int c = sharedText.indexOf("bangumi.bilibili.com/review/media/");
        int d = sharedText.indexOf("live.bilibili.com/live/");
        int e = sharedText.indexOf(".html");
        int f = sharedText.indexOf("www.bilibili.com/read/cv");
        if(sharedText != null) {
            if (a >= 0 && b >= 0) { //如果分享链接为视频时
                spinner.setSelection(0);
                Search_option_id = 1;
                EditText input = (EditText) findViewById(R.id.Search_AV);
                SearchName = sharedText.substring(a + 25, b);
                input.setText(SearchName);
                CoverSearch();
            } else if (c >= 0) {    //如果分享链接为番剧时
                spinner.setSelection(1);
                Search_option_id = 2;
                EditText input = (EditText) findViewById(R.id.Search_AV);
                SearchName = sharedText.substring(c + 34);
                input.setText(SearchName);
                CoverSearch();
            } else if (d >= 0 && e >= 0) {  //如果分享链接为直播时
                spinner.setSelection(2);
                Search_option_id = 3;
                EditText input = (EditText) findViewById(R.id.Search_AV);
                SearchName = sharedText.substring(d + 23, e);
                input.setText(SearchName);
                CoverSearch();
            } else if (f >= 0) {    //如果分享链接为专栏时
//                spinner.setSelection(3);
//                Search_option_id = 4;
                Toast.makeText(MainActivity.this, "建设中Orz...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "不是有效的链接哦Orz...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 初始化
     */
    public void init() {
        //读取配置
        SharedPreferences pref = getSharedPreferences("settings_data", MODE_PRIVATE );
        save_path = pref.getString("save_path", "");
        if (save_path == "") {
            save_path = "/storage/emulated/0/Android/data/org.biantan.bilifm3/download";
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("save_path",save_path);
            edit.commit();
        }

        //隐藏因 ScrollView 导致的启动时自动弹出的输入法
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //绑定下拉菜单控件
        spinner = (Spinner) findViewById(R.id.Search_option);
        //为dataList赋值，将下面这些数据添加到数据源中
        dataList = new ArrayList<String>();
        dataList.add("视频");
        dataList.add("番剧");
        dataList.add("直播");
//        dataList.add("专栏");
        /*
        为spinner定义适配器，也就是将数据源存入adapter，这里需要三个参数
        1. 第一个是Context（当前上下文），这里就是this
        2. 第二个是spinner的布局样式，这里用android系统提供的一个样式
        3. 第三个就是spinner的数据源，这里就是dataList
        */
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,dataList);
        //为适配器设置下拉列表下拉时的菜单样式。
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //为spinner绑定我们定义好的数据适配器
        spinner.setAdapter(adapter);
        //为spinner绑定监听器，这里我们使用匿名内部类的方式实现监听器
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String Item = adapter.getItem(position);
                EditText input = (EditText) findViewById(R.id.Search_AV);
                switch (Item){
                    case "视频":
                        Search_option_id = 1;
                        input.setHint("AV号");
                        break;
                    case "番剧":
                        Search_option_id = 2;
                        input.setHint("MD号");
                        break;
                    case "直播":
                        Search_option_id = 3;
                        input.setHint("房间号");
                        break;
                    case "专栏":
                        Search_option_id = 4;
                        input.setHint("CV号");
                        break;
                    default:
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 接收分享的文本
        fx();

        //注册按钮
        FloatingActionButton scan = (FloatingActionButton) findViewById(R.id.scan);
        ImageButton search_button = (ImageButton) findViewById(R.id.search_button);
        scan.setOnClickListener(this);
        search_button.setOnClickListener(this);

        //设置图片控件参数
        //绑定封面图片控件id
        ImageView coverid = (ImageView) findViewById(R.id.cover);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;  //获取屏幕width
        coverid.setMinimumHeight((20 * (screenWidth - 24) / 32) - 32);  //设置封面大小

        //控件长按
        coverid.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                switch (Search_option_id) {
                    case 1:
                        long_press(save_path + "/AV", 1);
                        break;
                    case 2:
                        long_press(save_path + "/MD", 2);
                        break;
                    case 3:
                        long_press(save_path + "/Live", 3);
                        break;
                    case 4:
                        long_press(save_path + "/CV", 4);
                        break;
                }
                return false;
            }
        });

    }

    /**
     * 长按事件
     */
    public void long_press(final String saveDir, final int save_id2) {
        final String[] items = {"百度识图", "保存到本地", "浏览器打开图片", "本地哔哩哔哩打开"};
        final String imgurl = cover;
        final String hm = SearchName;
        final String saveDir2 = saveDir.substring(20);
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("选择操作")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (items[which]){
                            case "百度识图":
                                String url1 = "http://image.baidu.com/wiseshitu?guess=1&queryImageUrl=" + imgurl;
                                Intent intent1 = new Intent(Intent.ACTION_VIEW);
                                intent1.setData(Uri.parse(url1));
                                startActivity(intent1);
                                break;
                            case "保存到本地":
                                Toast.makeText(MainActivity.this, "保存中...", Toast.LENGTH_SHORT).show();
                                DownloadUtil.get().download(imgurl, saveDir2, hm, new DownloadUtil.OnDownloadListener() {
                                    @Override
                                    public void onDownloadSuccess() {
                                        onDownload = 1;
                                        Message message = new Message();
                                        handler.sendMessage(message); // 将 Message 对象发送出去
                                    }
                                    @Override
                                    public void onDownloading(int progress) { //下载进度
                                    }
                                    @Override
                                    public void onDownloadFailed() {
                                        onDownload = 0;
                                        Message message = new Message();
                                        handler.sendMessage(message); // 将 Message 对象发送出去
                                    }
                                });
                                break;
                            case "浏览器打开图片":
                                Intent intent3 = new Intent(Intent.ACTION_VIEW);
                                intent3.setData(Uri.parse(imgurl));
                                startActivity(intent3);
                                break;
                            case "本地哔哩哔哩打开":
                                if (save_id2 == 1) {
                                    save_id_url = "https://www.bilibili.com/video/av" + hm;
                                } else if (save_id2 == 2) {
                                    save_id_url = "https://www.bilibili.com/bangumi/media/md" + hm;
                                } else if (save_id2 == 3) {
                                    save_id_url = "https://live.bilibili.com/" + hm;
                                } else if (save_id2 == 4) {
                                    save_id_url = "https://www.bilibili.com/read/cv" + hm;
                                }
                                ComponentName componentName = new ComponentName("tv.danmaku.bili", "tv.danmaku.bili.ui.intent.IntentHandlerActivity");
                                Intent intent4 = new Intent(Intent.ACTION_VIEW);
                                intent4.setComponent(componentName);
                                intent4.setData(Uri.parse(save_id_url));
                                startActivity(intent4);
                                break;
                        }
                    }
                });
        dialog.show();
    }

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
            //隐藏控件
            LinearLayout main = (LinearLayout) findViewById(R.id.info_owo);
            LinearLayout main2 = (LinearLayout) findViewById(R.id.md_main);
            CardView main3 = (CardView) findViewById(R.id.cover_layout);
            main.setVisibility(View.GONE);
            main2.setVisibility(View.GONE);
            main3.setVisibility(View.GONE);
            switch (Search_option_id) {
                case 1:
                    new Thread(GetCover_1).start();  // 启动子线程获取数据
                    break;
                case 2:
                    new Thread(GetCover_2).start();  // 启动子线程获取数据
                    break;
                case 3:
                    new Thread(GetCover_3).start();  // 启动子线程获取数据
                    break;
                case 4:
                    new Thread(GetCover_4).start();  // 启动子线程获取数据
                    break;
                default:
                    break;
            }
        } else {
            dialog.dismiss();
        }
    }

    /**
     * 获取视频封面线程
     */
    Runnable GetCover_1 = new Runnable() {
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
                    md_xx = 3;
                    live_xx = 3;
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

    /**
     * 获取番剧封面线程
     */
    Runnable GetCover_2 = new Runnable() {
        @Override
        public void run() {
            if (SearchName != null && !SearchName.equals("")){
                String url = "https://www.bilibili.com/bangumi/media/md" + SearchName;
                Connection conn = Jsoup.connect(url);
                conn.header("User-Agent", userAgent);  // 修改http包中的header,伪装成浏览器进行抓取
                Document doc = null;
                try {
                    doc = conn.get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //判断是否是错误的MD号
                String mdxxString = doc.title();
                Log.d("title", mdxxString);
                if( mdxxString.indexOf("出错啦") >= 0 ) {
                    dialog.dismiss();
                    md_xx = 1;
                } else {
                    cover = doc.getElementsByAttributeValue("property","og:image").first().attr("content");
                    title = doc.getElementsByAttributeValue("property","og:title").first().attr("content");
                    info = doc.getElementsByAttributeValue("name","description").first().attr("content") + "......";
                    md_score = doc.getElementsByClass("media-info-score-content").first().child(0).text();
                    md_NOP = doc.getElementsByClass("media-info-score-content").first().child(1).text();
                    //      分数格式  a.b
                    int fs2 = md_score.indexOf(".");                //      获取 "."
                    String fsq = md_score.substring(0, fs2);        //      截取前面数字a
                    String fsh = md_score.substring(fs2 + 1);
                    int intfs = Integer.parseInt(fsq);        //      转换为int格式
                    int intfsh = Integer.parseInt(fsh);
                    if (fsh == "0") {
                        md_score2 = intfs;
                    } else if (intfsh <= 9) {
                        intfs = intfs + 1;
                        md_score2 = intfs;
                    } else if (intfs > 9) {
                        md_score2 = intfs;
                    }
                    md_xx = 2;
                    cwxx = 3;
                    live_xx = 3;
                    dialog.dismiss();
                }
            } else {
                md_xx = 0;
                dialog.dismiss();
            }
            Message message = new Message();
            handler.sendMessage(message); // 将 Message 对象发送出去
        }
    };

    /**
     * 获取直播封面线程
     */
    Runnable GetCover_3 = new Runnable() {
        @Override
        public void run() {
            if (SearchName != null && !SearchName.equals("")){
                String url = "http://biantan.org/bili/?room_id=" + SearchName + "&type=2";
                Log.d("url",url);
                Connection conn = Jsoup.connect(url);
                conn.header("User-Agent", userAgent);  // 修改http包中的header,伪装成浏览器进行抓取
                Document doc = null;
                try {
                    doc = conn.get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String message = doc.getElementsByClass("root").first().child(0).text();
                if (message.equals("ok")){
                    title = doc.getElementsByClass("root").first().child(1).text();
                    cover = doc.getElementsByClass("root").first().child(2).text();
                    if (cover != null) {
                        live_xx = 2;
                    } else {
                        live_xx = 4;
                    }
                } else if (message.equals("0")) {
                    live_xx = 1;
                }
                md_xx = 3;
                cwxx = 3;
                dialog.dismiss();
            } else {
                live_xx = 0;
                dialog.dismiss();
            }
            Message message = new Message();
            handler.sendMessage(message); // 将 Message 对象发送出去
        }
    };

    /**
     * 获取专栏封面线程
     */
    Runnable GetCover_4 = new Runnable() {
        @Override
        public void run() {
            if (SearchName != null && !SearchName.equals("")){

            }
        }
    };

    // UI处理
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            cover_ui();//视频封面UI操作
            md_ui();//番剧封面UI操作
            live_ui();//直播封面UI操作
            down_satate();//判断下载状态
        }
    };

    /**
     * 视频封面下载UI操作
     */
    public void cover_ui() {
        if ( cwxx == 0 ) {
            Toast.makeText(MainActivity.this, "AV号不能为空哦 _(:3...", Toast.LENGTH_SHORT).show();
        } else if( cwxx == 1 ) {
            Toast.makeText(MainActivity.this, "输入的AV号有误哦 _(:3...", Toast.LENGTH_SHORT).show();
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
                    .into(covercr);
            Glide.with(MainActivity.this)
                    .load(up_img)
                    .error(R.drawable.akari)//图片加载失败后，显示的图片
                    .fitCenter()//等比拉伸
                    .into(up_imgcr);
            //控件状态
            LinearLayout main = (LinearLayout) findViewById(R.id.info_owo);
            CardView main2 = (CardView) findViewById(R.id.cover_layout);
            main.setVisibility(View.VISIBLE);
            main2.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 番剧封面下载UI操作
     */
    public void md_ui() {
        if ( md_xx == 0 ) {
            Toast.makeText(MainActivity.this, "MD号不能为空哦 _(:3...", Toast.LENGTH_SHORT).show();
        } else if( md_xx == 1 ) {
            Toast.makeText(MainActivity.this, "输入的MD号有误哦 也可能这个番仅港澳台啦 _(:3...", Toast.LENGTH_SHORT).show();
        } else if (md_xx == 2) {
            ImageView covercr = (ImageView) findViewById(R.id.cover);
            Glide.with(MainActivity.this)
                    .load(cover)
                    .error(R.drawable.akari)//图片加载失败后，显示的图片
                    .fitCenter()//等比拉伸
                    .into(covercr);
            TextView Titlecr = (TextView) findViewById(R.id.md_name);
            TextView infocr = (TextView) findViewById(R.id.md_info);
            TextView md_NOPcr = (TextView) findViewById(R.id.md_NOP);
            TextView md_scorecr = (TextView) findViewById(R.id.md_score);
            RatingBar RatingBar = (RatingBar) findViewById(R.id.ratingBar);
            Titlecr.setText(title);
            infocr.setText(info);
            md_NOPcr.setText(md_NOP);
            md_scorecr.setText(md_score);
            RatingBar.setProgress(md_score2);
            //控件状态
            LinearLayout main = (LinearLayout) findViewById(R.id.md_main);
            CardView main2 = (CardView) findViewById(R.id.cover_layout);
            main.setVisibility(View.VISIBLE);
            main2.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 直播UI操作
     */
    public void live_ui() {
        if ( live_xx == 0 ) {
            Toast.makeText(MainActivity.this, "房间号不能为空哦 _(:3...", Toast.LENGTH_SHORT).show();
        } else if( live_xx == 1 ) {
            Toast.makeText(MainActivity.this, "输入的房间号有误哦 _(:3...", Toast.LENGTH_SHORT).show();
        } else if (live_xx == 2) {
            ImageView covercr = (ImageView) findViewById(R.id.cover);
            Glide.with(MainActivity.this)
                    .load(cover)
                    .error(R.drawable.akari)//图片加载失败后，显示的图片
                    .fitCenter()//等比拉伸
                    .into(covercr);
            //控件状态
            CardView main2 = (CardView) findViewById(R.id.cover_layout);
            main2.setVisibility(View.VISIBLE);
        } else if (live_xx == 4) {
            Toast.makeText(MainActivity.this, "这个直播间没有封面哦 _(:3...", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 判断下载状态
     */
    public void down_satate() {
        if (onDownload == 1) {
            Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
            onDownload = 2;
        } else if (onDownload == 0) {
            Toast.makeText(MainActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
            onDownload = 2;
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
                String sharedText = data.getStringExtra(Constant.CODED_CONTENT);
                Log.d("扫描结果为：", sharedText);
                int a = sharedText.indexOf("www.bilibili.com/video/av");
                int b = sharedText.indexOf("/?bsource=pc_web");
                int c = sharedText.indexOf("www.bilibili.com/bangumi/media/md");
                int d = sharedText.indexOf("live.bilibili.com/");
                int e = sharedText.indexOf("?recommend_source");
                int f = sharedText.indexOf("www.bilibili.com/read/cv");
                int g = sharedText.indexOf("/",41);
                if(sharedText != null) {
                    if (a >= 0 && b >= 0) { //如果分享链接为视频时
                        spinner.setSelection(0);
                        Search_option_id = 1;
                        EditText input = (EditText) findViewById(R.id.Search_AV);
                        SearchName = sharedText.substring(a + 25, b);
                        input.setText(SearchName);
                        CoverSearch();
                    } else if (c >= 0) {    //如果分享链接为番剧时
                        spinner.setSelection(1);
                        Search_option_id = 2;
                        EditText input = (EditText) findViewById(R.id.Search_AV);
                        SearchName = sharedText.substring(c + 33, g);
                        input.setText(SearchName);
                        CoverSearch();
                    } else if (d >= 0 && e >= 0) {  //如果分享链接为直播时
                        spinner.setSelection(2);
                        Search_option_id = 3;
                        EditText input = (EditText) findViewById(R.id.Search_AV);
                        SearchName = sharedText.substring(d + 18, e);
                        input.setText(SearchName);
                        CoverSearch();
                    } else if (f >= 0) {    //如果分享链接为专栏时
//                spinner.setSelection(3);
//                Search_option_id = 4;
                        Toast.makeText(MainActivity.this, "建设中Orz...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "不是有效的链接哦Orz...", Toast.LENGTH_SHORT).show();
                    }
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
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.action_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            case R.id.action_help:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://biantan.org/?p=775"));
                startActivity(intent);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
