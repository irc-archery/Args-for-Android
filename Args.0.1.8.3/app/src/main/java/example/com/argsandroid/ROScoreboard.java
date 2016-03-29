package example.com.argsandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by soft on 2015/06/07.
 */
public class ROScoreboard extends ActionBarActivity {


    //通信関係
    private com.github.nkzawa.socketio.client.Socket socket;
    boolean first_flag = true;

    SharedPreferences sp;
    SharedPreferences.Editor spe;

    ArrayList<String> point = new ArrayList<String>();
    String setpoint[][] = new String[2][8];

    //id管理配列
    String[] length_str = {"90m","70m","60m","50m","40m","30m","70m前","70m後","18m"};
    int[] sumId = {R.id.sumbutton1, R.id.sumbutton2, R.id.sumbutton3, R.id.sumbutton4, R.id.sumbutton5, R.id.sumbutton6, R.id.sumbutton7, R.id.sumbutton8, R.id.sumbutton9};
    int[] resId = {R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9, R.id.button10, R.id.button11, R.id.button12, R.id.button13, R.id.button14, R.id.button15, R.id.button16};
    int[] ediId = {R.id.editText1, R.id.editText2, R.id.editText3, R.id.editText4, R.id.editText5, R.id.editText6};
    String[] pos = {"M", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "X"};

    //ビューのための変数
    LinearLayout box;
    TextView sett,lengtht;
    EditText et;
    Button[] sumb = new Button[9];
    EditText[] edie = new EditText[6];

    int id = 0;//スコアボード本体のid
    int sid = 0;//各ボタンのid

    //値保持のための変数
    String length;

    //動的グループビュー作成のための変数.スコア
    LayoutInflater inflater;
    View itemView;

