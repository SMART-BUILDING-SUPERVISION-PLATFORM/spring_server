package snust.sbsp.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class CryptoUtil {

  @Value("${crypto.key-spec-algorithm}")
  String keySpecAlg;

  @Value("${crypto.algorithm}")
  String alg;

  @Value("${crypto.aes-iv}")
  String aesIv;

  @Value("${crypto.aes-key}")
  String aesKey;

  public String encrypt(String plainText) {

    String encryptedCode = "";

    try {
      Cipher cipher = Cipher.getInstance(alg);
      SecretKeySpec keySpec = new SecretKeySpec(aesKey.getBytes(), keySpecAlg);
      IvParameterSpec ivParameterSpec = new IvParameterSpec(aesIv.getBytes());
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
      byte[] target = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
      encryptedCode = Base64.getEncoder().encodeToString(target);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return encryptedCode;
  }

  public String decrypt(String cipherText) {

    String decryptedPassword = "";

    try {
      Cipher cipher = Cipher.getInstance(alg);
      SecretKeySpec keySpec = new SecretKeySpec(aesKey.getBytes(), keySpecAlg);
      IvParameterSpec ivParameterSpec = new IvParameterSpec(aesIv.getBytes());
      cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
      byte[] target = Base64.getDecoder().decode(cipherText);
      byte[] decrypted = cipher.doFinal(target);
      decryptedPassword = new String(decrypted);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return decryptedPassword;
  }
}
