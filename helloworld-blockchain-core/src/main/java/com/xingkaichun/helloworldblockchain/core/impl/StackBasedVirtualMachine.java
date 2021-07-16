package com.xingkaichun.helloworldblockchain.core.impl;

import com.xingkaichun.helloworldblockchain.core.VirtualMachine;
import com.xingkaichun.helloworldblockchain.core.model.script.BooleanCodeEnum;
import com.xingkaichun.helloworldblockchain.core.model.script.OperationCodeEnum;
import com.xingkaichun.helloworldblockchain.core.model.script.Script;
import com.xingkaichun.helloworldblockchain.core.model.script.ScriptExecuteResult;
import com.xingkaichun.helloworldblockchain.core.model.transaction.Transaction;
import com.xingkaichun.helloworldblockchain.core.tools.TransactionTool;
import com.xingkaichun.helloworldblockchain.crypto.AccountUtil;
import com.xingkaichun.helloworldblockchain.crypto.ByteUtil;
import com.xingkaichun.helloworldblockchain.util.StringUtil;

/**
 * 基于栈的虚拟机
 *
 * @author 邢开春 409060350@qq.com
 */
public class StackBasedVirtualMachine extends VirtualMachine {

    @Override
    public ScriptExecuteResult executeScript(Transaction transactionEnvironment, Script script) throws RuntimeException {
        ScriptExecuteResult stack = new ScriptExecuteResult();

        for(int i=0;i<script.size();i++){
            String operationCode = script.get(i);
            byte[] bytesOperationCode = ByteUtil.hexStringToBytes(operationCode);
            if(ByteUtil.isEquals(OperationCodeEnum.OP_DUP.getCode(),bytesOperationCode)){
                if(stack.size()<1){
                    throw new RuntimeException("指令运行异常");
                }
                stack.push(stack.peek());
            }else if(ByteUtil.isEquals(OperationCodeEnum.OP_HASH160.getCode(),bytesOperationCode)){
                if(stack.size()<1){
                    throw new RuntimeException("指令运行异常");
                }
                String publicKey = stack.pop();
                String publicKeyHash = AccountUtil.publicKeyHashFromPublicKey(publicKey);
                stack.push(publicKeyHash);
            }else if(ByteUtil.isEquals(OperationCodeEnum.OP_EQUALVERIFY.getCode(),bytesOperationCode)){
                if(stack.size()<2){
                    throw new RuntimeException("指令运行异常");
                }
                if(!StringUtil.isEquals(stack.pop(),stack.pop())){
                    throw new RuntimeException("脚本执行失败");
                }
            }else if(ByteUtil.isEquals(OperationCodeEnum.OP_CHECKSIG.getCode(),bytesOperationCode)){
                if(stack.size()<2){
                    throw new RuntimeException("指令运行异常");
                }
                String publicKey = stack.pop();
                String signature = stack.pop();
                String message = TransactionTool.signatureHashAll(transactionEnvironment);
                byte[] bytesMessage = ByteUtil.hexStringToBytes(message);
                byte[] bytesSignature = ByteUtil.hexStringToBytes(signature);
                boolean verifySignatureSuccess = AccountUtil.verifySignature(publicKey,bytesMessage,bytesSignature);
                if(!verifySignatureSuccess){
                    throw new RuntimeException("脚本执行失败");
                }
                stack.push(ByteUtil.bytesToHexString(BooleanCodeEnum.TRUE.getCode()));
            }else if(ByteUtil.isEquals(OperationCodeEnum.OP_PUSHDATA.getCode(),bytesOperationCode)){
                if(script.size()<i+2){
                    throw new RuntimeException("指令运行异常");
                }
                ++i;
                stack.push(script.get(i));
            }else {
                throw new RuntimeException("不能识别的操作码");
            }
        }
        return stack;
    }
}
