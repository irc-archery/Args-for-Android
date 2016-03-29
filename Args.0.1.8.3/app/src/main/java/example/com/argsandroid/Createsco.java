package example.com.argsandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

/**
 * Created by soft on 2015/06/08.
 */
public class Createsco extends ActionBarActivity implements View.OnClickListener {

    RadioButton rdb_me;
    RadioButton rdb_you;

    EditText email;
    EditText pass;

    Button btn;

    int dialogcount = 0;
    int beforecount = 0;

    TextView txt1;
    TextView txt2;

    Intent i;
    LinearLayout ll1;

    boolean emitflag = true;
    boolean connect_flag = false;
    boolean flag = false;

    Context context = this;

    SharedPreferences sp = null;
    SharedPreferences.Editor spe = null;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.createsco);
        //textviewの初期化
        txt1 = (TextView)findViewById(R.id.textView1);
        txt2 = (TextView)findViewById(R.id.textView2);

        //radiobuttonの初期化
        rdb_me = (RadioButton)findViewById(R.id.rdb1);
        rdb_you = (RadioButton)findViewById(R.id.rdb2);

        //edittextの初期化
        email = (EditText)findViewById(R.id.editText1);
        pass = (EditText)findViewById(R.id.editText2);

        //layoutの初期化
        ll1 = (LinearLayout)findViewById(R.id.another);

        //sharedprefarenceの初期化
        sp = getSharedPreferences("setting", MODE_MULTI_PROCESS);
        spe = sp.edit();

        //アクションバー関連
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle(R.string.createsco);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //ラジオボタンがクリックされた時に入るリスナー
        RadioGroup rg = (RadioGroup)findViewById(R.id.rg);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    //ラジオボタン1が押されたときにはレイアウトを隠し
                    case R.id.rdb1:
                        ll1.setVisibility(View.GONE);
                        Log.i("tag", "radioButton1");
                        break;
                    //ラジオボタン2が押されたときにはレイアウトを表示する
                    case R.id.rdb2:
                        ll1.setVisibility(View.VISIBLE);
                        Log.i("tag", "radioButton2");
                        break;

                }
            }
        });
        //buttonの関連付けとクリックイベントの付与
        btn = (Button)findViewById(R.id.button1);
        btn.setOnClickListener(this);

        //intentの初期化
        i = new Intent(this,Scoreboard.class);

        //sharedprefarenceの初期化
        sp = getSharedPreferences("setting", MODE_MULTI_PROCESS);
        spe = sp.edit();

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
            Connecthelper.socket[3].off();
            Connecthelper.socket[3].close();
            startActivity(intent);
        }
        try {
            connect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    //アクションバーのメニュー表示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_score, menu);
        return true;
    }

    //アクションバーのメニューのボタン処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //ひとつ前のActivityに遷移する
            Intent intent = new Intent(this,Scorelist.class);
            Intent intents  = getIntent();
            intent.putExtra("m_id", sp.getInt("m_id", 1));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[3].off();
            Connecthelper.socket[3].close();
            startActivity(intent);
