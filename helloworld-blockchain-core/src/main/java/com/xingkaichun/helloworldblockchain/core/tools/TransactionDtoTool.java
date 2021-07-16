package com.xingkaichun.helloworldblockchain.core.tools;

import com.xingkaichun.helloworldblockchain.crypto.AccountUtil;
import com.xingkaichun.helloworldblockchain.crypto.ByteUtil;
import com.xingkaichun.helloworldblockchain.crypto.Sha256Util;
import com.xingkaichun.helloworldblockchain.netcore.dto.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TransactionDto工具类
 *
 * @author 邢开春 409060350@qq.com
 */
public class TransactionDtoTool {

    /**
     * 获取待签名数据
     */
    public static String signatureHashAll(TransactionDto transactionDto) {
        byte[] bytesTransaction = bytesTransaction(transactionDto,true);
        byte[] sha256Digest = Sha256Util.doubleDigest(bytesTransaction);
        return ByteUtil.bytesToHexString(sha256Digest);
    }

    /**
     * 交易签名
     */
    public static String signature(String privateKey, TransactionDto transactionDto) {
        String signatureHashAll = signatureHashAll(transactionDto);
        byte[] bytesSignatureHashAll = ByteUtil.hexStringToBytes(signatureHashAll);
        String signature = AccountUtil.signature(privateKey,bytesSignatureHashAll);
        return signature;
    }


    public static String calculateTransactionHash(TransactionDto transactionDto){
        byte[] bytesTransaction = bytesTransaction(transactionDto,false);
        byte[] bytesTransactionHash = Sha256Util.doubleDigest(bytesTransaction);
        return ByteUtil.bytesToHexString(bytesTransactionHash);
    }

