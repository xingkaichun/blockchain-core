package com.xingkaichun.helloworldblockchain.netcore.server;

import com.xingkaichun.helloworldblockchain.core.BlockchainCore;
import com.xingkaichun.helloworldblockchain.core.UnconfirmedTransactionDatabase;
import com.xingkaichun.helloworldblockchain.core.model.Block;
import com.xingkaichun.helloworldblockchain.core.tools.Model2DtoTool;
import com.xingkaichun.helloworldblockchain.netcore.dto.*;
import com.xingkaichun.helloworldblockchain.netcore.model.Node;
import com.xingkaichun.helloworldblockchain.netcore.service.NetCoreConfiguration;
import com.xingkaichun.helloworldblockchain.netcore.service.NodeService;
import com.xingkaichun.helloworldblockchain.setting.BlockSetting;
import com.xingkaichun.helloworldblockchain.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 节点请求处理器
 *
 * @author 邢开春 409060350@qq.com
 */
public class HttpServerHandlerResolver {

    private BlockchainCore blockchainCore;
    private NodeService nodeService;
    private NetCoreConfiguration netCoreConfiguration;

    public HttpServerHandlerResolver(BlockchainCore blockchainCore, NodeService nodeService, NetCoreConfiguration netCoreConfiguration) {
        this.blockchainCore = blockchainCore;
        this.nodeService = nodeService;
        this.netCoreConfiguration = netCoreConfiguration;
    }

    /**
     * Ping节点
     */
    public PingResponse ping(String requestIp, PingRequest request){
        try {
            //将ping的来路作为区块链节点
            if(netCoreConfiguration.isAutoSearchNode()){
                Node node = new Node();
                node.setIp(requestIp);
                nodeService.addNode(node);
                LogUtil.debug("发现节点["+requestIp+"]在Ping本地节点，已将发现的节点放入了节点数据库。");
            }
            PingResponse response = new PingResponse();
            return response;
        } catch (Exception e){
            String message = "ping node failed";
            LogUtil.error(message,e);
            return null;
        }
    }

    /**
     * 根据区块高度查询区块
     */
    public GetBlockResponse getBlock(GetBlockRequest request){
        try {
            Block blockByBlockHeight = blockchainCore.queryBlockByBlockHeight(request.getBlockHeight());
            BlockDto block = Model2DtoTool.block2BlockDto(blockByBlockHeight);
            GetBlockResponse response = new GetBlockResponse();
            response.setBlock(block);
            return response;
        } catch (Exception e){
            String message = "get block failed";
            LogUtil.error(message,e);
            return null;
        }
    }

    /**
     * 接收其它节点提交的交易
     */
    public PostTransactionResponse postTransaction(PostTransactionRequest request){
        try {
            blockchainCore.postTransaction(request.getTransaction());
            PostTransactionResponse response = new PostTransactionResponse();
            return response;
        } catch (Exception e){
            String message = "post transaction failed";
            LogUtil.error(message,e);
            return null;
        }
    }

    public PostBlockResponse postBlock(PostBlockRequest request) {
        try {
            BlockDto block = request.getBlock();
            blockchainCore.addBlockDto(block);
            PostBlockResponse response = new PostBlockResponse();
            return response;
        } catch (Exception e){
            String message = "post block failed";
            LogUtil.error(message,e);
            return null;
        }
    }

    public GetNodesResponse getNodes(GetNodesRequest request) {
        try {
            List<Node> nodeList = nodeService.queryAllNodes();
            if(nodeList == null){
                nodeList = new ArrayList<>();
            }
            String[] nodes = new String[nodeList.size()];
            for (int i=0;i<nodeList.size();i++){
                Node nodeEntity = nodeList.get(i);
                nodes[i] = nodeEntity.getIp();
            }
            GetNodesResponse response = new GetNodesResponse();
            response.setNodes(nodes);
            return response;
        }catch (Exception e){
            String message = "get nodes failed";
            LogUtil.error(message,e);
            return null;
        }
    }

    public PostBlockchainHeightResponse postBlockchainHeight(String requestIp, PostBlockchainHeightRequest request) {
        try {
            Node node = new Node();
            node.setIp(requestIp);
            node.setBlockchainHeight(request.getBlockchainHeight());
            nodeService.updateNode(node);

            PostBlockchainHeightResponse response = new PostBlockchainHeightResponse();
            return response;
        } catch (Exception e){
            String message = "post blockchain height failed";
            LogUtil.error(message,e);
            return null;
        }
    }

    public GetBlockchainHeightResponse getBlockchainHeight(GetBlockchainHeightRequest request) {
        try {
            long blockchainHeight = blockchainCore.queryBlockchainHeight();
            GetBlockchainHeightResponse response = new GetBlockchainHeightResponse();
            response.setBlockchainHeight(blockchainHeight);
            return response;
        } catch (Exception e){
            String message = "get blockchain height failed";
            LogUtil.error(message,e);
            return null;
        }
    }

    public GetUnconfirmedTransactionsResponse getUnconfirmedTransactions(GetUnconfirmedTransactionsRequest request) {
        try {
            UnconfirmedTransactionDatabase unconfirmedTransactionDatabase = blockchainCore.getUnconfirmedTransactionDatabase();
            List<TransactionDto> transactions = unconfirmedTransactionDatabase.selectTransactions(1, BlockSetting.BLOCK_MAX_TRANSACTION_COUNT);
            GetUnconfirmedTransactionsResponse response = new GetUnconfirmedTransactionsResponse();
            response.setTransactions(transactions);
            return response;
        } catch (Exception e){
            String message = "get transaction failed";
            LogUtil.error(message,e);
            return null;
        }
    }
}