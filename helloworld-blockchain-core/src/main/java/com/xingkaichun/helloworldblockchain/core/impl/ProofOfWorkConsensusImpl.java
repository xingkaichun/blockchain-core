package com.xingkaichun.helloworldblockchain.core.impl;

import com.xingkaichun.helloworldblockchain.core.BlockchainDatabase;
import com.xingkaichun.helloworldblockchain.core.Consensus;
import com.xingkaichun.helloworldblockchain.core.model.Block;
import com.xingkaichun.helloworldblockchain.core.tools.BlockTool;
import com.xingkaichun.helloworldblockchain.setting.GenesisBlockSetting;
import com.xingkaichun.helloworldblockchain.setting.IncentiveSetting;
import com.xingkaichun.helloworldblockchain.util.StringUtil;

import java.math.BigInteger;

/**
 * 工作量证明实现
 * 什么是工作？搬砖是建筑工的工作，改变世界是程序员工作，计算区块哈希是矿工的工作。矿工计算一次区块哈希等同于建筑工搬一块砖。
 * 什么是工作量？工作成果的量化。建筑工搬1块砖，工作量是1，搬10块砖，工作量是10，搬100块砖，工作量是100；矿工计算1次区块哈希，
 * 工作量是1，计算10次区块哈希，工作量是10，计算100次区块哈希，工作量是100。
 * 工作量证明就是用工作量证明谁该获得奖励，谁干的多(工作量多)，谁获得奖励。简而言之，一分耕耘一分收获，干的多，拿的多。
 * 如果总共有三个矿工：甲、乙、丙，分别工作量为100，200，300，那么甲挖矿成功的概率为100/(100+200+300)，
 * 乙挖矿成功的概率为200/(100+200+300)，丙挖矿成功的概率为300/(100+200+300)，所以说工作量证明十分的公平。
 *
 * @author 邢开春 409060350@qq.com
 */
public class ProofOfWorkConsensusImpl extends Consensus {

    @Override
    public boolean checkConsensus(BlockchainDatabase blockchainDatabase, Block block) {
        String difficulty = block.getDifficulty();
        if(StringUtil.isNullOrEmpty(difficulty)){
            difficulty = calculateDifficult(blockchainDatabase,block);
        }

        String hash = block.getHash();
        if(StringUtil.isNullOrEmpty(hash)){
            hash = BlockTool.calculateBlockHash(block);
        }
        return new BigInteger(difficulty,16).compareTo(new BigInteger(hash,16)) > 0;
    }

