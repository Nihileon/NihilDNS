package top.nihil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class MessageDataInputStreamTest {
    MessageDataInputStream in;

    @BeforeEach
    void before() throws FileNotFoundException {
        in = new MessageDataInputStream(new FileInputStream("./MessageDataInputStreamTest.bin"));
    }

    @AfterEach
    void after() throws IOException {
        in.close();
    }

    @Test
    void parseDomainName() throws IOException, NoSuchMethodException {
        Class c = in.getClass();
        Method parseDomainNameMethod = c.getDeclaredMethod("parseDomainName", String.class);
        parseDomainNameMethod.setAccessible(true);
        Object[] parameter = {null};
        try {
            assertEquals("blog.nihil.top", parseDomainNameMethod.invoke(in, parameter));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void readDomainName() throws IOException {
        assertEquals("blog.nihil.top", in.readDomainName());
    }

}