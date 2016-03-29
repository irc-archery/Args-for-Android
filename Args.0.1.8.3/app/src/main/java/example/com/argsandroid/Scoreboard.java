package example.com.argsandroid;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

public class Scoreboard extends ActionBarActivity implements OnClickListener ,View.OnFocusChangeListener {

    Button sumbt[] = new Button[9];//合計のボタン配列
    Button keybt[] = new Button[12];//入力のボタン配列
    EditText base[] = new EditText[6];//各種項目の入力
    Button numbt[] = new Button[6];//ゼッケンのボタンの配列
    Button def;//確定ボタン

    SharedPreferences sp;
    SharedPreferences.Editor spe;

    //点数保持・計算
    int sum = 0;
    ArrayList<String> point = new ArrayList<String>();
    int couset = 0;
    int total = 0;//destroyしたら最初のレイアウト作成のところのsumをこちらに代入
    int sums[] = new int[9];
    int tencou = 0;//10数のカウント
    int xcou = 0;//x数のカウント
    int subtotal=0;
    int maxperend = 6;
    boolean first_flag = true;
    boolean ro;
    boolean broadro = false;
    boolean back_flag = true;
    public ProgressDialog m_ProgressDialog;
    String number = "";
    String prefecture = "";

    int id = 0, sid = 0;//id:試合各回のグループビューのid,sid:グループビューの各ボタンのid
    int acId = 0;//タップされたボタンのidを保持するための変数
    int loId = 0;//ロングタップされたボタンのidを保持するための変数
    int cou = 1;//入力カウント
    int bId = 0;//キーボードの値保持
    int coo = 0;
    int sumbuttonid = 0;
    int sumscore[] = new int[6];
    boolean newscoreboard = true;
    boolean key = false;//キーボード表示判断
    boolean change = false;//値変更
    boolean next = false;//スコアボード増やしていいかのフラグ
    boolean connect_flag = false;
    String setpoint[][] = new String[2][8];
    String[] length_str = {"90m","70m","60m","50m","40m","30m","70m前","70m後","18m"};
    int[] sumId = {R.id.sumbutton1, R.id.sumbutton2, R.id.sumbutton3, R.id.sumbutton4, R.id.sumbutton5, R.id.sumbutton6, R.id.sumbutton7, R.id.sumbutton8, R.id.sumbutton9};
    int[] resId = {R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9, R.id.button10, R.id.button11, R.id.button12, R.id.button13, R.id.button14, R.id.button15, R.id.button16};
    int[] keyId = {R.id.keym, R.id.key1, R.id.key2, R.id.key3, R.id.key4, R.id.key5, R.id.key6, R.id.key7, R.id.key8, R.id.key9, R.id.key10, R.id.keyx};
    int[] ediId = {R.id.editText1, R.id.editText2, R.id.editText3, R.id.editText4, R.id.editText5, R.id.editText6};
    String[] pos = {"M", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "X"};
    LinearLayout box, keyboardSpace;//box:得点のグループビューを入れるレイアウトの変数,inpuSpace:得点入力を入れるレイアウトの変数
    TextView set;//各回目を表示するため
    ScrollView scrollview;//スクロールビュー

    //動的グループビュー作成のための変数.スコア
    LayoutInflater inflater;
    View itemView;
    //動的グループビュー作成のための変数.キーボード
    LayoutInflater keyinflater;
    View keyitem;

    EditText p_name;
    String length = null;
    ArrayList<Integer> arraycou;
    ArrayList<Button> scorebutton;
    int scoreboardcount=0;
    Timer mTimer = null;
    Handler mHandler = new Handler();

    //合計とか10数とかX数とか
    Button sum6,sum10,sumx;

    TextView lengtht;

    Button exb;

    //extra_layoutで使ってる変数
    Button su;

    //Emitter.Listener broadcastInsertScoreで使ってる変数
    int a = 0;

    Intent i2;

    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //readopnlyかどうかを受けとる
        Intent intent = getIntent();
        ro = intent.getBooleanExtra("ro", true);
        if(!ro) {
            setContentView(R.layout.scoreboard);
        }
        else{
            setContentView(R.layout.roscoreboard);
        }


        //関連付け
        for (int i = 0; i < 9; i++) {
            sumbt[i] = (Button) findViewById(sumId[i]);
        }
        for (int i = 0; i < 6; i++) {
            base[i] = (EditText) findViewById(ediId[i]);
            if(ro){
                if(i == 2 || i == 5) {
                    base[i].setFocusable(false);
                    base[i].setFocusableInTouchMode(false);
                }
            }
        }
        base[2].setOnFocusChangeListener(this);
        base[5].setOnFocusChangeListener(this);
        p_name = (EditText)findViewById(R.id.editText4);

        //sharedprefarenceの設定
        sp = getSharedPreferences("setting", Context.MODE_MULTI_PROCESS);
        spe = sp.edit();

        //layoutの初期化
        box = (LinearLayout) findViewById(R.id.Box);
        keyboardSpace = (LinearLayout) findViewById(R.id.keyboardSpace);
        scrollview = ((ScrollView) findViewById(R.id.scrollView1));
        //書き込み可能の場合確定ボタンを関連づけ
        if(!ro) {
            def = (Button) findViewById(R.id.button1);//確定ボタンの関連付け
            def.setOnClickListener(this);//確定ボタンのOnClick付加
            def.setClickable(false);//クリックできない状態
        }

