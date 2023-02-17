package jasypt;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;

public class TestJasypt {

  public static void main(String[] args) {
//    StandardPBEStringEncryptor allows us to make bidirectional encryption, in which we can indicate different values:the algorithm to use, the
//    IvGenerator, and most importantly the master key (password) to encrypt and decrypt.
    StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
    standardPBEStringEncryptor.setPassword(
        "Demo_Pwd!2020");// (MANDATORY value) It is the Master key, where we have to indicate which
                         // is the key on which we will start to perform this encryption/decryption
    standardPBEStringEncryptor.setAlgorithm("PBEWithHMACSHA512AndAES_256");
    standardPBEStringEncryptor.setIvGenerator(
        new RandomIvGenerator()); //The algorithms of type PBEWithDigestAndAES that are supported by Java Cryptography Extension aka JCE,
                                  // need an initialization vector aka IV. This must also be random and used only once.
    String result = standardPBEStringEncryptor.encrypt("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"); //the final part, the encryption
    System.out.println(result);
    System.out.println(standardPBEStringEncryptor.decrypt(result));

  }
}
