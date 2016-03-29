package example.com.argsandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.client.methods.HttpOptions;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;


public class Createorg extends ActionBarActivity implements View.OnClickListener{

    EditText org,place,email;

    SharedPreferences sp;
    SharedPreferences.Editor spe = null;
    Context context = this;
    Httpobject thred;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createorg);

        //SaredPrefrenceの初期化
        sp = context.getSharedPreferences("setting", Context.MODE_MULTI_PROCESS);
        spe = sp.edit();

        //アクションバー関連
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.creategro);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //buttonへのクリックベント付与
        Button bt1;
        bt1 = (Button)findViewById(R.id.button1);
        bt1.setOnClickListener(this);

        //edittextの初期化
        org = (EditText)findViewById(R.id.editText1);
        place = (EditText)findViewById(R.id.editText2);
        email = (EditText)findViewById(R.id.editText3);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_creatorg, menu);
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
    public void onClick(View v) {
        if(v.getId() == R.id.button1)
        {
            //必要な情報が入力されていたら
            if(!org.getText().toString().equals("") && !place.getText().toString().equals("")) {
                //ネットワークに接続されているか確認する
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = cm.getActiveNetworkInfo();

                //接続されていなかったらsessionIDを破棄してLogin画面へ遷移
                if (info == null || !info.isConnected()) {
                    Toast.makeText(this, "ネットワークに接続されていません\nログイン画面に遷移します", Toast.LENGTH_LONG).show();

                    spe.putString("sessionID", "");
                    spe.commit();

                    Intent intent = new Intent(this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else {
                    //情報がすべて入っていたら団体削除ようのhttp通信を行う
                    thred = new Httpobject(this, this);
                    try {
                        createorginformation(thred.execute(6, Connecthelper.ip + "organization",org.getText().toString(),
                                place.getText().toString(),email.getText().toString()).get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                Toast.makeText(this,"必要な情報をすべて入力してください",Toast.LENGTH_LONG).show();
            }
        }
    }
    public void createorginformation(JSONObject jsonObject){
        if(jsonObject != null) {
            try {
                if(jsonObject != null) {
                    //団体作成がうまくいったら団体ページに遷移する
                    if (jsonObject.getBoolean("results")) {
                        Intent intent = new Intent(this, Orgpage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    //団体作成に失敗したらエラー内容を表示する
                    if(!jsonObject.getBoolean("results")) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.Theme_AppCompat_Light_Dialog_Alert));
                        alertDialogBuilder.setTitle("エラーが発生しました");

                        if(!jsonObject.getString("err").toString().equals("null")) {
                            alertDialogBuilder.setMessage("エラー内容\n\n" + jsonObject.getString("err").toString());
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
