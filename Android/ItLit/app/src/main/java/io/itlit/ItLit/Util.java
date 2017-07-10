package io.itlit.ItLit;

import java.security.MessageDigest;

public class Util {
    private static final String pre = "io.itlit.ItLit.";
    public static String userprefs() { return pre + "USER"; }

    public static String phonify(String number) {
        String result = "";
        for (char c : number.toCharArray()) {
            if ("0123456789".contains(Character.toString(c))) {
                result += c;
            }
        }
        if (result.charAt(0) == '1') {
            result = result.substring(1);
        }
        return result;
    }

    public static String sha256(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data.getBytes());
            return bytesToHex(md.digest());
        }
        catch (Exception e) {
            return "";
        }
    }
    private static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }
}
