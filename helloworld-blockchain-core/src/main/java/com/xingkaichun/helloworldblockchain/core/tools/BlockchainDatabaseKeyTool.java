package com.xingkaichun.helloworldblockchain.core.tools;

import com.xingkaichun.helloworldblockchain.crypto.ByteUtil;
import com.xingkaichun.helloworldblockchain.util.StringUtil;

/**
 * 区块链数据库主键工具类
 *
 * @author 邢开春 409060350@qq.com
 */
public class BlockchainDatabaseKeyTool {
    //区块链标识：它对应的值是区块链的高度
    private static final String BLOCKCHAIN_HEIGHT_KEY = "A";
    //区块链标识：它对应的值是区块链的交易高度
    private static final String BLOCKCHAIN_TRANSACTION_HEIGHT_KEY = "B";
    //区块链标识：它对应的值是区块链的交易输出高度
    private static final String BLOCKCHAIN_TRANSACTION_OUTPUT_HEIGHT_KEY = "C";

    //哈希标识：哈希(区块哈希、交易哈希)的前缀
    private static final String HASH_PREFIX_FLAG = "D";


    //区块标识：存储区块链高度到区块的映射
    private static final String BLOCK_HEIGHT_TO_BLOCK_PREFIX_FLAG = "E";
    //区块标识：存储区块Hash到区块高度的映射
    private static final String BLOCK_HASH_TO_BLOCK_HEIGHT_PREFIX_FLAG = "F";


    //交易标识：存储交易高度到交易的映射
    private static final String TRANSACTION_HEIGHT_TO_TRANSACTION_PREFIX_FLAG = "G";
    //交易标识：存储交易哈希到交易高度的映射
    private static final String TRANSACTION_HASH_TO_TRANSACTION_HEIGHT_PREFIX_FLAG = "H";


    //交易输出标识：存储交易输出高度到交易输出的映射
    private static final String TRANSACTION_OUTPUT_HEIGHT_TO_TRANSACTION_OUTPUT_PREFIX_FLAG = "I";
    //交易输出标识：存储交易输出ID到交易输出高度的映射
    private static final String TRANSACTION_OUTPUT_ID_TO_TRANSACTION_OUTPUT_HEIGHT_PREFIX_FLAG = "J";
    //交易输出标识：存储交易输出ID到未花费交易输出高度的映射
    private static final String TRANSACTION_OUTPUT_ID_TO_UNSPENT_TRANSACTION_OUTPUT_HEIGHT_PREFIX_FLAG = "K";
    //交易输出标识：存储交易输出ID到已花费交易输出高度的映射
    private static final String TRANSACTION_OUTPUT_ID_TO_SPENT_TRANSACTION_OUTPUT_HEIGHT_PREFIX_FLAG = "L";
    //交易输出标识：存储交易输出ID到来源交易高度的映射
    private static final String TRANSACTION_OUTPUT_ID_TO_SOURCE_TRANSACTION_HEIGHT_PREFIX_FLAG = "M";
    //交易输出标识：存储交易输出ID到花费去向交易高度的映射
    private static final String TRANSACTION_OUTPUT_ID_TO_DESTINATION_TRANSACTION_HEIGHT_PREFIX_FLAG = "N";


    //地址标识：存储地址
    private static final String ADDRESS_PREFIX_FLAG = "O";
    //地址标识：存储地址到交易输出高度的映射
    private static final String ADDRESS_TO_TRANSACTION_OUTPUT_HEIGHT_PREFIX_FLAG = "P";
    //地址标识：存储地址到未花费交易输出高度的映射
    private static final String ADDRESS_TO_UNSPENT_TRANSACTION_OUTPUT_HEIGHT_PREFIX_FLAG = "Q";
    //地址标识：存储地址到已花费交易输出高度的映射
    private static final String ADDRESS_TO_SPENT_TRANSACTION_OUTPUT_HEIGHT_PREFIX_FLAG = "R";


    //截止标识
    private static final String END_FLAG = "#" ;
    //竖线分隔符
    private static final String VERTICAL_LINE_FLAG = "|" ;




