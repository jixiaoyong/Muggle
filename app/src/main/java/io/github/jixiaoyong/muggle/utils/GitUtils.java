package io.github.jixiaoyong.muggle.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import io.github.jixiaoyong.muggle.Constants;

/**
 * Created by jixiaoyong1995@gmail.com
 * Data: 2019/4/22.
 * Description: 用Git工具类
 */
public class GitUtils {

    /**
     * 获取应用授权链接
     *
     * @return
     */
    public static String getOAuth2Url() {
        String randomState = UUID.randomUUID().toString();
        return Constants.OAUTH2_URL +
                "?client_id=" + Constants.MUGGLE_CLIENT_ID +
                "&scope=" + Constants.OAUTH2_SCOPE +
                "&state=" + randomState;
    }


    /**
     * @param filePath 要加密的字符串
     * @return 加密的字符串
     * SHA1加密
     */
    public static String gitSHA1(String filePath) {
        try {
            byte[] fileBytes = FileByteArraytils.toByteArray(filePath);
            byte[] gitBytes = getGitBytes(fileBytes);
            return SHA1(gitBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @param bytes 要加密的字符串
     * @return 加密的字符串
     * SHA1加密
     * https://www.jianshu.com/p/244216d1616b
     */
    public static String SHA1(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest
                    .getInstance("SHA-1");
            digest.update(bytes);
            byte[] messageDigest = digest.digest();
            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            // 字节数组转换为 十六进制 数
            for (byte b : messageDigest) {
                String shaHex = Integer.toHexString(b & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


    //git hash原理参考这篇文章：http://www.importnew.com/29052.html
    private static byte[] getGitBytes(byte[] fileBytes) {
        byte[] header = ("blob " + fileBytes.length + "\0").getBytes();
        int byteLen = header.length + fileBytes.length;
        byte[] result = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(byteLen);
        try {
            byteArrayOutputStream.write(header);
            byteArrayOutputStream.write(fileBytes);
            result = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
