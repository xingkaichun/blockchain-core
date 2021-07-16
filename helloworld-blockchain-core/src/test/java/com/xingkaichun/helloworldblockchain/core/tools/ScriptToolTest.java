package com.xingkaichun.helloworldblockchain.core.tools;

import com.xingkaichun.helloworldblockchain.core.model.script.OperationCodeEnum;
import com.xingkaichun.helloworldblockchain.crypto.ByteUtil;
import com.xingkaichun.helloworldblockchain.netcore.dto.InputScriptDto;
import com.xingkaichun.helloworldblockchain.netcore.dto.OutputScriptDto;
import com.xingkaichun.helloworldblockchain.util.JsonUtil;
import org.junit.Assert;
import org.junit.Test;


public class ScriptToolTest {

    @Test
    public void bytesScriptTest()
    {
        InputScriptDto inputScriptDto = new InputScriptDto();
        inputScriptDto.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_PUSHDATA.getCode()));
        inputScriptDto.add("955c1464982a1c904b7b1029598de6ace11bd2b1");

        InputScriptDto resumeInputScriptDto =  ScriptTool.inputScriptDto(ScriptTool.bytesScript(inputScriptDto));
        Assert.assertEquals(JsonUtil.toJson(inputScriptDto),JsonUtil.toJson(resumeInputScriptDto));



        OutputScriptDto outputScriptDto = new OutputScriptDto();
        outputScriptDto.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_DUP.getCode()));
        outputScriptDto.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_HASH160.getCode()));
        outputScriptDto.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_PUSHDATA.getCode()));
        outputScriptDto.add("955c1464982a1c904b7b1029598de6ace11bd2b1");
        outputScriptDto.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_EQUALVERIFY.getCode()));
        outputScriptDto.add(ByteUtil.bytesToHexString(OperationCodeEnum.OP_CHECKSIG.getCode()));

        OutputScriptDto resumeOutputScriptDto =  ScriptTool.outputScriptDto(ScriptTool.bytesScript(outputScriptDto));
        Assert.assertEquals(JsonUtil.toJson(outputScriptDto),JsonUtil.toJson(resumeOutputScriptDto));
    }
}
