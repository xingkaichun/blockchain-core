package com.xingkaichun.helloworldblockchain.core.impl;

import com.xingkaichun.helloworldblockchain.core.*;
import com.xingkaichun.helloworldblockchain.core.model.Block;
import com.xingkaichun.helloworldblockchain.core.model.transaction.Transaction;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionInput;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionOutput;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionType;
import com.xingkaichun.helloworldblockchain.core.tools.*;
import com.xingkaichun.helloworldblockchain.crypto.ByteUtil;
import com.xingkaichun.helloworldblockchain.crypto.model.Account;
import com.xingkaichun.helloworldblockchain.netcore.dto.BlockDto;
import com.xingkaichun.helloworldblockchain.netcore.dto.TransactionDto;
import com.xingkaichun.helloworldblockchain.setting.BlockSetting;
import com.xingkaichun.helloworldblockchain.setting.GenesisBlockSetting;
import com.xingkaichun.helloworldblockchain.util.LogUtil;
import com.xingkaichun.helloworldblockchain.util.ThreadUtil;
import com.xingkaichun.helloworldblockchain.util.TimeUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 默认实现
 *
 * @author 邢开春 409060350@qq.com
 */
public class MinerDefaultImpl extends Miner {

    //region 属性与构造函数
    public MinerDefaultImpl(CoreConfiguration coreConfiguration, Wallet wallet, BlockchainDatabase blockchainDatabase, UnconfirmedTransactionDatabase unconfirmedTransactionDatabase) {
        super(coreConfiguration, wallet, blockchainDatabase, unconfirmedTransactionDatabase);
    }
    //endregion


