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
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;


public class Createacc extends ActionBarActivity implements View.OnClickListener{

    EditText surname,name,skana,nkana,email,pass,passs;
    Spinner year,month,day;
    RadioButton man,woman,other;
    Button signup;
    Boolean input[] = new Boolean[7];
    int count = 0;
    int sex_judge = 0;
    Context context = this;

    SharedPreferences sp = null;
    SharedPreferences.Editor spe = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createacc);

        sp = getSharedPreferences("setting", MODE_MULTI_PROCESS);
        spe = sp.edit();

        //アクションバー関係
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.createacc);
        actionBar.setDisplayHomeAsUpEnabled(true);

        for(int i = 0;i < 7;i++)
        {
            //データがすべてに入っているか確認する
            input[i] = false;
        }

        surname = (EditText)findViewById(R.id.editText11);
        name = (EditText)findViewById(R.id.editText12);
        skana = (EditText)findViewById(R.id.editText13);
        nkana = (EditText)findViewById(R.id.editText14);
        email = (EditText)findViewById(R.id.editText15);
        pass = (EditText)findViewById(R.id.editText16);
        passs = (EditText)findViewById(R.id.editText17);
        year = (Spinner)findViewById(R.id.spinner1);
        month = (Spinner)findViewById(R.id.spinner2);
        day = (Spinner)findViewById(R.id.spinner3);
        man = (RadioButton)findViewById(R.id.radio0);
        woman = (RadioButton)findViewById(R.id.radio1);
        other = (RadioButton)findViewById(R.id.radio2);
        signup = (Button)findViewById(R.id.button1);

        signup.setOnClickListener(this);

        surname.setNextFocusDownId(R.id.editText12);
        name.setNextFocusDownId(R.id.editText13);
        skana.setNextFocusDownId(R.id.editText14);
        nkana.setNextFocusDownId(R.id.editText15);
        email.setNextFocusDownId(R.id.editText16);
        pass.setNextFocusDownId(R.id.editText17);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    //アクションバーのメニューのボタン処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        //アカウント作成画面へ遷移
        if (item.getItemId() == R.id.menu_login) {
            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button1)
        {
            //姓名
            if(!surname.getText().toString().equals("") && !name.getText().toString().equals(""))
            {
                Log.v("aaa", "name:" + surname.getText().toString() + name.getText().toString());
                input[0] = true;
            }
            //姓名(仮名)
            if(!skana.getText().toString().equals("") && !nkana.getText().toString().equals(""))
            {
                Log.v("aaa", "kana:" + skana.getText().toString() + nkana.getText().toString());
                input[1] = true;
            }
            //メールアドレス
            if(!email.getText().toString().equals(""))
            {
                Log.v("aaa", "email:" + email.getText().toString());
                input[2] = true;
            }
            //パスワード
            if(!pass.getText().toString().equals(""))
            {
                Log.v("aaa", "pass:" + pass.getText().toString());
                input[3] = true;
            }
            //パスワードの確認
            if(pass.getText().toString().equals(passs.getText().toString()))
            {
                Log.v("aaa", "passs:" + passs.getText().toString());
                input[4] = true;
            }
            //年月日
            if(!year.getSelectedItem().equals("----")  && !month.getSelectedItem().equals("--") && !day.getSelectedItem().equals("--"))
            {
                Log.v("aaa", "yaer:" + year.getSelectedItem());
                Log.v("aaa", "month:" + month.getSelectedItem());
                Log.v("aaa", "day:" + day.getSelectedItem());
                input[5] = true;
            }
            //性別
            if(man.isChecked() == true  || woman.isChecked() == true || other.isChecked() == true)
            {
                if(man.isChecked() == true)
                {
                    Log.v("aaa", "sex:" + man.getText());
                    sex_judge = 0;
                }
                else if(woman.isChecked() == true)
                {
                    Log.v("aaa", "sex:" + woman.getText());
                    sex_judge = 1;
                }
                else
                {
                    Log.v("aaa", "sex:" + other.getText());
                    sex_judge = 8;
                }
                input[6] = true;
            }

            if(!input[4]){
                Toast.makeText(this,"パスワードが間違っています",Toast.LENGTH_LONG).show();
            }
            else {
                //すべてのデータが入力されているか確認
                for (int i = 0; i < 7; i++) {
                    if (input[i] == true) {
                        count++;
                        input[i] = false;
                    }
                }

                if (count == 7) {
                    Httpobject thred = new Httpobject(this, this);

                    try {
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
                            createccinformation(thred.execute(1, Connecthelper.ip + "createAccount", surname.getText(), name.getText(),
                                    skana.getText(), nkana.getText(), email.getText(), pass.getText(),
                                    year.getSelectedItem().toString() + "-" + month.getSelectedItem().toString() + "-" + day.getSelectedItem().toString(), sex_judge + "").get());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this,"必要な情報をすべて入力してください",Toast.LENGTH_LONG).show();
                }
            }
            count = 0;
        }
    }


    public void createccinformation(JSONObject jsonObject){
        try {
            if(jsonObject != null) {
                if (jsonObject.getBoolean("results")) {
                    Intent intent = new Intent(this, Mypage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                if(!jsonObject.getBoolean("results")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.Theme_AppCompat_Light_Dialog_Alert));
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
}
