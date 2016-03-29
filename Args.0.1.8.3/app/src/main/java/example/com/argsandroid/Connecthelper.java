package example.com.argsandroid;

import com.github.nkzawa.socketio.client.IO;

import java.net.URISyntaxException;

public class Connecthelper {
    static int detailFlag;

    //接続先の指定を行う
    static String ip = "http://160.16.101.132/app/";
    static String socketip = "http://160.16.101.132/";

    //socketを配列として宣言する
    public static com.github.nkzawa.socketio.client.Socket socket[];


    static void socketset() throws URISyntaxException {
        IO.Options opts = new IO.Options();
        opts.reconnection = true;

        socket = new com.github.nkzawa.socketio.client.Socket[6];

        //接続先の指定

        //試合一覧
        socket[0] = IO.socket(socketip + "matchIndex", opts);

        //試合作成
        socket[1] = IO.socket(socketip + "matchIndex", opts);

        //得点表一覧
        socket[2] = IO.socket(socketip + "scoreCardIndex", opts);

        //得点表作成
        socket[3] = IO.socket(socketip + "scoreCardIndex", opts);

        //得点表
        socket[4] = IO.socket(socketip + "scoreCard", opts);

        //ランキング
        socket[5] = IO.socket(socketip + "rankingIndex", opts);
    }
}