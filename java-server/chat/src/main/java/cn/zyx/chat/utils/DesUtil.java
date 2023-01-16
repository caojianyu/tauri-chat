package cn.zyx.chat.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.SecureRandom;

/**
 * @author caojianyu
 * @version 1.0.0
 * @date 2022-12-13 15:45
 * @email jieni_cao@foxmail.com
 */
public class DesUtil {

    private static Key key;

    private static final String KEY_STR = "7a138772683a7462b9053d8aede7ff36";
    private static final String CHARSET_NAME = "UTF-8";
    private static final String ALGORITHM = "DES";


    static {
        try {
            //生成DES算法对象
            KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM);
            //运用SHA1安全策略
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            //设置上密钥种子
            secureRandom.setSeed(KEY_STR.getBytes());
            //初始化基于SHA1的算法对象
            generator.init(secureRandom);
            //生成密钥对象
            key = generator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /***
     * 获取加密的信息
     * @param str
     * @return
     */
    public static String getEncryptString(String str) {
        //基于BASE64编码，接收byte[]并转换成String
        BASE64Encoder encoder = new BASE64Encoder();
        try {
            //按utf8编码
            byte[] bytes = str.getBytes(CHARSET_NAME);
            //获取加密对象
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            //初始化密码信息
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //加密
            byte[] byteArr = cipher.doFinal(bytes);
            //byte[]to encode好的String 并返回
            return encoder.encode(byteArr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /***
     * 获取解密之后的信息
     * @param str
     * @return
     */
    public static String getDecryptString(String str) {
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            //将字符串decode成byte[]
            byte[] bytes = decoder.decodeBuffer(str);
            //获取解密对象
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            //初始化解密信息
            cipher.init(Cipher.DECRYPT_MODE, key);
            //解密
            byte[] byteArr = cipher.doFinal(bytes);

            return new String(byteArr, CHARSET_NAME);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}