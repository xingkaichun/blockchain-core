package com.xingkaichun.helloworldblockchain.util;

import java.io.File;

/**
 * File工具类
 *
 * @author 邢开春 409060350@qq.com
 */
public class FileUtil {

    public static String newPath(String parent, String child) {
        return new File(parent,child).getAbsolutePath();
    }

    public static void makeDirectory(String path) {
        File file = new File(path);
        if(file.exists()){
            return;
        }
        boolean isMakeDirectorySuccess = file.mkdirs();
        if(!isMakeDirectorySuccess){
            throw new RuntimeException("create directory failed.");
        }
    }

    public static void deleteDirectory(String path) {
        File file = new File(path);
        if(file.isDirectory()){
            File[] childrenFiles = file.listFiles();
            for (File childFile:childrenFiles){
                deleteDirectory(childFile.getAbsolutePath());
            }
        }
        boolean isDeleteDirectorySuccess = file.delete();
        if(!isDeleteDirectorySuccess){
            throw new RuntimeException("delete directory failed.");
        }
    }
}
