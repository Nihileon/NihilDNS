package top.nihil;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

public class Converter {
    private Map<Integer, String> compressedDomainName = new HashMap<>();

    public static short byteArrayToShort(byte[] bytes) {
        int temp = (bytes[1] & 0xff) << 8;
        temp = temp | (bytes[0] & 0xff);
        return (short) temp;
    }

    public static int byteArrayToInt(byte[] bytes) {
        int temp = (bytes[3] & 0xff) << 8;
        temp = (temp | (bytes[2] & 0xff)) << 8;
        temp = (temp | (bytes[1] & 0xff)) << 8;
        temp = temp | (bytes[0] & 0xff);
        return temp;
    }

    public static String byteArrayToString(byte[] bytes, int left, int right) {
        String s1;
        return new String(bytes, left, right);
    }

    public static String byteArrayToDomainName(String domainName, byte[] bytes, int offset) {

        int partLength = bytes[offset];
        offset++;
        if (partLength >= 0b11000000) {
            int pos = ((partLength & 0b00111111) << 8) | bytes[offset];
            return byteArrayToDomainName(domainName, bytes, pos);
        } else if (partLength == 0) {
            return domainName;
        } else {
            byte[] partName = new byte[partLength];
            System.arraycopy(bytes, offset, partName, 0, partLength);
            offset += partLength;
            if (domainName == null) {
                return byteArrayToDomainName("" + new String(partName), bytes, offset);
            } else {
                return byteArrayToDomainName(domainName + "." + new String(partName), bytes, offset);
            }
        }
    }

    public static byte[] shortToByteArray(short s) {
        byte[] bytes = new byte[2];
        bytes[1] = (byte) (s & 0xff);
        bytes[0] = (byte) ((s >> 8) & 0xff);
        return bytes;
    }

    public static byte[] intToByteArray(int i) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte) (i & 0xff);
        bytes[2] = (byte) ((i >> 8) & 0xff);
        bytes[1] = (byte) ((i >> 16) & 0xff);
        bytes[0] = (byte) ((i >> 24) & 0xff);
        return bytes;
    }

    public static byte[] ipv4ToByteArray(String ipv4) {
        String[] ipStrings = ipv4.split("\\.");
        assert ipStrings.length == 4;
        byte[] ipBytes = new byte[4];
        for (int i = 0; i < ipStrings.length; i++) {
            int num = Integer.parseInt(ipStrings[i]);
            if (num > 127) {
                ipBytes[i] = (byte) (num - 256);
            } else {
                ipBytes[i] = (byte) num;
            }
        }
        return ipBytes;
    }

    public static byte[] domainNameToByteArray(String domainName) {
        int length = domainName.length();
        String[] domainNameStrings = domainName.split("\\.");
        byte[] domainNameBytes = new byte[length + 2];
        int offset = 0;
        for (String s : domainNameStrings) {
            domainNameBytes[offset] = (byte) (s.length() & 0xff);
            offset++;
            System.arraycopy(s.getBytes(), 0, domainNameBytes, offset, s.length());
            offset += s.length();
        }
        domainNameBytes[offset] = 0x00;
        return domainNameBytes;
    }
}