    //拼装数据库Key的值
    public static byte[] buildBlockchainHeightKey() {
        String stringKey = BLOCKCHAIN_HEIGHT_KEY + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }
    public static byte[] buildHashKey(String hash) {
        String stringKey = HASH_PREFIX_FLAG + hash + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }
    public static byte[] buildAddressKey(String address) {
        String stringKey = ADDRESS_PREFIX_FLAG + address + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }
    public static byte[] buildBlockHeightToBlockKey(long blockHeight) {
        String stringKey = BLOCK_HEIGHT_TO_BLOCK_PREFIX_FLAG + StringUtil.valueOfUint64(blockHeight) + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }
    public static byte[] buildBlockHashToBlockHeightKey(String blockHash) {
        String stringKey = BLOCK_HASH_TO_BLOCK_HEIGHT_PREFIX_FLAG + blockHash + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }
    public static byte[] buildTransactionHashToTransactionHeightKey(String transactionHash) {
        String stringKey = TRANSACTION_HASH_TO_TRANSACTION_HEIGHT_PREFIX_FLAG + transactionHash + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }
    public static byte[] buildTransactionOutputHeightToTransactionOutputKey(long transactionOutputHeight) {
        String stringKey = TRANSACTION_OUTPUT_HEIGHT_TO_TRANSACTION_OUTPUT_PREFIX_FLAG + StringUtil.valueOfUint64(transactionOutputHeight) + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }
    public static byte[] buildTransactionOutputIdToTransactionOutputHeightKey(String transactionHash,long transactionOutputIndex) {
        String transactionOutputId = buildTransactionOutputId(transactionHash, transactionOutputIndex);
        String stringKey = TRANSACTION_OUTPUT_ID_TO_TRANSACTION_OUTPUT_HEIGHT_PREFIX_FLAG + transactionOutputId + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }
    public static byte[] buildTransactionOutputIdToUnspentTransactionOutputHeightKey(String transactionHash,long transactionOutputIndex) {
        String transactionOutputId = buildTransactionOutputId(transactionHash, transactionOutputIndex);
        String stringKey = TRANSACTION_OUTPUT_ID_TO_UNSPENT_TRANSACTION_OUTPUT_HEIGHT_PREFIX_FLAG + transactionOutputId + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }
    public static byte[] buildTransactionOutputIdToSourceTransactionHeightKey(String transactionHash,long transactionOutputIndex) {
        String transactionOutputId = buildTransactionOutputId(transactionHash, transactionOutputIndex);
        String stringKey = TRANSACTION_OUTPUT_ID_TO_SOURCE_TRANSACTION_HEIGHT_PREFIX_FLAG + transactionOutputId + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }
    public static byte[] buildTransactionOutputIdToDestinationTransactionHeightKey(String transactionHash,long transactionOutputIndex) {
        String transactionOutputId = buildTransactionOutputId(transactionHash, transactionOutputIndex);
        String stringKey = TRANSACTION_OUTPUT_ID_TO_DESTINATION_TRANSACTION_HEIGHT_PREFIX_FLAG + transactionOutputId + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }
    public static byte[] buildAddressToTransactionOutputHeightKey(String address) {
        String stringKey = ADDRESS_TO_TRANSACTION_OUTPUT_HEIGHT_PREFIX_FLAG + address + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }
    public static byte[] buildAddressToUnspentTransactionOutputHeightKey(String address) {
        String stringKey = ADDRESS_TO_UNSPENT_TRANSACTION_OUTPUT_HEIGHT_PREFIX_FLAG + address + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }
    public static byte[] buildAddressToSpentTransactionOutputHeightKey(String address) {
        String stringKey = ADDRESS_TO_SPENT_TRANSACTION_OUTPUT_HEIGHT_PREFIX_FLAG + address + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }
    public static byte[] buildBlockchainTransactionHeightKey() {
        String stringKey = BLOCKCHAIN_TRANSACTION_HEIGHT_KEY + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }
    public static byte[] buildBlockchainTransactionOutputHeightKey() {
        String stringKey = BLOCKCHAIN_TRANSACTION_OUTPUT_HEIGHT_KEY + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }
    public static byte[] buildTransactionHeightToTransactionKey(long transactionHeight) {
        String stringKey = TRANSACTION_HEIGHT_TO_TRANSACTION_PREFIX_FLAG + transactionHeight + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }

    public static byte[] buildTransactionOutputIdToSpentTransactionOutputHeightKey(String transactionHash,long transactionOutputIndex) {
        String transactionOutputId = buildTransactionOutputId(transactionHash, transactionOutputIndex);
        String stringKey = TRANSACTION_OUTPUT_ID_TO_SPENT_TRANSACTION_OUTPUT_HEIGHT_PREFIX_FLAG + transactionOutputId + END_FLAG;
        return ByteUtil.stringToUtf8Bytes(stringKey);
    }

    public static String buildTransactionOutputId(String transactionHash,long transactionOutputIndex) {
        String transactionOutputId = StringUtil.concatenate3(transactionHash, VERTICAL_LINE_FLAG, StringUtil.valueOfUint64(transactionOutputIndex));
        return transactionOutputId;
    }
}