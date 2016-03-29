/*
    制作レポート
    ver：0.17.1(01/25)
    制作開始段階、基本読み込みを行っていた段階のため、少々コーディングを始めたレベル

    ver：0.17.2(01/25)
    制作中。
    sc_idを廃止、p_idで人数、sc_score_sc_idで得点票の数を取り扱う方向に変更
    sc_score,sc_score_sc_id,sc_score_p_idの3つを追加となる
    sc_scoreは得点票の点数、sc_score_sc_idはその得点票のsc_id、sc_score_p_idはその得点票制作者のp_idとなる
    avgは出力時にはdoubleにしているが、scoreTotalで基本管理
    broadcastupdatescoreは表示を除き、おそらく動作可能
    rank_sort() を追加。ここに入ることで、表示、ランキングの変更を行う。バグる可能性大

    ver:0.17.4(02/18)
    rankingを主に変更。
    SUMのextractと2つのbroadcastの両方の稼働を確認。
    ただし、2つのアカウントなのでデバッグ必須。
    avgはデバッグしていないので次のバージョンでは改良、デバッグを行う

    メモ：得点票で、起動時1つセットが多い(未確定)、
    ランキングにいながら得点票がバグったせいで強制終了(多分改善済み)
    を発見。デバッグ対象。

    ver:0.17.5(02/26)
    sum,avgの一定水準までは制作完了
    だが、一人で制作、二端末でのデバッグなので人手を借りてのデバッグは必須
    コードでは予定から大きな変更はなし
    ただ、sc_countなどのsc_系は使っていないものがあるので今後減らす

    ver:0.17.6(02/26)
    sum,avgのupdatescoreをいれていなかったので追加
    他に変更点はなし

    ver0.17.7(03/05)
    ランキングのonpause関係を追加してバグ？の対処を行う
    しかしむしろバグが増えたのでコードを少しきれいにしただけ

    ver0.1.7.8
    動作をかなり大きく変更
    broadcastが来る→extractで値を取り直すように変更
    動作速度は最小限度の更新にしたためか。もしくは通信のし直しをなくしたために高速化
    動作はそこまで検証できていないがほぼ安定。むしろ以前のバージョンよりはるかに安定。
    またonpause系は廃止
    レイアウトのsum,avgの文も変更
    以降はソースの大幅変更にともなった関数化が課題

    ver0.1.7.9
    avg時の条件文でupdate等が動かなかったバグが判明
    条件文の中に得点を比較して高かったらレイアウトを変更というものを追加
    以降はソースの大幅変更にともなった関数化が課題

    ver0.1.8.3(最終)
    avg時の細かい点を修正
    コメントの追加も行う
 */

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
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.ArrayList;


public class Rankinglist extends ActionBarActivity implements OnClickListener {

    ArrayList<TextView> sctxt;
    ArrayList<TextView> scoretxt;
    ArrayList<TextView> nametxt;
    ArrayList<TextView> ranktxt;
    ArrayList<Integer> shotScore;
    ArrayList<Integer> setNum;
    ArrayList<String> matchName;
    ArrayList<Integer> length;
    ArrayList<String> perEnd;
    ArrayList<LinearLayout> ll;
    ArrayList<LinearLayout> ll2;

    int save_id;
    int count = -1;

    ArrayList<String> playerName;
    ArrayList<Integer> total = null;
    ArrayList<Integer> sc_id;
    ArrayList<Integer> sc_count;
    ArrayList<Integer> p_id;
    ArrayList<Integer> sc_score;
    ArrayList<Integer> sc_score_p_id;
    ArrayList<Integer> sc_score_sc_id;
    ArrayList<Integer> arrowsTotal;
    ArrayList<Integer> rank;
    ArrayList<Integer> scoreTotal;

    int id_set = 0;
    boolean back_flag = true;
    boolean connect_flag = false;
    boolean firstlayoutset_flag = false;

    //true = sum   false = avg
    boolean sumflag = true;

    Point size;
    LinearLayout ll1;
    LinearLayout ll_h;
    TextView make_t;
    Button btn1;
    Intent get_i;
    Intent test_i;
    Intent test2_i;
    Intent i,j;

