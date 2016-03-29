package example.com.argsandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.echo.holographlibrary.Line;
import com.echo.holographlibrary.LineGraph;
import com.echo.holographlibrary.LinePoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;


public class Mypage extends ActionBarActivity implements View.OnClickListener{

    Button belong,made,list,oldscore;
    ImageView listimage,createimage;
    Context context = this;
    TextView name,email,organization,birth,sex;
    TextView old[] = new TextView[11];//過去の得点の入力
    LineGraph graph;
    Line line;

    //過去の得点を表示するためのID
    int[] sumId = {R.id.old_score1, R.id.old_score2, R.id.old_score3, R.id.old_score4, R.id.old_score5,
            R.id.old_score6, R.id.old_score7, R.id.old_score8, R.id.old_score9, R.id.old_score10};

    SharedPreferences sp;
    SharedPreferences.Editor spe = null;

    boolean org_flag = false;

    Intent i;

    Httpobject thred;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mypage);

        sp = context.getSharedPreferences("setting", Context.MODE_MULTI_PROCESS);
        spe = sp.edit();

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

        //TextViewやButtonなどの参照
        belong = (Button)findViewById(R.id.button1);
        made = (Button)findViewById(R.id.button2);
        list = (Button)findViewById(R.id.button3);
        oldscore = (Button)findViewById(R.id.button4);
        name = (TextView)findViewById(R.id.textView3);
        email = (TextView)findViewById(R.id.textView5);
        birth = (TextView)findViewById(R.id.textView21);
        sex = (TextView)findViewById(R.id.textView23);
        organization = (TextView)findViewById(R.id.textView7);
        graph = (LineGraph) findViewById(R.id.graph);

        //クリックリスナーの搭載
        belong.setOnClickListener(this);
        made.setOnClickListener(this);
        list.setOnClickListener(this);
        oldscore.setOnClickListener(this);

        findViewById(R.id.imageView1).setOnClickListener(this);
        findViewById(R.id.imageView2).setOnClickListener(this);

        //intentの設定
        i = new Intent(this,Login.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        //sumとavgを表示するtextviewを配列化する
        for (int i = 1; i <= 10; i++) {
            old[i] = (TextView) findViewById(sumId[i-1]);
        }

        //Http通信を行うためのスレッド
        thred = new Httpobject(this,this);

        //ネットワークに接続されているか確認する
        ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info =cm.getActiveNetworkInfo();

        //接続されていない場合ログイン画面に戻す
        if(info==null||!info.isConnected()) {
            Toast.makeText(this, "ネットワークに接続されていません\nログイン画面に遷移します", Toast.LENGTH_LONG).show();

            spe.putString("sessionID","");
            spe.commit();

            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        else{
            try {
                //グラフなどの更新を行う
                updateAdapter(thred.execute(2, Connecthelper.ip + "personal").get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        //アクションバー関係
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.mypage);

    }

    //アクションバーのメニュー表示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_mypage, menu);
        return true;
    }

    //アクションバーのメニューのボタン処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //ログアウトし、sessionIDを消去してログインページへ遷移する
        if (id == R.id.menu_logout) {
//            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//            alertDialogBuilder.setTitle("ログアウトします");
//            alertDialogBuilder.setMessage("ログイン画面に遷移します");
//            alertDialogBuilder.setCancelable(false);
//            alertDialogBuilder.setPositiveButton("OK!",
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            spe.putString("sessionID", "");
//                            spe.commit();
//
//                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            startActivity(i);
//                        }
//                    });
//            alertDialogBuilder.setCancelable(false);
//            AlertDialog alertDialog = alertDialogBuilder.create();
//            alertDialog.show();
            spe.putString("sessionID", "");
            spe.commit();

            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            return true;
        }

        if (id == R.id.menu_delacc) {

            //ネットワークに接続されているか確認する
            ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info =cm.getActiveNetworkInfo();

            //接続されていない
            if(info==null||!info.isConnected()) {
                Toast.makeText(this, "ネットワークに接続されていません\nログイン画面に遷移します", Toast.LENGTH_LONG).show();

                spe.putString("sessionID", "");
                spe.commit();

                Intent intent = new Intent(this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            else{
                thred = new Httpobject(this,this);

                //アラートダイアログでOKを押されたらアカウントを削除するためにHTTP通信を行う
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("アカウントを削除します");
                alertDialogBuilder.setMessage("本当によろしいですか？");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("OK!",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    accdelinformation(thred.execute(3, Connecthelper.ip + "personal").get());
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
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                alertDialogBuilder.setCancelable(false);
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
            return true;
        }
        //タップされたレイアウトに応じて画面遷移を行う
        Intent warpanyway;
        //団体ページに遷移
        if (id == R.id.menu_orgpage) {
            warpanyway = new Intent(this,Orgpage.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(warpanyway);
        }
        //試合一覧に遷移
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button1){
            //団体に所属している場合団体ページに飛び、所属してなければ団体作成に飛ぶ
            if(org_flag) {
                Intent intent = new Intent(this, Orgpage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("flag", false);
                startActivity(intent);
                finish();
            }
            else{
                Intent intent = new Intent(this, Createorg.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("flag", false);
                startActivity(intent);
                finish();
            }
        }
        //試合作成画面に遷移する
        if(v.getId() == R.id.button2 || v.getId() == R.id.imageView2){
            Intent intent = new Intent(this,Createmat.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("flag", false);
            startActivity(intent);
            finish();
        }
        //試合一覧に遷移する
        if(v.getId() == R.id.button3 || v.getId() == R.id.imageView1){
            Intent intent = new Intent(this,Matchlist.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        //過去の得点表一覧に遷移する
        if(v.getId() == R.id.button4){
            Intent intent = new Intent(this,Oldscorelist.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    //グラフの「点」を作成する
    public void addgraph(int sum,int count)
    {
        LinePoint p = new LinePoint();
        //x座標は点と点の幅がずれないように徐々に大きくしていく
        p.setX(9 - count * 2);
        p.setY(sum);
        //lineに点を追加する
        line.addPoint(p);
    }

    public void updateAdapter(JSONObject jsonObject) {
        try {
            int sexs;
            if(jsonObject != null) {
                //ユーザーデータの表示
                name.setText(jsonObject.getString("playerName").toString());
                email.setText(jsonObject.getString("email").toString());
                birth.setText(jsonObject.getString("birth").toString());
                //性別は数字で送られてくるのでその値を文字に変える
                sexs =jsonObject.getInt("sex");
                if(sexs == 0){
                    sex.setText("男");
                }
                else if(sexs == 1){
                    sex.setText("女");
                }else{
                    sex.setText("その他");
                }
                //団体に所属していた場合は表示するがしてない場合は未所属と表示する
                if (!jsonObject.getString("organizationName").toString().equals("null")) {
                    organization.setText(jsonObject.getString("organizationName").toString());
                    org_flag = true;
                    belong.setText(" 団体ページ ");
                } else {
                    organization.setText("無所属");
                }

                //点数をarrayとして扱うように代入する
                JSONArray jsonArray = (JSONArray) jsonObject.getJSONArray("record");

                int i = 0;
                double avg = 0;
                line = new Line();
                line.setColor(Color.parseColor("#9acd32"));
                //graphの最大Y座標を1000に設定
                graph.setRangeY(0, 1000);
                graph.setLineToFill(0);
                while (jsonArray.length() > i) {
                    JSONObject record = jsonArray.getJSONObject(i);
                    //得点表示用textview配列に試合日+試合名を表示する
                    if (record.getString("matchName").toString().length() > 9) {
                        old[i + 1].setText(record.getString("created").toString() + "\t" + record.getString("matchName").toString().substring(0, 7) + "...");
                    } else {
                        old[i + 1].setText(record.getString("created").toString() + "\t" + record.getString("matchName").toString());
                    }

                    //小数点以下の数字を
                    if(Integer.parseInt(record.getString("perEnd").toString()) != 0) {
                        avg = (double)Integer.parseInt(record.getString("sum").toString()) / Integer.parseInt(record.getString("perEnd").toString()) / 6;
                        BigDecimal big = new BigDecimal(String.valueOf(avg));
                        avg = big.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    }
                    else{
                        avg = 0;
                    }
                    //avg*100の値を点のY座標とする
                    addgraph((int)avg*100, i);

                    //得点表示用textview配列にsumとavgを表示する
                    old[i + 6].setText("sum (" + record.getString("sum").toString() + ")  /  avg (" + avg + ")");

                    i++;
                }
                if (i >= 2) {
                    //点が2個以上だった場合線を引く
                    graph.addLine(line);
                }
            }
        } catch (JSONException e) {
            Toast.makeText(this, "セッションが切れています\nログイン画面に遷移します", Toast.LENGTH_LONG).show();

            spe.putString("sessionID","");
            spe.commit();

            try {
                Connecthelper.socketset();
            } catch (URISyntaxException j) {
                j.printStackTrace();
            }

            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    public void accdelinformation(JSONObject jsonObject){
        try {
            if(jsonObject != null) {
                //アカウント削除に成功した場合
                if (jsonObject.getBoolean("results")) {
                    spe.putString("sessionID", "");
                    spe.commit();

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("アカウントの削除が完了しました");
                    alertDialogBuilder.setMessage("ログイン画面に遷移します");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("OK!", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //ログイン画面に遷移
                                    startActivity(i);
                                    finish();
                                }
                            });
                    alertDialogBuilder.setCancelable(false);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
                else{
                    //うまくいかなかった場合エラー内容が帰ってきていたらそれをアラートダイアログで表示する
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
    }

    //戻るボタンを押された場合
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction()==KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    //アプリを終了する
                    moveTaskToBack(true);
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
