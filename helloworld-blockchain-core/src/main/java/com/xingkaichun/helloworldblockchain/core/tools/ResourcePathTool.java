package com.xingkaichun.helloworldblockchain.core.tools;

import com.xingkaichun.helloworldblockchain.util.FileUtil;
import com.xingkaichun.helloworldblockchain.util.OperateSystemUtil;

/**
 * 资源路径工具类
 *
 * @author 邢开春 409060350@qq.com
 */
public class ResourcePathTool {

    /**
     * 获取区块链数据存放目录
     */
    public static String getDataRootPath() {
        String dataRootPath;
        if(OperateSystemUtil.isWindowsOperateSystem()){
            dataRootPath = "C:\\HelloworldBlockchainDataJava\\";
        }else if(OperateSystemUtil.isMacOperateSystem()){
            dataRootPath = "/tmp/HelloworldBlockchainDataJava/";
        }else{
            dataRootPath = "/opt/HelloworldBlockchainDataJava/";
        }
        FileUtil.makeDirectory(dataRootPath);
        return dataRootPath;
    }
}