        //button関連の初期化
        arraycou = new ArrayList<Integer>();
        scorebutton = new ArrayList<Button>();
        sum6 = (Button) findViewById(sumId[6]);
        sum10 = (Button) findViewById(sumId[7]);
        sumx = (Button) findViewById(sumId[8]);

        //インテントの設定
        i2 = new Intent(this,Matchlist.class);


        //アクションバー関連
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.scoreboard);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //ネットワークに接続されているか確認する
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        //通信できないときにfalseを返す
        if (info == null || !info.isConnected()) {
            Toast.makeText(this, "ネットワークに接続されていません\nログイン画面に遷移します", Toast.LENGTH_LONG).show();

            spe.putString("sessionID", "");
            spe.commit();

            Intent in = new Intent(this, Login.class);
            in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[4].off();
            Connecthelper.socket[4].close();
            startActivity(in);

        }
        //接続を行う
        try {
            connect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //読み込み用のダイアログを表示する
        m_ProgressDialog = new ProgressDialog(this);

        m_ProgressDialog.setMessage("少々お待ちください");

        m_ProgressDialog.setCancelable(false);

        m_ProgressDialog.show();

        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    public void run() {
                        if (back_flag) {
//                                if (m_ProgressDialog.isShowing()) {
//                                    Intent intents  = getIntent();
//                                    test.putExtra("m_id",intents.getIntExtra("m_id",1));
//                                    test.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                    startActivity(test);
//                                }
                            //うまくデータを読み込めなかった場合リロードする
                            if (m_ProgressDialog.isShowing()) {
                                if(Connecthelper.socket[4] != null) {
                                    //Connecthelper.socket[4].disconnect();
                                    Connecthelper.socket[4].close();
                                    Connecthelper.socket[4].off();
                                }
//                                    socket = null;
                                first_flag = true;
                                try {
                                    //サーバーとの接続をしなおす
                                    connect();
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            if (m_ProgressDialog.isShowing()) {
                                m_ProgressDialog.dismiss();
                            }
                        }
                        mTimer.cancel();
                    }
                });
            }
        }, 2000, 2000);

