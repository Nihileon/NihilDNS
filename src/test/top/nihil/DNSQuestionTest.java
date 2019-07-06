package test.top.nihil;

import org.junit.jupiter.api.Test;
import top.nihil.DNSQuestion;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DNSQuestionTest {

    @Test
    void getQTYPEName() {
    }

    @Test
    void toByteArray() throws IOException {

        DNSQuestion dq = new DNSQuestion("time-macos.apple.com",0x01,0x01);
        byte[] result = dq.toByteArray();
        DataInputStream in = new DataInputStream(new FileInputStream("./DNSQuestion.bin"));
        byte[] bytes = new byte[26];
        in.read(bytes);
        for(int i=0;i<bytes.length;i++){
            assertEquals(bytes[i],result[i]);
        }
    }
}
