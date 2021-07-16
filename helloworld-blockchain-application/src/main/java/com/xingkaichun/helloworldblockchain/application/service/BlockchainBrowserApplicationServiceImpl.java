package com.xingkaichun.helloworldblockchain.application.service;

import com.xingkaichun.helloworldblockchain.application.vo.block.BlockVo;
import com.xingkaichun.helloworldblockchain.application.vo.transaction.*;
import com.xingkaichun.helloworldblockchain.core.BlockchainCore;
import com.xingkaichun.helloworldblockchain.core.model.Block;
import com.xingkaichun.helloworldblockchain.core.model.transaction.Transaction;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionInput;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionOutput;
import com.xingkaichun.helloworldblockchain.core.tools.*;
import com.xingkaichun.helloworldblockchain.netcore.BlockchainNetCore;
import com.xingkaichun.helloworldblockchain.netcore.dto.TransactionDto;
import com.xingkaichun.helloworldblockchain.util.LogUtil;
import com.xingkaichun.helloworldblockchain.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 邢开春 409060350@qq.com
 */
@Service
public class BlockchainBrowserApplicationServiceImpl implements BlockchainBrowserApplicationService {

    @Autowired
    private BlockchainNetCore blockchainNetCore;

    @Autowired
    private BlockchainCore blockchainCore;



    @Override
    public TransactionOutputDetailVo queryTransactionOutputByTransactionOutputId(String transactionHash,long transactionOutputIndex) {
        //查询交易输出
        TransactionOutput transactionOutput = blockchainCore.getBlockchainDatabase().queryTransactionOutputByTransactionOutputId(transactionHash,transactionOutputIndex);
        if(transactionOutput == null){
            return null;
        }

        TransactionOutputDetailVo transactionOutputDetailVo = new TransactionOutputDetailVo();
        transactionOutputDetailVo.setFromBlockHeight(transactionOutput.getBlockHeight());
        transactionOutputDetailVo.setFromBlockHash(transactionOutput.getBlockHash());
        transactionOutputDetailVo.setFromTransactionHash(transactionOutput.getTransactionHash());
        transactionOutputDetailVo.setValue(transactionOutput.getValue());
        transactionOutputDetailVo.setFromOutputScript(ScriptTool.toString(transactionOutput.getOutputScript()));
        transactionOutputDetailVo.setFromTransactionOutputIndex(transactionOutput.getTransactionOutputIndex());

        //是否是未花费输出
        TransactionOutput transactionOutputTemp = blockchainCore.getBlockchainDatabase().queryUnspentTransactionOutputByTransactionOutputId(transactionOutput.getTransactionHash(),transactionOutput.getTransactionOutputIndex());
        transactionOutputDetailVo.setSpent(transactionOutputTemp==null);

        //来源
        TransactionVo inputTransactionVo = queryTransactionByTransactionHash(transactionOutput.getTransactionHash());
        transactionOutputDetailVo.setInputTransaction(inputTransactionVo);
        transactionOutputDetailVo.setTransactionType(inputTransactionVo.getTransactionType());


        //去向
        TransactionVo outputTransactionVo;
        if(transactionOutputTemp == null){
            Transaction destinationTransaction = blockchainCore.getBlockchainDatabase().queryDestinationTransactionByTransactionOutputId(transactionOutput.getTransactionHash(),transactionOutput.getTransactionOutputIndex());
            outputTransactionVo = queryTransactionByTransactionHash(destinationTransaction.getTransactionHash());

            Transaction outputTransaction = blockchainCore.getBlockchainDatabase().queryTransactionByTransactionHash(destinationTransaction.getTransactionHash());
            List<TransactionInput> inputs = outputTransaction.getInputs();
            if(inputs != null){
                for(TransactionInput transactionInput : inputs){
                    TransactionOutput unspentTransactionOutput = transactionInput.getUnspentTransactionOutput();
                    if(transactionOutput.getTransactionHash().equals(unspentTransactionOutput.getTransactionHash()) &&
                            transactionOutput.getTransactionOutputIndex()==unspentTransactionOutput.getTransactionOutputIndex()){
                        transactionOutputDetailVo.setToTransactionInputIndex(outputTransactionVo.getTransactionInputCount());
                        transactionOutputDetailVo.setToInputScript(ScriptTool.toString(transactionInput.getInputScript()));
                        break;
                    }
                }
            }
            transactionOutputDetailVo.setToBlockHeight(outputTransactionVo.getBlockHeight());
            transactionOutputDetailVo.setToBlockHash(outputTransactionVo.getBlockHash());
            transactionOutputDetailVo.setToTransactionHash(outputTransactionVo.getTransactionHash());
            transactionOutputDetailVo.setOutputTransaction(outputTransactionVo);
        }
        return transactionOutputDetailVo;
    }

