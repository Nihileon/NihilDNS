package top.nihil;


import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.java.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log
@Accessors(chain = true)
@AllArgsConstructor
class DNSRelayServer {
    private static InetSocketAddress listenAddress, remoteDNS;
    private Hosts hosts;
    static DatagramSocket socket;
    public static final Object mLock = new Object();

    DNSRelayServer(InetSocketAddress listenAddress, InetSocketAddress remoteDNS, Hosts hosts) {
        DNSRelayServer.listenAddress = listenAddress;
        DNSRelayServer.remoteDNS = remoteDNS;
        this.hosts = hosts;
        try {
            socket = new DatagramSocket(53, listenAddress.getAddress());
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    void start() {
        ExecutorService servicePool = Executors.newCachedThreadPool();
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        while (true) try {
            socket.receive(packet);
            servicePool.execute(new DNSRelayServerThread(packet, hosts, remoteDNS));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
