package top.nihil;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    Main mainClass = new Main();

    @Test
    void defaultParameter() {

        assertEquals("10.3.9.4", mainClass.getRemoteDNS());
        assertEquals("./hosts", mainClass.getHostFilePath());
    }

    @Test
    void dParameter() {
        String[] args = new String[3];
        args[0] = "-d";
        args[1] = "192.168.0.1";
        args[2] = "~/desktop/hosts";
        mainClass.main(args);
        assertEquals("192.168.0.1", mainClass.getRemoteDNS());
        assertEquals("~/desktop/hosts", mainClass.getHostFilePath());
    }
    @Test
    void ddParameter(){
        String[] args = new String[2];
        args[0] = "-dd";
        args[1] = "202.99.96.68";
        mainClass.main(args);
        assertEquals("202.99.96.68", mainClass.getRemoteDNS());
        assertEquals("./hosts", mainClass.getHostFilePath());
    }
}