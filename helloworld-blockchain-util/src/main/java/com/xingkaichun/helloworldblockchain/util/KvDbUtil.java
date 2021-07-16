package com.xingkaichun.helloworldblockchain.util;

import org.iq80.leveldb.*;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.iq80.leveldb.impl.WriteBatchImpl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * KV数据库工具类
 *
 * @author 邢开春 409060350@qq.com
 */
public class KvDbUtil {

    private static Map<String,DB> dbMap = new HashMap<>();

    private static DB getDb(String dbPath) {
        synchronized (KvDbUtil.class){
            DB db = dbMap.get(dbPath);
            if(db == null){
                try {
                    DBFactory factory = new Iq80DBFactory();
                    Options options = new Options();
                    db = factory.open(new File(dbPath), options);
                } catch (IOException e) {
                    LogUtil.error("create or load LevelDB database failed. LevelDB database file path is " + dbPath + ".",e);
                    throw new RuntimeException(e);
                }
                dbMap.put(dbPath,db);
            }
            return db;
        }
    }

    public static void put(String dbPath, byte[] bytesKey, byte[] bytesValue) {
        DB db = getDb(dbPath);
        db.put(bytesKey,bytesValue);
    }
    public static void delete(String dbPath, byte[] bytesKey) {
        DB db = getDb(dbPath);
        db.delete(bytesKey);
    }
    public static byte[] get(String dbPath, byte[] bytesKey) {
        DB db = getDb(dbPath);
        return db.get(bytesKey);
    }
    public static List<byte[]> gets(String dbPath, long from, long size) {
        synchronized (KvDbUtil.class){
            List<byte[]> valueList = new ArrayList<>();
            int cunrrentFrom = 0;
            int cunrrentSize = 0;
            DB db = getDb(dbPath);
            for (DBIterator iterator = db.iterator(); iterator.hasNext(); iterator.next()) {
                byte[] byteValue = iterator.peekNext().getValue();
                if(byteValue == null || byteValue.length==0){
                    //注意，用levelDB这里确是continue
                   continue;
                }
                cunrrentFrom++;
                if(cunrrentFrom>=from && cunrrentSize<size){
                    valueList.add(byteValue);
                    cunrrentSize++;
                }
                if(cunrrentSize>=size){
                    break;
                }
            }
            return valueList;
        }
    }
    public static void write(String dbPath, KvWriteBatch kvWriteBatch) {
        DB db = getDb(dbPath);
        WriteBatch levelDbWriteBatch = toLevelDbWriteBatch(kvWriteBatch);
        db.write(levelDbWriteBatch);
    }
    private static WriteBatch toLevelDbWriteBatch(KvWriteBatch kvWriteBatch) {
        WriteBatch writeBatch = new WriteBatchImpl();
        if(kvWriteBatch != null){
            for (KvWrite kvWrite : kvWriteBatch.getKvWrites()){
                if(kvWrite.getKvWriteActionEnum() == KvWriteActionEnum.ADD){
                    writeBatch.put(kvWrite.key, kvWrite.value);
                }else if(kvWrite.getKvWriteActionEnum() == KvWriteActionEnum.DELETE){
                    writeBatch.delete(kvWrite.key);
                }else {
                    throw new RuntimeException();
                }
            }
        }
        return writeBatch;
    }



    public static class KvWriteBatch {
        private List<KvWrite> kvWrites;
        public KvWriteBatch() {
            this.kvWrites = new ArrayList<>();
        }
        public List<KvWrite> getKvWrites() {
            return kvWrites;
        }
        public void setKvWrites(List<KvWrite> kvWrites) {
            this.kvWrites = kvWrites;
        }
        public void put(byte[] key, byte[] value) {
            kvWrites.add(new KvWrite(KvWriteActionEnum.ADD,key,value));
        }
        public void delete(byte[] key) {
            kvWrites.add(new KvWrite(KvWriteActionEnum.DELETE,key,null));
        }
    }
    public static class KvWrite {
        private KvWriteActionEnum kvWriteActionEnum;
        private byte[] key;
        private byte[] value;
        public KvWrite(KvWriteActionEnum kvWriteActionEnum, byte[] key, byte[] value) {
            this.kvWriteActionEnum = kvWriteActionEnum;
            this.key = key;
            this.value = value;
        }
        public KvWriteActionEnum getKvWriteActionEnum() {
            return kvWriteActionEnum;
        }
        public byte[] getKey() {
            return key;
        }
        public byte[] getValue() {
            return value;
        }
    }
    public enum KvWriteActionEnum {
        ADD,DELETE
    }
}
