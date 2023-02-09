package com.example.webrtcdemob;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.io.IOException;

public class LocalSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private Camera mCamera;
    private Camera.Size size;
    private byte[] buffer;
    EncodePushLiveH264 encodecPushLiveH264;
    public LocalSurfaceView(Context context) {
        super(context);
    }

    public LocalSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        //获取摄像头数据
        startPreview();
    }

    public void startCapture(SocketLiveService.SocketCallback socketCallback){
        //传递摄像头获取的宽高
        encodecPushLiveH264 = new EncodePushLiveH264(socketCallback,size.height,size.width);
        encodecPushLiveH264.startLive();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    private void startPreview() {
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
//        流程
        Camera.Parameters parameters = mCamera.getParameters();
//尺寸
        size = parameters.getPreviewSize();

        try {
            mCamera.setPreviewDisplay(getHolder());
//            横着
            mCamera.setDisplayOrientation(90);
//            width  heith  YUV数据 1.5倍
            buffer = new byte[size.width * size.height * 3 / 2];
            mCamera.addCallbackBuffer(buffer);
            mCamera.setPreviewCallbackWithBuffer(this);
//            输出数据怎么办
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //摄像头录制数据不对，需要对摄像头进行翻转  录制数据 NV21 是横向的
    // NV21---->NV12 转化
    //nv12 又名YUV420
    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (encodecPushLiveH264 != null){
            //数据丢给编码曾
            encodecPushLiveH264.encodeFrame(bytes);
        }

        mCamera.addCallbackBuffer(bytes);
    }


}
