package com.example.webrtcdemob;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

public class EncodePushLiveH264 {

    public static final int NAL_I = 0x5;
    public static final int NAL_SPS = 0x7;
    private final SocketLiveService socketLiveService;
    private MediaCodec mediaCodec;
    private int height;
    private int width;
    private byte[] yuv;
    private long frameIndex;
    private byte[] sps_pps_buf;
    private byte[] nv12;

    public EncodePushLiveH264(SocketLiveService.SocketCallback socketCallback, int height, int width) {
        this.socketLiveService = new SocketLiveService( socketCallback);
        socketLiveService.start();
        this.height = height;
        this.width = width;
    }

    public void startLive(){
        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            //硬编码配置信息
            //因为YuvUtils.portraitData2Raw换算额横屏转竖屏，所以这第一的宽高需要交换
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,height,width);
            // 指定比特率
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, height * width);
            // 指定帧率
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            // 指定编码器颜色格式
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            //指定关键帧时间间隔，一般设置为每秒关键帧
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5); //IDR帧刷新时间
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();
            int bufferLength = width*height*3/2;
            yuv = new byte[bufferLength];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 摄像头传过来的一帧数据
     * @param input
     */
    public void encodeFrame(byte[] input) {
        //数据转化 nv21---nv12
        nv12 = YuvUtils.nv21toNV12(input);
        //横竖屏转换  横 旋转90°
        YuvUtils.portraitData2Raw(nv12,yuv,width,height);
        //数据编码
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(100000);
        if (inputBufferIndex >= 0){
            ByteBuffer[] byteBuffers = mediaCodec.getInputBuffers();
            ByteBuffer inputBuffer = byteBuffers[inputBufferIndex];
            inputBuffer.clear();
            //添加摄像头数据
            inputBuffer.put(yuv);
            // dsp芯片解码
            long presentationTimeUs = computePresentationTime(frameIndex);
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, yuv.length, presentationTimeUs, 0);
            frameIndex++;
        }
//        cpu数据---》dsp   编码好
//        cpu  直播   通话 剪辑
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000);
        if (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
            //保存到本地进行测试
//            byte[] data = new byte[bufferInfo.size];
//            outputBuffer.get(data);
//            YuvUtils.writeBytes(data);
//            YuvUtils.writeContent(data);
            //解析 sps pps ipb帧 进行传输
            dealFrame(outputBuffer, bufferInfo);
            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
        }


    }

    private void dealFrame(ByteBuffer bb, MediaCodec.BufferInfo bufferInfo) {
//00 00  00 01
//00 00 01
        int offset = 4;
        if (bb.get(2) == 0x01) {
            offset = 3;
        }
//    sps
        int type = (bb.get(offset) & 0x1F) ;
//        有 1  没有2  type=7
        if (type == NAL_SPS) {
//            不发送       I帧
            sps_pps_buf = new byte[bufferInfo.size];
            bb.get(sps_pps_buf);
        }else if (type == NAL_I) {
            final byte[] bytes = new byte[bufferInfo.size];
            bb.get(bytes);
//            bytes           I帧的数据
            byte[] newBuf = new byte[sps_pps_buf.length + bytes.length];
            System.arraycopy(sps_pps_buf, 0, newBuf, 0, sps_pps_buf.length);
            System.arraycopy(bytes, 0, newBuf, sps_pps_buf.length, bytes.length);
//            编码层   推送出去
            socketLiveService.sendData(newBuf);

        }else {
            final byte[] bytes = new byte[bufferInfo.size];
            bb.get(bytes);
            this.socketLiveService.sendData(bytes);
        }
    }

    //dsp芯片解码
    private long computePresentationTime(long frameIndex) {
        return  frameIndex * 1000000 / 15;
    }
}