    @Override
    public TransactionOutputDetailVo queryTransactionOutputByAddress(String address) {
        TransactionOutput transactionOutput = blockchainCore.queryTransactionOutputByAddress(address);
        if(transactionOutput == null){
            return null;
        }
        TransactionOutputDetailVo transactionOutputDetailVo = queryTransactionOutputByTransactionOutputId(transactionOutput.getTransactionHash(),transactionOutput.getTransactionOutputIndex());
        return transactionOutputDetailVo;
    }

    @Override
    public List<TransactionVo> queryTransactionListByBlockHashTransactionHeight(String blockHash, long from, long size) {
        Block block = blockchainCore.queryBlockByBlockHash(blockHash);
        List<TransactionVo> transactionVos = new ArrayList<>();
        for(long i=from;i<from+size;i++){
            if(from < 0){
                break;
            }
            if(i > block.getTransactionCount()){
                break;
            }
            long transactionHeight = block.getPreviousTransactionHeight() + i;
            Transaction transaction = blockchainCore.queryTransactionByTransactionHeight(transactionHeight);
            TransactionVo transactionVo = queryTransactionByTransactionHash(transaction.getTransactionHash());
            transactionVos.add(transactionVo);
        }
        return transactionVos;
    }

    @Override
    public BlockVo queryBlockViewByBlockHeight(Long blockHeight) {
        Block block = blockchainCore.queryBlockByBlockHeight(blockHeight);
        if(block == null){
            return null;
        }
        Block nextBlock = blockchainCore.queryBlockByBlockHeight(block.getHeight()+1);

        BlockVo blockVo = new BlockVo();
        blockVo.setHeight(block.getHeight());
        blockVo.setConfirmCount(BlockTool.getTransactionCount(block));
        blockVo.setBlockSize(SizeTool.calculateBlockSize(block)+"字符");
        blockVo.setTransactionCount(BlockTool.getTransactionCount(block));
        blockVo.setTime(TimeUtil.formatMillisecondTimestamp(block.getTimestamp()));
        blockVo.setMinerIncentiveValue(BlockTool.getWritedIncentiveValue(block));
        blockVo.setDifficulty(BlockTool.formatDifficulty(block.getDifficulty()));
        blockVo.setNonce(block.getNonce());
        blockVo.setHash(block.getHash());
        blockVo.setPreviousBlockHash(block.getPreviousHash());
        blockVo.setNextBlockHash(nextBlock==null?null:nextBlock.getHash());
        blockVo.setMerkleTreeRoot(block.getMerkleTreeRoot());
        return blockVo;
    }

    @Override
    public UnconfirmedTransactionVo queryUnconfirmedTransactionByTransactionHash(String transactionHash) {

        try {
            TransactionDto transactionDto = blockchainCore.queryUnconfirmedTransactionByTransactionHash(transactionHash);
            if(transactionDto == null){
                return null;
            }
            Transaction transaction = Dto2ModelTool.transactionDto2Transaction(blockchainCore.getBlockchainDatabase(),transactionDto);
            UnconfirmedTransactionVo transactionDtoResp = new UnconfirmedTransactionVo();
            transactionDtoResp.setTransactionHash(transaction.getTransactionHash());

            List<UnconfirmedTransactionVo.TransactionInputVo> inputDtos = new ArrayList<>();
            List<TransactionInput> inputs = transaction.getInputs();
            if(inputs != null){
                for(TransactionInput input:inputs){
                    UnconfirmedTransactionVo.TransactionInputVo transactionInputVo = new UnconfirmedTransactionVo.TransactionInputVo();
                    transactionInputVo.setAddress(input.getUnspentTransactionOutput().getAddress());
                    transactionInputVo.setTransactionHash(input.getUnspentTransactionOutput().getTransactionHash());
                    transactionInputVo.setTransactionOutputIndex(input.getUnspentTransactionOutput().getTransactionOutputIndex());
                    transactionInputVo.setValue(input.getUnspentTransactionOutput().getValue());
                    inputDtos.add(transactionInputVo);
                }
            }
            transactionDtoResp.setInputs(inputDtos);

            List<UnconfirmedTransactionVo.TransactionOutputVo> outputDtos = new ArrayList<>();
            List<TransactionOutput> outputs = transaction.getOutputs();
            if(outputs != null){
                for(TransactionOutput output:outputs){
                    UnconfirmedTransactionVo.TransactionOutputVo transactionOutputVo = new UnconfirmedTransactionVo.TransactionOutputVo();
                    transactionOutputVo.setAddress(output.getAddress());
                    transactionOutputVo.setValue(output.getValue());
                    outputDtos.add(transactionOutputVo);
                }
            }
            transactionDtoResp.setOutputs(outputDtos);
            return transactionDtoResp;
        }catch (Exception e){
            LogUtil.error("根据交易哈希查询未确认交易异常",e);
            return null;
        }
    }


