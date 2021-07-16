package com.xingkaichun.helloworldblockchain.netcore;

import com.xingkaichun.helloworldblockchain.core.BlockchainCore;
import com.xingkaichun.helloworldblockchain.core.BlockchainCoreFactory;
import com.xingkaichun.helloworldblockchain.core.tools.ResourcePathTool;
import com.xingkaichun.helloworldblockchain.netcore.dao.NodeDao;
import com.xingkaichun.helloworldblockchain.netcore.dao.impl.NodeDaoImpl;
import com.xingkaichun.helloworldblockchain.netcore.server.BlockchainNodeHttpServer;
import com.xingkaichun.helloworldblockchain.netcore.server.HttpServerHandlerResolver;
import com.xingkaichun.helloworldblockchain.netcore.service.NetCoreConfiguration;
import com.xingkaichun.helloworldblockchain.netcore.service.NetCoreConfigurationImpl;
import com.xingkaichun.helloworldblockchain.netcore.service.NodeService;
import com.xingkaichun.helloworldblockchain.netcore.service.NodeServiceImpl;
import com.xingkaichun.helloworldblockchain.util.FileUtil;

/**
 * 区块链网络版核心工厂
 *
 * @author 邢开春 409060350@qq.com
 */
public class BlockchainNetCoreFactory {

    /**
     * 创建[区块链网络版核心]实例
     */
    public static BlockchainNetCore createDefaultBlockchainNetCore(){
        return createBlockchainNetCore(ResourcePathTool.getDataRootPath());
    }

    /**
     * 创建[区块链网络版核心]实例
     *
     * @param netcorePath 区块链数据存放位置
     */
    public static BlockchainNetCore createBlockchainNetCore(String netcorePath){
        NetCoreConfiguration netCoreConfiguration = new NetCoreConfigurationImpl(netcorePath);

        String blockchainCorePath = FileUtil.newPath(netcorePath,"BlockchainCore");
        BlockchainCore blockchainCore = BlockchainCoreFactory.createBlockchainCore(blockchainCorePath);
        String slaveBlockchainCorePath = FileUtil.newPath(netcorePath,"SlaveBlockchainCore");
        BlockchainCore slaveBlockchainCore = BlockchainCoreFactory.createBlockchainCore(slaveBlockchainCorePath);


        NodeDao nodeDao = new NodeDaoImpl(netCoreConfiguration);
        NodeService nodeService = new NodeServiceImpl(nodeDao);

        HttpServerHandlerResolver httpServerHandlerResolver = new HttpServerHandlerResolver(blockchainCore,nodeService,netCoreConfiguration);
        BlockchainNodeHttpServer blockchainNodeHttpServer = new BlockchainNodeHttpServer(httpServerHandlerResolver);

        SeedNodeInitializer seedNodeInitializer = new SeedNodeInitializer(netCoreConfiguration,nodeService);
        NodeSearcher nodeSearcher = new NodeSearcher(netCoreConfiguration,nodeService);
        NodeBroadcaster nodeBroadcaster = new NodeBroadcaster(netCoreConfiguration,nodeService);

        BlockchainHeightSearcher blockchainHeightSearcher = new BlockchainHeightSearcher(netCoreConfiguration,nodeService);
        BlockchainHeightBroadcaster blockchainHeightBroadcaster = new BlockchainHeightBroadcaster(netCoreConfiguration,nodeService,blockchainCore);

        BlockSearcher blockSearcher = new BlockSearcher(netCoreConfiguration,nodeService,blockchainCore, slaveBlockchainCore);
        BlockBroadcaster blockBroadcaster = new BlockBroadcaster(netCoreConfiguration,blockchainCore,nodeService);

        UnconfirmedTransactionsSearcher unconfirmedTransactionsSearcher = new UnconfirmedTransactionsSearcher(netCoreConfiguration,nodeService,blockchainCore);

        BlockchainNetCore blockchainNetCore
                = new BlockchainNetCore(netCoreConfiguration, blockchainCore, blockchainNodeHttpServer, nodeService
                , seedNodeInitializer, nodeSearcher, nodeBroadcaster
                , blockchainHeightSearcher, blockchainHeightBroadcaster
                , blockSearcher, blockBroadcaster
                , unconfirmedTransactionsSearcher
        );
        return blockchainNetCore;
    }
}
