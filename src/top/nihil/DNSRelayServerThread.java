package top.nihil;

import lombok.Data;
import lombok.extern.java.Log;

import java.io.IOException;
import java.net.*;

@Log
@Data
public class DNSRelayServerThread implements Runnable {
    byte[] data;
    InetAddress address;
    int port;
    Hosts hosts;
    InetSocketAddress remoteDNS;

    DNSRelayServerThread(DatagramPacket packet, Hosts hosts, InetSocketAddress remoteDNS) {
        data = packet.getData();
        address = packet.getAddress();
        port = packet.getPort();
        this.hosts = hosts;
        this.remoteDNS = remoteDNS;
    }

    @Override
    public void run() {

        DNSMessage receiveMessage = new DNSMessage(data);
        String ipAddress = hosts.getHostMap().getOrDefault(receiveMessage.getQuestion().getQNAME(), "");
        if (!ipAddress.equals("") && receiveMessage.getHeader().getQDCOUNT() == 1) {
            DNSMessage responseMessage = new DNSMessage();
            int flags = 0;
            if (ipAddress.equals("0.0.0.0")) {
                flags = 0x8580;
                log.info(String.format("%s is forbidden", receiveMessage.getQuestion().getQNAME()));
            } else {
                flags = 0x8583;
                log.info(String.format("host %s is in the host file", receiveMessage.getQuestion().getQNAME()));
            }
            responseMessage.setHeader(
                    receiveMessage.getHeader().getID(),
                    flags,
                    receiveMessage.getHeader().getQDCOUNT(),
                    1,
                    1,
                    0);

            responseMessage.setQuestion(receiveMessage.getQuestion());

            responseMessage.addAnswer(0xc00c,
                    receiveMessage.getQuestion().getQTYPE(),
                    receiveMessage.getQuestion().getQCLASS(),
                    3600 * 24,
                    4,
                    ipAddress);

            responseMessage.addAuthority(0xc00c,
                    6,
                    receiveMessage.getQuestion().getQCLASS(),
                    3600 * 24,
                    0,
                    null);

            byte[] responseBytes = responseMessage.getResponseByteArray();
            DatagramPacket responsePacket = new DatagramPacket(
                    responseBytes, responseBytes.length, address, port);
            synchronized (DNSRelayServer.socket) {
                try {
                    DNSRelayServer.socket.send(responsePacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                DatagramSocket remoteDNSSocket = new DatagramSocket();
                //超时
                remoteDNSSocket.setSoTimeout(2000);
//                log.info(String.format("Can't find the host %s", receiveMessage.getQuestion().getQNAME()));
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, remoteDNS.getAddress(), remoteDNS.getPort());
                remoteDNSSocket.send(sendPacket);
                byte[] receivedData = new byte[1024];
                DatagramPacket remoteReceivePacket = new DatagramPacket(receivedData, receivedData.length);
                try {
                    remoteDNSSocket.receive(remoteReceivePacket);
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                }
                DatagramPacket responsePacket = new DatagramPacket(receivedData, receivedData.length, address, port);
                synchronized (DNSRelayServer.socket) {
                    DNSRelayServer.socket.send(responsePacket);
                }
                remoteDNSSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
