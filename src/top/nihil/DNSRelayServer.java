package top.nihil;


import com.company.DNS;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log
@Accessors(chain = true)
@AllArgsConstructor
public class DNSRelayServer {
    public static InetSocketAddress listenAddress, remoteDNS;
    private Hosts hosts;

    public static DatagramSocket socket;

    public DNSRelayServer(InetSocketAddress listenAddress, InetSocketAddress remoteDNS, Hosts hosts) {
        DNSRelayServer.listenAddress = listenAddress;
        DNSRelayServer.remoteDNS = remoteDNS;
        this.hosts = hosts;
        try {
            socket = new DatagramSocket(53, listenAddress.getAddress());
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    public void start() throws IOException {


        ExecutorService servicePool = Executors.newCachedThreadPool();

        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        while (true) {
            try {
                socket.receive(packet);
                servicePool.execute(new DNSRelayServerThread(packet, hosts, remoteDNS));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
