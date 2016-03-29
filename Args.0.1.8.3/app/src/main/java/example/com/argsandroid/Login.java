package example.com.argsandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ContextThemeWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;


//mode 0 = login
//mode 1 = newaccount
//mode 2 =

public class Login extends ActionBarActivity implements View.OnClickListener{
    Button bt2;
    EditText email,pass;
    Boolean input[] = new Boolean[2];
    int count = 0;
    Context context = this;

    SharedPreferences sp = null;
    SharedPreferences.Editor spe = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = getSharedPreferences("setting", MODE_MULTI_PROCESS);
        spe = sp.edit();

        if(!sp.getString("sessionID","").equals("")){
            Intent intent = new Intent(this,Mypage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.login);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.login);

        /**********************************/
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        TextView vertext = (TextView)findViewById(R.id.ver);
        vertext.setText("ver." + packageInfo.versionName.toString());
        /**********************************/

        bt2 = (Button)findViewById(R.id.button2);
        bt2.setOnClickListener(this);

        email = (EditText)findViewById(R.id.editText1);
        pass = (EditText)findViewById(R.id.editText2);

        input[0] = input[1] = false;
    }

    @Override
     public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //アカウント作成画面へ遷移
        if (id == R.id.menu_createacc) {
            Intent intent = new Intent(this, Createacc.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        try {
            if(v.getId() == R.id.button2)
            {
                //内容がすべて入力されていたらHTTP通信を行う
                if(!pass.getText().toString().equals("") && !email.getText().toString().equals("")) {
                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo info = cm.getActiveNetworkInfo();

                    //接続されていない
                    if (info == null || !info.isConnected()) {
                        Toast.makeText(this,"ネットワークに接続されていません！\n接続してから再度ボタンを押してください",Toast.LENGTH_LONG).show();
                    }
                    else {
                        Httpobject thred = new Httpobject(this, this);
                        logininformation(thred.execute(0, Connecthelper.ip + "login", email.getText(), pass.getText()).get());
                    }
                }
                else{
                    Toast.makeText(this, "すべての内容を入力してください", Toast.LENGTH_LONG).show();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    public void logininformation(JSONObject jsonObject){
        try {
            if(jsonObject != null) {
                if (jsonObject.getBoolean("results")) {
                    Intent intent = new Intent(this, Mypage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                if(!jsonObject.getBoolean("results")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context,R.style.Theme_AppCompat_Light_Dialog_Alert));
                    alertDialogBuilder.setTitle("ログインしなおして下さい");

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
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction()==KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    moveTaskToBack(true);
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
