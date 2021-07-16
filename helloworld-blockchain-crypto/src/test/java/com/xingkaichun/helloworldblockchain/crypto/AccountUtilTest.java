package com.xingkaichun.helloworldblockchain.crypto;

import com.xingkaichun.helloworldblockchain.crypto.model.Account;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.junit.Test;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Random;

import static org.bitcoinj.core.Utils.HEX;
import static org.junit.Assert.*;

public class AccountUtilTest {

    private static final Random RANDOM = new Random();

    private static final Account ACCOUNT_1 = new Account("d5c654eeff2cf1c6af16d721f31854ef447dfc61a6344bf1ba7108ec77d29b80"
            ,"024a99261b7a4b4bad2cac46defa41936b5807bf35dc5d7645d4976431666c741f"
            ,"6f858467e5648287a419e05d6f1c317d871c0bc1"
            ,"1BAfxES4BAQdnKVTJSfAjcKRxh3CsYMN4Y"
    );

    @Test
    public void randomAccountTest()
    {
        Account account = AccountUtil.randomAccount();
        assertAccount(account);
    }

    @Test
    public void accountFromPrivateKeyTest()
    {
        Account account = AccountUtil.accountFromPrivateKey(ACCOUNT_1.getPrivateKey());
        assertEquals(ACCOUNT_1.getPrivateKey(),account.getPrivateKey());
        assertEquals(ACCOUNT_1.getPublicKey(),account.getPublicKey());
        assertEquals(ACCOUNT_1.getAddress(),account.getAddress());
        assertAccount(account);
    }

    @Test
    public void addressFromPublicKeyTest()
    {
        String address = AccountUtil.addressFromPublicKey(ACCOUNT_1.getPublicKey());
        assertEquals(ACCOUNT_1.getAddress(),address);
    }

    @Test
    public void signatureTest()
    {
        verifySignatureTest();
    }

    //来源org.bitcoinj.core.ECKeyTest.testSignatures()
    @Test
    public void verifySignatureTest()
    {
        byte[] zeroValueWith32Byte = new byte[32];
        Account account = AccountUtil.accountFromPrivateKey("180cb41c7c600be951b5d3d0a7334acc7506173875834f7a6c4c786a28fcbb19");
        byte[] signatureByAccount = signature0Proxy(decodePrivateKey0Proxy(account.getPrivateKey()),zeroValueWith32Byte);
        boolean verifySignatureByAccount = verifySignature0Proxy(decodePublicKey0Proxy(account.getPublicKey()),zeroValueWith32Byte,signatureByAccount);
        assertTrue(verifySignatureByAccount);

        byte[] signatureByFixValue = HEX.decode("3045022100cfd454a1215fdea463201a7a32c146c1cec54b60b12d47e118a2add41366cec602203e7875d23cc80f958e45298bb8369d4422acfbc1c317353eebe02c89206b3e73");
        boolean verifySignatureByFixValue = verifySignature0Proxy(decodePublicKey0Proxy(account.getPublicKey()),zeroValueWith32Byte,signatureByFixValue);
        assertTrue(verifySignatureByFixValue);
    }

    private void randomAssertSignature() throws Exception{
        Account account = AccountUtil.randomAccount();
        ECKey bitcoinjECKey = ECKey.fromPrivate(decodePrivateKey0Proxy(account.getPrivateKey()),true);

        //随机生成32位数组字节
        byte[] randomByte = new byte[32];
        RANDOM.nextBytes(randomByte);
        Sha256Hash randomSha256Hash = Sha256Hash.wrap(randomByte);

        byte[] signByAccount = signature0Proxy(decodePrivateKey0Proxy(account.getPrivateKey()),randomByte);
        byte[] signByAccount2 = signature0Proxy(decodePrivateKey0Proxy(account.getPrivateKey()),randomByte);
        //校验同一私钥，两次签名是否一致
        assertArrayEquals(signByAccount,signByAccount2);
        //校验签名是否正确
        assertTrue(verifySignature0Proxy(decodePublicKey0Proxy(account.getPublicKey()),randomByte,signByAccount));
        //用bitcoinj校验签名是否正确
        assert(bitcoinjECKey.verify(randomSha256Hash.getBytes(), signByAccount));


        byte[] signByBitcoinj = bitcoinjECKey.sign(randomSha256Hash).encodeToDER();
        ECKey bitcoinjECKey2 = ECKey.fromPrivate(decodePrivateKey0Proxy(account.getPrivateKey()),true);
        byte[] signByBitcoinj2 = bitcoinjECKey2.sign(randomSha256Hash).encodeToDER();
        //校验同一私钥，两次bitcoinj签名是否一致
        assertArrayEquals(signByBitcoinj,signByBitcoinj2);
        //bitcoinj校验bitcoinj签名是否正确
        assert(bitcoinjECKey.verify(randomSha256Hash.getBytes(), signByBitcoinj));
        //校验bitcoinj签名是否正确
        assertTrue(verifySignature0Proxy(decodePublicKey0Proxy(account.getPublicKey()),randomByte,signByBitcoinj));

        //校验同一私钥，签名是否与bitcoinj签名
        assertArrayEquals(signByAccount,signByBitcoinj);
    }

