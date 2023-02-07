package com.example.h265touping;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class H264Palyer implements SocketLive.SocketCallback {

    private MediaCodec mediaCodec;

    public H264Palyer(Surface surface) {
        try {
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            final MediaFormat format = MediaFormat.
                    createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 720, 1280);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 720 * 1280);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            mediaCodec.configure(format,
                    surface,
                    null, 0);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //收到处理消息
    @Override
    public void callBack(byte[] data) {
        Log.v("----> 接收到的消息长度：",data.length+"");
        int index = mediaCodec.dequeueInputBuffer(100000);
        if (index >= 0){
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(index);
            inputBuffer.clear();
            inputBuffer.put(data, 0, data.length);
            mediaCodec.queueInputBuffer(index,
                    0, data.length, System.currentTimeMillis(), 0);
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000);
//        100k   大于4M   小于 100k
//0-255  字节
//       放一个完整帧  ---》     渲染出来

//        if (outputBufferIndex >= 0) {
//            mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
//        }
        Log.i("---->", "解码器后长度  : " + bufferInfo.size);
        while (outputBufferIndex >= 0) {
            mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        }
    }


}
