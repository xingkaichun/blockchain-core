package com.xingkaichun.helloworldblockchain.netcore;

import com.xingkaichun.helloworldblockchain.netcore.client.BlockchainNodeClientImpl;
import com.xingkaichun.helloworldblockchain.netcore.dto.GetBlockchainHeightRequest;
import com.xingkaichun.helloworldblockchain.netcore.dto.GetBlockchainHeightResponse;
import com.xingkaichun.helloworldblockchain.netcore.model.Node;
import com.xingkaichun.helloworldblockchain.netcore.service.NetCoreConfiguration;
import com.xingkaichun.helloworldblockchain.netcore.service.NodeService;
import com.xingkaichun.helloworldblockchain.util.LogUtil;
import com.xingkaichun.helloworldblockchain.util.SystemUtil;
import com.xingkaichun.helloworldblockchain.util.ThreadUtil;

import java.util.List;


/**
 * 区块链高度搜索器
 * 为什么要搜索节点的高度？
 * 在我的设计之中，本节点已知的所有节点的信息(ip、高度等)都持久化在本地，区块链高度搜索器定时的更新已知节点的高度。
 * 区块搜寻器BlockSearcher定时的用本地区块链高度与已知节点高度(存储在本地的高度)作比较
 * ，若本地区块链高度较小，本地区块链则去同步远程节点的区块。
 *
 * @author 邢开春 409060350@qq.com
 */
public class BlockchainHeightSearcher {

    private NetCoreConfiguration netCoreConfiguration;
    private NodeService nodeService;

    public BlockchainHeightSearcher(NetCoreConfiguration netCoreConfiguration, NodeService nodeService) {

        this.netCoreConfiguration = netCoreConfiguration;
        this.nodeService = nodeService;
    }

    public void start() {
        new Thread(()->{
            while (true){
                try {
                    searchBlockchainHeight();
                    ThreadUtil.millisecondSleep(netCoreConfiguration.getSearchBlockchainHeightTimeInterval());
                } catch (Exception e) {
                    SystemUtil.errorExit("在区块链网络中搜索节点的高度异常",e);
                }
            }
        }).start();
    }

    private void searchBlockchainHeight() {
        List<Node> nodes = nodeService.queryAllNodes();
        for(Node node:nodes){
            try {
                GetBlockchainHeightRequest getBlockchainHeightRequest = new GetBlockchainHeightRequest();
                GetBlockchainHeightResponse getBlockchainHeightResponse = new BlockchainNodeClientImpl(node.getIp()).getBlockchainHeight(getBlockchainHeightRequest);
                if(getBlockchainHeightResponse != null){
                    node.setBlockchainHeight(getBlockchainHeightResponse.getBlockchainHeight());
                    nodeService.updateNode(node);
                }
            }catch (Exception e){
                LogUtil.error("搜索节点["+node.getIp()+"]的高度异常。",e);
            }
        }
    }
}
