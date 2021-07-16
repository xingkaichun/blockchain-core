package com.xingkaichun.helloworldblockchain.core.tools;


import com.xingkaichun.helloworldblockchain.core.model.transaction.Transaction;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionInput;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionOutput;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionType;
import com.xingkaichun.helloworldblockchain.crypto.AccountUtil;
import com.xingkaichun.helloworldblockchain.netcore.dto.TransactionDto;
import com.xingkaichun.helloworldblockchain.setting.TransactionSettingTool;
import com.xingkaichun.helloworldblockchain.util.DataStructureUtil;
import com.xingkaichun.helloworldblockchain.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Transaction工具类
 *
 * @author 邢开春 409060350@qq.com
 */
public class TransactionTool {

    /**
     * 交易输入总额
     */
    public static long getInputValue(Transaction transaction) {
        List<TransactionInput> inputs = transaction.getInputs();
        long total = 0;
        if(inputs != null){
            for(TransactionInput input : inputs) {
                total += input.getUnspentTransactionOutput().getValue();
            }
        }
        return total;
    }



    /**
     * 交易输出总额
     */
    public static long getOutputValue(Transaction transaction) {
        List<TransactionOutput> outputs = transaction.getOutputs();
        long total = 0;
        if(outputs != null){
            for(TransactionOutput output : outputs) {
                total += output.getValue();
            }
        }
        return total;
    }


    /**
     * 交易手续费（只计算标准交易的手续费，创世交易抛出异常）
     */
    public static long getTransactionFee(Transaction transaction) {
        if(transaction.getTransactionType() == TransactionType.STANDARD_TRANSACTION){
            long transactionFee = getInputValue(transaction) - getOutputValue(transaction);
            return transactionFee;
        }else {
            throw new RuntimeException("只能计算标准交易类型的手续费");
        }
    }
    /**
     * 交易费率（只计算标准交易的手续费，创世交易抛出异常）
     */
    public static long getTransactionFeeRate(Transaction transaction) {
        if(transaction.getTransactionType() == TransactionType.STANDARD_TRANSACTION){
            return getTransactionFee(transaction)/SizeTool.calculateTransactionSize(transaction);
        }else {
            throw new RuntimeException("只能计算标准交易类型的交易费率");
        }
    }


    /**
     * 获取待签名数据
     */
    public static String signatureHashAll(Transaction transaction) {
        TransactionDto transactionDto = Model2DtoTool.transaction2TransactionDto(transaction);
        return TransactionDtoTool.signatureHashAll(transactionDto);
    }

    /**
     * 交易签名
     */
    public static String signature(String privateKey, Transaction transaction) {
        TransactionDto transactionDto = Model2DtoTool.transaction2TransactionDto(transaction);
        return TransactionDtoTool.signature(privateKey,transactionDto);
    }

    /**
     * 计算交易哈希
     */
    public static String calculateTransactionHash(Transaction transaction){
        TransactionDto transactionDto = Model2DtoTool.transaction2TransactionDto(transaction);
        return TransactionDtoTool.calculateTransactionHash(transactionDto);
    }






    /**
     * 交易中的金额是否符合系统的约束
     */
    public static boolean checkTransactionValue(Transaction transaction) {
        List<TransactionInput> inputs = transaction.getInputs();
        if(inputs != null){
            //校验交易输入的金额
            for(TransactionInput input:inputs){
                if(!TransactionSettingTool.checkTransactionValue(input.getUnspentTransactionOutput().getValue())){
                    LogUtil.debug("交易金额不合法");
                    return false;
                }
            }
        }

        List<TransactionOutput> outputs = transaction.getOutputs();
        if(outputs != null){
            //校验交易输出的金额
            for(TransactionOutput output:outputs){
                if(!TransactionSettingTool.checkTransactionValue(output.getValue())){
                    LogUtil.debug("交易金额不合法");
                    return false;
                }
            }
        }

        //根据交易类型，做进一步的校验
        if(transaction.getTransactionType() == TransactionType.GENESIS_TRANSACTION){
            //没有需要校验的，跳过。
        } else if(transaction.getTransactionType() == TransactionType.STANDARD_TRANSACTION){
            //交易输入必须要大于等于交易输出
            long inputsValue = getInputValue(transaction);
            long outputsValue = getOutputValue(transaction);
            if(inputsValue < outputsValue) {
                LogUtil.debug("交易校验失败：交易的输入必须大于等于交易的输出。不合法的交易。");
                return false;
            }
            return true;
        } else {
            LogUtil.debug("区块数据异常，不能识别的交易类型。");
            return false;
        }
        return true;
    }