    //region 序列化与反序列化
    /**
     * 序列化。将交易转换为字节数组，要求生成的字节数组反过来能还原为原始交易。
     */
    public static byte[] bytesTransaction(TransactionDto transactionDto, boolean omitInputScript) {
        List<byte[]> bytesUnspentTransactionOutputs = new ArrayList<>();
        List<TransactionInputDto> inputs = transactionDto.getInputs();
        if(inputs != null){
            for(TransactionInputDto transactionInputDto:inputs){
                byte[] bytesUnspentTransactionOutput;
                if(omitInputScript){
                    byte[] bytesTransactionHash = ByteUtil.hexStringToBytes(transactionInputDto.getTransactionHash());
                    byte[] bytesTransactionOutputIndex = ByteUtil.uint64ToBytes(transactionInputDto.getTransactionOutputIndex());
                    bytesUnspentTransactionOutput = ByteUtil.concatenate(ByteUtil.concatenateLength(bytesTransactionHash),
                            ByteUtil.concatenateLength(bytesTransactionOutputIndex));
                }else {
                    byte[] bytesTransactionHash = ByteUtil.hexStringToBytes(transactionInputDto.getTransactionHash());
                    byte[] bytesTransactionOutputIndex = ByteUtil.uint64ToBytes(transactionInputDto.getTransactionOutputIndex());
                    byte[] bytesInputScript = ScriptTool.bytesScript(transactionInputDto.getInputScript());
                    bytesUnspentTransactionOutput = ByteUtil.concatenate3(ByteUtil.concatenateLength(bytesTransactionHash),
                            ByteUtil.concatenateLength(bytesTransactionOutputIndex),ByteUtil.concatenateLength(bytesInputScript));
                }
                bytesUnspentTransactionOutputs.add(ByteUtil.concatenateLength(bytesUnspentTransactionOutput));
            }
        }

        List<byte[]> bytesTransactionOutputs = new ArrayList<>();
        List<TransactionOutputDto> outputs = transactionDto.getOutputs();
        if(outputs != null){
            for(TransactionOutputDto transactionOutputDto:outputs){
                byte[] bytesOutputScript = ScriptTool.bytesScript(transactionOutputDto.getOutputScript());
                byte[] bytesValue = ByteUtil.uint64ToBytes(transactionOutputDto.getValue());
                byte[] bytesTransactionOutput = ByteUtil.concatenate(ByteUtil.concatenateLength(bytesOutputScript),ByteUtil.concatenateLength(bytesValue));
                bytesTransactionOutputs.add(ByteUtil.concatenateLength(bytesTransactionOutput));
            }
        }

        byte[] data = ByteUtil.concatenate(ByteUtil.flatAndConcatenateLength(bytesUnspentTransactionOutputs),
                ByteUtil.flatAndConcatenateLength(bytesTransactionOutputs));
        return data;
    }
    /**
     * 反序列化。将字节数组转换为交易。
     */
    public static TransactionDto transactionDto(byte[] bytesTransaction, boolean omitInputScript) {
        TransactionDto transactionDto = new TransactionDto();
        int start = 0;
        long bytesTransactionInputDtosLength = ByteUtil.bytesToUint64(Arrays.copyOfRange(bytesTransaction,start,start + ByteUtil.BYTE8_BYTE_COUNT));
        start += ByteUtil.BYTE8_BYTE_COUNT;
        byte[] bytesTransactionInputDtos = Arrays.copyOfRange(bytesTransaction,start, start+(int) bytesTransactionInputDtosLength);
        start += bytesTransactionInputDtosLength;
        List<TransactionInputDto> transactionInputDtos = transactionInputDtos(bytesTransactionInputDtos,omitInputScript);
        transactionDto.setInputs(transactionInputDtos);

        long bytesTransactionOutputsLength = ByteUtil.bytesToUint64(Arrays.copyOfRange(bytesTransaction,start,start + ByteUtil.BYTE8_BYTE_COUNT));
        start += ByteUtil.BYTE8_BYTE_COUNT;
        byte[] bytesTransactionOutputs = Arrays.copyOfRange(bytesTransaction,start, start+(int) bytesTransactionOutputsLength);
        start += bytesTransactionOutputsLength;
        List<TransactionOutputDto> transactionOutputDtos = transactionOutputDtos(bytesTransactionOutputs);
        transactionDto.setOutputs(transactionOutputDtos);
        return transactionDto;
    }
    private static List<TransactionOutputDto> transactionOutputDtos(byte[] bytesTransactionOutputs) {
        if(bytesTransactionOutputs == null || bytesTransactionOutputs.length == 0){
            return null;
        }
        int start = 0;
        List<TransactionOutputDto> transactionOutputDtos = new ArrayList<>();
        while (start < bytesTransactionOutputs.length){
            long bytesTransactionOutputDtoLength = ByteUtil.bytesToUint64(Arrays.copyOfRange(bytesTransactionOutputs,start,start + ByteUtil.BYTE8_BYTE_COUNT));
            start += ByteUtil.BYTE8_BYTE_COUNT;
            byte[] bytesTransactionOutput = Arrays.copyOfRange(bytesTransactionOutputs,start, start+(int) bytesTransactionOutputDtoLength);
            start += bytesTransactionOutputDtoLength;
            TransactionOutputDto transactionOutputDto = transactionOutputDto(bytesTransactionOutput);
            transactionOutputDtos.add(transactionOutputDto);
            if(start >= bytesTransactionOutputs.length){
                break;
            }
        }
        return transactionOutputDtos;
    }
    private static TransactionOutputDto transactionOutputDto(byte[] bytesTransactionOutput) {
        int start = 0;
        long bytesOutputScriptLength = ByteUtil.bytesToUint64(Arrays.copyOfRange(bytesTransactionOutput,start,start + ByteUtil.BYTE8_BYTE_COUNT));
        start += ByteUtil.BYTE8_BYTE_COUNT;
        byte[] bytesOutputScript = Arrays.copyOfRange(bytesTransactionOutput,start, start+(int) bytesOutputScriptLength);
        start += bytesOutputScriptLength;
        OutputScriptDto outputScriptDto = ScriptTool.outputScriptDto(bytesOutputScript);

        long bytesValueLength = ByteUtil.bytesToUint64(Arrays.copyOfRange(bytesTransactionOutput,start,start + ByteUtil.BYTE8_BYTE_COUNT));
        start += ByteUtil.BYTE8_BYTE_COUNT;
        byte[] bytesValue = Arrays.copyOfRange(bytesTransactionOutput,start, start+(int) bytesValueLength);
        start += bytesValueLength;

        TransactionOutputDto transactionOutputDto = new TransactionOutputDto();
        transactionOutputDto.setOutputScript(outputScriptDto);
        transactionOutputDto.setValue(ByteUtil.bytesToUint64(bytesValue));
        return transactionOutputDto;
    }
    private static List<TransactionInputDto> transactionInputDtos(byte[] bytesTransactionInputDtos, boolean omitInputScript) {
        if(bytesTransactionInputDtos == null || bytesTransactionInputDtos.length == 0){
            return null;
        }
        int start = 0;
        List<TransactionInputDto> transactionInputDtos = new ArrayList<>();
        while (start < bytesTransactionInputDtos.length){
            long bytesTransactionInputDtoLength = ByteUtil.bytesToUint64(Arrays.copyOfRange(bytesTransactionInputDtos,start,start + ByteUtil.BYTE8_BYTE_COUNT));
            start += ByteUtil.BYTE8_BYTE_COUNT;
            byte[] bytesTransactionInput = Arrays.copyOfRange(bytesTransactionInputDtos,start, start+(int) bytesTransactionInputDtoLength);
            start += bytesTransactionInputDtoLength;
            TransactionInputDto transactionInputDto = transactionInputDto(bytesTransactionInput,omitInputScript);
            transactionInputDtos.add(transactionInputDto);
            if(start >= bytesTransactionInputDtos.length){
                break;
            }
        }
        return transactionInputDtos;
    }
    private static TransactionInputDto transactionInputDto(byte[] bytesTransactionInputDto, boolean omitInputScript) {
        int start = 0;
        long bytesTransactionHashLength = ByteUtil.bytesToUint64(Arrays.copyOfRange(bytesTransactionInputDto,start,start + ByteUtil.BYTE8_BYTE_COUNT));
        start += ByteUtil.BYTE8_BYTE_COUNT;
        byte[] bytesTransactionHash = Arrays.copyOfRange(bytesTransactionInputDto,start, start+(int) bytesTransactionHashLength);
        start += bytesTransactionHashLength;

        long bytesTransactionOutputIndexLength = ByteUtil.bytesToUint64(Arrays.copyOfRange(bytesTransactionInputDto,start,start + ByteUtil.BYTE8_BYTE_COUNT));
        start += ByteUtil.BYTE8_BYTE_COUNT;
        byte[] bytesTransactionOutputIndex = Arrays.copyOfRange(bytesTransactionInputDto,start, start+(int) bytesTransactionOutputIndexLength);
        start += bytesTransactionOutputIndexLength;

        TransactionInputDto transactionInputDto = new TransactionInputDto();
        if(!omitInputScript){
            long bytesOutputScriptLength = ByteUtil.bytesToUint64(Arrays.copyOfRange(bytesTransactionInputDto,start,start + ByteUtil.BYTE8_BYTE_COUNT));
            start += ByteUtil.BYTE8_BYTE_COUNT;
            byte[] bytesOutputScript = Arrays.copyOfRange(bytesTransactionInputDto,start, start+(int) bytesOutputScriptLength);
            start += bytesOutputScriptLength;
            InputScriptDto inputScriptDto = ScriptTool.inputScriptDto(bytesOutputScript);
            transactionInputDto.setInputScript(inputScriptDto);
        }
        transactionInputDto.setTransactionHash(ByteUtil.bytesToHexString(bytesTransactionHash));
        transactionInputDto.setTransactionOutputIndex(ByteUtil.bytesToUint64(bytesTransactionOutputIndex));
        return transactionInputDto;
    }
    //endregion
}
