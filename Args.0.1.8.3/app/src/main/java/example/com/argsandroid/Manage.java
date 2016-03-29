package example.com.argsandroid;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.view.ContextThemeWrapper;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class Manage extends ActionBarActivity implements View.OnClickListener{

    Context context = this;
    SharedPreferences sp;
    SharedPreferences.Editor spe = null;

    TextView orgname,members;
    TextView make_t;
    LinearLayout ll1,ll_h;
    EditText emails,passs;

    ArrayList<Integer> p_id;//選手のid
    ArrayList<String> birth;//生年月日
    ArrayList<String> playerName;//選手名
    ArrayList<String> email;//Eメール
    int id_set = 0;
    int o_id;
    int ID = 0;
    Point size;

    Dialog dialog;

    int marginesize = 0;
    boolean first_flag = true;

    Intent i;
    Intent intent;

    Httpobject thred = new Httpobject(this,this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage);

        //SaredPrefrenceの初期化
        sp = context.getSharedPreferences("setting", Context.MODE_MULTI_PROCESS);
        spe = sp.edit();

        marginesize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics());

        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        // ディスプレイのインスタンス生成
        Display disp = wm.getDefaultDisplay();
        size = new Point();
        disp.getSize(size);

        //textview等の初期化
        orgname = (TextView)findViewById(R.id.textView1);
        members = (TextView)findViewById(R.id.textView3);
        ll1 = (LinearLayout)findViewById(R.id.ll1);

        //ArrayListの初期化
        p_id = new ArrayList<Integer>();
        birth = new ArrayList<String>();
        playerName = new ArrayList<String>();
        email = new ArrayList<String>();

        //Edittextの初期化
        emails = (EditText)findViewById(R.id.editText1);
        passs = (EditText)findViewById(R.id.editText2);

        findViewById(R.id.button1).setOnClickListener(this);

        //intentの初期化
        i = new Intent(this,Mypage.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        //前Activityからo_idを受け取る
        Intent get_i = getIntent();
        o_id = get_i.getIntExtra("o_id",1);

        //intentの初期化
        intent = new Intent(this, Manage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        //アクションバー関連
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.manage);
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
        try {
            if(first_flag) {
                first_flag = false;
                make_first();

                thred = new Httpobject(this, this);
                manageinformation(thred.execute(7, Connecthelper.ip + "organization/members").get());
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
        params1 = new LinearLayout.LayoutParams((int)ll1.getWidth(),(int)size.y);
        ll1.addView(ll_h);

        //選手名を並べるためのタイトルを作成
        make_t = new TextView(this);
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

    public void manageinformation(JSONObject jsonObject){
        try {
            //管理情報を取得し画面に反映する
            //master = jsonObject.getBoolean("permission");
            JSONArray jsonArray = jsonObject.getJSONArray("memberList");
            int i=0;
            while(jsonArray.length() > i){
                JSONObject memberobject = jsonArray.getJSONObject(i);
                p_id.add(Integer.parseInt(memberobject.getString("p_id")));
                playerName.add(memberobject.getString("playerName"));
                birth.add(memberobject.getString("birth"));
                email.add(memberobject.getString("email"));
                make_room();
                i++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void make_room(){
        String str;

        //枠の作成
        ll_h = new LinearLayout(this);
        ll_h.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams((int)(ll1.getWidth()),(int)size.y/10);
        ll_h.setLayoutParams(params1);

        //選手名を表示する
        make_t = new TextView(this);
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
        //主催が長い場合は8文字目を「...」とする
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
        if(email.get(id_set).length() > 8){
            str = email.get(id_set).substring(0,7) + "...";
        }
        else{
            str = email.get(id_set);
        }
        make_t.setText(str);
//        params1 = new LinearLayout.LayoutParams((int)size.x/6,(int)size.y/10);
        make_t.setLayoutParams(params1);
        make_t.setBackgroundResource(R.drawable.frame);
//        make_t.setTypeface(Typeface.createFromAsset(getAssets(), "JiyunoTsubasa.ttf"));
        ll_h.addView(make_t);

        id_set++;

        //レイアウトに反映する
        ll1.addView(ll_h);
    }

    //アクションバーのメニュー表示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manage, menu);
        return true;
    }


    //アクションバーのメニューのボタン処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            //ひとつ前のページに遷移する
            Intent intent = new Intent(this, Orgpage.class);
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
        //団体ページに遷移
        if (id == R.id.menu_orgpage) {
            warpanyway = new Intent(this, Orgpage.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(warpanyway);
        }

        return super.onOptionsItemSelected(item);
    }

    //バックキーを押した時の動作
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //団体ページに遷移する
            Intent i = new Intent(this, Orgpage.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        //dialoの設定をしてから表示する
        if(v.getId() == R.id.button1){
            try {
                if(!passs.getText().toString().equals("") && !emails.getText().toString().equals("")) {
                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo info = cm.getActiveNetworkInfo();

                    //接続されていない
                    if (info == null || !info.isConnected()) {
                        Toast.makeText(this, "ネットワークに接続されていません\nログイン画面に遷移します", Toast.LENGTH_LONG).show();

                        spe.putString("sessionID", "");
                        spe.commit();

                        Intent intent = new Intent(this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Httpobject thred = new Httpobject(this, this);
                        addinformation(thred.execute(8, Connecthelper.ip + "organization/members", emails.getText(), passs.getText()).get());

                    }
                }
                else{
                    Toast.makeText(this, "すべての情報を入力してください", Toast.LENGTH_LONG).show();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        else {
            thred = new Httpobject(this,this);

            ID = v.getId();

            //dialogの設定をしてから表示する
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context,R.style.Theme_AppCompat_Light_Dialog_Alert));
            alertDialogBuilder.setTitle("選手を団体から削除します");
            alertDialogBuilder.setMessage("試合開始日　:　" + playerName.get(ID) +
                    "\n生年月日　:　" + birth.get(ID) +
                    "\ne-mail　:　" + email.get(ID) +
                    "\n\nこの選手を団体から削除してよろしいですか？");
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
                            try {
                            //okを押されたら削除用のhttpも送信する
                                memberdelinformation(thred.execute(9, Connecthelper.ip + "organization/members/" + p_id.get(ID)).get());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            alertDialogBuilder.setCancelable(false);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    public void memberdelinformation(JSONObject jsonObject){
        try {
            if(jsonObject != null) {
                //アカウント削除に成功したらマイページに遷移するようにする
                if (jsonObject.getBoolean("results")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("");

                    alertDialogBuilder.setMessage("アカウントを削除しました");

                    alertDialogBuilder.setPositiveButton("OK!",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(intent);
                                }
                            });
                    alertDialogBuilder.setCancelable(false);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
                //失敗したらエラー内容を出力する
                if(!jsonObject.getBoolean("results")) {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("エラーが発生しました");

                    if(!jsonObject.getString("err").toString().equals("null")) {
                        alertDialogBuilder.setMessage(jsonObject.getString("err").toString());
                    }
                    else{
                        alertDialogBuilder.setMessage("エラーが発生しました");
                    }
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addinformation(JSONObject jsonObject){
        try {
            if(jsonObject != null) {
                if (jsonObject.getBoolean("results")) {
                    Intent intent = new Intent(this, Manage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                if(!jsonObject.getBoolean("results")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("エラーが発生しました");

                    if(!jsonObject.getString("err").toString().equals("null")) {
                        alertDialogBuilder.setMessage(jsonObject.getString("err").toString());
                    }
                    else{
                        alertDialogBuilder.setMessage("原因不明のエラーが起こりました\n再度ログインしてください");
                    }
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*public void orgdelinformation(JSONObject jsonObject){
        try {
            if(jsonObject != null) {
                if (jsonObject.getBoolean("results")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder.setTitle("団体の削除が完了しました");
                    alertDialogBuilder.setMessage("マイページに遷移します");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("OK!",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(i);
                                    finish();
                                }
                            });
                    alertDialogBuilder.setCancelable(false);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                }
                else{
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
                                    startActivity(i);
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
    }*/

    //dialogで拒否された場合のクリックリスナー
    public View.OnClickListener no = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //dialoigを削除]\
            dialog.dismiss();
        }
    };
}
