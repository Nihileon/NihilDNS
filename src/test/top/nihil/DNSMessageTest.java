package test.top.nihil;

import org.junit.jupiter.api.Test;
import top.nihil.DNSMessage;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DNSMessageTest {
    @Test
    void constructor() throws IOException {
        DataInputStream inputStream = new DataInputStream(new FileInputStream("./DNSMessage.bin"));
        byte[] bytes = new  byte[38];
        inputStream.read(bytes);
        DNSMessage message = new DNSMessage(bytes);
        assertTrue(message.getQuestion().getQNAME().equals("logs.leetcode-cn.com"));
        assertEquals(0x9371, message.getHeader().getID());
        assertEquals( 0x0100,message.getHeader().getFlags());
        assertEquals(1, message.getHeader().getQDCOUNT());
    }
}