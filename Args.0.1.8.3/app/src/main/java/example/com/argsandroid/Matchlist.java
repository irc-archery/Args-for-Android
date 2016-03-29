package example.com.argsandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class Matchlist extends ActionBarActivity implements View.OnClickListener {

    LinearLayout ll1;
    int battle_id = 0;

    Button btn1;
    Button btn111;

    Point size;

    String m_str[];

    int id_set = 0;
    int ids =10;
    int count = 0;
    int toastcount=0;
    int width = 50;

    LayoutInflater inflater;
    View itemView;

    ArrayList<Integer> m_id;//試合のid
    ArrayList<Integer> length;//的までの距離
    ArrayList<Integer> players;//人数
    ArrayList<Integer> arrows;//射数
    ArrayList<Integer> perEnd;//試合開始日
    ArrayList<String> matchName;//試合名
    ArrayList<String> sponsor;//主催者
    ArrayList<String> created;//試合開始日


    ArrayList<View> ll;

    TextView make_t;
    LinearLayout ll_h;

    Intent room;
    Context context;

    boolean first_flag = true;
    boolean back_flag = true;
    boolean connect_flag = false;

    SharedPreferences sp = null;
    SharedPreferences.Editor spe = null;

    Dialog dialog;
    public  ProgressDialog m_ProgressDialog;

    Timer mTimer = null;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.matchlist);

        context = this;

        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        // ディスプレイのインスタンス生成
        Display disp = wm.getDefaultDisplay();
        size = new Point();
        disp.getSize(size);

        //SaredPrefrenceの初期化
        sp = getSharedPreferences("setting", MODE_MULTI_PROCESS);
        spe = sp.edit();

        //ArrayListの初期化
        m_id = new ArrayList<Integer>();
        length = new ArrayList<Integer>();
        players = new ArrayList<Integer>();
        arrows = new ArrayList<Integer>();
        perEnd = new ArrayList<Integer>();
        matchName = new ArrayList<String>();
        sponsor = new ArrayList<String>();
        created = new ArrayList<String>();
        ll = new ArrayList<View>();

        //buttonの初期化とクリックリスナーの付与
        btn1 = (Button)findViewById(R.id.button1);
        btn1.setOnClickListener(this);

        //LinearLayoutの初期化
        ll1 = (LinearLayout)findViewById(R.id.ll1);

        //intentの初期化
        room = new Intent(this,Scorelist.class);

        first_flag = true;
        back_flag = true;

        //アクションバー関連
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.matchlist);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //ll1にレイアウトが追加された際のコールバック
        ViewTreeObserver vto = ll1.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                        //llにデータが入っている限り繰り返す
                        if (ll.size() > 0) {
                            runOnUiThread(new Runnable() {

                                public void run() {
                                    if (ll.size() != 0) {
                                        //0番目のデータを表示し、削除する
                                        ll1.addView(ll.get(0));
                                        ll.remove(0);
                                    }
                                }
                            });
                        }
                        else if (count == 1) {
                            //レイアウトが一つでも追加されてたらダイアログを消す
                            back_flag = true;
                            if (m_ProgressDialog.isShowing()) {
                                m_ProgressDialog.dismiss();
                            }
                            //試合が削除されて移動した場合はそのことをユーザーに分かるようにダイアログで表示する
                            Intent geti = getIntent();
                            if(geti.getBooleanExtra("close", false)){
                                Toast.makeText(context,"試合作成者により試合が終了されましたので試合一覧へ移動しました",Toast.LENGTH_LONG).show();

//                                final AlertDialog[] alertDialog = new AlertDialog[1];
//                                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context,R.style.Theme_AppCompat_Light_Dialog_Alert));
//                                alertDialogBuilder.setTitle("試合が終了しました");
//                                alertDialogBuilder.setMessage("試合作成者により試合が終了されましたので試合一覧へ移動しました");
//                                alertDialogBuilder.setCancelable(false);
//                                alertDialogBuilder.setPositiveButton("OK!",
//                                        new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
//                                            }
//                                        });
//                                alertDialogBuilder.setCancelable(false);
//                                runOnUiThread(new Runnable() {
//                                    public void run() {
//                                        alertDialog[0] = alertDialogBuilder.create();
//                                        alertDialog[0].show();
//                                    }
//                                });
                            }
                        }

                } catch (Exception e) {

                }
            }
        });
        try {
        } catch (Exception e) {
            Toast.makeText(this, "error" + e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        //ネットワークに接続されているか確認する
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        //通信できないときにfalseを返す
        if (info == null || !info.isConnected()) {
            Toast.makeText(this, "ネットワークに接続されていません\nログイン画面に遷移します", Toast.LENGTH_LONG).show();

            spe.putString("sessionID", "");
            spe.commit();

            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[0].off();
            Connecthelper.socket[0].close();
            startActivity(intent);
        }
        try {
            connect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        //通信できたらプログレスダイアログを表示する
        m_ProgressDialog = new ProgressDialog(this);

        m_ProgressDialog.setMessage("少々お待ちください");

        m_ProgressDialog.setCancelable(false);

        if(!m_ProgressDialog.isShowing()) {
            m_ProgressDialog.show();
        }

        //タイマーを使用し、5秒間データが入ってこなかったらダイアログを消すようにする
        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    public void run() {
                        if(m_ProgressDialog.isShowing()) {
                            if (back_flag) {
                                Connecthelper.socket[0].disconnect();

                                first_flag = true;
                                try {
                                    connect();
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                m_ProgressDialog.dismiss();
                            }
                        }
                        mTimer.cancel();
                    }
                });
            }
        }, 2000, 2000);
    }
    //アクションバーのメニュー表示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_match, menu);
        return true;
    }


    //アクションバーのメニューのボタン処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            //ひとつ前のActivityに遷移する
            Intent i = new Intent(this,Mypage.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[0].off();
            Connecthelper.socket[0].close();
            startActivity(i);
            return true;
        }
        int id = item.getItemId();
        Intent warpanyway;

        //ログアウトし、sessionIDを消去してログインページへ遷移する
        if (id == R.id.menu_logout) {

            spe.putString("sessionID", "");
            spe.commit();

            warpanyway = new Intent(this,Login.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[0].off();
            Connecthelper.socket[0].close();
            startActivity(warpanyway);
            return true;
        }

        //試合作成へ遷移
        if (id == R.id.menu_createmat) {
            warpanyway = new Intent(this,Createmat.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[0].off();
            Connecthelper.socket[0].close();
            startActivity(warpanyway);
        }
        //マイページに遷移
        if (id == R.id.menu_mypage) {
            warpanyway = new Intent(this,Mypage.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[0].off();
            Connecthelper.socket[0].close();
            startActivity(warpanyway);
        }
        //団体ページに遷移
        if (id == R.id.menu_orgpage) {
            warpanyway = new Intent(this, Orgpage.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[0].off();
            Connecthelper.socket[0].close();
            startActivity(warpanyway);
        }

        return super.onOptionsItemSelected(item);
    }
    //接続用関数
    private void connect() throws MalformedURLException {
        System.setProperty("java.net.preferIPv6Addresses", "false");
        try{
            if(!Connecthelper.socket[0].hasListeners("extractMatchIndex")) {
                //onイベントの登録
                Connecthelper.socket[0].once("extractMatchIndex", extractMatchIndex);
            }
            if(!Connecthelper.socket[0].hasListeners("broadcastInsertMatch")) {
                Connecthelper.socket[0].once("broadcastInsertMatch", broadcastInsertMatch);
            }
            //接続
            Connecthelper.socket[0].connect();
            //最初の1回目だけif文に入るようにする
            if(first_flag == true) {
                //試合の情報を取得する
                JSONObject json = new JSONObject();
                json.put("sessionID",sp.getString("sessionID",""));
                //試合のデータを送ってもらうようemitする
                Connecthelper.socket[0].emit("extractMatchIndex", json);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //画面を割合で区切り、初期尾のレイアウトを表示する
    public boolean make_first(){

        //レイアウトを追加するための枠の作成
        ll_h = new LinearLayout(this);
        ll_h.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth()),(int)size.y/20);
        ll_h.setLayoutParams(params1);


        //試合名を並べるためのタイトルを作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("試合名");
        params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth())/2,(int)size.y/20);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        //主催を並べるためのタイトルを作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("主催");
        params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth())/3,(int)size.y/20);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        //矢印を並べるためのタイトルを作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("詳細");
        params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth())/6,(int)size.y/20);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        ll.add(ll_h);

        return true;
    }

    //試合の内容を動的に表示する
    public void make_room(boolean last){
        String str;

        //枠の作成
        ll_h = new LinearLayout(this);
        ll_h.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth()),(int)size.y/10);
        ll_h.setLayoutParams(params1);
        ll_h.setId(id_set);
        ll_h.setOnClickListener(in);

        //試合名を表示する
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        //試合名が長い場合は8文字目を「...」とする
        if(matchName.get(id_set).length() > 8){
            str = matchName.get(id_set).substring(0,7) + "...";
        }
        else{
            str = matchName.get(id_set);
        }
        make_t.setText(str);
        params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth())/2,(int)size.y/10);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
