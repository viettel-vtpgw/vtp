package com.viettel.vtpgw.util;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.MultiMap;

public class NumberUtils {
  private static final Logger LOG = LogManager.getLogger(NumberUtils.class);
  private static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray();
  public static final int NUMBER_LEN_MAX = 15;

  public static final int NUMBER_LEN_MIN = 8;
  public static final String ip(int b1,int b2,int b3,int b4){
  	return new StringBuilder().append(b1).append('.').append(b2).append('.').append(b3).append('.').append(b4).toString();
  }
  public static final String encode64(byte[] sArr) {
    // Check special case
    int sLen = sArr != null ? sArr.length : 0;
    if (sLen == 0) {
      return "";
    }

    int eLen = (sLen / 3) * 3; // Length of even 24-bits.
    int cCnt = ((sLen - 1) / 3 + 1) << 2; // Returned character count
    cCnt -= (eLen + 3 - sLen) % 3;
    // Length of returned array.
    int dLen = cCnt;
    char[] dArr = new char[dLen];

    // Encode even 24-bits
    int d = 0;
    int s = 0;
    while ( s < eLen) {
      // Copy next three bytes into lower 24 bits of int, paying
      // attention to sign.
      int i = (sArr[s++] & 0xff) << 16 | (sArr[s++] & 0xff) << 8 | (sArr[s++] & 0xff);

      // Encode the int into four chars
      dArr[d++] = CA[(i >>> 18) & 0x3f];
      dArr[d++] = CA[(i >>> 12) & 0x3f];
      dArr[d++] = CA[(i >>> 6) & 0x3f];
      dArr[d++] = CA[i & 0x3f];
    }

    // Pad and encode last bits if source isn't even 24 bits.
    int left = sLen - eLen; // 0 - 2.
    if (left > 0) {
      // Prepare the int
      int i = ((sArr[eLen] & 0xff) << 10) | (left == 2 ? ((sArr[sLen - 1] & 0xff) << 2) : 0);

      // Set last four chars
      dArr[d++] = CA[i >> 12];
      dArr[d++] = CA[(i >>> 6) & 0x3f];
      if (left == 2) {
        dArr[d] = CA[i & 0x3f];
      }
    }
    return new String(dArr);
  }

  public static String generateUUID() {
    UUID uuid = UUID.randomUUID();
    return toString(uuid);
  }

  public static Long getLongFromMultiMap(MultiMap map, String key) {
    String value = map.get(key);
    if (value != null) {
      try {
        return new Long(value);
      } catch (NumberFormatException e) {
      }
    }
    return null;
  }

  public static boolean isNumber(String sample) {
    try {
      Long.parseLong(sample);
      return true;
    } catch (NumberFormatException e) {
      LOG.debug(e);
    }
    return false;
  }

  public static boolean isNumberString(String str, int length) {
    if (str == null || str.length() != length)
      return false;
    for (int i = str.length() - 1; i >= 0; i--) {
      char ch = str.charAt(i);
      if (ch < '0' || ch > '9')
        return false;
    }
    return true;
  }

  public static boolean isPhoneNumber(String sample) {
    try {
      if (!isNumber(sample)) {
        return false;
      }
      if ((sample.length() > NUMBER_LEN_MAX) || (sample.length() < NUMBER_LEN_MIN)) {
        return false;
      }
      return true;
    } catch (NumberFormatException e) {
      LOG.debug(e);
    }
    return false;
  }

  public static String parseMSISDN(String s) {
    String sample = s;
    if (sample == null) {
      return sample;
    }
    sample = sample.trim();
    if (sample.startsWith("+")) {
      sample = sample.substring(1);
    }
    if (!sample.startsWith("84")) {
      if (sample.startsWith("0")) {
        sample = "84" + sample.substring(1);
      } else {
        sample = "84" + sample;
      }
    }
    return sample;
  }

  public static void setLongToMultiMap(MultiMap map, String key, Long value) {
    if (value != null) {
      map.add(key, value.toString());
    }
  }

  public static String toString(UUID uuid) {
    long l = uuid.getLeastSignificantBits();
    long m = uuid.getMostSignificantBits();
    byte[] buff = new byte[16];
    buff[0] = (byte) (l & 0xFF);
    l >>= 8;
    buff[1] = (byte) (l & 0xFF);
    l >>= 8;
    buff[2] = (byte) (l & 0xFF);
    l >>= 8;
    buff[3] = (byte) (l & 0xFF);
    l >>= 8;
    buff[4] = (byte) (l & 0xFF);
    l >>= 8;
    buff[5] = (byte) (l & 0xFF);
    l >>= 8;
    buff[6] = (byte) (l & 0xFF);
    l >>= 8;
    buff[7] = (byte) (l & 0xFF);

    buff[8] = (byte) (m & 0xFF);
    m >>= 8;
    buff[9] = (byte) (m & 0xFF);
    m >>= 8;
    buff[10] = (byte) (m & 0xFF);
    m >>= 8;
    buff[11] = (byte) (m & 0xFF);
    m >>= 8;
    buff[12] = (byte) (m & 0xFF);
    m >>= 8;
    buff[13] = (byte) (m & 0xFF);
    m >>= 8;
    buff[14] = (byte) (m & 0xFF);
    m >>= 8;
    buff[15] = (byte) (m & 0xFF);
    return encode64(buff);
  }

}
