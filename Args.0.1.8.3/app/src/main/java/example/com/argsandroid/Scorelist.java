package example.com.argsandroid;

import android.app.AlertDialog;
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
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
import java.util.concurrent.ExecutionException;

/**
 * Created by soft on 2015/04/07.
 */

public class Scorelist extends ActionBarActivity implements OnClickListener {

    ArrayList<TextView> sctxt;
    ArrayList<TextView> scoretxt;
    ArrayList<TextView> perendtxt;
    ArrayList<Integer> shotScore;
    ArrayList<Integer> setNum;
    ArrayList<String> matchName;
    ArrayList<Integer> length;
    ArrayList<String> perEnd;
    ArrayList<LinearLayout> ll;
    ArrayList<LinearLayout> ll2;

//    int m_id = 0;
    int save_id;
    int count = -1;

    ArrayList<String> playerName;
    ArrayList<Integer> total = null;
    ArrayList<Integer> sc_id;

    int id_set = 0;
    boolean first_flag = true;
    boolean back_flag = true;
    boolean connect_flag = false;

    Point size;
    LinearLayout ll1;
    LinearLayout ll_h;
    TextView make_t;
    Button btn1,rankingbtn;
    Intent get_i;
    Intent test_i;
    Intent test2_i;
    Intent i;

    boolean checkflag = false;
    boolean maker = false;

    SharedPreferences sp = null;
    SharedPreferences.Editor spe = null;

    public ProgressDialog m_ProgressDialog;

    Timer mTimer = null;
    Handler mHandler = new Handler();

    Context context = this;