    @Override
    public TransactionVo queryTransactionByTransactionHash(String transactionHash) {
        Transaction transaction = blockchainCore.queryTransactionByTransactionHash(transactionHash);
        if(transaction == null){
            return null;
        }

        TransactionVo transactionVo = new TransactionVo();
        transactionVo.setTransactionHash(transaction.getTransactionHash());
        transactionVo.setBlockHeight(transaction.getBlockHeight());

        transactionVo.setTransactionFee(TransactionTool.calculateTransactionFee(transaction));
        transactionVo.setTransactionType(transaction.getTransactionType().name());
        transactionVo.setTransactionInputCount(TransactionTool.getTransactionInputCount(transaction));
        transactionVo.setTransactionOutputCount(TransactionTool.getTransactionOutputCount(transaction));
        transactionVo.setTransactionInputValues(TransactionTool.getInputValue(transaction));
        transactionVo.setTransactionOutputValues(TransactionTool.getOutputValue(transaction));

        long blockchainHeight = blockchainCore.queryBlockchainHeight();
        Block block = blockchainCore.queryBlockByBlockHeight(transaction.getBlockHeight());
        transactionVo.setConfirmCount(blockchainHeight-block.getHeight()+1);
        transactionVo.setBlockTime(TimeUtil.formatMillisecondTimestamp(block.getTimestamp()));
        transactionVo.setBlockHash(block.getHash());

        List<TransactionInput> inputs = transaction.getInputs();
        List<TransactionInputVo> transactionInputVos = new ArrayList<>();
        if(inputs != null){
            for(TransactionInput transactionInput:inputs){
                TransactionInputVo transactionInputVo = new TransactionInputVo();
                transactionInputVo.setAddress(transactionInput.getUnspentTransactionOutput().getAddress());
                transactionInputVo.setValue(transactionInput.getUnspentTransactionOutput().getValue());
                transactionInputVo.setInputScript(ScriptTool.toString(transactionInput.getInputScript()));
                transactionInputVo.setTransactionHash(transactionInput.getUnspentTransactionOutput().getTransactionHash());
                transactionInputVo.setTransactionOutputIndex(transactionInput.getUnspentTransactionOutput().getTransactionOutputIndex());
                transactionInputVos.add(transactionInputVo);
            }
        }
        transactionVo.setTransactionInputs(transactionInputVos);

        List<TransactionOutput> outputs = transaction.getOutputs();
        List<TransactionOutputVo> transactionOutputVos = new ArrayList<>();
        if(outputs != null){
            for(TransactionOutput transactionOutput:outputs){
                TransactionOutputVo transactionOutputVo = new TransactionOutputVo();
                transactionOutputVo.setAddress(transactionOutput.getAddress());
                transactionOutputVo.setValue(transactionOutput.getValue());
                transactionOutputVo.setOutputScript(ScriptTool.toString(transactionOutput.getOutputScript()));
                transactionOutputVo.setTransactionHash(transactionOutput.getTransactionHash());
                transactionOutputVo.setTransactionOutputIndex(transactionOutput.getTransactionOutputIndex());
                transactionOutputVos.add(transactionOutputVo);
            }
        }
        transactionVo.setTransactionOutputs(transactionOutputVos);

        List<String> inputScripts = new ArrayList<>();
        for (TransactionInputVo transactionInputVo : transactionInputVos){
            inputScripts.add(transactionInputVo.getInputScript());
        }
        transactionVo.setInputScripts(inputScripts);

        List<String> outputScripts = new ArrayList<>();
        for (TransactionOutputVo transactionOutputVo : transactionOutputVos){
            outputScripts.add(transactionOutputVo.getOutputScript());
        }
        transactionVo.setOutputScripts(outputScripts);
        return transactionVo;
    }
}
