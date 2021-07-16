package com.xingkaichun.helloworldblockchain.netcore;

import com.xingkaichun.helloworldblockchain.core.BlockchainCore;
import com.xingkaichun.helloworldblockchain.netcore.client.BlockchainNodeClientImpl;
import com.xingkaichun.helloworldblockchain.netcore.dto.GetUnconfirmedTransactionsRequest;
import com.xingkaichun.helloworldblockchain.netcore.dto.GetUnconfirmedTransactionsResponse;
import com.xingkaichun.helloworldblockchain.netcore.dto.TransactionDto;
import com.xingkaichun.helloworldblockchain.netcore.model.Node;
import com.xingkaichun.helloworldblockchain.netcore.service.NetCoreConfiguration;
import com.xingkaichun.helloworldblockchain.netcore.service.NodeService;
import com.xingkaichun.helloworldblockchain.util.JsonUtil;
import com.xingkaichun.helloworldblockchain.util.LogUtil;
import com.xingkaichun.helloworldblockchain.util.SystemUtil;
import com.xingkaichun.helloworldblockchain.util.ThreadUtil;

import java.util.List;

/**
 * 未确认交易搜索器
 * 搜索区块链网络中的未确认交易，放入未确认交易池。
 *
 * @author 邢开春 409060350@qq.com
 */
public class UnconfirmedTransactionsSearcher {

    private NetCoreConfiguration netCoreConfiguration;
    private NodeService nodeService;
    private BlockchainCore blockchainCore;


    public UnconfirmedTransactionsSearcher(NetCoreConfiguration netCoreConfiguration, NodeService nodeService
            , BlockchainCore blockchainCore) {
        this.netCoreConfiguration = netCoreConfiguration;
        this.nodeService = nodeService;
        this.blockchainCore = blockchainCore;
    }

    public void start() {
        new Thread(()->{
            while (true){
                try {
                    searchUnconfirmedTransactions();
                    ThreadUtil.millisecondSleep(netCoreConfiguration.getSearchUnconfirmedTransactionsInterval());
                } catch (Exception e) {
                    SystemUtil.errorExit("在区块链网络中搜寻未确认交易出现异常",e);
                }
            }
        }).start();
    }

    private void searchUnconfirmedTransactions() {
        List<Node> nodes = nodeService.queryAllNodes();
        if(nodes == null || nodes.size()==0){
            return;
        }

        for(Node node:nodes){
            try {
                GetUnconfirmedTransactionsRequest request = new GetUnconfirmedTransactionsRequest();
                GetUnconfirmedTransactionsResponse response = new BlockchainNodeClientImpl(node.getIp()).getUnconfirmedTransactions(request);
                if(response != null){
                    List<TransactionDto> transactions = response.getTransactions();
                    if(transactions != null){
                        for(TransactionDto transactionDto:transactions){
                            try {
                                blockchainCore.getUnconfirmedTransactionDatabase().insertTransaction(transactionDto);
                            }catch (Exception e){
                                LogUtil.error("交易["+JsonUtil.toJson(transactionDto)+"]放入交易池异常。",e);
                            }
                        }
                    }
                }
            }catch (Exception e){
                LogUtil.error("搜寻节点["+node.getIp()+"]的未确认交易出现异常。",e);
            }
        }
    }

}
