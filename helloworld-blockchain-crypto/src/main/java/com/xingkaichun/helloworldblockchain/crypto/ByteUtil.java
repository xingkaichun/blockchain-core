package com.xingkaichun.helloworldblockchain.crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 字节工具类
 *
 * @author 邢开春 409060350@qq.com
 */
public class ByteUtil {

    public static final int BYTE8_BYTE_COUNT = 8;



    /**
     * byte数组转十六进制字符串(十六进制字符串小写，仅包含字符0123456789abcdef)。
     * 不允许省略十六进制字符串前面的零，因此十六进制字符串的长度是字节数量的2倍。
     */
    public static String bytesToHexString(byte[] bytes) {
        return Hex.toHexString(bytes);
    }
    /**
     * 16进制字符串转byte数组(十六进制字符串小写，仅包含字符0123456789abcdef)
     * @param hexString 16进制字符串，该属性值的长度一定是2的整数倍
     */
    public static byte[] hexStringToBytes(String hexString) {
        return Hex.decode(hexString);
    }



    /**
     * 无符号整数转换为(大端模式)8个字节的字节数组。
     */
    public static byte[] uint64ToBytes(long number) {
        return Longs.toByteArray(number);
    }
    /**
     * (大端模式)8个字节的字节数组转换为无符号整数。
     */
    public static long bytesToUint64(byte[] bytes) {
        return Longs.fromByteArray(bytes);
    }



    /**
     * 字符串转换为UTF8字节数组
     */
    public static byte[] stringToUtf8Bytes(String stringValue) {
        return stringValue.getBytes(StandardCharsets.UTF_8);
    }
    /**
     * UTF8字节数组转换为字符串
     */
    public static String utf8BytesToString(byte[] bytesValue) {
        return new String(bytesValue,StandardCharsets.UTF_8);
    }



    /**
     * 拼接数组(数组数量为2个)。
     */
    public static byte[] concatenate(byte[] bytes1,byte[] bytes2) {
        return Bytes.concat(bytes1,bytes2);
    }
    /**
     * 拼接数组(数组数量为3个)。
     */
    public static byte[] concatenate3(byte[] bytes1,byte[] bytes2,byte[] bytes3) {
        return Bytes.concat(bytes1,bytes2,bytes3);
    }
    /**
     * 拼接数组(数组数量为4个)。
     */
    public static byte[] concatenate4(byte[] bytes1,byte[] bytes2,byte[] bytes3,byte[] bytes4) {
        return Bytes.concat(bytes1,bytes2,bytes3,bytes4);
    }

    /**
     * 拼接长度。
     * 计算[传入字节数组]的长度，然后将长度转为8个字节的字节数组(大端)，然后将长度字节数组拼接在[传入字节数组]前，然后返回。
     */
    public static byte[] concatenateLength(byte[] value) {
        return concatenate(uint64ToBytes(value.length),value);
    }

    /**
     * 碾平字节数组列表为字节数组。
     */
    public static byte[] flat(List<byte[]> values) {
        byte[] concatBytes = new byte[0];
        for(byte[] value:values){
            concatBytes = concatenate(concatBytes,value);
        }
        return concatBytes;
    }
    /**
     * 碾平字节数组列表为新的字节数组，然后拼接长度并返回。
     */
    public static byte[] flatAndConcatenateLength(List<byte[]> values) {
        byte[] flatBytes = flat(values);
        return concatenateLength(flatBytes);
    }



    public static boolean isEquals(byte[] bytes1, byte[] bytes2) {
        return Arrays.equals(bytes1,bytes2);
    }

    public static byte[] copy(byte[] sourceBytes, int startPosition, int length) {
        byte[] destinationBytes = new byte[length];
        System.arraycopy(sourceBytes,startPosition,destinationBytes,0,length);
        return destinationBytes;
    }

    public static void copyTo(byte[] sourceBytes, int sourceStartPosition, int length, byte[] destinationBytes, int destinationStartPosition){
        System.arraycopy(sourceBytes,sourceStartPosition,destinationBytes,destinationStartPosition,length);
    }

    public static byte[] random32Bytes(){
        byte[] randomBytes = new byte[32];
        Random RANDOM = new Random();
        RANDOM.nextBytes(randomBytes);
        return randomBytes;
    }
}