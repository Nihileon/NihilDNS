package test.top.nihil;

import org.junit.jupiter.api.Test;
import top.nihil.DNSHeader;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DNSHeaderTest {

    @Test
    void headerToStream() throws IOException {

        DataInputStream inputStream = new DataInputStream(new FileInputStream("./DNSHeaderTest.bin"));
        DNSHeader dnsHeader = new DNSHeader(0xe226,0x8180,1,1,1,0);
        assertEquals(0xe226, dnsHeader.getID());
        assertEquals(0x8180, dnsHeader.getFlags());
        assertEquals(0, dnsHeader.getOPCODE());
        assertEquals(1, dnsHeader.getQDCOUNT());
        assertEquals(1, dnsHeader.getANCOUNT());
        assertEquals(1, dnsHeader.getNSCOUNT());
        assertEquals(0, dnsHeader.getARCOUNT());
    }

}