    boolean maker = false;

    SharedPreferences sp = null;
    SharedPreferences.Editor spe = null;

    public ProgressDialog m_ProgressDialog,m_ProgressDialog2;

    Context context = this;

    Button sumtb,avgtb;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.rankinglist);

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        // ディスプレイのインスタンス生成
        Display disp = wm.getDefaultDisplay();
        size = new Point();
        //画面サイズ取得
        disp.getSize(size);

        //buttonの初期化とクリックイベント付与
        btn1 = (Button) findViewById(R.id.button1);
        btn1.setOnClickListener(this);

        //ToggleButtonの初期化とイベント付与
        sumtb = (Button)findViewById(R.id.sumbutton);
        avgtb = (Button)findViewById(R.id.avgbutton);
        sumtb.setOnClickListener(this);
        avgtb.setOnClickListener(this);

        sumtb.setEnabled(false);
        avgtb.setEnabled(false);

        //ArrayListの初期化
        sctxt = new ArrayList<TextView>();
        scoretxt = new ArrayList<TextView>();
        nametxt = new ArrayList<TextView>();
        ranktxt = new ArrayList<TextView>();
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
        p_id = new ArrayList<Integer>();
        sc_count = new ArrayList<Integer>();
        sc_score = new ArrayList<Integer>();
        sc_score_p_id = new ArrayList<Integer>();
        sc_score_sc_id = new ArrayList<Integer>();
        arrowsTotal = new ArrayList<Integer>();
        scoreTotal = new ArrayList<Integer>();
        rank  = new ArrayList<Integer>();

        //intentの初期化
        test_i = new Intent(this, Scoreboard.class);
        test2_i = new Intent(this, ROScoreboard.class);
        i = new Intent(this, Scorelist.class);

        //linearlayoutの初期化
        ll1 = (LinearLayout) findViewById(R.id.ll1);

        //SaredPrefrenceの初期化
        sp = getSharedPreferences("setting", MODE_MULTI_PROCESS);
        spe = sp.edit();

        //Dialog初期化
        m_ProgressDialog2 = new ProgressDialog(this);
        m_ProgressDialog2.setCancelable(false);

        get_i = getIntent();
        //m_idの取得
        spe.putInt("m_id", get_i.getIntExtra("m_id", 1));
        spe.commit();

        //アクションバー関連
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.rankinglist);
        actionBar.setDisplayHomeAsUpEnabled(true);

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

        sumflag = true;
        //ll1.removeAllViews();
        //make_first();
        sumtb.setBackgroundResource(R.drawable.tab_selected);

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
            Connecthelper.socket[5].off();
            Connecthelper.socket[5].close();
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
            Intent intent = new Intent(this, Scorelist.class);
            intent.putExtra("m_id", sp.getInt("m_id", 1));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[5].off();
            Connecthelper.socket[5].close();
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
            Connecthelper.socket[5].off();
            Connecthelper.socket[5].close();
            startActivity(warpanyway);
            return true;
        }
        //試合作成へ遷移
        if (id == R.id.menu_createmat) {
            warpanyway = new Intent(this,Createmat.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[5].off();
            Connecthelper.socket[5].close();
            startActivity(warpanyway);
        }
        //試合一覧へ遷移
        if (id == R.id.menu_matchlist) {
            warpanyway = new Intent(this,Matchlist.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[5].off();
            Connecthelper.socket[5].close();
            startActivity(warpanyway);
        }
        //マイページに遷移
        if (id == R.id.menu_mypage) {
            warpanyway = new Intent(this,Mypage.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[5].off();
            Connecthelper.socket[5].close();
            startActivity(warpanyway);
        }
        //団体ページに遷移
        if (id == R.id.menu_orgpage) {
            warpanyway = new Intent(this, Orgpage.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[5].off();
            Connecthelper.socket[5].close();
            startActivity(warpanyway);
        }
        //得点表作成へ遷移
        if (id == R.id.menu_createsco) {
            warpanyway = new Intent(this,Createsco.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            warpanyway.putExtra("m_id", sp.getInt("m_id", 0));
            Connecthelper.socket[5].off();
            Connecthelper.socket[5].close();
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
                                    Connecthelper.socket[5].emit("closeMatch", json);
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
            Connecthelper.socket[5].off();
            Connecthelper.socket[5].close();
            startActivity(sintent);
//            finish();
        }
        //sumの表示
        else if(v.getId() == R.id.sumbutton)
        {
            if(sumflag == false) {
                sumflag = true;

                //通信できたらプログレスダイアログを表示する
                m_ProgressDialog2 = new ProgressDialog(this);

                m_ProgressDialog2.setMessage("少々お待ちください");

                m_ProgressDialog2.setCancelable(false);

                if(!m_ProgressDialog2.isShowing()) {
                    m_ProgressDialog2.show();
                }

                Initializationarr();

                try {
                    //何回入っても大丈夫なようにする
                    Intent get_i = getIntent();
                    JSONObject json = new JSONObject();
                    json.put("m_id", sp.getInt("m_id", 0));
                    json.put("sessionID", sp.getString("sessionID", ""));

                    if(sumflag) {
                        //この試合の合計得点のランクを取得する
                        Connecthelper.socket[5].emit("extractTotalRankingIndex", json);
                    }
                    else {
                        //この試合の合計得点のランクを取得する
                        Connecthelper.socket[5].emit("extractAvgRankingIndex", json);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // make_first();
                sumtb.setBackgroundResource(R.drawable.tab_selected);
                avgtb.setBackgroundResource(R.drawable.tab_unselected);

                sumtb.setEnabled(false);
                avgtb.setEnabled(false);
            }
        }
        //avgの表示
        else if(v.getId() == R.id.avgbutton)
        {
            if(sumflag == true) {
                sumflag = false;

                Initializationarr();

                m_ProgressDialog2 = new ProgressDialog(this);

                m_ProgressDialog2.setMessage("少々お待ちください");

                m_ProgressDialog2.setCancelable(false);

                if(!m_ProgressDialog2.isShowing()) {
                    m_ProgressDialog2.show();
                }

                try {
                    //何回入っても大丈夫なようにする
                    Intent get_i = getIntent();
                    JSONObject json = new JSONObject();
                    json.put("m_id", sp.getInt("m_id", 0));
                    json.put("sessionID", sp.getString("sessionID", ""));

                    if(sumflag) {
                        //この試合の合計得点のランクを取得する
                        Connecthelper.socket[5].emit("extractTotalRankingIndex", json);
                    }
                    else {
                        //この試合の合計得点のランクを取得する
                        Connecthelper.socket[5].emit("extractAvgRankingIndex", json);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                avgtb.setBackgroundResource(R.drawable.tab_selected);
                sumtb.setBackgroundResource(R.drawable.tab_unselected);

                sumtb.setEnabled(false);
                avgtb.setEnabled(false);
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

        //ランクを並べるためのタイトルを作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("ランク");
        params1 = new LinearLayout.LayoutParams((int) (ll1.getWidth()) / 6, (int) size.y / 20);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        //選手名を並べるためのタイトルを作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("選手名");
        params1 = new LinearLayout.LayoutParams((int) (ll1.getWidth()) / 2, (int) size.y / 20);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        //得点を並べるためのタイトルを作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("得点");
        params1 = new LinearLayout.LayoutParams((int) (ll1.getWidth()) / 3, (int) size.y / 20);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        //詳細を並べるためのタイトルを作成
//        make_t = new TextView(this);
//        make_t.setGravity(Gravity.CENTER);
//        make_t.setText("詳細");
//        params1 = new LinearLayout.LayoutParams((int) (ll1.getWidth()) / 6, (int) size.y / 20);
//        make_t.setLayoutParams(params1);
//        make_t.setBackgroundResource(R.drawable.frame);
//        ll_h.addView(make_t);

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
        //ll_h.setOnClickListener(in);

        //ランクを表示する
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
//        make_t.setText(scoreTotal.get(id_set)+"");


        make_t.setText(rank.get(id_set) + "");
        params1 = new LinearLayout.LayoutParams((int) (ll1.getWidth()) / 6, (int)size.y / 10);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
//        make_t.setTypeface(Typeface.createFromAsset(getAssets(), "JiyunoTsubasa.ttf"));
        ll_h.addView(make_t);
        ranktxt.add(make_t);

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
        nametxt.add(make_t);


        //得点を表示する
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        if(sumflag) {
            make_t.setText(scoreTotal.get(id_set) + "");
        }
        else{
            //合計が0の場合小数点以下の計算ができないので分ける
            if(scoreTotal.get(id_set)  == 0 || arrowsTotal.get(id_set) == 0){
                make_t.setText("0.0");
            }
            else{
                BigDecimal bd = new BigDecimal((double) Math.round(scoreTotal.get(id_set)) / arrowsTotal.get(id_set));
                make_t.setText(bd.setScale(1, BigDecimal.ROUND_HALF_UP) + "");
            }
        }
        params1 = new LinearLayout.LayoutParams((int) (ll1.getWidth()) / 3, (int)size.y / 10);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
//        make_t.setTypeface(Typeface.createFromAsset(getAssets(), "JiyunoTsubasa.ttf"));
        ll_h.addView(make_t);
        scoretxt.add(make_t);

        //矢印を表示する
//        make_t = new TextView(this);
//        make_t.setGravity(Gravity.CENTER);
//        make_t.setText("⇒");
//        params1 = new LinearLayout.LayoutParams((int) (ll1.getWidth()) / 6, (int)size.y / 10);
//        make_t.setLayoutParams(params1);
//        make_t.setBackgroundResource(R.drawable.frame);
////        make_t.setTypeface(Typeface.createFromAsset(getAssets(), "JiyunoTsubasa.ttf"));
//        ll_h.addView(make_t);
//        sctxt.add(make_t);

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

    //追加するレイアウトをスレッドを使って表示する
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
                Connecthelper.socket[5].emit("checkPermission", json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //接続用関数
    private void connect() throws MalformedURLException {
        System.setProperty("java.net.preferIPv6Addresses", "false");
        try {
            //if(Connecthelper.socket[5] != null) {
            Connecthelper.socket[5].open();
            //}
            //onイベントの登録
            if(!Connecthelper.socket[5].hasListeners("extractTotalRankingIndex")) {
                Connecthelper.socket[5].once("extractTotalRankingIndex", extractTotalRankingIndex);
                Log.e("open","extractTotalRankingIndex");
            }
            if(!Connecthelper.socket[5].hasListeners("extractAvgRankingIndex")) {
                Connecthelper.socket[5].once("extractAvgRankingIndex", extractAvgRankingIndex);
                Log.e("open", "extractAvgRankingIndex");
            }
            if(!Connecthelper.socket[5].hasListeners("broadcastInsertScoreCard")) {
                Connecthelper.socket[5].once("broadcastInsertScoreCard", broadcastInsertScoreCard);
                Log.e("open", "broadcastInsertScoreCard");
            }
            if(!Connecthelper.socket[5].hasListeners("broadcastInsertScore")) {
                Connecthelper.socket[5].once("broadcastInsertScore", broadcastInsertScore);
                Log.e("open", "broadcastInsertScore");
            }
            if(!Connecthelper.socket[5].hasListeners("broadcastUpdateScore")) {
                Connecthelper.socket[5].once("broadcastUpdateScore", broadcastUpdateScore);
                Log.e("open", "broadcastUpdateScore");
            }

            Connecthelper.socket[5].connect();

            //何回入っても大丈夫なようにする
            Intent get_i = getIntent();
            JSONObject json = new JSONObject();
            json.put("m_id", sp.getInt("m_id", 0));
            json.put("sessionID", sp.getString("sessionID", ""));

            if(sumflag) {
                //この試合の合計得点のランクを取得する
                Connecthelper.socket[5].emit("extractTotalRankingIndex", json);
            }
            else {
                //この試合の合計得点のランクを取得する
                Connecthelper.socket[5].emit("extractAvgRankingIndex", json);
            }
        } catch (Exception e) {
            Toast.makeText(this, "error" + e.toString(), Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }
    }

    private Emitter.Listener broadcastInsertScoreCard = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Connecthelper.socket[5].once("broadcastInsertScoreCard", broadcastInsertScoreCard);

            try {
                Intent get_i = getIntent();
                JSONObject json = new JSONObject();
                json.put("m_id", sp.getInt("m_id", 0));
                json.put("sessionID", sp.getString("sessionID", ""));

                if(sumflag) {
                    //この試合の合計得点のランクを取得する
                    Connecthelper.socket[5].emit("extractTotalRankingIndex", json);
                }
                else {
                    //この試合の合計得点のランクを取得する
                    Connecthelper.socket[5].emit("extractAvgRankingIndex", json);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener broadcastInsertScore = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //argsがなぜかSocketIO時はJSONObjectだったのがJSONArrayになっていたので注意
            final JSONObject jsonObject = (JSONObject) args[0];
            Connecthelper.socket[5].once("broadcastInsertScore", broadcastInsertScore);
            try {
                Intent get_i = getIntent();
                JSONObject json = new JSONObject();
                json.put("m_id", sp.getInt("m_id", 0));
                json.put("sessionID", sp.getString("sessionID", ""));

                if(sumflag) {
                    //この試合の合計得点のランクを取得する
                    Connecthelper.socket[5].emit("extractTotalRankingIndex", json);
                }
                else {
                    //この試合の合計得点のランクを取得する
                    Connecthelper.socket[5].emit("extractAvgRankingIndex", json);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener broadcastUpdateScore = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Connecthelper.socket[5].once("broadcastUpdateScore", broadcastUpdateScore);
            try {
                Intent get_i = getIntent();
                JSONObject json = new JSONObject();
                json.put("m_id", sp.getInt("m_id", 0));
                json.put("sessionID", sp.getString("sessionID", ""));

                if(sumflag) {
                    //この試合の合計得点のランクを取得する
                    Connecthelper.socket[5].emit("extractTotalRankingIndex", json);
                }
                else {
                    //この試合の合計得点のランクを取得する
                    Connecthelper.socket[5].emit("extractAvgRankingIndex", json);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener extractTotalRankingIndex = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            int i = 0;
            final ArrayList<Integer> rank_get;
            final ArrayList<Integer> scoreTotal_get;
            final ArrayList<String> playerName_get;
            rank_get = new ArrayList<Integer>();
            scoreTotal_get = new ArrayList<Integer>();
            playerName_get = new ArrayList<String>();

            try {
                Connecthelper.socket[5].once("extractTotalRankingIndex", extractTotalRankingIndex);
                ll = new ArrayList<LinearLayout>();
                if(rank.size() == 0 && !firstlayoutset_flag) {
                    make_first();
                }
                //データが入っていたら
                if (!args[0].toString().equals("")){
                    JSONArray receive1 = (JSONArray) args[0];
                    //JSONArray eventArray = receive1.getJSONArray("id");
                    if (receive1.length() != 0) {
                        while (receive1.length() != i) {
                            //送られてきた得点表の値をTextViewに表示する
                            JSONObject jsonObject = receive1.getJSONObject(i);
                            if(!firstlayoutset_flag) {
                                rank.add(jsonObject.getInt("rank"));
                                p_id.add(jsonObject.getInt("p_id"));
                                playerName.add(jsonObject.getString("playerName").toString());
                                scoreTotal.add(jsonObject.getInt("total"));
                                make_scorelist(false);
                            }
                            else if(rank.size() == 0){
                                rank.add(jsonObject.getInt("rank"));
                                p_id.add(jsonObject.getInt("p_id"));
                                playerName.add(jsonObject.getString("playerName").toString());
                                scoreTotal.add(jsonObject.getInt("total"));
                                make_scorelist(false);
                            }
                            //その場しのぎです。要素を追加したいだけ(改善案がおもいつきません)
                            else if(rank.size() <= i){
                                id_set--;
                                make_scorelist(false);
                            }

                            rank_get.add(jsonObject.getInt("rank"));
                            scoreTotal_get.add(jsonObject.getInt("total"));
                            playerName_get.add(jsonObject.getString("playerName").toString());
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
                if(!firstlayoutset_flag || rank.size() <= i) {
                    ll_addview();
                    if (m_ProgressDialog.isShowing()) {
                        m_ProgressDialog.dismiss();
                    }
                    if (m_ProgressDialog2.isShowing()) {
                        m_ProgressDialog2.dismiss();
                    }
                    runOnUiThread(new Runnable() {

                        public void run() {
                            sumtb.setEnabled(true);
                            avgtb.setEnabled(true);
                        }
                    });
                }
                if(firstlayoutset_flag){
                    int j = 0;
                    while (rank_get.size() != j) {
                        final int finalI = j;
                        if(rank.size() <= j){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    ranktxt.get(finalI).setText(rank_get.get(finalI) + "");
                                }
                            });
                        }
                        else if (rank_get.get(finalI) != rank.get(finalI)) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    ranktxt.get(finalI).setText(rank_get.get(finalI) + "");
                                }
                            });
                        }
                        if(scoreTotal.size() <= j){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    scoretxt.get(finalI).setText(scoreTotal_get.get(finalI) + "");
                                }
                            });
                        }
                        else if (scoreTotal_get.get(finalI) != scoreTotal.get(finalI)) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    scoretxt.get(finalI).setText(scoreTotal_get.get(finalI) + "");
                                }
                            });
                        }
                        if(playerName.size() <= j){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    nametxt.get(finalI).setText(playerName_get.get(finalI) + "");
                                }
                            });
                        }
                        else if (playerName_get.get(finalI) != playerName.get(finalI)) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    nametxt.get(finalI).setText(playerName_get.get(finalI));
                                }
                            });
                        }
                        j++;
                    }
                    /*if(rank.size() > rank_get.size()) {
                        int k = rank_get.size();
                        while(rank.size() == k) {
                            rank_get.add(rank.get(k));
                            scoreTotal_get.add(scoreTotal.get(k));
                            k++;
                        }
                    }*/
                    rank = rank_get;
                    scoreTotal = scoreTotal_get;
                    playerName = playerName_get;
                }

                if(!firstlayoutset_flag){
                    firstlayoutset_flag = true;
                }


            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    };

    private Emitter.Listener extractAvgRankingIndex = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            int i = 0;
            final ArrayList<Integer> rank_get;
            final ArrayList<Integer> scoreTotal_get;
            final ArrayList<Integer> arrowsTotal_get;
            final ArrayList<String> playerName_get;
            rank_get = new ArrayList<Integer>();
            scoreTotal_get = new ArrayList<Integer>();
            arrowsTotal_get = new ArrayList<Integer>();
            playerName_get = new ArrayList<String>();
            try {
                Connecthelper.socket[5].once("extractAvgRankingIndex", extractAvgRankingIndex);
                ll = new ArrayList<LinearLayout>();
                if(rank.size() == 0 && !firstlayoutset_flag) {
                    make_first();
                }
                //データが入っていたら
                if (!args[0].toString().equals("")){
                    JSONArray receive1 = (JSONArray) args[0];
                    //JSONArray eventArray = receive1.getJSONArray("id");
                    if (receive1.length() != 0) {
                        while (receive1.length() != i) {
                            //送られてきた得点表の値をTextViewに表示する
                            JSONObject jsonObject = receive1.getJSONObject(i);
                            if(!firstlayoutset_flag) {
                                rank.add(jsonObject.getInt("rank"));
                                p_id.add(jsonObject.getInt("p_id"));
                                playerName.add(jsonObject.getString("playerName").toString());
                                scoreTotal.add(jsonObject.getInt("scoreTotal"));
                                arrowsTotal.add(jsonObject.getInt("arrowsTotal"));
                                make_scorelist(false);
                            }
                            else if(rank.size() == 0){
                                rank.add(jsonObject.getInt("rank"));
                                p_id.add(jsonObject.getInt("p_id"));
                                playerName.add(jsonObject.getString("playerName").toString());
                                scoreTotal.add(jsonObject.getInt("scoreTotal"));
                                arrowsTotal.add(jsonObject.getInt("arrowsTotal"));
                                make_scorelist(false);
                            }
                            //その場しのぎです。要素を追加したいだけ
                            else if(rank.size() <= i){
                                id_set--;
                                make_scorelist(false);
                            }

                            rank_get.add(jsonObject.getInt("rank"));
                            scoreTotal_get.add(jsonObject.getInt("scoreTotal"));
                            arrowsTotal_get.add(jsonObject.getInt("arrowsTotal"));
                            playerName_get.add(jsonObject.getString("playerName").toString());

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
                if(!firstlayoutset_flag || rank.size() <= i) {
                    ll_addview();
                    if (m_ProgressDialog.isShowing()) {
                        m_ProgressDialog.dismiss();
                    }
                    if (m_ProgressDialog2.isShowing()) {
                        m_ProgressDialog2.dismiss();
                    }
                    runOnUiThread(new Runnable() {

                        public void run() {
                            sumtb.setEnabled(true);
                            avgtb.setEnabled(true);
                        }
                    });
                }
                if(firstlayoutset_flag){
                    int j = 0;
                    while (rank_get.size() != j) {
                        final int finalI = j;
                        if(rank.size() <= j){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    ranktxt.get(finalI).setText(rank_get.get(finalI) + "");
                                }
                            });
                        }
                        else if (rank_get.get(finalI) != rank.get(finalI)) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    ranktxt.get(finalI).setText(rank_get.get(finalI) + "");
                                }
                            });
                        }
                        if(scoreTotal.size() <= j || arrowsTotal.size() <= j){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    if(scoreTotal_get.get(finalI) == 0 || arrowsTotal_get.get(finalI) == 0){
                                        scoretxt.get(finalI).setText("0.0");
                                    }
                                    else {
                                        BigDecimal bd = new BigDecimal((double) Math.round(scoreTotal_get.get(finalI)) / arrowsTotal_get.get(finalI));
                                        scoretxt.get(finalI).setText(bd.setScale(1, BigDecimal.ROUND_HALF_UP) + "");
                                    }
                                }
                            });
                        }
                        else if (arrowsTotal_get.get(finalI) != arrowsTotal.get(finalI) || scoreTotal_get.get(finalI) != scoreTotal.get(finalI)) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    if(scoreTotal_get.get(finalI) == 0 || arrowsTotal_get.get(finalI) == 0){
                                        scoretxt.get(finalI).setText("0.0");
                                    }
                                    else {
                                        BigDecimal bd = new BigDecimal((double) Math.round(scoreTotal_get.get(finalI)) / arrowsTotal_get.get(finalI));
                                        scoretxt.get(finalI).setText(bd.setScale(1, BigDecimal.ROUND_HALF_UP) + "");
                                    }
                                }
                            });
                        }
                        if(playerName.size() <= j){
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    nametxt.get(finalI).setText(playerName_get.get(finalI) + "");
                                }
                            });
                        }
                        else if (playerName_get.get(finalI) != playerName.get(finalI)) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    nametxt.get(finalI).setText(playerName_get.get(finalI));
                                }
                            });
                        }
                        j++;
                    }
                    /*if(rank.size() > rank_get.size()) {
                        int k = rank_get.size();
                        while(rank.size() == k) {
                            rank_get.add(rank.get(k));
                            scoreTotal_get.add(scoreTotal.get(k));
                            k++;
                        }
                    }*/
                    rank = rank_get;
                    scoreTotal = scoreTotal_get;
                }

                if(!firstlayoutset_flag){
                    firstlayoutset_flag = true;
                }
            } catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }
    };

    //バックキーを押した時の動作
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            i.putExtra("m_id", sp.getInt("m_id", 0));
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Connecthelper.socket[5].off();
            Connecthelper.socket[5].close();
            startActivity(i);
        }
        return false;
    }

    private void Initializationarr()
    {
        sctxt = new ArrayList<TextView>();
        scoretxt = new ArrayList<TextView>();
        nametxt = new ArrayList<TextView>();
        ranktxt = new ArrayList<TextView>();
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
        sc_count = new ArrayList<Integer>();
        sc_score = new ArrayList<Integer>();
        sc_score_p_id = new ArrayList<Integer>();
        sc_score_sc_id = new ArrayList<Integer>();
        arrowsTotal = new ArrayList<Integer>();
        scoreTotal = new ArrayList<Integer>();
        rank  = new ArrayList<Integer>();
        p_id = new ArrayList<Integer>();
        id_set = 0;
        ll1.removeAllViews();
        firstlayoutset_flag = false;
    }
}