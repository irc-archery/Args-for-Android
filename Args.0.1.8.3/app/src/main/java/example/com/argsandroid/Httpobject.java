
package example.com.argsandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ContextThemeWrapper;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Httpobject extends AsyncTask<Object, Void, JSONObject> {

    //modme

    private Activity main;

    private Context context;
    private Mypage mypage;

    public  ProgressDialog m_ProgressDialog;

    public  AlertDialog m_AlertDialog;

    public boolean flg_0 = true;

    String[] str;

    int mode;

    SharedPreferences sp;
    SharedPreferences.Editor spe = null;

    HttpCookie cookie;

    JSONObject jsonObject;

    public Httpobject(Activity activity, Context context) {
        super();
        main = activity;
        this.context=context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        sp = context.getSharedPreferences("setting", Context.MODE_MULTI_PROCESS);
        spe = sp.edit();
        return;
    }

    @Override
    protected JSONObject doInBackground(Object... params) {
        // TODO Auto-generated method stub
        //modeでイベントの使い分けを行う
        //0 = ログイン
        //1 = アカウント作成
        //2 = マイページ情報取得
        //3 = アカウント削除
        //4 = 団体ページ情報取得
        //5 = 団体削除
        //6 = 団体作成
        //7 = メンバー一覧情報取得
        //8 = メンバー追加
        //9 = メンバー削除
        //10 = 得点表一覧取得
        //11 = 得点表取得
        //12 = 得点表削除
        //13 = 団体削除権限確認
        mode = (Integer.parseInt(params[0].toString()));//modeの受け取り
        str = new String[10];
        try {
            str[1] = params[1].toString();//接続先
            //postを行うものは送る用のデータを受け取る
            if (mode == 0 || mode == 1 || mode == 6 || mode == 8) {
                str[2] = params[2].toString();
                str[3] = params[3].toString();
                if (mode == 1 || mode == 6) {
                    str[4] = params[4].toString();
                    if (mode == 1) {
                        str[5] = params[5].toString();
                        str[6] = params[6].toString();
                        str[7] = params[7].toString();
                        str[8] = params[8].toString();
                        str[9] = params[9].toString();
                    }
                }
            }

            //接続先を指定する
            CookieManager man = new CookieManager();
            CookieHandler.setDefault(man);
            URL url = new URL(str[1]);
            HttpURLConnection client = (HttpURLConnection) url.openConnection();
            client.setDoInput(true);

            //POSTとGET、DELETEの切り替えを行う
            if (mode == 2 || mode == 4 || mode == 7 || mode == 10 || mode == 11|| mode == 13) {
                client.setRequestMethod("GET");
            } else if (mode == 3 || mode == 5 || mode == 9 || mode == 12) {
                client.setRequestMethod("DELETE");
            } else {
                client.setRequestMethod("POST");
                client.setDoOutput(true);
            }
            client.setChunkedStreamingMode(0);
            client.setUseCaches(false);

            //sessionIDをログインとアカウント作成以外は付与
            if (mode >= 2) {
                client.setRequestProperty("cookie", sp.getString("sessionID", ""));
            }

            //接続確立
            client.connect();
            if (mode == 0 || mode == 1 || mode == 6 || mode == 8) {
                String postDataSample = null;
                //POSTする場合はデータを付与する
                if (mode == 0 || mode == 8) {
                    postDataSample = "email=" + str[2] + "&password=" + str[3];
                }
                if (mode == 1) {
                    postDataSample = "lastName=" + str[2] + "&firstName=" + str[3] + "&rubyLastName=" + str[4] + "&=rubyFirstName" + str[5]
                            + "&email=" + str[6] + "&password=" + str[7] + "&birth=" + str[8] + "&sex=" + str[9];
                }
                if (mode == 6) {
                    postDataSample = "organizationName=" + str[2] + "&place=" + str[3] + "&email=" + str[4];
                }
                PrintStream ps = new PrintStream(client.getOutputStream());
                //postの場合ここでデータを投げる
                ps.print(postDataSample);
                ps.close();
            }

            //受け取ったデータをJSONObjectに変更
            BufferedInputStream in = new BufferedInputStream(client.getInputStream());
            ByteArrayOutputStream responseArray = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int length;
            while ((length = in.read(buff)) != -1) {
                if (length > 0) {
                    responseArray.write(buff, 0, length);
                }
            }
            StringBuilder viewStrBuilder = new StringBuilder();
            jsonObject = new JSONObject(new String(responseArray.toByteArray()));

            //ログインとアカウント作成の場合sessionIDを保存する
            if (mode == 0 || mode == 1) {
                CookieStore store = man.getCookieStore();
                List<HttpCookie> clist = store.getCookies();
                if (clist.size() != 0) {
                    cookie = clist.get(0);
                    spe.putString("sessionID", cookie.toString());
                    spe.commit();
                }
            }
//            Log.e("", "Cookie[ name value " + cookie.getName() + " " + cookie.getValue());
//            Log.e("", cookie.toString());

        }catch(Exception e){
            Log.e("","error    "+e);
            return null;
        }
        return jsonObject;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        try {

            //上でtrycatchに入ったらアラートダイアログを表示する
            if(result == null){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context,R.style.Theme_AppCompat_Light_Dialog_Alert));
                alertDialogBuilder.setTitle("ERROR!");

                alertDialogBuilder.setMessage("エラーが発生しました");

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
