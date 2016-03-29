package example.com.argsandroid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.URISyntaxException;


public class Splash extends ActionBarActivity{

    private com.github.nkzawa.socketio.client.Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        //アクションバー関連
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        LinearLayout ll = (LinearLayout)findViewById(R.id.ll1);
        setTextSizeByInch(ll.getId());

        Handler hdl = new Handler();
        // 3000ms遅延させてsplashHandlerを実行します。
        hdl.postDelayed(new splashHandler(), 3000);

        try {
            Connecthelper.socketset();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    //textviewのフォントサイズを変更する
    private void setTextSizeByInch(int layoutid) {
        // ディスプレイ情報を取得する
        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        // ピクセル数（width, height）を取得する
        final int widthPx = metrics.widthPixels;
        final int heightPx = metrics.heightPixels;
        // dpi (xdpi, ydpi) を取得する
        final float xdpi = metrics.xdpi;
        final float ydpi = metrics.ydpi;
        // インチ（width, height) を計算する
        final float widthIn = widthPx / xdpi;
        final float heightIn = heightPx / ydpi;
        // 画面サイズ（インチ）を計算する
        final double in = Math.sqrt(widthIn * widthIn + heightIn * heightIn);
        // 4インチ以上は比率に応じて文字を拡大
        if (in > 4) {
            // 親のレイアウトを取得
            ViewGroup parent = (ViewGroup)findViewById(layoutid);
            setTextSizes(parent, in / 4);
        }
    }

    //textviewのフォントサイズを反映する
    private void setTextSizes(ViewGroup parent, double multiple) {
        for(int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            if (view instanceof ViewGroup) {
                setTextSizes((ViewGroup)view, multiple);
            } else if (view instanceof TextView) {
                TextView targetView = (TextView) view;
                targetView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)(targetView.getTextSize() * multiple));
            }
        }
    }

    private class splashHandler implements Runnable {
        public void run() {
            // スプラッシュ完了後に実行するActivityを指定します。
            Intent intent = new Intent(getApplication(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            // SplashActivityを終了させます。
            Splash.this.finish();
        }
    }
}