//        ViewTreeObserver vto = box.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                if (ro && broadro) {
//                    runOnUiThread(new Runnable() {
//                        //スレットでやらないとうまくいかないっぽい
//                        public void run() {
//                            Log.e("aaaaa","aaaaaa");
//                            extra_layout(tencou, xcou, sum,
//                                    "", "", true);
//                            couset = arraycou.get(0);
//                            arraycou.remove(0);
//                            for (int i = (couset - 1) * 16; i < couset * 16; i++) {
//                                //buttonの関連付け
//                                Button shoset = (Button) itemView.findViewById(i);
//                                shoset.setText(point.get(i) + "");
//                                if (i % 16 == 7)//合計のとき
//                                {
//                                    shoset = (Button) findViewById(sumId[couset-1]);
//                                    shoset.setText(point.get(i) + "");
//                                }
//                            }
//                        }
//                    });
//                }
//        });

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
            if(item.getItemId() == android.R.id.home) {
                Intent i = new Intent(this,Scorelist.class);
                Intent intents  = getIntent();
                i.putExtra("m_id", sp.getInt("m_id", 1));
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                Connecthelper.socket[4].off();
                Connecthelper.socket[4].close();
                startActivity(i);
                return true;
            }
        }

        int id = item.getItemId();
        Intent warpanyway = null;

        Connecthelper.socket[4].off();
        Connecthelper.socket[4].close();

        //ログアウトし、sessionIDを消去してログインページへ遷移する
        if (id == R.id.menu_logout) {

            spe.putString("sessionID", "");
            spe.commit();

            warpanyway = new Intent(this,Login.class);
        }
        //試合作成へ遷移
        if (id == R.id.menu_createmat) {
            warpanyway = new Intent(this,Createmat.class);
        }
        //試合一覧へ遷移
        if (id == R.id.menu_matchlist) {
            warpanyway = new Intent(this,Matchlist.class);
        }
        //マイページに遷移
        if (id == R.id.menu_mypage) {
            warpanyway = new Intent(this,Mypage.class);
        }
        //団体ページに遷移
        if (id == R.id.menu_orgpage) {
            warpanyway = new Intent(this, Orgpage.class);
        }
        //得点表作成へ遷移
        if (id == R.id.menu_scorelist) {
            warpanyway = new Intent(this,Scorelist.class);

        }
        //得点表作成へ遷移
        if (id == R.id.menu_createsco) {
            warpanyway = new Intent(this,Createsco.class);
        }
        warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Intent get_i = getIntent();
        warpanyway.putExtra("m_id", sp.getInt("m_id", 1));
        startActivity(warpanyway);
        return true;
    }

    //フォーカスが変わった際にゼッケンか都道府県が変わっていたら更新する
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(v.getId() == base[2].getId() && !hasFocus) {
            sendinsertnumber();
        }
        if(v.getId() == base[5].getId() && !hasFocus) {
            sendinsertprefectures();
        }
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        for (int a = 0; a < sid; a++)//スコアボードのボタン0から存在する数ループ
        {
            if (v.getId() == a) {
                //Toast.makeText(this,"a:" + a + "sid:" + sid,Toast.LENGTH_LONG).show();
                if(!key) {
                    //ゼッケンと都道府県が変わっていたら更新する
                    sendinsertnumber();
                    sendinsertprefectures();
                    if (a >= sid - 16) {
                        if(newscoreboard) {
                            newscoreboard = false;
                            cou = 1;
                            sum = 0;
                        }
                        acId =  ((a / 16) * 16) + cou;//タップされたid保持

                        Button in = (Button) scorebutton.get(acId);
                        in.setBackgroundDrawable(getResources().getDrawable(R.drawable.frame_s));

                        keyboardview();//キーボード表示
                    }
                }
            }

        }

        for (int b = 0; b < 12; b++)//キーボーﾄﾞからの入力
        {
            if (v.getId() == keyId[b]) {
                if (change == true) {
                    bId = b;//キーボードのボタン保持
                    Changef();//値を交換する関数

                    //キーボード処理
                    keyboardSpace.removeView(keyitem);
//                    def.set(true);
                    def.setVisibility(View.VISIBLE);
                    key = false;

                    change = false;//編集モード無効
                } else {
                    Button in = scorebutton.get(acId);
                    Button out = (Button) keyitem.findViewById(keyId[b]);
                    setpoint[0][cou] = out.getText().toString();
                    sumscore[cou - 1] = conversion(out.getText().toString());
                    sum += conversion(out.getText().toString());
                    in.setText(out.getText());
                    in.setBackgroundDrawable(getResources().getDrawable(R.drawable.frame));

                    //最初の1s入力時にはいる
                    //1つ値を打つたびに合計点を変動させる
                    if(cou == 1){
                        sumbuttonid = acId + 6;
                    }
                    Button bsum = scorebutton.get(sumbuttonid);
                    sum = 0;
                    for(int i = 0; i < 6;i++){
                        sum += sumscore[i];
                    }
                    bsum.setText(sum + "");
                    setpoint[0][7] = sum + "";
                    if(cou != 6) {
                        in = scorebutton.get(acId + 1);
                        in.setBackgroundDrawable(getResources().getDrawable(R.drawable.frame_s));
                    }
                    if (cou == 6) {
                        //キーボード処理
                        keyboardSpace.removeView(keyitem);
                        def.setVisibility(View.VISIBLE);
                        key = false;
                        //確定ボタンの処理
                        if(!ro) {
                            def.setOnClickListener(this);
                        }
                        next = true;
                        //初期化
                        sum = 0;
                        cou = 0;
                    }
                    cou++;
                    acId++;
                }
            }
        }
        if(!ro) {
            if (v.getId() == R.id.button1)//確定ボタンが押されたら
            {
                if (next == true)//couが0になってたら
                {
                    newscoreboard = true;
                    sumscore = new int[6];

                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 8; j++) {
                            if (setpoint[i][j] != null) {
                                if (setpoint[i][j].equals("X")) {
                                    xcou++;
                                }
                                if (setpoint[i][j].equals("10")) {
                                    tencou++;
                                }
                            }
                            //ArrayListに挿入
                            point.add(setpoint[i][j]);
                        }
                    }

                    try {
                        send_insertscoreboard();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //画面上部のボタンに合計表示
                    if (id < 7) {
                        sums[id - 1] = Integer.parseInt(setpoint[0][7]);
                        sums[6] += sums[id - 1];
                        Button bsum = (Button) findViewById(sumId[id - 1]);
                        bsum.setText(setpoint[0][7]);

                        sum6.setText(sums[6] + "");

                    }

                    sum10.setText(tencou + "");
                    sumx.setText(xcou + "");

                    next = false;
                    def.setSelected(false);//選択できない状態にする

                    Clickoff();//得点表をクリックできないようにする

                    scoreboard();//スコアボードの表示

                }
            }
        }
    }

    //バックキーを押した時の動作
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            //キーボードが表示されていたら非表示にする
            if (key == true) {
                keyboardSpace.removeView(keyitem);
//                def.setEnabled(true);
                Button in = scorebutton.get(acId);
                in.setBackgroundDrawable(getResources().getDrawable(R.drawable.frame));
                def.setVisibility(View.VISIBLE);
                key = false;
            } else {
                //EditTextが変更されていた際にそのデータを送信する
                judgebases();

                //得点表一覧に遷移する
                Intent i = new Intent(this,Scorelist.class);
                Intent intents  = getIntent();
                i.putExtra("m_id", sp.getInt("m_id", 1));
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //Connecthelper.socket[4].off();

//                Connecthelper.socket[4].disconnect();
                Connecthelper.socket[4].close();
                Connecthelper.socket[4].off();
                startActivity(i);
//                socket = null;
//                finish();
            }
        }
        return false;
    }

    public void Clickoff ()
    {
        /*OnClick解除*/
        int off = sid - 15;

        for (int ii = off; ii < sid; ii++) {
            Button moff = scorebutton.get(ii);
            moff.setClickable(false);//クリックできない状態
            moff.setOnClickListener(null);
            if (ii % 16 != 0 && ii % 16 != 8 && ii % 16 != 7 && ii % 16 != 15 && !ro)//ゼッケンと合計以外にクリックイベント付加
            {
                moff.setOnClickListener(up);
            }
        }
    }

    public OnClickListener up = new OnClickListener() {
        @Override
        public void onClick(View view) {
            for (int c = 0; c < sid; c++)//スコアボードのボタン0から存在する数ループ
            {
                if (view.getId() == c) {
                    loId = c;//ロングタップされたボタンのid保持
                    change = true;//OnClikの編集有効
                    dialogf();//ダイアログ表示
                }

            }
        }
    };

    public void Changef ()
    {
        int dset = loId / 16;
        Button origin = (Button) scorebutton.get(loId);
        Button second;
        Button out;
        Button sumb = scorebutton.get(dset * 16 + 7);


        if (loId % 16 > 8)//もしロングタップされたボタンが下の段だったら
        {
            loId -= 8;
            origin = (Button) scorebutton.get(loId);
        }

        second = (Button) scorebutton.get(loId + 8);
        out = (Button) keyitem.findViewById(keyId[bId]);

        if (second.getText() == "")//最初の変更は下のボタンが空白なので
        {
            total -= sums[dset];
            sums[dset] -= conversion(origin.getText().toString());//前のデータを引いて
            sums[dset] += conversion(out.getText().toString());//新しいデータを足す
            total += sums[dset];

            if (origin.getText().equals("X")) {
                xcou--;
            } else if (origin.getText().equals("10")) {
                tencou--;
            }
        } else//2回目以降
        {
            sums[dset] -= conversion(second.getText().toString());//前のデータを引いて
            total -= sums[dset];
            sums[dset] += conversion(out.getText().toString());//新しいデータを足す
            total += sums[dset];

            if (second.getText().equals("X")) {
                xcou--;
            } else if (second.getText().equals("10")) {
                tencou--;
            }
        }

        //10点X点をカウント
        if (out.getText().equals("X")) {
            xcou++;
        } else if (out.getText().equals("10")) {
            tencou++;
        }

        if (dset < 6)//セット数が6より小さかったら
        {
            //画面上部の合計の表示のところ
            Button bsum = (Button) findViewById(sumId[dset]);


            //updatescore用

            sums[6] -= Integer.parseInt(bsum.getText().toString());
            sums[6] += sums[dset];

            bsum.setText(sums[dset] + "");
            sum6.setText(sums[6] + "");
        }

        sum10.setText(tencou + "");
        sumx.setText(xcou + "");

        //各セットの合計表示
        second.setText(out.getText().toString());
        sumb.setText(sums[dset] + "");

        //ArrayListも更新する
        point.set(loId + 8, out.getText().toString());
        point.set(dset * 16 + 7, sums[dset] + "");

        try {
            //得点表の更新を行うようにemitする
            send_updatescoreboard(out.getText().toString(), sums[dset] + "", dset + "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /***********************/

        //背景を赤くする
        origin.setBackgroundDrawable(getResources().getDrawable(R.drawable.frame_r));
        second.setBackgroundDrawable(getResources().getDrawable(R.drawable.frame_r));
    }

    //スコアボードの動的生成
    public void scoreboard() {
        if(scoreboardcount < maxperend ) {
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = inflater.inflate(R.layout.score, null);
            itemView.setId(id);

            //set数を表示
            set = (TextView) itemView.findViewById(R.id.textView1);
            set.setText((id + 1) + "回目");

//            LinearLayout num = (LinearLayout)itemView.findViewById(R.id.linearlayout4);
            numbt[id] = (Button)itemView.findViewById(R.id.button1);
//            numbt[id].setText((id + 1) + "回目");

            id++;
            lengtht = (TextView) itemView.findViewById(R.id.lengtht);
            //距離を表示
            if (id != 1) {
                if (length != null) {
                    if (!length.equals("")) {
//                    lengtht.setText(length.toString() + "m");
                        lengtht.setText(length.toString() + "");
                    }
                }
            }

            for (int i = 0; i < 16; i++) {
                //各ボタンにidの振り直し、クリックイベントの付加
                Button mbt = (Button) itemView.findViewById(resId[i]);
                mbt.setId(sid);

                scorebutton.add(mbt);
                if (ro) {
//                    mbt.setEnabled(false);
                }
                else{
                    if (sid % 16 != 0) {
                        mbt.setOnClickListener(this);
                        //OnClick付加
                    } else {
                        mbt.setText(number);
                        //ゼッケンのところにEditTextの値を代入
                        //point.add(base[2].getText().toString());
                    }
                }
                sid++;
            }
            //上のレイアウトとくっつかないように適度に間をつくる
            itemView.setPadding(0,50,0,0);

            //設置
            runOnUiThread(new Runnable() {
                public void run() {
                    box.addView(itemView,0);
                    scoreboardcount++;
                }
            });
            ro_broadcast();
        }else{
            //Toast.makeText(this,"試合は6セットまでです！",Toast.LENGTH_LONG).show();
        }
    }

    public void ro_broadcast(){
        if (ro && broadro) {
            runOnUiThread(new Runnable() {
                //スレットでやらないとうまくいかないっぽい
                public void run() {
                    Log.e("aaaaa", "aaaaaa");
                    //画面上部のレイアウトを更新
                    extra_layout(tencou, xcou, sum);
                    //set数に値が入っていたらそれを変数に代入、保存する
                    if(arraycou.size() > 0) {
                        couset = arraycou.get(0);
                        arraycou.remove(0);
                    }
                    //ボタンに得点を表示する
                    for (int i = (couset - 1) * 16 + 1; i < couset * 16; i++) {
                        //buttonの関連付け
                        Button shoset = (Button) scorebutton.get(i);
                        shoset.setText(point.get(i) + "");
                        if (i % 16 == 7)//合計のとき
                        {
                            shoset = (Button) findViewById(sumId[couset-1]);
                            shoset.setText(point.get(i) + "");
                        }
                    }
                }
            });
        }
    }

    //得点入力Viewの表示
    public void keyboardview() {
        if (key == false) {
            keyinflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            keyitem = keyinflater.inflate(R.layout.keyboard, null);
            for (int i = 0; i < 12; i++) {
                //OnClickの付加
                keybt[i] = (Button) keyitem.findViewById(keyId[i]);
                keybt[i].setOnClickListener(this);
            }
            //設置
            runOnUiThread(new Runnable() {
                public void run() {
                    keyboardSpace.addView(keyitem);
//                    def.setEnabled(false);
                    def.setVisibility(View.INVISIBLE);
                }
            });
            //キーボード表示判断
            key = true;
        }
    }

    //Stringをintに変換
    public int conversion(String str) {
        int a = 0;
        for (int i = 0; i < 12; i++) {
            if (str.equals("X") || str.equals("x")) {
                return 10;
            } else if (str.equals(pos[i])) {
                return i;
            }
        }
        a = Integer.parseInt(str);
        return a;
    }

    //ダイアログ処理
    public void dialogf() {

        //キーボードを非表示にする
        keyboardSpace.removeView(keyitem);
//                def.setEnabled(true);
        Button in = scorebutton.get(acId);
        in.setBackgroundDrawable(getResources().getDrawable(R.drawable.frame));
        def.setVisibility(View.VISIBLE);
        key = false;

        if (loId % 16 > 8)//もしロングタップされたボタンが下の段だったら
        {
            //下の段のIDを入れる
            loId -= 8;
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(context,R.style.Theme_AppCompat_Light_Dialog_Alert));
        // ダイアログの設定
        alertDialog.setTitle("得点の変更");      //タイトル設定
        alertDialog.setMessage(loId/16+1 + "セット目の" + (loId-(loId/16*16)) + "射目が選択されています。" + "\n" + "変更しますか？");  //内容(メッセージ)設定

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // OKボタン押下時の処理
                //Log.v("aaa", "Positive which :" + which);
                keyboardview();//キーボード表示
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // SKIPボタン押下時の処理
                //Log.v("aaa", "Neutral which :" + which);
                change = false;
            }
        });

        // ダイアログの作成と描画
        //alertDialog.create();
        alertDialog.show();

    }

    //接続用関数
    private void connect() throws MalformedURLException {
        System.setProperty("java.net.preferIPv6Addresses", "false");
        try {
//            if (socket == null) {

            Connecthelper.socket[4].open();

            //onイベントの登録
            if(!Connecthelper.socket[4].hasListeners("extractScoreCard")) {
                Connecthelper.socket[4].once("extractScoreCard", extractScoreCard);
            }
            if(!Connecthelper.socket[4].hasListeners("broadcastInsertScore")) {
                Connecthelper.socket[4].once("broadcastInsertScore", broadcastInsertScore);
            }
            if(!Connecthelper.socket[4].hasListeners("broadcastUpdateScore")) {
                Connecthelper.socket[4].once("broadcastUpdateScore", broadcastUpdateScore);
            }
            if(!Connecthelper.socket[4].hasListeners("broadcastInsertNumber")) {
                Connecthelper.socket[4].once("broadcastInsertNumber", broadcastInsertNumber);
            }
            if(!Connecthelper.socket[4].hasListeners("broadcastInsertPrefectures")) {
                Connecthelper.socket[4].once("broadcastInsertPrefectures", broadcastInsertPrefectures);
            }
            if(!Connecthelper.socket[4].hasListeners("broadcastCloseMatch")) {
                Connecthelper.socket[4].once("broadcastCloseMatch", broadcastCloseMatch);
            }
                Connecthelper.socket[4].connect();
                //もし最初にここに入った場合は得点情報を送ってもらうようにemitする
                if (first_flag) {
                    JSONObject json = new JSONObject();
                    Intent get_i = getIntent();
                    Log.e("sc_id", get_i.getIntExtra("sc_id", 1) + "");
                    json.put("sc_id", get_i.getIntExtra("sc_id", 1));
                    json.put("sessionID", sp.getString("sessionID", ""));
                    Connecthelper.socket[4].emit("extractScoreCard", json);
                }

//            }
        } catch (Exception e) {
            Toast.makeText(this, "session error" + e.toString(), Toast.LENGTH_LONG).show();
//            throw new RuntimeException(e);
        }
//        catch (JSONException e) {
//            Toast.makeText(this, "json error" + e.toString(), Toast.LENGTH_LONG).show();
//        }
    }


    //insertScoreの送信
    public void send_insertscoreboard() throws JSONException {
        JSONObject json = new JSONObject();
        int subTotal = 0;//1セットの合計得点

        //↑のemitの戻り値が成功したとかだったら画面遷移
        Intent get_i = getIntent();
        //まだ値決まってないです
        json.put("sc_id", get_i.getIntExtra("sc_id", 1));
        json.put("m_id", sp.getInt("m_id", 1));
        json.put("perEnd", id);
        json.put("sessionID",sp.getString("sessionID",""));

        //得点をjsonobjectにいれていき、そのセットの合計得点も出す
        for (int j = 0; j <= 5; j++) {
            json.put("score_" + (j + 1), setpoint[0][j + 1]);
            subTotal += conversion(setpoint[0][j + 1]);
        }

        //そのセットの点を合計得点に追加する
        total += subTotal;

        json.put("subTotal", subTotal);
        json.put("ten", tencou);
        json.put("x", xcou);
        json.put("total", total);

        Connecthelper.socket[4].emit("insertScore", json);

        //EditTextが変更されていた際にそのデータを送信する
        judgebases();

    }

    //updatedScoreの送信
    public void send_updatescoreboard(String point1, String point2, String dset) throws JSONException {
        JSONObject json = new JSONObject();
        int perEnd = 0;//セット数
        int subTotal = 0;//1セットの合計得点


        //↑のemitの戻り値が成功したとかだったら画面遷移
        Intent get_i = getIntent();
        //まだ値決まってないです
        json.put("perEnd", conversion(dset) + 1);
        json.put("sessionID",sp.getString("sessionID",""));

        //得点表が更新されていた場合は更新された点を、そうでない場合は上段の値を足してそのセットの得点を計算する
        for (int j = 0; j <= 5; j++) {
            if (j + 1 != loId%16) {
                subTotal += conversion(point.get(Integer.parseInt(dset)*16+(j+1)));
            }
            else{
                subTotal += conversion(point.get(Integer.parseInt(dset)*16+(j+1)+8));
            }
        }

        json.put("updatedScore_" + (loId % 16), point1);
        json.put("subTotal", point2);
        json.put("ten", tencou);
        json.put("x", xcou);
        json.put("sc_id", get_i.getIntExtra("sc_id", 1));
        json.put("m_id", sp.getInt("m_id", 1));
        json.put("total", total);

        Connecthelper.socket[4].emit("updateScore", json);

        //EditTextが変更されていた際にそのデータを送信する
        judgebases();
    }

    private Emitter.Listener extractScoreCard = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                String org;
                if(first_flag) {
                    back_flag = false;
                    first_flag = false;
                    JSONObject jsonObject = (JSONObject) args[0];
                    Log.e("extractScoreCard",jsonObject + "");

                    //まずデータが入っていない可能性があるのでここで処理しておく
                    if(jsonObject.has("organizationName")){
                        if(jsonObject.getString("organizationName").toString().equals("null")) {
                            org = "";
                        }
                        else {
                            org = jsonObject.getString("organizationName").toString();
                        }
                    }
                    else{
                        org = "";
                    }
                    if(jsonObject.has("number")){
                        if(jsonObject.getString("number").toString().equals("null")) {
                            number = "";
                        }
                        else {
                            number = jsonObject.getString("number").toString();
                        }
                    }
                    else{
                        number = "";
                    }
                    if(jsonObject.has("prefectures")){
                        if(jsonObject.getString("prefectures").toString().equals("null")) {
                            prefecture = "";
                        }
                        else {
                            prefecture = jsonObject.getString("prefectures").toString();
                        }
                    }
                    else{
                        prefecture = "";
                    }

                    JSONArray scoreArray = jsonObject.getJSONArray("score");

                    int i = 0;
                    while (scoreArray.length() > i) {
                        JSONObject scoreObject = scoreArray.getJSONObject(i);

                        //setごとに得点をまとめる
                        setpoint[0][0] = "";
                        setpoint[1][0] = "";
                        setpoint[1][7] = "";
                        for (int j = 1; j <= 6; j++) {
                            setpoint[0][j] = scoreObject.getString("score_" + j).toString();

                            if (scoreObject.getString("updatedScore_" + j).toString() != "null") {
                                setpoint[1][j] = scoreObject.getString("updatedScore_" + j).toString();
                            } else {
                                setpoint[1][j] = "";
                            }
                        }

                        setpoint[0][7] = scoreObject.getString("subTotal").toString();
                        sums[i] = conversion(setpoint[0][7]);
                        total = Integer.parseInt(jsonObject.getString("total").toString());
                        //まとめた点をarraylistに代入する
                        for (int j = 0; j <= 1; j++) {
                            for (int k = 0; k <= 7; k++) {
                                point.add(setpoint[j][k]);
                                Log.v("aaa", coo + ":" + point.get(coo));
                                coo++;
                            }
                        }
                        i++;
                    }

                    //ten数などのカウント系の処理を行う
                    extra_layout(Integer.parseInt(jsonObject.getString("ten").toString()),
                            Integer.parseInt(jsonObject.getString("x").toString()),
                            Integer.parseInt(jsonObject.getString("total").toString()));

                    scoreboard();//スコアボード生成

                    maxperend = jsonObject.getInt("maxPerEnd");//最大セット数

                    //Edittextの部分の処理を行う
                    extra_edit(length_str[Integer.parseInt(jsonObject.getString("length").toString())],
                            jsonObject.getString("playerName").toString(),
                            jsonObject.getString("created").toString(),
                            org,
                            jsonObject.getString("matchName").toString(),
                            number, prefecture);

                    runOnUiThread(new Runnable() {
                        public void run() {

                            for(int j = 0; j <=coo; j++)//送られてきた値だけループ
                            {
                                if(!ro && coo == j && j != 0) {
                                        Clickoff();//クリックを無効化する
                                        if(j != 96) {
                                        scoreboard();
                                    }
                                }
                                else {
//                                Log.v("aaa", "point:" + point.get(j).toString());
                                    if (j % 16 == 0)//ここって何？
                                    {
                                        if(coo != j && j != 0) {
                                            Clickoff();
                                            scoreboard();
                                        }
                                    } else {
                                        //ボタンに得点を表示させる
                                        exb = (Button) scorebutton.get(j);
                                        exb.setText(point.get(j).toString());

                                        if (j % 16 >= 9 && j % 16 != 15 && !point.get(j).toString().equals("")) {
                                            exb.setBackgroundDrawable(getResources().getDrawable(R.drawable.frame_r));
                                            exb = (Button) scorebutton.get(j - 8);
                                            exb.setBackgroundDrawable(getResources().getDrawable(R.drawable.frame_r));
                                        }

                                        if (j == j / 16 * 16 + 7) {
                                            su = (Button) findViewById(sumId[j / 16]);
                                            su.setText(point.get(j).toString());
                                        }
                                    }
                                }

                            }
                        }
                    });
                    //ダイアログが表示されていたら消す
                    if (m_ProgressDialog.isShowing()) {
                        m_ProgressDialog.dismiss();
                    }
                }
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                //Log.e("", "error    " + e1);
                e1.printStackTrace();
            }
        }
    };
    public void extra_layout(int te,int s_x,int to){
        int cc = 0;
        tencou = te;
        xcou = s_x;
        sums[6] = to;
        runOnUiThread(new Runnable() {
            public void run() {
                sum6.setText(sums[6] + "");//合計反映
                sum10.setText(tencou + "");//10数反映
                sumx.setText(xcou + "");//X数反映
            }
        });
    }

    //試合が終了されたのを受け取る関数
    private Emitter.Listener broadcastCloseMatch = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //試合一覧へ遷移する
            Connecthelper.socket[4].once("broadcastCloseMatch", broadcastCloseMatch);
            Intent intents  = getIntent();
            i2.putExtra("m_id",sp.getInt("m_id", 1));
            i2.putExtra("close", true);
            i2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[4].off();
            Connecthelper.socket[4].close();
            startActivity(i2);

        }
    };

    public void extra_edit(String len, final String nam, final String cre, final String org, final String mat,final String num,final String pre) {
        length = len;

        if(!num.equals("null")){
            number = num;
        }
        if(!pre.equals("null")){
            prefecture = pre;
        }

        runOnUiThread(new Runnable() {
            public void run() {
                if(!length.equals("null")) {
                    lengtht.setText(length);//length(的までの距離)反映
                }
                if(!mat.equals("null")){
                    base[0].setText(mat);//matchName反映
                }
                if(!cre.equals("null")){
                    base[1].setText(cre);//Created(制作日時)反映
                }
                if(!num.equals("null")){
                    base[2].setText(num);//number反映
                }
                if(!nam.equals("null")){
                    base[3].setText(nam);//PlayerName反映
                }
                if(!org.equals("null")){
                    base[4].setText(org);//organizationName反映
                }
                if(!pre.equals("null")){
                    base[5].setText(pre);//organizationName反映
                }
            }
        });
    }

    //変更された値を受け取る
    private Emitter.Listener broadcastUpdateScore = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            //各配列へのデータの格納・・・完了
            //各ボタンへのデータの表示・・・

            try {
                Connecthelper.socket[4].once("broadcastUpdateScore", broadcastUpdateScore);
                final int ssum;
                String ups = null;
                int scou = 0;

                Log.v("aaa", "broadcastUpdateScore");
                //サーバーから受け取った値を格納
                final JSONObject jsonObject = (JSONObject) args[0];
                //各項目を取得及び代入
                tencou = Integer.parseInt(jsonObject.getString("ten").toString());
                xcou = Integer.parseInt(jsonObject.getString("x").toString());
                total = Integer.parseInt(jsonObject.getString("total").toString());
                couset = Integer.parseInt(jsonObject.getString("perEnd").toString());
                ssum = Integer.parseInt(jsonObject.getString("subTotal").toString());
                //何射目の値なのかソート
                for (int i = 1; i <= 6; i++) {
                    if (jsonObject.has("updatedScore_" + i) == true) {
                        ups = jsonObject.getString("updatedScore_" + i).toString();
                        scou = 16 * (couset - 1) + i + 8;
                        break;
                    }
                }

                //更新された部分をarraylistに反映する
                point.set(scou, ups);
                point.set((couset - 1) * 16 + 7, ssum + "");

                //レイアウトに反映
                final int finalScou = scou;
                runOnUiThread(new Runnable() {
                    //スレットでやらないとうまくいかないっぽい
                    public void run() {
                        extra_layout(tencou, xcou, total);

                        //buttonの関連付け
                        Button shoset = scorebutton.get(finalScou);
                        Button shoset2 = scorebutton.get(finalScou - 8);
                        Button shoset3 = scorebutton.get((couset - 1) * 16 + 7);
                        su = (Button) findViewById(sumId[couset - 1]);

                        //button背景赤
                        shoset.setBackgroundDrawable(getResources().getDrawable(R.drawable.frame_r));
                        shoset2.setBackgroundDrawable(getResources().getDrawable(R.drawable.frame_r));
                        //スコア表示
                        shoset.setText(point.get(finalScou) + "");
                        shoset3.setText(point.get((couset - 1) * 16 + 7) + "");
                        sums[couset - 1] = ssum;
                        su.setText(sums[couset - 1] + "");

                    }
                });

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.v("aaa", "e:" + e);
            }

        }
    };
    private Emitter.Listener broadcastInsertScore = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                Connecthelper.socket[4].once("broadcastInsertScore", broadcastInsertScore);
                broadro = true;
                int ssum;
                String ups = null;
                int scou = 0;

                Log.v("aaa", "broadcastInsertScore");
                //サーバーから受け取った値を格納
                final JSONObject jsonObject = (JSONObject) args[0];

                //各項目を取得及び代入
                tencou = Integer.parseInt(jsonObject.getString("ten").toString());
                xcou = Integer.parseInt(jsonObject.getString("x").toString());
                total = Integer.parseInt(jsonObject.getString("total").toString());
                arraycou.add(Integer.parseInt(jsonObject.getString("perEnd").toString()));

                //スコアをArrayリストに格納
                for (int j = 0; j < 8; j++) {

                    if (j == 0)//0はゼッケン
                    {
                        point.add("");
                    } else if (j == 7)//7は小計
                    {
                        point.add(jsonObject.getString("subTotal").toString());
                        sums[Integer.parseInt(jsonObject.getString("perEnd").toString()) - 1] = conversion(jsonObject.getString("subTotal").toString());
                    } else//他は各射
                    {
                        point.add(jsonObject.getString("score_" + j).toString());
                    }
                }
                //修正された得点をArrayリストに格納
                for (int k = 0; k < 8; k++) {
                    point.add("");
                }
                if(ro) {
                    if (arraycou.get(0) != 1) {
                        Clickoff();//クリックを無効化する
                        //スコアボードを生成
                        scoreboard();
                    } else {
                        ro_broadcast();
                    }
                }

                //レイアウトに反映
                runOnUiThread(new Runnable() {
                    //スレットでやらないとうまくいかないっぽい
                    public void run() {
                        //レイアウトの反映
                        extra_layout(tencou, xcou, total);
                        if (!ro) {
                            //現在のセットを取得
                            couset = arraycou.get(0);
                            arraycou.remove(0);
                        }
                        //得点をボタンに表示する
                        for (int i = (couset - 1) * 16 + 1; i < couset * 16; i++) {
                            //buttonの関連付け
                            scorebutton.get(i).setText(point.get(i) + "");
                            if (i % 16 == 7)//合計のとき
                            {
                                Button shoset = (Button) findViewById(sumId[couset - 1]);
                                try {
                                    sums[6] = total;
                                    sums[couset - 1] = jsonObject.getInt("subTotal") ;
                                    shoset.setText(sums[couset - 1]+"");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                sum6.setText(sums[6] + "");
                            }
                        }
//                        }
                    }
                });
                //得点がちゃんと入力されていてreadonlyではなかったら入力用の得点表を表示する
                if(scorebutton.size() == point.size() && !ro) {
                    Clickoff();//クリックを無効化する
                    scoreboard();
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.v("aaa", "e:" + e);
            }
        }
    };

    //ゼッケンが追加されたのを受け取る関数
    private Emitter.Listener broadcastInsertNumber = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                Connecthelper.socket[4].once("broadcastInsertNumber", broadcastInsertNumber);
                //更新された値を保存し、レイアウトに反映する
                JSONObject jsonObject = (JSONObject) args[0];
                number = jsonObject.getString("number");
                runOnUiThread(new Runnable() {
                    public void run() {
                        base[2].setText(number);

                        for(int i=0; i <scoreboardcount;i++){
                            numbt[i].setText(number);
                        }
                    }
                });

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.v("aaa", "e:" + e);
            }
        }
    };

    //都道府県が追加されたのを受け取る関数
    private Emitter.Listener broadcastInsertPrefectures = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                Connecthelper.socket[4].once("broadcastInsertPrefectures", broadcastInsertPrefectures);
                //更新された値を保存し、レイアウトに反映する
                JSONObject jsonObject = (JSONObject) args[0];
                prefecture = jsonObject.getString("Prefectures");
                runOnUiThread(new Runnable() {
                    public void run() {
                        base[5].setText(prefecture);
                    }
                });

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.v("aaa", "e:" + e);
            }
        }
    };

    //ゼッケンが更新された際にそのことをemitする
    public void sendinsertnumber(){
        try {
            //更新されていたらemit
            if(!base[2].getText().toString().equals(number)){
                JSONObject json = new JSONObject();
                Intent get_i = getIntent();
                json.put("sc_id", get_i.getIntExtra("sc_id", 1));
                json.put("sessionID", sp.getString("sessionID", ""));
                json.put("number", base[2].getText().toString());
                Connecthelper.socket[4].emit("insertNumber", json);

                //値を保存しレイアウトに反映する
                number = base[2].getText().toString();

                for(int i=0; i <scoreboardcount;i++) {
                    numbt[i].setText(number);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //都道府県が更新された際にそのことをemitする
    public void sendinsertprefectures(){
        try {
            //更新されていたらemit
            if (!base[5].getText().toString().equals(prefecture)){
                JSONObject json = new JSONObject();
                Intent get_i = getIntent();
                json.put("sc_id", get_i.getIntExtra("sc_id", 1));
                json.put("sessionID", sp.getString("sessionID", ""));
                json.put("prefectures", base[5].getText().toString());
                Connecthelper.socket[4].emit("insertPrefectures", json);

                //価を保存
                prefecture = base[5].getText().toString();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //edittextに変化があったら反映する
    public void judgebases(){
        sendinsertnumber();
        sendinsertprefectures();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //arraylist初期化
//        arraycou = new ArrayList<Integer>();
//        scorebutton = new ArrayList<Button>();
//        point = new ArrayList<String>();

        //Connecthelper.socket[4].close();

//        if(socket != null) {
//            Connecthelper.socket[4].disconnect();
//        }
    }
}
