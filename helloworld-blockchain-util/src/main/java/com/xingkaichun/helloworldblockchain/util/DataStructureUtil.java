package com.xingkaichun.helloworldblockchain.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author 邢开春 409060350@qq.com
 */
public class DataStructureUtil {

    public static boolean isExistDuplicateElement(List<String> list) {
        Set<String> set = new HashSet<>(list);
        return list.size() != set.size();
    }

}