    int toastcount = 0;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.scorelist);

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        // ディスプレイのインスタンス生成
        Display disp = wm.getDefaultDisplay();
        size = new Point();
        //画面サイズ取得
        disp.getSize(size);

        //buttonの初期化とクリックイベント付与
        btn1 = (Button) findViewById(R.id.button1);
        btn1.setOnClickListener(this);

        rankingbtn = (Button) findViewById(R.id.rankbutton);
        rankingbtn.setOnClickListener(this);

        //ArrayListの初期化
        sctxt = new ArrayList<TextView>();
        scoretxt = new ArrayList<TextView>();
        perendtxt = new ArrayList<TextView>();
        shotScore = new ArrayList<Integer>();
        setNum = new ArrayList<Integer>();
        matchName = new ArrayList<String>();
        length = new ArrayList<Integer>();
        perEnd = new ArrayList<String>();
        ll = new ArrayList<LinearLayout>();
        ll2 = new ArrayList<LinearLayout>();
        playerName = new ArrayList<String>();
        total = new ArrayList<Integer>();
        sc_id = new ArrayList<Integer>();

        //intentの初期化
        test_i = new Intent(this, Scoreboard.class);
        test2_i = new Intent(this, ROScoreboard.class);
        i = new Intent(this, Matchlist.class);

        //linearlayoutの初期化
        ll1 = (LinearLayout) findViewById(R.id.ll1);

        //SaredPrefrenceの初期化
        sp = getSharedPreferences("setting", MODE_MULTI_PROCESS);
        spe = sp.edit();

        get_i = getIntent();
        //m_idの取得
        spe.putInt("m_id", get_i.getIntExtra("m_id", 1));
        spe.commit();

        //アクションバー関連
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.scorelist);
        actionBar.setDisplayHomeAsUpEnabled(true);

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
            Connecthelper.socket[2].off();
            Connecthelper.socket[2].close();
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
                        if (m_ProgressDialog.isShowing()) {
                            if (back_flag) {
//                                if (m_ProgressDialog.isShowing()) {
//                                    Intent intents  = getIntent();
//                                    test.putExtra("m_id",intents.getIntExtra("m_id",1));
//                                    test.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                    ctivity(test);
//                                }
                                if (Connecthelper.socket[2] != null) {
                                    Connecthelper.socket[2].off();
                                    Connecthelper.socket[2].close();
                                }

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

        //レイアウト追加の際のコールバック(ll1)
        //コールバックで追加するので効率がよくメモリ落ちしにくい
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
                    } else if (count == 1) {
                        //レイアウトが一つでも追加されてたらダイアログを消す
                        back_flag = true;
                        if (m_ProgressDialog.isShowing()) {
                            m_ProgressDialog.dismiss();
                        }
                    }
                } catch (Exception e) {

                }
            }
        });

    }

    //アクションバーのメニュー表示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_scorelist, menu);
        return true;
    }

    //アクションバーのメニューのボタン処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //ひとつ前のActivityに遷移する
            Intent intent = new Intent(this, Matchlist.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[2].off();
            Connecthelper.socket[2].close();
            startActivity(intent);
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
            Connecthelper.socket[2].off();
            Connecthelper.socket[2].close();
            startActivity(warpanyway);
            return true;
        }
        //試合作成へ遷移
        if (id == R.id.menu_createmat) {
            warpanyway = new Intent(this,Createmat.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[2].off();
            Connecthelper.socket[2].close();
            startActivity(warpanyway);
        }
        //試合一覧へ遷移
        if (id == R.id.menu_matchlist) {
            warpanyway = new Intent(this,Matchlist.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[2].off();
            Connecthelper.socket[2].close();
            startActivity(warpanyway);
        }
        //マイページに遷移
        if (id == R.id.menu_mypage) {
            warpanyway = new Intent(this,Mypage.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[2].off();
            Connecthelper.socket[2].close();
            startActivity(warpanyway);
        }
        //団体ページに遷移
        if (id == R.id.menu_orgpage) {
            warpanyway = new Intent(this, Orgpage.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[2].off();
            Connecthelper.socket[2].close();
            startActivity(warpanyway);
        }
        //得点表作成へ遷移
        if (id == R.id.menu_createsco) {
            warpanyway = new Intent(this,Createsco.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            warpanyway.putExtra("m_id", sp.getInt("m_id", 0));
            Connecthelper.socket[2].off();
            Connecthelper.socket[2].close();
            startActivity(warpanyway);
        }

        //試合終了する
        if (item.getItemId() == R.id.menu_finmatch) {
            //試合制作者ではなかったら削除させないようにする
            if(!maker){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("試合を削除できません");
                alertDialogBuilder.setMessage("試合制作者ではないので削除することはできません");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("OK!",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                alertDialogBuilder.setCancelable(false);
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
            //試合制作者であったらダイアログで確認した後に削除用イベントをemitする
            else {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("試合を終了します");
                alertDialogBuilder.setMessage("よろしいですか？");
                alertDialogBuilder.setCancelable(false);
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
                                    json.put("m_id", sp.getInt("m_id",0));
                                    json.put("sessionID", sp.getString("sessionID", ""));
                                    Connecthelper.socket[2].emit("closeMatch", json);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                alertDialogBuilder.setCancelable(false);
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }
        return true;
    }

    //得点表の作成＋画面遷移
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v.getId() == R.id.button1) {
            //ボタンが押されたら得点表作成に遷移する
            Intent sintent = new Intent(this, Createsco.class);
            sintent.putExtra("m_id", sp.getInt("m_id", 0));
            sintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[2].off();
            Connecthelper.socket[2].close();
            startActivity(sintent);
//            finish();
        }
        else if(v.getId() == R.id.rankbutton)
        {
            try {
                //得点表一覧へ遷移
                Intent room = new Intent(this, Rankinglist.class);
                room.putExtra("m_id", sp.getInt("m_id",0));
                room.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                Connecthelper.socket[2].off();
                Connecthelper.socket[2].close();
                startActivity(room);
//                                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //試合名、日時などを表示するとともにリストの項目作成する関数
    public boolean make_first() {
        //レイアウトを追加するための枠の作成
        ll_h = new LinearLayout(this);
        ll_h.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams((int) (ll1.getWidth()), (int) size.y / 20);
        ll_h.setLayoutParams(params1);

        //選手名を並べるためのタイトルを作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("選手名");
        params1 = new LinearLayout.LayoutParams((int) (ll1.getWidth()) / 2, (int) size.y / 20);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        //セットを並べるためのタイトルを作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("セット");
        params1 = new LinearLayout.LayoutParams((int) (ll1.getWidth()) / 6, (int) size.y / 20);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        //得点を並べるためのタイトルを作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("得点");
        params1 = new LinearLayout.LayoutParams((int) (ll1.getWidth()) / 6, (int) size.y / 20);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        //詳細を並べるためのタイトルを作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("詳細");
        params1 = new LinearLayout.LayoutParams((int) (ll1.getWidth()) / 6, (int) size.y / 20);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        //llに作ったレイアウトを追加する
        ll.add(ll_h);

        return true;
    }

    public void make_scorelist(boolean flag) {

        String str;

        //枠の作成
        ll_h = new LinearLayout(this);
        ll_h.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams((int) (ll1.getWidth()), (int)size.y / 10);
        ll_h.setLayoutParams(params1);
        ll_h.setId(id_set);
        ll_h.setOnClickListener(in);

        //選手名を表示する
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);

        //選手名が長い場合は8文字目を「...」とする
        if (playerName.get(id_set).length() > 8) {
            str = playerName.get(id_set).substring(0, 7) + "...";
        } else {
            str = playerName.get(id_set);
        }

        make_t.setText(str);
        params1 = new LinearLayout.LayoutParams((int) (ll1.getWidth()) / 2, (int)size.y / 10);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
//        make_t.setTypeface(Typeface.createFromAsset(getAssets(), "JiyunoTsubasa.ttf"));
        ll_h.addView(make_t);

        //セットを表示する
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
//        make_t.setText(scoreTotal.get(id_set)+"");
        make_t.setText(perEnd.get(id_set));
        params1 = new LinearLayout.LayoutParams((int) (ll1.getWidth()) / 6, (int)size.y / 10);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
//        make_t.setTypeface(Typeface.createFromAsset(getAssets(), "JiyunoTsubasa.ttf"));
        ll_h.addView(make_t);
        perendtxt.add(make_t);

        //得点を表示する
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText(total.get(id_set) + "");

        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
//        make_t.setTypeface(Typeface.createFromAsset(getAssets(), "JiyunoTsubasa.ttf"));
        ll_h.addView(make_t);
        scoretxt.add(make_t);

        //矢印を表示する
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("⇒");
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
//        make_t.setTypeface(Typeface.createFromAsset(getAssets(), "JiyunoTsubasa.ttf"));
        ll_h.addView(make_t);
        sctxt.add(make_t);

        id_set++;

        ll.add(ll_h);
        if (flag) {
            //ll1に作ったレイアウトを追加し表示する
            runOnUiThread(new Runnable() {
                public void run() {
                    if(ll.size() != 0) {
                        ll1.addView(ll.get(0));
                        ll.remove(0);
                    }
                }
            });
        }
    }

    public void ll_addview() {

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

    public OnClickListener in = new OnClickListener() {
        @Override
        public void onClick(View view) {
            JSONObject json = new JSONObject();
            try {
                //サーバーに得点表を作れるのか確認するためのイベントをemitする
                save_id = view.getId();
                json.put("sc_id", sc_id.get(save_id));
                json.put("sessionID", sp.getString("sessionID", ""));
                Connecthelper.socket[2].emit("checkPermission", json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //接続用関数
    private void connect() throws MalformedURLException {
        System.setProperty("java.net.preferIPv6Addresses", "false");
        try {
            if(Connecthelper.socket[2] != null) {
                Connecthelper.socket[2].open();
            }

            //onイベントの登録
            if(!Connecthelper.socket[2].hasListeners("extractScoreCardIndex")) {
                Connecthelper.socket[2].once("extractScoreCardIndex", extractScoreCardIndex);
            }
            if(!Connecthelper.socket[2].hasListeners("broadcastInsertScoreCard")) {
                Connecthelper.socket[2].once("broadcastInsertScoreCard", broadcastInsertScoreCard);
            }
            if(!Connecthelper.socket[2].hasListeners("broadcastInsertScore")) {
                Connecthelper.socket[2].once("broadcastInsertScore", broadcastInsertScore);
            }
            if (!Connecthelper.socket[2].hasListeners("broadcastUpdateScore")) {
                Connecthelper.socket[2].once("broadcastUpdateScore", broadcastUpdateScore);
            }
            if(!Connecthelper.socket[2].hasListeners("checkPermission")) {
                Connecthelper.socket[2].once("checkPermission", checkPermission);
            }
            if(!Connecthelper.socket[2].hasListeners("checkMatchCreater")) {
                Connecthelper.socket[2].once("checkMatchCreater", checkMatchCreater);
            }
            if(!Connecthelper.socket[2].hasListeners("broadcastCloseMatch")) {
                Connecthelper.socket[2].once("broadcastCloseMatch", broadcastCloseMatch);
            }

            Connecthelper.socket[2].connect();
            //最初の1回目だけif文に入るようにする
            if (first_flag) {
                Intent get_i = getIntent();
                JSONObject json = new JSONObject();
                json.put("m_id", sp.getInt("m_id",0));
                json.put("sessionID", sp.getString("sessionID", ""));
                //得点表のデータを送ってもらうようemitする
                Connecthelper.socket[2].emit("joinMatch", json);

                //この試合の作成者か確認するためにemitする
                Connecthelper.socket[2].emit("checkMatchCreater", json);
            }
        } catch (Exception e) {
            Toast.makeText(this, "error" + e.toString(), Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }
    }

    private Emitter.Listener broadcastInsertScoreCard = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Connecthelper.socket[2].once("broadcastInsertScoreCard", broadcastInsertScoreCard);
            final JSONObject jsonObject = (JSONObject) args[0];
            try {
                //得点表が追加されたのでそのデータを反映する
                sc_id.add(Integer.parseInt(jsonObject.getString("sc_id").toString()));
                playerName.add(jsonObject.getString("playerName").toString());
                total.add(Integer.parseInt(jsonObject.getString("total").toString()));
                perEnd.add(jsonObject.getString("perEnd").toString());
                make_scorelist(true);
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    };
    private Emitter.Listener broadcastInsertScore = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //argsがなぜかSocketIO時はJSONObjectだったのがJSONArrayになっていたので注意
            final JSONObject jsonObject = (JSONObject) args[0];
            Connecthelper.socket[2].once("broadcastInsertScore", broadcastInsertScore);
            try {
                //得点が更新されたので、レイアウトのほうも変更する
                for (int i = 0; i < sc_id.size(); i++) {
                    if (Integer.parseInt(jsonObject.getString("sc_id").toString()) == sc_id.get(i)) {
                        final int finalI = i;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    perendtxt.get(finalI).setText(jsonObject.getString("perEnd").toString());
                                    scoretxt.get(finalI).setText(jsonObject.getString("total").toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
//            ll_addview();
        }
    };
    private Emitter.Listener broadcastUpdateScore = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Connecthelper.socket[2].once("broadcastUpdateScore", broadcastUpdateScore);
            final JSONObject jsonObject = (JSONObject) args[0];
            boolean flag = false;
            try {
                //得点表の得点がupdateされたのでその点を反映する
                for (int i = 0; i < sc_id.size(); i++) {
                    if (Integer.parseInt(jsonObject.getString("sc_id").toString()) == sc_id.get(i)) {
                        final int finalI = i;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
//                                    perendtxt.get(finalI).setText(jsonObject.getString("perEnd").toString());
                                    scoretxt.get(finalI).setText(jsonObject.getString("total").toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
//            ll_addview();
        }
    };
    //試合が終了されたのを受け取るイベント
    private Emitter.Listener broadcastCloseMatch = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Connecthelper.socket[2].once("broadcastCloseMatch", broadcastCloseMatch);
            //試合一覧に遷移する
            i.putExtra("m_id", sp.getInt("m_id",0));
            i.putExtra("close", true);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[2].off();
            Connecthelper.socket[2].close();
            startActivity(i);
        }
    };
    private Emitter.Listener extractScoreCardIndex = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            int i = 0;

            try {

                ll = new ArrayList<LinearLayout>();

                make_first();
                //データが入っていたら
                if (!args[0].toString().equals("")) {
                    back_flag = false;
                    first_flag = false;

                    JSONArray receive1 = (JSONArray) args[0];
                    //JSONArray eventArray = receive1.getJSONArray("id");
                    if (receive1.length() != 0) {
                        while (receive1.length() != i) {

                            //送られてきた得点表の値をTextViewに表わす
                            JSONObject jsonObject = receive1.getJSONObject(i);
                            sc_id.add(jsonObject.getInt("sc_id"));
                            playerName.add(jsonObject.getString("playerName").toString());
                            total.add(jsonObject.getInt("total"));
                            perEnd.add(jsonObject.getString("perEnd").toString());
                            make_scorelist(false);
                            i++;
                        }
                    }
                } else {
                    //得点表データが一つもなかった場合はダイアログを表示する
//                    displayDialogThroughHandlerThread("得点表が存在しません");
                    if (m_ProgressDialog.isShowing()) {
                        m_ProgressDialog.dismiss();
                    }
                    back_flag = true;
                }
                ll_addview();
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    };

    //タップした得点表が読み取り専用か書き込みができるかを返すイベント
    private Emitter.Listener checkPermission = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject jsonObject = (JSONObject) args[0];
                if (!checkflag && sc_id.size() != 0) {
                    checkflag = true;
                    test_i.putExtra("sc_id", sc_id.get(save_id));
                    test_i.putExtra("m_id", sp.getInt("m_id", 0));
//                    Log.e(get_i.getIntExtra("m_id", 1) + "", m_id+"");
                    if (jsonObject.getString("permission").toString().equals("true")) {
                        test_i.putExtra("ro", false);
                    } else {
                        test_i.putExtra("ro", true);
                    }
                    test_i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Connecthelper.socket[2].off();
                    Connecthelper.socket[2].close();
                    startActivity(test_i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //試合の作成者かどうか返すイベント
    private Emitter.Listener checkMatchCreater = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject jsonObject = (JSONObject) args[0];

                maker = jsonObject.getBoolean("permission");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //ダイアログ表示用UI
    private void displayDialog(String str) {
        final long id = Thread.currentThread().getId();
        final String thName = Thread.currentThread().getName();

        //１回以上表示しないようにする
        if(toastcount == 0) {
            toastcount = 1;
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle("得点表が存在しません");
            alertDialogBuilder.setMessage("画面下の得点表作成ボタンから得点表を作成してください");
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
    }
    //別スレッドからダイアログを表示する際に使用する関数
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            i.putExtra("m_id", sp.getInt("m_id", 0));
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[2].off();
            Connecthelper.socket[2].close();
            startActivity(i);
        }
        return false;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        //SocketIOを切る
        if(Connecthelper.socket[2] != null) {
            Connecthelper.socket[2].off();
            Connecthelper.socket[2].close();
        }
        //各変数を初期化する
//        id_set = 0;
//
//        sctxt = new ArrayList<TextView>();
//        scoretxt = new ArrayList<TextView>();
//        perendtxt = new ArrayList<TextView>();
//        shotScore = new ArrayList<Integer>();
//        setNum = new ArrayList<Integer>();
//        matchName = new ArrayList<String>();
//        length = new ArrayList<Integer>();
//        perEnd = new ArrayList<String>();
//        ll = new ArrayList<LinearLayout>();
//
//        playerName = new ArrayList<String>();
//        total = new ArrayList<Integer>();
//        sc_id = new ArrayList<Integer>();
    }
}