package com.xingkaichun.helloworldblockchain.core.impl;

import com.xingkaichun.helloworldblockchain.core.CoreConfiguration;
import com.xingkaichun.helloworldblockchain.core.UnconfirmedTransactionDatabase;
import com.xingkaichun.helloworldblockchain.core.tools.EncodeDecodeTool;
import com.xingkaichun.helloworldblockchain.core.tools.TransactionDtoTool;
import com.xingkaichun.helloworldblockchain.crypto.ByteUtil;
import com.xingkaichun.helloworldblockchain.netcore.dto.TransactionDto;
import com.xingkaichun.helloworldblockchain.util.FileUtil;
import com.xingkaichun.helloworldblockchain.util.KvDbUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认实现
 *
 * @author 邢开春 409060350@qq.com
 */
public class UnconfirmedTransactionDatabaseDefaultImpl extends UnconfirmedTransactionDatabase {

    private CoreConfiguration coreConfiguration;
    private static final String UNCONFIRMED_TRANSACTION_DATABASE_NAME = "UnconfirmedTransactionDatabase";

    public UnconfirmedTransactionDatabaseDefaultImpl(CoreConfiguration coreConfiguration) {
        this.coreConfiguration = coreConfiguration;
    }

    @Override
    public void insertTransaction(TransactionDto transactionDto) {
        String transactionHash = TransactionDtoTool.calculateTransactionHash(transactionDto);
        KvDbUtil.put(getUnconfirmedTransactionDatabasePath(), getKey(transactionHash), EncodeDecodeTool.encodeTransactionDto(transactionDto));
    }

    @Override
    public List<TransactionDto> selectTransactions(long from, long size) {
        List<TransactionDto> transactionDtoList = new ArrayList<>();
        List<byte[]> bytesTransactionDtos = KvDbUtil.gets(getUnconfirmedTransactionDatabasePath(),from,size);
        if(bytesTransactionDtos != null){
            for(byte[] bytesTransactionDto:bytesTransactionDtos){
                TransactionDto transactionDto = EncodeDecodeTool.decodeToTransactionDto(bytesTransactionDto);
                transactionDtoList.add(transactionDto);
            }
        }
        return transactionDtoList;
    }

    @Override
    public void deleteByTransactionHash(String transactionHash) {
        KvDbUtil.delete(getUnconfirmedTransactionDatabasePath(), getKey(transactionHash));
    }

    @Override
    public TransactionDto selectTransactionByTransactionHash(String transactionHash) {
        byte[] byteTransactionDto = KvDbUtil.get(getUnconfirmedTransactionDatabasePath(), getKey(transactionHash));
        if(byteTransactionDto == null){
            return null;
        }
        return EncodeDecodeTool.decodeToTransactionDto(byteTransactionDto);
    }

    private String getUnconfirmedTransactionDatabasePath() {
        return FileUtil.newPath(coreConfiguration.getCorePath(), UNCONFIRMED_TRANSACTION_DATABASE_NAME);
    }

    private byte[] getKey(String transactionHash){
        return ByteUtil.hexStringToBytes(transactionHash);
    }
}