//        make_t.setTypeface(Typeface.createFromAsset(getAssets(), "JiyunoTsubasa.ttf"));
        ll_h.addView(make_t);

        //主催を表示する
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        //主催が長い場合は8文字目を「...」とする
        if(sponsor.get(id_set).length() > 6){
            str = sponsor.get(id_set).substring(0,5) + "...";
        }
        else{
            str = sponsor.get(id_set);
        }
        make_t.setText(str);
        params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth())/3,(int)size.y/10);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
//        make_t.setTypeface(Typeface.createFromAsset(getAssets(), "JiyunoTsubasa.ttf"));
        ll_h.addView(make_t);

        //矢印を表示する
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("⇒");
        params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth())/6,(int)size.y/10);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
//        make_t.setTypeface(Typeface.createFromAsset(getAssets(), "JiyunoTsubasa.ttf"));
        ll_h.addView(make_t);

        id_set++;

        //レイアウトをllに貯めておく
        ll.add(ll_h);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button1) {
            //試合作成の画面に遷移する
            Intent intent = new Intent(this, Createmat.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("flag", true);
//            Connecthelper.socket[0].disconnect();
//            Connecthelper.socket[0].close();
            Connecthelper.socket[0].off();
            Connecthelper.socket[0].close();
            startActivity(intent);
//            finish();
        }
    }

    public View.OnClickListener in = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {

            final int id =  view.getId();
            String len = null;

            if(length.get(view.getId()).toString().equals("0")){
                len = "90m";
            }
            if(length.get(view.getId()).toString().equals("1")){
                len = "70m";
            }
            if(length.get(view.getId()).toString().equals("2")){
                len = "60m";
            }
            if(length.get(view.getId()).toString().equals("3")){
                len = "50m";
            }
            if(length.get(view.getId()).toString().equals("4")){
                len = "40m";
            }
            if(length.get(view.getId()).toString().equals("5")){
                len = "30m";
            }
            if(length.get(view.getId()).toString().equals("6")){
                len = "70m前";
            }
            if(length.get(view.getId()).toString().equals("7")){
                len = "90m後";
            }
            if(length.get(view.getId()).toString().equals("8")){
                len = "18m";
            }


            //試合内容をダイアログで表示する
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context,R.style.Theme_AppCompat_Light_Dialog_Alert));
            alertDialogBuilder.setTitle("試合に参加しますか？");
            alertDialogBuilder.setMessage("試合開始日　:　" + created.get(view.getId()) +
                    "\n試合名　:　" + matchName.get(view.getId()) +
                    "\n主催者　:　" + sponsor.get(view.getId()) +
                    "\n人数　:　" + players.
                    get(view.getId()) +
                    "\n距離　:　" + len +
                    "\n射数　:　" + arrows.get(view.getId()) +
                    "\nセット数　:　" + perEnd.get(view.getId()));
            alertDialogBuilder.setNegativeButton("NO!",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            alertDialogBuilder.setPositiveButton("OK!",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            JSONObject json = new JSONObject();
                            try {
                                //得点表一覧へ遷移
                                room.putExtra("m_id", m_id.get(id));
                                room.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                Connecthelper.socket[0].off();
                                Connecthelper.socket[0].close();
                                startActivity(room);
//                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
            alertDialogBuilder.setCancelable(false);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    };

    public void ll_addview(){

        count = 1;

        runOnUiThread(new Runnable() {
            public void run() {
            if (ll.size() > 0) {
                //make_firstで作ったデータを表示する
                ll1.addView(ll.get(0));
                ll.remove(0);
            }
            }
        });
    }

    //broadcatstが来た際にリストの追加を行う
    public void add_layout(){
        runOnUiThread(new Runnable() {
            public void run() {
                ll1.addView(ll.get(0));
                ll.remove(0);
            }
        });
    }

    //試合データの受け取りを行う
    private Emitter.Listener extractMatchIndex = new Emitter.Listener() {

        @Override
        public void call(Object... args) {
        int flag = 0;

        try {
            if(first_flag) {
                first_flag = false;
                //データが入っていたらレイアウトに反映するために一時的にllにレイアウトをためる
                back_flag = false;
                make_first();
                if (!args[0].toString().equals("")) {
                    JSONArray receive1 = (JSONArray) args[0];
                    int i = 0;
                    while (receive1.length() > i) {
                        flag = 0;
                        //データのダブりを防ぐ
                        JSONObject jsonObject = receive1.getJSONObject(i);
                        for (int j = 0; j < m_id.size(); j++) {
                            if (m_id.get(j) == jsonObject.getInt("m_id")){
                                flag = 1;
                            }
                        }
                        if(flag == 0){
                            //arraylistに値を追加する
                            length.add(Integer.parseInt(jsonObject.getString("length").toString()));
                            m_id.add(Integer.parseInt(jsonObject.getString("m_id").toString()));
                            players.add(Integer.parseInt(jsonObject.getString("players").toString()));
                            arrows.add(Integer.parseInt(jsonObject.getString("arrows").toString()));
                            perEnd.add(Integer.parseInt(jsonObject.getString("perEnd").toString()));
                            matchName.add(jsonObject.getString("matchName").toString());
                            sponsor.add(jsonObject.getString("sponsor").toString());
                            created.add(jsonObject.getString("created").toString());
                            i++;
                            if (receive1.length() > i) {
                                make_room(false);
                            } else {
                                make_room(true);
                            }
                        }
                    }
                } else {
                    //試合が存在しないとダイアログで表示
//                displayDialogThroughHandlerThread("試合が存在しません");
                    if (m_ProgressDialog.isShowing()) {
                        m_ProgressDialog.dismiss();
                    }
                    back_flag = true;
                }
                Log.e("args[0]", args[0].toString());
                ll_addview();
            }
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        }
    };

    private Emitter.Listener broadcastInsertMatch = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
        try {
            Connecthelper.socket[0].once("broadcastInsertMatch", broadcastInsertMatch);
            //ブロードキャストで試合が送られて来たら反映するようにする
            JSONObject jsonObject = (JSONObject) args[0];
            length.add(Integer.parseInt(jsonObject.getString("length").toString()));
            m_id.add(Integer.parseInt(jsonObject.getString("m_id").toString()));
            players.add(Integer.parseInt(jsonObject.getString("players").toString()));
            arrows.add(Integer.parseInt(jsonObject.getString("arrows").toString()));
            perEnd.add(Integer.parseInt(jsonObject.getString("perEnd").toString()));
            matchName.add(jsonObject.getString("matchName").toString());
            sponsor.add(jsonObject.getString("sponsor").toString());
            created.add(jsonObject.getString("created").toString());
            make_room(false);
            add_layout();
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        }
    };

    //Toast表示用UI
    private void displayDialog(String str) {
        final long id = Thread.currentThread().getId();
        final String thName = Thread.currentThread().getName();

        if(toastcount == 0) {
            toastcount++;
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context,R.style.Theme_AppCompat_Light_Dialog_Alert));
            alertDialogBuilder.setTitle("試合が存在しません");
            alertDialogBuilder.setMessage("画面下の試合作成ボタンから試合を作成してください");
            alertDialogBuilder.setPositiveButton("OK!",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            alertDialogBuilder.setCancelable(false);
            AlertDialog alertDialog = alertDialogBuilder.create();
            if(!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
        Log.d("MainActivity", "Toast displayed. thread id:" + id + ", name:" + thName);
    }
    //別スレッドからToastを表示する際に使用
    private void displayDialogThroughHandlerThread(final String str) {
        final HandlerThread ht = new HandlerThread("TestThread#3");
        ht.start();

        Handler h = new Handler(ht.getLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                displayDialog(str);
//                ht.quit();
            }
        });
    }

    //バックキーを押した時の動作
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && back_flag) {
            Intent i = new Intent(this,Mypage.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[0].off();
            Connecthelper.socket[0].close();

            startActivity(i);
//            finish();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(Connecthelper.socket[0] != null) {
            //SocketIOを切る
            Connecthelper.socket[0].close();
        }

//        //各データの初期化行う
//        id_set = 0;
//
//        //arraylistの初期化
//        m_id = new ArrayList<Integer>();
//        length = new ArrayList<Integer>();
//        players = new ArrayList<Integer>();d
//        players = new ArrayList<Integer>();dcreate
//        arrows = new ArrayList<Integer>();
//        perEnd = new ArrayList<Integer>();
//        matchName = new ArrayList<String>();
//        sponsor = new ArrayList<String>();
//        created = new ArrayList<String>();
//        ll = new ArrayList<View>();
//
//        //flagの初期化
//        first_flag = true;
//        back_flag = true;
//        connect_flag = false;
    }
}