package com.example.h265touping;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class SocketLive {
    private static final String TAG = "David";
    private SocketCallback socketCallback;
    MyWebSocketClient myWebSocketClient;
    private WebSocket webSocket;
    private int port;
    public SocketLive(SocketCallback socketCallback, int port) {
        this.socketCallback = socketCallback;
        this.port = port;
    }
    public void start() {
        try {
            URI url = new URI("ws://192.168.3.43:9007");
            myWebSocketClient = new MyWebSocketClient(url);
            myWebSocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class MyWebSocketClient extends WebSocketClient {

        public MyWebSocketClient(URI serverURI) {
            super(serverURI);
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
            Log.i(TAG, "打开 socket  onOpen: ");
        }

        @Override
        public void onMessage(String s) {
        }
//不断回调他
        @Override
        public void onMessage(ByteBuffer bytes) {
            Log.i(TAG, "消息长度  : "+bytes.remaining());
            byte[] buf = new byte[bytes.remaining()];
            bytes.get(buf);
            socketCallback.callBack(buf);
        }

        @Override
        public void onClose(int i, String s, boolean b) {
            Log.i(TAG, "onClose: ");
        }

        @Override
        public void onError(Exception e) {
            Log.i(TAG, "onError: ");
        }
    }
    public interface SocketCallback{
        void callBack(byte[] data);
    }
}
