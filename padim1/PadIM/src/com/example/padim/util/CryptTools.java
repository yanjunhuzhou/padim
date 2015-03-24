package com.example.padim.util;



import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
 

public class CryptTools
{
  public static byte[] encrypt(String content, String password)
  {
    try
    {
      SecretKeySpec key = new SecretKeySpec(password.getBytes(), "DES");
      Cipher cipher = Cipher.getInstance("DES");
      byte[] byteContent = content.getBytes("utf-8");
      cipher.init(1, key);
      byte[] result = cipher.doFinal(byteContent);
      return result;
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      e.printStackTrace();
    } catch (InvalidKeyException e) {
      e.printStackTrace();
    } catch (IllegalBlockSizeException e) {
      e.printStackTrace();
    } catch (BadPaddingException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static String encrypt(String content, String password, boolean lineSep)
  {
    return Base64.encodeToString(encrypt(content, password), lineSep);
  }

  public static byte[] decrypt(String content, String password)
  {
    byte[] result = null;
    try {
      SecretKeySpec key = new SecretKeySpec(password.getBytes(), "DES");
      Cipher cipher = Cipher.getInstance("DES");
      byte[] byteContent = Base64.decode(content);
      cipher.init(2, key);
      result = cipher.doFinal(byteContent);
    } catch (NoSuchAlgorithmException e) {
      result = null;
      e.printStackTrace();
    } catch (NoSuchPaddingException e) {
      result = null;
      e.printStackTrace();
    } catch (InvalidKeyException e) {
      result = null;
      e.printStackTrace();
    } catch (IllegalBlockSizeException e) {
      result = null;
      e.printStackTrace();
    } catch (BadPaddingException e) {
      result = null;
      e.printStackTrace();
    }
    return result;
  }

  public static String decrypt(String content, String password, boolean lineSep)
    throws UnsupportedEncodingException
  {
    byte[] str = decrypt(content, password);
    if (str != null) {
      return new String(str);
    }
    return null;
  }
}