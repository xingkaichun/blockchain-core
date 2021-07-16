package com.xingkaichun.helloworldblockchain.crypto;

import com.xingkaichun.helloworldblockchain.crypto.model.Account;
import com.xingkaichun.helloworldblockchain.util.LogUtil;
import com.xingkaichun.helloworldblockchain.util.StringUtil;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * 账户工具类
 *
 * @author 邢开春 409060350@qq.com
 */
public class AccountUtil {

    private static final ECDomainParameters CURVE;
    private static final SecureRandom SECURE_RANDOM;
    private static final boolean COMPRESSED = true;
    private static final BigInteger HALF_CURVE_ORDER;


    private static final byte VERSION = 0x00;

    static {
        JavaCryptographyExtensionProviderUtil.addBouncyCastleProvider();
    }

    static {
        X9ECParameters params = SECNamedCurves.getByName("secp256k1");
        CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
        SECURE_RANDOM = new SecureRandom();
        HALF_CURVE_ORDER = CURVE.getN().shiftRight(1);
    }

    /**
     * 随机生成一个账户
     */
    public static Account randomAccount() {
        try {
            ECKeyPairGenerator generator = new ECKeyPairGenerator();
            ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(CURVE, SECURE_RANDOM);
            generator.init(keygenParams);
            AsymmetricCipherKeyPair keypair = generator.generateKeyPair();
            ECPrivateKeyParameters ecPrivateKeyParameters = (ECPrivateKeyParameters) keypair.getPrivate();
            ECPublicKeyParameters pubParams = (ECPublicKeyParameters) keypair.getPublic();
            BigInteger bigIntegerPrivateKey = ecPrivateKeyParameters.getD();
            byte[] bytesPublicKey = pubParams.getQ().getEncoded(COMPRESSED);
            String privateKey = encodePrivateKey0(bigIntegerPrivateKey);
            String publicKey = encodePublicKey0(bytesPublicKey);
            String publicKeyHash = publicKeyHashFromPublicKey(publicKey);
            String address = addressFromPublicKey(publicKey);
            Account account = new Account(privateKey,publicKey,publicKeyHash,address);
            return account;
        } catch (Exception e) {
            LogUtil.error("生成账户失败。",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 私钥生成账户
     */
    public static Account accountFromPrivateKey(String privateKey) {
        try {
            BigInteger bigIntegerPrivateKey = decodePrivateKey0(privateKey);
            byte[] bytesPublicKey = publicKeyFromPrivateKey0(bigIntegerPrivateKey);

            String publicKey = encodePublicKey0(bytesPublicKey);
            String publicKeyHash = publicKeyHashFromPublicKey(publicKey);
            String address = addressFromPublicKey(publicKey);
            Account account = new Account(privateKey,publicKey,publicKeyHash,address);
            return account;
        } catch (Exception e) {
            LogUtil.error("从私钥恢复账户失败。",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 公钥生成地址
     */
    public static String addressFromPublicKey(String publicKey) {
        try {
            byte[] bytesPublicKey = ByteUtil.hexStringToBytes(publicKey);
            return base58AddressFromPublicKey0(bytesPublicKey);
        } catch (Exception e) {
            LogUtil.error("公钥生成地址失败。",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 公钥哈希生成地址
     */
    public static String addressFromPublicKeyHash(String publicKeyHash) {
        try {
            byte[] bytesPublicKeyHash = ByteUtil.hexStringToBytes(publicKeyHash);
            return base58AddressFromPublicKeyHash0(bytesPublicKeyHash);
        } catch (Exception e) {
            LogUtil.error("公钥哈希生成地址失败。",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 公钥生成公钥哈希
     */
    public static String publicKeyHashFromPublicKey(String publicKey) {
        try {
            byte[] bytesPublicKey = decodePublicKey0(publicKey);
            byte[] bytesPublicKeyHash = publicKeyHashFromPublicKey0(bytesPublicKey);
            return ByteUtil.bytesToHexString(bytesPublicKeyHash);
        } catch (Exception e) {
            LogUtil.error("公钥生成公钥哈希失败。",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 地址生成公钥哈希
     */
    public static String publicKeyHashFromAddress(String address) {
        try {
            byte[] bytesAddress = Base58Util.decode(address);
            byte[] bytesPublicKeyHash = ByteUtil.copy(bytesAddress, 1, 20);
            return ByteUtil.bytesToHexString(bytesPublicKeyHash);
        } catch (Exception e) {
            LogUtil.error("地址生成公钥哈希失败。",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 签名
     */
    public static String signature(String privateKey, byte[] bytesMessage) {
        try {
            BigInteger bigIntegerPrivateKey = decodePrivateKey0(privateKey);
            byte[] bytesSignature = signature0(bigIntegerPrivateKey,bytesMessage);
            return ByteUtil.bytesToHexString(bytesSignature);
        } catch (Exception e) {
            LogUtil.debug("签名出错。");
            throw new RuntimeException(e);
        }
    }

    /**
     * 验证签名
     */
    public static boolean verifySignature(String publicKey, byte[] bytesMessage, byte[] bytesSignature) {
        try {
            byte[] bytesPublicKey = decodePublicKey0(publicKey);
            return verifySignature0(bytesPublicKey,bytesMessage,bytesSignature);
        }catch(Exception e) {
            LogUtil.debug("验证签名出错。");
            return false;
        }
    }

    /**
     * 格式化私钥
     * 前置填零，返回[长度为64位][十六进制字符串][形式的]私钥
     */
    public static String formatPrivateKey(String privateKey) {
        //私钥长度是256bit，64位十六进制的字符串数，如果私钥的长度不够，这里进行前置补零进行格式化。
        return StringUtil.prefixPadding(privateKey,64,"0");
    }

    /**
     * 是否是合法的P2PKH地址
     */
    public static boolean isPayToPublicKeyHashAddress(String address) {
        try {
            byte[] bytesAddress = Base58Util.decode(address);
            byte[] bytesPublicKeyHash = new byte[20];
            ByteUtil.copyTo(bytesAddress, 1, 20, bytesPublicKeyHash, 0);
            String base58Address = addressFromPublicKeyHash(ByteUtil.bytesToHexString(bytesPublicKeyHash));
            return base58Address.equals(address);
        }catch (Exception e){
            LogUtil.debug("地址["+address+"]不是base58格式的地址。");
            return false;
        }
    }


    /**
     * 公钥生成公钥哈希
     * 对公钥进行两次哈希(第一次采用SHA256算法进行哈希，第二次采用RipeMD160算法进行哈希)得到的结果，就是公钥哈希
     */
    private static byte[] publicKeyHashFromPublicKey0(byte[] publicKey) {
        byte[] bytesPublicKeyHash = Ripemd160Util.digest(Sha256Util.digest(publicKey));
        return bytesPublicKeyHash;
    }
    /**
     * 由原始私钥推导出原始公钥
     */
    private static byte[] publicKeyFromPrivateKey0(BigInteger bigIntegerPrivateKey) {
        byte[] bytePublicKey = CURVE.getG().multiply(bigIntegerPrivateKey).getEncoded(COMPRESSED);
        return bytePublicKey;
    }
    /**
     * 由编码私钥解码出原始私钥
     */
    private static BigInteger decodePrivateKey0(String privateKey) {
        BigInteger bigIntegerPrivateKey = new BigInteger(privateKey,16);
        return bigIntegerPrivateKey;
    }
    /**
     * 由编码公钥解码出原始公钥
     */
    private static byte[] decodePublicKey0(String publicKey) {
        byte[] bytesPublicKey = ByteUtil.hexStringToBytes(publicKey);
        return bytesPublicKey;
    }
    /**
     * 将原始私钥进行编码操作，生成编码私钥
     */
    private static String encodePrivateKey0(BigInteger bigIntegerPrivateKey) {
        String hexPrivateKey = bigIntegerPrivateKey.toString(16);
        return formatPrivateKey(hexPrivateKey);
    }

    /**
     * 将原始公钥进行编码操作，生成编码公钥
     */
    private static String encodePublicKey0(byte[] bytesPublicKey) {
        String publicKey = ByteUtil.bytesToHexString(bytesPublicKey);
        return publicKey;
    }

    /**
     * 签名
     */
    private static byte[] signature0(BigInteger privateKey, byte[] message) {
        try {
            ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
            ECPrivateKeyParameters ecPrivateKeyParameters = new ECPrivateKeyParameters(privateKey, CURVE);
            signer.init(true, ecPrivateKeyParameters);
            BigInteger[] signature = signer.generateSignature(message);
            BigInteger s = signature[1];
            if (!isCanonical(s)) {
                s = CURVE.getN().subtract(s);
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DERSequenceGenerator seq = new DERSequenceGenerator(bos);
            seq.addObject(new ASN1Integer(signature[0]));
            seq.addObject(new ASN1Integer(s));
            seq.close();
            return bos.toByteArray();
        } catch (Exception e) {
            LogUtil.debug("签名出错。");
            throw new RuntimeException(e);
        }
    }

    /**
     * 验证签名
     */
    private static boolean verifySignature0(byte[] publicKey, byte[] message, byte[] signature) {
        try {
            ECDSASigner signer = new ECDSASigner();
            ECPublicKeyParameters ecPublicKeyParameters = new ECPublicKeyParameters(CURVE.getCurve().decodePoint(publicKey), CURVE);
            signer.init(false, ecPublicKeyParameters);
            ASN1InputStream decoder = new ASN1InputStream(signature);
            DLSequence seq = (DLSequence) decoder.readObject();
            ASN1Integer r = (ASN1Integer) seq.getObjectAt(0);
            ASN1Integer s = (ASN1Integer) seq.getObjectAt(1);
            if (!isCanonical(s.getValue())) {
                return false;
            }
            decoder.close();
            return signer.verifySignature(message, r.getValue(), s.getValue());
        } catch (Exception e) {
            LogUtil.debug("验证签名，出错。");
            return false;
        }
    }

    /**
     * 公钥生成base58格式地址
     */
    private static String base58AddressFromPublicKey0(byte[] bytesPublicKey) {
        byte[] publicKeyHash = publicKeyHashFromPublicKey0(bytesPublicKey);
        return base58AddressFromPublicKeyHash0(publicKeyHash);
    }

    /**
     * 公钥哈希生成base58格式地址
     */
    private static String base58AddressFromPublicKeyHash0(byte[] bytesPublicKeyHash) {

        //地址版本号(1个字节)与公钥哈希(20个字节)
        byte[] bytesVersionAndPublicKeyHash = ByteUtil.concatenate(new byte[]{VERSION},bytesPublicKeyHash);

        //地址校验码(4个字节)
        byte[] bytesCheckCode = ByteUtil.copy(Sha256Util.doubleDigest(bytesVersionAndPublicKeyHash), 0, 4);

        //地址(25个字节)=地址版本号(1个字节)+公钥哈希(20个字节)+地址校验码(4个字节)
        byte[] bytesAddress = ByteUtil.concatenate(bytesVersionAndPublicKeyHash,bytesCheckCode);

        //用Base58编码地址
        String base58Address = Base58Util.encode(bytesAddress);
        return base58Address;
    }

    /**
     * 这里是为了解决交易延展性攻击。
     * Returns true if the S component is "low", that means it is below {@link ECKey#HALF_CURVE_ORDER}. See <a
     * href="https://github.com/bitcoin/bips/blob/master/bip-0062.mediawiki#Low_S_values_in_signatures">BIP62</a>.
     * 参考：bitcoinj-core-0.15.10.jar org.bitcoinj.core.ECKey.isCanonical()
     */
    private static boolean isCanonical(BigInteger s) {
        return s.compareTo(HALF_CURVE_ORDER) <= 0;
    }
}