    Httpobject thred;
    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.roscoreboard);
        box = (LinearLayout) findViewById(R.id.Box);

        //sharedprefarenceの設定
        sp = getSharedPreferences("setting", Context.MODE_MULTI_PROCESS);
        spe = sp.edit();

        //各レイアウト参照
        for(int i = 0;i < 9;i++)
        {
            sumb[i] = (Button)findViewById(sumId[i]);
            if(i < 6){
                edie[i] = (EditText)findViewById(ediId[i]);
            }
        }
        Log.e("Create", "Create");

        //アクションバー関連
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.roscoreboard);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //intent初期化
        i = new Intent(this,Oldscorelist.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Intent intent = getIntent();

        //httpObject初期化
        thred = new Httpobject(this,this);
        try {
            //得点表情報を取得する
            roinformation(thred.execute(11, Connecthelper.ip + "personal/record/"+intent.getIntExtra("sc_id",1)).get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void roinformation(JSONObject jsonObject){
        try {
            //edittextに受け取ったデータを表示する
            edie[0].setText(jsonObject.getString("matchName"));
            edie[1].setText(jsonObject.getString("created"));
            if(jsonObject.has("number")){
                if(jsonObject.getString("number").toString().equals("null")) {
                    edie[2].setText("");
                }
                else {
                    edie[2].setText(jsonObject.getString("number").toString());
                }
            }
            else{
                edie[2].setText("");
            }
            edie[3].setText(jsonObject.getString("playerName"));
            if(jsonObject.has("organizationName")){
                if(jsonObject.getString("organizationName").toString().equals("null")) {
                    edie[4].setText("");
                }
                else {
                    edie[4].setText(jsonObject.getString("organizationName").toString());
                }
            }
            else{
                edie[4].setText("");
            }
            if(jsonObject.has("prefectures")){
                if(jsonObject.getString("prefectures").toString().equals("null")) {
                    edie[5].setText("");
                }
                else {
                    edie[5].setText(jsonObject.getString("prefectures").toString());
                }
            }
            else{
                edie[5].setText("");
            }
            //画面上部に追加されるものを追加する
            length = (length_str[Integer.parseInt(jsonObject.getString("length").toString())]);//length(的までの距離)反映
            sumb[6].setText(jsonObject.getString("total"));
            sumb[7].setText(jsonObject.getString("ten"));
            sumb[8].setText(jsonObject.getString("x"));

            int i=0,coo=0;
            JSONArray scoreArray = jsonObject.getJSONArray("score");
            while(scoreArray.length() > i){
                JSONObject scoreObject = scoreArray.getJSONObject(i);

                setpoint[0][0] = "";
                setpoint[1][0] = "";
                setpoint[1][7] = "";
                setpoint[0][7] = scoreObject.getString("subTotal");
                sumb[i].setText(setpoint[0][7]);
                for (int j = 1; j <= 6; j++) {
                    setpoint[0][j] = scoreObject.getString("score_" + j).toString();

                    if (scoreObject.getString("updatedScore_" + j).toString() != "null") {
                        setpoint[1][j] = scoreObject.getString("updatedScore_" + j).toString();
                    } else {
                        setpoint[1][j] = "";
                    }
                }
                for (int j = 0; j <= 1; j++) {
                    for (int k = 0; k <= 7; k++) {
                        //arraylistに得点を追加する
                        point.add(setpoint[j][k]);
                        coo++;
                    }
                }
                //得点表の表示
                scoreboard();
                i++;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //得点表の削除
    public void deletescorecard(JSONObject jsonObject){
        try {
            if(jsonObject != null) {
                //曳航した場合は過去の得点表一覧に遷移
                if (jsonObject.getBoolean("results")) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("得点表の削除が完了しました");
                    alertDialogBuilder.setMessage("過去の得点表一覧に遷移します");
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
                    //エラーが起こった際は何もしないようにする
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

    //アクションバーのメニュー表示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_roscoreboard, menu);
        return true;
    }

    //アクションバーのメニューのボタン処理
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, Oldscorelist.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        int ids = item.getItemId();
        Intent warpanyway;

        //ログアウトし、sessionIDを消去してログインページへ遷移する
        if (ids == R.id.menu_logout) {

            spe.putString("sessionID", "");
            spe.commit();

            warpanyway = new Intent(this,Login.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(warpanyway);
            return true;
        }
        //試合一覧へ遷移
        if (ids == R.id.menu_matchlist) {
            warpanyway = new Intent(this,Matchlist.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(warpanyway);
        }
        //試合作成へ遷移
        if (ids == R.id.menu_createmat) {
            warpanyway = new Intent(this,Createmat.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(warpanyway);
        }
        //マイページに遷移
        if (ids == R.id.menu_mypage) {
            warpanyway = new Intent(this,Mypage.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(warpanyway);
        }
        //団体ページに遷移
        if (ids == R.id.menu_orgpage) {
            warpanyway = new Intent(this,Orgpage.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(warpanyway);
        }
        //過去の得点表一覧ページに遷移
        if (ids == R.id.menu_oldscorelist) {
            warpanyway = new Intent(this,Oldscorelist.class);
            warpanyway.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(warpanyway);
        }
        //得点表の削除
        if (ids == R.id.menu_delsco) {

            thred = new Httpobject(this,this);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("得点表を削除します");
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
                            Intent intent = getIntent();
                            try {
                                deletescorecard(thred.execute(12, Connecthelper.ip + "personal/record/" + intent.getIntExtra("sc_id", 1)).get());
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

        return true;
    }
    //スコアボードの動的生成
    public void scoreboard() {
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        itemView = inflater.inflate(R.layout.score, null);
        itemView.setId(id);

        //得点表の各除法の表示
        sett = (TextView) itemView.findViewById(R.id.textView1);
        sett.setText((id + 1) + "回目");

        lengtht = (TextView) itemView.findViewById(R.id.lengtht);

        lengtht.setText(length);

        if(id != 0 && length != null)
        {
            lengtht.setText(length + "m");
        }

        int scoresum = 0;

        for (int i = 0; i < 16; i++) {
            //各ボタンにidの振り直し、クリックイベントの付加
            Button mbt = (Button) itemView.findViewById(resId[i]);
            mbt.setId(sid);

            //得点の表示
            mbt.setText(point.get(sid));

            //更新された得点だった場合は色を変える
            if (sid % 16 >= 9 && sid % 16 != 15 && !point.get(sid).toString().equals("")) {
                mbt.setBackgroundDrawable(getResources().getDrawable(R.drawable.frame_r));
                mbt = (Button) itemView.findViewById(sid - 8);
                mbt.setBackgroundDrawable(getResources().getDrawable(R.drawable.frame_r));
            }
            //mbt.setHint(sid+"");
            sid++;
        }

        id++;//セットのidカウント

        //レイアウトがするので少し間隔入れる
        itemView.setPadding(0,50,0,0);

        box.addView(itemView,0);
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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(i);
        }
        return false;
    }


}