    @Test
    public void allTest() throws Exception
    {
        assertAccount(ACCOUNT_1);

        for (int i=0;i<10000;i++){
            Account account = AccountUtil.randomAccount();
            assertAccount(account);
        }

        for (int i=0;i<10000;i++){
            Account account = AccountUtil.accountFromPrivateKey(randomPrivateKey());
            assertAccount(account);
        }

        for(int i=0;i<10000;i++){
            randomAssertSignature();
        }
    }


    private static void assertAccount(Account account){
        Account accountFromPrivateKey = AccountUtil.accountFromPrivateKey(account.getPrivateKey());
        assertEquals(account.getPrivateKey(),accountFromPrivateKey.getPrivateKey());
        assertEquals(account.getPublicKey(),accountFromPrivateKey.getPublicKey());
        assertEquals(account.getAddress(),accountFromPrivateKey.getAddress());

        //用bitcoinj解析账户，并对比私钥、公钥、地址
        ECKey bitcoinjECKey = ECKey.fromPrivate(decodePrivateKey0Proxy(account.getPrivateKey()),true);
        assertEquals(account.getPrivateKey(),bitcoinjECKey.getPrivateKeyAsHex());
        assertEquals(account.getPublicKey(),bitcoinjECKey.getPublicKeyAsHex());
        Address bitcoinjAddress = Address.fromKey(NetworkParameters.fromID(NetworkParameters.ID_MAINNET),bitcoinjECKey, Script.ScriptType.P2PKH);
        assertEquals(account.getAddress(),bitcoinjAddress.toString());
    }


    private static String randomPrivateKey() {
        StringBuilder result = new StringBuilder();
        for(int i=0; i<64; i++) {
            result.append(Integer.toHexString(new Random().nextInt(16)));
        }
        return result.toString();
    }





    private static BigInteger decodePrivateKey0Proxy(String privateKey) {
        try {
            Method method1 = Class.forName("com.xingkaichun.helloworldblockchain.crypto.AccountUtil").getDeclaredMethod("decodePrivateKey0", String.class);
            method1.setAccessible(true);
            return (BigInteger) method1.invoke(AccountUtil.class, privateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static byte[] decodePublicKey0Proxy(String publicKey) {
        try {
            Method method1 = Class.forName("com.xingkaichun.helloworldblockchain.crypto.AccountUtil").getDeclaredMethod("decodePublicKey0", String.class);
            method1.setAccessible(true);
            return (byte[]) method1.invoke(AccountUtil.class, publicKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static byte[] signature0Proxy(BigInteger bigIntegerPrivateKey, byte[] input) {
        try {
            Method method1 = Class.forName("com.xingkaichun.helloworldblockchain.crypto.AccountUtil").getDeclaredMethod("signature0", BigInteger.class,byte[].class);
            method1.setAccessible(true);
            return (byte[]) method1.invoke(AccountUtil.class, bigIntegerPrivateKey,input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static boolean verifySignature0Proxy(byte[] pub, byte[] rawData, byte[] signature) {
        try {
            Method method1 = Class.forName("com.xingkaichun.helloworldblockchain.crypto.AccountUtil").getDeclaredMethod("verifySignature0", byte[].class,byte[].class,byte[].class);
            method1.setAccessible(true);
            return (boolean) method1.invoke(AccountUtil.class, pub,rawData,signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}