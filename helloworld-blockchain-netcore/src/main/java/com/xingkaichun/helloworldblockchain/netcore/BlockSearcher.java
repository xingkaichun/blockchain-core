package com.xingkaichun.helloworldblockchain.netcore;

import com.xingkaichun.helloworldblockchain.core.BlockchainCore;
import com.xingkaichun.helloworldblockchain.core.model.Block;
import com.xingkaichun.helloworldblockchain.core.tools.BlockDtoTool;
import com.xingkaichun.helloworldblockchain.core.tools.BlockTool;
import com.xingkaichun.helloworldblockchain.netcore.client.BlockchainNodeClientImpl;
import com.xingkaichun.helloworldblockchain.netcore.dto.BlockDto;
import com.xingkaichun.helloworldblockchain.netcore.dto.GetBlockRequest;
import com.xingkaichun.helloworldblockchain.netcore.dto.GetBlockResponse;
import com.xingkaichun.helloworldblockchain.netcore.model.Node;
import com.xingkaichun.helloworldblockchain.netcore.service.NetCoreConfiguration;
import com.xingkaichun.helloworldblockchain.netcore.service.NodeService;
import com.xingkaichun.helloworldblockchain.setting.GenesisBlockSetting;
import com.xingkaichun.helloworldblockchain.util.*;

import java.util.List;

/**
 * 区块搜索器
 * 如果发现区块链网络中有可以进行同步区块的节点，则尝试同步区块放入本地区块链。
 *
 * @author 邢开春 409060350@qq.com
 */
public class BlockSearcher {

    private NetCoreConfiguration netCoreConfiguration;
    private NodeService nodeService;
    private BlockchainCore blockchainCore;
    private BlockchainCore slaveBlockchainCore;


    public BlockSearcher(NetCoreConfiguration netCoreConfiguration, NodeService nodeService
            , BlockchainCore blockchainCore, BlockchainCore slaveBlockchainCore) {
        this.netCoreConfiguration = netCoreConfiguration;
        this.nodeService = nodeService;
        this.blockchainCore = blockchainCore;
        this.slaveBlockchainCore = slaveBlockchainCore;
    }

    public void start() {
        /*
         * 同步区块
         */
        new Thread(()->{
            while (true){
                try {
                    if(netCoreConfiguration.isAutoSearchBlock()){
                        synchronizeBlocks();
                    }
                    ThreadUtil.millisecondSleep(netCoreConfiguration.getSearchBlockTimeInterval());
                } catch (Exception e) {
                    SystemUtil.errorExit("在区块链网络中同步节点的区块出现异常",e);
                }
            }
        }).start();
    }

    /**
     * 搜索新的区块，并同步这些区块到本地区块链系统
     */
    private void synchronizeBlocks() {
        if(!netCoreConfiguration.isAutoSearchBlock()){
            return;
        }
        List<Node> nodes = nodeService.queryAllNodes();
        if(nodes == null || nodes.size()==0){
            return;
        }

        long localBlockchainHeight = blockchainCore.queryBlockchainHeight();
        for(Node node:nodes){
            try {
                if(!netCoreConfiguration.isAutoSearchBlock()){
                    return;
                }
                //本地区块链高度小于远程节点区块链高度，此时需要将远程节点的区块同步到本地区块链。
                if(localBlockchainHeight < node.getBlockchainHeight()){
                    try {
                        //同步主区块链核心数据到从区块链核心
                        copyMasterBlockchainCoreToSlaveBlockchainCore(blockchainCore, slaveBlockchainCore);
                        //同步远程节点的区块到本地，未分叉同步至主链，分叉同步至从链
                        synchronizeRemoteNodeBlock(blockchainCore,slaveBlockchainCore,nodeService,node);
                        //提高主区块链核心的高度
                        promoteMasterBlockchainCore(blockchainCore, slaveBlockchainCore);
                    } catch (Exception e){
                        LogUtil.error("同步节点["+node.getIp()+"]区块到本地区块链系统出现异常",e);
                    }
                    //同步之后，本地区块链高度已经发生改变了
                    localBlockchainHeight = blockchainCore.queryBlockchainHeight();
                }
            }catch (Exception e){
                LogUtil.error("同步节点["+node.getIp()+"]的区块出现异常。",e);
            }
        }
    }

