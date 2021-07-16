package com.xingkaichun.helloworldblockchain.netcore;

import com.xingkaichun.helloworldblockchain.core.BlockchainCore;
import com.xingkaichun.helloworldblockchain.netcore.client.BlockchainNodeClientImpl;
import com.xingkaichun.helloworldblockchain.netcore.dto.PostBlockchainHeightRequest;
import com.xingkaichun.helloworldblockchain.netcore.model.Node;
import com.xingkaichun.helloworldblockchain.netcore.service.NetCoreConfiguration;
import com.xingkaichun.helloworldblockchain.netcore.service.NodeService;
import com.xingkaichun.helloworldblockchain.util.SystemUtil;
import com.xingkaichun.helloworldblockchain.util.ThreadUtil;

import java.util.List;

/**
 * 区块链高度广播器：将区块链高度传播至全网。
 * 如果本地区块链的高度高于全网，那么就应该(通过在区块链网络中广播自己的高度的方式)通知其它节点
 * ，好让其它节点知道可以来同步自己的区块数据了。
 * 至于其它节点什么时候来同步自己的区块，应该由其它节点来决定。
 *
 * 随便说一句，矿工把区块放入区块链后，当区块广播器广播区块链高度时，
 * 也就相当于通知了其它节点"自己挖出了新的区块"这件事。
 *
 * @author 邢开春 409060350@qq.com
 */
public class BlockchainHeightBroadcaster {

    NetCoreConfiguration netCoreConfiguration;
    private NodeService nodeService;
    private BlockchainCore blockchainCore;

    public BlockchainHeightBroadcaster(NetCoreConfiguration netCoreConfiguration, NodeService nodeService, BlockchainCore blockchainCore) {
        this.netCoreConfiguration = netCoreConfiguration;
        this.nodeService = nodeService;
        this.blockchainCore = blockchainCore;
    }

    public void start() {
        new Thread(()->{
            while (true){
                try {
                    broadcastBlockchainHeight();
                    ThreadUtil.millisecondSleep(netCoreConfiguration.getBlockchainHeightBroadcastTimeInterval());
                } catch (Exception e) {
                    SystemUtil.errorExit("在区块链网络中广播区块高度异常",e);
                }
            }
        }).start();
    }

    /*
     * 发现自己的区块链高度比全网节点都要高，则广播自己的区块链高度
     */
    private void broadcastBlockchainHeight() {
        List<Node> nodes = nodeService.queryAllNodes();
        if(nodes == null || nodes.size()==0){
            return;
        }

        long blockchainHeight = blockchainCore.queryBlockchainHeight();
        //按照节点的高度进行排序
        nodes.sort((Node node1, Node node2) -> {
            if (node1.getBlockchainHeight() > node2.getBlockchainHeight()) {
                return -1;
            } else if (node1.getBlockchainHeight() == node2.getBlockchainHeight()) {
                return 0;
            } else {
                return 1;
            }
        });
        /*
         * 将自己的高度传播给比自己高度低的若干个节点，好让其它节点知道可以来同步自己的区块。
         *
         * 用单线程轮询通知其它节点。
         * 这里可以利用多线程进行性能优化，因为本项目是helloworld项目，因此只采用单线程轮询每一个节点给它发送自己的高度，不做进一步优化拓展。
         * 这里需要考虑，如果你通知的节点立刻向你获取数据，需要考虑自己的宽带网络资源。
         * 这里采用只向部分节点发送的自己高度，且每给一个节点发送自己的高度后，睡眠几秒钟，可以认为这几秒带宽资源都分配给了这个节点。
         */
        //广播节点的数量
        int broadcastNodeCount = 0;
        for(Node node:nodes){
            try {
                if(blockchainHeight <= node.getBlockchainHeight()){
                    continue;
                }
                PostBlockchainHeightRequest postBlockchainHeightRequest = new PostBlockchainHeightRequest();
                postBlockchainHeightRequest.setBlockchainHeight(blockchainHeight);
                new BlockchainNodeClientImpl(node.getIp()).postBlockchainHeight(postBlockchainHeightRequest);
                ++broadcastNodeCount;
                if(broadcastNodeCount > 50){
                    return;
                }
                ThreadUtil.millisecondSleep(1000*10);
            }catch (Exception e){
                SystemUtil.errorExit("广播区块高度到节点["+node.getIp()+"]异常",e);
            }
        }
    }
}
