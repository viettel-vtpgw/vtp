package com.viettel.vtpgw.util;

public class Common {

    public final static int FORMAT_MSISDN_STANDARD_84XX = 0;
    public final static int FORMAT_MSISDN_VN_VIEW_0XX = 1;
    public final static int FORMAT_MSISDN_SHORT_XXX = 2;

    public static String formatPhoneNumber(String msisdn, int format) {
        String s;
        if (msisdn == null || msisdn.length() <= 1) {
            return null;	// invalid
        }

        char ch = msisdn.charAt(0);
        char ch2 = msisdn.charAt(1);
        if (ch == '0') {
            if (format == FORMAT_MSISDN_VN_VIEW_0XX) {
                return msisdn;
            }
            s = msisdn.substring(1, msisdn.length());
        } else if (ch == '8' && ch2 == '4') {
            if (format == FORMAT_MSISDN_STANDARD_84XX) {
                return msisdn;
            }
            s = msisdn.substring(2, msisdn.length());
        } else {
            if (format == FORMAT_MSISDN_SHORT_XXX) {
                return msisdn;
            }
            s = msisdn;
        }

        if (format == FORMAT_MSISDN_STANDARD_84XX) {
            s = "84" + s;
        } else if (format == FORMAT_MSISDN_SHORT_XXX) {
            return s;
        } else {
            s = "0" + s;
        }

        return s;
    }

    public static String[] split(String str, String regex) {
        String[] tokens = str.split(regex);
        String[] newTokens;
        String last = str.substring(str.length() - 1);
        if (last != null && regex.contains(last)) {
            newTokens = new String[tokens.length + 1];
            java.lang.System.arraycopy(tokens, 0, newTokens, 0, tokens.length);
            newTokens[newTokens.length - 1] = "";
            return newTokens;
        } else {
            return tokens;
        }
    }
}