    /**
     * 使得slaveBlockchainCore和masterBlockchainCore的区块链数据一模一样
     * @param masterBlockchainCore 主区块链核心
     * @param slaveBlockchainCore 从区块链核心
     */
    private void copyMasterBlockchainCoreToSlaveBlockchainCore(BlockchainCore masterBlockchainCore,BlockchainCore slaveBlockchainCore) {
        Block masterBlockchainTailBlock = masterBlockchainCore.queryTailBlock() ;
        Block slaveBlockchainTailBlock = slaveBlockchainCore.queryTailBlock() ;
        if(masterBlockchainTailBlock == null){
            //清空slave
            slaveBlockchainCore.deleteBlocks(GenesisBlockSetting.HEIGHT);
            return;
        }
        //删除slave区块直至slave区块和master区块保持一致
        while(true){
            if(slaveBlockchainTailBlock == null){
                break;
            }
            Block masterBlockchainBlock = masterBlockchainCore.queryBlockByBlockHeight(slaveBlockchainTailBlock.getHeight());
            if(BlockTool.simpleCheckBlockEquals(masterBlockchainBlock,slaveBlockchainTailBlock)){
                break;
            }
            slaveBlockchainCore.deleteTailBlock();
            slaveBlockchainTailBlock = slaveBlockchainCore.queryTailBlock();
        }
        //复制master数据至slave
        while(true){
            long slaveBlockchainHeight = slaveBlockchainCore.queryBlockchainHeight();
            Block nextBlock = masterBlockchainCore.queryBlockByBlockHeight(slaveBlockchainHeight+1) ;
            if(nextBlock == null){
                break;
            }
            boolean isAddBlockToBlockchainSuccess = slaveBlockchainCore.addBlock(nextBlock);
            if(!isAddBlockToBlockchainSuccess){
                return;
            }
        }
    }


    /**
     * 增加主区块链的区块
     * @param masterBlockchainCore 主区块链核心
     * @param slaveBlockchainCore 从区块链核心
     */
    private void promoteMasterBlockchainCore(BlockchainCore masterBlockchainCore,
                                                 BlockchainCore slaveBlockchainCore) {
        Block masterBlockchainTailBlock = masterBlockchainCore.queryTailBlock();
        Block slaveBlockchainTailBlock = slaveBlockchainCore.queryTailBlock() ;
        //不需要调整：主区块链的高度一定大于或等于辅区块链的高度
        if(slaveBlockchainTailBlock == null){
            return;
        }
        //主高度为0，直接同步从1个区块，让主链有区块存在
        if(masterBlockchainTailBlock == null){
            Block block = slaveBlockchainCore.queryBlockByBlockHeight(GenesisBlockSetting.HEIGHT +1);
            boolean isAddBlockToBlockchainSuccess = masterBlockchainCore.addBlock(block);
            if(!isAddBlockToBlockchainSuccess){
                return;
            }
            masterBlockchainTailBlock = masterBlockchainCore.queryTailBlock();
        }
        //至此，主链、从链高度至少都为1
        //判断主链是否需要同步从链
        if(masterBlockchainTailBlock.getHeight() <= slaveBlockchainTailBlock.getHeight()){
            return;
        }

        //是否硬分叉
        long blockHeight = masterBlockchainTailBlock.getHeight();
        while (true){
            if(blockHeight <= GenesisBlockSetting.HEIGHT){
                break;
            }
            Block masterBlock = masterBlockchainCore.queryBlockByBlockHeight(blockHeight);
            Block slaveBlock = slaveBlockchainCore.queryBlockByBlockHeight(blockHeight);
            if(BlockTool.simpleCheckBlockEquals(masterBlock,slaveBlock)){
                break;
            }
            if((masterBlockchainTailBlock.getHeight() - blockHeight + 1) >= netCoreConfiguration.getForkBlockCount()){
                //硬分叉，终止。
                return;
            }
            blockHeight--;
        }


        //删除主区块链分叉区块
        long masterBlockchainTailBlockHeight = masterBlockchainTailBlock.getHeight();
        while (true){
            if(masterBlockchainTailBlockHeight <= GenesisBlockSetting.HEIGHT){
                break;
            }
            Block masterBlock = masterBlockchainCore.queryBlockByBlockHeight(masterBlockchainTailBlockHeight);
            Block slaveBlock = slaveBlockchainCore.queryBlockByBlockHeight(masterBlockchainTailBlockHeight);
            if(StringUtil.isEquals(masterBlock.getHash(),slaveBlock.getHash())){
                break;
            }
            masterBlockchainCore.deleteTailBlock();
            masterBlockchainTailBlockHeight = masterBlockchainCore.queryBlockchainHeight();
        }

        //主区块链增加区块
        while(true){
            masterBlockchainTailBlockHeight = masterBlockchainCore.queryBlockchainHeight();
            Block nextBlock = slaveBlockchainCore.queryBlockByBlockHeight(masterBlockchainTailBlockHeight+1) ;
            if(nextBlock == null){
                break;
            }
            boolean isAddBlockToBlockchainSuccess = masterBlockchainCore.addBlock(nextBlock);
            if(!isAddBlockToBlockchainSuccess){
                break;
            }
        }
    }


