package com.xingkaichun.helloworldblockchain.core.tools;

import com.xingkaichun.helloworldblockchain.netcore.dto.*;
import com.xingkaichun.helloworldblockchain.setting.BlockSetting;
import com.xingkaichun.helloworldblockchain.setting.ScriptSetting;
import com.xingkaichun.helloworldblockchain.setting.TransactionSetting;
import com.xingkaichun.helloworldblockchain.util.LogUtil;

import java.util.List;

/**
 * (区块、交易)大小工具类
 *
 * @author 邢开春 409060350@qq.com
 */
public class DtoSizeTool {

    //region 校验大小
    /**
     * 校验区块大小。用来限制区块的大小。
     * 注意：校验区块的大小，不仅要校验区块的大小
     * ，还要校验区块内部各个属性(时间戳、前哈希、随机数、交易)的大小。
     */
    public static boolean checkBlockSize(BlockDto blockDto) {
        //区块的时间戳的长度不需要校验  假设时间戳长度不正确，则在随后的业务逻辑中走不通

        //区块的前哈希的长度不需要校验  假设前哈希长度不正确，则在随后的业务逻辑中走不通

        //校验区块随机数大小
        long nonceSize = sizeOfString(blockDto.getNonce());
        if(nonceSize != BlockSetting.NONCE_CHARACTER_COUNT){
            LogUtil.debug("nonce["+blockDto.getNonce()+"]长度非法。");
            return false;
        }

        //校验每一笔交易大小
        List<TransactionDto> transactionDtoList = blockDto.getTransactions();
        if(transactionDtoList != null){
            for(TransactionDto transactionDto:transactionDtoList){
                if(!checkTransactionSize(transactionDto)){
                    LogUtil.debug("交易数据异常，交易大小非法。");
                    return false;
                }
            }
        }

        //校验区块占用的存储空间
        long blockSize = calculateBlockSize(blockDto);
        if(blockSize > BlockSetting.BLOCK_MAX_CHARACTER_COUNT){
            LogUtil.debug("区块数据的大小是["+blockSize+"]超过了限制["+BlockSetting.BLOCK_MAX_CHARACTER_COUNT +"]。");
            return false;
        }
        return true;
    }
    /**
     * 校验交易的大小：用来限制交易的大小。
     * 注意：校验交易的大小，不仅要校验交易的大小
     * ，还要校验交易内部各个属性(交易输入、交易输出)的大小。
     */
    public static boolean checkTransactionSize(TransactionDto transactionDto) {
        //校验交易输入
        List<TransactionInputDto> transactionInputDtoList = transactionDto.getInputs();
        if(transactionInputDtoList != null){
            for(TransactionInputDto transactionInputDto:transactionInputDtoList){
                //交易的未花费输出大小不需要校验  假设不正确，则在随后的业务逻辑中走不通

                //校验脚本大小
                InputScriptDto inputScriptDto = transactionInputDto.getInputScript();
                //校验输入脚本的大小
                if(!checkInputScriptSize(inputScriptDto)){
                    return false;
                }
            }
        }

        //校验交易输出
        List<TransactionOutputDto> transactionOutputDtoList = transactionDto.getOutputs();
        if(transactionOutputDtoList != null){
            for(TransactionOutputDto transactionOutputDto:transactionOutputDtoList){
                //交易输出金额大小不需要校验  假设不正确，则在随后的业务逻辑中走不通

                //校验脚本大小
                OutputScriptDto outputScriptDto = transactionOutputDto.getOutputScript();
                //校验输出脚本的大小
                if(!checkOutputScriptSize(outputScriptDto)){
                    return false;
                }

            }
        }

        //校验整笔交易大小十分合法
        long transactionSize = calculateTransactionSize(transactionDto);
        if(transactionSize > TransactionSetting.TRANSACTION_MAX_CHARACTER_COUNT){
            LogUtil.debug("交易的大小是["+transactionSize+"]，超过了限制值["+TransactionSetting.TRANSACTION_MAX_CHARACTER_COUNT +"]。");
            return false;
        }
        return true;
    }

