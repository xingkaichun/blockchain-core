package com.xingkaichun.helloworldblockchain.core.impl;

import com.xingkaichun.helloworldblockchain.core.*;
import com.xingkaichun.helloworldblockchain.core.model.Block;
import com.xingkaichun.helloworldblockchain.core.model.transaction.Transaction;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionOutput;
import com.xingkaichun.helloworldblockchain.core.model.wallet.BuildTransactionRequest;
import com.xingkaichun.helloworldblockchain.core.model.wallet.BuildTransactionResponse;
import com.xingkaichun.helloworldblockchain.core.tools.Model2DtoTool;
import com.xingkaichun.helloworldblockchain.netcore.dto.BlockDto;
import com.xingkaichun.helloworldblockchain.netcore.dto.TransactionDto;
import com.xingkaichun.helloworldblockchain.util.SystemUtil;

import java.util.List;

/**
 * 默认实现
 *
 * @author 邢开春 409060350@qq.com
 */
public class BlockchainCoreImpl extends BlockchainCore {

    public BlockchainCoreImpl(CoreConfiguration coreConfiguration, BlockchainDatabase blockchainDatabase, UnconfirmedTransactionDatabase unconfirmedTransactionDatabase, Wallet wallet, Miner miner) {
        super(coreConfiguration,blockchainDatabase,unconfirmedTransactionDatabase,wallet,miner);
    }

    @Override
    public void start() {
        //启动矿工线程
        new Thread(
                ()->{
                    try {
                        miner.start();
                    } catch (Exception e) {
                        SystemUtil.errorExit("矿工在运行中发生异常，请检查修复异常！",e);
                    }
                }
        ).start();
    }

    @Override
    public long queryBlockchainHeight() {
        return blockchainDatabase.queryBlockchainHeight();
    }


    @Override
    public Transaction queryTransactionByTransactionHash(String transactionHash) {
        return blockchainDatabase.queryTransactionByTransactionHash(transactionHash);
    }

    @Override
    public Transaction queryTransactionByTransactionHeight(long transactionHeight) {
        return blockchainDatabase.queryTransactionByTransactionHeight(transactionHeight);
    }

    @Override
    public TransactionOutput queryTransactionOutputByAddress(String address) {
        return blockchainDatabase.queryTransactionOutputByAddress(address);
    }

    @Override
    public TransactionOutput queryUnspentTransactionOutputByAddress(String address) {
        return blockchainDatabase.queryUnspentTransactionOutputByAddress(address);
    }

    @Override
    public TransactionOutput querySpentTransactionOutputByAddress(String address) {
        return blockchainDatabase.querySpentTransactionOutputByAddress(address);
    }

    @Override
    public Block queryBlockByBlockHeight(long blockHeight) {
        return blockchainDatabase.queryBlockByBlockHeight(blockHeight);
    }

    @Override
    public Block queryBlockByBlockHash(String blockHash) {
        return blockchainDatabase.queryBlockByBlockHash(blockHash);
    }

    @Override
    public Block queryTailBlock() {
        return blockchainDatabase.queryTailBlock();
    }

    @Override
    public void deleteTailBlock() {
        blockchainDatabase.deleteTailBlock();
    }

    @Override
    public boolean addBlockDto(BlockDto blockDto) {
        return blockchainDatabase.addBlockDto(blockDto);
    }

    @Override
    public boolean addBlock(Block block) {
        BlockDto blockDto = Model2DtoTool.block2BlockDto(block);
        return addBlockDto(blockDto);
    }


    @Override
    public void deleteBlocks(long blockHeight) {
        blockchainDatabase.deleteBlocks(blockHeight);
    }


    @Override
    public BuildTransactionResponse buildTransaction(BuildTransactionRequest request) {
        return wallet.buildTransaction(request);
    }

    @Override
    public void postTransaction(TransactionDto transactionDto) {
        unconfirmedTransactionDatabase.insertTransaction(transactionDto);
    }

    @Override
    public List<TransactionDto> queryUnconfirmedTransactions(long from, long size) {
        return unconfirmedTransactionDatabase.selectTransactions(from,size);
    }

    @Override
    public TransactionDto queryUnconfirmedTransactionByTransactionHash(String transactionHash) {
        return unconfirmedTransactionDatabase.selectTransactionByTransactionHash(transactionHash);
    }
}