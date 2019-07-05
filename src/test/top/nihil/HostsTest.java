package test.top.nihil;

import org.junit.jupiter.api.Test;
import top.nihil.Hosts;

import static org.junit.jupiter.api.Assertions.*;

class HostsTest {
    @Test
    void testParse(){
        String hostPath = "./hosts";
        Hosts host = new Hosts(hostPath);
        assertEquals("0.0.0.0", host.getHostMap().get("2qq.cn"));
        assertEquals("11.111.11.111",host.getHostMap().get("test1"));
        assertEquals("210.242.125.98",host.getHostMap().get("spreadsheets9.google.com"));
    }
}