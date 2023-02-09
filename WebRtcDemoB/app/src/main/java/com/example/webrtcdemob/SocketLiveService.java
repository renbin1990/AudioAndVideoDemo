package com.example.webrtcdemob;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class SocketLiveService {
    public interface SocketCallback {
        void callBack(byte[] data);
    }
    private WebSocket webSocket;
    private SocketCallback socketCallback;
    public SocketLiveService(SocketCallback socketCallback ) {
        this.socketCallback = socketCallback;
    }
    public void start() {
        webSocketServer.start();
    }
    public void close() {
        try {
            webSocket.close();
            webSocketServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    public void sendData(byte[] bytes) {
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.send(bytes);
        }
    }

    private WebSocketServer webSocketServer = new WebSocketServer(new InetSocketAddress(7005)) {
        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            SocketLiveService.this.webSocket = conn;
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {

        }

        @Override
        public void onMessage(WebSocket conn, String message) {

        }
        //老张发送过来
        @Override
        public void onMessage(WebSocket conn, ByteBuffer bytes) {
            Log.i("renbin", "消息长度  : " + bytes.remaining());
            byte[] buf = new byte[bytes.remaining()];
            bytes.get(buf);
            socketCallback.callBack(buf);
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {

        }

        @Override
        public void onStart() {

        }
    };
}
