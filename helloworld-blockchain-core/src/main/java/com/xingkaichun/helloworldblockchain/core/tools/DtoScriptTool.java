package com.xingkaichun.helloworldblockchain.core.tools;

import com.xingkaichun.helloworldblockchain.core.model.script.OperationCodeEnum;
import com.xingkaichun.helloworldblockchain.crypto.ByteUtil;
import com.xingkaichun.helloworldblockchain.netcore.dto.InputScriptDto;
import com.xingkaichun.helloworldblockchain.netcore.dto.OutputScriptDto;

/**
 * 脚本工具类
 *
 * @author 邢开春 409060350@qq.com
 */
public class DtoScriptTool {

    /**
     * 是否是P2PKH输入脚本
     */
    public static boolean isPayToPublicKeyHashInputScript(InputScriptDto inputScriptDto) {
        try {
            return  inputScriptDto.size() == 4
                    && ByteUtil.bytesToHexString(OperationCodeEnum.OP_PUSHDATA.getCode()).equals(inputScriptDto.get(0))
                    && (136 <= inputScriptDto.get(1).length() && 144 >= inputScriptDto.get(1).length())
                    && ByteUtil.bytesToHexString(OperationCodeEnum.OP_PUSHDATA.getCode()).equals(inputScriptDto.get(2))
                    && 66 == inputScriptDto.get(3).length();
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 是否是P2PKH输出脚本
     */
    public static boolean isPayToPublicKeyHashOutputScript(OutputScriptDto outputScriptDto) {
        try {
            return  outputScriptDto.size() == 6
                    && ByteUtil.bytesToHexString(OperationCodeEnum.OP_DUP.getCode()).equals(outputScriptDto.get(0))
                    && ByteUtil.bytesToHexString(OperationCodeEnum.OP_HASH160.getCode()).equals(outputScriptDto.get(1))
                    && ByteUtil.bytesToHexString(OperationCodeEnum.OP_PUSHDATA.getCode()).equals(outputScriptDto.get(2))
                    && 40 == outputScriptDto.get(3).length()
                    && ByteUtil.bytesToHexString(OperationCodeEnum.OP_EQUALVERIFY.getCode()).equals(outputScriptDto.get(4))
                    && ByteUtil.bytesToHexString(OperationCodeEnum.OP_CHECKSIG.getCode()).equals(outputScriptDto.get(5));
        }catch (Exception e){
            return false;
        }
    }

}