//            finish();
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
            Connecthelper.socket[3].off();
            Connecthelper.socket[3].close();
            startActivity(warpanyway);
            return true;
        }
        //試合作成へ遷移
        if (id == R.id.menu_createmat) {
            warpanyway = new Intent(this,Createmat.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[3].off();
            Connecthelper.socket[3].close();
            startActivity(warpanyway);
        }
        //試合一覧へ遷移
        if (id == R.id.menu_matchlist) {
            warpanyway = new Intent(this,Matchlist.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[3].off();
            Connecthelper.socket[3].close();
            startActivity(warpanyway);
        }
        //マイページに遷移
        if (id == R.id.menu_mypage) {
            warpanyway = new Intent(this,Mypage.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[3].off();
            Connecthelper.socket[3].close();
            startActivity(warpanyway);
        }
        //団体ページに遷移
        if (id == R.id.menu_orgpage) {
            warpanyway = new Intent(this, Orgpage.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[3].off();
            Connecthelper.socket[3].close();
            startActivity(warpanyway);
        }
        //得点表作成へ遷移
        if (id == R.id.menu_scorelist) {
            warpanyway = new Intent(this,Scorelist.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Intent intents = getIntent();
            warpanyway.putExtra("m_id", sp.getInt("m_id", 1));
            Connecthelper.socket[3].off();
            Connecthelper.socket[3].close();
            startActivity(warpanyway);
        }
        return true;
    }

    //得点表の作成と画面遷移を行う
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        try {
            if (v.getId() == R.id.button1) {
                if(flag == false) {
                    flag = true;
//                btn.setEnabled(false);
                    JSONObject json = new JSONObject();
                    Intent intents = getIntent();
                    json.put("m_id", sp.getInt("m_id", 1));
                    //他者の得点表の場合
                    if (rdb_you.isChecked()) {
                        //全情報が入力されていれば得点表を追加する
                        if (!email.getText().toString().equals("") && !pass.getText().toString().equals("")) {
                            json.put("email", email.getText().toString());
                            json.put("password", pass.getText().toString());
                            json.put("sessionID", sp.getString("sessionID", ""));
                            Connecthelper.socket[3].emit("insertScoreCard", json);
                        } else {
                            Toast.makeText(this, "必要な情報をすべて入力してください", Toast.LENGTH_LONG).show();
                        }
                    }
                    //自分の得点表の場合はすでにログインしているのでセッションだけで徳tン票を追加する
                    else {
                        json.put("sessionID", sp.getString("sessionID", ""));
                        Connecthelper.socket[3].emit("insertOwnScoreCard", json);
                    }
                    flag = false;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            emitflag = true;
        }
    }

    //接続用関数
    private void connect() throws MalformedURLException {
        System.setProperty("java.net.preferIPv6Addresses", "false");
        try{
            Connecthelper.socket[3].open();
                //onイベントの設定
                Connecthelper.socket[3].once("insertScoreCard", insertScoreCard);

                //接続
                Connecthelper.socket[3].connect();
//            }
        } catch (Exception e) {
            Toast.makeText(this, "error" + e.toString(), Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }
    }
    //得点表の挿入イベント
    private Emitter.Listener insertScoreCard = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject jsonObject = (JSONObject) args[0];

                //挿入されたのがうまくいった場合得点表へ遷移する
                if(jsonObject.getInt("status") == 1) {
                    i.putExtra("sc_id", Integer.parseInt(jsonObject.getString("sc_id").toString()));
                    i.putExtra("ro", false);
                    Intent intents = getIntent();
                    i.putExtra("m_id", sp.getInt("m_id", 1));
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Connecthelper.socket[3].off();
                    Connecthelper.socket[3].close();
                    startActivity(i);
                }
                else{
                    //失敗した場合はエラー内容を表示する
                    displayDialogThroughHandlerThread(jsonObject.getString("err"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }catch(Exception e){
                e.printStackTrace();
            }

        }
    };
//    private Emitter.Listener insertOwnScoreCard = new Emitter.Listener() {
//        @Override
//        public void call(Object... args) {
//            try {
//                JSONObject jsonObject = (JSONObject) args[0];
//                i.putExtra("sc_id", Integer.parseInt(jsonObject.getString("sc_id").toString()));
//                i.putExtra("ro", false);
//                Intent intents = getIntent();
//                i.putExtra("m_id", intents.getIntExtra("m_id", 1));
//                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(i);
////                finish();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }catch(Exception e){
//            }
//        }
//    };

//    private Emitter.Listener checkPermission = new Emitter.Listener() {
//        @Override
//        public void call(Object... args) {
//            try {
//                JSONObject jsonObject = (JSONObject) args[0];
//
//                if (perflag && save_id != 0) {
//                    perflag = false;
//                    i.putExtra("sc_id", save_id);
//                    Log.e("save_idの sc_id", save_id + "");
//
//                    if (jsonObject.getString("permission").toString().equals("true")) {
//                        i.putExtra("ro", false);
//                    } else {
//                        i.putExtra("ro", true);
//                    }
//                    Intent intents = getIntent();
//                    i.putExtra("m_id", intents.getIntExtra("m_id", 1));
//                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(i);
//                    finish();
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        }
//    };

    private void displayDialog(String str) {
        final long id = Thread.currentThread().getId();
        final String thName = Thread.currentThread().getName();

        //得点表作成がうまくいかなかった際にダイアログを表示する
        //複数表示しないように変数を使う
        if(dialogcount == beforecount) {
            dialogcount++;
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context,R.style.Theme_AppCompat_Light_Dialog_Alert));
            alertDialogBuilder.setTitle("得点表を作れませんでした");
            alertDialogBuilder.setMessage(str);
            alertDialogBuilder.setPositiveButton("OK!",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            beforecount = dialogcount;
                        }
                    });
            alertDialogBuilder.setCancelable(false);
            AlertDialog alertDialog = alertDialogBuilder.create();
            if (!alertDialog.isShowing() ) {
                alertDialog.show();
            }
        }
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //一つ前の画面に遷移する
            Intent intent = new Intent(this,Scorelist.class);
            Intent intents  = getIntent();
            intent.putExtra("m_id", sp.getInt("m_id", 1));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[3].off();
            Connecthelper.socket[3].close();
            startActivity(intent);
//            finish();
        }
        return false;
    }
}
