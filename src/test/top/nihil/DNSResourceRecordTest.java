package test.top.nihil;

import org.junit.jupiter.api.Test;
import top.nihil.DNSResourceRecord;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DNSResourceRecordTest {
    @Test
    void toByteArray() throws IOException {
        DNSResourceRecord drr = new DNSResourceRecord(0xc00c,0x0005,0x0001,0x0bbe,0x18,"time-osx.g.aaplimg.com");
        byte[] result = drr.toByteArray();
        DataInputStream in = new DataInputStream(new FileInputStream("./DNSAnswer.bin"));
        byte[] bytes = new byte[36];
        in.read(bytes);
        for(int i=0;i<bytes.length;i++){
            assertEquals(bytes[i],result[i]);
        }
    }
}
