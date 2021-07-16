package com.xingkaichun.helloworldblockchain.crypto;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.regex.Pattern;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


public class ByteUtilTest {

    @Test
    public void uint64ToBytesTest()
    {
        for(int i=0;i<10000;i++){
            Random random = new Random();
            long number = random.nextLong();
            long resumeNumber = ByteUtil.bytesToUint64(ByteUtil.uint64ToBytes(number));
            //校验互转
            Assert.assertEquals(number,resumeNumber);
            //校验long转8字节大端数组
            Assert.assertArrayEquals(uint64ToBytes(number),ByteUtil.uint64ToBytes(number));
        }
    }
    @Test
    public void bytesToUint64Test()
    {
        Random random = new Random();
        byte[] byte8 = new byte[8];
        for(int i=0;i<10000;i++){
            random.nextBytes(byte8);
            byte[] resumeByte8 = ByteUtil.uint64ToBytes(ByteUtil.bytesToUint64(byte8));
            //校验互转
            Assert.assertArrayEquals(byte8,resumeByte8);
            //校验8字节大端数组转long
            Assert.assertEquals(bytesToUint64(byte8),ByteUtil.bytesToUint64(resumeByte8));
        }
    }
    /**
     * long转换为(大端模式)8个字节的字节数组(8*8=64个bit)。
     */
    private static byte[] uint64ToBytes(long value) {
        byte[] bytes = new byte[8];
        bytes[7] = (byte)(0xFF & (value));
        bytes[6] = (byte)(0xFF & (value >> 8));
        bytes[5] = (byte)(0xFF & (value >> 16));
        bytes[4] = (byte)(0xFF & (value >> 24));
        bytes[3] = (byte)(0xFF & (value >> 32));
        bytes[2] = (byte)(0xFF & (value >> 40));
        bytes[1] = (byte)(0xFF & (value >> 48));
        bytes[0] = (byte)(0xFF & (value >> 56));
        return bytes;
    }
    /**
     * (大端模式)8个字节的字节数组(8*8=64个bit)转换为long。
     */
    private static long bytesToUint64(byte[] bytes) {
        long n0 = bytes[0] & 0xff;
        long n1 = bytes[1] & 0xff;
        long n2 = bytes[2] & 0xff;
        long n3 = bytes[3] & 0xff;
        long n4 = bytes[4] & 0xff;
        long n5 = bytes[5] & 0xff;
        long n6 = bytes[6] & 0xff;
        long n7 = bytes[7] & 0xff;

        n7 <<= 0;
        n6 <<= 8;
        n5 <<= 16;
        n4 <<= 24;
        n3 <<= 32;
        n2 <<= 40;
        n1 <<= 48;
        n0 <<= 56;
        long n = n0 | n1 | n2 | n3 | n4 | n5 | n6 | n7;
        return n;
    }




    @Test
    public void bytesToHexStringTest()
    {
        hexTest();
    }
    @Test
    public void hexStringToBytesTest()
    {
        hexTest();
    }
    @Test
    public void hexTest()
    {
        String hexStr = "e19d05c5452598e24caad4a0d85a49146f7be089515c905ae6a19e8a578a6930";
        byte[] b = ByteUtil.hexStringToBytes(hexStr);
        String h = ByteUtil.bytesToHexString(b);
        assertEquals(hexStr,h);


        String hexStr2 = "0bcdef34";
        byte[] bytes2 = new byte[]{(byte)0x0b, (byte)0xcd, (byte)0xef, (byte)0x34};
        assertEquals(hexStr2,ByteUtil.bytesToHexString(bytes2));
        assertArrayEquals(bytes2,ByteUtil.hexStringToBytes(hexStr2));
        assertEquals(hexStr2,ByteUtil.bytesToHexString(ByteUtil.hexStringToBytes(hexStr2)));


        Random random = new Random();
        for (int j = 0; j < 1000; j++) {
            byte[] test = new byte[j];
            random.nextBytes(test);
            assert Arrays.equals(test, ByteUtil.hexStringToBytes(ByteUtil.bytesToHexString(test)));

            //十六进制字符串的长度是字节数量的2倍
            assertEquals(test.length*2, ByteUtil.bytesToHexString(test).length());

            //只包含0123456789abcdef这些字符
            assert Pattern.matches("^[0123456789abcdef]*$", ByteUtil.bytesToHexString(test));
        }
    }
}
