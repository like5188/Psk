package com.psk.device.util;

public class AsciiUtil {

    /**
     * 字符串转换为Ascii
     * @param value
     * @return
     */
    public static String stringToAscii(String value)
    {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if(i != chars.length - 1)
            {
                sbu.append((int)chars[i]).append(",");
            }
            else {
                sbu.append((int)chars[i]);
            }
        }
        return sbu.toString();
    }

    /**
     * Ascii转换为字符串
     * @param value
     * @return
     */
    public static String asciiToString(String value)
    {
        StringBuffer sbu = new StringBuffer();
        String[] chars = value.split(",");
        for (int i = 0; i < chars.length; i++) {
            sbu.append((char) Integer.parseInt(chars[i]));
        }
        return sbu.toString();
    }

    /**
     * 二维码加密
     * @param value
     * @return
     */
    public static String qrCodeEncrypt(String value){
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        int count=0;
        for (int i = 0; i < chars.length; i++) {
            int num=(int)chars[i]-count;
            if(count==5) {
                count=0;
            } else {
                count++;
            }
            sbu.append((char) num);
        }
        return sbu.toString();
    }

    /**
     * 二维码解密
     * @param value
     * @return
     */
    public static String qrCodeDecrypt(String value){
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        int count=0;
        for (int i = 0; i < chars.length; i++) {
            int num=(int)chars[i]+count;
            if(count==5) {
                count=0;
            } else {
                count++;
            }
            sbu.append((char) num);
        }
        return sbu.toString();
    }

    public static void main(String[] args) {
       // System.out.println(qrCodeEncrypt("15,1501102202009001,A01201000046"));
        System.out.println(qrCodeDecrypt("11*..+20.0.+22.221,666?@7/4B3/F"));
    }



}
