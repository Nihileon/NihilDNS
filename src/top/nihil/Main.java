package top.nihil;

import lombok.Data;
import lombok.extern.java.Log;

import java.net.InetSocketAddress;

@Log
@Data
public class Main {
    private static String remoteDNS = "10.3.9.4";
    private static String hostFilePath = "./hosts";

    public static String getRemoteDNS() {
        return remoteDNS;
    }

    public static String getHostFilePath() {
        return hostFilePath;
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            log.warning(args[0]);
            if (args[0].equals("-d")) {
                remoteDNS = args[1];
                hostFilePath = args[2];
            } else if (args[0].equals("-dd")) {
                remoteDNS = args[1];
            }
        }
        log.warning(String.format("DNS Relay Server Start, IPAddress:%s, host File Path: %s", remoteDNS, hostFilePath));
        DNSRelayServer server = new DNSRelayServer(
                new InetSocketAddress("0.0.0.0", 53),
                new InetSocketAddress(remoteDNS, 53),
                new Hosts(hostFilePath)
        );
    }

}
