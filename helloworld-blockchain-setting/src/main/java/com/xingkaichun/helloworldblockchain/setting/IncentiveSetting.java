package com.xingkaichun.helloworldblockchain.setting;

/**
 * 激励设置
 *
 * @author 邢开春 409060350@qq.com
 */
public class IncentiveSetting {

    //挖出一个区块的期望耗时时间(单位：毫秒)
    public static final long BLOCK_TIME = 1000 * 60 * 10;
    //一个挖矿难度周期内的区块数量
    public static final long INTERVAL_BLOCK_COUNT = 6 * 24 * 7 * 2;
    //一个挖矿周期内的期望周期耗时时间(单位：毫秒)
    public static final long INTERVAL_TIME = BLOCK_TIME * INTERVAL_BLOCK_COUNT;
    //区块初始奖励
    public static final long BLOCK_INIT_INCENTIVE = 50L * 100000000L;
    //激励金额减半周期：每210000个区块激励减半
    public static final long INCENTIVE_HALVING_INTERVAL = 210000L;
}
