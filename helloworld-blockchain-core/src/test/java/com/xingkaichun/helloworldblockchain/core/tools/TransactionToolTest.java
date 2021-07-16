package com.xingkaichun.helloworldblockchain.core.tools;

import com.xingkaichun.helloworldblockchain.core.model.script.OperationCodeEnum;
import com.xingkaichun.helloworldblockchain.crypto.ByteUtil;
import com.xingkaichun.helloworldblockchain.netcore.dto.*;
import com.xingkaichun.helloworldblockchain.util.JsonUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class TransactionToolTest {

    @Test
    public void bytesTransactionOmitTrueTest()
    {
        TransactionDto transactionDto = new TransactionDto();

        TransactionDto transactionDto2 = TransactionDtoTool.transactionDto(TransactionDtoTool.bytesTransaction(transactionDto,true),true);
        Assert.assertEquals(JsonUtil.toJson(transactionDto),JsonUtil.toJson(transactionDto2));

        List<TransactionInputDto> transactionInputDtoList = new ArrayList<>();
        TransactionInputDto transactionInputDto = new TransactionInputDto();
        transactionInputDto.setTransactionHash("53b780303a801edbf75fe3463799547daf88ae152c06d16769218cec78b5d48e");
        transactionInputDto.setTransactionOutputIndex(0);
        transactionInputDtoList.add(transactionInputDto);
        transactionDto.setInputs(transactionInputDtoList);

        transactionDto2 = TransactionDtoTool.transactionDto(TransactionDtoTool.bytesTransaction(transactionDto,true),true);
        Assert.assertEquals(JsonUtil.toJson(transactionDto),JsonUtil.toJson(transactionDto2));

        List<TransactionOutputDto> transactionOutputDtoList = new ArrayList<>();
        TransactionOutputDto transactionOutputDto = new TransactionOutputDto();
        OutputScriptDto outputScriptDto = new OutputScriptDto();
        outputScriptDto.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_DUP.getCode()));
        outputScriptDto.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_HASH160.getCode()));
        outputScriptDto.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_PUSHDATA.getCode()));
        outputScriptDto.add("955c1464982a1c904b7b1029598de6ace11bd2b1");
        outputScriptDto.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_EQUALVERIFY.getCode()));
        outputScriptDto.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_CHECKSIG.getCode()));
        transactionOutputDto.setOutputScript(outputScriptDto);
        transactionOutputDto.setValue(10);
        transactionOutputDtoList.add(transactionOutputDto);
        transactionDto.setOutputs(transactionOutputDtoList);

        transactionDto2 = TransactionDtoTool.transactionDto(TransactionDtoTool.bytesTransaction(transactionDto,true),true);
        Assert.assertEquals(JsonUtil.toJson(transactionDto),JsonUtil.toJson(transactionDto2));

        TransactionInputDto transactionInputDto2 = new TransactionInputDto();
        transactionInputDto2.setTransactionHash("53b780303a801edbf75fe3463799547daf88ae152c06d16769218cec78b5d48e");
        transactionInputDto2.setTransactionOutputIndex(0);
        transactionInputDtoList.add(transactionInputDto2);

        transactionDto2 = TransactionDtoTool.transactionDto(TransactionDtoTool.bytesTransaction(transactionDto,true),true);
        Assert.assertEquals(JsonUtil.toJson(transactionDto),JsonUtil.toJson(transactionDto2));

        TransactionOutputDto transactionOutputDto2 = new TransactionOutputDto();
        OutputScriptDto outputScriptDto2 = new OutputScriptDto();
        outputScriptDto2.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_DUP.getCode()));
        outputScriptDto2.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_HASH160.getCode()));
        outputScriptDto2.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_PUSHDATA.getCode()));
        outputScriptDto2.add("955c1464982a1c904b7b1029598de6ace11bd2b1");
        outputScriptDto2.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_EQUALVERIFY.getCode()));
        outputScriptDto2.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_CHECKSIG.getCode()));
        transactionOutputDto2.setOutputScript(outputScriptDto2);
        transactionOutputDto2.setValue(20);
        transactionOutputDtoList.add(transactionOutputDto2);

        transactionDto2 = TransactionDtoTool.transactionDto(TransactionDtoTool.bytesTransaction(transactionDto,true),true);
        Assert.assertEquals(JsonUtil.toJson(transactionDto),JsonUtil.toJson(transactionDto2));
    }

    @Test
    public void bytesTransactionOmitFalseTest()
    {
        TransactionDto transactionDto = new TransactionDto();

        TransactionDto transactionDto2 = TransactionDtoTool.transactionDto(TransactionDtoTool.bytesTransaction(transactionDto,false),false);
        Assert.assertEquals(JsonUtil.toJson(transactionDto),JsonUtil.toJson(transactionDto2));

        List<TransactionInputDto> transactionInputDtoList = new ArrayList<>();
        TransactionInputDto transactionInputDto = new TransactionInputDto();
        transactionInputDto.setTransactionHash("53b780303a801edbf75fe3463799547daf88ae152c06d16769218cec78b5d48e");
        transactionInputDto.setTransactionOutputIndex(0);
        InputScriptDto inputScriptDto = new InputScriptDto();
        inputScriptDto.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_PUSHDATA.getCode()));
        inputScriptDto.add("955c1464982a1c904b7b1029598de6ace11bd2b1");
        transactionInputDto.setInputScript(inputScriptDto);
        transactionInputDtoList.add(transactionInputDto);
        transactionDto.setInputs(transactionInputDtoList);

        transactionDto2 = TransactionDtoTool.transactionDto(TransactionDtoTool.bytesTransaction(transactionDto,false),false);
        Assert.assertEquals(JsonUtil.toJson(transactionDto),JsonUtil.toJson(transactionDto2));

        List<TransactionOutputDto> transactionOutputDtoList = new ArrayList<>();
        TransactionOutputDto transactionOutputDto = new TransactionOutputDto();
        OutputScriptDto outputScriptDto = new OutputScriptDto();
        outputScriptDto.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_DUP.getCode()));
        outputScriptDto.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_HASH160.getCode()));
        outputScriptDto.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_PUSHDATA.getCode()));
        outputScriptDto.add("955c1464982a1c904b7b1029598de6ace11bd2b1");
        outputScriptDto.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_EQUALVERIFY.getCode()));
        outputScriptDto.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_CHECKSIG.getCode()));
        transactionOutputDto.setOutputScript(outputScriptDto);
        transactionOutputDto.setValue(10);
        transactionOutputDtoList.add(transactionOutputDto);
        transactionDto.setOutputs(transactionOutputDtoList);

        transactionDto2 = TransactionDtoTool.transactionDto(TransactionDtoTool.bytesTransaction(transactionDto,false),false);
        Assert.assertEquals(JsonUtil.toJson(transactionDto),JsonUtil.toJson(transactionDto2));

        TransactionInputDto transactionInputDto2 = new TransactionInputDto();
        transactionInputDto2.setTransactionHash("53b780303a801edbf75fe3463799547daf88ae152c06d16769218cec78b5d48e");
        transactionInputDto2.setTransactionOutputIndex(0);
        InputScriptDto inputScriptDto2 = new InputScriptDto();
        inputScriptDto2.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_PUSHDATA.getCode()));
        inputScriptDto2.add("955c1464982a1c904b7b1029598de6ace11bd2b1");
        transactionInputDto2.setInputScript(inputScriptDto2);
        transactionInputDtoList.add(transactionInputDto2);

        transactionDto2 = TransactionDtoTool.transactionDto(TransactionDtoTool.bytesTransaction(transactionDto,false),false);
        Assert.assertEquals(JsonUtil.toJson(transactionDto),JsonUtil.toJson(transactionDto2));

        TransactionOutputDto transactionOutputDto2 = new TransactionOutputDto();
        OutputScriptDto outputScriptDto2 = new OutputScriptDto();
        outputScriptDto2.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_DUP.getCode()));
        outputScriptDto2.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_HASH160.getCode()));
        outputScriptDto2.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_PUSHDATA.getCode()));
        outputScriptDto2.add("955c1464982a1c904b7b1029598de6ace11bd2b1");
        outputScriptDto2.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_EQUALVERIFY.getCode()));
        outputScriptDto2.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_CHECKSIG.getCode()));
        transactionOutputDto2.setOutputScript(outputScriptDto2);
        transactionOutputDto2.setValue(20);
        transactionOutputDtoList.add(transactionOutputDto2);

        transactionDto2 = TransactionDtoTool.transactionDto(TransactionDtoTool.bytesTransaction(transactionDto,false),false);
        Assert.assertEquals(JsonUtil.toJson(transactionDto),JsonUtil.toJson(transactionDto2));
    }
}
