package top.nihil;

import static org.junit.jupiter.api.Assertions.*;

class HostsTest {
    void testParse(){
        String hostPath = "./hosts";
        Hosts host = new Hosts(hostPath);
//        assertEquals("test1", host.find("11.111.11.111"));
    }
}