    /**
     * 同步远程节点的区块到本地，未分叉同步至主链，分叉同步至从链
     */
    public void synchronizeRemoteNodeBlock(BlockchainCore masterBlockchainCore, BlockchainCore slaveBlockchainCore, NodeService nodeService, Node node) {

        Block masterBlockchainCoreTailBlock = masterBlockchainCore.queryTailBlock();
        long masterBlockchainCoreTailBlockHeight = masterBlockchainCore.queryBlockchainHeight();

        //本地区块链与node区块链是否分叉？
        boolean fork = false;
        if(masterBlockchainCoreTailBlockHeight == GenesisBlockSetting.HEIGHT){
            fork = false;
        } else {
            GetBlockRequest getBlockRequest = new GetBlockRequest();
            getBlockRequest.setBlockHeight(masterBlockchainCoreTailBlockHeight);
            GetBlockResponse getBlockResponse = new BlockchainNodeClientImpl(node.getIp()).getBlock(getBlockRequest);
            if(getBlockResponse == null){
                return;
            }
            BlockDto blockDto = getBlockResponse.getBlock();
            if(blockDto == null){
                return;
            }
            String blockHash = BlockDtoTool.calculateBlockHash(blockDto);
            //没有查询到区块哈希，代表着远程节点的高度没有本地大
            if(StringUtil.isNullOrEmpty(blockHash)){
                return;
            }
            //没有分叉
            //有分叉
            fork = !StringUtil.isEquals(masterBlockchainCoreTailBlock.getHash(), blockHash);
        }

        if(fork){
            //分叉
            //分叉的高度
            long forkBlockHeight = masterBlockchainCoreTailBlockHeight;
            while (true) {
                GetBlockRequest getBlockRequest = new GetBlockRequest();
                getBlockRequest.setBlockHeight(forkBlockHeight);
                GetBlockResponse getBlockResponse = new BlockchainNodeClientImpl(node.getIp()).getBlock(getBlockRequest);
                if(getBlockResponse == null){
                    return;
                }
                BlockDto blockDto = getBlockResponse.getBlock();
                if(blockDto == null){
                    return;
                }
                String blockHash = BlockDtoTool.calculateBlockHash(blockDto);
                Block localBlock = slaveBlockchainCore.queryBlockByBlockHeight(forkBlockHeight);
                if(StringUtil.isEquals(blockHash,localBlock.getHash())){
                    break;
                }
                //分叉长度过大，不可同步。这里，认为这已经形成了硬分叉(两条完全不同的区块链)。
                if (masterBlockchainCoreTailBlockHeight > forkBlockHeight + netCoreConfiguration.getForkBlockCount()) {
                    //硬分叉了，删除该节点
                    nodeService.deleteNode(node.getIp());
                    return;
                }
                if (forkBlockHeight <= (GenesisBlockSetting.HEIGHT + 1)) {
                    //再向后已经没有区块了
                    break;
                }
                forkBlockHeight--;
            }
            //从分叉高度开始同步
            slaveBlockchainCore.deleteBlocks(forkBlockHeight);
            while (true){
                GetBlockRequest getBlockRequest = new GetBlockRequest();
                getBlockRequest.setBlockHeight(forkBlockHeight);
                GetBlockResponse getBlockResponse = new BlockchainNodeClientImpl(node.getIp()).getBlock(getBlockRequest);
                if(getBlockResponse == null){
                    return;
                }
                BlockDto blockDto = getBlockResponse.getBlock();
                if(blockDto == null){
                    return;
                }
                boolean isAddBlockSuccess = slaveBlockchainCore.addBlockDto(blockDto);
                if(!isAddBlockSuccess){
                    return;
                }
                forkBlockHeight++;

                //若是有分叉时，一次同步的最后一个区块至少要比本地区块链的高度大于N个
                if(forkBlockHeight >= (masterBlockchainCoreTailBlockHeight + netCoreConfiguration.getForkBlockCount())){
                    return;
                }
            }
        } else {
            //未分叉
            while (true){
                long nextBlockHeight = masterBlockchainCore.queryBlockchainHeight()+1;
                GetBlockRequest getBlockRequest = new GetBlockRequest();
                getBlockRequest.setBlockHeight(nextBlockHeight);
                GetBlockResponse getBlockResponse = new BlockchainNodeClientImpl(node.getIp()).getBlock(getBlockRequest);
                if(getBlockResponse == null){
                    return;
                }
                BlockDto blockDto = getBlockResponse.getBlock();
                if(blockDto == null){
                    return;
                }
                boolean isAddBlockSuccess = masterBlockchainCore.addBlockDto(blockDto);
                if(!isAddBlockSuccess){
                    return;
                }
            }
        }
    }

    public void deleteBlocks(long blockHeight) {
        blockchainCore.deleteBlocks(blockHeight);
        slaveBlockchainCore.deleteBlocks(blockHeight);
    }
}
