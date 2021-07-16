package com.xingkaichun.helloworldblockchain.setting;

/**
 * 系统版本工具
 *
 * @author 邢开春 409060350@qq.com
 */
public class SystemVersionSettingTool {

    /**
     * 校验系统版本是否支持
     */
    public static boolean checkSystemVersion(long blockHeight){
        return blockHeight <= SystemVersionSetting.BLOCK_CHAIN_VERSION_LIST[SystemVersionSetting.BLOCK_CHAIN_VERSION_LIST.length-1];
    }
}
