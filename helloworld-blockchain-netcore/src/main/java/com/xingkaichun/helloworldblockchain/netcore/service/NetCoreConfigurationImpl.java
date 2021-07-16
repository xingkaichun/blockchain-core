package com.xingkaichun.helloworldblockchain.netcore.service;

import com.xingkaichun.helloworldblockchain.crypto.ByteUtil;
import com.xingkaichun.helloworldblockchain.util.FileUtil;
import com.xingkaichun.helloworldblockchain.util.KvDbUtil;

/**
 *
 * @author 邢开春 409060350@qq.com
 */
public class NetCoreConfigurationImpl implements NetCoreConfiguration {

    private String netcorePath;
    private static final String NETCORE_CONFIGURATION_DATABASE_NAME = "NetCoreConfigurationDatabase";

    //节点搜索器"是否是自动搜索新区块"状态存入到数据库时的主键
    private static final String AUTO_SEARCH_BLOCK_OPTION_KEY = "IS_AUTO_SEARCH_BLOCK";
    //节点搜索器"是否是自动搜索新区块"开关的默认状态
    private static final boolean AUTO_SEARCH_BLOCK_OPTION_DEFAULT_VALUE = true;

    //节点搜索器'是否自动搜索节点'状态存入到数据库时的主键
    private static final String AUTO_SEARCH_NODE_OPTION_KEY = "IS_AUTO_SEARCH_NODE";
    //节点搜索器'是否自动搜索节点'开关的默认状态
    private static final boolean AUTO_SEARCH_NODE_OPTION_DEFAULT_VALUE = true;

    //在区块链网络中自动搜寻新的节点的间隔时间
    private static final long SEARCH_NODE_TIME_INTERVAL = 1000 * 60 * 2;
    //在区块链网络中自动搜索节点的区块链高度
    private static final long SEARCH_BLOCKCHAIN_HEIGHT_TIME_INTERVAL = 1000 * 60 * 2;
    //在区块链网络中自动搜寻新的区块的间隔时间。
    private static final long SEARCH_BLOCKS_TIME_INTERVAL = 1000 * 60 * 2;
    //区块高度广播时间间隔
    private static final long BLOCKCHAIN_HEIGHT_BROADCASTER_TIME_INTERVAL = 1000 * 20;
    //区块广播时间间隔。
    private static final long BLOCK_BROADCASTER_TIME_INTERVAL = 1000 * 20;
    //定时将种子节点加入本地区块链网络的时间间隔。
    private static final long ADD_SEED_NODE_TIME_INTERVAL = 1000 * 60 * 2;
    //广播自己节点的时间间隔。
    private static final long NODE_BROADCAST_TIME_INTERVAL = 1000 * 60 * 2;

    //两个区块链有分叉时，区块差异数量大于这个值，则真的分叉了。
    public static final long FORK_BLOCK_COUNT = 100;

    //在区块链网络中搜寻未确认交易的间隔时间。
    public static final long SEARCH_UNCONFIRMED_TRANSACTIONS_INTERVAL = 1000 * 60 * 2;

    public NetCoreConfigurationImpl(String netcorePath) {
        FileUtil.makeDirectory(netcorePath);
        this.netcorePath = netcorePath;
    }


    @Override
    public String getNetCorePath() {
        return netcorePath;
    }

    @Override
    public boolean isAutoSearchBlock() {
        byte[] bytesConfigurationValue = getConfigurationValue(ByteUtil.stringToUtf8Bytes(AUTO_SEARCH_BLOCK_OPTION_KEY));
        if(bytesConfigurationValue == null){
            return AUTO_SEARCH_BLOCK_OPTION_DEFAULT_VALUE;
        }
        return Boolean.valueOf(ByteUtil.utf8BytesToString(bytesConfigurationValue));
    }

    @Override
    public void activeAutoSearchBlock() {
        addOrUpdateConfiguration(ByteUtil.stringToUtf8Bytes(AUTO_SEARCH_BLOCK_OPTION_KEY),ByteUtil.stringToUtf8Bytes(String.valueOf(Boolean.TRUE)));
    }

    @Override
    public void deactiveAutoSearchBlock() {
        addOrUpdateConfiguration(ByteUtil.stringToUtf8Bytes(AUTO_SEARCH_BLOCK_OPTION_KEY),ByteUtil.stringToUtf8Bytes(String.valueOf(Boolean.FALSE)));
    }

    @Override
    public boolean isAutoSearchNode() {
        byte[] bytesConfigurationValue = getConfigurationValue(ByteUtil.stringToUtf8Bytes(AUTO_SEARCH_NODE_OPTION_KEY));
        if(bytesConfigurationValue == null){
            return AUTO_SEARCH_NODE_OPTION_DEFAULT_VALUE;
        }
        return Boolean.valueOf(ByteUtil.utf8BytesToString(bytesConfigurationValue));
    }

    @Override
    public void activeAutoSearchNode() {
        addOrUpdateConfiguration(ByteUtil.stringToUtf8Bytes(AUTO_SEARCH_NODE_OPTION_KEY),ByteUtil.stringToUtf8Bytes(String.valueOf(Boolean.valueOf(true))));
    }

    @Override
    public void deactiveAutoSearchNode() {
        addOrUpdateConfiguration(ByteUtil.stringToUtf8Bytes(AUTO_SEARCH_NODE_OPTION_KEY),ByteUtil.stringToUtf8Bytes(String.valueOf(Boolean.valueOf(false))));
    }

    @Override
    public long getSearchNodeTimeInterval() {
        return SEARCH_NODE_TIME_INTERVAL;
    }

    @Override
    public long getSearchBlockchainHeightTimeInterval() {
        return SEARCH_BLOCKCHAIN_HEIGHT_TIME_INTERVAL;
    }

    @Override
    public long getSearchBlockTimeInterval() {
        return SEARCH_BLOCKS_TIME_INTERVAL;
    }

    @Override
    public long getBlockchainHeightBroadcastTimeInterval() {
        return BLOCKCHAIN_HEIGHT_BROADCASTER_TIME_INTERVAL;
    }

    @Override
    public long getBlockBroadcastTimeInterval() {
        return BLOCK_BROADCASTER_TIME_INTERVAL;
    }

    @Override
    public long getAddSeedNodeTimeInterval() {
        return ADD_SEED_NODE_TIME_INTERVAL;
    }

    @Override
    public long getNodeBroadcastTimeInterval() {
        return NODE_BROADCAST_TIME_INTERVAL;
    }

    @Override
    public long getForkBlockCount() {
        return FORK_BLOCK_COUNT;
    }

    @Override
    public long getSearchUnconfirmedTransactionsInterval() {
        return SEARCH_UNCONFIRMED_TRANSACTIONS_INTERVAL;
    }


    private byte[] getConfigurationValue(byte[] configurationKey) {
        byte[] bytesConfigurationValue = KvDbUtil.get(getNetCoreConfigurationDatabasePath(), configurationKey);
        return bytesConfigurationValue;
    }

    private void addOrUpdateConfiguration(byte[] configurationKey, byte[] configurationValue) {
        KvDbUtil.put(getNetCoreConfigurationDatabasePath(), configurationKey, configurationValue);
    }

    private String getNetCoreConfigurationDatabasePath(){
        return FileUtil.newPath(netcorePath, NETCORE_CONFIGURATION_DATABASE_NAME);
    }
}