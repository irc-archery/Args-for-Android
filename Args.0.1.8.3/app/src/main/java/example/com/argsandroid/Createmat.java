package example.com.argsandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class Createmat extends ActionBarActivity implements OnClickListener {

	Button btn1;

	EditText edt1;
	EditText edt2;
	EditText edt3;
	EditText edt4;

	Spinner spn1;

	RadioButton rdb1;
	RadioButton rdb2;

	LinearLayout ll1;

	Intent intent;

	SharedPreferences sp = null;
	SharedPreferences.Editor spe = null;

	Context context = this;

	boolean connect_flag = false;

	TextView tvset;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.createmat);

		//button等の初期化
		btn1 = (Button)findViewById(R.id.button1);

		edt1 = (EditText)findViewById(R.id.editText1);
		edt2 = (EditText)findViewById(R.id.editText2);
//		edt3 = (EditText)findViewById(R.id.editText3);
//		edt4 = (EditText)findViewById(R.id.editText4);

		rdb1 = (RadioButton)findViewById(R.id.rdb1);
		rdb2 = (RadioButton)findViewById(R.id.rdb2);

		//クリックイベントの付与
		btn1.setOnClickListener(this);

		//sharedprefarenceの初期化
		sp = getSharedPreferences("setting", MODE_MULTI_PROCESS);
		spe = sp.edit();

		//アクションバー関連
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(R.string.createmat);
		actionBar.setDisplayHomeAsUpEnabled(true);

		//intentの初期化
		intent = new Intent(this,Scorelist.class);

		spn1 = (Spinner)findViewById(R.id.spinner1);
		tvset = (TextView)findViewById(R.id.textView14);
		spn1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(spn1.getSelectedItemId() -1 == -1)
				{
					tvset.setText("セット数 : --");
				}
				else if (spn1.getSelectedItemId() - 1 != 8) {
					tvset.setText("セット数 : 6");
				}
				else
				{
					tvset.setText("セット数 : 5");
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
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
			startActivity(intent);
		}
		try {
			connect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}

	//アクションバーのメニュー表示
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_match, menu);
		return true;
	}


	//アクションバーのメニューのボタン処理
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home) {
			//ひとつ前のページに遷移する
			Intent geti = getIntent();
			Intent i;
			//マイページから飛んできた際はマイページに遷移するようにする
			if(geti.getBooleanExtra("flag",true)){
				i = new Intent(this,Matchlist.class);
			}
			else{
				i = new Intent(this,Mypage.class);
			}
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


	public void create_game() throws JSONException {

		JSONObject json = new JSONObject();
		json.put("sessionID", sp.getString("sessionID", ""));
		//射数、セット数は以降の機能とする
//		if(!edt1.getText().toString().equals("") && !edt2.getText().toString().equals("") &&
//				!edt4.getText().toString().equals("") &&!edt3.getText().toString().equals("") &&
//				spn1.getSelectedItemId() != 0){
//			json.put("matchName", edt1.getText());
//			json.put("sponsor", edt2.getText());
//			json.put("arrows", edt3.getText());
//			json.put("perEnd", edt4.getText());
//			json.put("length", spn1.getSelectedItemId() - 1);//-1するかもしれない
//			if(rdb1.isChecked()) {
//				json.put("permission", 0);
//			}
//			else{
//				json.put("permission", 1);
//			}
//			Connecthelper.socket[1].emit("insertMatch", json);
//		}
		//すべての入力項目が埋まっていたら試合を挿入するためにemitする
		if(!edt1.getText().toString().equals("") && !edt2.getText().toString().equals("") &&	spn1.getSelectedItemId() != 0){
			json.put("matchName", edt1.getText());
			json.put("sponsor", edt2.getText());
			json.put("arrows", "6");
			if(spn1.getSelectedItemId() - 1 == 8)
			{
				json.put("perEnd", "5");
			}
			else {
				json.put("perEnd", "6");
			}
			json.put("length", spn1.getSelectedItemId() - 1);
			if(rdb1.isChecked()) {
				json.put("permission", 0);
			}
			else{
				json.put("permission", 1);
			}
			Connecthelper.socket[1].emit("insertMatch", json);
		}
		else{
			Toast.makeText(this,"必要な情報をすべて入力してください",Toast.LENGTH_LONG).show();
		}
//		btn1.setEnabled(false);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method st
		if(v.getId() == R.id.button1) {
			try {
				//ボタンを押されたら試合を挿入するための関数に入る
				create_game();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}
	@Override
	protected void onDestroy() {
		super.onDestroy();

		//SocketIOを切る
		Connecthelper.socket[1].disconnect();
	}
	private void connect() throws MalformedURLException {
		System.setProperty("java.net.preferIPv6Addresses", "false");
		try{

			Connecthelper.socket[1].open();

				//onイベントの設定
				Connecthelper.socket[1].once("insertMatch", insertMatch);
				Connecthelper.socket[1].once("checkOrganization", checkOrganization);

			//接続
			Connecthelper.socket[1].connect();

			JSONObject json = new JSONObject();
				//自分が団体に所属しているか確認する
				json.put("sessionID",sp.getString("sessionID",""));
				Connecthelper.socket[1].emit("checkOrganization", json);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private Emitter.Listener checkOrganization = new Emitter.Listener() {
		@Override
		public void call(Object... args) {
			try {
				//自分が団体に所属していていなかったら、所属団体のみの欄を消す
				JSONObject jsonObject = (JSONObject) args[0];
				if(jsonObject.getString("belongs").toString().equals("false")){
					runOnUiThread(new Runnable() {
						public void run() {
							rdb2.setVisibility(View.GONE);
						}
					});
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				Log.e("", "error    " + e1);
				e1.printStackTrace();
			}
		}
	};
	private Emitter.Listener insertMatch = new Emitter.Listener() {
		@Override
		public void call(Object... args) {
			try {
				//試合が追加で来た際に相手からemitされるので試合の追加は完了
				//得点表一覧に遷移する
				JSONObject jsonObject = (JSONObject) args[0];
				intent.putExtra("m_id", Integer.parseInt(jsonObject.getString("m_id").toString()));
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
				finish();
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
			Intent geti = getIntent();
			Intent i;
			//マイページから飛んできた際はマイページに遷移するようにする
			if(geti.getBooleanExtra("flag",true)){
				i = new Intent(this,Matchlist.class);
			}
			else{
				i = new Intent(this,Mypage.class);
			}
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(i);
			finish();
		}
		return false;
	}
}
