package example.com.argsandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class Oldscorelist extends ActionBarActivity implements View.OnClickListener{

    Context context = this;
    SharedPreferences sp;
    SharedPreferences.Editor spe = null;
    TextView make_t;
    LinearLayout ll1,ll_h;

    ArrayList<Integer> sc_id;//得点表のid
    ArrayList<String> matchName;//選手名
    ArrayList<String> created;//作成日時
    ArrayList<String> sum;//合計得点
    ArrayList<Integer> perEnd;//セット数
    ArrayList<LinearLayout> ll;
    int id_set = 0;
    Point size;

    boolean first_flag = true;

    Intent alartintent;

    Intent intent;

    Httpobject thred;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oldscorelist);

        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        // ディスプレイのインスタンス生成
        Display disp = wm.getDefaultDisplay();
        size = new Point();
        disp.getSize(size);

        //レイアウトの初期化
        ll1 = (LinearLayout)findViewById(R.id.ll1);

        //arraylistの初期化
        sc_id = new ArrayList<Integer>();
        perEnd = new ArrayList<Integer>();
        created = new ArrayList<String>();
        sum = new ArrayList<String>();
        matchName = new ArrayList<String>();
        ll = new ArrayList<LinearLayout>();

        //インテントの初期化
        alartintent = new Intent(this, Mypage.class);
        intent = new Intent(this, ROScoreboard.class);

        //アクションバー関連
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.oldscorelist);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //ネットワークに接続されているか
        ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info =cm.getActiveNetworkInfo();

        //接続されていない場合ログイン画面に遷移
        if(info==null||!info.isConnected()) {
            Toast.makeText(this, "ネットワークに接続されていません\nログイン画面に遷移します", Toast.LENGTH_LONG).show();

            spe.putString("sessionID","");
            spe.commit();

            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        try {
            //最初の時だけif文の中に入る
            if (first_flag) {
                first_flag = false;
                //最初のレイアウト表示
                make_first();

                //得点表のデータを取得
                thred = new Httpobject(this,this);
                oldlistinformation(thred.execute(10, Connecthelper.ip + "personal/record").get());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    //画面を割合で区切り、初期レイアウトを表示する
    public boolean make_first(){

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(size.x,(int)size.y/10);
        ll_h = new LinearLayout(this);
        ll_h.setOrientation(LinearLayout.HORIZONTAL);
        params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth()),(int)size.y/20);
        ll_h.setLayoutParams(params1);

        //試合名を並べるためのタイトルを作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("試合名");
        params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth())/3,(int)size.y/20);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        //得点表作成日時を並べるためのタイトルを作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("作成日時");
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        //得点を並べるためのタイトルを作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("sum");
        params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth())/6,(int)size.y/20);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        //画面遷移を知らせるための作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("詳細");
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        ll1.addView(ll_h);

        return true;
    }

    public void make_room(){
        String str;

        //選手情報を表示するためのレイアウトを作成
        ll_h = new LinearLayout(this);
        ll_h.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth()),(int)size.y/10);
        ll_h.setLayoutParams(params1);
        ll_h.setId(id_set);
        ll_h.setOnClickListener(this);
        ll1.addView(ll_h);

        //試合名を表示する欄を作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        //5文字以上はレイアウトが崩れるので表示しないようにする
        if(matchName.get(id_set).length() > 5){
            str = matchName.get(id_set).substring(0,4) + "...";
        }
        else{
            str = matchName.get(id_set);
        }
        make_t.setText(str);
        params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth())/3,(int)size.y/ 10);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        //制作日を表示する欄を作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
//        make_t.setText(scoreTotal.get(id_set)+"");
        make_t.setText(created.get(id_set));
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        //合計得点を表示する欄を作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
//        make_t.setText(scoreTotal.get(id_set)+"");
        make_t.setText(sum.get(id_set));
        params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth())/6,(int)size.y/10);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        //矢印を表示する欄を作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("⇒");
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        id_set++;
    }

    public void oldlistinformation(JSONObject jsonObject){
        try {
            //エラーが発生した場合はマイページに遷移させる
            if(jsonObject == null){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context,R.style.Theme_AppCompat_Light_Dialog_Alert));
                alertDialogBuilder.setTitle("エラーが発生しました");

                alertDialogBuilder.setMessage("試合情報の取得に失敗しました");

                alertDialogBuilder.setPositiveButton("OK!",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alartintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(alartintent);
                            }
                        });
                alertDialogBuilder.setCancelable(false);
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
            //データがなかった際もマイページに遷移させる
            else if(jsonObject.getInt("status") == 0){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context,R.style.Theme_AppCompat_Light_Dialog_Alert));
                alertDialogBuilder.setTitle("エラーが発生しました");

                alertDialogBuilder.setMessage("試合のデータが存在しません");

                alertDialogBuilder.setPositiveButton("OK!",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alartintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(alartintent);
                            }
                        });
                alertDialogBuilder.setCancelable(false);
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
            else{
                //データがあったらレイアウトに表示していく
                JSONArray jsonArray = jsonObject.getJSONArray("record");
                int i = 0;
                while(jsonArray.length() > i){
                    //arraylistにデータを追加する
                    JSONObject scoreObject = jsonArray.getJSONObject(i);
                    sc_id.add(scoreObject.getInt("sc_id"));
                    perEnd.add(scoreObject.getInt("perEnd"));
                    created.add(scoreObject.getString("created"));
                    sum.add(scoreObject.getString("sum"));
                    matchName.add(scoreObject.getString("matchName"));

                    //データを表示する
                    make_room();
                    i++;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_oldscorelist, menu);
        return true;
    }

    //アクションバーのメニューのボタン処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            //ひとつ前のマイページに遷移する
            Intent i = new Intent(this,Mypage.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
            startActivity(warpanyway);
            return true;
        }
        //試合一覧へ遷移
        if (id == R.id.menu_matchlist) {
            warpanyway = new Intent(this,Matchlist.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(warpanyway);
        }
        //試合作成へ遷移
        if (id == R.id.menu_createmat) {
            warpanyway = new Intent(this,Createmat.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(warpanyway);
        }
        //マイページに遷移
        if (id == R.id.menu_mypage) {
            warpanyway = new Intent(this,Mypage.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(warpanyway);
        }
        //団体ページに遷移
        if (id == R.id.menu_orgpage) {
            warpanyway = new Intent(this,Orgpage.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(warpanyway);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(final View v) {
        final int Id = v.getId();

        //クリックした試合の情報を表示しする
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context,R.style.Theme_AppCompat_Light_Dialog_Alert));
        alertDialogBuilder.setTitle("得点表を表示しますか？");
        alertDialogBuilder.setMessage("試合開始日　:　" + created.get(v.getId()) +
                "\n試合名　:　" + matchName.get(v.getId()) +
                "\n得点　:　" + sum.get(v.getId()) +
                "\nセット数　:　" + perEnd.get(v.getId()));
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
                            intent.putExtra("sc_id", sc_id.get(Id));
                            Oldscorelist.this.intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(Oldscorelist.this.intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        alertDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            //ひとつ前の画面に遷移する
            Intent i = new Intent(this,Mypage.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
        return false;
    }
}
