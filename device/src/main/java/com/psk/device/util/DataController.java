package com.psk.device.util;

/**
 * ER1 心电仪工具类
 */
public class DataController {
    public static int index = 0;

    public static int[] amp = {5, 10, 20};
    public static int ampKey = 1;

    public static int maxIndex;
    public static float mm2px;

    // for wave
    public static float[] dataSrc;
    // received from device
    public static float[] dataRec = new float[0];

    public static void feed(float[] fs) {
        if (fs == null || fs.length == 0) {
            fs = new float[5];
        }

        if (dataSrc == null) {
            dataSrc = new float[maxIndex];
        }

        for (int i = 0; i < fs.length; i++) {
            int tempIndex = (index + i) % dataSrc.length;
            dataSrc[tempIndex] = fs[i];
        }

        index = (index + fs.length) % dataSrc.length;

    }

    synchronized public static void receive(float[] fs) {
        if (fs == null || fs.length == 0) {
            return;
        }

        float[] temp = new float[dataRec.length + fs.length];
        System.arraycopy(dataRec, 0, temp, 0, dataRec.length);
        System.arraycopy(fs, 0, temp, dataRec.length, fs.length);

        dataRec = temp;

    }

    synchronized public static float[] draw(int n) {
        if (n == 0 || n > dataRec.length) {
            return null;
        }

        float[] res = new float[n];
        float[] temp = new float[dataRec.length - n];
        System.arraycopy(dataRec, 0, res, 0, n);
        System.arraycopy(dataRec, n, temp, 0, dataRec.length - n);

        dataRec = temp;

        return res;
    }

    synchronized public static void clear() {
        index = 0;
        dataRec = new float[0];
        dataSrc = null;
    }

    public static float byteTomV(byte a, byte b) {
        if (a == (byte) 0xff && b == (byte) 0x7f)
            return 0f;

        int n = ((a & 0xFF) | (short) (b << 8));

//        float mv = (float) (n*12.7*1800*1.03)/(10*227*4096);
        float mv = (float) (n * (1.0035 * 1800) / (4096 * 178.74));
//        float mv = (float) (n * 0.002467);

        return mv;
    }
}
