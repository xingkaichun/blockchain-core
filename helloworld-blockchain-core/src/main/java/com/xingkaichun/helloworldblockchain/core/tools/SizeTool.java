package com.xingkaichun.helloworldblockchain.core.tools;

import com.xingkaichun.helloworldblockchain.core.model.Block;
import com.xingkaichun.helloworldblockchain.core.model.transaction.Transaction;

/**
 * (区块、交易)大小工具类
 *
 * @author 邢开春 409060350@qq.com
 */
public class SizeTool {

    //region 校验大小
    /**
     * 校验区块大小。用来限制区块的大小。
     * 注意：校验区块的大小，不仅要校验区块的大小
     * ，还要校验区块内部各个属性(时间戳、前哈希、随机数、交易)的大小。
     */
    public static boolean checkBlockSize(Block block) {
        return DtoSizeTool.checkBlockSize(Model2DtoTool.block2BlockDto(block));
    }
    /**
     * 校验交易的大小：用来限制交易的大小。
     * 注意：校验交易的大小，不仅要校验交易的大小
     * ，还要校验交易内部各个属性(交易输入、交易输出)的大小。
     */
    public static boolean checkTransactionSize(Transaction transaction) {
        return DtoSizeTool.checkTransactionSize(Model2DtoTool.transaction2TransactionDto(transaction));
    }
    //endregion



    //region 计算大小
    public static long calculateBlockSize(Block block) {
        return DtoSizeTool.calculateBlockSize(Model2DtoTool.block2BlockDto(block));
    }
    public static long calculateTransactionSize(Transaction transaction) {
        return DtoSizeTool.calculateTransactionSize(Model2DtoTool.transaction2TransactionDto(transaction));
    }
    //endregion
}
