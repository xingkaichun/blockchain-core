package com.xingkaichun.helloworldblockchain.setting;

/**
 * 区块设置
 *
 * @author 邢开春 409060350@qq.com
 */
public class BlockSetting {

    //区块最多含有的交易数量(1秒1个)
    public static final long BLOCK_MAX_TRANSACTION_COUNT = IncentiveSetting.BLOCK_TIME / 1000;
    //区块的最大字符数量：用于限制区块的大小
    public static final long BLOCK_MAX_CHARACTER_COUNT = 1024 * 1024;
    //随机数的字符数量：用于限制随机数的大小只能是64个字符。
    public static final long NONCE_CHARACTER_COUNT = 64;
}
