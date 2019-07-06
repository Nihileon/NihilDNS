package top.nihil;

import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.Map;

@Log
public class Converter {
    private final Map<Integer, String> compressedDomainName = new HashMap<>();

    public static int byteArrayToUnsignedShort(byte[] bytes) {
        return Converter.byteArrayToUnsignedShort(bytes, 0);
    }

    static int byteArrayToUnsignedShort(byte[] bytes, int offset) {
        int temp = (bytes[offset] & 0xff) << 8;
        temp = temp | (bytes[offset + 1] & 0xff);
        return temp;
    }

    public static long byteArrayToUnsignedInt(byte[] bytes) {
        return Converter.byteArrayToUnsignedInt(bytes, 0);
    }


    static long byteArrayToUnsignedInt(byte[] bytes, int offset) {
        long temp = (bytes[offset] & 0xff) << 8;
        temp = (temp | (bytes[offset + 1] & 0xff)) << 8;
        temp = (temp | (bytes[offset + 2] & 0xff)) << 8;
        temp = temp | (bytes[offset + 3] & 0xff);
        return temp;
    }

    public static String byteArrayToString(byte[] bytes, int left, int right) {
        String s1;
        return new String(bytes, left, right);
    }

    public static String byteArrayToDomainName(String domainName, byte[] bytes, int offset) {
        int partLength = bytes[offset] & 0xff;
        offset++;
        if (partLength >= 0b11000000) {
            int pos = ((partLength & 0b00111111) << 8) | bytes[offset];
            return Converter.byteArrayToDomainName(domainName, bytes, pos);
        } else if (partLength == 0) {
            return domainName;
        } else {
            byte[] partName = new byte[partLength];
            System.arraycopy(bytes, offset, partName, 0, partLength);
            offset += partLength;
            if (domainName == null) {
                return Converter.byteArrayToDomainName("" + new String(partName), bytes, offset);
            } else {
                return Converter.byteArrayToDomainName(domainName + "." + new String(partName), bytes, offset);
            }
        }
    }

    static byte[] shortToByteArray(int i) {
        byte[] bytes = new byte[2];
        bytes[1] = (byte) (i & 0xff);
        bytes[0] = (byte) ((i >> 8) & 0xff);
        return bytes;
    }

    static byte[] intToByteArray(long l) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte) (l & 0xff);
        bytes[2] = (byte) ((l >> 8) & 0xff);
        bytes[1] = (byte) ((l >> 16) & 0xff);
        bytes[0] = (byte) ((l >> 24) & 0xff);
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