    /**
     * 计算目标区块的挖矿难度
     * 为什么要设置挖矿难度？目的是控制出块的时间。
     * 挖矿的难度在一个挖矿周期内是不变的，而每当一个新的挖矿周期开始时，就需要重新计算新周期的挖矿难度。
     * 每Setting.IncentiveSetting.INTERVAL_BLOCK_COUNT个区块为一个挖矿周期。
     * 计算第X周期的难度需要参考第(X-1)周期的耗时。
     * 第X周期的难度 = 第(X-1)周期的难度 * 第(X-1)周期的实际挖矿耗时 / 一个周期的期望挖矿耗时。
     * 第一周期、第二周期的难度默认为创世区块的难度。
     *
     * @param blockchainDatabase "目标区块"所在的区块链
     * @param targetBlock 需要计算难度的"目标区块"
     * @return "目标区块"的难度
     */
    public String calculateDifficult(BlockchainDatabase blockchainDatabase, Block targetBlock) {

        // 目标难度
        String targetDifficult;
        // 目标区块高度
        long targetBlockHeight = targetBlock.getHeight();
        // 目标区块高度在第一周期、第二周期内
        if(targetBlockHeight <= IncentiveSetting.INTERVAL_BLOCK_COUNT * 2){
            /*
             * 最初，区块链有区块 0(创世区块)，假设每个挖矿周期有二个区块。
             * 现在要挖第一个周期的区块，即要挖区块1，区块2
             * 第一个周期挖矿难度需要参考前一个周期的挖矿难度，因为没有前一个周期，所以第一个周期的区块默认使用创世区块的难度值。
             * 有了难度，相继把第一个周期内的区块挖出来了，此时，区块链有区块 0(创世区块)，1(高度为1的区块)，2(高度为2的区块)。
             * 现在要计算第二个周期的难度了，此时需要第一个周期的耗时，即[产生区块2的时间戳]减去[开始挖区块1的时刻]
             * ，因为[开始挖区块1的时刻]也不知道(我们知道的是区块1被挖出来的那个时刻，不要搞混)
             * ，所以第二个周期的区块也默认使用创世区块的难度值。
             * 上面，我们提出了第X周期耗时的计算方法，即，第X周期耗时=[第X周期最后一个区块的时间戳]减去[第(X-1)周期最后一个区块的时间戳]。
             * 有了难度，相继把第二个周期内的区块挖出来了，此时，区块链有区块 0，1，2，3，4。
             * 现在要计算第三个周期的难度了，此时需要第二个周期的耗时，即[产生区块4的时间戳]减去[开始挖区块3的时刻]
             * ，因为[开始挖区块3的时刻]就是[产生区块2的时间戳]，所以第二周期的耗时可以计算，
             * 所以从第三个开始的周期不用设置挖矿难度默认值了。
             */
            targetDifficult = GenesisBlockSetting.DIFFICULTY;
            return targetDifficult;
        }

        // 目标区块的上一个区块
        Block targetBlockPreviousBlock = blockchainDatabase.queryBlockByBlockHeight(targetBlockHeight-1);
        /*
         * 目标区块的上一个区块如果不是一个周期的末尾，说明一个周期尚未结束
         * ，说明目标区块和[目标区块的上一个区块]位于同一个周期，此时目标区块难度和[目标区块的上一个区块]的难度相同。
         */
        if (targetBlockPreviousBlock.getHeight() % IncentiveSetting.INTERVAL_BLOCK_COUNT != 0){
            targetDifficult = targetBlockPreviousBlock.getDifficulty();
            return targetDifficult;
        }

        // *** 计算新周期难度值 ***
        // 上个周期的最后一个区块，此时，targetBlockPreviousBlock是上一个周期的最后一个区块，这里仅仅是重新命名了一个更为准确的变量名称。
        Block previousIntervalLastBlock = targetBlockPreviousBlock;
        // 上上个周期最后一个区块
        Block previousPreviousIntervalLastBlock = blockchainDatabase.queryBlockByBlockHeight(previousIntervalLastBlock.getHeight()- IncentiveSetting.INTERVAL_BLOCK_COUNT);
        // 上个周期出块实际耗时
        long previousIntervalActualTimespan = previousIntervalLastBlock.getTimestamp() - previousPreviousIntervalLastBlock.getTimestamp();

        /*
         * 假设有一款随机数生成器，其每秒钟生成1个随机数，其随机数范围是0到9999。
         * 那么每一次生成的随机数的值  < 1      的概率是: 1/10000
         * 那么每一次生成的随机数的值  < 10     的概率是: 10/10000
         * 那么每一次生成的随机数的值  < 100    的概率是: 100/10000
         * 那么每一次生成的随机数的值  < 1000   的概率是: 1000/10000
         * 那么每一次生成的随机数的值  < M      的概率是: M/10000
         * 那么每一次生成的随机数在范围[0,M)内  的概率是: M/10000
         * 那么如果我想要确定一个范围[0,Y)(Y是未知的)，生成器每次生成的数落在这个范围的概率是M/10000(M是已知的)，那么这个Y=概率*范围=(M/10000)*10000=M。
         *
         * 第一个周期
         * 现在我有一个随机数生成器。
         * 能控制随机数生成器在大约100秒前后(不要生成的太快，例如1秒中就生成出来了，
         * 也不要生成的太慢，例如1000秒还没有生成出来)生成出来一个在范围[0到Y)内的随机数吗？
         * 如果能控制在100秒，那么这个Y是多少？
         * 时间*生成器每秒生成随机数次数*每次落在范围的概率=1
         * 带入数值得，100*1*每次落在范围的概率=1，即每次落在范围的概率P=0.01，Y自然是10000*P=10000*0.01=100。
         *
         * 第二个周期
         * 那么现在我有10个随机数生成器。
         * Y如果是上次计算的100，那么需要花费多少时间随机数生成器(10个生成器只要有一个生成就可以了)才能生成满足生成[0-Y)内的数。
         * 时间*生成器台数*每台生成器每秒生成随机数次数*每次落在范围的概率=1
         * 带入数值得，时间*10*1*0.01=1，即时间=10秒。
         *
         * 第三个周期
         * 那么Y应该如何改变，才能控制10个随机数生成器也在大约100秒前后生成出来一个在范围[0到Y)内的随机数？
         * 如果能控制在100秒，那么这个Y是多少？
         * 第二个周期满足：时间*生成器台数*每台生成器每秒生成随机数次数*每次落在范围的概率=1
         * 第三个周期满足：时间*生成器台数*每台生成器每秒生成随机数次数*每次落在范围的概率=1
         * 即周期2(时间)*周期2(生成器台数)*周期2(每台生成器每秒生成随机数次数)*周期2(每次落在范围的概率)=周期3(时间)*周期3(生成器台数)*周期3(每台生成器每秒生成随机数次数)*周期3(每次落在范围的概率)
         * 化简得：周期2(时间)*周期2(每次落在范围的概率)=周期3(时间)*周期3(每次落在范围的概率)
         * 得：周期3(每次落在范围的概率)=周期2(时间)*周期2(每次落在范围的概率)/周期3(时间)
         * 两边同时乘以生成器生成范围得：周期3(每次落在范围的概率)*生成器范围=周期2(时间)*周期2(每次落在范围的概率)*生成器范围/周期3(时间)
         * 由于 (每次落在范围的概率)*生成器范围就是Y
         * 故周期3(Y)=周期2(Y)*周期2(时间)/周期3(时间)
         *
         * Y就是挖矿难度。
         * 故第X周期的难度 = 第(X-1)周期的难度 * 第(X-1)周期的实际挖矿耗时 / 一个周期的期望挖矿耗时。
         */
        BigInteger bigIntegerTargetDifficult =
                new BigInteger(previousIntervalLastBlock.getDifficulty(),16)
                        .multiply(new BigInteger(String.valueOf(previousIntervalActualTimespan), 10))
                        .divide(new BigInteger(String.valueOf(IncentiveSetting.INTERVAL_TIME), 10));
        return bigIntegerTargetDifficult.toString(16);
    }
}