    /**
     * 校验交易中的地址是否是P2PKH地址
     */
    public static boolean checkPayToPublicKeyHashAddress(Transaction transaction) {
        List<TransactionOutput> outputs = transaction.getOutputs();
        if(outputs != null){
            for(TransactionOutput output:outputs){
                if(!AccountUtil.isPayToPublicKeyHashAddress(output.getAddress())){
                    LogUtil.debug("交易地址不合法");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 交易中是否存在重复的[未花费交易输出]
     */
    public static boolean isExistDuplicateUtxo(Transaction transaction) {
        List<String> utxoIds = new ArrayList<>();
        List<TransactionInput> inputs = transaction.getInputs();
        if(inputs != null){
            for(TransactionInput transactionInput : inputs) {
                TransactionOutput unspentTransactionOutput = transactionInput.getUnspentTransactionOutput();
                String utxoId = getTransactionOutputId(unspentTransactionOutput);
                utxoIds.add(utxoId);
            }
        }
        return DataStructureUtil.isExistDuplicateElement(utxoIds);
    }
    /**
     * 区块新产生的地址是否存在重复
     */
    public static boolean isExistDuplicateNewAddress(Transaction transaction) {
        List<String> newAddresss = new ArrayList<>();
        List<TransactionOutput> outputs = transaction.getOutputs();
        if(outputs != null){
            for (TransactionOutput output:outputs){
                String address = output.getAddress();
                newAddresss.add(address);
            }
        }
        return DataStructureUtil.isExistDuplicateElement(newAddresss);
    }

    public static long getTransactionInputCount(Transaction transaction) {
        List<TransactionInput> inputs = transaction.getInputs();
        long transactionInputCount = inputs==null?0:inputs.size();
        return transactionInputCount;
    }

    public static long getTransactionOutputCount(Transaction transaction) {
        List<TransactionOutput> outputs = transaction.getOutputs();
        long transactionOutputCount = outputs==null?0:outputs.size();
        return transactionOutputCount;
    }

    public static long calculateTransactionFee(Transaction transaction) {
        if(transaction.getTransactionType() == TransactionType.GENESIS_TRANSACTION){
            //创世交易没有交易手续费
            return 0;
        }else if(transaction.getTransactionType() == TransactionType.STANDARD_TRANSACTION){
            long inputsValue = getInputValue(transaction);
            long outputsValue = getOutputValue(transaction);
            return inputsValue - outputsValue;
        }else {
            throw new RuntimeException("没有该交易类型。");
        }
    }

    /**
     * 按照费率(每字符的手续费)从大到小排序交易
     */
    public static void sortByTransactionFeeRateDescend(List<Transaction> transactions) {
        if(transactions == null){
            return;
        }
        transactions.sort((transaction1, transaction2) -> {
            long transaction1FeeRate = TransactionTool.getTransactionFeeRate(transaction1);
            long transaction2FeeRate = TransactionTool.getTransactionFeeRate(transaction2);
            long diffFeeRate = transaction1FeeRate - transaction2FeeRate;
            if(diffFeeRate>0){
                return -1;
            }else if(diffFeeRate==0){
                return 0;
            }else {
                return 1;
            }
        });
    }

    /**
     * 校验交易中的脚本是否是P2PKH脚本
     */
    public static boolean checkPayToPublicKeyHashScript(Transaction transaction) {
        List<TransactionInput> inputs = transaction.getInputs();
        if(inputs != null){
            for(TransactionInput input:inputs){
                if(!ScriptTool.isPayToPublicKeyHashInputScript(input.getInputScript())){
                    return false;
                }
            }
        }
        List<TransactionOutput> outputs = transaction.getOutputs();
        if(outputs != null){
            for (TransactionOutput output:outputs) {
                if(!ScriptTool.isPayToPublicKeyHashOutputScript(output.getOutputScript())){
                    return false;
                }
            }
        }
        return true;
    }

    public static String getTransactionOutputId(TransactionOutput transactionOutput) {
        return BlockchainDatabaseKeyTool.buildTransactionOutputId(transactionOutput.getTransactionHash(),transactionOutput.getTransactionOutputIndex());
    }
}
