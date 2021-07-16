package com.xingkaichun.helloworldblockchain.application.controller;

import com.xingkaichun.helloworldblockchain.application.service.BlockchainBrowserApplicationService;
import com.xingkaichun.helloworldblockchain.application.vo.BlockchainBrowserApplicationApi;
import com.xingkaichun.helloworldblockchain.application.vo.block.*;
import com.xingkaichun.helloworldblockchain.application.vo.framwork.PageCondition;
import com.xingkaichun.helloworldblockchain.application.vo.framwork.ServiceResult;
import com.xingkaichun.helloworldblockchain.application.vo.node.QueryBlockchainHeightRequest;
import com.xingkaichun.helloworldblockchain.application.vo.node.QueryBlockchainHeightResponse;
import com.xingkaichun.helloworldblockchain.application.vo.transaction.*;
import com.xingkaichun.helloworldblockchain.core.BlockchainCore;
import com.xingkaichun.helloworldblockchain.core.model.Block;
import com.xingkaichun.helloworldblockchain.core.tools.BlockTool;
import com.xingkaichun.helloworldblockchain.core.tools.SizeTool;
import com.xingkaichun.helloworldblockchain.core.tools.TransactionDtoTool;
import com.xingkaichun.helloworldblockchain.netcore.BlockchainNetCore;
import com.xingkaichun.helloworldblockchain.netcore.dto.TransactionDto;
import com.xingkaichun.helloworldblockchain.setting.GenesisBlockSetting;
import com.xingkaichun.helloworldblockchain.util.LogUtil;
import com.xingkaichun.helloworldblockchain.util.StringUtil;
import com.xingkaichun.helloworldblockchain.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 区块链浏览器应用控制器：查询区块、交易、地址等功能。
 *
 * @author 邢开春 409060350@qq.com
 */
@RestController
public class BlockchainBrowserApplicationController {

    @Autowired
    private BlockchainNetCore blockchainNetCore;

    @Autowired
    private BlockchainCore blockchainCore;

    @Autowired
    private BlockchainBrowserApplicationService blockchainBrowserApplicationService;



