package com.example.h265toupingpush;

import static android.media.MediaCodec.CONFIGURE_FLAG_ENCODE;

import android.hardware.display.DisplayManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

public class CodecLiveH265 extends Thread{
//    数据源
    private MediaProjection mMediaProjection;
    private int width;
    private int height;
//    编码器
    MediaCodec mediaCodec;
    private byte [] vps_sps_pps_buffer;
    private int NAL_VPS = 32;
    // 输出 文件
    public CodecLiveH265(MediaProjection mMediaProjection) {
        this.mMediaProjection = mMediaProjection;
//        解码  ======》json----》 自身提供了 你想要的信息 jiema
        this.width = 640;
        this.height = 1920;
//        编码   -----》 输出json  基本  款考 sps pps 解码   json
//        编码   json
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height);

        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC);
//            1s  20  david   生产黄豆       20个黄豆  每秒     每隔30个数量  绿豆
//            GOP   很长 -----》   30 I强制  直播  强制
//
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
//            30帧    一个I帧
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 30);
//            码率     width heigth  帧  码率    zip   txt 100k  zip（h264）    50k    20k
            format.setInteger(MediaFormat.KEY_BIT_RATE, width * height);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
//            后面   编码标志位
            mediaCodec.configure(format,null,null,CONFIGURE_FLAG_ENCODE);
//            mediaCodec  david   ---》场地 越大   1  越小 2        1dp-----
            Surface surface= mediaCodec.createInputSurface();
//            mMediaProjection---屏幕  ----》    虚拟的屏幕
            mMediaProjection.createVirtualDisplay("ren_bin", width, height, 2,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    surface, null, null
            );
//前面事情做完了
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void run() {
        super.run();
        mediaCodec.start();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        while (true) {
//直接拿到输出，不用管输入，输入已经被实现了
            int outIndex =    mediaCodec.dequeueOutputBuffer(info, 10000);
            if (outIndex >= 0) {
//                编码的数据
                ByteBuffer byteBuffer =  mediaCodec.getOutputBuffer(outIndex);
//                byte[] ba = new byte[byteBuffer.remaining()];
//                byteBuffer.get(ba);//将容器的byteBuffer  内部的数据 转移到 byte[]中
//                FileUtils.writeBytes(ba);
//                FileUtils.writeContent(ba);
                dealFrame(byteBuffer,info);
                mediaCodec.releaseOutputBuffer(outIndex, false

                );
            }
        }

    }

    /**
     * **VPS=32**
     * **SPS=33**
     * **PPS=34**
     * **IDR=19**
     * **P=1**
     * **B=0**
     * @param bb
     * @param info
     */
    private void dealFrame(ByteBuffer bb, MediaCodec.BufferInfo info) {
        int offset = 4 ; //第五个字节  获取帧类型
        int type = (bb.get(offset) & 0x7E) >> 1;
        if (type == NAL_VPS){  //vps
            vps_sps_pps_buffer = new byte[info.size];
            bb.get(vps_sps_pps_buffer);
        }

    }
}
