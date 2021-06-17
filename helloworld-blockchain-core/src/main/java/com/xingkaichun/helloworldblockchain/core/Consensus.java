package com.xingkaichun.helloworldblockchain.core;

import com.xingkaichun.helloworldblockchain.core.model.Block;

import java.io.Serializable;

/**
 * 挖矿共识
 * 区块链是一个分布式的数据库。任何节点都可以产生下一个区块，如果同时有多个节点都产生了下一个区块，
 * 以哪个节点产生的区块为准？
 * 理想状态下，我们希望整个区块链网络只产生一个下一个区块，这样就不存在以哪个为准的问题了。
 * 理想状态毕竟是理想状态，是一种不能实现的美好愿望，在开放的区块链网络中，不应该、也不会存在一种算法能保证在一定的时间之内，只有一个节点能产生区块。
 * 因此，我们应当控制产生区块的难度，使得一个时间间隔内最好少产生区块，尽可能只产生一个区块。
 * 增加区块产生的难度，区块再也不是随意产生的了，只有满足一定条件的区块才能被认定为下一个区块。
 * 而这个满足的条件是事先约定好的，这个"约定"即共识，共识，共同的认识，这个"事先约定好的"条件即产生下一个区块的共同的认识，这个条件即产生下一个区块的共识。
 * 如果区块满足了这些条件，这个区块也就达到了所有节点的共识，代表它是一个合格的区块。
 * 当然，即使我们控制了产生区块的难度，也有可能多个节点同时都产生了下一个区块(有了共识，只是不同节点同时产生下一个区块的概率少了很多)。
 * 同时产生了多个区块怎么办，区块链网络以哪一个区块为准？
 * 这个问题，一个解决办法就是约定区块链网络中的节点以链长的为准，让它们继续竞争下去，比较谁能尽快的产生下一个区块,
 * 一定存在，在某一时刻，有确定的一条最长的链存在，在这一时刻，为准的链显而易见，就是那条最长的链。
 *
 * @author 邢开春 409060350@qq.com
 */
public abstract class Consensus implements Serializable {

    /**
     * 校验区块是否满足共识
     * 如果区块满足共识的要求，这个区块就可能(为什么说是可能呢？因为还要进一步校验区块的结构、大小等信息)是一个合格的区块
     * ，如果进一步校验通过的话，那么这个区块就被允许添加进区块链了。
     * 如果区块不满足共识的要求，那么这个区块一定是一个非法的区块，非法的区块一定不能被添加进区块链。
     */
    public abstract boolean checkConsensus(BlockchainDatabase blockchainDatabase, Block block) ;

    /**
     * 计算目标区块的挖矿难度
     */
    public abstract String calculateDifficult(BlockchainDatabase blockchainDatabase, Block block) ;
}

