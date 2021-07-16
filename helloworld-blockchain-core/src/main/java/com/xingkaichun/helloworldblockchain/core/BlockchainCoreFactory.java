package com.xingkaichun.helloworldblockchain.core;

import com.xingkaichun.helloworldblockchain.core.impl.*;
import com.xingkaichun.helloworldblockchain.core.tools.ResourcePathTool;

/**
 * 创建BlockchainCore的工厂
 *
 * @author 邢开春 409060350@qq.com
 */
public class BlockchainCoreFactory {

    /**
     * 创建BlockchainCore实例
     */
    public static BlockchainCore createDefaultBlockchainCore(){
        return createBlockchainCore(ResourcePathTool.getDataRootPath());
    }

    /**
     * 创建BlockchainCore实例
     *
     * @param corePath BlockchainCore数据存放位置
     */
    public static BlockchainCore createBlockchainCore(String corePath) {

        CoreConfiguration coreConfiguration = new CoreConfigurationDefaultImpl(corePath);
        Incentive incentive = new IncentiveDefaultImpl();
        Consensus consensus = new ProofOfWorkConsensusImpl();
        VirtualMachine virtualMachine = new StackBasedVirtualMachine();
        BlockchainDatabase blockchainDatabase = new BlockchainDatabaseDefaultImpl(coreConfiguration,incentive,consensus,virtualMachine);

        UnconfirmedTransactionDatabase unconfirmedTransactionDatabase = new UnconfirmedTransactionDatabaseDefaultImpl(coreConfiguration);
        Wallet wallet = new WalletImpl(coreConfiguration,blockchainDatabase);
        Miner miner = new MinerDefaultImpl(coreConfiguration,wallet,blockchainDatabase,unconfirmedTransactionDatabase);
        return new BlockchainCoreImpl(coreConfiguration,blockchainDatabase,unconfirmedTransactionDatabase,wallet,miner);
    }
}
