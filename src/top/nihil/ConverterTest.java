package top.nihil;

import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ConverterTest {

    @Test
    void byteArrayToShort() {
    }

    @Test
    void byteArrayToInt() {
    }

    @Test
    void byteArrayToString() {
    }

    @Test
    void shortToByteArray() {
    }

    @Test
    void intToByteArray() {
    }

    @Test
    void ipv4ToByteArray() {
        byte[] ip = new byte[4];
        ip[0] = (byte) (0x11 & 0xff);
        ip[1] = (byte) (0xfd & 0xff);
        ip[2] = (byte) (0x54 & 0xff);
        ip[3] = (byte) (0x7b & 0xff);
        byte[] ipToArray = Converter.ipv4ToByteArray("17.253.84.123");
        assertEquals(ip[0], ipToArray[0]);
        assertEquals(ip[3], ipToArray[3]);
    }

    @Test
    void domainNameToByteArray() throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream("./MessageDataInputStreamTest.bin"));
        byte[] bytes = new byte[16];
        in.read(bytes);
        byte[] result = Converter.domainNameToByteArray("blog.nihil.top");
        assertEquals(0x6f, result[13]);
        for (int i = 0; i < 16; i++) {
            assertEquals(bytes[i], result[i]);
        }
    }

    @Test
    void byteArrayToDomainName() throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream("./MessageDataInputStreamTest.bin"));
        byte[] bytes = new byte[16];
        in.read(bytes);
        String result = Converter.byteArrayToDomainName(null, bytes, 0);
        assertTrue(result.equals("blog.nihil.top"));
    }

}