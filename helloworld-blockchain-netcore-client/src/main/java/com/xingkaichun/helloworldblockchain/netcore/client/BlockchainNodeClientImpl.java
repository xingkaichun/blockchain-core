package com.xingkaichun.helloworldblockchain.netcore.client;

import com.xingkaichun.helloworldblockchain.netcore.dto.*;
import com.xingkaichun.helloworldblockchain.setting.NetworkSetting;
import com.xingkaichun.helloworldblockchain.util.JsonUtil;
import com.xingkaichun.helloworldblockchain.util.LogUtil;
import com.xingkaichun.helloworldblockchain.util.NetUtil;

/**
 *
 * @author 邢开春 409060350@qq.com
 */
public class BlockchainNodeClientImpl implements BlockchainNodeClient {

    private String ip;

    public BlockchainNodeClientImpl(String ip) {
        this.ip = ip;
    }

    @Override
    public PostTransactionResponse postTransaction(PostTransactionRequest request) {
        try {
            String requestUrl = getUrl(API.POST_TRANSACTION);
            String requestBody = JsonUtil.toJson(request);
            String responseHtml = NetUtil.get(requestUrl,requestBody);
            return JsonUtil.fromJson(responseHtml,PostTransactionResponse.class);
        } catch (Exception e) {
            LogUtil.error("提交交易["+JsonUtil.toJson(request)+"]至节点["+ip+"]出现异常",e);
            return null;
        }
    }

    @Override
    public PingResponse pingNode(PingRequest request) {
        try {
            String requestUrl = getUrl(API.PING);
            String requestBody = JsonUtil.toJson(request);
            String responseHtml = NetUtil.get(requestUrl,requestBody);
            return JsonUtil.fromJson(responseHtml,PingResponse.class);
        } catch (Exception e) {
            LogUtil.error("Ping节点["+ip+"]出现异常",e);
            return null;
        }
    }

    @Override
    public GetBlockResponse getBlock(GetBlockRequest request) {
        try {
            String requestUrl = getUrl(API.GET_BLOCK);
            String requestBody = JsonUtil.toJson(request);
            String responseHtml = NetUtil.get(requestUrl,requestBody);
            return JsonUtil.fromJson(responseHtml,GetBlockResponse.class);
        } catch (Exception e) {
            LogUtil.error("在节点["+ip+"]查询区块出现异常",e);
            return null;
        }
    }

    @Override
    public GetNodesResponse getNodes(GetNodesRequest request) {
        try {
            String requestUrl = getUrl(API.GET_NODES);
            String requestBody = JsonUtil.toJson(request);
            String responseHtml = NetUtil.get(requestUrl,requestBody);
            return JsonUtil.fromJson(responseHtml, GetNodesResponse.class);
        } catch (Exception e) {
            LogUtil.error("在节点["+ip+"]查询节点列表出现异常",e);
            return null;
        }
    }

    @Override
    public PostBlockResponse postBlock(PostBlockRequest request) {
        try {
            String requestUrl = getUrl(API.POST_BLOCK);
            String requestBody = JsonUtil.toJson(request);
            String responseHtml = NetUtil.get(requestUrl,requestBody);
            return JsonUtil.fromJson(responseHtml,PostBlockResponse.class);
        } catch (Exception e) {
            LogUtil.error("向节点["+ip+"]提交区块出现异常",e);
            return null;
        }
    }

    @Override
    public PostBlockchainHeightResponse postBlockchainHeight(PostBlockchainHeightRequest request) {
        try {
            String requestUrl = getUrl(API.POST_BLOCKCHAIN_HEIGHT);
            String requestBody = JsonUtil.toJson(request);
            String responseHtml = NetUtil.get(requestUrl,requestBody);
            return JsonUtil.fromJson(responseHtml, PostBlockchainHeightResponse.class);
        } catch (Exception e) {
            LogUtil.error("向节点["+ip+"]提交区块链高度出现异常",e);
            return null;
        }
    }

    @Override
    public GetBlockchainHeightResponse getBlockchainHeight(GetBlockchainHeightRequest request) {
        try {
            String requestUrl = getUrl(API.GET_BLOCKCHAIN_HEIGHT);
            String requestBody = JsonUtil.toJson(request);
            String responseHtml = NetUtil.get(requestUrl,requestBody);
            return JsonUtil.fromJson(responseHtml, GetBlockchainHeightResponse.class);
        } catch (Exception e) {
            LogUtil.error("向节点["+ip+"]获取区块链高度出现异常",e);
            return null;
        }
    }

    @Override
    public GetUnconfirmedTransactionsResponse getUnconfirmedTransactions(GetUnconfirmedTransactionsRequest request) {
        try {
            String requestUrl = getUrl(API.GET_UNCONFIRMED_TRANSACTIONS);
            String requestBody = JsonUtil.toJson(request);
            String responseHtml = NetUtil.get(requestUrl,requestBody);
            return JsonUtil.fromJson(responseHtml, GetUnconfirmedTransactionsResponse.class);
        } catch (Exception e) {
            LogUtil.error("在节点["+ip+"]查询交易出现异常",e);
            return null;
        }
    }

    private String getUrl(String api) {
        return "http://" + ip + ":" + NetworkSetting.PORT + api;
    }
}