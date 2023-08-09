package com.psk.recovery.util;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ShangXiaZhiParser {
    private ByteBuf buffer = Unpooled.buffer(1024 * 1000);
    private ByteBuf bufferShort = Unpooled.buffer(1024 * 1000);

    //骑行数据长度
    private final int FRAME_LENGTH = 13;
    //暂停或者停止
    private final int FRAME_LENGTH_SHORT = 5;
    private ShangXiaZhiReceiver receiver;

    public void setReceiver(ShangXiaZhiReceiver receiver) {
        this.receiver = receiver;
    }

    public void putData(byte[] data) {
        parseBleNormalData(data);
        parseBlePauseOrStopData3(data);
    }

    // 解析游戏数据
    private void parseBleNormalData(byte[] data) {
//        [-93, 33, 32, -128, 1, 4, 15, 15, 0, 6, 1, 65, 81]

        if (data != null && data.length > 0) {
            buffer.writeBytes(data);

            try {
                while (buffer.readableBytes() >= FRAME_LENGTH) {
                    ByteBuf bufTemp = buffer.readBytes(1);
                    byte[] bytesTemp = new byte[1];
                    bufTemp.readBytes(bytesTemp);
                    if (bytesTemp[0] == (byte) 0xA3) {

                        buffer.markReaderIndex();
                        ByteBuf bufTemp1 = buffer.readBytes(3);
                        byte[] bytesTemp1 = new byte[3];
                        bufTemp1.readBytes(bytesTemp1);

                        if (bytesTemp1[0] == (byte) 0x21 && bytesTemp1[1] == (byte) 0x20 && bytesTemp1[2] == (byte) 0x80) {
                            ByteBuf bufTemp2 = buffer.readBytes(FRAME_LENGTH - 4);
                            byte[] bytesTemp2 = new byte[FRAME_LENGTH - 4];
                            bufTemp2.readBytes(bytesTemp2);

                            //重新组帧
                            byte[] bytesTemp3 = new byte[FRAME_LENGTH];
                            bytesTemp3[0] = (byte) 0xA3;
                            bytesTemp3[1] = (byte) 0x21;
                            bytesTemp3[2] = (byte) 0x20;
                            bytesTemp3[3] = (byte) 0x80;
                            System.arraycopy(bytesTemp2, 0, bytesTemp3, 4, bytesTemp2.length);
//                        System.out.println("组包后 骑行： "+Arrays.toString(bytesTemp3));

                            byte mModel = bytesTemp3[4];
                            int mSpeedLevel = bytesTemp3[5] & 0xFF;
                            int mSpeedValue = bytesTemp3[6] & 0xFF;
                            if (mSpeedValue != 0) {
                                mSpeedValue += 2;
                            }
                            int mOffset = bytesTemp3[7] & 0xFF;
                            int mSpasmNum = bytesTemp3[8] & 0xFF;
                            int mSpasmLevel = bytesTemp3[9] & 0xFF;
                            int mRes = bytesTemp3[10] & 0xFF;
                            byte mIntelligence = bytesTemp3[11];
                            byte mDirection = bytesTemp3[12];

                            if (receiver != null) {
                                receiver.onReceive(mModel, mSpeedLevel, mSpeedValue, mOffset, mSpasmNum, mSpasmLevel, mRes, mIntelligence, mDirection);
                            }

                            buffer.discardReadBytes();   //将取出来的这一帧数据在buffer的内存进行清除，释放内存

                        } else {
                            buffer.resetReaderIndex();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 解析暂停、结束
    private void parseBlePauseOrStopData3(byte[] data) {
        if (data != null && data.length > 0) {
            bufferShort.writeBytes(data);

            while (bufferShort.readableBytes() >= FRAME_LENGTH_SHORT) {
                ByteBuf bufTemp = bufferShort.readBytes(1);
                byte[] bytesTemp = new byte[1];
                bufTemp.readBytes(bytesTemp);
                if (bytesTemp[0] == (byte) 0xA3) {

                    bufferShort.markReaderIndex();
                    ByteBuf bufTemp1 = bufferShort.readBytes(3);
                    byte[] bytesTemp1 = new byte[3];
                    bufTemp1.readBytes(bytesTemp1);

                    if (bytesTemp1[0] == (byte) 0x21 && bytesTemp1[1] == (byte) 0x20 && bytesTemp1[2] == (byte) 0x85) {
                        //暂停

                        ByteBuf bufTemp2 = bufferShort.readBytes(FRAME_LENGTH_SHORT - 4);
                        byte[] bytesTemp2 = new byte[FRAME_LENGTH_SHORT - 4];
                        bufTemp2.readBytes(bytesTemp2);

                        //重新组帧
                        byte[] bytesTemp3 = new byte[FRAME_LENGTH_SHORT];
                        bytesTemp3[0] = (byte) 0xA3;
                        bytesTemp3[1] = (byte) 0x21;
                        bytesTemp3[2] = (byte) 0x20;
                        bytesTemp3[3] = (byte) 0x85;
                        System.arraycopy(bytesTemp2, 0, bytesTemp3, 4, bytesTemp2.length);
                        System.out.println("组包后 暂停： " + Arrays.toString(bytesTemp3));

                        if (receiver != null) {
                            receiver.onPause();
                        }

                        bufferShort.discardReadBytes();   //将取出来的这一帧数据在buffer的内存进行清除，释放内存
                    } else if (bytesTemp1[0] == (byte) 0x21 && bytesTemp1[1] == (byte) 0x20 && bytesTemp1[2] == (byte) 0x86) {
                        //停止
                        ByteBuf bufTemp2 = bufferShort.readBytes(FRAME_LENGTH_SHORT - 4);
                        byte[] bytesTemp2 = new byte[FRAME_LENGTH_SHORT - 4];
                        bufTemp2.readBytes(bytesTemp2);

                        //重新组帧
                        byte[] bytesTemp3 = new byte[FRAME_LENGTH_SHORT];
                        bytesTemp3[0] = (byte) 0xA3;
                        bytesTemp3[1] = (byte) 0x21;
                        bytesTemp3[2] = (byte) 0x20;
                        bytesTemp3[3] = (byte) 0x86;
                        System.arraycopy(bytesTemp2, 0, bytesTemp3, 4, bytesTemp2.length);
                        System.out.println("组包后 停止： " + Arrays.toString(bytesTemp3));

                        if (receiver != null) {
                            receiver.onOver();
                        }

                        bufferShort.discardReadBytes();   //将取出来的这一帧数据在buffer的内存进行清除，释放内存
                    } else {
                        bufferShort.resetReaderIndex();
                    }
                }
            }
        }
    }
}