    /**
     * 校验输入脚本的大小
     */
    private static boolean checkInputScriptSize(InputScriptDto inputScriptDto) {
        //校验脚本大小
        if(!checkScriptSize(inputScriptDto)){
            return false;
        }
        return true;
    }

    /**
     * 校验输出脚本的大小
     */
    public static boolean checkOutputScriptSize(OutputScriptDto outputScriptDto) {
        //校验脚本大小
        if(!checkScriptSize(outputScriptDto)){
            return false;
        }
        return true;
    }

    /**
     * 校验脚本的大小
     */
    public static boolean checkScriptSize(ScriptDto scriptDto) {
        //脚本内的操作码、操作数大小不需要校验，因为操作码、操作数不合规，在脚本结构上就构不成一个合格的脚本。
        if(calculateScriptSize(scriptDto) > ScriptSetting.SCRIPT_MAX_CHARACTER_COUNT){
            LogUtil.debug("交易校验失败：交易输出脚本大小超出限制。");
            return false;
        }
        return true;
    }
    //endregion



    //region 计算大小
    public static long calculateBlockSize(BlockDto blockDto) {
        long size = 0;
        long timestamp = blockDto.getTimestamp();
        size += sizeOfUint64(timestamp);

        String previousBlockHash = blockDto.getPreviousHash();
        size += sizeOfString(previousBlockHash);

        String nonce = blockDto.getNonce();
        size += sizeOfString(nonce);
        List<TransactionDto> transactionDtoList = blockDto.getTransactions();
        for(TransactionDto transactionDto:transactionDtoList){
            size += calculateTransactionSize(transactionDto);
        }
        return size;
    }
    public static long calculateTransactionSize(TransactionDto transactionDto) {
        long size = 0;
        List<TransactionInputDto> transactionInputDtoList = transactionDto.getInputs();
        size += calculateTransactionInputsSize(transactionInputDtoList);
        List<TransactionOutputDto> transactionOutputDtoList = transactionDto.getOutputs();
        size += calculateTransactionsOutputSize(transactionOutputDtoList);
        return size;
    }
    private static long calculateTransactionsOutputSize(List<TransactionOutputDto> transactionOutputDtos) {
        long size = 0;
        if(transactionOutputDtos == null || transactionOutputDtos.size()==0){
            return size;
        }
        for(TransactionOutputDto transactionOutputDto:transactionOutputDtos){
            size += calculateTransactionOutputSize(transactionOutputDto);
        }
        return size;
    }
    private static long calculateTransactionOutputSize(TransactionOutputDto transactionOutputDto) {
        long size = 0;
        OutputScriptDto outputScriptDto = transactionOutputDto.getOutputScript();
        size += calculateScriptSize(outputScriptDto);
        long value = transactionOutputDto.getValue();
        size += sizeOfUint64(value);
        return size;
    }
    private static long calculateTransactionInputsSize(List<TransactionInputDto> inputs) {
        long size = 0;
        if(inputs == null || inputs.size()==0){
            return size;
        }
        for(TransactionInputDto transactionInputDto:inputs){
            size += calculateTransactionInputSize(transactionInputDto);
        }
        return size;
    }
    private static long calculateTransactionInputSize(TransactionInputDto input) {
        long size = 0;
        String transactionHash = input.getTransactionHash();
        size += sizeOfString(transactionHash);
        long transactionOutputIndex = input.getTransactionOutputIndex();
        size += sizeOfUint64(transactionOutputIndex);
        InputScriptDto inputScriptDto = input.getInputScript();
        size += calculateScriptSize(inputScriptDto);
        return size;
    }
    private static long calculateScriptSize(ScriptDto script) {
        long size = 0;
        if(script == null || script.size()==0){
            return size;
        }
        for(String scriptCode:script){
            size += sizeOfString(scriptCode);
        }
        return size;
    }


    private static long sizeOfString(String value) {
        return value.length();
    }

    private static long sizeOfUint64(long number) {
        return String.valueOf(number).length();
    }
    //endregion
}
