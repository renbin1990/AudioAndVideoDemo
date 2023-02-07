//package com.example.h265toupingpush;
//
//import static android.media.MediaCodec.CONFIGURE_FLAG_ENCODE;
//import static android.media.MediaFormat.KEY_BIT_RATE;
//import static android.media.MediaFormat.KEY_FRAME_RATE;
//import static android.media.MediaFormat.KEY_I_FRAME_INTERVAL;
//
//import android.hardware.display.DisplayManager;
//import android.hardware.display.VirtualDisplay;
//import android.media.MediaCodec;
//import android.media.MediaCodecInfo;
//import android.media.MediaFormat;
//import android.media.projection.MediaProjection;
//import android.util.Log;
//import android.view.Surface;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.util.Arrays;
//
//public class CodecLiveH264 extends Thread{
//    private SocketLive socketLive;
//    //    数据源
//    private MediaProjection mMediaProjection;
//    private int width;
//    private int height;
//    private byte [] sps_pps_buffer;
////    编码器
//    MediaCodec mediaCodec;
//    private int Nal_SPS = 7;
//    private int Nal_I = 5;
//    // 输出 文件
//    VirtualDisplay virtualDisplay;
//    public CodecLiveH264(SocketLive socketLive, MediaProjection mMediaProjection) {
//        this.socketLive = socketLive;
//        this.mMediaProjection = mMediaProjection;
//    }
//
//
//    public void startLive() {
//        try {
////            mediacodec  中间联系人      dsp芯片   帧
//            MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
//            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
//            format.setInteger(KEY_BIT_RATE, width * height);
//            format.setInteger(KEY_FRAME_RATE, 20);
//            format.setInteger(KEY_I_FRAME_INTERVAL, 1);
//            mediaCodec = MediaCodec.createEncoderByType("video/avc");
//            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//            Surface surface = mediaCodec.createInputSurface();
//            //创建场地
//            virtualDisplay = mMediaProjection.createVirtualDisplay(
//                    "-display",
//                    width, height, 1,
//                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, surface,
//                    null, null);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        start();
//    }
//
//
//
//    @Override
//    public void run() {
//        super.run();
//        mediaCodec.start();
//        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
//        while (true) {
////直接拿到输出，不用管输入，输入已经被实现了
//            int outIndex =    mediaCodec.dequeueOutputBuffer(info, 10000);
//            if (outIndex >= 0) {
////                编码的数据
//                ByteBuffer byteBuffer =  mediaCodec.getOutputBuffer(outIndex);
////                byte[] ba = new byte[byteBuffer.remaining()];
////                byteBuffer.get(ba);//将容器的byteBuffer  内部的数据 转移到 byte[]中
////                FileUtils.writeBytes(ba);
////                FileUtils.writeContent(ba);
//                dealFrame(byteBuffer,info);
//                mediaCodec.releaseOutputBuffer(outIndex, false
//                );
//            }
//        }
//
//    }
//
//    private void dealFrame(ByteBuffer bb, MediaCodec.BufferInfo info) {
//        int offset = 4 ; //第五个字节  获取帧类型
//        if (bb.get(2) == 0x01) {
//            offset = 3;
//        }
//        int type = (bb.get(offset) & 0x1F);
//        if (type == Nal_SPS){  //sps  只输出一份
//            sps_pps_buffer = new byte[info.size];
//            bb.get(sps_pps_buffer);
//        }else if (type == Nal_I){  //i针
//            final  byte [] bytes = new byte[info.size];
//            bb.get(bytes);
//            byte [] newBuf = new byte[sps_pps_buffer.length+ bytes.length];
//            //把SPS I 针拷贝到新的数组中，发送数据
//            System.arraycopy(sps_pps_buffer,0,newBuf,0,sps_pps_buffer.length);
//            System.arraycopy(bytes,0,newBuf,sps_pps_buffer.length,bytes.length);
//            this.socketLive.sendData(newBuf);
//        }else {
//            final  byte [] bytes = new byte[info.size];
//            bb.get(bytes);
//            this.socketLive.sendData(bytes);
//            Log.v("---- > 视频数据:",""+ Arrays.toString(bytes));
//        }
//    }
//}
