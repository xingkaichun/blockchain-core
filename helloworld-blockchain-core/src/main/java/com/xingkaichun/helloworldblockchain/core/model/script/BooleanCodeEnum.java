package com.xingkaichun.helloworldblockchain.core.model.script;

/**
 * 布尔枚举
 *
 * @author 邢开春 409060350@qq.com
 */
public enum BooleanCodeEnum {
    FALSE(new byte[]{(byte)0x00}),
    TRUE(new byte[]{(byte)0x01});

    private byte[] code;
    BooleanCodeEnum(byte[] code) {
        this.code = code;
    }

    public byte[] getCode() {
        return code;
    }
}
