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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class Orgpage extends ActionBarActivity implements View.OnClickListener{

    Context context = this;
    SharedPreferences sp;
    SharedPreferences.Editor spe = null;
    TextView name,created,count,admin,place,email;
    Intent intent;

    boolean makeflag = true;
    boolean master = true;
    boolean first_flag = true;
    LinearLayout ll1;
    int id_set = 0;
    Point size;
    int o_id = 0;
    Button bt1;

    Httpobject thred;

    ArrayList<String> birth;//生年月日
    ArrayList<String> playerName;//選手名
    ArrayList<String> emails;//Eメール

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orgpage);

        //SaredPrefrenceの初期化
        sp = getSharedPreferences("setting", MODE_MULTI_PROCESS);
        spe = sp.edit();

        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        // ディスプレイのインスタンス生成
        Display disp = wm.getDefaultDisplay();
        size = new Point();
        disp.getSize(size);

        //textview等の初期化
        name = (TextView)findViewById(R.id.textView1);
        created = (TextView)findViewById(R.id.textView2);
        count = (TextView)findViewById(R.id.textView3);
        admin = (TextView)findViewById(R.id.textView4);
        place = (TextView)findViewById(R.id.textView5);
        email = (TextView)findViewById(R.id.textView6);
        ll1 = (LinearLayout)findViewById(R.id.ll1);
        bt1 = (Button)findViewById(R.id.button1);

        //クリックイベントの付与
        bt1.setOnClickListener(this);


        //intentの初期化
        intent = new Intent(this, Mypage.class);

        //ArrayListの初期化
        birth = new ArrayList<String>();
        playerName = new ArrayList<String>();
        emails = new ArrayList<String>();



        //アクションバー関連
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.grouppage);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //ネットワークに接続されているか確認する
        ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info =cm.getActiveNetworkInfo();

        //接続されていない場合はsessionIDを破棄してログイン画面に遷移する
        if(info==null||!info.isConnected()) {
            Toast.makeText(this, "ネットワークに接続されていません\nログイン画面に遷移します", Toast.LENGTH_LONG).show();

            spe.putString("sessionID", "");
            spe.commit();

            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //最初にこの変数に入ったら団体情報の取得を行うためのhttpを送信する
        if(first_flag) {
            first_flag = false;
            thred = new Httpobject(this, this);
            try {
                orgpageinformation(thred.execute(4, Connecthelper.ip + "organization").get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void orgpageinformation(JSONObject jsonObject){
        try {
            if(jsonObject != null) {
                //団体に所属していなかったらマイページに強制遷移
                if (jsonObject.getInt("status") == 0) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context,R.style.Theme_AppCompat_Light_Dialog_Alert));
                    alertDialogBuilder.setTitle("エラーが発生しました");

                    alertDialogBuilder.setMessage("団体に所属していないため、ページを表示できません");

                    alertDialogBuilder.setPositiveButton("OK!",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            });
                    alertDialogBuilder.setCancelable(false);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {

                    make_first();

                    //もし堕胎に所属していたら各種情報を表示する
                    o_id = jsonObject.getInt("o_id");
                    name.setText(jsonObject.getString("organizationName").toString());
                    created.setText(jsonObject.getString("establish").toString());
                    count.setText(jsonObject.getString("members").toString());
                    admin.setText(jsonObject.getString("admin").toString());
                    place.setText(jsonObject.getString("place").toString());
                    email.setText(jsonObject.getString("email").toString());
                    master = jsonObject.getBoolean("permission");
                    //団体管理者でなかったら団体管理へ遷移できないようにする
                    if(!master){
                        bt1.setVisibility(View.GONE);
                    }

                    //団体メンバーの情報をArrayListに入れて順に出力していく
                    JSONArray jsonArray = jsonObject.getJSONArray("memberList");
                    int i = 0;
                    while (jsonArray.length() > i) {
                        JSONObject memberobject = jsonArray.getJSONObject(i);
                        playerName.add(memberobject.getString("playerName"));
                        birth.add(memberobject.getString("birth"));
                        emails.add(memberobject.getString("email"));
                        make_playerinfo();
                        i++;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //画面を割合で区切り、初期レイアウトを表示する
    public boolean make_first(){

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(size.x,(int)size.y/10);
        LinearLayout ll_h = new LinearLayout(this);
        ll_h.setOrientation(LinearLayout.HORIZONTAL);
        params1 = new LinearLayout.LayoutParams((int)ll1.getWidth(),(int)size.y);
        ll1.addView(ll_h);

        //選手名を並べるためのタイトルを作成
        TextView make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("選手名");
        params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth())/3,(int)size.y/20);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        //生年月日を並べるためのタイトルを作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("生年月日");
//        params1 = new LinearLayout.LayoutParams((int)size.x/3,(int)size.y/20);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        //メールアドレスを並べるためのタイトルを作成
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setText("email");
//        params1 = new LinearLayout.LayoutParams((int)size.x/6,(int)size.y/20);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        return true;
    }

    //選手の各種情報を表示する
    public void make_playerinfo(){
        String str;

        //枠の作成
        LinearLayout ll_h = new LinearLayout(this);
        ll_h.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth()),(int)size.y/10);
        ll_h.setLayoutParams(params1);
        ll1.addView(ll_h);

        //選手名を表示する
        TextView make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        //選手名が長い場合は8文字目を「...」とする
        if(playerName.get(id_set).length() > 7){
            str = playerName.get(id_set).substring(0,6) + "...";
        }
        else{
            str = playerName.get(id_set);
        }
        make_t.setText(str);
        make_t.setId(id_set);
        make_t.setOnClickListener(this);
        params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth())/3,(int)size.y/10);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
//        make_t.setTypeface(Typeface.createFromAsset(getAssets(), "JiyunoTsubasa.ttf"));
        ll_h.addView(make_t);

        //生年月日を表示する
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        //生年月日が長い場合は8文字目を「...」とする
        if(birth.get(id_set).length() > 7){
            str = birth.get(id_set).substring(0,6) + "...";
        }
        else{
            str = birth.get(id_set);
        }
        make_t.setText(str);
        make_t.setId(id_set);
        make_t.setOnClickListener(this);
//        params1 = new LinearLayout.LayoutParams((int)size.x/3,(int)size.y/10);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
//        make_t.setTypeface(Typeface.createFromAsset(getAssets(), "JiyunoTsubasa.ttf"));
        ll_h.addView(make_t);

        //メールアドレスを表示する
        make_t = new TextView(this);
        make_t.setGravity(Gravity.CENTER);
        make_t.setId(id_set);
        make_t.setOnClickListener(this);
        //メールアドレスが長い場合は8文字目を「...」とする
        if(emails.get(id_set).length() > 7){
            str = emails.get(id_set).substring(0,6) + "...";
        }
        else{
            str = emails.get(id_set);
        }
        make_t.setText(str);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
        ll_h.addView(make_t);

        id_set++;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_orgpage, menu);
        return true;
    }

    //アクションバーのメニューのボタン処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            //ひとつ前のマイページに遷移する
            Intent intent = new Intent(this, Mypage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
            startActivity(warpanyway);
            return true;
        }

        //団体を削除するイベント
        if (id == R.id.menu_delorg) {

            //ネットワークに接続されているか確認する
            ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info =cm.getActiveNetworkInfo();

            //接続されていない場合はsessionIDを破棄してログイン画面に遷移する
            if(info==null||!info.isConnected()) {
                Toast.makeText(this, "ネットワークに接続されていません\nログイン画面に遷移します", Toast.LENGTH_LONG).show();

                spe.putString("sessionID","");
                spe.commit();

                Intent intent = new Intent(this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            else{
                //接続されてたら確認のダイアログをだし、団体削除用のhttpを送信する
                thred = new Httpobject(this,this);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("団体を削除します");
                alertDialogBuilder.setMessage("本当によろしいですか？");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("OK!",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    orgdelinformation(thred.execute(5, Connecthelper.ip + "organization/" + o_id).get());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                alertDialogBuilder.setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });
                alertDialogBuilder.setCancelable(false);
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
            return true;
        }
        //試合一覧へ遷移
        if (id == R.id.menu_matchlist) {
            warpanyway = new Intent(this,Matchlist.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(warpanyway);
        }
        //試合作成に遷移
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
        //遷移団体管理者であるならばメンバー管理に遷移する
        if (id == R.id.menu_manage) {
            if(master) {
                warpanyway = new Intent(this, Manage.class);
                warpanyway.putExtra("o_id",o_id);
                warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(warpanyway);
            }
            else{
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("団体代表者ではありません");
                alertDialogBuilder.setMessage("団体の代表者ではないのでメンバー管理画面へ移動できません");
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
        }

        return super.onOptionsItemSelected(item);
    }

    public void orgdelinformation(JSONObject jsonObject){
        try {
            if(jsonObject != null) {
                //団体削除がうまくいったらマイページに遷移する
                if (jsonObject.getBoolean("results")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("団体の削除が完了しました");
                    alertDialogBuilder.setMessage("マイページに遷移します");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("OK!",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                    alertDialogBuilder.setCancelable(false);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                }
                else{
                    //エラーが発生したらエラー内容を表示する
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("エラーが発生しました");
                    if(!jsonObject.getString("err").equals("null")){
                        alertDialogBuilder.setMessage(jsonObject.getString("err"));
                    }
                    else {
                        alertDialogBuilder.setMessage("エラーが発生しました");
                    }
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("OK!",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                    alertDialogBuilder.setCancelable(false);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button1)
        {
            //画面下のボタンを押された際にはメンバー管理画面に遷移する
            Intent intent = new Intent(this, Manage.class);
            intent.putExtra("o_id",o_id);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
    //バックキーを押した時の動作
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //マイページに遷移する
            Intent i = new Intent(this,Mypage.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
        return false;
    }
}
