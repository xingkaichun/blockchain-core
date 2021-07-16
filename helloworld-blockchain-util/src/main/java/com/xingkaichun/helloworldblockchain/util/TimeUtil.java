package com.xingkaichun.helloworldblockchain.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间工具类
 *
 * @author 邢开春 409060350@qq.com
 */
public class TimeUtil {

    public static long millisecondTimestamp(){
        return System.currentTimeMillis();
    }

    //TODO 纳秒
    public static String formatMillisecondTimestamp(long millisecondTimestamp) {
        Date date = new Date(millisecondTimestamp);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return simpleDateFormat.format(date);
    }
}