    @Override
    public void start() {
        while(true){
            ThreadUtil.millisecondSleep(10);
            if(!isActive()){
                continue;
            }
            //创建一个账户用于存放挖矿成功后发放的激励金额
            Account minerAccount = wallet.createAccount();
            Block block = buildMiningBlock(blockchainDatabase,unconfirmedTransactionDatabase,minerAccount);
            long startTimestamp = TimeUtil.millisecondTimestamp();
            while(true){
                if(!isActive()){
                    break;
                }
                //在挖矿的期间，可能收集到新的交易。每隔一定的时间，重新组装挖矿中的区块，这样新收集到交易就可以被放进挖矿中的区块了。
                if(TimeUtil.millisecondTimestamp()-startTimestamp > coreConfiguration.getMinerMineTimeInterval()){
                    break;
                }
                //随机数
                block.setNonce(ByteUtil.bytesToHexString(ByteUtil.random32Bytes()));
                //计算区块哈希
                block.setHash(BlockTool.calculateBlockHash(block));
                //判断共识是否达成(即挖矿是否成功)
                if(blockchainDatabase.getConsensus().checkConsensus(blockchainDatabase,block)){
                    //挖到矿了，账户里有挖矿成功发放的激励金额，将账户放入钱包。
                    wallet.saveAccount(minerAccount);
                    LogUtil.debug("祝贺您！挖矿成功！！！区块高度:"+block.getHeight()+",区块哈希:"+block.getHash());
                    //业务模型转换
                    BlockDto blockDto = Model2DtoTool.block2BlockDto(block);
                    //将矿放入区块链
                    boolean isAddBlockToBlockchainSuccess = blockchainDatabase.addBlockDto(blockDto);
                    if(!isAddBlockToBlockchainSuccess){
                        LogUtil.debug("挖矿成功，但是区块放入区块链失败。");
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void deactive() {
        coreConfiguration.deactiveMiner();
    }

    @Override
    public void active() {
        coreConfiguration.activeMiner();
    }

    @Override
    public boolean isActive() {
        return coreConfiguration.isMinerActive();
    }




    /**
     * 构建挖矿区块
     */
    private Block buildMiningBlock(BlockchainDatabase blockchainDatabase, UnconfirmedTransactionDatabase unconfirmedTransactionDatabase, Account minerAccount) {
        long timestamp = TimeUtil.millisecondTimestamp();

        Block tailBlock = blockchainDatabase.queryTailBlock();
        Block nonNonceBlock = new Block();
        //这个挖矿时间不需要特别精确，没必要非要挖出矿的前一霎那时间。
        nonNonceBlock.setTimestamp(timestamp);

        if(tailBlock == null){
            nonNonceBlock.setHeight(GenesisBlockSetting.HEIGHT +1);
            nonNonceBlock.setPreviousHash(GenesisBlockSetting.HASH);
        } else {
            nonNonceBlock.setHeight(tailBlock.getHeight()+1);
            nonNonceBlock.setPreviousHash(tailBlock.getHash());
        }
        List<Transaction> packingTransactions = packingTransactions(blockchainDatabase,unconfirmedTransactionDatabase);
        nonNonceBlock.setTransactions(packingTransactions);

        //创建挖矿奖励交易
        //激励金额
        Incentive incentive = blockchainDatabase.getIncentive();
        long incentiveValue = incentive.incentiveValue(blockchainDatabase,nonNonceBlock);
        //激励交易
        Transaction mineAwardTransaction = buildIncentiveTransaction(minerAccount.getAddress(),incentiveValue);
        packingTransactions.add(0,mineAwardTransaction);

        String merkleTreeRoot = BlockTool.calculateBlockMerkleTreeRoot(nonNonceBlock);
        nonNonceBlock.setMerkleTreeRoot(merkleTreeRoot);

        //计算挖矿难度
        nonNonceBlock.setDifficulty(blockchainDatabase.getConsensus().calculateDifficult(blockchainDatabase,nonNonceBlock));
        return nonNonceBlock;
    }
    /**
     * 构建区块的挖矿奖励交易。
     */
    private Transaction buildIncentiveTransaction(String address,long incentiveValue) {
        Transaction transaction = new Transaction();
        transaction.setTransactionType(TransactionType.GENESIS_TRANSACTION);

        ArrayList<TransactionOutput> outputs = new ArrayList<>();
        TransactionOutput output = new TransactionOutput();
        output.setAddress(address);
        output.setValue(incentiveValue);
        output.setOutputScript(ScriptTool.createPayToPublicKeyHashOutputScript(address));
        outputs.add(output);

        transaction.setOutputs(outputs);
        transaction.setTransactionHash(TransactionTool.calculateTransactionHash(transaction));
        return transaction;
    }
    /**
     * 确认打包哪些'未确认交易'
     */
    private List<Transaction> packingTransactions(BlockchainDatabase blockchainDatabase, UnconfirmedTransactionDatabase unconfirmedTransactionDatabase) {
        //获取一部分未确认交易，最优的方式是获取所有未确认的交易进行处理，但是数据处理起来会很复杂，因为项目是helloworld的，所以简单的拿一部分数据即可。
        List<TransactionDto> forMineBlockTransactionDtos = unconfirmedTransactionDatabase.selectTransactions(1,10000);

        List<Transaction> transactions = new ArrayList<>();
        List<Transaction> backupTransactions = new ArrayList<>();

        if(forMineBlockTransactionDtos != null){
            for(TransactionDto transactionDto:forMineBlockTransactionDtos){
                try {
                    Transaction transaction = Dto2ModelTool.transactionDto2Transaction(blockchainDatabase,transactionDto);
                    transactions.add(transaction);
                } catch (Exception e) {
                    String transactionHash = TransactionDtoTool.calculateTransactionHash(transactionDto);
                    LogUtil.error("类型转换异常,将从挖矿交易数据库中删除该交易["+transactionHash+"]。",e);
                    unconfirmedTransactionDatabase.deleteByTransactionHash(transactionHash);
                }
            }
        }

        backupTransactions.clear();
        backupTransactions.addAll(transactions);
        transactions.clear();

        for(Transaction transaction : backupTransactions){
            boolean checkTransaction = blockchainDatabase.checkTransaction(transaction);
            if(checkTransaction){
                transactions.add(transaction);
            }else {
                String transactionHash = TransactionTool.calculateTransactionHash(transaction);
                LogUtil.debug("交易不能被挖矿,将从挖矿交易数据库中删除该交易。交易哈希"+transactionHash);
                unconfirmedTransactionDatabase.deleteByTransactionHash(transactionHash);
            }
        }

        backupTransactions.clear();
        backupTransactions.addAll(transactions);
        transactions.clear();


        //防止双花
        Set<String> transactionOutputIdSet = new HashSet<>();
        for(Transaction transaction : backupTransactions){
            List<TransactionInput> inputs = transaction.getInputs();
            if(inputs != null){
                boolean canAdd = true;
                for(TransactionInput transactionInput : inputs) {
                    TransactionOutput unspentTransactionOutput = transactionInput.getUnspentTransactionOutput();
                    String transactionOutputId = TransactionTool.getTransactionOutputId(unspentTransactionOutput);
                    if(transactionOutputIdSet.contains(transactionOutputId)){
                        canAdd = false;
                        String transactionHash = TransactionTool.calculateTransactionHash(transaction);
                        LogUtil.debug("交易不能被挖矿,将从挖矿交易数据库中删除该交易。交易哈希"+transactionHash);
                        unconfirmedTransactionDatabase.deleteByTransactionHash(transactionHash);
                        break;
                    }else {
                        transactionOutputIdSet.add(transactionOutputId);
                    }
                }
                if(canAdd){
                    transactions.add(transaction);
                }
            }
        }



        backupTransactions.clear();
        backupTransactions.addAll(transactions);
        transactions.clear();


        //防止一个地址被用多次
        Set<String> addressSet = new HashSet<>();
        for(Transaction transaction : backupTransactions){
            List<TransactionOutput> outputs = transaction.getOutputs();
            if(outputs != null){
                boolean canAdd = true;
                for (TransactionOutput output:outputs){
                    String address = output.getAddress();
                    if(addressSet.contains(address)){
                        canAdd = false;
                        String transactionHash = TransactionTool.calculateTransactionHash(transaction);
                        LogUtil.debug("交易不能被挖矿,将从挖矿交易数据库中删除该交易。交易哈希"+transactionHash);
                        unconfirmedTransactionDatabase.deleteByTransactionHash(transactionHash);
                        break;
                    }else {
                        addressSet.add(address);
                    }
                }
                if(canAdd){
                    transactions.add(transaction);
                }
            }
        }


        //按照费率(每字符的手续费)从大到小排序交易
        TransactionTool.sortByTransactionFeeRateDescend(transactions);


        backupTransactions.clear();
        backupTransactions.addAll(transactions);
        transactions.clear();

        //到此时，剩余交易都是经过验证的了，且按照交易费率从大到小排列了。
        //尽可能多的获取交易
        long size = 0;
        for(int i=0; i<backupTransactions.size(); i++){
            //序号从0开始，加一。
            //留给挖矿交易一个位置，减一。
            if(i+1 > BlockSetting.BLOCK_MAX_TRANSACTION_COUNT-1){
                break;
            }
            Transaction transaction = backupTransactions.get(i);
            size += SizeTool.calculateTransactionSize(transaction);
            if(size > BlockSetting.BLOCK_MAX_CHARACTER_COUNT){
                break;
            }
            transactions.add(transaction);
        }
        return transactions;
    }

}