    /**
     * 根据交易哈希查询交易
     */
    @RequestMapping(value = BlockchainBrowserApplicationApi.QUERY_TRANSACTION_BY_TRANSACTION_HASH,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryTransactionByTransactionHashResponse> queryTransactionByTransactionHash(@RequestBody QueryTransactionByTransactionHashRequest request){
        try {
            TransactionVo transactionVo = blockchainBrowserApplicationService.queryTransactionByTransactionHash(request.getTransactionHash());
            if(transactionVo == null){
                return ServiceResult.createFailServiceResult("根据交易哈希未能查询到交易");
            }

            QueryTransactionByTransactionHashResponse response = new QueryTransactionByTransactionHashResponse();
            response.setTransaction(transactionVo);
            return ServiceResult.createSuccessServiceResult("根据交易哈希查询交易成功",response);
        } catch (Exception e){
            String message = "根据交易哈希查询交易失败";
            LogUtil.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 根据区块哈希与交易高度查询交易列表
     */
    @RequestMapping(value = BlockchainBrowserApplicationApi.QUERY_TRANSACTIONS_BY_BLOCK_HASH_TRANSACTION_HEIGHT,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryTransactionsByBlockHashTransactionHeightResponse> queryTransactionsByBlockHashTransactionHeight(@RequestBody QueryTransactionsByBlockHashTransactionHeightRequest request){
        try {
            PageCondition pageCondition = request.getPageCondition();
            if(StringUtil.isNullOrEmpty(request.getBlockHash())){
                return ServiceResult.createFailServiceResult("区块哈希不能是空");
            }
            List<TransactionVo> transactionVos = blockchainBrowserApplicationService.queryTransactionListByBlockHashTransactionHeight(request.getBlockHash(),pageCondition.getFrom(),pageCondition.getSize());
            QueryTransactionsByBlockHashTransactionHeightResponse response = new QueryTransactionsByBlockHashTransactionHeightResponse();
            response.setTransactions(transactionVos);
            return ServiceResult.createSuccessServiceResult("根据交易高度查询交易成功",response);
        } catch (Exception e){
            String message = "根据交易高度查询交易失败";
            LogUtil.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 根据地址获取交易输出
     */
    @RequestMapping(value = BlockchainBrowserApplicationApi.QUERY_TRANSACTION_OUTPUT_BY_ADDRESS,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryTransactionOutputByAddressResponse> queryTransactionOutputByAddress(@RequestBody QueryTransactionOutputByAddressRequest request){
        try {
            TransactionOutputDetailVo transactionOutputDetailVo = blockchainBrowserApplicationService.queryTransactionOutputByAddress(request.getAddress());
            QueryTransactionOutputByAddressResponse response = new QueryTransactionOutputByAddressResponse();
            response.setTransactionOutputDetail(transactionOutputDetailVo);
            return ServiceResult.createSuccessServiceResult("[查询交易输出]成功",response);
        } catch (Exception e){
            String message = "[查询交易输出]失败";
            LogUtil.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 根据交易输出ID获取交易输出
     */
    @RequestMapping(value = BlockchainBrowserApplicationApi.QUERY_TRANSACTION_OUTPUT_BY_TRANSACTION_OUTPUT_ID,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryTransactionOutputByTransactionOutputIdResponse> queryTransactionOutputByTransactionOutputId(@RequestBody QueryTransactionOutputByTransactionOutputIdRequest request){
        try {
            TransactionOutputDetailVo transactionOutputDetailVo = blockchainBrowserApplicationService.queryTransactionOutputByTransactionOutputId(request.getTransactionHash(),request.getTransactionOutputIndex());
            QueryTransactionOutputByTransactionOutputIdResponse response = new QueryTransactionOutputByTransactionOutputIdResponse();
            response.setTransactionOutputDetail(transactionOutputDetailVo);
            return ServiceResult.createSuccessServiceResult("[查询交易输出]成功",response);
        } catch (Exception e){
            String message = "[查询交易输出]失败";
            LogUtil.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 查询区块链高度
     */
    @RequestMapping(value = BlockchainBrowserApplicationApi.QUERY_BLOCKCHAIN_HEIGHT,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryBlockchainHeightResponse> queryBlockchainHeight(@RequestBody QueryBlockchainHeightRequest request){
        try {
            long blockchainHeight = blockchainCore.queryBlockchainHeight();
            QueryBlockchainHeightResponse response = new QueryBlockchainHeightResponse();
            response.setBlockchainHeight(blockchainHeight);
            return ServiceResult.createSuccessServiceResult("查询区块链高度成功",response);
        } catch (Exception e){
            String message = "查询区块链高度失败";
            LogUtil.error(message,e);
            return ServiceResult.createSuccessServiceResult(message,null);
        }
    }

    /**
     * 根据交易哈希查询未确认交易
     */
    @RequestMapping(value = BlockchainBrowserApplicationApi.QUERY_UNCONFIRMED_TRANSACTION_BY_TRANSACTION_HASH,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryUnconfirmedTransactionByTransactionHashResponse> queryUnconfirmedTransactionByTransactionHash(@RequestBody QueryUnconfirmedTransactionByTransactionHashRequest request){
        try {
            UnconfirmedTransactionVo unconfirmedTransactionVo = blockchainBrowserApplicationService.queryUnconfirmedTransactionByTransactionHash(request.getTransactionHash());
            if(unconfirmedTransactionVo == null){
                return ServiceResult.createFailServiceResult("交易哈希["+request.getTransactionHash()+"]不是未确认交易。");
            }
            QueryUnconfirmedTransactionByTransactionHashResponse response = new QueryUnconfirmedTransactionByTransactionHashResponse();
            response.setTransaction(unconfirmedTransactionVo);
            return ServiceResult.createSuccessServiceResult("根据交易哈希查询未确认交易成功",response);
        } catch (Exception e){
            String message = "根据交易哈希查询未确认交易失败";
            LogUtil.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 查询未确认交易
     */
    @RequestMapping(value = BlockchainBrowserApplicationApi.QUERY_UNCONFIRMED_TRANSACTIONS,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryUnconfirmedTransactionsResponse> queryUnconfirmedTransactions(@RequestBody QueryUnconfirmedTransactionsRequest request){
        try {
            PageCondition pageCondition = request.getPageCondition();
            List<TransactionDto> transactionDtoList = blockchainCore.queryUnconfirmedTransactions(pageCondition.getFrom(),pageCondition.getSize());
            if(transactionDtoList == null){
                return ServiceResult.createSuccessServiceResult("未查询到未确认的交易");
            }

            List<UnconfirmedTransactionVo> transactionDtoListResp = new ArrayList<>();
            for(TransactionDto transactionDto : transactionDtoList){
                UnconfirmedTransactionVo unconfirmedTransactionVo = blockchainBrowserApplicationService.queryUnconfirmedTransactionByTransactionHash(TransactionDtoTool.calculateTransactionHash(transactionDto));
                if(unconfirmedTransactionVo != null){
                    transactionDtoListResp.add(unconfirmedTransactionVo);
                }
            }
            QueryUnconfirmedTransactionsResponse response = new QueryUnconfirmedTransactionsResponse();
            response.setTransactions(transactionDtoListResp);
            return ServiceResult.createSuccessServiceResult("查询未确认交易成功",response);
        } catch (Exception e){
            String message = "查询未确认交易失败";
            LogUtil.error(message,e);
            return ServiceResult.createSuccessServiceResult(message,null);
        }
    }

    /**
     * 根据区块高度查询区块
     */
    @RequestMapping(value = BlockchainBrowserApplicationApi.QUERY_BLOCK_BY_BLOCK_HEIGHT,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryBlockByBlockHeightResponse> queryBlockByBlockHeight(@RequestBody QueryBlockByBlockHeightRequest request){
        try {
            BlockVo blockVo = blockchainBrowserApplicationService.queryBlockViewByBlockHeight(request.getBlockHeight());
            if(blockVo == null){
                return ServiceResult.createFailServiceResult("区块链中不存在区块高度["+request.getBlockHeight()+"]，请检查输入高度。");
            }
            QueryBlockByBlockHeightResponse response = new QueryBlockByBlockHeightResponse();
            response.setBlock(blockVo);
            return ServiceResult.createSuccessServiceResult("成功获取区块",response);
        } catch (Exception e){
            String message = "查询获取失败";
            LogUtil.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 根据区块哈希查询区块
     */
    @RequestMapping(value = BlockchainBrowserApplicationApi.QUERY_BLOCK_BY_BLOCK_HASH,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryBlockByBlockHashResponse> queryBlockByBlockHash(@RequestBody QueryBlockByBlockHashRequest request){
        try {
            Block block = blockchainCore.queryBlockByBlockHash(request.getBlockHash());
            if(block == null){
                return ServiceResult.createFailServiceResult("区块链中不存在区块哈希["+request.getBlockHash()+"]，请检查输入哈希。");
            }
            BlockVo blockVo = blockchainBrowserApplicationService.queryBlockViewByBlockHeight(block.getHeight());
            QueryBlockByBlockHashResponse response = new QueryBlockByBlockHashResponse();
            response.setBlock(blockVo);
            return ServiceResult.createSuccessServiceResult("[根据区块哈希查询区块]成功",response);
        } catch (Exception e){
            String message = "[根据区块哈希查询区块]失败";
            LogUtil.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }

    /**
     * 查询最近的10个区块
     */
    @RequestMapping(value = BlockchainBrowserApplicationApi.QUERY_TOP10_BLOCKS,method={RequestMethod.GET,RequestMethod.POST})
    public ServiceResult<QueryTop10BlocksResponse> queryTop10Blocks(@RequestBody QueryTop10BlocksRequest request){
        try {
            List<Block> blockList = new ArrayList<>();
            long blockHeight = blockchainCore.queryBlockchainHeight();
            while (true){
                if(blockHeight <= GenesisBlockSetting.HEIGHT){
                    break;
                }
                Block block = blockchainCore.queryBlockByBlockHeight(blockHeight);
                blockList.add(block);
                if(blockList.size() >= 10){
                    break;
                }
                blockHeight--;
            }

            List<QueryTop10BlocksResponse.BlockVo> BlockVos = new ArrayList<>();
            for(Block block : blockList){
                QueryTop10BlocksResponse.BlockVo blockVo = new QueryTop10BlocksResponse.BlockVo();
                blockVo.setHeight(block.getHeight());
                blockVo.setBlockSize(SizeTool.calculateBlockSize(block)+"字符");
                blockVo.setTransactionCount(BlockTool.getTransactionCount(block));
                blockVo.setMinerIncentiveValue(BlockTool.getWritedIncentiveValue(block));
                blockVo.setTime(TimeUtil.formatMillisecondTimestamp(block.getTimestamp()));
                blockVo.setHash(block.getHash());
                BlockVos.add(blockVo);
            }

            QueryTop10BlocksResponse response = new QueryTop10BlocksResponse();
            response.setBlocks(BlockVos);
            return ServiceResult.createSuccessServiceResult("[查询最近的10个区块]成功",response);
        } catch (Exception e){
            String message = "[查询最近的10个区块]失败";
            LogUtil.error(message,e);
            return ServiceResult.createFailServiceResult(message);
        }
    }
}